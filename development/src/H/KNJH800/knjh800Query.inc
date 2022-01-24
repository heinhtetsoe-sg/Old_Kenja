<?php
class knjh800Query extends Query {
    //選択生徒情報取得
    function getStudentName($gakuseki, $year)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t6.GRADE_NAME as GRADE, ";
        $query .= "     t6.HR_CLASS_NAME as HR_CLASS, ";
        $query .= "     t1.ATTENDNO, ";
        $query .= "     t1.SEMESTER, ";
        $query .= "     t2.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "     left join (SELECT ";
        $query .= "                     YEAR, ";
        $query .= "                     GRADE, ";
        $query .= "                     SEMESTER, ";
        $query .= "                     HR_CLASS, ";
        $query .= "                     to_single_byte(GRADE_NAME) as GRADE_NAME, ";
        $query .= "                     to_single_byte(HR_CLASS_NAME1) as HR_CLASS_NAME ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_HDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$year."' ";
        $query .= "                 ) t6 on t1.YEAR = t6.YEAR and t1.GRADE = t6.GRADE and t1.HR_CLASS = t6.HR_CLASS and t1.SEMESTER = t6.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO =  (SELECT ";
        $query .= "                         SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         PV_SCHREG_MST ";
        $query .= "                     WHERE ";
        $query .= "                         KNJID = '".$gakuseki."' ";
        $query .= "                     ) AND ";
        $query .= "     t1.YEAR = '".$year."' AND ";
        $query .= "     t1.SEMESTER = (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         SCHREGNO = (SELECT ";
        $query .= "                                         SCHREGNO ";
        $query .= "                                     FROM ";
        $query .= "                                         PV_SCHREG_MST ";
        $query .= "                                     WHERE ";
        $query .= "                                         KNJID = '".$gakuseki."' ";
        $query .= "                                     ) AND ";
        $query .= "                         YEAR = '".$year."' ";
        $query .= "                     )  ";
        
        return $query;
    }
    
    //教科取得
    function getKyouka($kyouka="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CLASSCD as VALUE, ";
        $query .= "     CLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SUBCLASS_MST ";
        if($kyouka != ""){
            $query .= " WHERE ";
            $query .= "     CLASSCD = '".$kyouka."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLASSCD ";
        
        return $query;
    }
    //科目取得
    function getKamoku($year, $gakuseki, $kyouka, $kamoku="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t1.SUBCLASSCD as VALUE, ";
        $query .= "     t1.SUBCLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SUBCLASS_MST t1 ";
        $query .= "     left join PV_CBT_SCORE_COUNT_DAT t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     t1.CLASSCD = '".$kyouka."' ";
        if($kamoku != ""){
            $query .= " AND ";
            $query .= "     t2.SUBCLASSCD = '".$kamoku."' ";
        }else{
            $query .= " AND ";
            $query .= "     t2.SUBCLASSCD IS NOT NULL ";
        }
        $query .= " AND  ";
        $query .= "     t2.KNJID = '".$gakuseki."' ";
        $query .= " AND ";
        $query .= "     t2.YEAR = '".$year."' ";
        
        return $query;
    }
    //テストレベル
    function getTestLevel($year, $gakuseki, $kyouka="", $kamoku="",$testkind="")
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     t3.FIELD1 as VALUE, ";
        $query .= "     t3.FIELD2 as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT t2 ";
        $query .= "     left join PV_CBT_SUBCLASS_MST t1 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= "     left join PV_CBT_GENERAL_MST t3 on t2.TEST_LEVEL_CD = t3.FIELD1 and t3.ID = 'M002' ";
        $query .= " WHERE ";
        $query .= "     t2.KNJID = '".$gakuseki."' ";
        if($kyouka != ""){
            $query .= " AND  ";
            $query .= "     t1.CLASSCD = '".$kyouka."' ";
        }
        if($kamoku != ""){
            $query .= " AND ";
            $query .= "     t2.SUBCLASSCD = '".$kamoku."' ";
        }
        if($testkind != ""){
            $query .= " AND ";
            $query .= "     t3.FIELD1 = '".$testkind."' ";
        }
        $query .= " AND ";
        $query .= "     t2.YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     t3.FIELD1 ";
        
        return $query;
    }
    
