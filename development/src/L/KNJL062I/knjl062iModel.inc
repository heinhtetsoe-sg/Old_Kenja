<?php

class knjl062iModel extends Model
{
    public $schoolKind;
    public $cmd;
    public $field = array();
    public $file;

    /**
     * メンバ変数の初期化
     */
    public function knjl062iModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->schoolKind = VARS::get("URL_SCHOOLKIND");
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //入試制度
        if (VARS::post("APPLICANTDIV")) {
            $this->field["APPLICANTDIV"] = VARS::post("APPLICANTDIV");
        }
        //入試区分
        if (VARS::post("TESTDIV")) {
            $this->field["TESTDIV"] = VARS::post("TESTDIV");
        }

        if (VARS::post("OUTPUT")) {
            $this->field["OUTPUT"] = VARS::post("OUTPUT");
        }
        if (VARS::post("SHORI_MEI")) {
            $this->field["SHORI_MEI"] = VARS::post("SHORI_MEI");
        }
        if (VARS::post("HEADER")) {
            $this->field["HEADER"] = VARS::post("HEADER");
        }
        if (VARS::post("HR_CLASS")) {
            $this->field["HR_CLASS"] = VARS::post("HR_CLASS");
        }

        $this->file = VARS::file("FILE");


        //CSV処理用データ取得
        $db = Query::dbCheckOut();

