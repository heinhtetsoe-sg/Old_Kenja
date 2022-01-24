<?php

require_once('for_php7.php');

class knjd154kModel extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $field    = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $tutisyoTokubetuKatudo;

    function knjd154kModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->cmd      = VARS::request("cmd");
        $this->selectdata = VARS::request("selectdata");
        $this->ALL3 = "333333";
        $this->ALL5 = "555555";
        $this->ALL9 = "999999";

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd);
        }
        $this->field = array("OUTPUT" =>  VARS::post("OUTPUT"));   //総合点、平均点ラジオボタン

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $hasData = false;

        //テストデータ
        $testArray = array();
        $query = knjd154kQuery::getTestSql($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testArray[] = $row;
        }
        $result->free();

        //生徒データ
        $studentInfo = array();
        $query = knjd154kQuery::getStudentInfo($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["SET_NAME"] = $row["USE_REAL_NAME"] == "1" ? $row["REAL_NAME"] : $row["NAME"];
            $studentInfo[$row["SCHREGNO"]] = $row;
            $hasData = true;
        }
        $result->free();

        //学校マスタ
        $schoolMst = $db->getRow(knjd154kQuery::getSchoolMstSql(), DB_FETCHMODE_ASSOC);
        //年組
        list($grade, $hrClass) = preg_split("/-/", $this->hrClass);

        $header .= CTRL_YEAR."年度　個人成績表\n";
        list($year, $month, $day) = preg_split("/-/", CTRL_DATE);
        $header .= common::Calc_Wareki($year, $month, $day).$month."月".$day."日\n";
        $header .= $schoolMst["SCHOOLNAME1"]."\n";

        foreach ($studentInfo as $schregNo => $schInfo) {
            $contents .= $header;
            if ($grade > "03") {
                $contents .= $schInfo["MAJORNAME"].$schInfo["COURSECODENAME"]."　".$schInfo["HR_NAME"].$schInfo["ATTENDNO"]."番　".$schInfo["SET_NAME"]."\n";
            } else {
                $contents .= $schInfo["HR_NAME"].$schInfo["ATTENDNO"]."番　".$schInfo["SET_NAME"]."\n";
            }

            $contents .= $this->getSetData($db, $schInfo, $testArray);

            $contents .= "\n\n";

        }

        if ($hasData) {
            $couse = "個人成績表";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    function getSetData($db, $schInfo, $testArray) {
        $year = CTRL_YEAR;
        $setNendo = common::Calc_Wareki($year, "04", "01")."度　";
        $retContents = "";
        if ($schInfo["GRADE"] > "03") {
            foreach ($testArray as $testKey => $testVal) {
                $seiseki = array();
                $retContents .= $setNendo.$testVal["TESTITEMNAME"]."\n";

                $subclasscd = "";
                $query = knjd154kQuery::getRecordScoreTestAppointSql($this, $testVal, $schInfo["SCHREGNO"]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                    } else {
                        $subclasscd = $row["SUBCLASSCD"];
                    }
                    $seiseki[$subclasscd]["SCORE"] = "欠";
                    $seiseki[$subclasscd]["NAME"] = $row["SUBCLASSABBV"];
                }
                $result->free();

                $this->setRecordData($db, $seiseki, $schInfo, $testVal);

                $retContents .= "科目,";
                foreach ($seiseki as $seisekiKey => $seisekiVal) {
                    if ($this->ALL9 != $seisekiKey) {
                        $retContents .= $seisekiVal["NAME"].",";
                    }
                }
                $retContents .= "平均点,合計点,クラス順位,コース順位,学科順位\n";

                $retContents .= "得点,";
                foreach ($seiseki as $seisekiKey => $seisekiVal) {
                    if ($this->ALL9 != $seisekiKey) {
                        $retContents .= $seisekiVal["SCORE"].",";
                    }
                }
                $retContents .= $seiseki[$this->ALL9]["ALL_AVG"].",";
                $retContents .= $seiseki[$this->ALL9]["ALL_SCORE"].",";
                $retContents .= $seiseki[$this->ALL9]["ALL_CLASS_RANK"]."/".$seiseki[$this->ALL9]["ALL_CLASS_CNT"].",";
                $retContents .= $seiseki[$this->ALL9]["ALL_COURSE_RANK"]."/".$seiseki[$this->ALL9]["ALL_COURSE_CNT"].",";
                $retContents .= $seiseki[$this->ALL9]["ALL_MAJOR_RANK"]."/".$seiseki[$this->ALL9]["ALL_MAJOR_CNT"].",";
                $retContents .= "\n";

                $retContents .= "学科平均,";
                foreach ($seiseki as $seisekiKey => $seisekiVal) {
                    $retContents .= $seisekiVal["MAJOR_AVG"].",";
                }
                $retContents .= "\n\n";
            }
        } else {
            //対象テストでの全ての科目
            $subclassTotal = array();
            $subclassArray = array();
            $subclasscd = "";
            $query = knjd154kQuery::getRecordScoreSubClassAllSql($this, $schInfo["SCHREGNO"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                } else {
                    $subclasscd = $row["SUBCLASSCD"];
                }
                if ($this->ALL3 == $row["SUBCLASSCD"] || $this->ALL5 == $row["SUBCLASSCD"] || $this->ALL9 == $row["SUBCLASSCD"]) {
                    $subclassArray[$row["SUBCLASSCD"]]["SCORE"] = "";
                    $subclassArray[$row["SUBCLASSCD"]]["NAME"] = $row["SUBCLASSABBV"];
                } else {
                    $subclassArray[$subclasscd]["SCORE"] = "";
                    $subclassArray[$subclasscd]["NAME"] = $row["SUBCLASSABBV"];
                }
                if ($this->ALL3 != $row["SUBCLASSCD"] && $this->ALL5 != $row["SUBCLASSCD"] && $this->ALL9 != $row["SUBCLASSCD"]) {
                    $subclassTotal[$subclasscd]["SCORE"] = 0;
                    $subclassTotal[$subclasscd]["CNT"] = 0;
                }
            }
            $result->free();

            foreach ($testArray as $testKey => $testVal) {
                $seiseki = $subclassArray;

                //欠を設定
                $subclasscd = "";
                $query = knjd154kQuery::getKetuSql($this, $schInfo, $testVal);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                    } else {
                        $subclasscd = $row["SUBCLASSCD"];
                    }
                    $seiseki[$subclasscd]["SCORE"] = $row["KETU"];
                }
                $result->free();

                $this->setRecordData($db, $seiseki, $schInfo, $testVal);

                $retContents .= $setNendo.$testVal["TESTITEMNAME"]."\n";

                $retContents .= "科目,";
                foreach ($seiseki as $seisekiKey => $seisekiVal) {
                    if ($this->ALL3 != $seisekiKey && $this->ALL5 != $seisekiKey && $this->ALL9 != $seisekiKey) {
                        $retContents .= $seisekiVal["NAME"].",";
                    }
                }
                $retContents .= "5教科合計,5教科学年順位,全教科合計,全教科学年順位\n";

                $retContents .= "得点,";
                foreach ($seiseki as $seisekiKey => $seisekiVal) {
                    if ($this->ALL3 != $seisekiKey && $this->ALL5 != $seisekiKey && $this->ALL9 != $seisekiKey) {
                        $retContents .= $seisekiVal["SCORE"].",";
                        if ($seisekiVal["SCORE"] != "欠" && $seisekiVal["SCORE"] != "") {
                            $subclassTotal[$seisekiKey]["SCORE"] += $seisekiVal["SCORE"];
                            $subclassTotal[$seisekiKey]["CNT"]++;
                        }
                    }
                }
                $retContents .= $seiseki[$this->ALL5]["SCORE"].",";
                $retContents .= $seiseki[$this->ALL5]["GRADE_RANK"]."/".$seiseki[$this->ALL5]["GRADE_CNT"].",";
                $retContents .= $seiseki[$this->ALL9]["ALL_SCORE"].",";
                $retContents .= $seiseki[$this->ALL9]["ALL_GRADE_RANK"]."/".$seiseki[$this->ALL9]["ALL_GRADE_CNT"].",";
                $retContents .= "\n";

                $retContents .= "学年平均,";
                foreach ($seiseki as $seisekiKey => $seisekiVal) {
                    if ($this->ALL3 != $seisekiKey && $this->ALL5 != $seisekiKey && $this->ALL9 != $seisekiKey) {
                        $retContents .= $seisekiVal["GRADE_AVG"].",";
                    }
                }
                $retContents .= "\n";

                $retContents .= "学年順位,";
                foreach ($seiseki as $seisekiKey => $seisekiVal) {
                    if ($this->ALL3 != $seisekiKey && $this->ALL5 != $seisekiKey && $this->ALL9 != $seisekiKey) {
                        $retContents .= $seisekiVal["GRADE_RANK"].",";
                    }
                }
                $retContents .= "\n";
            }
            $retContents .= "教科合計,";
            foreach ($subclassTotal as $totalKey => $totalVal) {
                $retContents .= $totalVal["SCORE"].",";
            }
            $retContents .= "\n";

            $retContents .= "教科平均,";
            foreach ($subclassTotal as $totalKey => $totalVal) {
                $setTotalAvg = "";
                if ($totalVal["CNT"] > 0) {
                    $setTotalAvg = round((($totalVal["SCORE"] / $totalVal["CNT"]) * 100 / 100), 1);
                }
                $retContents .= $setTotalAvg.",";
            }
            $retContents .= "\n\n";

            //実力テスト
            $mockMst = array();
            $query = knjd154kQuery::getMockData($schInfo);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $mockMst[$row["MOCKCD"]] = $row["MOCKNAME1"];
            }
            $result->free();

            foreach ($mockMst as $mockCd => $mockName) {
                $retContents .= $setNendo.$mockName."\n";

                $mockData = $this->setMockData($db, $mockCd, $schInfo);

                $retContents .= "教科,";
                foreach ($mockData as $mockSub => $mockRank) {
                    if ($this->ALL3 != $mockSub && $this->ALL5 != $mockSub && $this->ALL9 != $mockSub) {
                        $retContents .= $mockRank["NAME"].",";
                    }
                }
                $retContents .= "5教科合計,5教科学年順位,全教科合計,全教科学年順位\n";

                $retContents .= "学年平均,";
                foreach ($mockData as $mockSub => $mockRank) {
                    if ($this->ALL3 != $mockSub && $this->ALL5 != $mockSub && $this->ALL9 != $mockSub) {
                        $retContents .= $mockRank["GRADE_AVG"].",";
                    }
                }
                $retContents .= $mockData[$this->ALL5]["SCORE"].",";
                $retContents .= $mockData[$this->ALL5]["GRADE_RANK"]."/".$mockData[$this->ALL5]["GRADE_CNT"].",";
                $retContents .= $mockData[$this->ALL9]["ALL_SCORE"].",";
                $retContents .= $mockData[$this->ALL9]["ALL_GRADE_RANK"]."/".$mockData[$this->ALL9]["ALL_GRADE_CNT"].",";
                $retContents .= "\n";

                $retContents .= "学年順位,";
                foreach ($mockData as $mockSub => $mockRank) {
                    if ($this->ALL3 != $mockSub && $this->ALL5 != $mockSub && $this->ALL9 != $mockSub) {
                        $retContents .= $mockRank["GRADE_RANK"].",";
                    }
                }
                $retContents .= "\n\n";
            }

        }
        $retContents .= "\n\n\n\n";
        return $retContents;
    }

    function setRecordData($db, &$seiseki, $schInfo, $testVal) {
        $subclasscd = "";
        $query = knjd154kQuery::getRecordRankAppointSql($this, $testVal, $schInfo["SCHREGNO"]);
        $result = $db->query($query);
        $avgField = ("2" == $this->field["OUTPUT"]) ? "AVG_" : "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
            } else {
                $subclasscd = $row["SUBCLASSCD"];
            }
            $seiseki[$subclasscd]["SCORE"]      = $row["SCORE"];
            $seiseki[$subclasscd]["GRADE_RANK"] = $row["GRADE_".$avgField."RANK"];
            $seiseki[$subclasscd]["CLASS_RANK"] = $row["CLASS_".$avgField."RANK"];
            $seiseki[$subclasscd]["MAJOR_RANK"] = $row["MAJOR_".$avgField."RANK"];
            if ($row["SUBCLASSCD"] == $this->ALL9) {
                $seiseki[$row["SUBCLASSCD"]]["ALL_SCORE"]       = $row["SCORE"];
                $seiseki[$row["SUBCLASSCD"]]["ALL_GRADE_RANK"]  = $row["GRADE_".$avgField."RANK"];
                $seiseki[$row["SUBCLASSCD"]]["ALL_CLASS_RANK"]  = $row["CLASS_".$avgField."RANK"];
                $seiseki[$row["SUBCLASSCD"]]["ALL_COURSE_RANK"] = $row["COURSE_".$avgField."RANK"];
                $seiseki[$row["SUBCLASSCD"]]["ALL_MAJOR_RANK"]  = $row["MAJOR_".$avgField."RANK"];
                $seiseki[$row["SUBCLASSCD"]]["ALL_AVG"]         = round($row["AVG"], 1);
            }
        }
        $result->free();

        $query = knjd154kQuery::getRecordAverageTestAppointSql($this, $testVal, $schInfo, "1");
        $this->setAvg($db, $seiseki, $query, "GRADE", $testVal);

        $query = knjd154kQuery::getRecordAverageTestAppointSql($this, $testVal, $schInfo, "2");
        $this->setAvg($db, $seiseki, $query, "CLASS", $testVal);

        $query = knjd154kQuery::getRecordAverageTestAppointSql($this, $testVal, $schInfo, "3");
        $this->setAvg($db, $seiseki, $query, "COURSE", $testVal);

        $query = knjd154kQuery::getRecordAverageTestAppointSql($this, $testVal, $schInfo, "4");
        $this->setAvg($db, $seiseki, $query, "MAJOR", $testVal);
    }

    function setAvg($db, &$seiseki, $query, $avgDiv, $testVal) {
        $subclasscd = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["AVG"] != "") {
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                } else {
                    $subclasscd = $row["SUBCLASSCD"];
                }
                $seiseki[$subclasscd][$avgDiv."_CNT"] = $row["COUNT"];
                $seiseki[$subclasscd][$avgDiv."_AVG"] = round($row["AVG"], 1);
                $seiseki[$subclasscd][$avgDiv."_HIGHSCORE"] = $row["HIGHSCORE"];
                $seiseki[$subclasscd][$avgDiv."_LOWSCORE"] = $row["LOWSCORE"];
            }
            if ($this->ALL9 == $row["SUBCLASSCD"]) {
                $seiseki[$row["SUBCLASSCD"]]["ALL_".$avgDiv."_CNT"] = $row["COUNT"];
            }
        }
        $result->free();
    }

    function setMockData($db, $mockCd, $schInfo) {
        $mockData = array();
        $query = knjd154kQuery::getMockRank($mockCd, $schInfo);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->ALL9 != $row["MOCK_SUBCLASS_CD"]) {
                $mockData[$row["MOCK_SUBCLASS_CD"]]["NAME"] = $row["SUBCLASS_NAME"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["SCORE"] = $row["SCORE"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["GRADE_RANK"] = $row["GRADE_RANK"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["GRADE_CNT"] = $row["G_COUNT"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["GRADE_AVG"] = round($row["G_AVG"], 1);
                $mockData[$row["MOCK_SUBCLASS_CD"]]["HR_RANK"] = $row["CLASS_RANK"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["HR_CNT"] = $row["H_COUNT"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["HR_AVG"] = round($row["H_AVG"], 1);
                $mockData[$row["MOCK_SUBCLASS_CD"]]["COURSE_RANK"] = $row["COURSE_RANK"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["COURSE_CNT"] = $row["C_COUNT"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["COURSE_AVG"] = round($row["C_AVG"], 1);
            } else  {
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_SCORE"] = $row["SCORE"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_GRADE_RANK"] = $row["GRADE_RANK"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_GRADE_CNT"] = $row["G_COUNT"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_GRADE_AVG"] = round($row["G_AVG"], 1);
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_HR_RANK"] = $row["CLASS_RANK"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_HR_CNT"] = $row["H_COUNT"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_HR_AVG"] = round($row["H_AVG"], 1);
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_COURSE_RANK"] = $row["COURSE_RANK"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_COURSE_CNT"] = $row["C_COUNT"];
                $mockData[$row["MOCK_SUBCLASS_CD"]]["ALL_COURSE_AVG"] = round($row["C_AVG"], 1);
            }
        }
        $result->free();

        return $mockData;
    }
}
?>
