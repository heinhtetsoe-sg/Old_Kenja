<?php
class knjh810Query extends Query {
    
    //年度
    function getYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR as VALUE, ";
        $query .= "     YEAR as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT ";

        return $query;
    }
    //学期
    function getSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER as VALUE, ";
        $query .= "     SEMESTERNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER != '9' AND ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }

    //科目データの取得
    function getSubclassMst($model) {
        $query  = "";
        $query .= " WITH REPLACE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1' AS DIV, ";
        $query .= "     COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$model->topfield["EXP_YEAR"]."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.SUBCLASSCD AS VALUE, ";
        $query .= "     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= "     LEFT JOIN REPLACE L1 ON L1.DIV = '1' ";
        $query .= "         AND T3.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     (T1.YEAR, T1.SEMESTER, T1.SUBCLASSCD, T1.CURRICULUM_CD) in (SELECT ";
        $query .= "                                                                     YEAR, ";
        $query .= "                                                                     SEMESTER, ";
        $query .= "                                                                     SUBCLASSCD, ";
        $query .= "                                                                     MAX(CURRICULUM_CD) ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     CHAIR_DAT ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     YEAR = '".$model->topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                     SEMESTER = '".$model->topfield["SEMESTER"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "                                                                 AND SCHOOL_KIND       = '".SCHOOLKIND."' ";
        }
        $query .= "                                                                 GROUP BY ";
        $query .= "                                                                     YEAR, ";
        $query .= "                                                                     SEMESTER, ";
        $query .= "                                                                     SUBCLASSCD ";
        $query .= "                                                                 ) ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T4.YEAR       = T1.YEAR ";
        $query .= "     AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";

        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "     AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "     AND T1.SCHOOL_KIND       = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || '：' || T1.chairname AS LABEL ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".$model->topfield["EXP_YEAR"]."' AND ";
        $query .= "     T1.semester   = '".$model->topfield["SEMESTER"]."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        $query .= "     T1.subclasscd = '".$model->topfield["SUBCLASSCD"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "     AND T1.SCHOOL_KIND       = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
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
    
    //大分野・小分野ごとのときのテスト全体のクラス平均点と最高点を取得　左上
    function getAllClass($topfield, $field)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SCORE) as MAX, ";
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
        $query .= "                                                 KNJID IN (SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                 SCHREGNO ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                             ) ";
        $query .= "                                                             ) ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "         ) t1";
        $query .= "         left join PV_CBT_SUBCLASS_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "             t2.CLASSCD = '".$field["KYOKA"]."' ";
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
        $query .= "     GROUP BY ";
        $query .= "         KNJID ";
        $query .= "     ) ";
        
        return $query;
    }
    //大分野・小分野ごとのときのテスト全体の学年平均と学年最高点取得　左上
    function getAllGrade($topfield, $field)
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
        $query .= "                                                 KNJID IN (SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN  (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                     GRADE =  (SELECT DISTINCT ";
        $query .= "                                                                                                     GRADE ";
        $query .= "                                                                                                 FROM ";
        $query .= "                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                 WHERE ";
        $query .= "                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                     SCHREGNO ";
        $query .= "                                                                                                                 FROM ";
        $query .= "                                                                                                                     CHAIR_STD_DAT ";
        $query .= "                                                                                                                 WHERE ";
        $query .= "                                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                     CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                 ) ";
        $query .= "                                                                                                 ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                             ) ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "         ) t1";
        $query .= "         left join PV_CBT_SUBCLASS_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         t2.CLASSCD = '".$field["KYOKA"]."' ";
        if($field["KAMOKU"] != ""){
            $query .= "         AND ";
            $query .= "             t1.SUBCLASSCD = '".$field["KAMOKU"]."' ";
        }
        if($field["TEST_KIND"] != ""){
            $query .= "         AND ";
            $query .= "             t1.TEST_LEVEL_CD = '".$field["TEST_KIND"]."' ";
        }
        if($field["F_KAISU"] != "" && $field["T_KAISU"] != ""){
            $query .= "         AND ";
            $query .= "             t1.TAKE_TURN BETWEEN '".$field["F_KAISU"]."' AND '".$field["T_KAISU"]."' ";
        }else if($field["F_KAISU"] != "" && $field["T_KAISU"] == ""){
            $query .= "         AND ";
            $query .= "             t1.TAKE_TURN >= '".$field["F_KAISU"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         KNJID ";
        $query .= "     ) ";
        
        return $query;
    }
    
    //実施回と配点等取得　右上　共通
    function getTestClass($topfield, $field, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }else{
            $query  = " SELECT ";
            $query .= "     a1.*, ";
            $query .= "     a2.TEST_KIND_NAME ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= " SELECT ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN, ";
        $query .= "     POINT, ";
        $query .= "     MAX(SCORE) as MAX, ";
        $query .= "     AVG(TO_NUMBER(SCORE)) as AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.TEST_ID, ";
        $query .= "         t1.TAKE_TURN, ";
        $query .= "         t1.KNJID, ";
        $query .= "         t1.POINT, ";
        $query .= "         SUM(t1.GET_POINT) as SCORE ";
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
        $query .= "                                                 KNJID IN (SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                 SCHREGNO ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                             ) ";
        $query .= "                                                             ) ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "         ) t1";
        $query .= "         left join PV_CBT_SUBCLASS_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
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
        $query .= "     GROUP BY ";
        $query .= "         t1.TEST_ID, ";
        $query .= "         t1.TAKE_TURN, ";
        $query .= "         t1.KNJID, ";
        $query .= "         t1.POINT ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN, ";
        $query .= "     POINT ";
        $query .= " ORDER BY ";
        $query .= "     TAKE_TURN ";
        if($mode != ""){
            $query .= "     ) ";
        }else{
            $query .= " ) a1 ";
            $query .= " left join PV_CBT_TESTKIND_MST a2 on a1.TEST_ID = a2.TEST_ID ";
            $query .= " ORDER BY ";
            $query .= "     TAKE_TURN ";
        }
        
        return $query;
    }
    //学年平均・学年最高点取得　右上　共通
    function getTestGrade($topfield, $field, $testid)
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
        $query .= "                                                 KNJID IN (SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN  (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                     GRADE =  (SELECT DISTINCT ";
        $query .= "                                                                                                     GRADE ";
        $query .= "                                                                                                 FROM ";
        $query .= "                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                 WHERE ";
        $query .= "                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                     SCHREGNO ";
        $query .= "                                                                                                                 FROM ";
        $query .= "                                                                                                                     CHAIR_STD_DAT ";
        $query .= "                                                                                                                 WHERE ";
        $query .= "                                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                     CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                 ) ";
        $query .= "                                                                                                 ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                             ) ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' ";
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
    
    //回数別用-----------------------------------------------------------------------------
    //回数別時の得点率取得
    function getGetPercent($topfield, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN, ";
        $query .= "     TYPE, ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         a1.*, ";
        $query .= "         case when PERCENT >= 100 then '1' ";
        $query .= "              when PERCENT >= 80 AND PERCENT < 100 then '2'  ";
        $query .= "              when PERCENT >= 60 AND PERCENT < 80 then '3'  ";
        $query .= "              when PERCENT >= 40 AND PERCENT < 60 then '4'  ";
        $query .= "              when PERCENT >= 20 AND PERCENT < 40 then '5'  ";
        $query .= "              else '6' end as TYPE ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             t1.TEST_ID, ";
        $query .= "             t1.TAKE_TURN, ";
        $query .= "             t1.KNJID, ";
        $query .= "             TO_NUMBER(t1.GET_POINT) / TO_NUMBER(t1.POINT) * 100 as PERCENT ";
        $query .= "         FROM ";
        $query .= "             ( ";
        $query .= "             SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "             WHERE ";
        $query .= "                 (KNJID, END_DATE, TEST_ID) IN (SELECT ";
        $query .= "                                                     KNJID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                     MAX(END_DATE) as END_DATE , ";
        }else{
            $query .= "                                                     MIN(END_DATE) as END_DATE , ";
        }
        $query .= "                                                     TEST_ID ";
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     KNJID IN (SELECT ";
        $query .= "                                                                     KNJID ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     CHAIR_STD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                     CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                 ) ";
        $query .= "                                                                 ) ";
        $query .= "                                                 AND ";
        $query .= "                                                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     KNJID, ";
        $query .= "                                                     TEST_ID ";
        $query .= "                                                 ) ";
        $query .= "             AND ";
        $query .= "                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "             ) t1";
        $query .= "             left join PV_CBT_SUBCLASS_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= "         WHERE ";
        $query .= "             t1.KNJID in (SELECT ";
        $query .= "                             KNJID ";
        $query .= "                         FROM ";
        $query .= "                             PV_SCHREG_MST ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO IN (SELECT ";
        $query .= "                                             SCHREGNO ";
        $query .= "                                         FROM ";
        $query .= "                                             CHAIR_STD_DAT ";
        $query .= "                                         WHERE ";
        $query .= "                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                         ) ";
        $query .= "                         ) ";
        $query .= "         AND ";
        $query .= "             t2.CLASSCD = '".$field["KYOKA"]."' ";
        if($field["KAMOKU"] != ""){
            $query .= "         AND ";
            $query .= "             t1.SUBCLASSCD = '".$field["KAMOKU"]."' ";
        }
        if($field["TEST_KIND"] != ""){
            $query .= "         AND ";
            $query .= "             t1.TEST_LEVEL_CD = '".$field["TEST_KIND"]."' ";
        }
        if($field["F_KAISU"] != "" && $field["T_KAISU"] != ""){
            $query .= "     AND ";
            $query .= "         t1.TAKE_TURN BETWEEN '".$field["F_KAISU"]."' AND '".$field["T_KAISU"]."' ";
        }else if($field["F_KAISU"] != "" && $field["T_KAISU"] == ""){
            $query .= "     AND ";
            $query .= "         t1.TAKE_TURN >= '".$field["F_KAISU"]."' ";
        }
        $query .= "         ) a1 ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN, ";
        $query .= "     TYPE ";
        $query .= " ORDER BY ";
        $query .= "     TAKE_TURN,TYPE ";
        
        return $query;
    }
    
    //大分野別用-----------------------------------------------------------------------------
    //大分野名称と配点クラス平均・最高点取得
    function getPartClass($topfield, $field, $testid)
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
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     KNJID IN (SELECT ";
        $query .= "                                                                     KNJID ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     CHAIR_STD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                     CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                 ) ";
        $query .= "                                                                 ) AND ";
        $query .= "                                                     TEST_ID IN (".$testid.") ";
        $query .= "                                                 AND ";
        $query .= "                                                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     YEAR, ";
        $query .= "                                                     KNJID, ";
        $query .= "                                                     TEST_ID ";
        $query .= "                                                 ) ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
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
    function getPartGrade($topfield, $field, $testid)
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
        $query .= "                                             FROM ";
        $query .= "                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 KNJID IN (SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN  (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                     GRADE =  (SELECT DISTINCT ";
        $query .= "                                                                                                     GRADE ";
        $query .= "                                                                                                 FROM ";
        $query .= "                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                 WHERE ";
        $query .= "                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                     SCHREGNO ";
        $query .= "                                                                                                                 FROM ";
        $query .= "                                                                                                                     CHAIR_STD_DAT ";
        $query .= "                                                                                                                 WHERE ";
        $query .= "                                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                     CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                 ) ";
        $query .= "                                                                                                 ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                           ) AND ";
        $query .= "                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 YEAR, ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
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
    
    //大分野選択時右下のデータ取得
    //クラス平均・クラス最高点取得
    function getPartClassPoint($topfield, $field, $testid)
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
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     KNJID IN (SELECT ";
        $query .= "                                                                     KNJID ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     CHAIR_STD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                     CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                 ) ";
        $query .= "                                                                 ) AND ";
        $query .= "                                                     TEST_ID IN (".$testid.") ";
        $query .= "                                                 AND ";
        $query .= "                                                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     YEAR, ";
        $query .= "                                                     KNJID, ";
        $query .= "                                                     TEST_ID ";
        $query .= "                                                 ) ";
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
        $query .= "                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                 ) t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    
    //大分野別学年平均・学年最高点取得
    function getPartGradePoint($topfield, $field, $testid)
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
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     KNJID IN (SELECT ";
        $query .= "                                                                     KNJID ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     SCHREGNO IN  (SELECT ";
        $query .= "                                                                                         SCHREGNO ";
        $query .= "                                                                                     FROM ";
        $query .= "                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                         GRADE =  (SELECT DISTINCT ";
        $query .= "                                                                                                         GRADE ";
        $query .= "                                                                                                     FROM ";
        $query .= "                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                     WHERE ";
        $query .= "                                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                         SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                     FROM ";
        $query .= "                                                                                                                         CHAIR_STD_DAT ";
        $query .= "                                                                                                                     WHERE ";
        $query .= "                                                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                         CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                     ) ";
        $query .= "                                                                                                     ) ";
        $query .= "                                                                                     ) ";
        $query .= "                                                             ) AND ";
        $query .= "                                                     TEST_ID IN (".$testid.") ";
        $query .= "                                                 AND ";
        $query .= "                                                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     YEAR, ";
        $query .= "                                                     KNJID, ";
        $query .= "                                                     TEST_ID ";
        $query .= "                                                 ) ";
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
        $query .= "                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                 ) t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    
    //小分野別用--------------------------------------------------
    //大分野の名称とその大分野に付く小分野の個数取得
    function getPartCnt($topfield, $field, $testid)
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
        $query .= "                                             FROM ";
        $query .= "                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 KNJID IN (SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                 SCHREGNO ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                             ) ";
        $query .= "                                                             ) AND ";
        $query .= "                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 YEAR, ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
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
    function getQuestNo($topfield, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.FOURTH_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         SUBSTR(QUESTION_FIELDNO,1, 4) as PART_FIELDNO, ";
        $query .= "         SUBSTR(QUESTION_FIELDNO,1, 6) as QUESTION_FIELDNO ";
        $query .= "     FROM ";
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
        $query .= "                                         FROM ";
        $query .= "                                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                         WHERE ";
        $query .= "                                             KNJID IN (SELECT ";
        $query .= "                                                             KNJID ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_SCHREG_MST ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                             SCHREGNO ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                         ) ";
        $query .= "                                                         ) AND ";
        $query .= "                                             TEST_ID IN (".$testid.") ";
        $query .= "                                         AND ";
        $query .= "                                             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                         GROUP BY ";
        $query .= "                                             YEAR, ";
        $query .= "                                             KNJID, ";
        $query .= "                                             TEST_ID ";
        $query .= "                                         ) ";
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
    function getQuestClass($topfield, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO, ";
        $query .= "     MAX(SCORE) as MAX, ";
        $query .= "     AVG(TO_NUMBER(SCORE)) as AVG ";
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
        $query .= "             SUBSTR(t1.QUESTION_FIELDNO,1, 4) as PART_FIELDNO, ";
        $query .= "             SUBSTR(t1.QUESTION_FIELDNO,1, 6) as QUESTION_FIELDNO, ";
        $query .= "             t1.QUESTION_SCORE ";
        $query .= "         FROM ";
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
        $query .= "                                             FROM ";
        $query .= "                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 KNJID IN (SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                 SCHREGNO ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                             ) ";
        $query .= "                                                             ) AND ";
        $query .= "                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 YEAR, ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         KNJID, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野ごとの学年平均・学年最高点取得
    function getQuestGrade($topfield, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO, ";
        $query .= "     MAX(SCORE) as GRADE_MAX, ";
        $query .= "     AVG(TO_NUMBER(SCORE)) as GRADE_AVG ";
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
        $query .= "             SUBSTR(t1.QUESTION_FIELDNO,1, 4) as PART_FIELDNO, ";
        $query .= "             SUBSTR(t1.QUESTION_FIELDNO,1, 6) as QUESTION_FIELDNO, ";
        $query .= "             t1.QUESTION_SCORE ";
        $query .= "         FROM ";
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
        $query .= "                                             FROM ";
        $query .= "                                                 PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 KNJID IN (SELECT ";
        $query .= "                                                                 KNJID ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                             WHERE ";
        $query .= "                                                                 SCHREGNO IN  (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                     GRADE =  (SELECT DISTINCT ";
        $query .= "                                                                                                     GRADE ";
        $query .= "                                                                                                 FROM ";
        $query .= "                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                 WHERE ";
        $query .= "                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                     SCHREGNO ";
        $query .= "                                                                                                                 FROM ";
        $query .= "                                                                                                                     CHAIR_STD_DAT ";
        $query .= "                                                                                                                 WHERE ";
        $query .= "                                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                     CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                 ) ";
        $query .= "                                                                                                 ) ";
        $query .= "                                                                                 ) ";
        $query .= "                                                           ) AND ";
        $query .= "                                                 TEST_ID IN (".$testid.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 YEAR, ";
        $query .= "                                                 KNJID, ";
        $query .= "                                                 TEST_ID ";
        $query .= "                                             ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         KNJID, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    
    //小分野ごと、実施回ごとのクラス平均・クラス最高点取得
    function getQuestClassPoint($topfield, $field, $testid)
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
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     KNJID IN (SELECT ";
        $query .= "                                                                     KNJID ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     SCHREGNO IN (SELECT ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     CHAIR_STD_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                     CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                 ) ";
        $query .= "                                                                 ) AND ";
        $query .= "                                                     TEST_ID IN (".$testid.") ";
        $query .= "                                                 AND ";
        $query .= "                                                     YEAR = '".$topfield["EXP_YEAR"]."' ";
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
        $query .= "                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                 ) t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野ごと、実施回数ごとの学年平均・学年最高点取得
    function getQuestGradePoint($topfield, $field, $testid)
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
        $query .= "                                                     KNJID IN (SELECT ";
        $query .= "                                                                     KNJID ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     SCHREGNO IN  (SELECT ";
        $query .= "                                                                                         SCHREGNO ";
        $query .= "                                                                                     FROM ";
        $query .= "                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                         GRADE =  (SELECT DISTINCT ";
        $query .= "                                                                                                         GRADE ";
        $query .= "                                                                                                     FROM ";
        $query .= "                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                     WHERE ";
        $query .= "                                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                         SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                     FROM ";
        $query .= "                                                                                                                         CHAIR_STD_DAT ";
        $query .= "                                                                                                                     WHERE ";
        $query .= "                                                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                         CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                     ) ";
        $query .= "                                                                                                     ) ";
        $query .= "                                                                                     ) ";
        $query .= "                                                             ) AND ";
        $query .= "                                                     TEST_ID IN (".$testid.") ";
        $query .= "                                                 AND ";
        $query .= "                                                     YEAR = '".$topfield["EXP_YEAR"]."' ";
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
        $query .= "                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                 ) t2 on t1.TEST_ID = t2.TEST_ID ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    
}
?>