        //元号情報取得
        $this->nameMstL007 = array();
        $query = knjl062iQuery::getNameMst($this, "L007");

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->nameMstL007[$row["NAMECD2"]] = array("ERA_SYEAR" => $row["NAMESPARE1"],
                                                        "ERA_SDATE" => $row["NAMESPARE2"],
                                                        "ERA_EDATE" => $row["NAMESPARE3"],
                                                 );
        }

        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }


    /**
     * CSV取込処理
    */
    public function &getCsvExecModel()
    {
        if ($this->field["OUTPUT"] == "2") {
            //データ取込
            return $this->getCsvInputModel();
        }
        $this->setWarning("MSG901", "処理区分");
        return false;
    }

    /**
     * CSV出力処理
    */
    public function &getDownloadModel()
    {
        if ($this->field["OUTPUT"] == "1" || $this->field["OUTPUT"] == "4") {
            //ヘッダ出力(見本)
            //データ出力
            return $this->getCsvDownloadModel();
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー出力
            return $this->getErrorCsvDownloadModel();
        }
        $this->setWarning("MSG901", "処理区分");
        return false;
    }

    /**
     * CSV取込
    */
    public function &getCsvInputModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->err_data = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            //データ更新
            $this->entexamApplicantList = array();
            foreach ($this->data_arr as $examNo => $applicant) {
                $this->entexamApplicantList[] = $applicant;
            }
            if ($this->field["SHORI_MEI"] == "1") {
                knjl062iQuery::update($this);
            } else {
                knjl062iQuery::delete($this);
            }

            $this->setMessage("MSG202", "処理済件数　：".count($this->data_arr)."件\\nエラー件数    ：".count($this->err_data)."件\\n");
            if (count($this->data_arr) > 0) {
                return true;
            }
        }
        return false;
    }

    //取込
    public function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]  = array( 4, "入試年度");
            $checkdata["APPLICANTDIV"] = array( 1, "学校種別");
            $checkdata["TESTDIV"]      = array( 1, "入試区分");
            $checkdata["EXAMNO"]       = array(10, "受験番号");

            $checkdata["NAME"]         = array(60, "志願者氏名");
            $checkdata["NAME_KANA"]    = array(60, "志願者氏名カナ");
            $checkdata["SEX"]          = array( 1, "性別");
            $checkdata["BIRTHDAY"]     = array(10, "生年月日");

            $checkdata["ZIPCD"]        = array(  8, "郵便番号");
            $checkdata["ADDRESS1"]     = array(150, "現住所１");
            $checkdata["ADDRESS2"]     = array(150, "現住所２");
            $checkdata["TELNO"]        = array( 14, "電話番号");

            $checkdata["GNAME"]        = array( 60, "保護者氏名");
            $checkdata["GKANA"]        = array( 60, "保護者氏名カナ");
            $checkdata["GZIPCD"]       = array(  8, "保護者・郵便番号");
            $checkdata["GADDRESS1"]    = array(150, "保護者・現住所１");
            $checkdata["GADDRESS2"]    = array(150, "保護者・現住所２");
            $checkdata["GTELNO"]       = array( 14, "保護者・電話番号");
            //中学部入試のみ
            $checkdata["GTELNO2"]      = array( 14, "保護者・携帯電話");

            $checkdata["FS_CD"]        = array( 7, "出身学校コード");
            $checkdata["FS_YM"]        = array( 7, "卒業予定年月");
            $checkdata["FS_GRDDIV"]    = array( 1, "卒業区分");

            //高等部入試のみ
            $checkdata["RANK"]          = array(  1, "高等部志望順位");
            $checkdata["SCHOOLCD1"]     = array(  7, "併願校１学校コード");
            $checkdata["SCHOOLCD2"]     = array(  7, "併願校２学校コード");
            $checkdata["SEND_ZIPCD"]    = array(  8, "送付先・郵便番号");
            $checkdata["SEND_ADDRESS1"] = array(150, "送付先・住所１");
            $checkdata["SEND_ADDRESS2"] = array(150, "送付先・住所２");
            $checkdata["SEND_TELNO"]    = array( 14, "送付先・電話番号");

            // $checkdata["DUMMY"] = array(0,"DUMMY");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl062iQuery::deleteCsvMsgPrgDat();
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);

                    $data = array();
                    $data = $this->getTorikomiData($tmp);

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$data["EXAMNO"]] = $data;
                        $i++;
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

    //中学部入試データ
    public function getTorikomiData($tmp)
    {
        $data = array();
        $data["ENTEXAMYEAR"]   = $tmp[0];
        $data["APPLICANTDIV"]  = $tmp[1];
        $data["TESTDIV"]       = $tmp[2];
        $data["EXAMNO"]        = $tmp[3];
        $data["NAME"]          = $tmp[4];
        $data["NAME_KANA"]     = $tmp[5];
        $data["SEX"]           = $tmp[6];
        $data["BIRTHDAY"]      = $tmp[7];
        $data["ZIPCD"]         = $tmp[8];
        $data["ADDRESS1"]      = $tmp[9];
        $data["ADDRESS2"]      = $tmp[10];
        $data["TELNO"]         = $tmp[11];
        $data["GNAME"]         = $tmp[12];
        $data["GKANA"]         = $tmp[13];
        $data["GZIPCD"]        = $tmp[14];
        $data["GADDRESS1"]     = $tmp[15];
        $data["GADDRESS2"]     = $tmp[16];
        $data["GTELNO"]        = $tmp[17];
        $data["GTELNO2"]       = $tmp[18];
        $data["FS_CD"]         = $tmp[19];
        $data["FS_YM"]         = $tmp[20];
        $data["FS_GRDDIV"]     = $tmp[21];
        $data["RANK"]          = $tmp[22];
        $data["SCHOOLCD1"]     = $tmp[23];
        $data["SCHOOLCD2"]     = $tmp[24];
        $data["SEND_ZIPCD"]    = $tmp[25];
        $data["SEND_ADDRESS1"] = $tmp[26];
        $data["SEND_ADDRESS2"] = $tmp[27];
        $data["SEND_TELNO"]    = $tmp[28];

        //使用しないフィールドはデータが入っていても無視する
        if ($this->field["APPLICANTDIV"] == "1") {
            $data["RANK"]          = "";
            $data["SCHOOLCD1"]     = "";
            $data["SCHOOLCD2"]     = "";
            $data["SEND_ZIPCD"]    = "";
            $data["SEND_ADDRESS1"] = "";
            $data["SEND_ADDRESS2"] = "";
            $data["SEND_TELNO"]    = "";
        } else {
            $data["GTELNO2"]       = "";
        }
        return $data;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $existErrFlg     = false;    //存在エラーフラグ
        $orderErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $existErrMsg     = "";       //存在エラーメッセージ
        $orderErrMsg     = "";       //その他エラーメッセージ
        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $existSep     = "";
        $orderSep     = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            $hissuArray = array("ENTEXAMYEAR", "APPLICANTDIV", "TESTDIV", "EXAMNO", "NAME", "NAME_KANA", "SEX", "BIRTHDAY", "ADDRESS1");
            if ($this->field["TESTDIV"] != "3") {
                $hissuArray[] = "ZIPCD";
            }
            if (in_array($key, $hissuArray)) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("ENTEXAMYEAR", "APPLICANTDIV", "TESTDIV", "EXAMNO", "SEX", "RANK")) && $data[$key] != "") {
                if (!is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }


            //入試年度チェック
            if (in_array($key, array("ENTEXAMYEAR")) && $data[$key] != "") {
                if ((CTRL_YEAR + 1) != $data[$key]) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, $val[1]);
                }
            }
            //入試制度チェック
            if (in_array($key, array("APPLICANTDIV")) && $data[$key] != "") {
                if ($this->field["APPLICANTDIV"] != $data[$key]) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, $val[1]);
                }
            }

            //生年月日の日付型チェック
            if (in_array($key, array("BIRTHDAY")) && $data[$key] != "") {
                $matches = array();
                if (preg_match("/([0-9]{4})[\/]([0-9]{1,2})[\/]([0-9]{1,2})/", $data[$key], $matches) != 1) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, "日付書式".$val[1]);
                } else {
                    if (!checkdate($matches[2], $matches[3], $matches[1])) {
                        $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, "日付不正".$val[1]);
                    }
                }
            }
            //卒業予定年月の日付型チェック
            if (in_array($key, array("FS_YM")) && $data[$key] != "") {
                $matches = array();
                if (preg_match("/([0-9]{4})[\/]([0-9]{1,2})/", $data[$key], $matches) != 1) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, "日付書式".$val[1]);
                } else {
                    if (!checkdate($matches[2], 1, $matches[1])) {
                        $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, "日付不正".$val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existErrMsg.$orderErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
            knjl062iQuery::insertCsvMsgPrgDat($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getCsvDownloadModel()
    {
        //項目
        $header = array();

        $header["ENTEXAMYEAR"]   = "※入試年度";
        $header["APPLICANTDIV"]  = "※入試制度";
        $header["TESTDIV"]       = "※入試区分";
        $header["EXAMNO"]        = "※受験番号";
        $header["NAME"]          = "※志願者氏名";
        $header["NAME_KANA"]     = "※志願者氏名カナ";
        $header["SEX"]           = "※性別";
        $header["BIRTHDAY"]      = "※生年月日";
        $header["ZIPCD"]         = "※郵便番号";
        $header["ADDRESS1"]      = "※現住所１";
        $header["ADDRESS2"]      = "現住所２";
        $header["TELNO"]         = "電話番号";
        $header["GNAME"]         = "保護者氏名";
        $header["GKANA"]         = "保護者氏名カナ";
        $header["GZIPCD"]        = "保護者・郵便番号";
        $header["GADDRESS1"]     = "保護者・現住所１";
        $header["GADDRESS2"]     = "保護者・現住所２";
        $header["GTELNO"]        = "保護者・電話番号";
        $header["GTELNO2"]       = "保護者・携帯電話";
        $header["FS_CD"]         = "出身学校コード";
        $header["FS_YM"]         = "卒業予定年月";
        $header["FS_GRDDIV"]     = "卒業区分";
        $header["RANK"]          = "高等部志望順位";
        $header["SCHOOLCD1"]     = "併願校１学校コード";
        $header["SCHOOLCD2"]     = "併願校２学校コード";
        $header["SEND_ZIPCD"]    = "送付先・郵便番号";
        $header["SEND_ADDRESS1"] = "送付先・住所１";
        $header["SEND_ADDRESS2"] = "送付先・住所２";
        $header["SEND_TELNO"]    = "送付先・電話番号";

        $contents  = "";
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $sample = "";
        $isData = false;
        //データ取得
        $query = knjl062iQuery::selectQuery($this);
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ出力(見本)
            $query = knjl062iQuery::selectSampleQuery($this);
            $sample = "（見本）";
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            foreach ($header as $key => $val) {
                //日付
                if (in_array($key, array("BIRTHDAY", "FS_YM"))) {
                    $row[$key] =  str_replace("-", "/", $row[$key]);
                }
                $data[] = $sep.$row[$key];
            }
            $contents .= implode($data, ",") ."\n";
            $isData = true;
        }
        $result->free();

        //ＣＳＶ出力
        $rtn = false;
        if ($isData) {
            $year = CTRL_YEAR + 1;

            //入試制度名の取得
            $applicantDiv = $this->field["APPLICANTDIV"];
            $query = knjl062iQuery::getApplicantDivName($applicantDiv);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $applicantDivName = $row["NAME1"];
            }
            $result->free();

            //入試区分名の取得
            $testDiv = $this->field["TESTDIV"];
            $query = knjl062iQuery::getEntexamTestDivName($applicantDiv, $testDiv);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $testDivName = $row["TESTDIV_ABBV"];
            }
            $result->free();

            //$fileName = sprintf("%s年度_%s_%s_志願者%s.csv", $year, $applicantDivName, $testDivName, $sample);
            $fileName = sprintf("%s年度入試志願者情報取込（%s%s）%s.csv", $year, $applicantDivName, $testDivName, $sample);
            common::downloadContents($contents, $fileName, "SJIS-win");
            $rtn = true;
        } else {
            $this->warning = "該当データは、ありません。";
        }

        Query::dbCheckIn($db);
        return $rtn;
    }

    /*================================== エラーデータ処理 ==================================*/
    //エラー出力
    public function getErrorCsvDownloadModel()
    {
        //項目
        $header = array(
              "MSGROW"    => "行番号"
            , "MSGREMARK" => "メッセージ"
        );

        $contents  = "";
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $isData = false;
        //データ取得
        $query = knjl062iQuery::selectCsvMsgPrgDat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            // $row["DUMMY"] = "DUMMY";
            foreach ($header as $key => $val) {
                $data[] = $sep.$row[$key];
            }
            $contents .= implode($data, ",") ."\n";
            $isData = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($isData) {
            $fileName = "エラー出力.csv";
            common::downloadContents($contents, $fileName, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
