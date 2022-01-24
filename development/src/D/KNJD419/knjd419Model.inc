<?php

require_once('for_php7.php');

class knjd419Model extends Model {
    var $cmd;
    var $control;
    var $year;
    var $semester;
    var $gakubu_school_kind;
    var $hukusiki_radio = "";
    var $grade_hr_class = "";
    var $grade_hr_class2 = "";
    var $condition;
    var $groupcd;
    var $guidance_pattern;
    var $pattern;
    var $field;
    var $field2;
    var $field3;
    var $field4;
    var $fieldH;

    function knjd419Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->semester);
            unset($this->gakubu_school_kind);
            unset($this->hukusiki_radio);
            unset($this->grade_hr_class);
            unset($this->grade_hr_class2);
            unset($this->condition);
            unset($this->groupcd);
            unset($this->guidance_pattern);
            unset($this->classcd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
            unset($this->subclasscd);
            unset($this->unit_aim_div);
            unset($this->pattern);
            $this->field = array();
            $this->field2 = array();
            $this->field3 = array();
            $this->field4 = array();
            $this->fieldH = array();
            unset($this->setcnt);
        }

        //左フレーム
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::request("SEMESTER");
        }
        if (VARS::post("GAKUBU_SCHOOL_KIND") != "") {
            $this->gakubu_school_kind = VARS::request("GAKUBU_SCHOOL_KIND");
        }
        if ($this->Properties["useSpecial_Support_School"] == '1') {
            if (VARS::post("HUKUSIKI_RADIO") != "") {
                $this->hukusiki_radio = VARS::request("HUKUSIKI_RADIO");
            }
        }
        if (VARS::post("GRADE_HR_CLASS") != "") {
            $this->grade_hr_class = VARS::request("GRADE_HR_CLASS");
        }
        if (VARS::post("GRADE_HR_CLASS2") != "") {
            $this->grade_hr_class2 = VARS::request("GRADE_HR_CLASS2");
        }
        if (VARS::post("CONDITION") != "") {
            $this->condition = VARS::request("CONDITION");
        }
        if (VARS::post("GROUPCD") != "") {
            $this->groupcd = VARS::request("GROUPCD");
        }

        //右フレームに渡したパラメータ
        if (VARS::get("CLASSCD") != ""){
            $this->classcd = VARS::get("CLASSCD");
        }
        if (VARS::get("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("CURRICULUM_CD") != "") {
            $this->curriculum_cd = VARS::get("CURRICULUM_CD");
        }
        if (VARS::get("SUBCLASSCD") != "") {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("SEND_UNIT_AIM_DIV") != "") {
            $this->unit_aim_div = VARS::get("SEND_UNIT_AIM_DIV");
        }

        $this->cmd = VARS::request("cmd");

        $db = Query::dbCheckOut();
        //初期値設定（学部）
        $gakubu_school_kind = $db->getOne(knjd419Query::getSchoolKind());
        $this->gakubu_school_kind = ($this->gakubu_school_kind) ? $this->gakubu_school_kind : $gakubu_school_kind;
        //年組コンボ切替
        $this->hukusiki_radio = ($this->hukusiki_radio != "") ? $this->hukusiki_radio : 1;
        //初期値設定（年組）
        $grade_hr_class = $db->getOne(knjd419Query::getHrClass($this));
        $this->grade_hr_class = ($this->grade_hr_class != "") ? $this->grade_hr_class : $grade_hr_class;
        //初期値設定（年組（特別クラス選択時））
        $grade_hr_class2 = $db->getOne(knjd419Query::getHrClass2($this));
        $this->grade_hr_class2 = ($this->grade_hr_class2 != "") ? $this->grade_hr_class2 : (($this->grade_hr_class2 == "") ? '00-000' : $grade_hr_class2);
        //初期値設定（状態区分）
        $condition = $db->getOne(knjd419Query::getCondition($this));
        $this->condition = ($this->condition) ? $this->condition : $condition;
        //初期値設定（グループコード）
        $groupcd = $db->getOne(knjd419Query::getGroupcd($this));
        $this->groupcd = ($this->groupcd) ? $this->groupcd : $groupcd;
        //初期値設定（指導計画帳票パターン）
        $guidance_pattern = $db->getOne(knjd419Query::getGuidancePattern($this));
        $this->guidance_pattern = ($this->guidance_pattern) ? $this->guidance_pattern : $guidance_pattern;
        Query::dbCheckIn($db);

        //設定（UNIT_AIM_DIV / GUIDANCE_PATTERN / 連番 => 項目名 / テキスト(nullはGROUP_REMARKの項目数分) / 文字数 / 行数 / SEQ加算)
        $this->pattern[1][1][0] = array("ITEM_REMARK2", "GROUP_REMARK1",        "35", "10", "");
        $this->pattern[1][2][0] = array("ITEM_REMARK2", "GROUP_REMARK1",        "23", "10", "");
        $this->pattern[1][2][1] = array("ITEM_REMARK5", "GROUP_PROCEDURE",      "23", "50", "");
        $this->pattern[1][3][0] = array("ITEM_REMARK2", "GROUP_REMARK1",        "16", "10", "");
        $this->pattern[1][4][0] = array("ITEM_REMARK2", "GROUP_REMARK1",        "16", "10", "");
        $this->pattern[1][4][1] = array("ITEM_REMARK5", "GROUP_PROCEDURE",      "19", "50", "");
        $this->pattern[1][5][0] = array("ITEM_REMARK2", "GROUP_REMARK1",        "35", "10", "");
        $this->pattern[1][6][0] = array("ITEM_REMARK2", "GROUP_REMARK1",        "40", "10", "");
        $this->pattern[1][7][0] = array("ITEM_REMARK7", "GROUP_YEAR_TARGET",    "23", "5",  "");
        $this->pattern[1][7][1] = array("ITEM_REMARK2", "GROUP_REMARK1",        "35", "10", "");
        $this->pattern[1][8][0] = array("ITEM_REMARK2", "GROUP_REMARK1",        "35", "10", "");
        $this->pattern[1]["A"][0] = array("ITEM_REMARK3", "GROUP_REMARK1",        "27", "8", "");
        $this->pattern[1]["A"][1] = array("ITEM_REMARK4", "GROUP_REMARK2",        "25", "8", "");

        $this->pattern[0][1][0] = array("ITEM_REMARK2", "",                     "35", "10", "");
        $this->pattern[0][2][0] = array("ITEM_REMARK2", "",                     "23", "10", "");
        $this->pattern[0][2][1] = array("ITEM_REMARK5", "GROUP_PROCEDURE",      "23", "50", "");
        $this->pattern[0][3][0] = array("ITEM_REMARK2", "",                     "16", "10", "");
        $this->pattern[0][4][0] = array("ITEM_REMARK2", "",                     "16", "10", "");
        $this->pattern[0][4][1] = array("ITEM_REMARK5", "GROUP_PROCEDURE",      "19", "50", "");
        $this->pattern[0][5][0] = array("ITEM_REMARK2", "",                     "35", "10", "");
        $this->pattern[0][6][0] = array("ITEM_REMARK2", "",                     "40", "10", "");
        $this->pattern[0][7][0] = array("ITEM_REMARK7", "GROUP_YEAR_TARGET",    "23", "5",  "");
        $this->pattern[0][7][1] = array("ITEM_REMARK2", "",                     "35", "10", "");
        $this->pattern[0][8][0] = array("ITEM_REMARK2", "",                     "35", "10", "");
        $this->pattern[0]["A"][0] = array("ITEM_REMARK3", "",                     "27", "8", "");
        $this->pattern[0]["A"][1] = array("ITEM_REMARK4", "",                     "25", "8", 60);

        //単元
        $this->field2["UNITCD"] = VARS::post("UNITCD");

        //項目数
        $this->fieldH["GROUP_REMARK_CNT"] = VARS::post("GROUP_REMARK_CNT");

        //項目数（確定後）
        if (VARS::post("setcnt") != "") {
            $this->setcnt = VARS::post("setcnt");
        }

        //所見
        $pattern = $this->pattern[$this->unit_aim_div][$this->guidance_pattern];
        for ($i=0; $i < get_count($pattern); $i++) {
            list ($label, $value, $moji, $gyo, $seqadd) = $pattern[$i];
            if ($this->unit_aim_div == "1") {
                if ($value == "GROUP_PROCEDURE") {
                    $this->field4[$value] = VARS::post($value);
                } else if ($value == "GROUP_YEAR_TARGET") {
                    $this->field3[$value] = VARS::post($value);
                } else {
                    $this->field2[$value] = VARS::post($value);
                }
            } else {
                if ($value == "" && $this->fieldH["GROUP_REMARK_CNT"] > 0) {
                    for ($j=1; $j <= $this->fieldH["GROUP_REMARK_CNT"]; $j++) {
                        $key = "GROUP_REMARK".($seqadd + $j);
                        $this->field[$key] = VARS::post($key);
                    }
                } else if ($value == "GROUP_PROCEDURE") {
                    $this->field4[$value] = VARS::post($value);
                } else if ($value == "GROUP_YEAR_TARGET") {
                    $this->field3[$value] = VARS::post($value);
                } else {
                    $this->field[$value] = VARS::post($value);
                }
            }
        }
    }

    //入力項目チェック
    function validateInput($pre_year, $pre_semester) {
        $db = Query::dbCheckOut();

        //存在チェック（指定年度） -- GRADE_KIND_COMP_GROUP_DAT
        $cnt_comp_this = $db->getOne(knjd419Query::checkGradeKindCompGroupDat($this->year, $this->semester));
        if ($cnt_comp_this == 0) {
            $this->setWarning("MSG305");
            return false;
        }

        //存在チェック（前年度） -- HREPORT_GUIDANCE_GROUP_DAT
        $cnt_group_pre = $db->getOne(knjd419Query::checkHreportGuidanceGroupDat($pre_year, $pre_semester));
        //存在チェック（前年度） -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
        $cnt_unit_pre = $db->getOne(knjd419Query::checkHreportGuidanceGroupUnitDat($pre_year, $pre_semester));
        if ($cnt_group_pre == 0 && $cnt_unit_pre == 0) {
            $this->setWarning("MSG917","対象データが存在しません。");
            return false;
        }

        //存在チェック（指定年度） -- HREPORT_GUIDANCE_GROUP_DAT
        $cnt_groupD_this = $db->getOne(knjd419Query::checkHreportGuidanceGroupDat($this->year, $this->semester));
        //存在チェック（指定年度） -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
        $cnt_groupU_this = $db->getOne(knjd419Query::checkHreportGuidanceGroupUnitDat($this->year, $this->semester));
        //存在チェック（指定年度） -- HREPORT_GUIDANCE_GROUP_YDAT
        $cnt_groupY_this = $db->getOne(knjd419Query::checkHreportGuidanceGroupYdat($this->year, $this->semester));
        if ($cnt_groupD_this > 0 || $cnt_groupU_this > 0 || $cnt_groupY_this > 0) {
            $this->setWarning("MSG917","指定年度・学期にデータが存在します。");
            return false;
        }

        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        $db = Query::dbCheckOut();

        //サイズチェック
        $field = $field2 = $field3 = $field4 = "";
        $pattern = $this->pattern[$this->unit_aim_div][$this->guidance_pattern];
        for ($i=0; $i < get_count($pattern); $i++) {
            list ($label, $value, $moji, $gyo, $seqadd) = $pattern[$i];

            if ($this->unit_aim_div == "1") {
                if ($value == "GROUP_PROCEDURE") {
                    //バイト数チェック
                    if (strlen($this->field4[$value]) > 6100) {
                        $this->setWarning("MSG915");
                        return false;
                    }
                } else if ($value == "GROUP_YEAR_TARGET") {
                    //行数チェック
                    if ($this->validate_row_cnt($this->field3[$value], ($moji * 2)) > $gyo) {
                        $this->setWarning("MSG915","({$this->item[$label]}は{$gyo}行までです)");
                        return false;
                    }
                    $field3 .= ($field3 == "") ? $value : ','.$value;
                } else {
                    //行数チェック
                    if ($this->validate_row_cnt($this->field2[$value], ($moji * 2)) > $gyo) {
                        $this->setWarning("MSG915","({$this->item[$label]}は{$gyo}行までです)");
                        return false;
                    }
                    $field2 .= ($field2 == "") ? $value : ','.$value;
                }
            } else {
                if ($value == "") {
                    //項目数
                    $this->fieldH["GROUP_REMARK_CNT"] = $this->setcnt;

                    for ($j=1; $j <= $this->fieldH["GROUP_REMARK_CNT"]; $j++) {
                        //バイト数チェック
                        if (strlen($this->field["GROUP_REMARK".($seqadd + $j)]) > 6100) {
                            $this->setWarning("MSG915");
                            return false;
                        }
                    }
                } else if ($value == "GROUP_PROCEDURE") {
                    //バイト数チェック
                    if (strlen($this->field4[$value]) > 6100) {
                        $this->setWarning("MSG915");
                        return false;
                    }
                } else if ($value == "GROUP_YEAR_TARGET") {
                    //行数チェック
                    if ($this->validate_row_cnt($this->field3[$value], ($moji * 2)) > $gyo) {
                        $this->setWarning("MSG915","({$this->item[$label]}は{$gyo}行までです)");
                        return false;
                    }
                    $field3 .= ($field3 == "") ? $value : ','.$value;
                }
            }
        }

        if (strlen($this->grade_hr_class) == "2") {
            $ghr_cd = $this->grade_hr_class;
            list ($grade, $hr_class) = explode('-', $this->grade_hr_class2);
        } else {
            $ghr_cd = '00';
            list ($grade, $hr_class) = explode('-', $this->grade_hr_class);
        }

        if ($this->unit_aim_div == "0") {
            //削除 -- HREPORT_GUIDANCE_GROUP_DAT
            $query = knjd419Query::getDeleteQuery1($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
            //更新 -- HREPORT_GUIDANCE_GROUP_DAT
            $pattern = $this->pattern[$this->unit_aim_div][$this->guidance_pattern];
            for ($i=0; $i < get_count($pattern); $i++) {
                list ($label, $value, $moji, $gyo, $seqadd) = $pattern[$i];
                if ($value == "" && $this->fieldH["GROUP_REMARK_CNT"] > 0) {
                    for ($j=1; $j <= $this->fieldH["GROUP_REMARK_CNT"]; $j++) {
                        $seq = $j + $seqadd;
                        $query = knjd419Query::getUpdateQuery1($this, $ghr_cd, $grade, $hr_class, $seq, $this->field["GROUP_REMARK".$seq]);
                        $db->query($query);
                    }
                }
            }

            //削除 -- HREPORT_GUIDANCE_GROUP_HDAT
            $query = knjd419Query::getDeleteQuery4($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
            //更新 -- HREPORT_GUIDANCE_GROUP_HDAT
            $query = knjd419Query::getUpdateQuery4($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
        }
        if ($this->unit_aim_div == "1") {
            //削除 -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
            $query = knjd419Query::getDeleteQuery2($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
            //更新 -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
            $query = knjd419Query::getUpdateQuery2($this, $field2, $ghr_cd, $grade, $hr_class);
            $db->query($query);
        }
        if ($this->guidance_pattern == "7") {
            //削除 -- HREPORT_GUIDANCE_GROUP_YDAT
            $query = knjd419Query::getDeleteQuery3($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
            //更新 -- HREPORT_GUIDANCE_GROUP_YDAT
            $query = knjd419Query::getUpdateQuery3($this, $field3, $ghr_cd, $grade, $hr_class);
            $db->query($query);
        }
        if ($this->guidance_pattern == "2" || $this->guidance_pattern == "4") {
            //削除 -- HREPORT_GUIDANCE_GROUP_DAT(seq => 51)
            $query = knjd419Query::getDeleteQuery1($this, $ghr_cd, $grade, $hr_class, "51");
            $db->query($query);
            //更新 -- HREPORT_GUIDANCE_GROUP_DAT(seq => 51)
            $query = knjd419Query::getUpdateQuery1($this, $ghr_cd, $grade, $hr_class, "51", $this->field4["GROUP_PROCEDURE"]);
            $db->query($query);
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }

    //削除処理
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        if (strlen($this->grade_hr_class) == "2") {
            $ghr_cd = $this->grade_hr_class;
            list ($grade, $hr_class) = explode('-', $this->grade_hr_class2);
        } else {
            $ghr_cd = '00';
            list ($grade, $hr_class) = explode('-', $this->grade_hr_class);
        }

        if ($this->unit_aim_div == "0") {
            //削除 -- HREPORT_GUIDANCE_GROUP_DAT
            $query = knjd419Query::getDeleteQuery1($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
            //削除 -- HREPORT_GUIDANCE_GROUP_HDAT
            $query = knjd419Query::getDeleteQuery4($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
        }
        if ($this->unit_aim_div == "1") {
            //削除 -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
            $query = knjd419Query::getDeleteQuery2($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
        }
        if ($this->guidance_pattern == "7") {
            //削除 -- HREPORT_GUIDANCE_GROUP_YDAT
            $query = knjd419Query::getDeleteQuery3($this, $ghr_cd, $grade, $hr_class);
            $db->query($query);
        }
        if ($this->guidance_pattern == "2" || $this->guidance_pattern == "4") {
            //削除 -- HREPORT_GUIDANCE_GROUP_DAT
            $query = knjd419Query::getDeleteQuery1($this, $ghr_cd, $grade, $hr_class, "51");
            $db->query($query);
        }

        $this->field = array();
        $this->field2 = array();
        $this->field3 = array();
        $this->field4 = array();
        $this->fieldH = array();

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
        return;
    }

    //コピー処理
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        //MAX学期取得
        $maxSem = array();
        $result = $db->query(knjd419Query::getMaxSemester());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $maxSem[$row["YEAR"]] = $row["MAX_SEMESTER"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //前年度・学期設定
        if (intval($this->semester - 1) > 0) {
            $pre_year     = $this->year;
            $pre_semester = intval($this->semester - 1);
        } else {
            $pre_year     = intval($this->year - 1);
            $pre_semester = $maxSem[$pre_year];
        }

        if ($this->validateInput($pre_year, $pre_semester)) {
            $db = Query::dbCheckOut();

            //前年度のデータ取得 -- HREPORT_GUIDANCE_GROUP_DAT
            $result = $db->query(knjd419Query::copyPreData1($this, $pre_year, $pre_semester));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー -- HREPORT_GUIDANCE_GROUP_DAT
                $db->query(knjd419Query::copyInsertQuery1($this, $row));
            }
            $result->free();

            //前年度のデータ取得 -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
            $result = $db->query(knjd419Query::copyPreData2($this, $pre_year, $pre_semester));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー -- HREPORT_GUIDANCE_GROUP_UNIT_DAT
                $db->query(knjd419Query::copyInsertQuery2($this, $row));
            }
            $result->free();

            //前年度のデータ取得 -- HREPORT_GUIDANCE_GROUP_YDAT
            $result = $db->query(knjd419Query::copyPreData3($this, $pre_year, $pre_semester));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー -- HREPORT_GUIDANCE_GROUP_YDAT
                $db->query(knjd419Query::copyInsertQuery3($this, $row));
            }
            $result->free();

            //前年度のデータ取得 -- HREPORT_GUIDANCE_GROUP_HDAT
            $result = $db->query(knjd419Query::copyPreData4($this, $pre_year, $pre_semester));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー -- HREPORT_GUIDANCE_GROUP_HDAT
                $db->query(knjd419Query::copyInsertQuery4($this, $row));
            }
            $result->free();

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
