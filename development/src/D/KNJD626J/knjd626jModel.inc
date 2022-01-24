<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd626jModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ

    //※以下、CSV出力時のみ利用
    public $gradeInfo;      //選択学年情報
    public $lastSemester;   //最終学期
    public $useSemester;    //利用学期
    public $peEst;          //入学期(開校してからの年期)
    public $cntInfo;        //在籍者数情報
    public $SEMEALL = '9';
    public $SEARCHTESTCD1;
    public $SEARCHSUBCLSCD1;
    public $LF;
    public $SEX_MANMARK;
    public $RYUKYU_MARK;

    /**
     * メンバ変数の初期化
     */
    public function knjd626jModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->LF = "\n";
        $this->SEARCHTESTCD1 = "9-990008";
        $this->SEARCHSUBCLSCD1 = "900100";
        $this->SEX_MANMARK = "○";
        $this->RYUKYU_MARK = "○";
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),       //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),   //学期
                             "GRADE"            =>  VARS::post("GRADE"),      //学年
                             "CHKALL"           =>  VARS::post("CHKALL"),     //全選択
                             "CHKBOX1"          =>  VARS::post("CHKBOX1"),    //チェックボックス
                             "CHKBOX2"          =>  VARS::post("CHKBOX2"),    //チェックボックス
                             "CHKBOX3"          =>  VARS::post("CHKBOX3"),    //チェックボックス
                             "CHKBOX4"          =>  VARS::post("CHKBOX4"),    //チェックボックス
                             "CHKBOX5"          =>  VARS::post("CHKBOX5"),    //チェックボックス
                             "CHKBOX6"          =>  VARS::post("CHKBOX6"),    //チェックボックス
                             "CHKBOX7"          =>  VARS::post("CHKBOX7"),    //チェックボックス
                             "CHKBOX8"          =>  VARS::post("CHKBOX8"),    //チェックボックス
                             "CHKBOX9"          =>  VARS::post("CHKBOX9"),    //チェックボックス
                             "DBNAME"           =>  VARS::post("DBNAME")      //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $dataContents = ""; //CSVデータ出力用
        $csvOutFlg = false;

        $this->lastSemester = $db->getOne(knjd626jQuery::getLastSemester($this));
        $this->useSemester = $this->field["SEMESTER"] == $this->SEMEALL ? $this->lastSemester : $this->field["SEMESTER"];

        $this->gradeInfo = array();
        $this->gradeInfo = $db->getRow(knjd626jQuery::getGradeInfo($this), DB_FETCHMODE_ASSOC);
        if (get_count($this->gradeInfo) > 0 && $this->gradeInfo["GRADE_CD"] != null) {
            $gradeCd = $this->gradeInfo["GRADE_CD"];
        } else {
            $gradeCd = null;
        }

        $peEstWk = $db->getOne(knjd626jQuery::getPeEst());
        $this->peEst = ($gradeCd == null || $peEstWk == null) ? "" : (intval($peEstWk) - (intval($gradeCd) - 1));

        $cntInfo = array();
        if ($this->field["CHKBOX1"] == "1" || $this->field["CHKBOX9"] == "9") {  //在籍者数を出力するなら、計算する。
            $this->cntInfo = $db->getRow(knjd626jQuery::getGrdCnt($this), DB_FETCHMODE_ASSOC);
        }

        $dataContents = "";
        $crDelim = "";
        if ($this->field["CHKBOX1"] == "1") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv1($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        if ($this->field["CHKBOX2"] == "2") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv2($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        if ($this->field["CHKBOX3"] == "3") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv3($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        if ($this->field["CHKBOX4"] == "4") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv4($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        if ($this->field["CHKBOX5"] == "5") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv5($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        if ($this->field["CHKBOX6"] == "6") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv6($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        if ($this->field["CHKBOX7"] == "7") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv7($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        if ($this->field["CHKBOX8"] == "8") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv8($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        if ($this->field["CHKBOX9"] == "9") {
            $dataContents .= $crDelim;
            knjd626jModel::printCsv9($db, $dataContents);
            $crDelim = $this->LF;
            $csvOutFlg = true;
        }
        $contents .= $dataContents;

        //DB切断
        Query::dbCheckIn($db);
        if ($csvOutFlg) {
            $title = "成績会議資料";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function printScore($rv)
    {
        return !is_array($rv) ? "" : ($rv["SCORE"] == "" ? "×" : $rv["SCORE"]);
    }

    public function printCsv1($db, &$contents)
    {
        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo1Sql($this);
        $result1 = $db->query($query1);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataInfoArry[$row1["SCHREGNO"]][] = $row1;
        }

        $dataContents  = "【１．在籍者数】".$this->LF;
        $dataContents .= "男子,".($this->cntInfo["MAN_CNT"])."名,女子,".($this->cntInfo["WOMAN_CNT"])."名,合計,".($this->cntInfo["CNT"])."名".$this->LF;
        $dataContents .= $this->LF;
        $dataContents .= "《第一種留学者》".$this->LF;
        $dataContents .= "HR,NO,性別,氏名,開始,終了".$this->LF;

        if (get_count($dataInfoArry) > 0) {
            $schInfoArry = knjd626jModel::getSchInfoArry($db, $dataInfoArry);
            foreach ($schInfoArry as $schregNo => $Obj) {
                if (!is_null($dataInfoArry[$schregNo])) {
                    $sexStr = knjd626jModel::convPrtSex($Obj["SEX"]);
                    $schInfoStr = $Obj["HR_NAME"].",".intval($Obj["ATTENDNO"]).",".$sexStr.",".$Obj["NAME"];
                    foreach ($dataInfoArry[$schregNo] as $d1Obj) {
                        $dataContents .= $schInfoStr.",".str_replace("-", "/", $d1Obj["SDATE"]).",".str_replace("-", "/", $d1Obj["EDATE"]).$this->LF;
                        $schInfoStr = ",,,";
                    }
                }
            }
        }
        if (get_count($schInfoArry) == 0) {
            $dataContents .= ",,,該当者なし,,".$this->LF;
        }

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }

    public function printCsv2($db, &$contents)
    {
        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo2Sql($this);
        $result1 = $db->query($query1);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataInfoArry[$row1["SCHREGNO"]][] = $row1;
        }

        $dataContents  = "【２．長期欠席による留級者】".$this->LF;
        $dataContents .= "HR,NO,性別,氏名".$this->LF;

        $putCnt = 0;
        if (get_count($dataInfoArry) > 0) {
            $schInfoArry = knjd626jModel::getSchInfoArry($db, $dataInfoArry);
            foreach ($schInfoArry as $schregNo => $Obj) {
                $prtFlg = false;
                $subclsCnt = 0;
                if (!is_null($dataInfoArry[$schregNo])) {
                    $subArry = $dataInfoArry[$schregNo];
                    foreach ($subArry as $rv) {
                        if (2 * (intval($rv["SICK"]) + intval($rv["NOTICE"]) + intval($rv["NONOTICE"])) > intval($rv["LESSON"]) - intval($rv["OFFDAYS"]) - intval($rv["ABROAD"]) - intval($rv["ABSENT"]) - intval($rv["MOURNING"])) {
                            $subclsCnt++;
                        }
                    }
                    if ($subclsCnt == get_count($subArry)) {
                        $prtFlg = true;
                    }
                } else {
                    continue;
                }
                if ($prtFlg) {
                    $sexStr = knjd626jModel::convPrtSex($Obj["SEX"]);
                    $dataContents .= $Obj["HR_NAME"].",".intval($Obj["ATTENDNO"]).",".$sexStr.",".$Obj["NAME"].$this->LF;
                    $putCnt++;
                }
            }
        }

        if ($putCnt == 0) {
            $dataContents .= ",,,該当者なし".$this->LF;
        }

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }

    public function printCsv3($db, &$contents)
    {
        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo3Sql($this);
        $result1 = $db->query($query1);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataInfoArry[$row1["SCHREGNO"]][] = $row1;
        }

        $dataContents  = "【３．卒業基準に達しない者】".$this->LF;
        $dataContents .= "HR,NO,性別,氏名,出身,留級,科目名,単位数,評価,合計科目数,合計単位数,ステータス".$this->LF;

        $scoreLine = 3;
        $maxUnderCnt = 3;
        $chkTotalCredit = 8;
        $putCnt = 0;
        if (get_count($dataInfoArry) > 0) {
            $schInfoArry = knjd626jModel::getSchInfoArry($db, $dataInfoArry);
            foreach ($schInfoArry as $schregNo => $Obj) {
                $prtFlg = false;
                $subclsCnt = 0;
                if (is_null($dataInfoArry[$schregNo])) {
                    continue;
                }
                $subArry = $dataInfoArry[$schregNo];
                if (get_count($subArry) == 0) {
                    continue;
                }
                $underCnt = 0;
                $totalCredit = 0;
                foreach ($subArry as $rv) {
                    if ($rv["SCORE"] != "") {
                        if (intval($rv["SCORE"]) <= $scoreLine) {
                            if ($rv["CREDITS"] != "") {
                                $totalCredit += intval($rv["CREDITS"]);
                            }
                            $underCnt++;
                        }
                    } else {
                        $underCnt++;
                    }
                }
                if ($underCnt < $maxUnderCnt || $totalCredit < $chkTotalCredit) { //SCOREが3以下の科目数が3以上かつ合計単位8以上の"否定"
                    continue;
                }
                $sexStr = knjd626jModel::convPrtSex($Obj["SEX"]);
                $schInfoStr = $Obj["HR_NAME"].",".intval($Obj["ATTENDNO"]).",".$sexStr.",".$Obj["NAME"].",".$Obj["SYUSSIN"].",".$Obj["RYUNEN1"].$Obj["RYUNEN2"].$Obj["RYUNEN3"];
                $statStr = "不可";
                $prtCnt = 0;
                foreach ($subArry as $rv) {
                    $prtCnt++;
                    $dataContents .= $schInfoStr.",".$rv["SUBCLASSNAME"].",".$rv["CREDITS"].",".$this->printScore($rv);
                    $schInfoStr = ",,,,,";
                    if ($prtCnt < get_count($subArry)) {
                        $dataContents .= ",,,";
                    } else {
                        $dataContents .= ",".$underCnt.",".$totalCredit.",";
                    }
                    $dataContents .= $statStr.$this->LF;
                    $statStr = "";
                }
                $putCnt += $prtCnt;
            }
        }

        if ($putCnt == 0) {
            $dataContents .= ",,,該当者なし,,,,,,,,".$this->LF;
        }

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }

    public function printCsv4($db, &$contents)
    {
        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo4Sql($this);
        $result1 = $db->query($query1);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataInfoArry[$row1["SCHREGNO"]][] = $row1;
        }

        $dataContents  = "【４．卒業基準内で不合格科目のある者】".$this->LF;
        $dataContents .= "HR,NO,性別,氏名,出身,留級,科目名,単位数,評価,合計科目数,合計単位数".$this->LF;

        $scoreLine = 3;
        $maxUnderCnt1 = 3;
        $chkTotalCredit1 = 7;
        $maxUnderCnt2 = 2;
        $chkTotalCredit2 = 8;
        $putCnt = 0;
        if (get_count($dataInfoArry) > 0) {
            $schInfoArry = knjd626jModel::getSchInfoArry($db, $dataInfoArry);
            foreach ($schInfoArry as $schregNo => $Obj) {
                $prtFlg = false;
                $subclsCnt = 0;
                if (is_null($dataInfoArry[$schregNo])) {
                    continue;
                }
                $subArry = $dataInfoArry[$schregNo];
                if (get_count($subArry) == 0) {
                    continue;
                }
                $underCnt = 0;
                $totalCredit = 0;
                foreach ($subArry as $rv) {
                    if ($rv["SCORE"] != "") {
                        if (intval($rv["SCORE"]) <= $scoreLine) {
                            if ($rv["CREDITS"] != "") {
                                $totalCredit += intval($rv["CREDITS"]);
                            }
                            $underCnt++;
                        }
                    } else {
                        $underCnt++;
                    }
                }
                if (($underCnt > $maxUnderCnt1 || $totalCredit < $chkTotalCredit1)       //(scoreが3以下の数3個以下、合計単位数7以下)の"否定"
                    && ($underCnt > $maxUnderCnt2 || $totalCredit < $chkTotalCredit2)) { //(scoreが3以下の数2個以下、合計単位数8以下)の"否定"
                    continue;
                }
                $sexStr = knjd626jModel::convPrtSex($Obj["SEX"]);
                $schInfoStr = $Obj["HR_NAME"].",".intval($Obj["ATTENDNO"]).",".$sexStr.",".$Obj["NAME"].",".$Obj["SYUSSIN"].",".$Obj["RYUNEN1"].$Obj["RYUNEN2"].$Obj["RYUNEN3"];
                $prtCnt = 0;
                foreach ($subArry as $rv) {
                    $prtCnt++;
                    $dataContents .= $schInfoStr.",".$rv["SUBCLASSNAME"].",".$rv["CREDITS"].",".$this->printScore($rv);
                    $schInfoStr = ",,,,,";
                    if ($prtCnt < get_count($subArry)) {
                        $dataContents .= ",,";
                    } else {
                        $dataContents .= ",".$underCnt.",".$totalCredit;
                    }
                    $dataContents .= $this->LF;
                }
                $putCnt += $prtCnt;
            }
        }

        if ($putCnt == 0) {
            $dataContents .= ",,,該当者なし,,,,,,,".$this->LF;
        }

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }

    public function printCsv5($db, &$contents)
    {
        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo5Sql($this);
        $result1 = $db->query($query1);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataInfoArry[$row1["SCHREGNO"]][] = $row1;
        }

        $dataContents  = "【５．時間不足による未履修科目を持つ者】".$this->LF;
        $dataContents .= "HR,NO,性別,氏名,出身,留級,科目名,単位数,欠課時数,授業時数,合計科目数,合計単位数".$this->LF;

        $totalCredit = 0;
        $putCnt = 0;
        if (get_count($dataInfoArry) > 0) {
            $schInfoArry = knjd626jModel::getSchInfoArry($db, $dataInfoArry);
            foreach ($schInfoArry as $schregNo => $Obj) {
                $prtFlg = false;
                $subclsCnt = 0;
                if (!is_null($dataInfoArry[$schregNo])) {
                    $subArry = $dataInfoArry[$schregNo];
                    if (get_count($subArry) == 0) {
                        continue;
                    }
                    foreach ($subArry as $rv) {
                        $calc1 = intval($rv["SICK"]) + intval($rv["NOTICE"]) + intval($rv["NONOTICE"]);
                        $calc2 = intval($rv["LESSON"]) - intval($rv["OFFDAYS"]) - intval($rv["ABROAD"]) - intval($rv["ABSENT"]) - intval($rv["MOURNING"]);
                        if (2 * $calc1 >= $calc2) {
                            if ($rv["CREDITS"] != "") {
                                $totalCredit += intval($rv["CREDITS"]);
                            }
                            $subclsCnt++;
                        }
                    }
                    if ($subclsCnt == 0) {
                        continue;
                    }
                    $sexStr = knjd626jModel::convPrtSex($Obj["SEX"]);
                    $schInfoStr = $Obj["HR_NAME"].",".intval($Obj["ATTENDNO"]).",".$sexStr.",".$Obj["NAME"].",".$Obj["SYUSSIN"].",".$Obj["RYUNEN1"].$Obj["RYUNEN2"].$Obj["RYUNEN3"];
                    $prtCnt = 0;
                    foreach ($subArry as $rv) {
                        $calc1 = intval($rv["SICK"]) + intval($rv["NOTICE"]) + intval($rv["NONOTICE"]);
                        $calc2 = intval($rv["LESSON"]) - intval($rv["OFFDAYS"]) - intval($rv["ABROAD"]) - intval($rv["ABSENT"]) - intval($rv["MOURNING"]);
                        if (2 * $calc1 < $calc2) {
                            continue;
                        }
                        $prtCnt++;
                        $dataContents .= $schInfoStr.",".$rv["SUBCLASSNAME"].",".$rv["CREDITS"].",".$calc1.",".$calc2;
                        $schInfoStr = ",,,,,";
                        if ($prtCnt == $subclsCnt) {
                            $dataContents .= ",".$subclsCnt.",".$totalCredit;
                        } else {
                            $dataContents .= ",,";
                        }
                        $dataContents .= $this->LF;
                    }

                    $putCnt += $prtCnt;
                }
            }
        }

        if ($putCnt == 0) {
            $dataContents .= ",,,該当者なし,,,,,,,,".$this->LF;
        }

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }

    public function printCsv6($db, &$contents)
    {
        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo6Sql($this);
        $result1 = $db->query($query1);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataInfoArry[$row1["SCHREGNO"]][] = $row1;
        }

        $dataContents  = "【６．10段階評価で1・2の科目を持つ者】".$this->LF;
        $dataContents .= "HR,NO,性別,氏名,出身,留級,科目名,単位数,評定,合計科目数,合計単位数".$this->LF;

        $putCnt = 0;
        if (get_count($dataInfoArry) > 0) {
            $schInfoArry = knjd626jModel::getSchInfoArry($db, $dataInfoArry);
            foreach ($schInfoArry as $schregNo => $Obj) {
                $totalCredit = 0;
                $prtFlg = false;
                $underCnt = 0;
                if (!is_null($dataInfoArry[$schregNo])) {
                    $subArry = $dataInfoArry[$schregNo];
                    foreach ($subArry as $rv) {
                        if ($rv["CREDITS"] != "") {
                            $totalCredit += intval($rv["CREDITS"]);
                        }
                        $underCnt++;
                    }
                    if ($underCnt == 0) {
                        continue;
                    }
                    $sexStr = knjd626jModel::convPrtSex($Obj["SEX"]);
                    $schInfoStr = $Obj["HR_NAME"].",".intval($Obj["ATTENDNO"]).",".$sexStr.",".$Obj["NAME"].",".$Obj["SYUSSIN"].",".$Obj["RYUNEN1"].$Obj["RYUNEN2"].$Obj["RYUNEN3"];
                    $prtCnt = 0;
                    foreach ($subArry as $rv) {
                        $prtCnt++;
                        $dataContents .= $schInfoStr.",".$rv["SUBCLASSNAME"].",".$rv["CREDITS"].",".$this->printScore($rv);
                        $schInfoStr = ",,,,,";
                        if ($prtCnt < get_count($subArry)) {
                            $dataContents .= ",,";
                        } else {
                            $dataContents .= ",".$underCnt.",".$totalCredit;
                        }
                        $dataContents .= $this->LF;
                    }
                    $putCnt += $prtCnt;
                }
            }
        }

        if ($putCnt == 0) {
            $dataContents .= ",,,該当者なし,,,,,,,".$this->LF;
        }

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }

    //生徒の情報は不要なので、他の処理とは違う所に注意。
    public function printCsv7($db, &$contents)
    {
        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo7Sql($this);
        $result1 = $db->query($query1);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseCodeArry[] = $row1["COURSECODE"];
            $dataInfoArry[$row1["COURSECODE"]][$row1["REQUIRE_FLG"]][] = $row1;
        }

        $dataContents = "【７．教科別科目別不合格者数】".$this->LF;

        if (get_count($courseCodeArry) > 0) {
            $courseCodeArry = array_unique($courseCodeArry);
            $nextPageDelimCd = "";
            foreach ($courseCodeArry as $cCode) {
                $maxRecCnt = get_count($dataInfoArry[$cCode]["0"]) > get_count($dataInfoArry[$cCode]["1"]) ? get_count($dataInfoArry[$cCode]["0"]) : get_count($dataInfoArry[$cCode]["1"]);
                if ($maxRecCnt > 0) {
                    $dataContents .= $nextPageDelimCd."必修,,,選択,,".$this->LF;
                    $dataContents .= "教科名,科目名,人数,教科名,科目名,人数".$this->LF;
                    $bakClsCd1 = "";
                    $bakClsCd2 = "";
                    for ($recCnt = 0; $recCnt < $maxRecCnt; $recCnt++) {
                        if (get_count($dataInfoArry[$cCode]["0"] > $recCnt)) {
                            $prtObj = $dataInfoArry[$cCode]["0"][$recCnt];
                            $clsNameStr = $bakClsCd1 == $prtObj["CLASSCD"] ? "" : $prtObj["CLASSNAME"];
                            $dataContents .= $clsNameStr.",".$prtObj["SUBCLASSNAME"].",".$prtObj["CNT"].",";
                            $bakClsCd1 = $prtObj["CLASSCD"];
                        } else {
                            $dataContents .= ",,,";
                            $bakClsCd1 = "";
                        }
                        if (get_count($dataInfoArry[$cCode]["1"] > $recCnt)) {
                            $prtObj = $dataInfoArry[$cCode]["1"][$recCnt];
                            $clsNameStr = $bakClsCd2 == $prtObj["CLASSCD"] ? "" : $prtObj["CLASSNAME"];
                            $dataContents .= $clsNameStr.",".$prtObj["SUBCLASSNAME"].",".$prtObj["CNT"];
                            $bakClsCd2 = $prtObj["CLASSCD"];
                        } else {
                            $dataContents .= ",,";
                            $bakClsCd2 = "";
                        }
                        $dataContents .= $this->LF;
                    }
                } else {
                    $dataContents .= $nextPageDelimCd."必修,,,選択,,".$this->LF;
                    $dataContents .= "教科名,科目名,人数,教科名,科目名,人数".$this->LF;
                }
                $nextPageDelimCd = $this->LF;
            }
        } else {
            $dataContents .= $nextPageDelimCd."必修,,,選択,,".$this->LF;
            $dataContents .= "教科名,科目名,人数,教科名,科目名,人数".$this->LF;
        }

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }
    public function printCsv8($db, &$contents)
    {
        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo8Sql($this);
        $result1 = $db->query($query1);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataInfoArry[$row1["SCHREGNO"]][] = $row1;
        }
        $schInfoArry = array();
        if (get_count($dataInfoArry) > 0) {
            $schInfoArry = knjd626jModel::getSchInfoArry($db, $dataInfoArry);
        }

        $dataContents  = "【８．総合的な探究の時間 未履修・未修得者】".$this->LF;
        $dataContents .= "HR,NO,性別,氏名,出身,留級,年度,学年,科目名,単位数,評定".$this->LF;

        foreach ($schInfoArry as $schregNo => $Obj) {
            if (!is_null($dataInfoArry[$schregNo])) {
                $sexStr = knjd626jModel::convPrtSex($Obj["SEX"]);
                $schInfoStr = $Obj["HR_NAME"].",".intval($Obj["ATTENDNO"]).",".$sexStr.",".$Obj["NAME"].",".$Obj["SYUSSIN"].",".$Obj["RYUNEN1"].$Obj["RYUNEN2"].$Obj["RYUNEN3"];
                foreach ($dataInfoArry[$schregNo] as $d1Obj) {
                    $dataContents .= $schInfoStr.",".$d1Obj["YEAR"].",".$d1Obj["YGRADE"].",".$d1Obj["SUBCLASSNAME"].",".$d1Obj["CREDITS"].",".$d1Obj["STAT"].$this->LF;
                    $schInfoStr = ",,,,,";
                }
            }
        }
        if (get_count($schInfoArry) == 0) {
            $dataContents .= ",,,該当者なし,,,,,,,".$this->LF;
        }

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }

    public function printCsv9($db, &$contents)
    {
        $maxUnderCnt = 3;
        $chkTotalCredit = 8;
        $underCnt = 0;

        $dataInfoArry = array();
        $query1 = knjd626jQuery::getDataInfo3Sql($this);
        $result1 = $db->query($query1);
        $chkTotalCreditArry = array();
        $underCntArry = array();
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row1["SCHREGNO"] == "") {
                continue;
            }
            if ($row1["SCORE"] != "") {
                if ($row1["SCORE"] <= 3) {
                    if ($row1["CREDITS"] != "") {
                        $dataInfoArry[$row1["SCHREGNO"]][] = $row1;
                        $chkTotalCreditArry[$row1["SCHREGNO"]]++;
                    }
                    $underCntArry[$row1["SCHREGNO"]]++;
                }
            } else {
                $underCntArry[$row1["SCHREGNO"]]++;
            }
        }

        $ttlCnt = 0;
        $manCnt = 0;
        $womanCnt = 0;

        if (get_count($dataInfoArry) > 0) {
            $schInfoArry = knjd626jModel::getSchInfoArry($db, $dataInfoArry);
            foreach ($schInfoArry as $schregNo => $Obj) {
                if ($underCntArry[$schregNo] < $maxUnderCnt || $chkTotalCreditArry[$schregNo] < $chkTotalCredit) {
                    continue;
                }
                if (!is_null($dataInfoArry[$schregNo])) {
                    $ttlCnt++;
                    if ($Obj["SEX"] == "1") {
                        $manCnt++;
                    } else {
                        $womanCnt++;
                    }
                }
            }
        }

        $dataContents  = "【９．退学予定者】".$this->LF;
        $dataContents .= "【１０．復学予定者】".$this->LF;
        $dataContents .= "【１１．卒業予定者数】".$this->LF;
        $dataContents .= "男子,".($this->cntInfo["MAN_CNT"] - $manCnt)."名,女子,".($this->cntInfo["WOMAN_CNT"] - $womanCnt)."名,合計,".($this->cntInfo["CNT"] - $ttlCnt)."名".$this->LF;

        $title = "第".$this->peEst."期 ".$this->gradeInfo["GRADE_NAME1"]." 学年末評価 成績会議資料".$this->LF;
        $contents .= $title.$this->LF;
        $contents .= $dataContents.$this->LF;
    }

    public function convPrtSex($sex)
    {
        return $sex == "1" ? $this->SEX_MANMARK : "";
    }

    public function getSchInfoArry($db, $dataInfoArry)
    {
        $retRow = array();
        $query2 = knjd626jQuery::getSchInfo($this, $dataInfoArry);
        $result2 = $db->query($query2);
        while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            $retRow[$row2["SCHREGNO"]] = $row2;
        }
        return $retRow;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
