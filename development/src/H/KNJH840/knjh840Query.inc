<?php
class knjh840Query extends Query {
    
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
    
    //サイト名取得
    function getSite($mnid = "")
    {
        $query .= " SELECT ";
        if($mnid == ""){
            $query .= "     NAME1, ";
        }
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H800' ";
        if($mnid != ""){
            $query .= " AND  ";
            $query .= "     NAMECD2 = '".$mnid."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        
        return $query;
    }
    
    //教科取得
    function getKyouka($year, $site, $kyouka="")
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     CLASSCD as VALUE, ";
        $query .= "     CLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if($kyouka != ""){
            $query .= " AND ";
            $query .= "     CLASSCD = '".$kyouka."' ";
        }
        $query .= "  AND ";
        $query .= "     SITE_ID = '".$site."' ";
        $query .= " ORDER BY ";
        $query .= "     CLASSCD ";
        
        return $query;
    }
    //科目取得
    function getKamoku($year, $site, $kyouka, $kamoku="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SUBCLASSCD as VALUE, ";
        $query .= "     SUBCLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " AND ";
        $query .= "     CLASSCD = '".$kyouka."' ";
        if($kamoku != ""){
            $query .= " AND ";
            $query .= "     SUBCLASSCD = '".$kamoku."' ";
        }
        $query .= "  AND ";
        $query .= "     SITE_ID = '".$site."' ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD ";
        
        return $query;
    }
    
    //取込日取得
    function getImportDate()
    {
        $query  = " SELECT ";
        $query .= "     SUBSTR(MAX(UPDATED), 1, 10) ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";

        return $query;
    }
    
    //大分野・小分野ごとのときのテスト全体のクラス平均点と最高点を取得　左上
    function getAllClass($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     MAX(CNT) as MAX, ";
        $query .= "     AVG(TO_NUMBER(CNT)) as AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         STUDENT_ID, ";
        $query .= "         SUM(RIGHT_CNT) as CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                     AND ";
        $query .= "                                                         SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         ) ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "     AND ";
        $query .= "         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "     GROUP BY ";
        $query .= "         STUDENT_ID ";
        $query .= "     ) ";
        
        return $query;
    }
    //大分野・小分野ごとのときのテスト全体の学年平均と学年最高点取得　左上
    function getAllGrade($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     MAX(CNT) as GRADE_MAX, ";
        $query .= "     AVG(TO_NUMBER(CNT)) as GRADE_AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         STUDENT_ID, ";
        $query .= "         SUM(RIGHT_CNT) as CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                 GRADE = (SELECT DISTINCT ";
        $query .= "                                                                                                             GRADE ";
        $query .= "                                                                                                         FROM ";
        $query .= "                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                     AND ";
        $query .= "                                                         SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         ) ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "     AND ";
        $query .= "         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "     GROUP BY ";
        $query .= "         STUDENT_ID ";
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
        }
        $query .= " SELECT ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     ALL_CNT as POINT, ";
        $query .= "     MAX(RIGHT_CNT) as MAX, ";
        $query .= "     AVG(TO_NUMBER(RIGHT_CNT)) as AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "     WHERE ";
        $query .= "         (STUDENT_ID, TAKE_CNT,TEACHING_CD) IN (SELECT ";
        $query .= "                                                     STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                     TEACHING_CD ";
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     STUDENT_ID IN (SELECT ";
        $query .= "                                                                         LOWER(LOGINID) ";
        $query .= "                                                                     FROM ";
        $query .= "                                                                         PV_SCHREG_MST ";
        $query .= "                                                                     WHERE ";
        $query .= "                                                                         SCHREGNO IN (SELECT ";
        $query .= "                                                                                         SCHREGNO ";
        $query .= "                                                                                     FROM ";
        $query .= "                                                                                         CHAIR_STD_DAT ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                         CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                     ) ";
        $query .= "                                                                     ) ";
        $query .= "                                                 AND ";
        $query .= "                                                     SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     STUDENT_ID, ";
        $query .= "                                                     TEACHING_CD ";
        $query .= "                                                 ) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= " AND ";
        $query .= "     CLASSCD = '".$field["KYOKA"]."' ";
        if($field["KAMOKU"] != ""){
            $query .= " AND ";
            $query .= "     SUBCLASSCD = '".$field["KAMOKU"]."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     ALL_CNT ";
        $query .= " ORDER BY ";
        $query .= "     TEACHING_CD ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    //学年平均・学年最高点取得　右上　共通
    function getTestGrade($topfield, $field, $teachingcd)
    {
        $query .= " SELECT ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     ALL_CNT as POINT, ";
        $query .= "     MAX(RIGHT_CNT) as GRADE_MAX, ";
        $query .= "     AVG(TO_NUMBER(RIGHT_CNT)) as GRADE_AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "     WHERE ";
        $query .= "         (STUDENT_ID, TAKE_CNT,TEACHING_CD) IN (SELECT ";
        $query .= "                                                     STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                     TEACHING_CD ";
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     STUDENT_ID IN (SELECT ";
        $query .= "                                                                         LOWER(LOGINID) ";
        $query .= "                                                                     FROM ";
        $query .= "                                                                         PV_SCHREG_MST ";
        $query .= "                                                                     WHERE ";
        $query .= "                                                                         SCHREGNO IN (SELECT ";
        $query .= "                                                                                         SCHREGNO ";
        $query .= "                                                                                     FROM ";
        $query .= "                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                         GRADE IN (SELECT DISTINCT ";
        $query .= "                                                                                                         GRADE ";
        $query .= "                                                                                                     FROM ";
        $query .= "                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                     WHERE ";
        $query .= "                                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                         SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                                     ) ";
        $query .= "                                                                                     ) ";
        $query .= "                                                                     )  ";
        $query .= "                                                 AND ";
        $query .= "                                                     SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     STUDENT_ID, ";
        $query .= "                                                     TEACHING_CD ";
        $query .= "                                                 ) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= " AND ";
        $query .= "     TEACHING_CD IN (".$teachingcd.") ";
        $query .= " GROUP BY ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     ALL_CNT ";
        $query .= " ORDER BY ";
        $query .= "     TEACHING_CD ";
        
        return $query;
    }
    
    //回数別用-----------------------------------------------------------------------------
    //回数別時の得点率取得
    function getGetPercent($topfield, $field, $testid)
    {
        $query  = " SELECT ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     TYPE, ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         a1.*, ";
        $query .= "         case when PERCENT >= 100 then '1'  ";
        $query .= "         when PERCENT >= 80 AND PERCENT < 100 then '2' ";
        $query .= "         when PERCENT >= 60 AND PERCENT < 80 then '3' ";
        $query .= "         when PERCENT >= 40 AND PERCENT < 60 then '4' ";
        $query .= "         when PERCENT >= 20 AND PERCENT < 40 then '5' ";
        $query .= "         else '6' end as TYPE ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             TEACHING_CD, ";
        $query .= "             TEACHING_NAME, ";
        $query .= "             STUDENT_ID, ";
        $query .= "             TO_NUMBER(RIGHT_CNT) / TO_NUMBER(ALL_CNT) * 100 as PERCENT ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 PV_MBT_SCORE_COUNT_DAT ";
        $query .= "             WHERE ";
        $query .= "                 (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                             STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                             MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             STUDENT_ID IN (SELECT ";
        $query .= "                                                                                 LOWER(LOGINID) ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                             ) ";
        $query .= "                                                         AND ";
        $query .= "                                                             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                         AND ";
        $query .= "                                                             SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         ) AND ";
        $query .= "                 YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "             ) ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "             CLASSCD = '".$field["KYOKA"]."' ";
        if($field["KAMOKU"] != ""){
            $query .= "         AND ";
            $query .= "             SUBCLASSCD = '".$field["KAMOKU"]."' ";
        }
        $query .= "         ) a1 ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     TYPE ";
        $query .= " ORDER BY ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TYPE ";
        
        return $query;
    }
    
    //大分野別用-----------------------------------------------------------------------------
    //大分野名称と配点クラス平均・最高点取得
    function getPartClass($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     a1.FIELDNO, ";
        $query .= "     case when t1.AVG IS NOT NULL then t1.AVG else '0' end as AVG, ";
        $query .= "     case when t1.MAX IS NOT NULL then t1.MAX else '0' end as MAX, ";
        $query .= "     t2.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "             FIELDNO ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 TEACHING_CD, ";
        $query .= "                 STUDENT_ID, ";
        $query .= "                 SUBSTR(FIELDNO,1,4) as FIELDNO, ";
        $query .= "                 FIELDNO as FFIELDNO, ";
        $query .= "                 JUDGE ";
        $query .= "             FROM ";
        $query .= "                 PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                 (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                             STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                             MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             STUDENT_ID IN (SELECT ";
        $query .= "                                                                                 LOWER(LOGINID) ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                             ) AND ";
        $query .= "                                                             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                         AND ";
        $query .= "                                                             SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         ) AND ";
        $query .= "                 TEACHING_CD in (".$teachingcd.") ";
        $query .= "             ) ";
        $query .= "         ) a1 ";
        $query .= "     left join  ";
        $query .= "     (SELECT ";
        $query .= "         FIELDNO, ";
        $query .= "         AVG(TO_NUMBER(CNT)) as AVG, ";
        $query .= "         MAX(CNT) as MAX ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             TEACHING_CD, ";
        $query .= "             STUDENT_ID, ";
        $query .= "             FIELDNO, ";
        $query .= "             SUM(JUDGE) as CNT ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 TEACHING_CD, ";
        $query .= "                 STUDENT_ID, ";
        $query .= "                 SUBSTR(FIELDNO,1,4) as FIELDNO, ";
        $query .= "                 FIELDNO as FFIELDNO, ";
        $query .= "                 JUDGE ";
        $query .= "             FROM ";
        $query .= "                 PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                 (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                             STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                             MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             STUDENT_ID IN (SELECT ";
        $query .= "                                                                                 LOWER(LOGINID) ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                             ) AND ";
        $query .= "                                                             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                         AND ";
        $query .= "                                                             SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         ) AND ";
        $query .= "                 TEACHING_CD in (".$teachingcd.") ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             TEACHING_CD, ";
        $query .= "             STUDENT_ID, ";
        $query .= "             FIELDNO ";
        $query .= "         )  ";
        $query .= "     GROUP BY ";
        $query .= "         FIELDNO ";
        $query .= "     ) t1  on a1.FIELDNO = t1.FIELDNO ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID as FIELDNO, ";
        $query .= "                     THIRD_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t2 on a1.FIELDNO = t2.FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     a1.FIELDNO ";

        return $query;
    }
    //大分野別学年平均・最高点取得
    function getPartGrade($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     a1.FIELDNO, ";
        $query .= "     case when t1.AVG IS NOT NULL then t1.AVG else '0' end as GRADE_AVG, ";
        $query .= "     case when t1.MAX IS NOT NULL then t1.MAX else '0' end as GRADE_MAX, ";
        $query .= "     t2.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "             FIELDNO ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 TEACHING_CD, ";
        $query .= "                 STUDENT_ID, ";
        $query .= "                 SUBSTR(FIELDNO,1,4) as FIELDNO, ";
        $query .= "                 FIELDNO as FFIELDNO, ";
        $query .= "                 JUDGE ";
        $query .= "             FROM ";
        $query .= "                 PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                 (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                             STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                             MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             STUDENT_ID IN (SELECT ";
        $query .= "                                                                                 LOWER(LOGINID) ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                     SCHREGNO ";
        $query .= "                                                                                                 FROM ";
        $query .= "                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                 WHERE ";
        $query .= "                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                     GRADE = (SELECT DISTINCT ";
        $query .= "                                                                                                                 GRADE ";
        $query .= "                                                                                                             FROM ";
        $query .= "                                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                                             WHERE ";
        $query .= "                                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                                 SCHREGNO ";
        $query .= "                                                                                                                             FROM ";
        $query .= "                                                                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                                                                             WHERE ";
        $query .= "                                                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                             ) ";
        $query .= "                                                                                                             ) ";
        $query .= "                                                                                                 ) ";
        $query .= "                                                                             )  AND ";
        $query .= "                                                             YEAR = '".$topfield["EXP_YEAR"]."'  ";
        $query .= "                                                         AND ";
        $query .= "                                                             SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         ) AND ";
        $query .= "                 TEACHING_CD in (".$teachingcd.") ";
        $query .= "             ) ";
        $query .= "         ) a1 ";
        $query .= "     left join  ";
        $query .= "     (SELECT ";
        $query .= "         FIELDNO, ";
        $query .= "         AVG(TO_NUMBER(CNT)) as AVG, ";
        $query .= "         MAX(CNT) as MAX ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             TEACHING_CD, ";
        $query .= "             STUDENT_ID, ";
        $query .= "             FIELDNO, ";
        $query .= "             SUM(JUDGE) as CNT ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 TEACHING_CD, ";
        $query .= "                 STUDENT_ID, ";
        $query .= "                 SUBSTR(FIELDNO,1,4) as FIELDNO, ";
        $query .= "                 FIELDNO as FFIELDNO, ";
        $query .= "                 JUDGE ";
        $query .= "             FROM ";
        $query .= "                 PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                 (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                             STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                             MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                             MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             STUDENT_ID IN (SELECT ";
        $query .= "                                                                                 LOWER(LOGINID) ";
        $query .= "                                                                             FROM ";
        $query .= "                                                                                 PV_SCHREG_MST ";
        $query .= "                                                                             WHERE ";
        $query .= "                                                                                 SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                     SCHREGNO ";
        $query .= "                                                                                                 FROM ";
        $query .= "                                                                                                     SCHREG_REGD_DAT ";
        $query .= "                                                                                                 WHERE ";
        $query .= "                                                                                                     YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                     SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                     GRADE = (SELECT DISTINCT ";
        $query .= "                                                                                                                 GRADE ";
        $query .= "                                                                                                             FROM ";
        $query .= "                                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                                             WHERE ";
        $query .= "                                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                 SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                                 SCHREGNO ";
        $query .= "                                                                                                                             FROM ";
        $query .= "                                                                                                                                 CHAIR_STD_DAT ";
        $query .= "                                                                                                                             WHERE ";
        $query .= "                                                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                                 CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                             ) ";
        $query .= "                                                                                                             ) ";
        $query .= "                                                                                                 ) ";
        $query .= "                                                                             )  AND ";
        $query .= "                                                             YEAR = '".$topfield["EXP_YEAR"]."'  ";
        $query .= "                                                         AND ";
        $query .= "                                                             SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         ) AND ";
        $query .= "                 TEACHING_CD in (".$teachingcd.") ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             TEACHING_CD, ";
        $query .= "             STUDENT_ID, ";
        $query .= "             FIELDNO ";
        $query .= "         )  ";
        $query .= "     GROUP BY ";
        $query .= "         FIELDNO ";
        $query .= "     ) t1  on a1.FIELDNO = t1.FIELDNO ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID as FIELDNO, ";
        $query .= "                     THIRD_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t2 on a1.FIELDNO = t2.FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     a1.FIELDNO ";

        
        return $query;
    }
    
    //大分野選択時右下のデータ取得
    //クラス平均・クラス最高点取得
    function getPartClassPoint($topfield, $field, $teachingcd)
    {
        $query .= " SELECT ";
        $query .= "         TEACHING_CD, ";
        $query .= "         FIELDNO, ";
        $query .= "         POINT, ";
        $query .= "         AVG(TO_NUMBER(CNT)) as AVG, ";
        $query .= "         MAX(CNT) as MAX ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             a2.TEACHING_CD, ";
        $query .= "             a1.STUDENT_ID, ";
        $query .= "             a2.FIELDNO, ";
        $query .= "             case when a1.CNT IS NOT NULL then a1.CNT else '0' end as CNT, ";
        $query .= "             POINT ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 TEACHING_CD,  ";
        $query .= "                 FIELDNO,  ";
        $query .= "                 COUNT(*) as POINT  ";
        $query .= "             FROM  ";
        $query .= "                 (SELECT DISTINCT ";
        $query .= "                     TEACHING_CD,  ";
        $query .= "                     SUBSTR(FIELDNO,1,4) as FIELDNO,  ";
        $query .= "                     QUESTION_NO  ";
        $query .= "                 FROM  ";
        $query .= "                     PV_MBT_SCORE_DETAIL_DAT  ";
        $query .= "                 )  ";
        $query .= "             GROUP BY  ";
        $query .= "                 TEACHING_CD,  ";
        $query .= "                 FIELDNO  ";
        $query .= "             ) a2  ";
        $query .= "             left join (SELECT ";
        $query .= "                             TEACHING_CD, ";
        $query .= "                             STUDENT_ID, ";
        $query .= "                             FIELDNO , ";
        $query .= "                             SUM(JUDGE) as CNT ";
        $query .= "                        FROM ";
        $query .= "                         (SELECT ";
        $query .= "                             TEACHING_CD, ";
        $query .= "                             STUDENT_ID, ";
        $query .= "                             SUBSTR(FIELDNO,1,4) as FIELDNO, ";
        $query .= "                             JUDGE ";
        $query .= "                         FROM ";
        $query .= "                             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     FROM ";
        $query .= "                                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                     WHERE ";
        $query .= "                                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                                             LOWER(LOGINID) ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             PV_SCHREG_MST ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                                             SCHREGNO ";
        $query .= "                                                                                                         FROM ";
        $query .= "                                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                         )  AND ";
        $query .= "                                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                                     AND ";
        $query .= "                                                                         SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                                     AND ";
            $query .= "                                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                                     AND ";
            $query .= "                                                                           DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                                     GROUP BY ";
        $query .= "                                                                         STUDENT_ID, ";
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     ) AND ";
        $query .= "                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                 TEACHING_CD in (".$teachingcd.") ";
        $query .= "                             )  ";
        $query .= "                             GROUP BY TEACHING_CD,STUDENT_ID,FIELDNO ";
        $query .= "                         ) a1 on a1.TEACHING_CD = a2.TEACHING_CD and a1.FIELDNO = a2.FIELDNO ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         TEACHING_CD, ";
        $query .= "         FIELDNO, ";
        $query .= "         POINT ";
        $query .= " ORDER BY ";
        $query .= "     TEACHING_CD,FIELDNO ";

        
        return $query;
    }
    
    //大分野別学年平均・学年最高点取得
    function getPartGradePoint($topfield, $field, $teachingcd)
    {
        $query .= " SELECT ";
        $query .= "         TEACHING_CD, ";
        $query .= "         FIELDNO, ";
        $query .= "         AVG(TO_NUMBER(CNT)) as GRADE_AVG, ";
        $query .= "         MAX(CNT) as GRADE_MAX ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             a2.TEACHING_CD, ";
        $query .= "             a1.STUDENT_ID, ";
        $query .= "             a2.FIELDNO, ";
        $query .= "             case when a1.CNT IS NOT NULL then a1.CNT else '0' end as CNT, ";
        $query .= "             POINT ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 TEACHING_CD, ";
        $query .= "                 FIELDNO, ";
        $query .= "                 COUNT(*) as POINT ";
        $query .= "             FROM ";
        $query .= "                 (SELECT DISTINCT ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     SUBSTR(FIELDNO,1,4) as FIELDNO, ";
        $query .= "                     FIELDNO as FFIELDNO ";
        $query .= "                 FROM ";
        $query .= "                     PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                 ) ";
        $query .= "             GROUP BY ";
        $query .= "                 TEACHING_CD, ";
        $query .= "                 FIELDNO ";
        $query .= "             ) a2  ";
        $query .= "             left join (SELECT TEACHING_CD, STUDENT_ID, FIELDNO , SUM(JUDGE) as CNT FROM ";
        $query .= "                         (SELECT ";
        $query .= "                             TEACHING_CD, ";
        $query .= "                             STUDENT_ID, ";
        $query .= "                             SUBSTR(FIELDNO,1,4) as FIELDNO, ";
        $query .= "                             JUDGE ";
        $query .= "                         FROM ";
        $query .= "                             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     FROM ";
        $query .= "                                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                     WHERE ";
        $query .= "                                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                                             LOWER(LOGINID) ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             PV_SCHREG_MST ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                                 SCHREGNO ";
        $query .= "                                                                                                             FROM ";
        $query .= "                                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                                             WHERE ";
        $query .= "                                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                 GRADE = (SELECT DISTINCT ";
        $query .= "                                                                                                                             GRADE ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                                         FROM ";
        $query .= "                                                                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                                                                         WHERE ";
        $query .= "                                                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                                         ) ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                                             ) ";
        $query .= "                                                                                         )  AND ";
        $query .= "                                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                                                     AND ";
        $query .= "                                                                         SITE_ID = '".$field["SITE"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                                     AND ";
            $query .= "                                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                                     AND ";
            $query .= "                                                                           DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                                     GROUP BY ";
        $query .= "                                                                         STUDENT_ID, ";
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     ) AND ";
        $query .= "                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                 TEACHING_CD in (".$teachingcd.") ";
        $query .= "                             )  ";
        $query .= "                             GROUP BY TEACHING_CD,STUDENT_ID,FIELDNO ";
        $query .= "                         ) a1 on a1.TEACHING_CD = a2.TEACHING_CD and a1.FIELDNO = a2.FIELDNO ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         TEACHING_CD, ";
        $query .= "         FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     TEACHING_CD,FIELDNO ";
        
        return $query;
    }
    
    //小分野別用--------------------------------------------------
    //大分野の名称とその大分野に付く小分野の個数取得
    function getPartCnt($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         COUNT(QUEST_FIELDNO) as QUESTION_CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             SUBSTR(FIELDNO,1,4) as PART_FIELDNO, ";
        $query .= "             SUBSTR(FIELDNO,1,6) as QUEST_FIELDNO ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                     AND ";
        $query .= "                                                         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         PART_FIELDNO ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID as PART_FIELDNO, ";
        $query .= "                     THIRD_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t2 on t1.PART_FIELDNO = t2.PART_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO ";
        
        return $query;
    }
    //小分野名称とFIELDNOを取得
    function getQuestNo($topfield, $field, $teachingcd)
    {
        $query .= " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.FOURTH_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "             SUBSTR(FIELDNO,1,4) as PART_FIELDNO, ";
        $query .= "             SUBSTR(FIELDNO,1,6) as QUESTION_FIELDNO ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                     AND ";
        $query .= "                                                         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID || FOURTH_ID as QUEST_FIELDNO, ";
        $query .= "                     FOURTH_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t2 on t1.QUESTION_FIELDNO = t2.QUEST_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     PART_FIELDNO, QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野のクラス平均・最高点を取得
    function getQuestClass($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     a1.PART_FIELDNO, ";
        $query .= "     a1.QUESTION_FIELDNO, ";
        $query .= "     case when MAX(a2.CNT) IS NULL then '0' else MAX(a2.CNT) end as MAX, ";
        $query .= "     case when AVG(TO_NUMBER(a2.CNT)) IS NULL then '0' else AVG(TO_NUMBER(a2.CNT)) end as AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         STUDENT_ID, ";
        $query .= "         TEACHING_CD, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         SUM(JUDGE) as CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             STUDENT_ID, ";
        $query .= "             TEACHING_CD, ";
        $query .= "             SUBSTR(FIELDNO,1,4) as PART_FIELDNO, ";
        $query .= "             SUBSTR(FIELDNO,1,6) as QUESTION_FIELDNO, ";
        $query .= "             JUDGE ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                     AND ";
        $query .= "                                                         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         STUDENT_ID, ";
        $query .= "         TEACHING_CD, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO ";
        $query .= "     ) a1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     STUDENT_ID, ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     PART_FIELDNO, ";
        $query .= "                     QUESTION_FIELDNO, ";
        $query .= "                     SUM(JUDGE) as CNT ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         STUDENT_ID, ";
        $query .= "                         TEACHING_CD, ";
        $query .= "                         SUBSTR(FIELDNO,1,4) as PART_FIELDNO, ";
        $query .= "                         SUBSTR(FIELDNO,1,6) as QUESTION_FIELDNO, ";
        $query .= "                         JUDGE ";
        $query .= "                     FROM ";
        $query .= "                         PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                         (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                                     STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                                     MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                                     MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                                     TEACHING_CD ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     STUDENT_ID IN (SELECT ";
        $query .= "                                                                                         LOWER(LOGINID) ";
        $query .= "                                                                                     FROM ";
        $query .= "                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         SCHREGNO IN (SELECT ";
        $query .= "                                                                                                         SCHREGNO ";
        $query .= "                                                                                                     FROM ";
        $query .= "                                                                                                         CHAIR_STD_DAT ";
        $query .= "                                                                                                     WHERE ";
        $query .= "                                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                         CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                     ) ";
        $query .= "                                                                                     )  AND ";
        $query .= "                                                                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                                 AND ";
        $query .= "                                                                     TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                                                 GROUP BY ";
        $query .= "                                                                     STUDENT_ID, ";
        $query .= "                                                                     TEACHING_CD ";
        $query .= "                                                                 ) ";
        $query .= "                     ) ";
        $query .= "                 GROUP BY ";
        $query .= "                     STUDENT_ID, ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     PART_FIELDNO, ";
        $query .= "                     QUESTION_FIELDNO ";
        $query .= "                 ) a2 on a1.PART_FIELDNO = a2.PART_FIELDNO and a1.QUESTION_FIELDNO = a2.QUESTION_FIELDNO ";
        $query .= " GROUP BY ";
        $query .= "     a1.PART_FIELDNO, ";
        $query .= "     a1.QUESTION_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     a1.PART_FIELDNO, ";
        $query .= "     a1.QUESTION_FIELDNO ";
        
        return $query;
    }
    //小分野ごとの学年平均・学年最高点取得
    function getQuestGrade($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     a1.PART_FIELDNO, ";
        $query .= "     a1.QUESTION_FIELDNO, ";
        $query .= "     case when MAX(a2.CNT) IS NULL then '0' else MAX(a2.CNT) end as GRADE_MAX, ";
        $query .= "     case when AVG(TO_NUMBER(a2.CNT)) IS NULL then '0' else AVG(TO_NUMBER(a2.CNT)) end as GRADE_AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         STUDENT_ID, ";
        $query .= "         TEACHING_CD, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO, ";
        $query .= "         SUM(JUDGE) as CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             STUDENT_ID, ";
        $query .= "             TEACHING_CD, ";
        $query .= "             SUBSTR(FIELDNO,1,4) as PART_FIELDNO, ";
        $query .= "             SUBSTR(FIELDNO,1,6) as QUESTION_FIELDNO, ";
        $query .= "             JUDGE ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                 GRADE = (SELECT DISTINCT ";
        $query .= "                                                                                                             GRADE ";
        $query .= "                                                                                                         FROM ";
        $query .= "                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                     AND ";
        $query .= "                                                         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         STUDENT_ID, ";
        $query .= "         TEACHING_CD, ";
        $query .= "         PART_FIELDNO, ";
        $query .= "         QUESTION_FIELDNO ";
        $query .= "     ) a1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     STUDENT_ID, ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     PART_FIELDNO, ";
        $query .= "                     QUESTION_FIELDNO, ";
        $query .= "                     SUM(JUDGE) as CNT ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         STUDENT_ID, ";
        $query .= "                         TEACHING_CD, ";
        $query .= "                         SUBSTR(FIELDNO,1,4) as PART_FIELDNO, ";
        $query .= "                         SUBSTR(FIELDNO,1,6) as QUESTION_FIELDNO, ";
        $query .= "                         JUDGE ";
        $query .= "                     FROM ";
        $query .= "                         PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                         (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                                     STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                                     MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                                     MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                                     TEACHING_CD ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     STUDENT_ID IN (SELECT ";
        $query .= "                                                                                         LOWER(LOGINID) ";
        $query .= "                                                                                     FROM ";
        $query .= "                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                             SCHREGNO ";
        $query .= "                                                                                                         FROM ";
        $query .= "                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                             GRADE = (SELECT DISTINCT ";
        $query .= "                                                                                                                         GRADE ";
        $query .= "                                                                                                                     FROM ";
        $query .= "                                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                                     WHERE ";
        $query .= "                                                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                         SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                                     FROM ";
        $query .= "                                                                                                                                         CHAIR_STD_DAT ";
        $query .= "                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                                         CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                                     ) ";
        $query .= "                                                                                                                     ) ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                     )  AND ";
        $query .= "                                                                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                                 AND ";
        $query .= "                                                                     TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                                                 GROUP BY ";
        $query .= "                                                                     STUDENT_ID, ";
        $query .= "                                                                     TEACHING_CD ";
        $query .= "                                                                 ) ";
        $query .= "                     ) ";
        $query .= "                 GROUP BY ";
        $query .= "                     STUDENT_ID, ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     PART_FIELDNO, ";
        $query .= "                     QUESTION_FIELDNO ";
        $query .= "                 ) a2 on a1.PART_FIELDNO = a2.PART_FIELDNO and a1.QUESTION_FIELDNO = a2.QUESTION_FIELDNO ";
        $query .= " GROUP BY ";
        $query .= "     a1.PART_FIELDNO, ";
        $query .= "     a1.QUESTION_FIELDNO ";
        $query .= " ORDER BY ";
        $query .= "     a1.PART_FIELDNO, ";
        $query .= "     a1.QUESTION_FIELDNO ";
        
        return $query;
    }
    
    //小分野ごと、実施回ごとのクラス平均・クラス最高点取得
    function getQuestClassPoint($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     t1.TEACHING_CD, ";
        $query .= "     t1.PARTCD, ";
        $query .= "     t1.QUESTCD, ";
        $query .= "     t1.ALLCNT as POINT, ";
        $query .= "     case when t2.MAX IS NOT NULL then t2.MAX else '0' end as MAX, ";
        $query .= "     case when t2.AVG IS NOT NULL then t2.AVG else '0' end as AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEACHING_CD, ";
        $query .= "         PARTCD, ";
        $query .= "         QUESTCD, ";
        $query .= "         COUNT(*) as ALLCNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             TEACHING_CD, ";
        $query .= "             QUESTION_NO, ";
        $query .= "             SUBSTR(FIELDNO, 1, 4) as PARTCD, ";
        $query .= "             SUBSTR(FIELDNO, 1, 6) as QUESTCD ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         ) AND ";
        $query .= "                                                         TEACHING_CD IN (".$teachingcd.") AND ";
        $query .= "                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         TEACHING_CD, ";
        $query .= "         PARTCD, ";
        $query .= "         QUESTCD ";
        $query .= "     ) t1 ";
        $query .= "     left join  (SELECT ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     PARTCD, ";
        $query .= "                     QUESTCD, ";
        $query .= "                     MAX(CNT) as MAX, ";
        $query .= "                     AVG(TO_NUMBER(CNT)) as AVG ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         STUDENT_ID, ";
        $query .= "                         TEACHING_CD, ";
        $query .= "                         PARTCD, ";
        $query .= "                         QUESTCD, ";
        $query .= "                         SUM(JUDGE) as CNT ";
        $query .= "                     FROM ";
        $query .= "                         (SELECT ";
        $query .= "                             STUDENT_ID, ";
        $query .= "                             TEACHING_CD, ";
        $query .= "                             SUBSTR(FIELDNO, 1, 4) as PARTCD, ";
        $query .= "                             SUBSTR(FIELDNO, 1, 6) as QUESTCD, ";
        $query .= "                             JUDGE ";
        $query .= "                         FROM ";
        $query .= "                             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     FROM ";
        $query .= "                                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                     WHERE ";
        $query .= "                                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                                             LOWER(LOGINID) ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             PV_SCHREG_MST ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                                             SCHREGNO ";
        $query .= "                                                                                                         FROM ";
        $query .= "                                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                         ) AND ";
        $query .= "                                                                         TEACHING_CD IN (".$teachingcd.") AND ";
        $query .= "                                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                                     GROUP BY ";
        $query .= "                                                                         STUDENT_ID, ";
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     ) ";
        $query .= "                         ) ";
        $query .= "                     GROUP BY ";
        $query .= "                         STUDENT_ID, ";
        $query .= "                         TEACHING_CD, ";
        $query .= "                         PARTCD, ";
        $query .= "                         QUESTCD ";
        $query .= "                     ) ";
        $query .= "                 GROUP BY ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     PARTCD, ";
        $query .= "                     QUESTCD ";
        $query .= "                 ) t2 on t1.TEACHING_CD = t2.TEACHING_CD and t1.PARTCD = t2.PARTCD and t1.QUESTCD = t2.QUESTCD ";
        $query .= " ORDER BY ";
        $query .= "     t1.TEACHING_CD, ";
        $query .= "     t1.PARTCD, ";
        $query .= "     t1.QUESTCD ";

        
        return $query;
    }
    //小分野ごと、実施回数ごとの学年平均・学年最高点取得
    function getQuestGradePoint($topfield, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     t1.TEACHING_CD, ";
        $query .= "     t1.PARTCD, ";
        $query .= "     t1.QUESTCD, ";
        $query .= "     t1.ALLCNT, ";
        $query .= "     case when t2.MAX IS NOT NULL then t2.MAX else '0' end as GRADE_MAX, ";
        $query .= "     case when t2.AVG IS NOT NULL then t2.AVG else '0' end as GRADE_AVG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEACHING_CD, ";
        $query .= "         PARTCD, ";
        $query .= "         QUESTCD, ";
        $query .= "         COUNT(*) as ALLCNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             TEACHING_CD, ";
        $query .= "             QUESTION_NO, ";
        $query .= "             SUBSTR(FIELDNO, 1, 4) as PARTCD, ";
        $query .= "             SUBSTR(FIELDNO, 1, 6) as QUESTCD ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                             FROM ";
        $query .= "                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                             WHERE ";
        $query .= "                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                 GRADE = (SELECT DISTINCT ";
        $query .= "                                                                                                             GRADE ";
        $query .= "                                                                                                         FROM ";
        $query .= "                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                             ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                     AND ";
        $query .= "                                                         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         TEACHING_CD, ";
        $query .= "         PARTCD, ";
        $query .= "         QUESTCD ";
        $query .= "     ) t1 ";
        $query .= "     left join  (SELECT ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     PARTCD, ";
        $query .= "                     QUESTCD, ";
        $query .= "                     MAX(CNT) as MAX, ";
        $query .= "                     AVG(TO_NUMBER(CNT)) as AVG ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         STUDENT_ID, ";
        $query .= "                         TEACHING_CD, ";
        $query .= "                         PARTCD, ";
        $query .= "                         QUESTCD, ";
        $query .= "                         SUM(JUDGE) as CNT ";
        $query .= "                     FROM ";
        $query .= "                         (SELECT ";
        $query .= "                             STUDENT_ID, ";
        $query .= "                             TEACHING_CD, ";
        $query .= "                             SUBSTR(FIELDNO, 1, 4) as PARTCD, ";
        $query .= "                             SUBSTR(FIELDNO, 1, 6) as QUESTCD, ";
        $query .= "                             JUDGE ";
        $query .= "                         FROM ";
        $query .= "                             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             (STUDENT_ID,TAKE_CNT, TEACHING_CD ) IN (SELECT ";
        $query .= "                                                                         STUDENT_ID, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                                         MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                                         MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     FROM ";
        $query .= "                                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                     WHERE ";
        $query .= "                                                                         STUDENT_ID IN (SELECT ";
        $query .= "                                                                                             LOWER(LOGINID) ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             PV_SCHREG_MST ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             SCHREGNO IN  (SELECT ";
        $query .= "                                                                                                                 SCHREGNO ";
        $query .= "                                                                                                             FROM ";
        $query .= "                                                                                                                 SCHREG_REGD_DAT ";
        $query .= "                                                                                                             WHERE ";
        $query .= "                                                                                                                 YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                 SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                 GRADE = (SELECT DISTINCT ";
        $query .= "                                                                                                                             GRADE ";
        $query .= "                                                                                                                         FROM ";
        $query .= "                                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                                         WHERE ";
        $query .= "                                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                                                                             SCHREGNO ";
        $query .= "                                                                                                                                         FROM ";
        $query .= "                                                                                                                                             CHAIR_STD_DAT ";
        $query .= "                                                                                                                                         WHERE ";
        $query .= "                                                                                                                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                                                                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                                                                                                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                                                                                                                         ) ";
        $query .= "                                                                                                                         ) ";
        $query .= "                                                                                                             ) ";
        $query .= "                                                                                         )  AND ";
        $query .= "                                                                         YEAR = '".$topfield["EXP_YEAR"]."' ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                                     AND ";
            $query .= "                                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                                     AND ";
            $query .= "                                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                                     AND ";
        $query .= "                                                                         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                                                     GROUP BY ";
        $query .= "                                                                         STUDENT_ID, ";
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     ) ";
        $query .= "                         ) ";
        $query .= "                     GROUP BY ";
        $query .= "                         STUDENT_ID, ";
        $query .= "                         TEACHING_CD, ";
        $query .= "                         PARTCD, ";
        $query .= "                         QUESTCD ";
        $query .= "                     ) ";
        $query .= "                 GROUP BY ";
        $query .= "                     TEACHING_CD, ";
        $query .= "                     PARTCD, ";
        $query .= "                     QUESTCD ";
        $query .= "                 ) t2 on t1.TEACHING_CD = t2.TEACHING_CD and t1.PARTCD = t2.PARTCD and t1.QUESTCD = t2.QUESTCD ";
        $query .= " ORDER BY ";
        $query .= "     t1.TEACHING_CD, ";
        $query .= "     t1.PARTCD, ";
        $query .= "     t1.QUESTCD ";
        
        return $query;
    }
    
}
?>
