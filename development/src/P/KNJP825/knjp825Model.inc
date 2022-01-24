<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp825Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjp825Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->dateConfirm);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->file = VARS::file("FILE");
    }

    function &getMainModel() {
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    function getDownloadModel() {
        //エラー項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();

        $header_rei = array();

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "2") {
            //エラー
            $headerDuplicate = array("YEAR"         => "年度",
                                     "DIRECT_DEBIT" => "引落し日",
                                     "SEQ"          => "SEQ");
            $tables = array();
            if ($this->dataDiv == "1") {
                $tables = array("1" => "COLLECT_ZENGIN_HEADER_RECORD_DAT");
            } elseif ($this->dataDiv == "2") {
                $tables = array("1" => "COLLECT_ZENGIN_DATA_RECORD_DAT");
            } elseif ($this->dataDiv == "8") {
                $tables = array("1" => "COLLECT_ZENGIN_TRAILER_RECORD_DAT");
            } elseif ($this->dataDiv == "9") {
                $tables = array("1" => "COLLECT_ZENGIN_END_RECORD_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        if($this->field["OUTPUT"] == "2") {
            //エラー
            $query = $this->selectCsvErrQuery(PROGRAMID);
        }

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $array = $header_err;

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($array as $key => $headerName) {
                    $contents .= $comma.$row[$key];
                    $comma = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"] == "2"){        //エラー
                $couse = "エラー出力";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    //エラーデータ削除
    function &getDeleteModel() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //エラーデータの削除
        $query = knjp825Query::deleteQueryErr();
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //ＣＳＶ取込
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        if ($this->dateConfirm) {
            return;
        }

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjp825Query::insertQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //ＣＳＶ取込(日付チェック後)
    function &getExecModelSecond() {
        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjp825Query::insertQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {

            $checkdata["RECORD_DATA"]      = array(120,    "HISSU",    "",   "レコードデータ");
            $checkdata["DATA_DIV"]         = array(1,      "",         "",   "データ区分");
            $checkdata["SCHREGNO"]         = array(8,      "",         "",   "学籍番号");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            //チェック処理用
            $dataRecCnt = $totalDataRecCnt = 0;
            $dataMoney  = $totaldataMoney  = 0;
            $setDirectDebit = '';

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $this->dataDiv = "";
            $primaryKey1   = array();
            $primaryKey2   = array();
            $primaryKey3   = array();
            $primaryKey4   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey("COLLECT_ZENGIN_HEADER_RECORD_DAT");
            $primaryKey2   = $this->getPrimaryKey("COLLECT_ZENGIN_DATA_RECORD_DAT");
            $primaryKey3   = $this->getPrimaryKey("COLLECT_ZENGIN_TRAILER_RECORD_DAT");
            $primaryKey4   = $this->getPrimaryKey("COLLECT_ZENGIN_END_RECORD_DAT");
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = i18n_convert($buffer, i18n_http_output(), "SJIS-win");
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $data = array();
                    $data["RECORD_DATA"]            = $tmp[0];
                    $data["DATA_DIV"]               = mb_substr($tmp[0],   0,  1); // データ区分
                    // ヘッダー・レコード
                    if ($data["DATA_DIV"] == "1") {
                        $data["TYPE_CD"]            = mb_substr($tmp[0],   1,  2); // 種別コード
                        $data["CD_DIV"]             = mb_substr($tmp[0],   3,  1); // コード区分
                        $data["CLIENT_CD"]          = mb_substr($tmp[0],   4, 10); // 依頼人コード
                        $data["CLIENT_NAME"]        = mb_substr($tmp[0],  14, 40); // 委託業者
                        $data["DIRECT_DEBIT"]       = mb_substr($tmp[0],  54,  4); // 引落し日
                        $data["T_BANKCD"]           = mb_substr($tmp[0],  58,  4); // 取引銀行番号
                        $data["T_BANKNAME"]         = mb_substr($tmp[0],  62, 15); // 取引銀行名
                        $data["T_BRANCHCD"]         = mb_substr($tmp[0],  77,  3); // 取引支店番号
                        $data["T_BRANCHNAME"]       = mb_substr($tmp[0],  80, 15); // 取引支店名
                        $data["DEPOSIT_DIV"]        = mb_substr($tmp[0],  95,  1); // 預金種目
                        $data["T_ACCOUNTNO"]        = mb_substr($tmp[0],  96,  7); // 口座番号
                        $data["DUMMY"]              = mb_substr($tmp[0], 103, 17); // ダミー
                        $data["YEAR"]               = CTRL_YEAR;

                        $setDirectDebit = $data["DIRECT_DEBIT"];
                        $this->dataDiv = "1";

                    // データ・レコード
                    } else if ($data["DATA_DIV"] == "2") {
                        $data["BANKCD"]             = mb_substr($tmp[0],   1,  4); // 引落銀行番号
                        $data["BANKNAME"]           = mb_substr($tmp[0],   5, 15); // 引落銀行名
                        $data["BRANCHCD"]           = mb_substr($tmp[0],  20,  3); // 引落支店番号
                        $data["BRANCHNAME"]         = mb_substr($tmp[0],  23, 15); // 引落支店名
                        $data["DUMMY1"]             = mb_substr($tmp[0],  38,  4); // ダミー
                        $data["DEPOSIT_DIV"]        = mb_substr($tmp[0],  42,  1); // 預金種目
                        $data["ACCOUNTNO"]          = mb_substr($tmp[0],  43,  7); // 口座番号
                        $data["ACCOUNTNAME"]        = mb_substr($tmp[0],  50, 30); // 預金者名
                        $data["PLAN_MONEY"]         = mb_substr($tmp[0],  80, 10) - 86; // 引落金額(86円引く)
                        $data["NEW_CD"]             = mb_substr($tmp[0],  90,  1); // 新規コード
                        $data["CUSTOMER_CD"]        = mb_substr($tmp[0],  91, 20); // 顧客番号
                        $data["RESULT_CD"]          = mb_substr($tmp[0], 111,  1); // 振替結果コード
                        $data["DUMMY2"]             = mb_substr($tmp[0], 112,  8); // ダミー

                        $data["SCHREGNO"]           = mb_substr($tmp[0], 103,  8); // 学籍番号
                        $data["YEAR"]               = CTRL_YEAR;
                        $data["SEQ"]                = "";

                        $dataRecCnt++;
                        $dataMoney += $data["PLAN_MONEY"];
                        $this->dataDiv = "2";

                    // トレーラ・レコード
                    } else if ($data["DATA_DIV"] == "8") {
                        $data["TOTAL_CNT"]          = mb_substr($tmp[0],   1,  6); // 合計件数
                        $data["TOTAL_MONEY"]        = mb_substr($tmp[0],   7, 12); // 合計金額
                        $data["TRANSFER_CNT"]       = mb_substr($tmp[0],  19,  6); // 振替済件数
                        $data["TRANSFER_MONEY"]     = mb_substr($tmp[0],  25, 12); // 振替済金額
                        $data["NOT_TRANSFER_CNT"]   = mb_substr($tmp[0],  37,  6); // 振替不能件数
                        $data["NOT_TRANSFER_MONEY"] = mb_substr($tmp[0],  43, 12); // 振替不能金額
                        $data["DUMMY"]              = mb_substr($tmp[0],  55, 65); // ダミー
                        $data["YEAR"]               = CTRL_YEAR;

                        $totalDataRecCnt = $data["TOTAL_CNT"];
                        $totaldataMoney  = $data["TOTAL_MONEY"];
                        $this->dataDiv = "8";

                    // エンド・レコード
                    } else if ($data["DATA_DIV"] == "9") {
                        $data["DUMMY"]          = mb_substr($tmp[0],   1, 119); // ダミー
                        $data["YEAR"]           = CTRL_YEAR;
                        $this->dataDiv = "9";
                    }
                    $data["DIRECT_DEBIT"] = $setDirectDebit; //各テーブルのキー

                    $dataCnt = 1;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    if ($data["DATA_DIV"] == '1') {
                        foreach ($primaryKey1 as $key => $val) {
                            $duplicateCsv1[$line][$val] = $data[$val];
                        }
                    } elseif ($data["DATA_DIV"] == '2') {
                        foreach ($primaryKey2 as $key => $val) {
                            $duplicateCsv1[$line][$val] = $data[$val];
                        }
                    } elseif ($data["DATA_DIV"] == '8') {
                        foreach ($primaryKey3 as $key => $val) {
                            $duplicateCsv1[$line][$val] = $data[$val];
                        }
                    } elseif ($data["DATA_DIV"] == '9') {
                        foreach ($primaryKey4 as $key => $val) {
                            $duplicateCsv1[$line][$val] = $data[$val];
                        }
                    }
                    $line++;
                }
            }

            //ヘッダー・レコードチェック
            if ($setDirectDebit == '') {
                $this->warning = "CSVファイルを確認して下さい。\\nヘッダー・レコードが存在しません。";
                return;
            }

            //合計件数(トレーラ)がデータレコードの数と一致するか
            if ($totalDataRecCnt != $dataRecCnt) {
                $numTotalCnt   = intval($totalDataRecCnt);
                $numCnt        = intval($dataRecCnt);
                $this->warning = "トレーラ・レコードの合計件数[{$numTotalCnt}]が、\\nデータ・レコードの件数[{$numCnt}]と一致しません。";
                return;
            }

            //合計金額(トレーラ)がデータレコードの引落金額の合計と一致するか
            if ($totaldataMoney != $dataMoney) {
                $numTotalMoney = intval($totaldataMoney);
                $numMoney      = intval($dataMoney);
                $this->warning = "トレーラ・レコードの合計金額[{$numTotalMoney}]が、\\nデータ・レコードの引落金額の合計[{$numMoney}]と一致しません";
                return;
            }

            //データが存在するか
            $chkCnt = $db->getOne(knjp825Query::getRowCnt($setDirectDebit));
            if ($chkCnt > 0) {
                $this->dateConfirm = true;
            }

            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $existenceSep = "";
        $lengthSep    = "";
        $duplicateCsvSep = "";

        if ($data["DATA_DIV"] == '1') {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        } elseif ($data["DATA_DIV"] == '2') {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey2, $data);
        } elseif ($data["DATA_DIV"] == '8') {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey3, $data);
        } elseif ($data["DATA_DIV"] == '9') {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey4, $data);
        }
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //長さチェック
            if ($key == "RECORD_DATA") {
                $setLengh = mb_strlen($data[$key]) - 2; // 改行コード分を引く
                if ($setLengh != 120) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3].'['.$setLengh.']');
                }
            }

            //SCHREG_BASE_DAT にその生徒が登録されているのかチェックする
            if ($data["DATA_DIV"] == "2") {
                if (($key == "SCHREGNO") && (0 == $db->getOne(knjp825Query::schregNoCntSql($data[$key])))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjp825Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }
}
?>
