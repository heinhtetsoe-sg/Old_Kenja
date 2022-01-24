<?php

require_once('for_php7.php');

class knjc031fModel extends Model {
    var $cmd;
    var $field = array();
    var $reset = array();
    var $appointed_day;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjc031fModel() {
        $this->field = array();
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
        $this->getPropertiesAll();  //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd              = VARS::request("cmd");
        $this->appointed_day    = VARS::post("SET_APPOINTED_DAY");      //締め日

        //課程学科コード
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->field["COURSE_MAJOR"] = VARS::post("COURSE_MAJOR");
        } else {
            $this->field["COURSE_MAJOR"] = '0000';
        }

        $this->field["SELECT_CLASS_TYPE"] = VARS::post("SELECT_CLASS_TYPE");    //学級タイプ選択
        if ($this->cmd == "change_radio") {
            $this->field["hr_class"]        = "";   //対象学級
            $this->field["GROUP_HR_CLASS"]  = "";   //対象複式学級
        } else {
            $this->field["hr_class"]        = VARS::post("HR_CLASS");       //対象学級
            $this->field["GROUP_HR_CLASS"]  = VARS::post("GROUP_HR_CLASS"); //対象複式学級
        }
        $this->field["month"]       = VARS::post("MONTH");      //対象月

        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //移動方向
        $this->field["EXECUTED"]    = VARS::post("EXECUTED");   //出欠完了
        $this->field["useLine"]     = VARS::post("useLine");

        //編集対象データを取得
        $this->field["SCHREGNO"]    = VARS::post("SCHREGNO");   //学籍番号
        $this->field["LESSON"]      = VARS::post("LESSON");     //授業日数
        $this->field["OFFDAYS"]     = VARS::post("OFFDAYS");    //休学日数
        $this->field["ABROAD"]      = VARS::post("ABROAD");     //留学日数
        $this->field["ABSENT"]      = VARS::post("ABSENT");     //公欠日数
        $this->field["SUSPEND"]     = VARS::post("SUSPEND");    //出停日数
        $this->field["KOUDOME"]     = VARS::post("KOUDOME");    //交止
        $this->field["VIRUS"]       = VARS::post("VIRUS");      //伝染病
        $this->field["MOURNING"]    = VARS::post("MOURNING");   //忌引日数
        $this->field["SICK"]        = VARS::post("SICK");       //欠席
        $this->field["NOTICE"]      = VARS::post("NOTICE");     //事故欠届
        $this->field["NONOTICE"]    = VARS::post("NONOTICE");   //事故欠無
        $this->field["LATE"]        = VARS::post("LATE");       //遅刻回数
        $this->field["EARLY"]       = VARS::post("EARLY");      //早退回数
        $this->field["DETAIL_001"]  = VARS::post("DETAIL_001"); //DETAIL
        $this->field["DETAIL_002"]  = VARS::post("DETAIL_002"); //DETAIL
        $this->field["DETAIL_003"]  = VARS::post("DETAIL_003"); //DETAIL
        $this->field["DETAIL_004"]  = VARS::post("DETAIL_004"); //DETAIL
        $this->field["DETAIL_101"]  = VARS::post("DETAIL_101"); //DETAIL
        $this->field["DETAIL_102"]  = VARS::post("DETAIL_102"); //DETAIL
        $this->field["REMARK"]      = VARS::post("REMARK");     //備考
        $this->field["LESSON_SET"]  = VARS::post("LESSON_SET"); //授業日数(セット用)
        $this->field["HR_REMARK"]   = VARS::post("HR_REMARK");  //クラス備考

