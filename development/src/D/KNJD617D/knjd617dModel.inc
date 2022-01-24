<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd617dModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectdata = array();
    public $testCdReadType;

    /**
     * メンバ変数の初期化
     */
    public function knjd617dModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //出欠集計日付
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠集計開始日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->selectdata = VARS::post("selectdata");
        $this->testCdReadType = VARS::post("testcd_readtype");
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }


    public function getDownloadModel()
    {
        $fixedTitle = "学業成績一覧表";
        $contents = "";
        $data = false;

        $db = Query::dbCheckOut();

        //クラス単位でループ
        $hrList = explode(",", $this->selectdata);
        foreach ($hrList as $grHrCls) {
            //タイトル関連名称取得
            $setNendo = common::Calc_Wareki($this->field["YEAR"], "04", "01")."度";

            $query = knjd617dQuery::getHrClass($this->field["SEMESTER"], $this->field["GRADE"], $grHrCls);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $setHRName = $row["HR_NAMEABBV"];
            $setStaffName = $row["STAFFNAME"];

            $query = knjd617dQuery::getSemester($this, $this->field["SEMESTER"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $setSemesName = $row["LABEL"];

            $query = knjd617dQuery::getTest($this, $this->field["GRADE"], $this->testCdReadType, $this->field["TESTKINDCD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $setTestName = $row["TESTITEMNAME"];
            $titleStr = $setNendo . $setHRName . ' ' . $setSemesName . ' ' . $setTestName . ' ' . $fixedTitle . ",,校長,,教頭,,担任," .$setStaffName.",,";

            $contents .= $titleStr."\n";

            //タイトル部分SQL文発行
            $ttlArray = array();
            $query = knjd617dQuery::getCsvSubclsInfoSql($this, $grHrCls);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $ttlArray[] = $row;
            }

            //SQL文発行
            $schregnoArry = array();
            $scoreArry = array();
            $query = knjd617dQuery::getCsvBaseStudentInfoSql($this, $grHrCls);

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (!in_array($row["SCHREGNO"], $schregnoArry, true)) {
                    $schregnoArry[] = $row["SCHREGNO"];
                }
                $scoreArry[] = $row;
            }

            $subclsAttendArry = array();
            $query = knjd617dQuery::getCsvSubclsAttendInfoSql($this, $grHrCls);

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclsAttendArry[] = $row;
            }

            $attitudeArry = array();
            $query = knjd617dQuery::getCsvBaseStudentInfoSql($this, $grHrCls, '92');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $attitudeArry[] = $row;
            }

            $maxLesson = 0;
            $totalAttend = array();
            $query = knjd617dQuery::getCsvTotalAttendInfoSql($this, $grHrCls);

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($maxLesson < $row["LESSON"]) {
                    $maxLesson = $row["LESSON"];
                }
                $totalAttend[] = $row;
            }

            //タイトル出力(※出席すべき日数の出力がタイトル部分にあるので、このタイミングでしか設定できない。)
            $lineNameArry = array("教科名", "単位数", "時間", "氏名");
            for ($tcnt = 0; $tcnt < 4; $tcnt++) {
                if ($tcnt == 0) {
                    $contents .= "出席番号,".$lineNameArry[$tcnt];
                } else {
                    $contents .= ",".$lineNameArry[$tcnt];
                }
                foreach ($ttlArray as $getR) {
                    switch ($tcnt) {
                        case 0:
                            $contents .= ",".$getR["SUBCLASSABBV"].",,";
                            break;
                        case 1:
                            $contents .= ",".$getR["CREDITS"].",,";
                            break;
                        case 2:
                            $contents .= ",,,";
                            break;
                        case 3:
                            $contents .= ",評価,欠時,出停";
                            break;
                        default:
                            $contents .= ",";
                            break;
                    }
                }
                if ($tcnt == 0) {
                    $contents .= ",生活態度,総得点,平均点,順位,,出席すべき日数".($maxLesson > 0 ? $maxLesson : "").",,,,忌引・出停,備考";
                } elseif ($tcnt == 3) {
                    $contents .= ",,,,学級,学年,欠席,遅刻,早退,欠課,,";
                } else {
                    $contents .= ",,,,,,,,,,,";
                }
                $contents .= "\n";
            }

            // //1生徒のデータを取得
            $sCnt = 0;
            $maxLpCnt = 50 > get_count($schregnoArry) ? 50 : get_count($schregnoArry);
            for ($lpCnt = 0; $lpCnt < $maxLpCnt; $lpCnt++) {
                $sPtr = 0;
                $getFst = null;
                foreach ($scoreArry as $getSWk) {
                    if ((int)$getSWk["ATTENDNO"] == $lpCnt) {
                        $getFst = $getSWk;
                        break;
                    }
                    $sPtr++;
                }
                $saPtr = 0;
                $getAFst = null;
                foreach ($subclsAttendArry as $getSWk) {
                    if ((int)$getSWk["ATTENDNO"] == $lpCnt) {
                        $getAFst = $getSWk;
                        break;
                    }
                    $saPtr++;
                }
                
                $contents .= ($lpCnt+1);
                if ($getFst != null) {
                    $contents .= ",".$getFst["NAME"];
                } else {
                    $contents .= ",";
                }
                foreach ($ttlArray as $getR) {
                    if ($getFst != null) {
                        $getS = null;
                        for ($sCnt = $sPtr; $sCnt < get_count($scoreArry) && $scoreArry[$sCnt]["SCHREGNO"] == $getFst["SCHREGNO"]; $sCnt++) {
                            $getSWk = $scoreArry[$sCnt];
                            if ($getSWk["CLASSCD"] . '-' . $getSWk["SCHOOL_KIND"] . '-' . $getSWk["CURRICULUM_CD"] . '-' . $getSWk["SUBCLASSCD"]
                                 == $getR["CLASSCD"] . '-' . $getR["SCHOOL_KIND"] . '-' . $getR["CURRICULUM_CD"] . '-' . $getR["SUBCLASSCD"]) {
                                $getS = $scoreArry[$sCnt];
                                break;
                            }
                        }
                        $getA = null;
                        for ($aCnt = $saPtr; $aCnt < get_count($subclsAttendArry) && $subclsAttendArry[$aCnt]["SCHREGNO"] == $getAFst["SCHREGNO"]; $aCnt++) {
                            $getSWk = $subclsAttendArry[$aCnt];
                            if ($getSWk["CLASSCD"] . '-' . $getSWk["SCHOOL_KIND"] . '-' . $getSWk["CURRICULUM_CD"] . '-' . $getSWk["SUBCLASSCD"]
                                 == $getR["CLASSCD"] . '-' . $getR["SCHOOL_KIND"] . '-' . $getR["CURRICULUM_CD"] . '-' . $getR["SUBCLASSCD"]) {
                                $getA = $subclsAttendArry[$aCnt];
                                break;
                            }
                        }
                        if ($getS != null) {
                            //データを埋める
                            $contents .= ",".$getS["SCORE"];  //評価
                            $data = true;
                        } else {
                            //空データを埋める
                            $contents .= ",";
                        }
                        if ($getA != null) {
                            //データを埋める
                            $contents .= ",".($getA["SICK"]+$getA["NOTICE"]+$getA["NONOTICE"]);  //欠時
                            $contents .= ",".$getA["SUSPEND"];  //出停
                            $data = true;
                        } else {
                            //空データを埋める
                            $contents .= ",,";
                        }
                    } else {
                        //空データを埋める
                        $contents .= ",,,";
                    }
                }
                //生活態度
                $getA = null;
                foreach ($attitudeArry as $getAWk) {
                    if ($getAWk["SCHREGNO"] == $getFst["SCHREGNO"]) {
                        $getA = $getAWk;
                        break;
                    }
                }
                $convTbl = array("11"=>"A", "22"=>"B","33"=>"C","44"=>"D");
                if ($getA != null && $convTbl[$getA["SCORE"]] != "") {
                    $contents .= ",".$convTbl[$getA["SCORE"]];
                } else {
                    $contents .= ",";
                }
                //総得点/平均点/順位(学級/学年)
                $getS = null;
                for ($sCnt = $sPtr; ($sCnt < get_count($scoreArry)) && ($scoreArry[$sCnt]["SCHREGNO"] == $getFst["SCHREGNO"]); $sCnt++) {
                    $getSWk = $scoreArry[$sCnt];
                    if ($getSWk["SUBCLASSCD"] == "999999") {
                        $getS = $scoreArry[$sCnt];
                        break;
                    }
                }
                if ($getS != null) {
                    $contents .= ",".$getS["SCORE"];  //総得点
                    $contents .= ",".$getS["AVG"];    //平均点
                    $contents .= ",".$getS["CLASS_RANK"];  //順位(学級)
                    $contents .= ",".$getS["GRADE_RANK"];  //順位(学年)
                } else {
                    $contents .= ",,,,";
                }
                //出欠データ(欠席/遅刻/早退/忌引・出停)
                $getS = null;
                for ($sCnt = 0; $sCnt < get_count($totalAttend); $sCnt++) {
                    if ($totalAttend[$sCnt]["SCHREGNO"] == $getFst["SCHREGNO"]) {
                        $getS = $totalAttend[$sCnt];
                        break;
                    }
                }
                if ($getS != null) {
                    $contents .= ",".($getS["SICK"]+$getS["NOTICE"]+$getS["NONOTICE"]);  //欠席
                    $contents .= ",".$getS["LATE"];   //遅刻
                    $contents .= ",".$getS["EARLY"];  //早退
                    $contents .= ",".$getS["KEKKA"];  //欠課
                    $contents .= ",".($getS["SUSPEND"] + $getS["MOURING"]);  //忌引・出停
                    //備考
                    if (($getS["SICK"]+$getS["NOTICE"]+$getS["NONOTICE"]) == 0 && $getS["LATE"] == 0 && $getS["EARLY"] == 0) {
                        $contents .= ",皆勤";
                    } else {
                        $contents .= ",";
                    }
                } else {
                    $contents .= ",,,,,,";
                }
                //レコード終わり
                $contents .= "\n";
            }
            $data = true;
            //ページ終わり
            $contents .= "\n\n";
        }
        Query::dbCheckIn($db);

        if ($data) {
            $filename = $this->examyear."_".$fixedTitle.".csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
