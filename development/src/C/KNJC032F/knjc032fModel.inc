<?php

require_once('for_php7.php');

class knjc032fModel extends Model {
    var $cmd;
    var $field;

    function knjc032fModel() {
        $this->field = array();
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //課程学科コード
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->field["COURSE_MAJOR"] = VARS::post("COURSE_MAJOR");
        } else {
            $this->field["COURSE_MAJOR"] = '0000';
        }

        $this->field["SELECT_CLASS_TYPE"] = VARS::post("SELECT_CLASS_TYPE");  //学級タイプ選択
        if ($this->cmd == "change_radio") {
            $this->field["hr_class"]        = "";   //対象学級
            $this->field["GROUP_HR_CLASS"]  = "";   //対象複式学級
            $this->field["schregno"]        = "";   //対象生徒
        } else if ($this->cmd == "change_class") {
            $this->field["hr_class"]        = VARS::post("HR_CLASS");       //対象学級
            $this->field["GROUP_HR_CLASS"]  = VARS::post("GROUP_HR_CLASS"); //対象複式学級
            $this->field["schregno"]        = "";   //対象生徒
        } else {
            $this->field["hr_class"]        = VARS::post("HR_CLASS");       //対象学級
            $this->field["GROUP_HR_CLASS"]  = VARS::post("GROUP_HR_CLASS"); //対象複式学級
            $this->field["schregno"]        = VARS::post("SCHREGNO");       //対象生徒
        }
        $this->field["MOVE_ENTER"] = VARS::post("MOVE_ENTER");  //移動方向