    //前年度と次年度の学籍データがあるかチェック
    function getBackNextData($knjid, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = (SELECT ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     PV_SCHREG_MST ";
        $query .= "                 WHERE ";
        $query .= "                     KNJID = '".$knjid."' ";
        $query .= "                 ) AND ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }
    
    //選択した条件のテストIDを取得
    function getTestId($year, $gakuseki, $kyouka="", $kamoku="", $testkind="", $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            if($mode == "1"){
                $query .= "     COUNT(*) ";
            }else{
                $query .= "     MAX(CNT) ";
            }
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= " SELECT ";
        $query .= "     TEST_ID, ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT t1 ";
        $query .= "     left join PV_CBT_SUBCLASS_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     t1.KNJID = '".$gakuseki."'  ";
        if($kyouka != ""){
            $query .= " AND ";
            $query .= "     t2.CLASSCD = '".$kyouka."' ";
        }
        if($kamoku != ""){
            $query .= " AND ";
            $query .= "     t1.SUBCLASSCD = '".$kamoku."'  ";
        }
        if($testkind != ""){
            $query .= " AND ";
            $query .= "     t1.TEST_LEVEL_CD = '".$testkind."' ";
        }
        $query .= " AND ";
        $query .= "     t1.YEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     TEST_ID ";
        if($mode != ""){
            $query .= "     ) ";
        }

        return $query;
    }
    
    //選択したテスト情報
    function getTestName($year, $gakuseki, $testid)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t1.TAKE_TURN, ";
        $query .= "     t2.TEST_KIND_NAME, ";
        $query .= "     t1.POINT ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT t1  ";
        $query .= "     left join PV_CBT_TESTKIND_MST t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " WHERE ";
        $query .= "     t1.KNJID = '".$gakuseki."' AND ";
        $query .= "     t1.TEST_ID = '".$testid."' ";
        $query .= " AND ";
        $query .= "     t1.YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.TAKE_TURN ";
        
        return $query;
    }
    //選択したテストの得点取得
    function getTestData($year, $gakuseki, $testid)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     KNJID = '".$gakuseki."' ";
        $query .= " AND ";
        $query .= "     TEST_ID = '".$testid."' ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     END_DATE ";
        
        return $query;
    }
    
    //選択したテストの大問別得点率取得
    function getPercent($year, $gakuseki, $kyouka="", $kamoku="", $testkind="")
    {
        $query  = " SELECT ";
        $query .= "     t1.PARTCD, ";
        $query .= "     PARTGET/PARTPOINT*100 as PERCENT, ";
        $query .= "     t2.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         PARTCD, ";
        $query .= "         SUM(QUESTION_POINT) as PARTPOINT, ";
        $query .= "         SUM(QUESTION_SCORE) as PARTGET ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             SUBSTR(QUESTION_FIELDNO, 1,4) as PARTCD, ";
        $query .= "             QUESTION_POINT, ";
        $query .= "             QUESTION_SCORE ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "             left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "             t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "         WHERE ";
        $query .= "             t1.KNJID = '".$gakuseki."'  ";
        $query .= "         AND ";
        $query .= "             (t2.END_DATE,t1.TEST_ID) in (SELECT ";
        $query .= "                                         MAX(END_DATE), ";
        $query .= "                                         TEST_ID ";
        $query .= "                                     FROM ";
        $query .= "                                         PV_CBT_SCORE_COUNT_DAT a1 ";
        $query .= "                                         left join PV_CBT_SUBCLASS_MST a2 on a1.SUBCLASSCD = a2.SUBCLASSCD ";
        $query .= "                                     WHERE ";
        $query .= "                                         a1.KNJID = '".$gakuseki."'  ";
        if($kyouka != ""){
            $query .= "                                     AND ";
            $query .= "                                         a2.CLASSCD = '".$kyouka."'  ";
        }
        if($kamoku != ""){
            $query .= "                                     AND ";
            $query .= "                                         a1.SUBCLASSCD = '".$kamoku."'  ";
        }
        if($testkind != ""){
            $query .= "                                     AND ";
            $query .= "                                         a1.TEST_LEVEL_CD = '".$testkind."' ";
        }
        $query .= "                                     AND ";
        $query .= "                                         a1.YEAR = '".$year."' ";
        $query .= "                                     GROUP BY ";
        $query .= "                                         KNJID, ";
        $query .= "                                         TEST_ID ";
        $query .= "                                     ) ";
        $query .= "         AND ";
        $query .= "             t1.YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         PARTCD ";
        
        $query .= "     ) t1     left join (SELECT DISTINCT ";
        $query .= "                             FIRST_ID || SECOND_ID || THIRD_ID as PARTCD, ";
        $query .= "                             THIRD_NAME ";
        $query .= "                         FROM ";
        $query .= "                             PV_CBT_FIELD_MST ";
        $query .= "                         ) t2 on t1.PARTCD = t2.PARTCD ";
        $query .= " ORDER BY ";
        $query .= "     t1.PARTCD ";
        
        return $query;
    }
    //大問ごと成績取得
    function getPartScore($year, $gakuseki, $testid)
    {
        $query  = "     SELECT ";
        $query .= "         PARTCD, ";
        $query .= "         SUM(QUESTION_POINT) as PARTPOINT, ";
        $query .= "         SUM(QUESTION_SCORE) as PARTGET ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             SUBSTR(QUESTION_FIELDNO, 1,4) as PARTCD, ";
        $query .= "             QUESTION_POINT, ";
        $query .= "             QUESTION_SCORE ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "             left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "             t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "         WHERE ";
        $query .= "             t1.KNJID = '".$gakuseki."'  ";
        $query .= "         AND ";
        $query .= "             t1.TEST_ID = '".$testid."' ";
        $query .= "         AND ";
        $query .= "             END_DATE = (SELECT ";
        $query .= "                             MAX(END_DATE) ";
        $query .= "                         FROM ";
        $query .= "                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             KNJID = '".$gakuseki."' AND ";
        $query .= "                             TEST_ID = '".$testid."' ";
        $query .= "                         AND ";
        $query .= "                             YEAR = '".$year."' ";
        $query .= "                         ) ";
        $query .= "         AND ";
        $query .= "             t1.YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         PARTCD ";
        $query .= "     ORDER BY ";
        $query .= "         PARTCD ";

        return $query;
    }
    
