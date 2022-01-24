<?php

require_once('for_php7.php');
class knjx_c035fModel extends Model
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

    public function knjx_c035fModel()
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
            unset($this->field);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->getPropertiesAll();  //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座
                             "MONTHCD"          =>  VARS::post("MONTHCD"),          //対象月
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        //校種コード
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");
        } elseif ($this->Properties["useSchool_KindField"] == "1") {
            $this->field["SCHOOL_KIND"] = SCHOOLKIND;
        }

        if (VARS::get("SEND_PRGID")) {
            $this->field["SCHOOL_KIND"] = (VARS::get("SEND_SCHOOL_KIND")) ? VARS::get("SEND_SCHOOL_KIND") : $this->field["SCHOOL_KIND"];
            $this->field["SUBCLASSCD"]  = (VARS::get("SEND_SUBCLASSCD"))  ? VARS::get("SEND_SUBCLASSCD")  : $this->field["SUBCLASSCD"];
            $this->field["CHAIRCD"]     = (VARS::get("SEND_CHAIRCD"))     ? VARS::get("SEND_CHAIRCD")     : $this->field["CHAIRCD"];
            $this->field["MONTHCD"]     = (VARS::get("SEND_MONTHCD"))     ? VARS::get("SEND_MONTHCD")     : $this->field["MONTHCD"];
        }

        //課程学科コード
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->field["COURSE_MAJOR"] = VARS::post("COURSE_MAJOR");
        } else {
            $this->field["COURSE_MAJOR"] = '0000';
        }

        $month = preg_split("/-/", $this->field["MONTHCD"]);
        $this->field["MONTH"]    = $month[0];
        $this->field["SEMESTER"] = $month[1];

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        /******************/
        /*  出欠項目取得  */
        /******************/
        //出欠項目一覧
        $this->attendItem = array();
        $this->attendItem["LESSON"]     = array("0000", "",     "授業時数");
        $this->attendItem["OFFDAYS"]    = array("A004", "2",    "");
        $this->attendItem["ABROAD"]     = array("A004", "1",    "");
        $this->attendItem["ABSENT"]     = array("C001", "1",    "");
        $this->attendItem["SUSPEND"]    = array("C001", "2",    "");
        if ($this->Properties["useKoudome"] == "true") {
            $this->attendItem["KOUDOME"]    = array("C001", "25",   "");
        }
        if ($this->Properties["useVirus"] == "true") {
            $this->attendItem["VIRUS"]      = array("C001", "19",   "");
        }
        $this->attendItem["MOURNING"]   = array("C001", "3",    "");
        $this->attendItem["SICK"]       = array("C001", "4",    "");
        $this->attendItem["NOTICE"]     = array("C001", "5",    "");
        $this->attendItem["NONOTICE"]   = array("C001", "6",    "");
        $this->attendItem["NURSEOFF"]   = array("C001", "14",   "");
        $this->attendItem["LATE"]       = array("C001", "15",   "");
        $this->attendItem["EARLY"]      = array("C001", "16",   "");

        $db = Query::dbCheckOut();

        $sk = "";
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            //校種
            $query = knjx_c035fQuery::getNameMstA023($this);
            $getSK = $db->getOne($query);
            $sk = ($this->field["SCHOOL_KIND"]) ? $this->field["SCHOOL_KIND"] : $getSK;
        } elseif ($this->Properties["useSchool_KindField"] == "1") {
            $sk = SCHOOLKIND;
        }

        //権限グループコード
        $this->groupcd = $db->getOne(knjx_c035fQuery::getMinGroupcd($this, $sk));

        //課程学科
        $query = knjx_c035fQuery::getCourseMajor($this, $sk);
        $getCM = $db->getOne($query);
        $cm = ($this->field["COURSE_MAJOR"]) ? $this->field["COURSE_MAJOR"] : $getCM;

        //名称マスタから項目名取得
        $nameMst = array();
        $query = knjx_c035fQuery::getAttendNameList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($this->attendItem as $attend_item => $val) {
                if ($val[0] == $row["NAMECD1"] && $val[1] == $row["NAMECD2"]) {
                    $nameMst[$attend_item] = $row["NAME1"];
                }
            }
        }

        //ADMIN_CONTROL_ATTEND_ITEMNAME_DATから項目名取得
        $itemName = array();
        $query = knjx_c035fQuery::getAdminControlAttendItemnameDat($this, $sk, $cm);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemName[$row["ATTEND_ITEM"]] = $row["ATTEND_ITEMNAME"];
        }

        //出欠管理者コントロール取得
        $this->item_array = $this->itemR_array = array();
        $query = knjx_c035fQuery::getAdminControlAttendDat($this, $sk, $cm);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //項目名
            $label = $this->attendItem[$row["ATTEND_ITEM"]][2];
            if (strlen($nameMst[$row["ATTEND_ITEM"]])) {
                $label = $nameMst[$row["ATTEND_ITEM"]];
            }
            if (strlen($itemName[$row["ATTEND_ITEM"]])) {
                $label = $itemName[$row["ATTEND_ITEM"]];
            }

            if ($row["CONTROL_DIV"] == "1") {
                $this->item_array[$row["SHOWORDER"]] = array("label"    => $label,
                                                             "item"     => $row["ATTEND_ITEM"],
                                                             "input"    => $row["INPUT_FLG"]);
            } else {
                $this->itemR_array[$row["SHOWORDER"]] = array("label"   => $label,
                                                              "key"     => $row["ATTEND_ITEM"],
                                                              "item"    => 'RUISEKI_' . $row["ATTEND_ITEM"],
                                                              "input"   => $row["INPUT_FLG"]);
            }
        }

        Query::dbCheckIn($db);

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
                    knjx_c035fQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_c035fQuery::deleteQueryCsv($this, $this->data_arr);
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
            foreach ($this->item_array as $key => $val) {
                $checkdata[$val["item"]]        =  array(3, $val["label"]);
            }

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
            $primaryKey1   = $this->getPrimaryKey("ATTEND_SUBCLASS_DAT");
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
                    $data["COPYCD"]         = "0";
                    $tmpCnt = $tmpCnt + 5;

                    $default = ($this->Properties["use_Attend_zero_hyoji"] == "1") ? "" : "0";

                    foreach ($this->item_array as $key => $val) {
                        $data[$val["item"]]     =  (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }

                    foreach ($this->itemR_array as $key => $val) {
                        $tmpCnt++;
                    }

                    $checkCnt  = ($this->Properties["useCurriculumcd"] == "1") ? 16 : 14;
                    $checkCnt +=get_count($this->item_array);
                    $checkCnt +=get_count($this->itemR_array);

                    //項目数によるファイルチェック
                    if (get_count($tmp) != $checkCnt) {
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
            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $orderErrFlg        = false;    //固有エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $orderErrMsg        = "";       //固有エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $orderSep        = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($this->Properties["useCurriculumcd"] == "1") {
                if (in_array($key, array("SCHREGNO","YEAR","MONTH","SEMESTER","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","CHAIRCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("SCHREGNO","YEAR","MONTH","SEMESTER","CLASSCD","SUBCLASSCD","CHAIRCD"))) {
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
        if (1 > $db->getOne(knjx_c035fQuery::checkRegdDat($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
        }

        //存在チェック（講座名簿）
        if (1 > $db->getOne(knjx_c035fQuery::checkChairStdDat($data["YEAR"], $data["SEMESTER"], $data["CHAIRCD"], $data["SCHREGNO"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "受講データがありません。");
        }

        //存在チェック（対象月）
        if (!$db->getOne(knjx_c035fQuery::selectMonthQuery($data["YEAR"], $data["MONTH"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された年月のデータがありません。");
        }

        //存在チェック（異動者）
        $idou = $this->getIdouCnt($db, $data);
        if ($idou > 0) {
            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "異動者");
        }

        //権限チェック
        if ($this->auth == DEF_UPDATE_RESTRICT) {
            if (!$db->getOne(knjx_c035fQuery::checkRestrict($this, $data))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "担当科目ではありません。");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_c035fQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //異動者チェック
    public function getIdouCnt($db, $data)
    {
        $year_sem = $data["YEAR"].$data["SEMESTER"];
        $idou_month = sprintf('%02d', $data["MONTH"]);
        $idou_year = ($idou_month < '04') ? $data["YEAR"] + 1 : $data["YEAR"];
        //学期の開始日・終了日取得
        $semeday = $db->getRow(knjx_c035fQuery::selectSemesAll($year_sem, "day"), DB_FETCHMODE_ASSOC);
        //締め日取得
        $appointed_day = $db->getOne(knjx_c035fQuery::getAppointedDay($this, $data["YEAR"], $idou_month, $data["SEMESTER"]));
        //最終日
        $lastday = date("t", mktime(0, 0, 0, $idou_month, 1, $idou_year));
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $month && $semeday["E_DAY"] < $lastday) {
            $lastday = $semeday["E_DAY"];
        }
        $idou_day = ($appointed_day == "") ? $lastday : $appointed_day;

        //異動対象日付
        $idou_date = $idou_year.'-'.$idou_month.'-'.sprintf('%02d', $idou_day);

        //開始日付
        if (sprintf('%02d', $semeday["S_MONTH"]) == $idou_month) {
            $sdate = $idou_year.'-'.$idou_month.'-'.sprintf('%02d', $semeday["S_DAY"]);
        } else {
            $sdate = $idou_year.'-'.$idou_month.'-01';
        }

        $idou = 0;
        //異動者（退学・転学・卒業）
        $idouData1 = $db->getOne(knjx_c035fQuery::getIdouData($data["SCHREGNO"], $idou_date, $sdate));
        $idou += $idouData1;
        //異動者（留学・休学）
        $idouData2 = $db->getOne(knjx_c035fQuery::getTransferData($data["SCHREGNO"], $idou_date, $sdate));
        $idou += $idouData2;

        return $idou;
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

        //ヘッダー
        $header = $header_rei = array();
        $header["CLASSCD"]              = "※教科コード";
        $header_rei["CLASSCD"]          =  "12";
        if ($this->Properties["useCurriculumcd"] == "1") {
            //学校種別ヘッダー例取得
            $school_kind_rei = "";
            if ($this->Properties["use_prg_schoolkind"] == "1" || $this->Properties["useSchool_KindField"] == "1") {
                $school_kind_rei = $this->field["SCHOOL_KIND"];
            } else {
                $result = $db->query(knjx_c035fquery::getSchoolKind());
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($school_kind_rei) {
                        $school_kind_rei .= " ";
                    }
                    $school_kind_rei .= $row["NAME1"].":".$row["ABBV1"];
                }
                $result->free();
            }
            if ($school_kind_rei == "") {
                $school_kind_rei = "未設定";
            }
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
        foreach ($this->item_array as $key => $val) {
            $header[$val["item"]]           = $val["label"];
            $header_rei[$val["item"]]       = ($val["input"]) ? "10" : "入力不可";
        }
        foreach ($this->itemR_array as $key => $val) {
            $header[$val["item"]]           = $val["label"];
            $header_rei[$val["item"]]       = ($val["input"]) ? "10" : "入力不可";
        }
        $header[$this->lastColumn]       = $this->lastColumn;
        $header_rei[$this->lastColumn]   = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("COPYCD"        => "複写区分",
                                     "YEAR"          => "年度",
                                     "MONTH"         => "対象月",
                                     "SEMESTER"      => "学期",
                                     "SCHREGNO"      => "学籍番号",
                                     "CLASSCD"       => "教科コード",
                                     "SCHOOL_KIND"   => "学校種別",
                                     "CURRICULUM_CD" => "教育課程コード",
                                     "SUBCLASSCD"    => "科目コード");
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
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_c035fQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {            //データ出力
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);

                    $sep = "";
                    foreach ($header as $key => $val) {
                        $contents .= $sep.$row[$key];
                        $sep = ",";
                    }
                    $contents .= "\n";
                } else {        //エラー出力
                    $contents .= implode($row, ",") ."\n";
                }
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