        if ($this->cmd == "update") {
            if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
                $this->field["COURSE_MAJOR"]        = VARS::post("HIDDEN_COURSE_MAJOR");
            }
            $this->field["SELECT_CLASS_TYPE"]   = VARS::post("HIDDEN_SELECT_CLASS_TYPE");
            $this->field["hr_class"]            = VARS::post("HIDDEN_HR_CLASS");
            $this->field["GROUP_HR_CLASS"]      = VARS::post("HIDDEN_GROUP_HR_CLASS");
            $this->field["schregno"]            = VARS::post("HIDDEN_SCHREGNO");
            $this->field["MOVE_ENTER"]          = VARS::post("HIDDEN_MOVE_ENTER");
        }

        //学年・学級に分割
        list($this->field["grade"], $this->field["class"]) = explode("-", $this->field["hr_class"]);

        //編集対象データを取得
        $this->field["MONTH"]           = VARS::post("MONTH");          //対象月
        $this->field["APPOINTED_DAY"]   = VARS::post("APPOINTED_DAY");  //締め日
        $this->field["LESSON"]          = VARS::post("LESSON");         //授業日数
        $this->field["OFFDAYS"]         = VARS::post("OFFDAYS");        //休学日数
        $this->field["ABROAD"]          = VARS::post("ABROAD");         //留学日数
        $this->field["ABSENT"]          = VARS::post("ABSENT");         //公欠日数
        $this->field["SUSPEND"]         = VARS::post("SUSPEND");        //出停日数
        $this->field["VIRUS"]           = VARS::post("VIRUS");          //伝染病
        $this->field["KOUDOME"]         = VARS::post("KOUDOME");        //交止
        $this->field["MOURNING"]        = VARS::post("MOURNING");       //忌引日数
        $this->field["SICK"]            = VARS::post("SICK");           //病欠
        $this->field["NOTICE"]          = VARS::post("NOTICE");         //事故欠届
        $this->field["NONOTICE"]        = VARS::post("NONOTICE");       //事故欠無
        $this->field["LATE"]            = VARS::post("LATE");           //遅刻回数
        $this->field["EARLY"]           = VARS::post("EARLY");          //早退回数
        $this->field["DETAIL_001"]      = VARS::post("DETAIL_001");     //DETAIL
        $this->field["DETAIL_002"]      = VARS::post("DETAIL_002");     //DETAIL
        $this->field["DETAIL_003"]      = VARS::post("DETAIL_003");     //DETAIL
        $this->field["DETAIL_004"]      = VARS::post("DETAIL_004");     //DETAIL
        $this->field["DETAIL_101"]      = VARS::post("DETAIL_101");     //DETAIL
        $this->field["DETAIL_102"]      = VARS::post("DETAIL_102");     //DETAIL
        $this->field["REMARK"]          = VARS::post("REMARK");         //備考

        /****************************/
        /*  出欠項目のコントロール  */
        /****************************/
        //出欠項目一覧
        $this->attendItem = array();
        $this->attendItem["LESSON"]         = array("0000", "1",    "授業日数");
        $this->attendItem["OFFDAYS"]        = array("A004", "2",    "");
        $this->attendItem["ABROAD"]         = array("A004", "1",    "");
        $this->attendItem["ABSENT"]         = array("C001", "1",    "");
        $this->attendItem["SUSPEND"]        = array("C001", "2",    "");
        if ($this->Properties["useKoudome"] == "true") {
            $this->attendItem["KOUDOME"]        = array("C001", "25",   "");
        }
        if ($this->Properties["useVirus"] == "true") {
            $this->attendItem["VIRUS"]          = array("C001", "19",   "");
        }
        $this->attendItem["MOURNING"]       = array("C001", "3",    "");
        $this->attendItem["ATTEND"]         = array("0000", "2",    "出席すべき日数");
        $this->attendItem["SICK"]           = array("C001", "4",    "");
        $this->attendItem["NOTICE"]         = array("C001", "5",    "");
        $this->attendItem["NONOTICE"]       = array("C001", "6",    "");
        $this->attendItem["PRESENT"]        = array("0000", "4",    "出席日数");
        $this->attendItem["LATE"]           = array("C001", "15",   "");
        $this->attendItem["EARLY"]          = array("C001", "16",   "");
        $this->attendItem["DETAIL_001"]     = array("C002", "001",  "");
        $this->attendItem["DETAIL_002"]     = array("C002", "002",  "");
        $this->attendItem["DETAIL_003"]     = array("C002", "003",  "");
        $this->attendItem["DETAIL_004"]     = array("C002", "004",  "");
        $this->attendItem["DETAIL_101"]     = array("C002", "101",  "");
        $this->attendItem["DETAIL_102"]     = array("C002", "102",  "");

        $db = Query::dbCheckOut();

        if ($this->field["grade"]) {
            $gradeSchoolKind = $db->getOne(knjc032fQuery::getSchoolKind1($this, $this->field["grade"]));
        } else if ('' != SCHOOLKIND) {
            $gradeSchoolKind = SCHOOLKIND;
        }
        //権限グループコード
        $this->groupcd = $db->getOne(knjc032fQuery::getMinGroupcd($this, $gradeSchoolKind));

        //課程学科
        $query = knjc032fQuery::getCourseMajor($this);
        $getCM = $db->getOne($query);
        $cm = ($this->field["COURSE_MAJOR"]) ? $this->field["COURSE_MAJOR"] : $getCM;

        //名称マスタから項目名取得
        $nameMst = array();
        $query = knjc032fQuery::getAttendNameList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($this->attendItem as $attend_item => $val) {
                if ($val[0] == $row["NAMECD1"] && $val[1] == $row["NAMECD2"]) {
                    $nameMst[$attend_item] = $row["NAME1"];
                }
            }
        }

        //学校種別
        $this->school_kind = '00';
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $hr_class = $hr_class_flg = "";
            if ($this->Properties["useSpecial_Support_Hrclass"] == 1 && $this->field["SELECT_CLASS_TYPE"] == 2) {
                $hr_class = $this->field["GROUP_HR_CLASS"];
                $hr_class_flg = "1";
            } else {
                $hr_class = $this->field["hr_class"];
                $hr_class_flg = "2";
            }

            if ($hr_class == "" || $hr_class_flg != "2") {
                if ($this->urlSchoolKind) {
                    $this->school_kind = $this->urlSchoolKind;
                }
                if ($hr_class_flg != "2" && $this->field["schregno"]) {
                    $query = knjc032fQuery::getSchoolKind2($this, $this->field["schregno"]);
                    $this->school_kind = $db->getOne($query);
                }
            } else {
                $query = knjc032fQuery::getSchoolKind1($this, $this->field["grade"]);
                $this->school_kind = $db->getOne($query);
            }
        } else if ($this->Properties["useSchool_KindField"] == "1") {
            $this->school_kind = SCHOOLKIND;
        }

        //ADMIN_CONTROL_ATTEND_ITEMNAME_DATから項目名取得
        $itemName = array();
        $query = knjc032fQuery::getAdminControlAttendItemnameDat($this, $cm, $this->school_kind);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemName[$row["ATTEND_ITEM"]] = $row["ATTEND_ITEMNAME"];
        }

        //出欠管理者コントロール取得
        $this->item_array = array();
        $query = knjc032fQuery::getAdminControlAttendDat($this, $cm, $this->school_kind);
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

            $this->item_array[$row["SHOWORDER"]] = array("label"    => $label,
                                                         "item"     => $row["ATTEND_ITEM"],
                                                         "input"    => $row["INPUT_FLG"]);
        }

        //学期開始日・終了日等取得
        $this->semeday = array();
        $query = knjc032fQuery::selectSemesAll();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $feild => $val) {
                $this->semeday[$row["SEMESTER"]][$feild] = $val;
            }
        }

        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();

        //更新対象データがない場合
        if ($this->field["MONTH"] == NULL || $this->field["MONTH"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        foreach ($this->field["MONTH"] as $key => $month) {
            //$monthAr[0] = 月、$monthAr[1] = 学期
            $monthAr = preg_split("/-/", $month);
            if ($this->dataCheck($key) || (!$this->dataCheck($key) && "" != $this->field["APPOINTED_DAY"][$key])) {
                $errMsg = $this->appDateCheck($db, $key, $monthAr);
                if ("" != $errMsg) {
                    $this->setWarning("MSG901", $errMsg);
                    return false;
                }
            }
        }

        Query::dbCheckIn($db);

        return true;
    }

    //入力データ判定
    function dataCheck($key) {
        $flg = false;
        foreach ($this->item_array as $ikey => $ival) {
            if ($val["input"] == "1") {
                if ($this->field[$ival["item"]][$key] != "") $flg = true;
            }
        }

        return $flg;
    }

    //締め日チェック
    function appDateCheck($db, $key, $monthAr) {
        //締め日の最終日
        $year = CTRL_YEAR;
        if ($monthAr[0] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime( 0, 0, 0, $monthAr[0], 1, $year ));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->field["APPOINTED_DAY"][$key]) {
            return $monthAr[0]."月の最終日は、".$lastday."日です。";
        }
        //締め日がNullの場合
        if ("" == $this->field["APPOINTED_DAY"][$key]) {
            return "日付が入力されていません。(".$monthAr[0]."月)";
        }
        //締め日が0の場合
        if ("" != $this->field["APPOINTED_DAY"][$key] && 0 == $this->field["APPOINTED_DAY"][$key]) {
            return "0日は存在しません。(".$monthAr[0]."月)";
        }
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $this->semeday[$monthAr[1]]["S_MONTH"]) == $monthAr[0] &&
            $this->semeday[$monthAr[1]]["S_DAY"] > $this->field["APPOINTED_DAY"][$key]) {
            return $this->semeday[$monthAr[1]]["SEMESTERNAME"]."の開始日は、".$this->semeday[$monthAr[1]]["S_MONTH"]."月".$this->semeday[$monthAr[1]]["S_DAY"]."日です。";
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $this->semeday[$monthAr[1]]["E_MONTH"]) == $monthAr[0] &&
            $this->semeday[$monthAr[1]]["E_DAY"] < $this->field["APPOINTED_DAY"][$key]) {
            return $this->semeday[$monthAr[1]]["SEMESTERNAME"]."の最終日は、".$this->semeday[$monthAr[1]]["E_MONTH"]."月".$this->semeday[$monthAr[1]]["E_DAY"]."日です。";
        }
        return "";
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            if ($this->update()) {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //編集対象データを更新
    function &update() {
        $result = knjc032fQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
