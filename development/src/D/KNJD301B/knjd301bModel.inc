<?php

require_once('for_php7.php');

class knjd301bModel extends Model
{
    public $programID;
    public $schoolcd;
    public $schoolKind = "H";
    public $cmd;
    public $eventFrom;
    public $year;
    public $semester;       //学期
    public $maxSemester;    //最終学期
    public $testKindCd;     //テスト種別
    public $reportKind;     //帳票種別
    public $selectedClubs;  //出力対象クラブ一覧
    public $scoreDivRawScore = "01"; //素点
    public $scoreDivResults  = "08"; //成績
    public $scoreDivRating   = "09"; //評価
    public $scoreDivKimatsuResults  = "990008"; //期末成績
    public $scoreDivKimatsuRating   = "990009"; //期末評価
    public $semeAll = "9";  //学年末
    public $printPattern = "";
    /**
     * 成績一覧表（reportKind = 3）以外が選択された場合、もしくは、
     * 学年末以外が選択されており、かつ、テスト種別の大分類（testKindの先頭２桁）が 01 の場合。
     * 指定されたテスト種別の素点を出力する。
     */
    public $printPatternSpecify = "1";
    /**
     * 成績一覧表（reportKind = 3）が選択されており、
     * かつ、学年末（semester = 9）以外が選択、
     * かつ、テスト種別の大分類（testKind の先頭２桁）が 02 の場合は、
     * 指定されたテスト種別の素点と、期末成績、期末評価を出力する。
     */
    public $printPatternAll = "2";
    /**
     * 成績一覧表（reportKind = 3）が選択されており、
     * 学年末が選択された場合は、期末成績と期末評価を出力する。
     */
    public $printPatternKimatuOnly = "3";

    public function init()
    {
        $this->year       = CTRL_YEAR;
        $this->schoolcd   = sprintf("%012d", SCHOOLCD);
        $this->cmd        = VARS::request("cmd");
        $this->semester   = VARS::post("SEMESTER");
        $this->testKindCd = VARS::post("TESTKIND");
        $this->reportKind = VARS::post("REPORT_KIND");
        $this->eventFrom  = VARS::post("HID_EVENT_FROM"); //chnageイベント発行元要素名

        if (isset($this->semester) == false) {
            $this->semester = CTRL_SEMESTER;
        }

        if (isset($this->reportKind) == false) {
            $this->reportKind = 1;
        }

        if (isset($this->eventFrom)) {
            $this->selectedClubs = VARS::post("HID_SELECTED_CLUBS");
            if (isset($this->selectedClubs)) {
                $this->selectedClubs = explode(",", $this->selectedClubs);
            }
        } else {
            $this->selectedClubs = array();
        }

        $this->printPattern = $this->getPrintPattern();
    }

    //入力内容チェック
    private function validateInput()
    {
        if (isset($this->selectedClubs) == false
            || is_array($this->selectedClubs) == false
            || get_count($this->selectedClubs) < 1) {
            $this->setWarning("MSG310", "( 出力対象クラブ )");
            return false;
        }

        if ($this->reportKind != 3 && isset($this->testKindCd) == false) {
            $this->setWarning("MSG310", "( テスト種別 )");
            return false;
        }

        switch ($this->reportKind) {
            case 1: //学年別平均点
                break;
            case 2: //度数分布表
                break;
            case 3: //成績一覧表
                break;
            default:
                $this->setWarning("MSG901", "( 帳票種別 )");
                return false;
        }

        return true;
    }