    //小分野ごとの出力用
    //大分野名称とテストに含まれる小分野個数を取得
    function getQuestCnt($year, $gakuseki, $kyouka="", $kamoku="", $testkind="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t1.*, ";
        $query .= "     t2.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT  ";
        $query .= "         PARTCD, ";
        $query .= "         COUNT(QUESTCD) as CNT ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             SUBSTR(QUESTION_FIELDNO,1,4) as PARTCD, ";
        $query .= "             SUBSTR(QUESTION_FIELDNO,1,6) as QUESTCD ";
        $query .= "         FROM ";
        $query .= "             (SELECT DISTINCT ";
        $query .= "                 QUESTION_FIELDNO ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "                 left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "                 t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "             WHERE ";
        $query .= "                 t1.KNJID = '".$gakuseki."'  ";
        $query .= "             AND ";
        $query .= "                 t1.TEST_ID in (SELECT DISTINCT ";
        $query .= "                                 TEST_ID ";
        $query .= "                             FROM ";
        $query .= "                                 PV_CBT_SCORE_COUNT_DAT a1 ";
        $query .= "                                 left join PV_CBT_SUBCLASS_MST a2 on a1.SUBCLASSCD = a2.SUBCLASSCD ";
        $query .= "                             WHERE ";
        $query .= "                                 a1.KNJID = '".$gakuseki."'  ";
        if($kyouka != ""){
            $query .= "                             AND ";
            $query .= "                                 a2.CLASSCD = '".$kyouka."'  ";
        }
        if($kamoku != ""){
            $query .= "                             AND ";
            $query .= "                                a1.SUBCLASSCD = '".$kamoku."'  ";
        }
        if($testkind != ""){
            $query .= "                             AND ";
            $query .= "                                 a1.TEST_LEVEL_CD = '".$testkind."' ";
        }
        $query .= "                             AND ";
        $query .= "                                 a1.YEAR = '".$year."' ";
        $query .= "                             ) ";
        $query .= "             AND ";
        $query .= "                 t1.YEAR = '".$year."' ";
        $query .= "             ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         PARTCD ";
        $query .= "     ) t1 ";
        $query .= "     left join PV_CBT_FIELD_MST t2 on t1.PARTCD = t2.FIRST_ID || t2.SECOND_ID || t2.THIRD_ID ";
        $query .= " ORDER BY ";
        $query .= "     PARTCD ";
        
        return $query;
    }
    //小分野名称取得
    function getQuestName($year, $gakuseki, $kyouka="", $kamoku="", $testkind="")
    {
        $query  = " SELECT ";
        $query .= "     t1.QUESTION_FIELDNO, ";
        $query .= "     TO_NUMBER(t1.SCORE) / TO_NUMBER(t1.POINT) * 100 as PERCENT, ";
        $query .= "     t2.FOURTH_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         SUM(QUESTION_POINT) as POINT, ";
        $query .= "         SUM(QUESTION_SCORE) as SCORE ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             t1.TEST_ID, ";
        $query .= "             END_DATE, ";
        $query .= "             SUBSTR(QUESTION_FIELDNO, 1, 6) as QUESTION_FIELDNO, ";
        $query .= "             QUESTION_POINT, ";
        $query .= "             QUESTION_SCORE ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "             left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "             t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "         WHERE ";
        $query .= "             t1.KNJID = '".$gakuseki."'  ";
        $query .= "         AND ";
        $query .= "             (END_DATE,t1.TEST_ID) in (SELECT ";
        $query .= "                                         MAX(END_DATE), ";
        $query .= "                                         TEST_ID ";
        $query .= "                                     FROM ";
        $query .= "                                         PV_CBT_SCORE_COUNT_DAT a1 ";
        $query .= "                                         left join PV_CBT_SUBCLASS_MST a2 on a1.SUBCLASSCD = a2.SUBCLASSCD ";
        $query .= "                                     WHERE ";
        $query .= "                                         a1.KNJID = '".$gakuseki."'  ";
        if($kyouka != ""){
            $query .= "                                     AND ";
            $query .= "                                         a2.CLASSCD = '".$kyouka."'  ";
        }
        if($kamoku != ""){
            $query .= "                                     AND ";
            $query .= "                                         a1.SUBCLASSCD = '".$kamoku."'  ";
        }
        if($testkind != ""){
            $query .= "                                     AND ";
            $query .= "                                         a1.TEST_LEVEL_CD = '".$testkind."' ";
        }
        $query .= "                                     AND ";
        $query .= "                                         a1.YEAR = '".$year."' ";
        $query .= "                                     GROUP BY ";
        $query .= "                                         KNJID, ";
        $query .= "                                         TEST_ID ";
        $query .= "                                     ) ";
        $query .= "         AND ";
        $query .= "             t1.YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         QUESTION_FIELDNO ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID || FOURTH_ID as FIELDNO, ";
        $query .= "                     FOURTH_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t2 on t1.QUESTION_FIELDNO = t2.FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     t1.QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野得点取得
    function getQuestPoint($year, $gakuseki, $testid)
    {
        $query  = " SELECT ";
        $query .= "     PARTCD, ";
        $query .= "     QUESTCD, ";
        $query .= "     SUM(QUESTION_POINT) as POINT, ";
        $query .= "     SUM(QUESTION_SCORE) as SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SUBSTR(QUESTION_FIELDNO,1,4) as PARTCD, ";
        $query .= "         SUBSTR(QUESTION_FIELDNO,1,6) as QUESTCD, ";
        $query .= "         QUESTION_POINT, ";
        $query .= "         QUESTION_SCORE ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             t1.QUESTION_FIELDNO, ";
        $query .= "             t1.QUESTION_POINT, ";
        $query .= "             t1.QUESTION_SCORE ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "             left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "             t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "         WHERE ";
        $query .= "             t1.KNJID = '".$gakuseki."' AND ";
        $query .= "             t1.TEST_ID = '".$testid."' AND ";
        $query .= "             END_DATE = (SELECT ";
        $query .= "                             MAX(END_DATE) ";
        $query .= "                         FROM ";
        $query .= "                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             KNJID = '".$gakuseki."' AND ";
        $query .= "                             TEST_ID = '".$testid."' ";
        $query .= "                         AND ";
        $query .= "                             YEAR = '".$year."' ";
        $query .= "                         ) ";
        $query .= "         AND ";
        $query .= "             t1.YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     PARTCD, ";
        $query .= "     QUESTCD ";
        
        return $query;
    }
}
?>
