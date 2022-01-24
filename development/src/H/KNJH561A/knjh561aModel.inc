<?php

require_once('for_php7.php');

class knjh561aModel extends Model {
    var $field = array();
    var $cmd;
    var $subclassGroup;
    var $control;       //コントロールマスタ

    function knjh561aModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),   //データ種別
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),    //テスト名称
                             "SELECT_DIV"       =>  VARS::post("SELECT_DIV"),       //選択区分
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),        //順位
                             "JUNI"             =>  VARS::post("JUNI"),             //順位の基準点
                             "SORT"             =>  VARS::post("SORT"),             //出力順
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->selectData = VARS::post("selectData");

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;
        $this->useKnjd106cJuni1 = (strlen($this->Properties["useKnjd106cJuni1"])) ? $this->Properties["useKnjd106cJuni1"] : 1;
        $this->useKnjd106cJuni2 = (strlen($this->Properties["useKnjd106cJuni2"])) ? $this->Properties["useKnjd106cJuni2"] : 2;
        $this->useKnjd106cJuni3 = (strlen($this->Properties["useKnjd106cJuni3"])) ? $this->Properties["useKnjd106cJuni3"] : 3;
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {

        //DB接続
        $db = Query::dbCheckOut();

        /** 合計の科目コード */
        $SUBCLASSCD_ALL3 = "333333";
        $SUBCLASSCD_ALL5 = "555555";
        $SUBCLASSCD_ALL9 = "999999";

        $PRINT_TYPE_CLASS = 1;
        $PRINT_TYPE_COURSECD = 2;
        $PRINT_TYPE_GRADE = 3;
        $RANK_DIV_GRADE = "01";
        $RANK_DIV_HRCLASS = "02";
        $RANK_DIV_COURSE = "03";
        $RANK_DIV_COURSEGROUP = "05";
        $INVALID_INDEX = -100; // 無効なインデックス
        $MAX_LINE = 45;
        $MAX_SUBCLASS = 5;
        $RANK_DATA_DIV_SCORE = "01";
        if ($this->field["SELECT_DIV"] == "1") {
            $avgDiv = "02";
        } else if ($this->field["SELECT_DIV"] == "2") {
            $avgDiv = "03";
        } else {
            $avgDiv = "01";
        }
        $query = knjh561aQuery::chuKoIkkan();
        $isChuKoIkkan = $db->getOne($query) ? true : false;

        if ($this->field["JUNI"] == "1") {
            $rankDivTemp = $this->useKnjd106cJuni1;
        } else if ($this->field["JUNI"] == "2") {
            $rankDivTemp = $this->useKnjd106cJuni2;
        } else if ($this->field["JUNI"] == "3") {
            $rankDivTemp = $this->useKnjd106cJuni3;
        }
        $rankDataDiv = $rankDivTemp ? $rankDivTemp : $this->field["JUNI"];
        $rankDataSub = strlen($rankDataDiv) < 2 ? "0" : "";
        $rankDataDiv = $rankDataSub.$rankDataDiv;
        $avgDataDiv = "1";
        $query = knjh561aQuery::getTestName($this);
        $testName = $db->getOne($query);

        if ($model->field["SELECT_DIV"] == $PRINT_TYPE_GRADE) {
            $selectArray[] = $this->field["GRADE"];
        } else {
            $selectArray = preg_split("/,/", $this->selectData);
        }
        $group = array();
        for ($cnt = 0; $cnt < get_count($selectArray); $cnt++) {
            $selectCnt = $selectArray[$cnt];
            $group[$selectCnt] = array();
            $query = knjh561aQuery::getStudentSql($this, $selectCnt, $PRINT_TYPE_CLASS, $PRINT_TYPE_GRADE, $PRINT_TYPE_COURSECD, $rankDataDiv, $RANK_DIV_GRADE, $RANK_DIV_HRCLASS, $RANK_DIV_COURSE, $RANK_DIV_COURSEGROUP, $RANK_DATA_DIV_SCORE);
            $result = $db->query($query);
            $csvDataCnt = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schregNo = $row["SCHREGNO"];
                if (!is_array($group[$selectCnt]["STUDENT"][$schregNo])) {
                    $group[$selectCnt]["STUDENT"][$schregNo]["YEAR"]        = $row["YEAR"];
                    $group[$selectCnt]["STUDENT"][$schregNo]["SCHREGNO"]    = $schregNo;
                    $group[$selectCnt]["STUDENT"][$schregNo]["GRADE"]       = $row["GRADE"];
                    $group[$selectCnt]["STUDENT"][$schregNo]["CLASSNO"]     = $row["CLASS1"];
                    $group[$selectCnt]["STUDENT"][$schregNo]["HRNAME"]      = $row["HR_NAME"];
                    $group[$selectCnt]["STUDENT"][$schregNo]["COURSECD"]    = $row["COURSECODE1"];
                    $group[$selectCnt]["STUDENT"][$schregNo]["ATTENDNO"]    = ($row["ATTENDNO"] * 1);
                    $group[$selectCnt]["STUDENT"][$schregNo]["NAME"]        = $row["NAME"];
                    $group[$selectCnt]["STUDENT"][$schregNo]["SEXNAME"]     = $row["SEXNAME"];

                    $group[$selectCnt]["GRADE"]             = $row["GRADE"];
                    $group[$selectCnt]["GRADEHRCLASS"]      = $row["CLASS1"];
                    $group[$selectCnt]["HRNAME"]            = $row["HR_NAME"];
                    $group[$selectCnt]["COURSECD"]          = $row["COURSECODE1"];
                    $group[$selectCnt]["COURSECODENAME"]    = $row["COURSECODENAME"];
                    $group[$selectCnt]["GROUP_CD"]          = $row["GROUP_CD"];
                    $group[$selectCnt]["GROUP_NAME"]        = $row["GROUP_NAME"];
                    $group[$selectCnt]["STUDENT_SORT"][]    = $schregNo;
                }
                $subclassCd = $row["SUBCLASSCD"];
                $setScoreVal = strlen($row["SCORE"]) == 0 ? $row["SCORE_DI"] : $row["SCORE"];
                $avg              = $row["AVG"];
                $classRank        = $row["CLASS_RANK"];
                $gradeRank        = $row["GRADE_RANK"];
                $courseRank       = $row["COURSE_RANK"];
                $coursegroupRank  = $row["COURSEGROUP_RANK"];
                $classDev         = $row["CLASS_DEVIATION"];
                $gradeDev         = $row["GRADE_DEVIATION"];
                $courseDev        = $row["COURSE_DEVIATION"];
                $coursegroupDev   = $row["COURSEGROUP_DEVIATION"];

                if ($subclassCd != $SUBCLASSCD_ALL3 && $subclassCd != $SUBCLASSCD_ALL5 && $subclassCd != $SUBCLASSCD_ALL9) {
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["SCORE"]              = $setScoreVal;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["AVG"]                = $avg;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["HR_RANK"]            = $classRank;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["GRADE_RANK"]         = $gradeRank;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["COURSE_RANK"]        = $courseRank;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["COURSEGROUP_RANK"]   = $coursegroupRank;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["HR_DEV"]             = $classDev;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["GRADE_DEV"]          = $gradeDev;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["COURSE_DEV"]         = $courseDev;
                    $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["COURSEGROUP_DEV"]    = $coursegroupDev;
                } else {
                    if ($subclassCd == $SUBCLASSCD_ALL3) {
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["SCORE"]              = $setScoreVal;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["AVG"]                = round($avg * 100, -1) / 100;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["HR_RANK"]            = $classRank;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["GRADE_RANK"]         = $gradeRank;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["COURSE_RANK"]        = $courseRank;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["COURSEGROUP_RANK"]   = $coursegroupRank;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["HR_DEV"]             = $classDev;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["GRADE_DEV"]          = $gradeDev;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["COURSE_DEV"]         = $courseDev;
                        $group[$selectCnt]["STUDENT"][$schregNo][$subclassCd]["COURSEGROUP_DEV"]    = $coursegroupDev;
                    }
                }
                $csvDataCnt++;
            }
            $result->free();

            if ($csvDataCnt == 0) {
                unset($group[$selectCnt]);
                continue;
            }

            //科目配列
            $query = knjh561aQuery::getSubclassSql($this, $this->field["GRADE"], $selectCnt, $PRINT_TYPE_CLASS, $PRINT_TYPE_COURSECD, $PRINT_TYPE_GRADE);
            $result = $db->query($query);
            $subclassArray[$selectCnt] = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $checkFlg = false;
                foreach ($subclassArray[$selectCnt] as $checkKey => $checkVal) {
                    if ($row["SUBCLASSCD"] == $checkVal["CD"]) {
                        $checkFlg = true;
                    }
                }
                if (!$checkFlg) {
                    $subclassArray[$selectCnt][] = array("CD" => $row["SUBCLASSCD"], "NAME" => $row["SUBCLASS_NAME"]);
                }
            }
            $result->free();

            //科目平均
            $query = knjh561aQuery::getAverageSql($this, $group[$selectCnt]["GRADEHRCLASS"], $this->field["GRADE"], $group[$selectCnt]["COURSECD"], $avgDataDiv, $avgDiv);
            $result = $db->query($query);
            $subclassAvg = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclassAvg[$selectCnt][$row["SUBCLASSCD"]] = array("SCORE" => $row["SCORE"], "AVG" => $row["AVG"]);
            }
            $result->free();

            //科目グループ
            $query = knjh561aQuery::getSubclassGroupMstSql($this, $group[$selectCnt]["COURSECD"]);
            $result = $db->query($query);
            $subclassGroupName = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclassGroupName[$selectCnt][$row["GROUP_DIV"]] = $row["GROUP_NAME"];
            }
            $result->free();
        }

        $contents  = "";    //クリア
        $header_data = false;
        list($mkYear, $mkMonth, $mkDay) = preg_split("/-/", CTRL_DATE);
        $setMkDate = $mkYear."年".($mkMonth * 1)."月".($mkDay * 1)."日";
        foreach ($group as $key => $val) {
            $course = $PRINT_TYPE_COURSECD == $this->field["SELECT_DIV"] ? $val["COURSECODENAME"]."コース" : "";
            $setG = ($val["GRADE"] * 1);
            $contents .= ",,,,,,,,第{$setG}学年{$course}　".CTRL_YEAR."年度{$testName}成績一覧表\n";
            $setSubTitle = "";
            if ($this->field["SORT"] == "2") {
                $setSubTitle = "出力順：３教科順";
            } else if ($this->field["SORT"] == "3") {
                $setSubTitle = "出力順：年組番順";
            }
            $contents .= ",,,,,,,,　　　　　　　　　({$setSubTitle})\n";
            $contents .= ",,,,,,,,,,,,{$setMkDate}\n";
            $setNenkumi = "";
            if ($this->field["SELECT_DIV"] == $PRINT_TYPE_CLASS) {
                $setNenkumi = $val["HRNAME"];
            } else if ($this->field["SELECT_DIV"] == $PRINT_TYPE_COURSECD) {
                $setNenkumi = $val["COURSECODENAME"];
            } else if ($this->field["SELECT_DIV"] == $PRINT_TYPE_GRADE) {
                $setNenkumi = $setG."年生";
            }
            $contents .= "{$setNenkumi}\n";
            $contents .= "No.,クラス,番号,氏名,性別,";
            $contents2 = ",,,,,";
            foreach ($subclassArray[$key] as $subclassVal) {
                $contents .= ",{$subclassVal["NAME"]},,";
                $contents2 .= "得点,偏差値,順位,";
            }
            $contents .= "合計,平均,偏差値,順位\n";
            $contents .= $contents2."\n";
            $setNo = 1;
            if ("2" == $this->field["GROUP_DIV"]) {
                $rankDev =  "COURSE_";
            } else if ("3" == $this->field["GROUP_DIV"]) {
                $rankDev =  "COURSEGROUP_";
            } else {
                $rankDev =  "GRADE_";
            }
            $students = array();
            foreach ($val["STUDENT"] as $schKey => $schVal) {
                $students[] = $schKey;
            }
            $c = get_count($students);
            // クラス出席番号順でソート
            for ($i = 0; $i < $c - 1; $i++) {
                for ($j = $i + 1; $j < $c; $j++) {
                    $schVal1 = $val["STUDENT"][$students[$i]];
                    $schVal2 = $val["STUDENT"][$students[$j]];
                    $hrclassattendno1 = $schVal1["CLASSNO"].$schVal1["ATTENDNO"];
                    $hrclassattendno2 = $schVal2["CLASSNO"].$schVal2["ATTENDNO"];
                    if ($hrclassattendno1 == '') $hrclassattendno1 = '999999';
                    if ($hrclassattendno2 == '') $hrclassattendno2 = '999999';
                    if ($hrclassattendno1 > $hrclassattendno2) {
                        $temp = $students[$i];
                        $students[$i] = $students[$j];
                        $students[$j] = $temp;
                    }
                }
            }
            if ($this->field["SORT"] == "2") { // "出力順：３教科順"
                for ($i = 0; $i < $c - 1; $i++) {
                    for ($j = $i + 1; $j < $c; $j++) {
                        $swap = false;
                        $schVal1 = $val["STUDENT"][$students[$i]];
                        $schVal2 = $val["STUDENT"][$students[$j]];
                        $rank1 = $schVal1[$SUBCLASSCD_ALL3][$rankDev."RANK"];
                        $rank2 = $schVal2[$SUBCLASSCD_ALL3][$rankDev."RANK"];
                        if ($rank1 == '') $rank1 = 9999;
                        if ($rank2 == '') $rank2 = 9999;
                        if ($rank1 > $rank2) {
                            $swap = true;
                        } else if ($rank1 == $rank2) {
                            $hrclassattendno1 = $schVal1["CLASSNO"].$schVal1["ATTENDNO"];
                            $hrclassattendno2 = $schVal2["CLASSNO"].$schVal2["ATTENDNO"];
                            if ($hrclassattendno1 == '') $hrclassattendno1 = '999999';
                            if ($hrclassattendno2 == '') $hrclassattendno2 = '999999';
                            if ($hrclassattendno1 > $hrclassattendno2) {
                                $swap = true;
                            }
                        }
                        if ($swap) {
                            $temp = $students[$i];
                            $students[$i] = $students[$j];
                            $students[$j] = $temp;
                        }
                    }
                }
            }
            foreach ($students as $schKey) {
                $schVal = $val["STUDENT"][$schKey];
                $contents .= "{$setNo},{$schVal["HRNAME"]},{$schVal["ATTENDNO"]},{$schVal["NAME"]},{$schVal["SEXNAME"]},";
                foreach ($subclassArray[$key] as $subclassVal) {
                    $contents .= "{$schVal[$subclassVal["CD"]]["SCORE"]},{$schVal[$subclassVal["CD"]][$rankDev."DEV"]},{$schVal[$subclassVal["CD"]][$rankDev."RANK"]},";
                }
                $contents .= "{$schVal[$SUBCLASSCD_ALL3]["SCORE"]},{$schVal[$SUBCLASSCD_ALL3]["AVG"]},{$schVal[$SUBCLASSCD_ALL3][$rankDev."DEV"]},{$schVal[$SUBCLASSCD_ALL3][$rankDev."RANK"]},";
                $contents .= "\n";
                $setNo++;
            }
            $header_data = true;
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "実力テスト成績一覧";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
