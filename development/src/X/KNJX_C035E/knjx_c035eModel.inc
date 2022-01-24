<?php

require_once('for_php7.php');
class knjx_c035eModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;   //コントロールマスタ
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx_c035eModel()
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座
                             "MONTHCD"          =>  VARS::post("MONTHCD"),          //対象月
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        if (VARS::get("SEND_PRGID")) {
            $this->field["SUBCLASSCD"]  = (VARS::get("SEND_SUBCLASSCD"))  ? VARS::get("SEND_SUBCLASSCD")  : $this->field["SUBCLASSCD"];
            $this->field["CHAIRCD"]     = (VARS::get("SEND_CHAIRCD"))     ? VARS::get("SEND_CHAIRCD")     : $this->field["CHAIRCD"];
            $this->field["MONTHCD"]     = (VARS::get("SEND_MONTHCD"))     ? VARS::get("SEND_MONTHCD")     : $this->field["MONTHCD"];
        }

        $month = preg_split("/-/", $this->field["MONTHCD"]);
        $this->field["MONTH"]    = $month[0];
        $this->field["SEMESTER"] = $month[1];

        $this->cmd  = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
            if (VARS::post("selectSchoolKind")) {
                $this->selectSchoolKind = VARS::post("selectSchoolKind");
            } elseif (VARS::get("selectSchoolKind")) {
                $this->selectSchoolKind = VARS::get("selectSchoolKind");
            }
        }
        $this->file = VARS::file("FILE");

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
                    knjx_c035eQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_c035eQuery::deleteQueryCsv($this, $this->data_arr);
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
            $query = knjx_c035equery::getA004($this);
            $this->titleValA004 = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->titleValA004[$row["NAMECD2"]] = $row["NAME1"];
            }
            $result->free();

            //C001
            $query = knjx_c035equery::getC001($this);
            $this->titleValC001 = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->titleValC001[$row["NAMECD2"]] = $row["NAME1"];
            }
            $result->free();

            $checkdata = array();
            $checkdata["CLASSCD"]       =  array(2, "教科コード");
            if ($this->Properties["useCurriculumcd"] == "1") {
                $checkdata["SCHOOL_KIND"]   =  array(2, "学校種別");
                $checkdata["CURRICULUM_CD"] =  array(2, "教育課程コード");
            }
            $checkdata["SUBCLASSCD"]    =  array(6, "科目コード");
            $checkdata["CHAIRCD"]       =  array(7, "講座コード");
            $checkdata["YEAR"]          =  array(4, "年度");
            $checkdata["MONTH"]         =  array(2, "対象月");
            $checkdata["SEMESTER"]      =  array(1, "学期");
            $checkdata["SCHREGNO"]      =  array(8, "学籍番号");
            $checkdata["LESSON"]        =  array(3, "授業時数");
            if ($this->titleValA004["2"]) {
                $checkdata["OFFDAYS"]       =  array(3, "休学時数");
            }
            if ($this->titleValA004["1"]) {
                $checkdata["ABROAD"]        =  array(3, "留学時数");
            }
            if ($this->titleValC001["1"]) {
                $checkdata["ABSENT"]        =  array(3, "公欠時数");
            }
            if ($this->titleValC001["2"]) {
                $suspendName = "出停";
                if ($this->titleValC001["25"] && $this->Properties["useKoudome"] == "true") {
                    $suspendName .= "(法止)";
                }
                $checkdata["SUSPEND"]       =  array(3, $suspendName);
            }
            if ($this->titleValC001["25"] && $this->Properties["useKoudome"] == "true") {
                $checkdata["KOUDOME"]       =  array(3, "出停(交止)");
            }
            if ($this->titleValC001["19"] && $this->Properties["useVirus"] == "true") {
                $checkdata["VIRUS"]         =  array(3, "出停(伝染)");
            }
            if ($this->titleValC001["3"]) {
                $checkdata["MOURNING"]      =  array(3, "忌引");
            }
            if ($this->titleValC001["4"]) {
                $checkdata["SICK"]          =  array(3, $this->titleValC001["4"]);
            }
            if ($this->titleValC001["5"]) {
                $checkdata["NOTICE"]        =  array(3, $this->titleValC001["5"]);
            }
            if ($this->titleValC001["6"]) {
                $checkdata["NONOTICE"]      =  array(3, $this->titleValC001["6"]);
            }
            if ($this->titleValC001["14"]) {
                $checkdata["NURSEOFF"]      =  array(3, "保健室欠課");
            }
            if ($this->titleValC001["15"]) {
                $checkdata["LATE"]          =  array(3, "遅刻");
            }
            if ($this->titleValC001["16"]) {
                $checkdata["EARLY"]         =  array(3, "早退");
            }

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
            $primaryKey1 = $this->getPrimaryKey('ATTEND_SUBCLASS_DAT');
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

                    $tmpCnt = 0;
                    $data = array();
                    $data["CLASSCD"]        =  (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%02d", $tmp[$tmpCnt]) : "";
                    $tmpCnt++;
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $data["SCHOOL_KIND"]    =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : "";
                        $tmpCnt++;
                        $data["CURRICULUM_CD"]  =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : "";
                        $tmpCnt++;
                    }
                    $data["SUBCLASSCD"]     =  (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%06d", $tmp[$tmpCnt]) : "";
                    $tmpCnt = $tmpCnt + 2;
                    $data["CHAIRCD"]        =  (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%07d", $tmp[$tmpCnt]) : "";
                    $tmpCnt = $tmpCnt + 2;
                    $data["YEAR"]           =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : "";
                    $tmpCnt++;
                    $data["MONTH"]          =  (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%02d", $tmp[$tmpCnt]) : "";
                    $tmpCnt++;
                    $data["SEMESTER"]       =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : "";
                    $tmpCnt++;
                    $data["SCHREGNO"]       =  (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%08d", $tmp[$tmpCnt]) : "";
                    $tmpCnt = $tmpCnt + 5;
                    $data["LESSON"]         =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : "";
                    $tmpCnt++;

                    $default = ($this->Properties["use_Attend_zero_hyoji"] == "1") ? "" : "0";

                    if ($this->titleValA004["2"]) {
                        $data["OFFDAYS"]    =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValA004["1"]) {
                        $data["ABROAD"]     =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["1"]) {
                        $data["ABSENT"]     =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["2"]) {
                        $data["SUSPEND"]    =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["25"] && $this->Properties["useKoudome"] == "true") {
                        $data["KOUDOME"]    =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["19"] && $this->Properties["useVirus"] == "true") {
                        $data["VIRUS"]      =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["3"]) {
                        $data["MOURNING"]   =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    $tmpCnt++;
                    if ($this->titleValC001["4"]) {
                        $data["SICK"]       =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["5"]) {
                        $data["NOTICE"]     =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["6"]) {
                        $data["NONOTICE"]   =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["14"]) {
                        $data["NURSEOFF"]   =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["15"]) {
                        $data["LATE"]       =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    if ($this->titleValC001["16"]) {
                        $data["EARLY"]      =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    $data["COPYCD"]         = "0";
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
            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";    //重複(CSV)メッセージ

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $existenceSep = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($this->Properties["useCurriculumcd"] == "1") {
                if (in_array($key, array("SCHREGNO","YEAR","MONTH","SEMESTER","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","CHAIRCD","LESSON"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("SCHREGNO","YEAR","MONTH","SEMESTER","CLASSCD","SUBCLASSCD","CHAIRCD","LESSON"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }

            //数値チェック
            if ($key != "SCHOOL_KIND" && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        //存在チェック（在籍）
        if (1 > $db->getOne(knjx_c035eQuery::checkRegdDat($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"], $this))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $data_each["SCHREGNO"][1]);
        }

        //存在チェック（講座名簿）
        if (1 > $db->getOne(knjx_c035eQuery::checkChairStdDat($data["YEAR"], $data["SEMESTER"], $data["CHAIRCD"], $data["SCHREGNO"]))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "講座名簿");
        }

        //存在チェック（対象月）
        if (!$db->getOne(knjx_c035eQuery::selectMonthQuery($data["YEAR"], $data["MONTH"], $this))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $data_each["MONTH"][1]);
        }

        //権限チェック
        if ($this->auth == DEF_UPDATE_RESTRICT) {
            if (!$db->getOne(knjx_c035eQuery::checkRestrict($this, $data))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "取込対象外");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_c035eQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
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
        $query = knjx_c035equery::getA004($this);
        $this->titleValA004 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleValA004[$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

        //C001
        $query = knjx_c035equery::getC001($this);
        $this->titleValC001 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleValC001[$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

        //ヘッダー
        $header = $header_rei = array();
        $header["CLASSCD"]              = "※教科コード";
        $header_rei["CLASSCD"]          =  "12";
        if ($this->Properties["useCurriculumcd"] == "1") {
            //学校種別ヘッダー例取得
            $school_kind_rei = "";
            $result = $db->query(knjx_c035equery::getSchoolKind());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($school_kind_rei) {
                    $school_kind_rei .= " ";
                }
                $school_kind_rei .= $row["NAME1"].":".$row["ABBV1"];
            }
            if ($school_kind_rei == "") {
                $school_kind_rei = "名称マスタ未設定";
            }
            $result->free();
            $header["SCHOOL_KIND"]          = "※学校種別";
            $header_rei["SCHOOL_KIND"]      =  $school_kind_rei;
            $header["CURRICULUM_CD"]        = "※教育課程コード";
            $header_rei["CURRICULUM_CD"]    =  "1";
        }
        $header["SUBCLASSCD"]           = "※科目コード";
        $header_rei["SUBCLASSCD"]       = "123456";
        $header["SUBCLASSNAME"]         = "科目名";
        $header_rei["SUBCLASSNAME"]     = "数学";
        $header["CHAIRCD"]              = "※講座コード";
        $header_rei["CHAIRCD"]          = "1234567";
        $header["CHAIRNAME"]            = "講座名";
        $header_rei["CHAIRNAME"]        = "数学1-1";
        $header["YEAR"]                 = "※年度";
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header["MONTH"]                = "※対象月";
        $header_rei["MONTH"]            = "04";
        $header["SEMESTER"]             = "※学期";
        $header_rei["SEMESTER"]         = "1";
        $header["SCHREGNO"]             = "※学籍番号";
        $header_rei["SCHREGNO"]         = "12345678";
        $header["HR_NAME"]              = "年組";
        $header_rei["HR_NAME"]          = "1年1組";
        $header["ATTENDNO"]             = "出席番号";
        $header_rei["ATTENDNO"]         = "001";
        $header["NAME"]                 = "氏名";
        $header_rei["NAME"]             = "アルプ太郎";
        $header["APPOINTED_DAY"]        = "締め日";
        $header_rei["APPOINTED_DAY"]    = "30";
        $header["LESSON"]               = "※授業時数";
        $header_rei["LESSON"]           = "10";
        if ($this->titleValA004["2"]) {
            $header["OFFDAYS"]          = "休学時数";
            $header_rei["OFFDAYS"]      = "10";
        }
        if ($this->titleValA004["1"]) {
            $header["ABROAD"]           = "留学時数";
            $header_rei["ABROAD"]       = "10";
        }
        if ($this->titleValC001["1"]) {
            $header["ABSENT"]           = "公欠時数";
            $header_rei["ABSENT"]       = "10";
        }
        if ($this->titleValC001["2"]) {
            $suspendName = "出停";
            if ($this->titleValC001["25"] && $this->Properties["useKoudome"] == "true") {
                $suspendName .= "(法止)";
            }
            $header["SUSPEND"]          = $suspendName;
            $header_rei["SUSPEND"]      = "10";
        }
        if ($this->titleValC001["25"] && $this->Properties["useKoudome"] == "true") {
            $header["KOUDOME"]          = "出停(交止)";
            $header_rei["KOUDOME"]      = "10";
        }
        if ($this->titleValC001["19"] && $this->Properties["useVirus"] == "true") {
            $header["VIRUS"]            = "出停(伝染)";
            $header_rei["VIRUS"]        = "10";
        }
        if ($this->titleValC001["3"]) {
            $header["MOURNING"]         = "忌引";
            $header_rei["MOURNING"]     = "10";
        }
        $header["ATTEND"]           = "出席すべき時数";
        $header_rei["ATTEND"]       = "入力不可";
        if ($this->titleValC001["4"]) {
            $header["SICK"]             = "病欠";
            $header_rei["SICK"]         = "10";
        }
        if ($this->titleValC001["5"]) {
            $header["NOTICE"]           = "事故欠";
            $header_rei["NOTICE"]       = "10";
        }
        if ($this->titleValC001["6"]) {
            $header["NONOTICE"]         = "欠席";
            $header_rei["NONOTICE"]     = "10";
        }
        if ($this->titleValC001["14"]) {
            $header["NURSEOFF"]         = "保健室欠課";
            $header_rei["NURSEOFF"]     = "10";
        }
        if ($this->titleValC001["15"]) {
            $header["LATE"]             = "遅刻";
            $header_rei["LATE"]         = "10";
        }
        if ($this->titleValC001["16"]) {
            $header["EARLY"]            = "早退";
            $header_rei["EARLY"]        = "10";
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
                                    "CLASSCD"              => "教科コード",
                                    "SCHOOL_KIND"          => "学校校種",
                                    "CURRICULUM_CD"        => "教育課程コード",
                                    "SUBCLASSCD"           => "科目コード");
            $tables = array();
            $tables = array("1" => "ATTEND_SUBCLASS_DAT");
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
            //学校マスタ取得
            $knjSchoolMst = $db->getRow(knjx_c035eQuery::getSchoolMst($this), DB_FETCHMODE_ASSOC);

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_c035eQuery::selectMainQuery($this, $knjSchoolMst);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {             //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);

                    //データがない場合、指示画面の値をセットする。
                    if ($row["SUBCLASSCD"] == "") {
                        if ($this->Properties["useCurriculumcd"] == "1") {
                            list($row["CLASSCD"], $row["SCHOOL_KIND"], $row["CURRICULUM_CD"], $row["SUBCLASSCD"]) = preg_split("/-/", $this->field["SUBCLASSCD"]);
                            $row["CHAIRCD"] = $this->field["CHAIRCD"];
                            //講座名、科目名セット
                            $getName = $db->getRow(knjx_c035eQuery::getName($this), DB_FETCHMODE_ASSOC);
                            $row["SUBCLASSNAME"] = $getName["SUBCLASSNAME"];
                            $row["CHAIRNAME"]    = $getName["CHAIRNAME"];
                        } else {
                            $row["CLASSCD"] = substr($this->field["SUBCLASSCD"], 0, 2);
                            $row["SUBCLASSCD"] = $this->field["SUBCLASSCD"];
                        }
                    }
                    if ($row["MONTH"] == "") {
                        list($row["MONTH"], $row["SEMESTER"]) = preg_split("/-/", $this->field["MONTHCD"]);
                    }
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {     //エラー
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