    //CSV出力
    public function outputCSV()
    {
        //入力チェック
        if ($this->validateInput() == false) {
            return false;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //クエリ選択
        $query = "";
        $header = array();
        $data = array();
        $filename = "";
        $query = knjd301bQuery::getSemester($this, $this->semester);
        $semester_name = $db->getOne($query);

        //DB切断
        Query::dbCheckIn($db);

        switch ($this->reportKind) {
            case 1: //学年別平均点
                $header = $this->getGakunenbetsuHeikintenHeadar();
                $data = $this->getGakunenbetsuHeikintenContents();
                $filename = "{$this->year}年度{$semester_name}学年別平均点.csv";
                break;
            case 2: //度数分布表
                $header = $this->getDosuBunpuhyoHeadar();
                $data = $this->getDosuBunpuhyoContents();
                $filename = "{$this->year}年度{$semester_name}度数分布表.csv";
                break;
            case 3: //成績一覧表
                $header = $this->getBukatsudoubetsuSeisekiIchiranhyoHeadar();
                $data = $this->getBukatsudoubetsuSeisekiIchiranhyoContents();
                $filename = "{$this->year}年度{$semester_name}成績一覧表.csv";
                break;
        }

        if (get_count($data) < 1) {
            $this->setWarning("MSG303");
            return false;
        }

        //CSVデータ出力
        $contents = implode($header, ",")."\n";
        //帳票種別項目別フォーマット編集
        switch ($this->reportKind) {
            case 1: //学年別平均点
                for ($idx = 0; $idx < get_count($data); $idx++) {
                    $contents .= implode($data[$idx], ",");
                    $contents .= "\n";
                }
                break;
            case 2: //度数分布表
                //DB接続
                $db = Query::dbCheckOut();

                $gradeMst = array();
                $query = knjd301bQuery::getGradeQuery($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $gradeMst[$row["GRADE"]] = $row["GRADE_CD"];
                }
                $result->free();

                $clubMst = array();
                $query = knjd301bQuery::getClubQuery($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $clubMst[$row["CLUBCD"]] = $row["CLUBNAME"];
                }
                $result->free();

                $subclassMst = array();
                $query = knjd301bQuery::getSubclassQuery($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $subclassMst[] = $row["SUBCLASSCD"];
                }
                $result->free();

                foreach ($data as $grade => $clubList) {
                    foreach ($clubList as $clubcd => $tickWidthList) {
                        if ("09" == substr($this->testKindCd, 4, 2)) {
                            $tickWidthVal = 5;
                            for ($idx = 5, $id = 1; 0 < $idx; $idx--, $id++) {
                                if ($id == 1) {
                                    $contents .= intval($gradeMst[$grade])."年生";
                                    $contents .= ",".$clubcd.":".$clubMst[$clubcd];
                                } else {
                                    $contents .= ",";
                                }
                                $contents .= ",".$tickWidthVal;
                                foreach ($subclassMst as $subclasscd) {
                                    if (array_key_exists($subclasscd, $tickWidthList["COUNT".$id])) {
                                        $count = $tickWidthList["COUNT".$id][$subclasscd] == 0 ? "" : $tickWidthList["COUNT".$id][$subclasscd];
                                        $contents .= ",".$count;
                                    } else {
                                        $contents .= ",";
                                    }
                                }
                                $tickWidthVal--;
                                $contents .= "\n";
                            }
                        } else {
                            for ($idx = 100, $id = 1; 0 < $idx; $idx -= 5, $id++) {
                                $low = 0;
                                if ($id == 1) {
                                    $contents .= intval($gradeMst[$grade])."年生";
                                    $contents .= ",".$clubcd.":".$clubMst[$clubcd];
                                    $low = $idx - 5;
                                } else {
                                    $contents .= ",";
                                    $low = $idx - 4;
                                }
                                $contents .= ",".$idx."　～　".$low;
                                foreach ($subclassMst as $subclasscd) {
                                    if (array_key_exists($subclasscd, $tickWidthList["COUNT".$id])) {
                                        $count = $tickWidthList["COUNT".$id][$subclasscd] == 0 ? "" : $tickWidthList["COUNT".$id][$subclasscd];
                                        $contents .= ",".$count;
                                    } else {
                                        $contents .= ",";
                                    }
                                }
                                if ($id == 1) {
                                    $idx--;
                                };
                                $contents .= "\n";
                            }
                        }

                        $totalFieldNames = array(
                            "VALUE_DI_COUNT" => "未受験",
                            "COUNT"          => "受験者数",
                            "SCORE"          => "得点総計",
                            "AVG"            => "クラブ内平均",
                            "STDDEV"         => "標準偏差",
                            "MAX"            => "最高点",
                            "MIN"            => "最低点");

                        foreach ($totalFieldNames as $fieldKey => $fieldName) {
                            $contents .= ",,".$fieldName;
                            foreach ($subclassMst as $subclasscd) {
                                if (array_key_exists($subclasscd, $tickWidthList[$fieldKey])) {
                                    $contents .= ",".$tickWidthList[$fieldKey][$subclasscd];
                                } else {
                                    $contents .= ",";
                                }
                            }
                            $contents .= "\n";
                        }
                        $contents .= "\n\n\n\n\n";
                    }
                }

                //DB切断
                Query::dbCheckIn($db);
                break;
            case 3: //成績一覧表
                //DB接続
                $db = Query::dbCheckOut();

                $gradeMst = array();
                $query = knjd301bQuery::getGradeQuery($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $gradeMst[$row["GRADE"]] = $row["GRADE_CD"];
                }
                $result->free();

                $clubMst = array();
                $query = knjd301bQuery::getClubQuery($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $clubMst[$row["CLUBCD"]] = $row["CLUBNAME"];
                }
                $result->free();

                $subclassMst = array();
                $query = knjd301bQuery::getSubclassQuery($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $subclassMst[] = $row["SUBCLASSCD"];
                }
                $result->free();

                $no = 1;
                foreach ($data as $grade => $clubList) {
                    foreach ($clubList as $clubcd => $schregnoList) {
                        $startFlg = true;
                        foreach ($schregnoList as $schregno => $student) {
                            if ($startFlg) {
                                $contents .= $gradeMst[$grade]."年生";
                                $contents .= ",".$clubcd.":".$clubMst[$clubcd];
                            } else {
                                $contents .= ",";
                            }
                            $contents .= ",".$no;
                            $contents .= ",".$student["HR_NAMEABBV"];
                            $contents .= ",".$schregno;
                            $contents .= ",".$student["NAME"];
                            $contents .= ",素点";

                            $total = 0;
                            $count = 0;
                            foreach ($subclassMst as $subclasscd) {
                                if (array_key_exists($subclasscd, $student["RAW_SCORE"])) {
                                    $contents .= ",".$student["RAW_SCORE"][$subclasscd];
                                    $total += $student["RAW_SCORE"][$subclasscd];
                                    $count++;
                                } else {
                                    $contents .= ",";
                                }
                            }
                            $contents .= ",".$total;
                            $avarage = $count == 0 ? 0 : round($total / $count, 1);
                            $contents .= ",".$avarage;
                            $contents .= "\n";

                            $contents .= ",,,,,,成績";
                            $total = 0;
                            $count = 0;
                            foreach ($subclassMst as $subclasscd) {
                                if (array_key_exists($subclasscd, $student["RESULTS"])) {
                                    $contents .= ",".$student["RESULTS"][$subclasscd];
                                    $total += $student["RESULTS"][$subclasscd];
                                    $count++;
                                } else {
                                    $contents .= ",";
                                }
                            }
                            $contents .= ",".$total;
                            $avarage = $count == 0 ? 0 : round($total / $count, 1);
                            $contents .= ",".$avarage;
                            $contents .= "\n";

                            $contents .= ",,,,,,評価";
                            $total = 0;
                            $count = 0;
                            foreach ($subclassMst as $subclasscd) {
                                if (array_key_exists($subclasscd, $student["RATING"])) {
                                    $contents .= ",".$student["RATING"][$subclasscd];
                                    $total += $student["RATING"][$subclasscd];
                                    $count++;
                                } else {
                                    $contents .= ",";
                                }
                            }
                            $contents .= ",";
                            $avarage = $count == 0 ? 0 : round($total / $count, 1);
                            $contents .= ",".$avarage;
                            $startFlg = false;
                            $no++;
                            $contents .= "\n";
                        }
                        $contents .= "\n\n\n\n\n";
                    }
                }

                //DB切断
                Query::dbCheckIn($db);
                break;
        }

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
        return true;
    }

    public function getGakunenbetsuHeikintenHeadar()
    {
        $header = array(
            "CLUBCD" => "部クラブコード" ,
            "NAME"   => "部クラブ名",
            "G01"    => "1年生",
            "G02"    => "2年生",
            "G03"    => "3年生",
            "AVG"    => "3学年平均"
        );

        return $header;
    }

    public function getGakunenbetsuHeikintenContents()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $query = knjd301bQuery::getGradeRange($this);
        $result = $db->query($query);
        $gradeRangeList = array();
        for ($idx = 1; $idx <= 3; $idx++) {
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $gradeRangeList[$idx] = $row["GRADE"];
        }
        $result->free();

        $contents = array();
        $query = knjd301bQuery::getGakunenbetsuHeikintenQuery($this, $gradeRangeList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents[] = $row;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        return $contents;
    }

    public function getDosuBunpuhyoHeadar()
    {
        $header = array(
            "GRADE "     => "学年" ,
            "NAME"       => "部クラブ名",
            "TICK_WIDTH" => "得点範囲"
        );

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjd301bQuery::getSubclassQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header[$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        return $header;
    }

    public function getDosuBunpuhyoContents()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $retContents = array();
        $query = knjd301bQuery::getDosuBunpuhyoTickWidthQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ("09" == substr($this->testKindCd, 4, 2)) {
                for ($idx = 1; $idx <= 5; $idx++) {
                    $retContents[$row["GRADE"]][$row["CLUBCD"]]["COUNT".$idx][$row["SUBCLASSCD"]] = $row["COUNT".$idx];
                }
            } else {
                for ($idx = 1; $idx <= 100; $idx++) {
                    $retContents[$row["GRADE"]][$row["CLUBCD"]]["COUNT".$idx][$row["SUBCLASSCD"]] = $row["COUNT".$idx];
                }
            }
        }
        $result->free();

        // 集計情報を加える
        $query = knjd301bQuery::getDosuBunpuhyoStatisticsQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $retContents[$row["GRADE"]][$row["CLUBCD"]]["VALUE_DI_COUNT"][$row["SUBCLASSCD"]] = $row["VALUE_DI_COUNT"];
            $retContents[$row["GRADE"]][$row["CLUBCD"]]["COUNT"]         [$row["SUBCLASSCD"]] = $row["COUNT"];
            $retContents[$row["GRADE"]][$row["CLUBCD"]]["SCORE"]         [$row["SUBCLASSCD"]] = $row["SCORE"];
            $retContents[$row["GRADE"]][$row["CLUBCD"]]["AVG"]           [$row["SUBCLASSCD"]] = $row["AVG"];
            $retContents[$row["GRADE"]][$row["CLUBCD"]]["STDDEV"]        [$row["SUBCLASSCD"]] = $row["STDDEV"];
            $retContents[$row["GRADE"]][$row["CLUBCD"]]["MAX"]           [$row["SUBCLASSCD"]] = $row["MAX"];
            $retContents[$row["GRADE"]][$row["CLUBCD"]]["MIN"]           [$row["SUBCLASSCD"]] = $row["MIN"];
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        return $retContents;
    }

