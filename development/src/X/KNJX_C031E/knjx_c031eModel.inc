<?php

require_once('for_php7.php');
class knjx_c031eModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx_c031eModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
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

        $this->field = array("OUTPUT"               =>  VARS::post("OUTPUT"),               //出力取込種別
                             "SELECT_CLASS_TYPE"    =>  VARS::post("SELECT_CLASS_TYPE"),    //学級タイプ選択
                             "YEAR"                 =>  VARS::post("YEAR"),                 //年度一覧
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //年組一覧
                             "GROUP_HR_CLASS"       =>  VARS::post("GROUP_HR_CLASS"),       //複式クラス
                             "MONTH"                =>  VARS::post("MONTH"),                //対象月
                             "HEADER"               =>  VARS::post("HEADER"),               //ヘッダ有
                             "SHORI_MEI"            =>  VARS::post("SHORI_MEI")             //処理名
                             );

        if (VARS::get("SEND_PRGID")) {
            $this->field["GRADE_HR_CLASS"] = (VARS::get("SEND_hr_class"))       ? str_replace("-", "", VARS::get("SEND_hr_class")) : $this->field["GRADE_HR_CLASS"];
            $this->field["GROUP_HR_CLASS"] = (VARS::get("SEND_GROUP_HR_CLASS")) ? VARS::get("SEND_GROUP_HR_CLASS") : $this->field["GROUP_HR_CLASS"];
            $this->field["MONTH"]          = (VARS::get("SEND_month"))          ? VARS::get("SEND_month")          : $this->field["MONTH"];
        }

        $this->grade = substr($this->field["GRADE_HR_CLASS"], 0, 2);

        $this->cmd  = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            if (VARS::post("selectSchoolKind")) {
                $this->selectSchoolKind = VARS::post("selectSchoolKind");
            } elseif (VARS::get("selectSchoolKind")) {
                $this->selectSchoolKind = VARS::get("selectSchoolKind");
            }
        }
        $this->file = VARS::file("FILE");

        $this->c001 = array();
        $this->c001[4] = "SICK";
        $this->c001[5] = "NOTICE";
        $this->c001[6] = "NONOTICE";

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx_c031eQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_c031eQuery::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            //A004
            $query = knjx_c031equery::getA004();
            $this->titleValA004 = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->titleValA004[$row["NAMECD2"]] = $row["NAME1"];
            }
            $result->free();

            //C001
            $query = knjx_c031equery::getC001();
            $this->titleValC001 = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->titleValC001[$row["NAMECD2"]] = $row["NAME1"];
            }
            $result->free();

            //C002
            $query = knjx_c031equery::getC002();
            $this->titleValC002 = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->titleValC002[$row["NAMECD2"]] = $row["NAME1"];
            }
            $result->free();

            $checkdata = array();
            $checkdata["YEAR"]          = array(4,"年度");
            $checkdata["MONTH"]         = array(2,"対象月");
            $checkdata["SEMESTER"]      = array(1,"学期");
            $checkdata["SCHREGNO"]      = array(8,"学籍番号");
            $checkdata["LESSON"]        = array(3,"授業日数");
            $checkdata["OFFDAYS"]       = array(3,"休学日数");
            $checkdata["ABROAD"]        = array(3,"留学日数");
            $checkdata["ABSENT"]        = array(3,"公欠日数");
            $checkdata["SUSPEND"]       = array(3,$this->titleValC001["2"]);
            $checkdata["KOUDOME"]       = array(3,$this->titleValC001["25"]);
            $checkdata["VIRUS"]         = array(3,$this->titleValC001["19"]);
            $checkdata["MOURNING"]      = array(3,"忌引");
            $checkdata["SICK"]          = array(3,$this->titleValC001["4"]);
            $checkdata["NOTICE"]        = array(3,$this->titleValC001["5"]);
            $checkdata["NONOTICE"]      = array(3,$this->titleValC001["6"]);
            $checkdata["LATE"]          = array(3,"遅刻");
            $checkdata["EARLY"]         = array(3,"早退");
            $checkdata["DETAIL_001"]    = array(3,$this->titleValC002["001"]);
            $checkdata["DETAIL_002"]    = array(3,$this->titleValC002["002"]);
            $checkdata["DETAIL_003"]    = array(3,$this->titleValC002["003"]);
            $checkdata["DETAIL_004"]    = array(3,$this->titleValC002["004"]);
            $checkdata["DETAIL_101"]    = array(5,$this->titleValC002["101"]);

            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey2 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('ATTEND_SEMES_DAT');
            $primaryKey2 = $this->getPrimaryKey('ATTEND_SEMES_DETAIL_DAT');
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
                    $data["YEAR"]       = (strlen(trim($tmp[3]))) ? $tmp[3] : "";
                    $data["MONTH"]      = (strlen(trim($tmp[4]))) ? sprintf("%02d", $tmp[4]) : "";
                    $data["SEMESTER"]   = (strlen(trim($tmp[5]))) ? $tmp[5] : "";
                    $data["SCHREGNO"]   = (strlen(trim($tmp[6]))) ? sprintf("%08d", $tmp[6]) : "";
                    $data["LESSON"]     = (strlen(trim($tmp[8]))) ? $tmp[8] : "0";
                    $tmpCnt = 9;
                    $default = ($this->Properties["use_Attend_zero_hyoji"] == "1") ? "" : "0";
                    if ($this->titleValA004["2"]) {
                        $data["OFFDAYS"]    = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValA004["1"]) {
                        $data["ABROAD"]     = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["1"]) {
                        $data["ABSENT"]     = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["2"]) {
                        $data["SUSPEND"]    = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["25"]) {
                        $data["KOUDOME"]    = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["19"]) {
                        $data["VIRUS"]      = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["3"]) {
                        $data["MOURNING"]   = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["4"]) {
                        $data["SICK"]       = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["5"]) {
                        $data["NOTICE"]     = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["6"]) {
                        $data["NONOTICE"]   = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["15"]) {
                        $data["LATE"]       = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["16"]) {
                        $data["EARLY"]      = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC002["001"]) {
                        $data["DETAIL_001"] = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC002["002"]) {
                        $data["DETAIL_002"] = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC002["003"]) {
                        $data["DETAIL_003"] = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC002["004"]) {
                        $data["DETAIL_004"] = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC002["101"]) {
                        $data["DETAIL_101"] = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    $data["SEQ"]            = "";
                    $data["COPYCD"]         = "0";
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $limitErrFlg        = false;    //範囲エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $limitErrMsg        = "";       //範囲エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $masterSep          = "";
        $limitSep           = "";
        $duplicateCsvSep    = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO","YEAR","MONTH","SEMESTER"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("SCHREGNO","YEAR","MONTH","SEMESTER"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            } elseif ($this->field["SHORI_MEI"] == "1") {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                //範囲チェック
                if ($key == "DETAIL_101" && $data[$key] > 999.9) {
                    $returnFlg = $this->errSet($limitErrFlg, $limitErrMsg, "※範囲:", $limitSep, $val[1]);
                }
            }
        }

        //存在チェック（在籍）
        if (1 > $db->getOne(knjx_c031eQuery::checkRegdDat($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
        }

        //存在チェック（対象月）
        $schoolKind = $db->getOne(knjx_c031eQuery::getSchoolKind2($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]));
        if (!$db->getOne(knjx_c031eQuery::selectMonthQuery($data["YEAR"], $data["MONTH"], $this, $schoolKind))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された年月のデータがありません。");
        }

        //権限チェック
        if ($this->auth == DEF_UPDATE_RESTRICT) {
            if (!$db->getOne(knjx_c031eQuery::checkRegdHdat($this, $data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "そのクラスの情報は取り込めません。");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$limitErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_c031eQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダー（エラー）
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //A004
        $query = knjx_c031equery::getA004();
        $this->titleValA004 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleValA004[$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

        //C001
        $query = knjx_c031equery::getC001();
        $this->titleValC001 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleValC001[$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

        //C002
        $query = knjx_c031equery::getC002();
        $this->titleValC002 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleValC002[$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

        //ヘッダー
        $header = array();
        $header_rei = array();
        $header["HR_NAME"]              = "学級";
        $header_rei["HR_NAME"]          =  "1年1組";
        $header["ATTENDNO"]             = "No.";
        $header_rei["ATTENDNO"]         =  "001";
        $header["NAME"]                 = "氏名";
        $header_rei["NAME"]             =  "アルプ太郎";
        $header["YEAR"]                 = "※年度";
        $header_rei["YEAR"]             =  CTRL_YEAR;
        $header["MONTH"]                = "※対象月";
        $header_rei["MONTH"]            =  "04";
        $header["SEMESTER"]             = "※学期";
        $header_rei["SEMESTER"]         =  "1";
        $header["SCHREGNO"]             = "※学籍番号";
        $header_rei["SCHREGNO"]         =  "12345678";
        $header["APPOINTED_DAY"]        = "締め日";
        $header_rei["APPOINTED_DAY"]    =  "30";
        $header["LESSON"]               = "授業日数";
        $header_rei["LESSON"]           =  "10";

        if ($this->titleValA004["2"]) {
            $header["OFFDAYS"]          = "休学日数";
            $header_rei["OFFDAYS"]      =  "10";
        }
        if ($this->titleValA004["1"]) {
            $header["ABROAD"]           = "留学日数";
            $header_rei["ABROAD"]       =  "10";
        }
        if ($this->titleValC001["1"]) {
            $header["ABSENT"]           = "公欠日数";
            $header_rei["ABSENT"]       =  "10";
        }
        if ($this->titleValC001["2"]) {
            $header["SUSPEND"]          = $this->titleValC001["2"];
            $header_rei["SUSPEND"]      =  "10";
        }
        if ($this->titleValC001["25"]) {
            $header["KOUDOME"]          = $this->titleValC001["25"];
            $header_rei["KOUDOME"]      =  "10";
        }
        if ($this->titleValC001["19"]) {
            $header["VIRUS"]            = $this->titleValC001["19"];
            $header_rei["VIRUS"]        =  "10";
        }
        if ($this->titleValC001["3"]) {
            $header["MOURNING"]         = "忌引";
            $header_rei["MOURNING"]      =  "10";
        }
        if ($this->titleValC001["4"]) {
            $header["SICK"]             = $this->titleValC001["4"];
            $header_rei["SICK"]         =  "10";
        }
        if ($this->titleValC001["5"]) {
            $header["NOTICE"]           = $this->titleValC001["5"];
            $header_rei["NOTICE"]       =  "10";
        }
        if ($this->titleValC001["6"]) {
            $header["NONOTICE"]         = $this->titleValC001["6"];
            $header_rei["NONOTICE"]     =  "10";
        }
        if ($this->titleValC001["15"]) {
            $header["LATE"]             = "遅刻";
            $header_rei["LATE"]         =  "10";
        }
        if ($this->titleValC001["16"]) {
            $header["EARLY"]            = "早退";
            $header_rei["EARLY"]        =  "10";
        }
        if ($this->titleValC002["001"]) {
            $header["DETAIL_001"]       = $this->titleValC002["001"];
            $header_rei["DETAIL_001"]   =  "10";
        }
        if ($this->titleValC002["002"]) {
            $header["DETAIL_002"]       = $this->titleValC002["002"];
            $header_rei["DETAIL_002"]   =  "10";
        }
        if ($this->titleValC002["003"]) {
            $header["DETAIL_003"]       = $this->titleValC002["003"];
            $header_rei["DETAIL_003"]   =  "10";
        }
        if ($this->titleValC002["004"]) {
            $header["DETAIL_004"]       = $this->titleValC002["004"];
            $header_rei["DETAIL_004"]   =  "10";
        }
        if ($this->titleValC002["101"]) {
            $header["DETAIL_101"]       = $this->titleValC002["101"];
            $header_rei["DETAIL_101"]   =  "10.0";
        }
        $header[$this->lastColumn]      = $this->lastColumn;
        $header_rei[$this->lastColumn]  = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("COPYCD"              => "複写区分",
                                    "YEAR"                 => "年度",
                                    "MONTH"                => "対象月",
                                    "SEMESTER"             => "学期",
                                    "SCHREGNO"             => "学籍番号",
                                    "SEQ"                  => "詳細区分");
            $tables = array();
            $tables = array("1" => "ATTEND_SEMES_DAT", "2" => "ATTEND_SEMES_DETAIL_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_c031eQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {              //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "出欠情報入力";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
