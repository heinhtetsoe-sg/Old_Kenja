<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjh410_assessModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $attendno;
    var $field = array();
    var $control;

    function knjh410_assessModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        if (VARS::request("SCHREGNO") != "" && $this->cmd == "edit") {
            $db = Query::dbCheckOut();
            $this->schregno = VARS::request("SCHREGNO");
            $this->year = VARS::request("EXP_YEAR");
            $this->semester = VARS::request("EXP_SEMESTER");
            
            $query = knjh410_assessQuery::getSchregData($this->schregno, $this->year, $this->semester);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $this->grade = $row["GRADE"];
            $this->hr_class = $row["HR_CLASS"];
            $this->attendno = $row["ATTENDNO"];
            $this->name = $row["NAME"];
            $this->hrname = $row["HR_NAME"];
            
            Query::dbCheckIn($db);
            unset($this->seq);
            unset($this->relano);
        }

        for ($i=1; $i <= 10; $i++) {
            $div = sprintf("%02d", $i);
            //設問
            for ($j=1; $j <= 15; $j++) {
                $this->field[$div."_QUESTION".$j] = VARS::post($div."_QUESTION".$j);
            }
            //所見
            $this->field[$div."_REMARK1"] = VARS::post($div."_REMARK1");
            $this->field[$div."_REMARK2"] = VARS::post($div."_REMARK2");
        }
        $this->field["HANDICAP"]            = VARS::post("HANDICAP");
        $this->field["DIAGNOSIS_DATE"]      = $this->getSeirekiHenkan(VARS::post("DIAGNOSIS_DATE"));
        $this->field["INSTITUTES_CD"]       = VARS::post("INSTITUTES_CD");
        $this->field["ATTENDING_DOCTOR"]    = VARS::post("ATTENDING_DOCTOR");
        $this->field["REMARK"]              = VARS::post("REMARK");
        $this->field["MEDICINE_FLG"]        = VARS::post("MEDICINE_FLG");
        $this->field["MEDICINE_NAME"]       = VARS::post("MEDICINE_NAME");
        $this->field["EXAMINATION_DATE_1"]  = $this->getSeirekiHenkan(VARS::post("EXAMINATION_DATE_1"));
        $this->field["EXAM_INST_CD_1"]      = VARS::post("EXAM_INST_CD_1");
        $this->field["TESTER_NAME_1"]       = VARS::post("TESTER_NAME_1");
        $this->field["REMARK1_1"]           = VARS::post("REMARK1_1");
        $this->field["REMARK2_1"]           = VARS::post("REMARK2_1");
        $this->field["REMARK3_1"]           = VARS::post("REMARK3_1");
        $this->field["OTHER_TEXT_1"]        = VARS::post("OTHER_TEXT_1");
        $this->field["EXAMINATION_DATE_2"]  = $this->getSeirekiHenkan(VARS::post("EXAMINATION_DATE_2"));
        $this->field["EXAM_INST_CD_2"]      = VARS::post("EXAM_INST_CD_2");
        $this->field["TESTER_NAME_2"]       = VARS::post("TESTER_NAME_2");
        $this->field["REMARK1_2"]           = VARS::post("REMARK1_2");
        $this->field["REMARK2_2"]           = VARS::post("REMARK2_2");
        $this->field["REMARK3_2"]           = VARS::post("REMARK3_2");
        $this->field["REMARK4_2"]           = VARS::post("REMARK4_2");
        $this->field["REMARK5_2"]           = VARS::post("REMARK5_2");
        $this->field["REMARK6_2"]           = VARS::post("REMARK6_2");
        $this->field["REMARK7_2"]           = VARS::post("REMARK7_2");
        $this->field["OTHER_TEXT_2"]        = VARS::post("OTHER_TEXT_2");
        $this->field["P_S_YM"]              = $this->getSeirekiHenkan(VARS::post("P_S_YM"));
        $this->field["P_E_YM"]              = $this->getSeirekiHenkan(VARS::post("P_E_YM"));
        $this->field["P_PASSING_GRADE_FLG"] = VARS::post("P_PASSING_GRADE_FLG");
        $this->field["P_SUPPORT_FLG"]       = VARS::post("P_SUPPORT_FLG");
        $this->field["P_DATE_S_YM"]         = $this->getSeirekiHenkan(VARS::post("P_DATE_S_YM"));
        $this->field["P_DATE_E_YM"]         = $this->getSeirekiHenkan(VARS::post("P_DATE_E_YM"));
        $this->field["J_S_YM"]              = $this->getSeirekiHenkan(VARS::post("J_S_YM"));
        $this->field["J_E_YM"]              = $this->getSeirekiHenkan(VARS::post("J_E_YM"));
        $this->field["J_PASSING_GRADE_FLG"] = VARS::post("J_PASSING_GRADE_FLG");
        $this->field["J_SUPPORT_FLG"]       = VARS::post("J_SUPPORT_FLG");
        $this->field["J_DATE_S_YM"]         = $this->getSeirekiHenkan(VARS::post("J_DATE_S_YM"));
        $this->field["J_DATE_E_YM"]         = $this->getSeirekiHenkan(VARS::post("J_DATE_E_YM"));
        $this->field["EDUCATION_TEXT"]      = VARS::post("EDUCATION_TEXT");
        $this->field["CONS_INST_CD_1"]      = VARS::post("CONS_INST_CD_1");
        $this->field["CONSULT_DATE_1"]      = $this->getSeirekiHenkan(VARS::post("CONSULT_DATE_1"));
        $this->field["CONSULT_TEXT_1"]      = VARS::post("CONSULT_TEXT_1");
        $this->field["CONS_INST_CD_2"]      = VARS::post("CONS_INST_CD_2");
        $this->field["CONSULT_DATE_2"]      = $this->getSeirekiHenkan(VARS::post("CONSULT_DATE_2"));
        $this->field["CONSULT_TEXT_2"]      = VARS::post("CONSULT_TEXT_2");
        $this->field["CONS_INST_CD_3"]      = VARS::post("CONS_INST_CD_3");
        $this->field["CONSULT_DATE_3"]      = $this->getSeirekiHenkan(VARS::post("CONSULT_DATE_3"));
        $this->field["CONSULT_TEXT_3"]      = VARS::post("CONSULT_TEXT_3");
        $this->field["DATE"]                = VARS::post("DATE");
    }

    //入力項目チェック
    function validateInput($cmd) {
        //必須チェック
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return false;
        }

        if ($cmd == "subform1") {
            //文字数チェック
            foreach ($this->assess as $key => $val) {
                if ($key == "01") {
                    if ($this->validate_row_cnt($this->field[$key."_REMARK2"], ($val["moji"] * 2)) > $val["gyo"]) {
                        $this->setWarning("MSG915","({$val["title"]}のその他は{$val["moji"]}文字までです)");
                        return false;
                    }
                } else {
                    if ($this->validate_row_cnt($this->field[$key."_REMARK1"], ($val["moji"] * 2)) > $val["gyo"]) {
                        $this->setWarning("MSG915","({$val["title"]}は{$val["gyo"]}行までです)");
                        return false;
                    }
                }
            }
        } else if ($cmd == "subform2") {
            //文字数チェック
            $checkitem = array();
            $checkitem[] = array("name" => "HANDICAP",          "moji" => 50, "gyo" => 1, "label" => "障害名");
            $checkitem[] = array("name" => "ATTENDING_DOCTOR",  "moji" => 10, "gyo" => 1, "label" => "主治医");
            $checkitem[] = array("name" => "REMARK",            "moji" => 25, "gyo" => 1, "label" => "備考");
            $checkitem[] = array("name" => "MEDICINE_NAME",     "moji" => 25, "gyo" => 1, "label" => "薬剤名");
            $checkitem[] = array("name" => "TESTER_NAME_1",     "moji" => 10, "gyo" => 1, "label" => "（新版Ｋ式）検査者");
            $checkitem[] = array("name" => "REMARK1_1",         "moji" => 3,  "gyo" => 0, "label" => "認知・適応(CA)");
            $checkitem[] = array("name" => "REMARK2_1",         "moji" => 3,  "gyo" => 0, "label" => "言語・社会(LS)");
            $checkitem[] = array("name" => "REMARK3_1",         "moji" => 3,  "gyo" => 0, "label" => "全領域(DQ)");
            $checkitem[] = array("name" => "OTHER_TEXT_1",      "moji" => 26, "gyo" => 4, "label" => "（新版Ｋ式）解釈・特記事項等");
            $checkitem[] = array("name" => "TESTER_NAME_2",     "moji" => 10, "gyo" => 1, "label" => "（WISC-Ⅲ）検査者");
            $checkitem[] = array("name" => "REMARK1_2",         "moji" => 3,  "gyo" => 0, "label" => "言語性IQ");
            $checkitem[] = array("name" => "REMARK2_2",         "moji" => 3,  "gyo" => 0, "label" => "動作性IQ");
            $checkitem[] = array("name" => "REMARK3_2",         "moji" => 3,  "gyo" => 0, "label" => "全検査IQ");
            $checkitem[] = array("name" => "REMARK4_2",         "moji" => 3,  "gyo" => 0, "label" => "言語理解");
            $checkitem[] = array("name" => "REMARK5_2",         "moji" => 3,  "gyo" => 0, "label" => "知覚統合");
            $checkitem[] = array("name" => "REMARK6_2",         "moji" => 3,  "gyo" => 0, "label" => "注意記憶");
            $checkitem[] = array("name" => "REMARK7_2",         "moji" => 3,  "gyo" => 0, "label" => "処理速度");
            $checkitem[] = array("name" => "OTHER_TEXT_2",      "moji" => 26, "gyo" => 4, "label" => "（WISC-Ⅲ）解釈・特記事項等");;
            $checkitem[] = array("name" => "EDUCATION_TEXT",    "moji" => 50, "gyo" => 4, "label" => "中学校からの引き継ぎ");
            $checkitem[] = array("name" => "CONSULT_TEXT_1",    "moji" => 25, "gyo" => 5, "label" => "相談内容１");
            $checkitem[] = array("name" => "CONSULT_TEXT_2",    "moji" => 25, "gyo" => 5, "label" => "相談内容２");
            $checkitem[] = array("name" => "CONSULT_TEXT_3",    "moji" => 25, "gyo" => 5, "label" => "相談内容３");

            //文字数チェック
            foreach ($checkitem as $key => $val) {
                if ($val["gyo"] == "0") {
                    if (strlen($this->field[$val["name"]]) > $val["moji"]) {
                        $this->setWarning("MSG915", "{$val["label"]}は{$val["moji"]}バイトまでです");
                        return false;
                    }
                } else {
                    $message = ($val["gyo"] == "1") ? $val["moji"]."文字" : $val["gyo"]."行";
                    if ($this->validate_row_cnt($this->field[$val["name"]], ($val["moji"] * 2)) > $val["gyo"]) {
                        $this->setWarning("MSG915","({$val["label"]}は{$message}までです)");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();

        $cmd = preg_split("/_/", $this->cmd);

        if (!$this->validateInput($cmd[0])) {
            $this->cmd = $cmd[0]."A";
            return;
        }

        if ($cmd[0] == "subform1") {
            knjh410_assessQuery::getUpdateQuery1($db, $this, $this->field);
        } else if ($cmd[0] == "subform2") {
            knjh410_assessQuery::getUpdateQuery2($db, $this, $this->field);
        }

        $this->cmd = $cmd[0]."A";
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }

    //対象のフィールド西暦変換
    function getSeirekiHenkan($year_month) {
        if ($year_month) {
            $setYearMonth = array();
            $setYearMonth = preg_split("/\//", $year_month);
            $gengo = substr($setYearMonth[0], 0, 1);
            $warekiyear = substr($setYearMonth[0], 1, 2);
            $warekiList = common::getWarekiList();
            // 和暦から西暦
            foreach($warekiList as $w) {
                if ($gengo == $w["SName"]) {
                    $seireki = intval($warekiyear) + intval($w["YearStart"]) - 1;
                    break;
                }
            }
            $year_month = $seireki.'-'.$setYearMonth[1];
        }
        return $year_month;
    }
}
?>