        if ($this->cmd == "update") {
            if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
                $this->field["COURSE_MAJOR"]    = VARS::post("HIDDEN_COURSE_MAJOR");
            }
            if ($this->Properties["useSpecial_Support_Hrclass"] == "1") {
                $this->field["SELECT_CLASS_TYPE"]   = VARS::post("HIDDEN_SELECT_CLASS_TYPE");
                if ($this->field["SELECT_CLASS_TYPE"] == "2") {
                    $this->field["GROUP_HR_CLASS"]  = VARS::post("HIDDEN_GROUP_HR_CLASS");
                } else {
                    $this->field["hr_class"]    = VARS::post("HIDDEN_HR_CLASS");
                }
            } else {
                $this->field["SELECT_CLASS_TYPE"]   = 1;
                $this->field["hr_class"]            = VARS::post("HIDDEN_HR_CLASS");
            }
            $this->field["month"]       = VARS::post("HIDDEN_MONTH");
            $this->field["LESSON_SET"]  = VARS::post("HIDDEN_LESSON_SET");
            $this->field["MOVE_ENTER"]  = VARS::post("HIDDEN_MOVE_ENTER");
            $this->field["EXECUTED"]    = VARS::post("HIDDEN_EXECUTED");
        }

        //学年・学級に分割
        list($this->field["grade"], $this->field["class"]) = explode("-",$this->field["hr_class"]);

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
        $this->attendItem["KESSEKI"]        = array("0000", "3",    "欠席日数");
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

        //課程学科
        $query = knjc031fQuery::getCourseMajor($this);
        $getCM = $db->getOne($query);
        $cm = ($this->field["COURSE_MAJOR"]) ? $this->field["COURSE_MAJOR"] : $getCM;

        //名称マスタから項目名取得
        $nameMst = array();
        $query = knjc031fQuery::getAttendNameList($this);
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
                $query = knjc031fQuery::getNameMstA023($this);
                $this->school_kind = $db->getOne($query);
            } else {
                $query = knjc031fQuery::getSchoolKind($this, $this->field["grade"]);
                $this->school_kind = $db->getOne($query);
            }
        } else if ($this->Properties["useSchool_KindField"] == "1") {
            $this->school_kind = SCHOOLKIND;
        }

        $gradeSchoolKind = SCHOOLKIND;
        if ($this->school_kind && $this->school_kind != '00') {
            $gradeSchoolKind = $this->school_kind;
        }
        //権限グループコード
        $this->groupcd = $db->getOne(knjc031fQuery::getMinGroupcd($this, $gradeSchoolKind));

        //ADMIN_CONTROL_ATTEND_ITEMNAME_DATから項目名取得
        $itemName = array();
        $query = knjc031fQuery::getAdminControlAttendItemnameDat($this, $cm, $this->school_kind);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemName[$row["ATTEND_ITEM"]] = $row["ATTEND_ITEMNAME"];
        }

        //出欠管理者コントロール取得
        $this->item_array = $this->itemR_array = array();
        $query = knjc031fQuery::getAdminControlAttendDat($this, $cm, $this->school_kind);
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
                                                              "item"    => $row["ATTEND_ITEM"]);
            }
        }

        if ($this->Properties["useAttendLessonOverHyouji"] == "1") {
            //欠席日数注意の日数(COURSECD-MAJORCDをキーとした連想配列)
            $this->tyuuiNissuArray = array();
            $query = knjc031fQuery::getAttendLessonInfo($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->tyuuiNissuArray[strval($row["CMCD"])] = $row["TYUUI_NISSU"];
            }
        }
        $this->kessekiItem = array("KESSEKI", "SICK", "NOTICE", "NONOTICE");

        // 備考の最大入力文字
        $this->remarkMaxlen = 30;
        $dbRemarkLength = knjc031fQuery::getColumnLength($db, "ATTEND_SEMES_REMARK_DAT", "REMARK1");
        if ($dbRemarkLength >= 150) {
            $this->remarkMaxlen = 50;
        }
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();

        if (!isset($this->field["SCHREGNO"])) {
            $this->setWarning("MSG304");
            return false;
        }

        $monthAr = preg_split("/-/", $this->field["month"]);

        //締め日の最終日
        $year = CTRL_YEAR;
        if ($monthAr[0] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime( 0, 0, 0, $monthAr[0], 1, $year ));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->appointed_day) {
            $this->setWarning("MSG901",$monthAr[0]."月の最終日は、".$lastday."日です。");
            return false;
        }
        //締め日が0の場合
        if (0 == $this->appointed_day) {
            $this->setWarning("MSG901","0日は存在しません。");
            return false;
        }

        $semeday = $db->getRow(knjc031fQuery::selectSemesAll($monthAr[1]), DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $monthAr[0] &&
            $semeday["S_DAY"] > $this->appointed_day) {
            $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。");
            return false;
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $monthAr[0] &&
            $semeday["E_DAY"] < $this->appointed_day) {
            $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。");
            return false;
        }

        //文字数チェック
        $err = false;
        foreach($this->field["SCHREGNO"] as $key => $schregno) {
            if (strlen($this->field["REMARK"][$key]) > $this->remarkMaxlen * 3) {
                $err = true;
            }
        }
        if ($err) {
            $this->setWarning("MSG915","出欠の備考は全角{$this->remarkMaxlen}文字までです。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            if ($this->update()) {
                unset($this->field["LESSON_SET"]);
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //編集対象データを更新
    function &update() {
        $result = knjc031fQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
