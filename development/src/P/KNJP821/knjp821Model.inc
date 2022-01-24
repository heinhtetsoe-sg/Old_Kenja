<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp821Model extends Model {
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
    function knjp821Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
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
            $tables = array();
            $tables = array("1" => "COLLECT_SGL_SCHREGNO_MAPPING_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $header["SCHREGNO"] = "学籍番号";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
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
        $query = $this->deleteQueryErr(PROGRAMID);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //ＣＳＶ取込
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //最大登録件数チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    $cnt = knjp821Query::insertQueryCsv($this, $this->data_arr);
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

            $checkdata["GRADE"]      = array(2,      "HISSU",    "",   "学年");
            $checkdata["SCHREGNO"]   = array(8,      "",         "",   "学籍番号");
            $checkdata["SCHREGNO_J"] = array(8,      "",         "",   "生徒個人番号(J)");
            $checkdata["SCHREGNO_H"] = array(9,      "",         "",   "生徒個人番号(H)");
            $checkdata["SIGEL_NO_J"] = array(20,     "",         "",   "SIGEL用学籍番号(J)");
            $checkdata["SIGEL_NO_H"] = array(20,     "",         "",   "SIGEL用学籍番号(H)");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('COLLECT_SGL_SCHREGNO_MAPPING_DAT');
            while (!feof ($fp)) {
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
                    $data["GRADE"]    = $tmp[44];
                    $data["SCHREGNO"] = $tmp[39];

                    if ($data["GRADE"] <= 3) {
                        $data["SCHREGNO_J"] = $tmp[39];
                        $data["SIGEL_NO_J"] = $tmp[0];
                    } else {
                        $data["SCHREGNO_H"] = $tmp[39];
                        $data["SIGEL_NO_H"] = $tmp[0];
                    }

                    $dataCnt = 79;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }

            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1) {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //COLLECT_SGL_SCHREGNO_MAPPING_DAT にその生徒が登録されているのかチェックする
            if ($key == "SCHREGNO") {
                if (($data["SCHREGNO"] != "") && (0 == $db->getOne(knjp821Query::getMappingCnt($data)))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjp821Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }
}
?>
