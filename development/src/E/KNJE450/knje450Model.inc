<?php

require_once('for_php7.php');
require_once('../../common/mycalendar.php');

class knje450Model extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $attendno;
    var $field = array();
    var $control;

    function knje450Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->writing_date);
            unset($this->field);
        }

        //作成年月日
        $this->writing_date         = VARS::post("WRITING_DATE");

        //作成日
        $this->field["WRT_DATE"]    = VARS::post("WRT_DATE");

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
        $this->field["P_ETC_FLG"]           = VARS::post("P_ETC_FLG");
        $this->field["P_ETC"]               = VARS::post("P_ETC");
        $this->field["P_DATE_S_YM"]         = $this->getSeirekiHenkan(VARS::post("P_DATE_S_YM"));
        $this->field["P_DATE_E_YM"]         = $this->getSeirekiHenkan(VARS::post("P_DATE_E_YM"));
        $this->field["J_S_YM"]              = $this->getSeirekiHenkan(VARS::post("J_S_YM"));
        $this->field["J_E_YM"]              = $this->getSeirekiHenkan(VARS::post("J_E_YM"));
        $this->field["J_PASSING_GRADE_FLG"] = VARS::post("J_PASSING_GRADE_FLG");
        $this->field["J_SUPPORT_FLG"]       = VARS::post("J_SUPPORT_FLG");
        $this->field["J_ETC_FLG"]           = VARS::post("J_ETC_FLG");
        $this->field["J_ETC"]               = VARS::post("J_ETC");
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

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
            unset($this->seq);
            unset($this->relano);
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO") != "") {
            $this->attendno = VARS::get("ATTENDNO");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
        }

        //アセスメント情報
        $this->assess = array();
        $this->assess["01"] = array("check" => 1, "moji" => 25, "gyo" => 1,  "title" => "支援が必要な項目");
        $this->assess["02"] = array("check" => 1, "moji" => 25, "gyo" => 12, "title" => "学習面");
        $this->assess["03"] = array("check" => 1, "moji" => 25, "gyo" => 7,  "title" => "生活・行動面");
        $this->assess["04"] = array("check" => 1, "moji" => 25, "gyo" => 12, "title" => "社会性・対人関係");
        $this->assess["05"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "総合所見１");
        $this->assess["06"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "総合所見２");
        $this->assess["09"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "保護者の希望");
        $this->assess["10"] = array("check" => 1, "moji" => 10, "gyo" => 1,  "title" => "進路について");
    }

    //入力項目チェック
    function validateInput($db, $cmd, $div) {
        //必須チェック
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return false;
        }

        if ($div == "insert") {
            //必須チェック
            if ($this->field["WRT_DATE"] == "") {
                $this->setWarning("MSG301", "（作成日）");
                return false;
            }

            //存在チェック
            $cnt = $db->getOne(knje450Query::checkExistsData($this, $this->field["WRT_DATE"], "ASSESSMENT_ANS_HDAT"));
            if ($cnt > 0) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        if ($div == "update") {
            //必須チェック
            if ($this->writing_date == "") {
                $this->setWarning("MSG304", "（作成年月日）");
                return false;
            }

            //存在チェック
            $cnt = $db->getOne(knje450Query::checkExistsData($this, $this->writing_date, "ASSESSMENT_ANS_HDAT"));
            if ($cnt == 0) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        if ($cmd == "subform1") {
            //文字数チェック
            foreach ($this->assess as $key => $val) {
                if ($key == "01" || $key == "10") {
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
            $checkitem[] = array("name" => "TESTER_NAME_2",     "moji" => 10, "gyo" => 1, "label" => "（WISC-Ⅳ）検査者");
            $checkitem[] = array("name" => "REMARK3_2",         "moji" => 3,  "gyo" => 0, "label" => "全検査IQ");
            $checkitem[] = array("name" => "REMARK4_2",         "moji" => 3,  "gyo" => 0, "label" => "言語理解");
            $checkitem[] = array("name" => "REMARK5_2",         "moji" => 3,  "gyo" => 0, "label" => "知覚推理");
            $checkitem[] = array("name" => "REMARK6_2",         "moji" => 3,  "gyo" => 0, "label" => "ワーキングメモリー");
            $checkitem[] = array("name" => "REMARK7_2",         "moji" => 3,  "gyo" => 0, "label" => "処理速度");
            $checkitem[] = array("name" => "OTHER_TEXT_2",      "moji" => 26, "gyo" => 4, "label" => "（WISC-Ⅳ）解釈・特記事項等");;
            $checkitem[] = array("name" => "P_ETC",             "moji" => 6,  "gyo" => 1, "label" => "（小学校）その他");
            $checkitem[] = array("name" => "J_ETC",             "moji" => 6,  "gyo" => 1, "label" => "（中学校）その他");
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

        $cmd = preg_split('/\_/', $this->cmd);

        if (!$this->validateInput($db, $cmd[0], $cmd[1])) {
            $this->cmd = $cmd[0]."A";
            Query::dbCheckIn($db);
            return;
        }

        $writing_date = ($cmd[1] == "insert") ? str_replace("/", "-", $this->field["WRT_DATE"]) : $this->writing_date;

        if ($cmd[0] == "subform1") {
            knje450Query::getUpdateQuery1($db, $this, $this->field, $cmd[1], $writing_date);
        } else if ($cmd[0] == "subform2") {
            knje450Query::getUpdateQuery2($db, $this, $this->field, $cmd[1], $writing_date);
        }

        $this->cmd = $cmd[0]."A";
        $this->setMessage("MSG201");
        if ($cmd[1] == "insert") {
            $this->writing_date = $writing_date;
            $this->field["WRT_DATE"] = "";
        }
        Query::dbCheckIn($db);

        return;
    }

    //対象のフィールド西暦変換
    function getSeirekiHenkan($year_month) {
        if ($year_month) {
            $setYearMonth = array();
            $setYearMonth = preg_split('/\//', $year_month);
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
