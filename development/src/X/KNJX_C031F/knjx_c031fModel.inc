<?php

require_once('for_php7.php');
class knjx_c031fModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx_c031fModel()
    {
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

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("OUTPUT"               =>  VARS::post("OUTPUT"),               //出力取込種別
                             "SELECT_CLASS_TYPE"    =>  VARS::post("SELECT_CLASS_TYPE"),    //学級タイプ選択
                             "YEAR"                 =>  VARS::post("YEAR"),                 //年度一覧
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //年組一覧
                             "GROUP_HR_CLASS"       =>  VARS::post("GROUP_HR_CLASS"),       //複式クラス
                             "MONTH"                =>  VARS::post("MONTH"),                //対象月
                             "HEADER"               =>  VARS::post("HEADER"),               //ヘッダ有
                             "SHORI_MEI"            =>  VARS::post("SHORI_MEI"),            //処理名
                             "SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND")           //校種
                             );


        //課程学科コード
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->field["COURSE_MAJOR"] = VARS::post("COURSE_MAJOR");
        } else {
            $this->field["COURSE_MAJOR"] = '0000';
        }

        if (VARS::get("SEND_PRGID")) {
            $this->field["SCHOOL_KIND"]    = (VARS::get("SEND_SCHOOL_KIND"))    ? VARS::get("SEND_SCHOOL_KIND")    : $this->field["SCHOOL_KIND"];
            $this->field["COURSE_MAJOR"]   = (VARS::get("SEND_COURSE_MAJOR"))   ? VARS::get("SEND_COURSE_MAJOR")   : $this->field["COURSE_MAJOR"];
            $this->field["GRADE_HR_CLASS"] = (VARS::get("SEND_hr_class"))       ? str_replace("-", "", VARS::get("SEND_hr_class")) : $this->field["GRADE_HR_CLASS"];
            $this->field["GROUP_HR_CLASS"] = (VARS::get("SEND_GROUP_HR_CLASS")) ? VARS::get("SEND_GROUP_HR_CLASS") : $this->field["GROUP_HR_CLASS"];
            $this->field["MONTH"]          = (VARS::get("SEND_month"))          ? VARS::get("SEND_month")          : $this->field["MONTH"];
        }
        //校種コード
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->field["SCHOOL_KIND"] = $this->field["SCHOOL_KIND"];
        } elseif ($this->Properties["useSchool_KindField"] == "1") {
            $this->field["SCHOOL_KIND"] = SCHOOLKIND;
        }
        if ($this->field["GROUP_HR_CLASS"]) {
            $this->field['grade'] = substr($this->field["GROUP_HR_CLASS"], 0, 2);
        } else {
            $this->field['grade'] = '00';
        }

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        /******************/
        /*  出欠項目取得  */
        /******************/
        //出欠項目一覧
        $this->attendItem = array();
        $this->attendItem["LESSON"]     = array("0000", "",     "授業日数");
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
        $this->attendItem["ATTEND"]     = array("0000", "",     "出席すべき日数");
        $this->attendItem["SICK"]       = array("C001", "4",    "");
        $this->attendItem["NOTICE"]     = array("C001", "5",    "");
        $this->attendItem["NONOTICE"]   = array("C001", "6",    "");
        $this->attendItem["KESSEKI"]    = array("0000", "",     "欠席日数");
        $this->attendItem["PRESENT"]    = array("0000", "",     "出席日数");
        $this->attendItem["LATE"]       = array("C001", "15",   "");
        $this->attendItem["EARLY"]      = array("C001", "16",   "");
        $this->attendItem["DETAIL_001"] = array("C002", "001",  "");
        $this->attendItem["DETAIL_002"] = array("C002", "002",  "");
        $this->attendItem["DETAIL_003"] = array("C002", "003",  "");
        $this->attendItem["DETAIL_004"] = array("C002", "004",  "");
        $this->attendItem["DETAIL_101"] = array("C002", "101",  "");
        $this->attendItem["DETAIL_102"] = array("C002", "102",  "");

        $db = Query::dbCheckOut();

        //権限グループコード
        $this->groupcd = $db->getOne(knjx_c031fQuery::getMinGroupcd($this));

        //課程学科
        $query = knjx_c031fQuery::getCourseMajor($this);
        $getCM = $db->getOne($query);
        $cm = ($this->field["COURSE_MAJOR"]) ? $this->field["COURSE_MAJOR"] : $getCM;

        //名称マスタから項目名取得
        $nameMst = array();
        $query = knjx_c031fQuery::getAttendNameList($this);
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
        $query = knjx_c031fQuery::getAdminControlAttendItemnameDat($this, $cm);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemName[$row["ATTEND_ITEM"]] = $row["ATTEND_ITEMNAME"];
        }

        //出欠管理者コントロール取得
        $this->item_array = array();
        $query = knjx_c031fQuery::getAdminControlAttendDat($this, $cm, 1);
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

            $this->item_array[$row["SHOWORDER"]] = array("label"  => $label,
                                                         "item"   => $row["ATTEND_ITEM"],
                                                         "input"  => $row["INPUT_FLG"]);
        }
        $this->item_array2 = array();
        $query = knjx_c031fQuery::getAdminControlAttendDat($this, $cm, 2);
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

            $this->item_array2[$row["SHOWORDER"]] = array("label"  => '累積' . $label,
                                                          "item"   => 'SUM_'.$row["ATTEND_ITEM"],
                                                          "input"  => $row["INPUT_FLG"]);
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
                    knjx_c031fQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_c031fQuery::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata["YEAR"]          = array(4,  "年度");
            $checkdata["MONTH"]         = array(2,  "対象月");
            $checkdata["SEMESTER"]      = array(1,  "学期");
            $checkdata["SCHREGNO"]      = array(8,  "学籍番号");
            foreach ($this->item_array as $key => $val) {
                if ($val["input"]) {
                    $checkdata[$val["item"]]    = array(3,  $val["label"]);
                }
            }
            $checkdata["REMARK"]        = array(90, "出欠の備考");

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
            $primaryKey3 = array();
            $duplicateCsv3 = array();
            $primaryKey1 = $this->getPrimaryKey('ATTEND_SEMES_DAT');
            $primaryKey2 = $this->getPrimaryKey('ATTEND_SEMES_DETAIL_DAT');
            $primaryKey3 = $this->getPrimaryKey('ATTEND_SEMES_REMARK_DAT');
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

                    $tmpCnt = 8;
                    $default = ($this->Properties["use_Attend_zero_hyoji"] == "1") ? "" : "0";
                    foreach ($this->item_array as $key => $val) {
                        $data[$val["item"]] = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    foreach ($this->item_array2 as $key => $val) {
                        $data[$val["item"]] = (strlen(trim($tmp[$tmpCnt]))) ? $tmp[$tmpCnt] : $default;
                        $tmpCnt++;
                    }
                    $data["REMARK"]     = $tmp[$tmpCnt];
                    $data["SEQ"]        = "";
                    $data["COPYCD"]     = "0";
                    $checkCnt  = 10;
                    $checkCnt +=get_count($this->item_array);
                    $checkCnt +=get_count($this->item_array2);

                    //項目数によるファイルチェック
                    if (get_count($tmp) != $checkCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $limitErrFlg        = false;    //範囲エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $orderErrFlg        = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $limitErrMsg        = "";       //範囲エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $orderErrMsg        = "";

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $masterSep          = "";
        $limitSep           = "";
        $duplicateCsvSep    = "";
        $orderSep           = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
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
                if ($key != "REMARK" && $data[$key] != "" && !is_numeric($data[$key])) {
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
        if (1 > $db->getOne(knjx_c031fQuery::checkRegdDat($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
        }

        //存在チェック（対象月）
        if (!$db->getOne(knjx_c031fQuery::selectMonthQuery($data["YEAR"], $data["MONTH"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された年月のデータがありません。");
        }

        //存在チェック（異動者）
        $idou = $this->getIdouCnt($db, $data);
        if ($idou > 0) {
            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "異動者");
        }

        //権限チェック
        if ($this->auth == DEF_UPDATE_RESTRICT) {
            if (!$db->getOne(knjx_c031fQuery::checkRegdHdat($this, $data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "そのクラスの情報は取り込めません。");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$masterErrMsg.$limitErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_c031fQuery::insertQueryErr($db, $line, $checkError);
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
        $semeday = $db->getRow(knjx_c031fQuery::selectSemesAll($year_sem, "day"), DB_FETCHMODE_ASSOC);
        //締め日取得
        $appointed_day = $db->getOne(knjx_c031fQuery::getAppointedDay($data["YEAR"], $this->field["SCHOOL_KIND"], $idou_month, $data["SEMESTER"]));
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
        $idouData1 = $db->getOne(knjx_c031fQuery::getIdouData($data["SCHREGNO"], $idou_date, $sdate));
        $idou += $idouData1;
        //異動者（留学・休学）
        $idouData2 = $db->getOne(knjx_c031fQuery::getTransferData($data["SCHREGNO"], $idou_date, $sdate));
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
        $header["HR_NAME"]              = "学級";
        $header_rei["HR_NAME"]          = "1年1組";
        $header["ATTENDNO"]             = "No.";
        $header_rei["ATTENDNO"]         = "001";
        $header["NAME"]                 = "氏名";
        $header_rei["NAME"]             = "アルプ太郎";
        $header["YEAR"]                 = "※年度";
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header["MONTH"]                = "※対象月";
        $header_rei["MONTH"]            = "04";
        $header["SEMESTER"]             = "※学期";
        $header_rei["SEMESTER"]         = "1";
        $header["SCHREGNO"]             = "※学籍番号";
        $header_rei["SCHREGNO"]         = "12345678";
        $header["APPOINTED_DAY"]        = "締め日";
        $header_rei["APPOINTED_DAY"]    = "入力不可";
        foreach ($this->item_array as $key => $val) {
            $header[$val["item"]]           = $val["label"];
            $header_rei[$val["item"]]       = ($val["input"]) ? "10" : "入力不可";
        }
        foreach ($this->item_array2 as $key => $val) {
            $header[$val["item"]]           = $val["label"];
            $header_rei[$val["item"]]       = "入力不可";
        }
        $header["REMARK"]               = "出欠の備考";
        $header_rei["REMARK"]           = "全角30文字まで";
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
                                    "SEQ"                  => "詳細区分",
                                    "SCHREGNO"             => "学籍番号");
            $tables = array();
            $tables = array("1" => "ATTEND_SEMES_DAT", "2" => "ATTEND_SEMES_DETAIL_DAT", "3" => "ATTEND_SEMES_REMARK_DAT");
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
                $schoolMst = array();
                $query = knjx_c031fQuery::getSchoolMst($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    foreach ($row as $key => $val) {
                        $schoolMst[$key] = $val;
                    }
                }
                $query = knjx_c031fQuery::selectMainQuery($this, $schoolMst);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {         //データ出力
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