    public function getBukatsudoubetsuSeisekiIchiranhyoHeadar()
    {
        $header = array(
            "GRADE "   => "学年" ,
            "CLUBCD"   => "部クラブ名",
            "NO"       => "NO",
            "HR_CLASS" => "クラス",
            "SCHREGNO" => "学籍番号",
            "NAME"     => "生徒名",
            "DIV"      => "項目"
        );

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjd301bQuery::getSubclassQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header[$row["SUBCLASSCD"]] = $row["SUBCLASSABBV"];
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        $header += array("TOTAL"   => "合計");
        $header += array("AVERAGE" => "平均");

        return $header;
    }

    public function getBukatsudoubetsuSeisekiIchiranhyoContents()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = array();
        $query = knjd301bQuery::getBukatsudoubetsuSeisekiIchiranhyoQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents[$row["GRADE"]][$row["CLUBCD"]][$row["SCHREGNO"]]["RAW_SCORE"][$row["SUBCLASSCD"]] = $row["RAW_SCORE"];
            $contents[$row["GRADE"]][$row["CLUBCD"]][$row["SCHREGNO"]]["RESULTS"][$row["SUBCLASSCD"]] = $row["RESULTS"];
            $contents[$row["GRADE"]][$row["CLUBCD"]][$row["SCHREGNO"]]["RATING"][$row["SUBCLASSCD"]] = $row["RATING"];
            $contents[$row["GRADE"]][$row["CLUBCD"]][$row["SCHREGNO"]]["NAME"] = $row["NAME"];
            $contents[$row["GRADE"]][$row["CLUBCD"]][$row["SCHREGNO"]]["HR_NAMEABBV"] = $row["HR_NAMEABBV"];
        }
        $result->free();

        return $contents;
    }

    public function getPrintPattern()
    {
        if ($this->reportKind == "3") {
            if ($this->semester == $this->semeAll) {
                return $this->printPatternKimatuOnly;
            } else {
                if (substr($this->testKindCd, 0, 2) == "01") {
                    return $this->printPatternSpecify;
                } elseif (substr($this->testKindCd, 0, 2) == "02") {
                    return $this->printPatternAll;
                } else {
                    return "";
                }
            }
        } else {
            return $this->printPatternSpecify;
        }
    }
}
