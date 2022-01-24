<?php
class knjh815Query extends Query {
    
    //生徒氏名取得
    function getName($knjid, $year)
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
        $query .= "     left join SCHREG_REGD_GDAT t3 on t1.YEAR = t3.YEAR AND t1.GRADE = t3.GRADE ";
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
        $query .= "                         KNJID = '".$knjid."' ";
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
        $query .= "                                         KNJID = '".$knjid."' ";
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
    function getKamoku($year, $kyouka, $kamoku="")
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
        $query .= " AND ";
        $query .= "     t2.YEAR = '".$year."' ";
        
        return $query;
    }
    //テストレベル
    function getTestLevel($year, $kyouka="", $kamoku="", $testkind="")
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     t3.FIELD1 as VALUE, ";
        $query .= "     t3.FIELD2 as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT t2 ";
        $query .= "     left join PV_CBT_SUBCLASS_MST t1 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= "     left join PV_CBT_GENERAL_MST t3 on t2.TEST_LEVEL_CD = t3.FIELD1 and t3.ID = 'M002' ";
        
        $where = "WHERE";
        if($kyouka != ""){
            $query .= " ".$where."  ";
            $query .= "     t1.CLASSCD = '".$kyouka."' ";
            $where = "AND";
        }
        if($kamoku != ""){
            $query .= " ".$where." ";
            $query .= "     t2.SUBCLASSCD = '".$kamoku."' ";
            $where = "AND";
        }
        if($testkind != ""){
            $query .= " ".$where." ";
            $query .= "     t3.FIELD1 = '".$testkind."' ";
        }
        $query .= " AND ";
        $query .= "     t2.YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     t3.FIELD1 ";
        
        return $query;
    }
    
    //取込日取得
    function getImportDate()
    {
        $query  = " SELECT ";
        $query .= "     SUBSTR(MAX(UPDATED), 1, 10) ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

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
    
    //個人の成績データの有無チェック
    function getIndData($year, $knjid, $field)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     KNJID = '".$knjid."' ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        if($field["KYOKA"] != ""){
            $query .= " AND ";
            $query .= "     CLASSCD = '".$field["KYOKA"]."' ";
        }
        if($field["KAMOKU"] != ""){
            $query .= " AND ";
            $query .= "     SUBCLASSCD = '".$field["KAMOKU"]."' ";
        }
        if($field["TEST_KIND"] != ""){
            $query .= " AND ";
            $query .= "     TEST_LEVEL_CD = '".$field["TEST_KIND"]."' ";
        }
        if($field["F_KAISU"] != "" && $field["T_KAISU"] != ""){
            $query .= " AND ";
            $query .= "     TAKE_TURN BETWEEN '".$field["F_KAISU"]."' AND '".$field["T_KAISU"]."' ";
        }else if($field["F_KAISU"] != "" && $field["T_KAISU"] == ""){
            $query .= " AND ";
            $query .= "     TAKE_TURN >= '".$field["F_KAISU"]."' ";
        }
        
        return $query;
    }
    
    
    //大分野・小分野ごとのときのテスト全体のクラス平均点と最高点を取得　左上
    function getAllClass($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SCORE) as SCORE, ";
        $query .= "     AVG(TO_NUMBER(SCORE)) as AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         KNJID, ";
        $query .= "         SUM(GET_POINT) as SCORE ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "         WHERE ";
        $query .= "             (KNJID, END_DATE, TEST_ID) IN (SELECT ";
        $query .= "                                                 KNJID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                 MAX(END_DATE) as END_DATE , ";
        }else{
            $query .= "                                                 MIN(END_DATE) as END_DATE , ";
        }
        $query .= "                                                 TEST_ID ";
        $query .= "                                             FROM ";
        $query .= "                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 KNJID IN ( ";
        $query .= "                                                             SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                 SCHREGNO ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                             YEAR, ";
        $query .= "                                                                                                                             SEMESTER, ";
        $query .= "                                                                                                                             GRADE, ";
        $query .= "                                                                                                                             HR_CLASS ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                                         FROM ";
        $query .= "                                                                                                                                             PV_SCHREG_MST ";
        $query .= "                                                                                                                                         WHERE ";
        $query .= "                                                                                                                                             KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                         ) AND ";
        $query .= "                                                                                                                             YEAR = '".$year."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                             ) ";
        $query .= "                                             AND ";
        $query .= "                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$year."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     WHERE ";
        $query .= "         TEST_ID IN (".$testid.") ";
        $query .= "     GROUP BY ";
        $query .= "         KNJID ";
        $query .= "     ) ";
        
        return $query;
    }
    //大分野・小分野ごとのときのテスト全体の学年平均と学年最高点取得　左上
    function getAllGrade($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SCORE) as GRADE_MAX, ";
        $query .= "     AVG(TO_NUMBER(SCORE)) as GRADE_AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         KNJID, ";
        $query .= "         SUM(GET_POINT) as SCORE ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "         WHERE ";
        $query .= "             (KNJID, END_DATE, TEST_ID) IN (SELECT ";
        $query .= "                                                 KNJID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                 MAX(END_DATE) as END_DATE , ";
        }else{
            $query .= "                                                 MIN(END_DATE) as END_DATE , ";
        }
        $query .= "                                                 TEST_ID ";
        $query .= "                                             FROM ";
        $query .= "                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 KNJID IN ( ";
        $query .= "                                                             SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                 SCHREGNO ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                 YEAR, ";
        $query .= "                                                                                                                 SEMESTER, ";
        $query .= "                                                                                                                 GRADE ";
        $query .= "                                                                                                             FROM ";
        $query .= "                                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                                             WHERE ";
        $query .= "                                                                                                                 SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                 SCHREGNO ";
        $query .= "                                                                                                                             FROM ";
        $query .= "                                                                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                                                                             WHERE ";
        $query .= "                                                                                                                                 KNJID = '".$knjid."' ";
        $query .= "                                                                                                                             ) AND ";
        $query .= "                                                                                                                 YEAR = '".$year."' AND ";
        $query .= "                                                                                                                 SEMESTER = '".$semester."' ";
        $query .= "                                                                                                              ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                             ) ";
        $query .= "                                             AND ";
        $query .= "                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$year."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     WHERE ";
        $query .= "         TEST_ID IN (".$testid.") ";
        $query .= "     GROUP BY ";
        $query .= "         KNJID ";
        $query .= "     ) ";
        
        return $query;
    }
    //個人得点合計header_x部分
    function getAllSchreg($year, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     SUM(GET_POINT) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     (KNJID, TEST_ID, END_DATE) in (SELECT ";
        $query .= "                                         KNJID, ";
        $query .= "                                         TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                         MAX(END_DATE) ";
        }else{
            $query .= "                                         MIN(END_DATE) ";
        }
        $query .= "                                     FROM ";
        $query .= "                                         PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                     WHERE ";
        $query .= "                                         KNJID = '".$knjid."'  ";
        $query .= "                                     AND ";
        $query .= "                                         TEST_ID IN (".$testid.") ";
        $query .= "                                     AND ";
        $query .= "                                         YEAR = '".$year."' ";
        $query .= "                                     GROUP BY ";
        $query .= "                                         KNJID, ";
        $query .= "                                         TEST_ID ";
        $query .= "                                     ) ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }
    
    //個人の成績取得　右上
    function getTestScregData($year, $knjid, $field)
    {
        $query  = " SELECT ";
        $query .= "     a1.*, ";
        $query .= "     a2.TEST_KIND_NAME ";
        $query .= " FROM ";
        $query .= "         (SELECT ";
        $query .= "             t1.TEST_ID, ";
        $query .= "             t1.TAKE_TURN, ";
        $query .= "             t1.KNJID, ";
        $query .= "             t1.POINT, ";
        $query .= "             SUM(t1.GET_POINT) as SCHREG_SCORE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "             WHERE ";
        $query .= "                 (KNJID, END_DATE, TEST_ID) IN (SELECT ";
        $query .= "                                                     KNJID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                 MAX(END_DATE), ";
        }else{
            $query .= "                                                 MIN(END_DATE), ";
        }
        $query .= "                                                     TEST_ID ";
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     KNJID = '".$knjid."' AND ";
        $query .= "                                                     YEAR = '".$year."' AND ";
        $query .= "                                                     TEST_ID IN (SELECT ";
        $query .= "                                                                     TEST_ID ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     KNJID = '".$knjid."' ";
        $query .= "                                                                 AND ";
        $query .= "                                                                     CLASSCD = '".$field["KYOKA"]."'  ";
        if($field["KAMOKU"] != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     SUBCLASSCD = '".$field["KAMOKU"]."' ";
        }
        $query .= "                                                             ) ";
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     KNJID, ";
        $query .= "                                                     TEST_ID ";
        $query .= "                                                 ) AND ";
        $query .= "                 YEAR = '".$year."' ";
        $query .= "             ) t1 left join PV_CBT_SUBCLASS_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         t2.CLASSCD = '".$field["KYOKA"]."' ";
        if($field["KAMOKU"] != ""){
            $query .= "     AND ";
            $query .= "         t1.SUBCLASSCD = '".$field["KAMOKU"]."' ";
        }
        if($field["TEST_KIND"] != ""){
            $query .= "     AND ";
            $query .= "         t1.TEST_LEVEL_CD = '".$field["TEST_KIND"]."' ";
        }
        if($field["F_KAISU"] != "" && $field["T_KAISU"] != ""){
            $query .= "     AND ";
            $query .= "         t1.TAKE_TURN BETWEEN '".$field["F_KAISU"]."' AND '".$field["T_KAISU"]."' ";
        }else if($field["F_KAISU"] != "" && $field["T_KAISU"] == ""){
            $query .= "     AND ";
            $query .= "         t1.TAKE_TURN >= '".$field["F_KAISU"]."' ";
        }
        $query .= "         GROUP BY ";
        $query .= "             t1.TEST_ID, ";
        $query .= "             t1.TAKE_TURN, ";
        $query .= "             t1.KNJID, ";
        $query .= "             t1.POINT ";
        $query .= "     ) a1 left join PV_CBT_TESTKIND_MST a2 on a1.TEST_ID = a2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     TAKE_TURN ";

        return $query;
    }
    
    //実施回と配点等取得　右上　共通
    function getTestClass($year, $semester, $knjid, $field, $testid)
    {
        $query .= " SELECT ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN, ";
        $query .= "     MAX(SCORE) as MAX, ";
        $query .= "     AVG(TO_NUMBER(SCORE)) as AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEST_ID, ";
        $query .= "         TAKE_TURN, ";
        $query .= "         KNJID, ";
        $query .= "         SUM(GET_POINT) as SCORE ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "         WHERE ";
        $query .= "             (KNJID, END_DATE, TEST_ID) IN (SELECT ";
        $query .= "                                                 KNJID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                 MAX(END_DATE) as END_DATE , ";
        }else{
            $query .= "                                                 MIN(END_DATE) as END_DATE , ";
        }
        $query .= "                                                 TEST_ID ";
        $query .= "                                             FROM ";
        $query .= "                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 KNJID IN ( ";
        $query .= "                                                             SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                 SCHREGNO ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                             YEAR, ";
        $query .= "                                                                                                                             SEMESTER, ";
        $query .= "                                                                                                                             GRADE, ";
        $query .= "                                                                                                                             HR_CLASS ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                                         FROM ";
        $query .= "                                                                                                                                             PV_SCHREG_MST ";
        $query .= "                                                                                                                                         WHERE ";
        $query .= "                                                                                                                                             KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                         ) AND ";
        $query .= "                                                                                                                             YEAR = '".$year."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                             ) ";
        $query .= "                                             AND ";
        $query .= "                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$year."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     WHERE ";
        $query .= "         TEST_ID IN (".$testid.") ";
        $query .= "     GROUP BY ";
        $query .= "         TEST_ID, ";
        $query .= "         TAKE_TURN, ";
        $query .= "         KNJID ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN ";
        $query .= " ORDER BY ";
        $query .= "     TAKE_TURN ";
        
        return $query;
    }
    //学年平均・学年最高点取得　右上　共通
    function getTestGrade($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN, ";
        $query .= "     MAX(SCORE) as GRADE_MAX, ";
        $query .= "     AVG(TO_NUMBER(SCORE)) as GRADE_AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEST_ID, ";
        $query .= "         TAKE_TURN, ";
        $query .= "         KNJID, ";
        $query .= "         SUM(GET_POINT) as SCORE ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "         WHERE ";
        $query .= "             (KNJID, END_DATE, TEST_ID) IN (SELECT ";
        $query .= "                                                 KNJID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                 MAX(END_DATE) as END_DATE , ";
        }else{
            $query .= "                                                 MIN(END_DATE) as END_DATE , ";
        }
        $query .= "                                                 TEST_ID ";
        $query .= "                                             FROM ";
        $query .= "                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 KNJID IN ( ";
        $query .= "                                                             SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                 SCHREGNO ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                 YEAR, ";
        $query .= "                                                                                                                 SEMESTER, ";
        $query .= "                                                                                                                 GRADE ";
        $query .= "                                                                                                             FROM ";
        $query .= "                                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                                             WHERE ";
        $query .= "                                                                                                                 SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                 SCHREGNO ";
        $query .= "                                                                                                                             FROM ";
        $query .= "                                                                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                                                                             WHERE ";
        $query .= "                                                                                                                                 KNJID = '".$knjid."' ";
        $query .= "                                                                                                                             ) AND ";
        $query .= "                                                                                                                 YEAR = '".$year."' AND ";
        $query .= "                                                                                                                 SEMESTER = '".$semester."' ";
        $query .= "                                                                                                              ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                             ) ";
        $query .= "                                             AND ";
        $query .= "                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$year."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     WHERE ";
        $query .= "         TEST_ID IN (".$testid.") ";
        $query .= "     GROUP BY ";
        $query .= "         TEST_ID, ";
        $query .= "         TAKE_TURN, ";
        $query .= "         KNJID ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN ";
        $query .= " ORDER BY ";
        $query .= "     TAKE_TURN ";
        
        return $query;
    }
    
    
    //大分野別用-----------------------------------------------------------------------------
    //大分野名称と配点クラス平均・最高点取得
    function getPartClass($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t3.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         FIELDNO, ";
        $query .= "         MAX(SCORE) as MAX, ";
        $query .= "         AVG(TO_NUMBER(SCORE)) as AVG ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             FIELDNO, ";
        $query .= "             KNJID, ";
        $query .= "             SUM(QUESTION_SCORE) as SCORE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 t1.TEST_ID, ";
        $query .= "                 t1.KNJID, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO,1,4) as FIELDNO, ";
        $query .= "                 t1.QUESTION_SCORE ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "                 left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "                 t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "             WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             KNJID IN (SELECT ";
        $query .= "                                                                             KNJID ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                         YEAR, ";
        $query .= "                                                                                                                                         SEMESTER, ";
        $query .= "                                                                                                                                         GRADE, ";
        $query .= "                                                                                                                                         HR_CLASS ";
        $query .= "                                                                                                                                     FROM ";
        $query .= "                                                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                         SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                                                     FROM ";
        $query .= "                                                                                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                                         KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                     ) AND ";
        $query .= "                                                                                                                                         YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                         SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                     ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         ) AND ";
        $query .= "                                                             TEST_ID IN (".$testid.") AND ";
        $query .= "                                                             YEAR = '".$year."' ";
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID ";
        $query .= "                                                         ) ";
        $query .= "                     ) ";
        $query .= "                 GROUP BY ";
        $query .= "             KNJID, ";
        $query .= "             FIELDNO ";
        $query .= "         ORDER BY ";
        $query .= "             FIELDNO, ";
        $query .= "             KNJID ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         FIELDNO ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID as FIELDNO, ";
        $query .= "                     THIRD_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t3 on t1.FIELDNO = t3.FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    //大分野別学年平均・最高点取得
    function getPartGrade($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     FIELDNO, ";
        $query .= "     MAX(SCORE) as GRADE_MAX, ";
        $query .= "     AVG(TO_NUMBER(SCORE)) as GRADE_AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         FIELDNO, ";
        $query .= "         KNJID, ";
        $query .= "         SUM(QUESTION_SCORE) as SCORE ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             t1.TEST_ID, ";
        $query .= "             t1.KNJID, ";
        $query .= "             SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as FIELDNO, ";
        $query .= "             t1.QUESTION_SCORE ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "             left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "             t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "         WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 KNJID IN ( ";
        $query .= "                                                                             SELECT ";
        $query .= "                                                                                 KNJID ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                                 YEAR, ";
        $query .= "                                                                                                                                 SEMESTER, ";
        $query .= "                                                                                                                                 GRADE ";
        $query .= "                                                                                                                             FROM ";
        $query .= "                                                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                                                             WHERE ";
        $query .= "                                                                                                                                 SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                 SCHREGNO ";
        $query .= "                                                                                                                                             FROM ";
        $query .= "                                                                                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                                                                                             WHERE ";
        $query .= "                                                                                                                                                 KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                             ) AND ";
        $query .= "                                                                                                                                 YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                 SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                              ) ";
        $query .= "                                                                                                 ) ";
        $query .= "                                                                             ) ";
        $query .= "                                                             AND ";
        $query .= "                                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                                             AND ";
        $query .= "                                                                 YEAR = '".$year."' ";
        $query .= "                                                             GROUP BY ";
        $query .= "                                                                 YEAR, ";
        $query .= "                                                                 KNJID, ";
        $query .= "                                                                 TEST_ID ";
        $query .= "                                                             ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         FIELDNO, ";
        $query .= "         KNJID ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    
    //大分野選択時の大分野ごと全テストの合計点取得
    function getPartSchreg($year, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     FIELDNO, ";
        $query .= "     INTEGER(SUM(QUESTION_SCORE)) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.TEST_ID, ";
        $query .= "         t1.KNJID, ";
        $query .= "         SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as FIELDNO, ";
        $query .= "         t1.QUESTION_SCORE ";
        $query .= "     FROM ";
        $query .= "         PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "         left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "         t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "     WHERE ";
        $query .= "         (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                         YEAR, ";
        $query .= "                                                         KNJID, ";
        $query .= "                                                         TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(END_DATE) ";
        }else{
            $query .= "                                                         MIN(END_DATE) ";
        }
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         KNJID = '".$knjid."' AND ";
        $query .= "                                                         TEST_ID in (".$testid.") AND ";
        $query .= "                                                         YEAR = '".$year."' ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         YEAR, ";
        $query .= "                                                         KNJID, ";
        $query .= "                                                         TEST_ID ";
        $query .= "                                                     ) ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    
    //大分野選択時右下のデータ取得
    //クラス平均・クラス最高点取得
    function getPartClassPoint($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.TAKE_TURN ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEST_ID, ";
        $query .= "         FIELDNO, ";
        $query .= "         POINT, ";
        $query .= "         AVG(TO_NUMBER(SCORE)) as AVG, ";
        $query .= "         MAX(SCORE) as MAX ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             TEST_ID, ";
        $query .= "             FIELDNO, ";
        $query .= "             KNJID, ";
        $query .= "             SUM(QUESTION_POINT) as POINT, ";
        $query .= "             SUM(QUESTION_SCORE) as SCORE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 t1.KNJID, ";
        $query .= "                 t1.TEST_ID, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as FIELDNO, ";
        $query .= "                 t1.QUESTION_POINT, ";
        $query .= "                 t1.QUESTION_SCORE ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "                 left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "                 t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "             WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             KNJID IN ( ";
        $query .= "                                                                         SELECT ";
        $query .= "                                                                             KNJID ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                         YEAR, ";
        $query .= "                                                                                                                                         SEMESTER, ";
        $query .= "                                                                                                                                         GRADE, ";
        $query .= "                                                                                                                                         HR_CLASS ";
        $query .= "                                                                                                                                     FROM ";
        $query .= "                                                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                         SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                                                     FROM ";
        $query .= "                                                                                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                                         KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                     ) AND ";
        $query .= "                                                                                                                                         YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                         SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                     ) ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                         ) ";
        $query .= "                                                         AND ";
        $query .= "                                                             TEST_ID IN (".$testid.") ";
        $query .= "                                                         AND ";
        $query .= "                                                             YEAR = '".$year."' ";
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID ";
        $query .= "                                                         ) ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             TEST_ID, ";
        $query .= "             FIELDNO, ";
        $query .= "             KNJID ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         TEST_ID, ";
        $query .= "         FIELDNO, ";
        $query .= "         POINT ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     TEST_ID, ";
        $query .= "                     TAKE_TURN ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$year."' ";
        $query .= "                 ) t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    
    //大分野別学年平均・学年最高点取得
    function getPartGradePoint($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.TAKE_TURN ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEST_ID, ";
        $query .= "         FIELDNO, ";
        $query .= "         POINT, ";
        $query .= "         AVG(TO_NUMBER(SCORE)) as GRADE_AVG, ";
        $query .= "         MAX(SCORE) as GRADE_MAX ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             TEST_ID, ";
        $query .= "             FIELDNO, ";
        $query .= "             KNJID, ";
        $query .= "             SUM(QUESTION_POINT) as POINT, ";
        $query .= "             SUM(QUESTION_SCORE) as SCORE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 t1.KNJID, ";
        $query .= "                 t1.TEST_ID, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as FIELDNO, ";
        $query .= "                 t1.QUESTION_POINT, ";
        $query .= "                 t1.QUESTION_SCORE ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "                 left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "                 t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "             WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     KNJID IN ( ";
        $query .= "                                                                                 SELECT ";
        $query .= "                                                                                     KNJID ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     PV_SCHREG_MST ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                                     SCHREGNO ";
        $query .= "                                                                                                 FROM ";
        $query .= "                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                 WHERE ";
        $query .= "                                                                                                     (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                                     YEAR, ";
        $query .= "                                                                                                                                     SEMESTER, ";
        $query .= "                                                                                                                                     GRADE ";
        $query .= "                                                                                                                                 FROM ";
        $query .= "                                                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                 WHERE ";
        $query .= "                                                                                                                                     SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                     SCHREGNO ";
        $query .= "                                                                                                                                                 FROM ";
        $query .= "                                                                                                                                                     PV_SCHREG_MST ";
        $query .= "                                                                                                                                                 WHERE ";
        $query .= "                                                                                                                                                     KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                 ) AND ";
        $query .= "                                                                                                                                     YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                     SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                  ) ";
        $query .= "                                                                                                     ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                                 AND ";
        $query .= "                                                                     TEST_ID IN (".$testid.") ";
        $query .= "                                                                 AND ";
        $query .= "                                                                     YEAR = '".$year."' ";
        $query .= "                                                                 GROUP BY ";
        $query .= "                                                                     YEAR, ";
        $query .= "                                                                     KNJID, ";
        $query .= "                                                                     TEST_ID ";
        $query .= "                                                                 ) ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             TEST_ID, ";
        $query .= "             FIELDNO, ";
        $query .= "             KNJID ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         TEST_ID, ";
        $query .= "         FIELDNO, ";
        $query .= "         POINT ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     TEST_ID, ";
        $query .= "                     TAKE_TURN ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$year."' ";
        $query .= "                 ) t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    //大分野ごとTEST_IDごとの個人得点取得
    function getPartSchregPoint($year, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     TEST_ID, ";
        $query .= "     FIELDNO, ";
        $query .= "     INTEGER(SUM(QUESTION_SCORE)) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.TEST_ID, ";
        $query .= "         t1.KNJID, ";
        $query .= "         SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as FIELDNO, ";
        $query .= "         t1.QUESTION_SCORE ";
        $query .= "     FROM ";
        $query .= "         PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "         left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "         t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "     WHERE ";
        $query .= "         (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                         YEAR, ";
        $query .= "                                                         KNJID, ";
        $query .= "                                                         TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(END_DATE) ";
        }else{
            $query .= "                                                         MIN(END_DATE) ";
        }
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         KNJID = '".$knjid."' AND ";
        $query .= "                                                         TEST_ID in (".$testid.") AND ";
        $query .= "                                                         YEAR = '".$year."' ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         YEAR, ";
        $query .= "                                                         KNJID, ";
        $query .= "                                                         TEST_ID ";
        $query .= "                                                     ) ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     TEST_ID, ";
        $query .= "     FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     TEST_ID, ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    
    //小分野別用--------------------------------------------------
    //大分野の名称とその大分野に付く小分野の個数取得
    function getPartCnt($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         COUNT(QUESTION_FIELDNO) as QUESTION_CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             SUBSTR(QUESTION_FIELDNO,1, 4) as PART_FIELDNO, ";
        $query .= "             SUBSTR(QUESTION_FIELDNO,1, 6) as QUESTION_FIELDNO ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "             left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "             t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "         WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 KNJID IN ( ";
        $query .= "                                                                             SELECT ";
        $query .= "                                                                                 KNJID ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                             YEAR, ";
        $query .= "                                                                                                                                             SEMESTER, ";
        $query .= "                                                                                                                                             GRADE, ";
        $query .= "                                                                                                                                             HR_CLASS ";
        $query .= "                                                                                                                                         FROM ";
        $query .= "                                                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                         WHERE ";
        $query .= "                                                                                                                                             SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                                                         FROM ";
        $query .= "                                                                                                                                                             PV_SCHREG_MST ";
        $query .= "                                                                                                                                                         WHERE ";
        $query .= "                                                                                                                                                             KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                         ) AND ";
        $query .= "                                                                                                                                             YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                             SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                         ) ";
        $query .= "                                                                                                 ) ";
        $query .= "                                                                             ) ";
        $query .= "                                                             AND ";
        $query .= "                                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                                             AND ";
        $query .= "                                                                 YEAR = '".$year."' ";
        $query .= "                                                             GROUP BY ";
        $query .= "                                                                 YEAR, ";
        $query .= "                                                                 KNJID, ";
        $query .= "                                                                 TEST_ID ";
        $query .= "                                                             ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         PART_FIELDNO ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID as FIELDNO, ";
        $query .= "                     THIRD_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t2 on t1.PART_FIELDNO = t2.FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO ";
        
        return $query;
    }
    //小分野名称とFIELDNOを取得
    function getQuestNo($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.FOURTH_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         SUBSTR(QUESTION_FIELDNO,1, 4) as PART_FIELDNO, ";
        $query .= "         SUBSTR(QUESTION_FIELDNO,1, 6) as QUESTION_FIELDNO ";
        $query .= "     FROM ";
        $query .= "         PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "         left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "         t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "     WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             KNJID IN ( ";
        $query .= "                                                                         SELECT ";
        $query .= "                                                                             KNJID ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                         YEAR, ";
        $query .= "                                                                                                                                         SEMESTER, ";
        $query .= "                                                                                                                                         GRADE, ";
        $query .= "                                                                                                                                         HR_CLASS ";
        $query .= "                                                                                                                                     FROM ";
        $query .= "                                                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                         SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                                                     FROM ";
        $query .= "                                                                                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                                         KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                     ) AND ";
        $query .= "                                                                                                                                         YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                         SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                     ) ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                         ) ";
        $query .= "                                                         AND ";
        $query .= "                                                             TEST_ID IN (".$testid.") ";
        $query .= "                                                         AND ";
        $query .= "                                                             YEAR = '".$year."' ";
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID ";
        $query .= "                                                         ) ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID || FOURTH_ID as FIELDNO, ";
        $query .= "                     FOURTH_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t2 on t1.QUESTION_FIELDNO = t2.FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野のクラス平均・最高点を取得
    function getQuestClass($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO, ";
        $query .= "     MAX(SCORE) as MAX, ";
        $query .= "     AVG(SCORE) as AVG ";
/*        $query .= "     MAX(AVG) as MAX, ";
        $query .= "     AVG(AVG) as AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         KNJID, ";
        $query .= "         AVG(SCORE) as AVG ";*/
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             KNJID, ";
        $query .= "             PART_FIELDNO, ";
        $query .= "             QUESTION_FIELDNO, ";
//        $query .= "             TEST_ID, ";
        $query .= "             SUM(QUESTION_SCORE) as SCORE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 t1.KNJID, ";
        $query .= "                 t1.TEST_ID, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as PART_FIELDNO, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO, 1, 6) as QUESTION_FIELDNO, ";
        $query .= "                 t1.QUESTION_SCORE ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "                 left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "                 t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "             WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 KNJID in (SELECT ";
        $query .= "                                                                                 KNJID ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 SCHREGNO in (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 (YEAR,SEMESTER,GRADE,HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                         YEAR, ";
        $query .= "                                                                                                                                         SEMESTER, ";
        $query .= "                                                                                                                                         GRADE, ";
        $query .= "                                                                                                                                         HR_CLASS ";
        $query .= "                                                                                                                                     FROM ";
        $query .= "                                                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                         SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                                                     FROM ";
        $query .= "                                                                                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                                         KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                     ) AND ";
        $query .= "                                                                                                                                         YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                         SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                     ) ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                             ) AND ";
        $query .= "                                                                 TEST_ID in(".$testid.") ";
        $query .= "                                                             AND ";
        $query .= "                                                                 YEAR = '".$year."' ";
        $query .= "                                                             GROUP BY ";
        $query .= "                                                                 YEAR, ";
        $query .= "                                                                 KNJID, ";
        $query .= "                                                                 TEST_ID ";
        $query .= "                                                             ) ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             KNJID, ";
//        $query .= "             TEST_ID, ";
        $query .= "             PART_FIELDNO, ";
        $query .= "             QUESTION_FIELDNO ";
        $query .= "         ) ";
/*        $query .= "     GROUP BY ";
        $query .= "         KNJID, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO ";
        $query .= "     ) ";*/
        $query .= " GROUP BY ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野ごとの学年平均・学年最高点取得
    function getQuestGrade($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO, ";
        $query .= "     MAX(SCORE) as GRADE_MAX, ";
        $query .= "     AVG(SCORE) as GRADE_AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         KNJID, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         SUM(QUESTION_SCORE) as SCORE ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             t1.KNJID, ";
        $query .= "             t1.TEST_ID, ";
        $query .= "             SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as PART_FIELDNO, ";
        $query .= "             SUBSTR(t1.QUESTION_FIELDNO, 1, 6) as QUESTION_FIELDNO, ";
        $query .= "             t1.QUESTION_SCORE ";
        $query .= "         FROM ";
        $query .= "             PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "             left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "             t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "         WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             KNJID in (SELECT ";
        $query .= "                                                                             KNJID ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO in (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR,SEMESTER,GRADE) in (SELECT ";
        $query .= "                                                                                                                             YEAR, ";
        $query .= "                                                                                                                             SEMESTER, ";
        $query .= "                                                                                                                             GRADE ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                                         FROM ";
        $query .= "                                                                                                                                             PV_SCHREG_MST ";
        $query .= "                                                                                                                                         WHERE ";
        $query .= "                                                                                                                                             KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                         ) AND ";
        $query .= "                                                                                                                             YEAR = '".$year."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         ) AND ";
        $query .= "                                                             TEST_ID in(".$testid.") ";
        $query .= "                                                             AND ";
        $query .= "                                                                 YEAR = '".$year."' ";
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID ";
        $query .= "                                                         ) ";
        $query .= "         ) ";
        $query .= "         GROUP BY ";
        $query .= "             KNJID, ";
        $query .= "             PART_FIELDNO, ";
        $query .= "             QUESTION_FIELDNO ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    
    //小分野ごとの個人成績トータル
    function getQuestSchreg($year, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO, ";
        $query .= "     INTEGER(SUM(QUESTION_SCORE)) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.TEST_ID, ";
        $query .= "         t1.KNJID, ";
        $query .= "         SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as PART_FIELDNO, ";
        $query .= "         SUBSTR(t1.QUESTION_FIELDNO, 1, 6) as QUESTION_FIELDNO, ";
        $query .= "         t1.QUESTION_SCORE ";
        $query .= "     FROM ";
        $query .= "         PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "         left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "         t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "     WHERE ";
        $query .= "         (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                         YEAR, ";
        $query .= "                                                         KNJID, ";
        $query .= "                                                         TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(END_DATE) ";
        }else{
            $query .= "                                                         MIN(END_DATE) ";
        }
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         KNJID = '".$knjid."' AND ";
        $query .= "                                                         TEST_ID in (".$testid.") AND ";
        $query .= "                                                         YEAR = '".$year."' ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         YEAR, ";
        $query .= "                                                         KNJID, ";
        $query .= "                                                         TEST_ID ";
        $query .= "                                                     ) ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        
        return $query;
    }
    
    //小分野ごと、TEST_IDごとのクラス平均・クラス最高点取得
    function getQuestClassPoint($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.TAKE_TURN ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEST_ID, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         POINT, ";
        $query .= "         MAX(SCORE) as MAX, ";
        $query .= "         AVG(TO_NUMBER(SCORE)) as AVG ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             KNJID, ";
        $query .= "             TEST_ID, ";
        $query .= "             PART_FIELDNO, ";
        $query .= "             QUESTION_FIELDNO, ";
        $query .= "             SUM(QUESTION_POINT) as POINT, ";
        $query .= "             SUM(QUESTION_SCORE) as SCORE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 t1.KNJID, ";
        $query .= "                 t1.TEST_ID, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO,1, 4) as PART_FIELDNO, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO,1, 6) as QUESTION_FIELDNO, ";
        $query .= "                 t1.QUESTION_POINT, ";
        $query .= "                 t1.QUESTION_SCORE ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "                 left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "                 t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "             WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             KNJID IN ( ";
        $query .= "                                                                         SELECT ";
        $query .= "                                                                             KNJID ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                         YEAR, ";
        $query .= "                                                                                                                                         SEMESTER, ";
        $query .= "                                                                                                                                         GRADE, ";
        $query .= "                                                                                                                                         HR_CLASS ";
        $query .= "                                                                                                                                     FROM ";
        $query .= "                                                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                         SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                                                     FROM ";
        $query .= "                                                                                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                                         KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                     ) AND ";
        $query .= "                                                                                                                                         YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                         SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                     ) ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                         ) ";
        $query .= "                                                         AND ";
        $query .= "                                                             TEST_ID IN (".$testid.") ";
        $query .= "                                                         AND ";
        $query .= "                                                             YEAR = '".$year."' ";
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID ";
        $query .= "                                                         ) ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             KNJID, ";
        $query .= "             TEST_ID, ";
        $query .= "             PART_FIELDNO, ";
        $query .= "             QUESTION_FIELDNO ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         TEST_ID, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         POINT ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     TEST_ID, ";
        $query .= "                     TAKE_TURN ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$year."' ";
        $query .= "                 ) t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野ごと、TEST_IDごとの学年平均・学年最高点取得
    function getQuestGradePoint($year, $semester, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.TAKE_TURN ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEST_ID, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         POINT, ";
        $query .= "         MAX(SCORE) as GRADE_MAX, ";
        $query .= "         AVG(TO_NUMBER(SCORE)) as GRADE_AVG ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             KNJID, ";
        $query .= "             TEST_ID, ";
        $query .= "             PART_FIELDNO, ";
        $query .= "             QUESTION_FIELDNO, ";
        $query .= "             SUM(QUESTION_POINT) as POINT, ";
        $query .= "             SUM(QUESTION_SCORE) as SCORE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 t1.KNJID, ";
        $query .= "                 t1.TEST_ID, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO,1, 4) as PART_FIELDNO, ";
        $query .= "                 SUBSTR(t1.QUESTION_FIELDNO,1, 6) as QUESTION_FIELDNO, ";
        $query .= "                 t1.QUESTION_POINT, ";
        $query .= "                 t1.QUESTION_SCORE ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "                 left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "                 t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "             WHERE ";
        $query .= "             (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                             YEAR, ";
        $query .= "                                                             KNJID, ";
        $query .= "                                                             TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(END_DATE) as END_DATE ";
        }else{
            $query .= "                                                             MIN(END_DATE) as END_DATE ";
        }
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     KNJID IN ( ";
        $query .= "                                                                 SELECT ";
        $query .= "                                                                     KNJID ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                     YEAR, ";
        $query .= "                                                                                                                     SEMESTER, ";
        $query .= "                                                                                                                     GRADE ";
        $query .= "                                                                                                                 FROM ";
        $query .= "                                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                                 WHERE ";
        $query .= "                                                                                                                     SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                     SCHREGNO ";
        $query .= "                                                                                                                                 FROM ";
        $query .= "                                                                                                                                     PV_SCHREG_MST ";
        $query .= "                                                                                                                                 WHERE ";
        $query .= "                                                                                                                                     KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                 ) AND ";
        $query .= "                                                                                                                     YEAR = '".$year."' AND ";
        $query .= "                                                                                                                     SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                  ) ";
        $query .= "                                                                                     ) ";
        $query .= "                                                                 ) ";
        $query .= "                                                 AND ";
        $query .= "                                                     TEST_ID IN (".$testid.") ";
        $query .= "                                                 AND ";
        $query .= "                                                     YEAR = '".$year."' ";
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     YEAR, ";
        $query .= "                                                     KNJID, ";
        $query .= "                                                     TEST_ID ";
        $query .= "                                                 ) ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             KNJID, ";
        $query .= "             TEST_ID, ";
        $query .= "             PART_FIELDNO, ";
        $query .= "             QUESTION_FIELDNO ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         TEST_ID, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         POINT ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     TEST_ID, ";
        $query .= "                     TAKE_TURN ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$year."' ";
        $query .= "                 ) t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野ごと、TEST_IDごとの個人得点取得
    function getQuestSchregPoint($year, $knjid, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     TEST_ID, ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO, ";
        $query .= "     INTEGER(SUM(QUESTION_SCORE)) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.TEST_ID, ";
        $query .= "         t1.KNJID, ";
        $query .= "         SUBSTR(t1.QUESTION_FIELDNO, 1, 4) as PART_FIELDNO, ";
        $query .= "         SUBSTR(t1.QUESTION_FIELDNO, 1, 6) as QUESTION_FIELDNO, ";
        $query .= "         t1.QUESTION_SCORE ";
        $query .= "     FROM ";
        $query .= "         PV_CBT_SCORE_DETAIL_DAT t1  ";
        $query .= "         left join PV_CBT_SCORE_COUNT_DAT t2 on t1.YEAR = t2.YEAR and t1.KNJID = t2.KNJID and  ";
        $query .= "         t1.TEST_ID = t2.TEST_ID and t1.TAKE_CNT = t2.TAKE_CNT and t1.TAKE_GRADE = t2.TAKE_GRADE and t1.TAKE_CNT = t2.TAKE_CNT ";
        $query .= "     WHERE ";
        $query .= "         (t1.YEAR, t1.KNJID, t1.TEST_ID, END_DATE) IN (SELECT ";
        $query .= "                                                         YEAR, ";
        $query .= "                                                         KNJID, ";
        $query .= "                                                         TEST_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(END_DATE) ";
        }else{
            $query .= "                                                         MIN(END_DATE) ";
        }
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         KNJID = '".$knjid."' AND ";
        $query .= "                                                         TEST_ID in (".$testid.") AND ";
        $query .= "                                                         YEAR = '".$year."' ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         YEAR, ";
        $query .= "                                                         KNJID, ";
        $query .= "                                                         TEST_ID ";
        $query .= "                                                     ) ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     TEST_ID, ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     TEST_ID, ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        
        return $query;
    }
}
?>
