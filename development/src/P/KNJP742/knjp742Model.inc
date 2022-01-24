<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjp742Model extends Model {
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
    function knjp742Model() {
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
        $this->seq        = '001'; // 現状は"001"固定

        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        $this->field = array("INPUT"            =>  VARS::post("INPUT"),             //取込種別（1:引落し, 2:返金）
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),       //校種
                             "HEADER"           =>  VARS::post("HEADER")             //ヘッダ有
                             );

        $db = Query::dbCheckOut();
        //学校銀行情報取得
        $this->bankRow = array();
        $query = knjp742Query::getCollectSchoolBankMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->bankRow[$row["SCHOOL_KIND"]] = $row;
        }

        //校種セット
        $this->schKindArr = array();
        $query = knjp742Query::getSchoolKind();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->schKindArr[$row["YEAR"].$row["GRADE"]] = $row["SCHOOL_KIND"];
        }
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->file = VARS::file("FILE");

        $this->keiyakuSyubetsu    = ($this->field["INPUT"] == "1") ? "30": "09";
    }

    function &getMainModel() {
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        //エラー
        $headerDuplicate = array("SCHOOLCD"     => "学校コード",
                                 "SCHOOL_KIND"  => "学校校種",
                                 "YEAR"         => "年度",
                                 "SCHREGNO"     => "学籍番号",
                                 "SLIP_NO"      => "伝票番号",
                                 "COLLECT_L_CD" => "会計科目コード",
                                 "COLLECT_M_CD" => "会計項目コード",
                                 "PLAN_YEAR"    => "入金計画年",
                                 "PLAN_MONTH"   => "入金計画月",
                                 "OUTGO_L_CD"   => "出金科目",
                                 "OUTGO_M_CD"   => "出金項目",
                                 "REQUEST_NO"   => "支出伺番号");
        $tables = array();
        if ($this->field["INPUT"] == "1") {
            $tables = array("1" => "COLLECT_SLIP_PLAN_M_DAT");
        } else {
            $tables = array("1" => "LEVY_REQUEST_OUTGO_DAT");
        }
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            $contents .= implode($headerErr, ",")."\n";
        }
        $contents .= implode($header_err, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //エラー
        $query = $this->selectCsvErrQuery(PROGRAMID);

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    //ＣＳＶ取込
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knjp742Query::insertQueryCsv($this, $this->data_arr);
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
            $checkdata["RECORD_DIV"]       = array(1,      "",         "",   "レコード区分");
            $checkdata["SCHREGNO"]         = array(8,      "",         "",   "学籍番号");
            $checkdata["PLAN_MONEY"]       = array(10,     "",         "",   "請求金額");
            $checkdata["REQUEST_GK"]       = array(10,     "",         "",   "返金額");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            if ($this->field["INPUT"] == "1") {
                $primaryKey1 = array("SCHREGNO", "SCHOOL_KIND", "YEAR", "PLAN_YEAR", "PLAN_MONTH", "LIMIT_MONTH", "ACCOUNTNO", "ACCOUNTNAME");
            } else {
                $primaryKey1 = array("SCHREGNO", "SCHOOL_KIND", "YEAR", "ACCOUNTNO", "ACCOUNTNAME");
            }
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    //入金フォーマット
                    if ($this->field["INPUT"] == "1") {
                        $data["RECORD_DATA"]            = $tmp[0];
                        $data["RECORD_DIV"]             = mb_substr($tmp[0],   0,  1); // レコード区分
                        $data["BANKCD"]                 = mb_substr($tmp[0],   1,  4); // 金融機関コード
                        $data["BANKNAME"]               = mb_substr($tmp[0],   5, 15); // 金融機関名
                        $data["BRANCHCD"]               = mb_substr($tmp[0],  20,  3); // 通常貯金記号(店番)
                        $data["YOBI_1"]                 = mb_substr($tmp[0],  23, 15); // 予備
                        $data["KOUZA_FURIKAE"]          = mb_substr($tmp[0],  38,  2); // 振替口座表示
                        $data["KEIYAKU_SYUBETSU"]       = mb_substr($tmp[0],  40,  2); // 契約種別表示
                        $data["YOBI_2"]                 = mb_substr($tmp[0],  42,  1); // 予備
                        $data["ACCOUNTNO"]              = mb_substr($tmp[0],  43,  7); // 通常貯金番号
                        $data["ACCOUNTNAME"]            = mb_substr($tmp[0],  50, 30); // 氏名
                        $data["PLAN_MONEY"]             = (int)mb_substr($tmp[0],  80, 10); // 請求金額
                        $data["SYOKAI"]                 = mb_substr($tmp[0],  90,  1); // 照会表示
                        $data["GRADE"]                  = mb_substr($tmp[0],  91,  2); // 顧客番号（学年）
                        $data["HR_CLASS"]               = mb_substr($tmp[0],  93,  3); // 顧客番号（組）
                        $data["ATTENDNO"]               = mb_substr($tmp[0],  96,  3); // 顧客番号（番号）
                        $data["SCHREGNO"]               = mb_substr($tmp[0],  99,  8); // 顧客番号（学籍番号）
                        $data["PLAN_YEAR_MONTH"]        = mb_substr($tmp[0], 107,  4); // 顧客番号（月日）
                        $data["RESULT_CD"]              = mb_substr($tmp[0], 111,  1); // 振替結果コード
                        $data["PLAN_YEAR_MONTH_2"]      = mb_substr($tmp[0], 112,  4); // 優先処理（月日）
                        $data["PRIORITY_CD"]            = mb_substr($tmp[0], 116,  2); // 優先処理（コード）
                        $data["PLAN_MONTH"]             = mb_substr($tmp[0], 118,  2); // 補助文言表示
                        // 請求金額(手数料を引く)
                        if ($this->Properties["IncludeBankTransferFee"] == "1") {
                            $data["PLAN_MONEY"] -= $this->bankRow[$this->field["SCHOOL_KIND"]]["BANK_TRANSFER_FEE"];
                        }

                        if ($data["RECORD_DIV"] == "1") {
                            $setHurikae = mb_substr($tmp[0],  107,  1);// 振替回数
                            if ($setHurikae == "1" || $setHurikae == " " || $setHurikae == "") {
                                $setPaidDate = mb_substr($tmp[0],  54,  4); // 請求月日
                            } else if ($setHurikae == "2") {
                                $setPaidDate = mb_substr($tmp[0], 103,  4); // 再振替日
                            } else {
                                $this->warning = "処理不可データです。\\nヘッダレコード（振替回数）。";
                                return;
                            }
                            if ($this->keiyakuSyubetsu != mb_substr($tmp[0], 1, 2)) {//契約種別コードが一致しているか
                                $this->warning = "処理不可データです。\\n入金データか確認して下さい。";
                                return;
                            }
                        }
                        $data["PAID_DATE"] = $setPaidDate;

                        $setPlanYear   = "20".mb_substr($data["PLAN_YEAR_MONTH"], 0, 2);
                        $setSchKind    = $this->schKindArr[CTRL_YEAR.$data["GRADE"]];
                        $setPlanYear   = "20".mb_substr($data["PLAN_YEAR_MONTH"], 0, 2);
                        $setPlanMonth  = mb_substr($data["PLAN_YEAR_MONTH"], 2, 2);
                        $setLimitMonth = mb_substr($data["PAID_DATE"], 0, 2);
                        $data["SCHOOL_KIND"] = $setSchKind;
                        $data["YEAR"]        = CTRL_YEAR;
                        $data["PLAN_YEAR"]   = $setPlanYear;
                        $data["PLAN_MONTH"]  = $setPlanMonth;
                        $data["LIMIT_MONTH"] = $setLimitMonth;
                    //返金フォーマット
                    } else {
                        $data["RECORD_DATA"]            = $tmp[0];
                        $data["RECORD_DIV"]             = mb_substr($tmp[0],   0,  1); // レコード区分
                        $data["BANKCD"]                 = mb_substr($tmp[0],   1,  4); // 金融機関コード
                        $data["BANKNAME1"]              = mb_substr($tmp[0],   5, 15); // 金融機関名１
                        $data["BRANCHCD"]               = mb_substr($tmp[0],  20,  3); // 通常貯金記号(店番)
                        $data["BANKNAME2"]              = mb_substr($tmp[0],  23, 15); // 金融機関名２
                        $data["TEGATANO"]               = mb_substr($tmp[0],  38,  4); // 手形交換所番号
                        $data["YOKINSYUMOKU"]           = mb_substr($tmp[0],  42,  1); // 預金種目
                        $data["ACCOUNTNO"]              = mb_substr($tmp[0],  43,  7); // 通常貯金番号
                        $data["ACCOUNTNAME"]            = mb_substr($tmp[0],  50, 30); // 氏名
                        $data["REQUEST_GK"]             = mb_substr($tmp[0],  80, 10); // 送金金額
                        $data["SINKI_CD"]               = mb_substr($tmp[0],  90,  1); // 新規コード
                        $data["GRADE"]                  = mb_substr($tmp[0],  91,  2); // 顧客番号（学年）
                        $data["HR_CLASS"]               = mb_substr($tmp[0],  93,  3); // 顧客番号（組）
                        $data["ATTENDNO"]               = mb_substr($tmp[0],  96,  3); // 顧客番号（番号）
                        $data["SCHREGNO"]               = mb_substr($tmp[0],  99,  8); // 顧客番号（学籍番号）
                        $data["PLAN_YEAR_MONTH"]        = mb_substr($tmp[0], 107,  4); // 顧客番号（月日）
                        $data["RESULT_CD"]              = mb_substr($tmp[0], 111,  1); // 振替結果コード
                        $data["YOBI"]                   = mb_substr($tmp[0], 112,  6); // 予備
                        $data["PLAN_MONTH"]             = mb_substr($tmp[0], 118,  2); // 補助文言表示

                        if ($data["RECORD_DIV"] == "1") {
                            $setHenkinDate = mb_substr($tmp[0],  54,  4); // 取扱月日
                            if ($this->keiyakuSyubetsu != mb_substr($tmp[0], 1, 2)) {//契約種別コードが一致しているか
                                $this->warning = "処理不可データです。\\n返金データか確認して下さい。";
                                return;
                            }
                        }
                        $data["HENKIN_DATE"] = $setHenkinDate;
                        $setSchKind = $this->schKindArr[CTRL_YEAR.$data["GRADE"]];
                        $data["SCHOOL_KIND"] = $setSchKind;
                        $data["YEAR"]        = CTRL_YEAR;
                    }

                    $dataCnt = 1;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    if ($this->field["INPUT"] == "1") {
                        //振替結果コードが"0"以外は、処理しない。
                        if ($data["RECORD_DIV"] == "2") {
                            if ($data["RESULT_CD"] !== "0") {
                                $line++;
                                continue;
                            }
                        }
                    } else {
                        //振替結果コードが"0"　またはブランク以外は、処理しない。
                        if ($data["RECORD_DIV"] == "2") {
                            $data["RESULT_CD"] = $data["RESULT_CD"] ? $data["RESULT_CD"] : "0";
                            if ($data["RESULT_CD"] !== "0") {
                                $line++;
                                continue;
                            }
                        }
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                }
                $line++;
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $orderErrFlg     = false;    //その他エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $orderErrMsg     = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $existenceSep = "";
        $orderSep     = "";
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

            //2:データレコード(教職員以外)のみ、チェック。※1:ヘッダーレコード 8:トレーラレコード 9:エンドレコード
            if ($data["RECORD_DIV"] == "2" && $data["GRADE"] != "00") {
                //schreg_base_mst
                if (($key == "SCHREGNO") && (0 == $db->getOne(knjp742Query::checkBaseMstSql($data[$key])))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3].$data[$key]);
                }

                //入金フォーマット
                if ($this->field["INPUT"] == "1") {
                    //データと入金計画の金額が一致しているか
                    if (($key == "PLAN_MONEY")) {
                        $chkMoney = $db->getOne(knjp742Query::checkPlanMdat($this, $data));
                        if ($data[$key] != $chkMoney) {
                            $setMessage = "引落予定金額（{$chkMoney}）　"."引落金額（{$data[$key]}）";
                            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー：", $orderSep, $setMessage);
                        }
                    }
                } else {
                    //データと返金伝票の金額が一致しているか
                    if ($key == "REQUEST_GK") {
                        if ($this->bankRow[$this->schKindArr[CTRL_YEAR.$data["GRADE"]]]["BANK_TRANSFER_FEE"] > 0) {
                            $this->tesuRyo = $this->bankRow[$this->schKindArr[CTRL_YEAR.$data["GRADE"]]]["BANK_TRANSFER_FEE"];
                        } else if ($this->bankRow["99"]["BANK_TRANSFER_FEE"] > 0) {
                            $this->tesuRyo = $this->bankRow["99"]["BANK_TRANSFER_FEE"];
                        } else {
                            $this->tesuRyo = "30";
                        }

                        $chk1 = $db->getOne(knjp742Query::checkHenkinDat($this, $data)) - $this->tesuRyo;
                        $chkMoney  = sprintf("%010d", $chk1);
                        if ($data[$key] != $chkMoney) {
                            $setMessage = "返金予定額（{$chkMoney}）　"."返金額（{$data[$key]}）";
                            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー：", $orderSep, $setMessage);
                        }
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjp742Query::insertQueryErr($db, $line, $checkError);
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
