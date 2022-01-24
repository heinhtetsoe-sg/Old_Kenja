<?php

require_once('for_php7.php');

class knjd418Model extends Model {
    var $cmd;
    var $control;
    var $year;
    var $semester;
    var $gakubu_school_kind = "";
    var $condition = "";
    var $guidance_pattern = "";
    var $pattern;

    function knjd418Model() {
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
            unset($this->condition);
            unset($this->guidance_pattern);
            $this->field = array();
        }

        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if ($this->Properties["useGradeKindCompGroupSemester"] == "1") {
            if (VARS::post("SEMESTER") != "") {
                $this->semester = VARS::request("SEMESTER");
            }
        } else {
            $this->semester = "9";
        }
        if (VARS::post("GAKUBU_SCHOOL_KIND") != "") {
            $this->gakubu_school_kind = VARS::request("GAKUBU_SCHOOL_KIND");
        }

        if (VARS::get("CONDITION") != "") {
            $this->condition = VARS::get("CONDITION");
        }
        if (VARS::get("GUIDANCE_PATTERN") != "") {
            $this->guidance_pattern = VARS::get("GUIDANCE_PATTERN");
        }

        if (VARS::post("CONDITION") != "") {
            $this->condition = VARS::post("CONDITION");
        }
        if (VARS::post("GUIDANCE_PATTERN") != "") {
            $this->guidance_pattern = VARS::post("GUIDANCE_PATTERN");
        }

        //指導計画帳票パターン別設定
        $this->pattern["1"] = array("1" =>  "内容",
                                    "2" =>  "ねらい",
                                    "3" =>  "評価",
                                    "4" =>  "文言評価");
        $this->pattern["2"] = array("2" =>  "ねらい",
                                    "5" =>  "手立て",
                                    "4" =>  "文言評価");
        $this->pattern["3"] = array("1" =>  "内容",
                                    "2" =>  "ねらい",
                                    "4" =>  "文言評価");
        $this->pattern["4"] = array("2" =>  "ねらい",
                                    "5" =>  "手立て",
                                    "4" =>  "文言評価",
                                    "8" =>  "備考");
        $this->pattern["5"] = array("1" =>  "単元題材",
                                    "2" =>  "評価項目",
                                    "3" =>  "評価");
        $this->pattern["6"] = array("2" =>  "評価項目",
                                    "3" =>  "評価",
                                    "6" =>  "評定");
        $this->pattern["7"] = array("7" =>  "年間目標",
                                    "1" =>  "単元・題材",
                                    "2" =>  "評価の項目",
                                    "3" =>  "評価",
                                    "4" =>  "文言評価");
        $this->pattern["8"] = array("2" =>  "学習のめあて",
                                    "3" =>  "評価",
                                    "4" =>  "文言評価");
        $this->pattern["9"] = array("2" =>  "評価の項目",
                                    "3" =>  "評価");
        $this->pattern["A"] = array("1" =>  "指導目標",
                                    "2" =>  "重点目標",
                                    "3" =>  "指導の具体的な手立て",
                                    "4" =>  "結果及び課題");
        $this->pattern["B"] = array("1" =>  "目標",
                                    "2" =>  "指導内容・方法",
                                    "3" =>  "学習の様子および所見");

        if ($this->guidance_pattern) {
            $this->field = array();
            if ($this->pattern[$this->guidance_pattern]) {
                foreach ($this->pattern[$this->guidance_pattern] as $key => $val) {
                    $this->field["ITEM_REMARK".$key] =  VARS::post("ITEM_REMARK".$key);
                }
            }
        } else {
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput($pre_year, $pre_semester) {

        if ($this->cmd == "update") {
            //必須チェック
            if ($this->condition == "") {
                $this->setWarning("MSG301", "（状態区分）");
                return false;
            }
            if ($this->guidance_pattern == "") {
                $this->setWarning("MSG301", "（指導計画帳票パターン）");
                return false;
            }

            //テキスト入力チェック
            $cnt = 1;
            $remark = "";
            foreach ($this->pattern[$this->guidance_pattern] as $key => $val) {
                if (strlen($this->field["ITEM_REMARK".$key]) > 30 ) {
                    $this->setWarning("MSG914","(項目".$cnt."は30バイトまでです)");
                    return false;
                }
                $remark .= $this->field["ITEM_REMARK".$key];
                $cnt++;
            }
            if (strlen($remark) == 0 ) {
                $this->setWarning("MSG301","値を入力してください。");
                return false;
            }
        } else if ($this->cmd == "delete") {
            //必須チェック
            if ($this->condition == "") {
                $this->setWarning("MSG301", "（状態区分）");
                return false;
            }
            if ($this->guidance_pattern == "") {
                $this->setWarning("MSG301", "（指導計画帳票パターン）");
                return false;
            }
        } else if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            //存在チェック（前年度） -- HREPORT_GUIDANCE_ITEM_NAME_DAT
            $cnt_schreg_pre = $db->getOne(knjd418Query::checkHreportGuidanceItemNameDat($pre_year, $pre_semester));
            if ($cnt_schreg_pre == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
                return false;
            }

            //存在チェック（指定年度） -- HREPORT_GUIDANCE_ITEM_NAME_DAT
            $cnt_schreg_this = $db->getOne(knjd418Query::checkHreportGuidanceItemNameDat($this->year, $this->semester));
            if ($cnt_schreg_this > 0) {
                if ($this->Properties["useGradeKindCompGroupSemester"] == "1") {
                    $warning = "指定年度・学期にデータが存在します。";
                } else {
                    $warning = "指定年度にデータが存在します。";
                }
                $this->setWarning("MSG917",$warning);
                return false;
            }
            Query::dbCheckIn($db);
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("", "")) {
            knjd418Query::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->setCmd("edit");
        } else {
            $this->setCmd("check");
        }

        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("", "")) {
            knjd418Query::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        //MAX学期取得
        $maxSem = array();
        $result = $db->query(knjd418Query::getMaxSemester());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $maxSem[$row["YEAR"]] = $row["MAX_SEMESTER"];
        }
        Query::dbCheckIn($db);
        //前年度・学期設定
        if ($this->Properties["useGradeKindCompGroupSemester"] == "1") {
            if (intval($this->semester - 1) > 0) {
                $pre_year     = $this->year;
                $pre_semester = intval($this->semester - 1);
            } else {
                $pre_year     = intval($this->year - 1);
                $pre_semester = $maxSem[$pre_year];
            }
        } else {
            $pre_year     = intval($this->year - 1);
            $pre_semester = "9";
        }

        if ($this->validateInput($pre_year, $pre_semester)) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $cnt = 0;
            $result = $db->query(knjd418Query::copyHreportGuidanceItemNameDat($this, $pre_year, $pre_semester));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjd418Query::copyInsertQuery($this, $row));
                $cnt++;
            }
            Query::dbCheckIn($db);
            if ($cnt == 0) {
                $this->setMessage("MSG917","対象データが存在しません。");
            } else {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
?>
