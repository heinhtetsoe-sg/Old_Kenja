<?php
class knjh845Query extends Query {
    
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
    function getKyouka($year, $knjid, $site, $kyouka="")
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
        $query .= "  AND ";
        $query .= "     STUDENT_ID = (SELECT ";
        $query .= "                         LOWER(LOGINID) ";
        $query .= "                     FROM ";
        $query .= "                         PV_SCHREG_MST ";
        $query .= "                     WHERE ";
        $query .= "                         KNJID = '".$knjid."' ";
        $query .= "                     ) ";
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
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     STUDENT_ID = (SELECT ";
        $query .= "                         LOWER(LOGINID) ";
        $query .= "                     FROM ";
        $query .= "                         PV_SCHREG_MST ";
        $query .= "                     WHERE ";
        $query .= "                         KNJID = '".$knjid."' ";
        $query .= "                     ) ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " AND ";
        $query .= "     CLASSCD = '".$field["KYOKA"]."' ";
        if($field["KAMOKU"] != ""){
            $query .= " AND ";
            $query .= "     SUBCLASSCD = '".$field["KAMOKU"]."' ";
        }
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= " AND ";
            $query .= "     DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= " AND ";
            $query .= "     DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        
        return $query;
    }
    
    //大分野・小分野ごとのときのテスト全体のクラス平均点と最高点を取得　左上
    function getAllClass($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "             YEAR = '".$year."' AND ";
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
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$year."' ";
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
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND ";
        $query .= "         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "     GROUP BY ";
        $query .= "         STUDENT_ID ";
        $query .= "     ) ";
        
        return $query;
    }
    //大分野・小分野ごとのときのテスト全体の学年平均と学年最高点取得　左上
    function getAllGrade($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "             YEAR = '".$year."' AND ";
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
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE) in (SELECT ";
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
        $query .= "                                                                                                                          ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$year."' ";
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
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND ";
        $query .= "         TEACHING_CD IN (".$teachingcd.") ";
        $query .= "     GROUP BY ";
        $query .= "         STUDENT_ID ";
        $query .= "     ) ";
        
        return $query;
    }
    //個人得点合計header_x部分
    function getAllSchreg($year, $knjid, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     SUM(RIGHT_CNT) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     (STUDENT_ID, TEACHING_CD, TAKE_CNT) in (SELECT ";
        $query .= "                                                 STUDENT_ID, ";
        $query .= "                                                 TEACHING_CD, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                 MAX(TAKE_CNT) as TAKE_CNT ";
        }else{
            $query .= "                                                 MIN(TAKE_CNT) as TAKE_CNT ";
        }
        $query .= "                                             FROM ";
        $query .= "                                                 PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 STUDENT_ID = (SELECT ";
        $query .= "                                                                 LOWER(LOGINID) ";
        $query .= "                                                               FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                               WHERE ";
        $query .= "                                                                 KNJID = '".$knjid."' ";
        $query .= "                                                               ) ";
        $query .= "                                             AND ";
        $query .= "                                                 SITE_ID = '".$field["SITE"]."' ";
        $query .= "                                             AND ";
        $query .= "                                                 TEACHING_CD IN (".$teachingcd.") ";
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$year."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 STUDENT_ID, ";
        $query .= "                                                 TEACHING_CD ";
        $query .= "                                             ) ";
        
        return $query;
    }

    
    //個人成績データ取得　右上
    function getTestScregData($year, $knjid, $field)
    {
        $query  = " SELECT ";
        $query .= "     STUDENT_ID, ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     TAKE_CNT, ";
        $query .= "     ALL_CNT as POINT, ";
        $query .= "     RIGHT_CNT as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     (STUDENT_ID, TEACHING_CD, TAKE_CNT, YEAR)  in (SELECT ";
        $query .= "                                                 STUDENT_ID, ";
        $query .= "                                                 TEACHING_CD, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                 MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                 MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                 YEAR ";
        $query .= "                                             FROM ";
        $query .= "                                                 PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 STUDENT_ID = (SELECT ";
        $query .= "                                                                 LOWER(LOGINID) ";
        $query .= "                                                               FROM ";
        $query .= "                                                                 PV_SCHREG_MST ";
        $query .= "                                                               WHERE ";
        $query .= "                                                                 KNJID = '".$knjid."' ";
        $query .= "                                                               ) AND ";
        $query .= "                                                 SITE_ID = '".$field["SITE"]."' AND ";
        $query .= "                                                 CLASSCD = '".$field["KYOKA"]."' ";
        if($field["KAMOKU"] != ""){
            $query .= "                                             AND ";
            $query .= "                                                 SUBCLASSCD = '".$field["KAMOKU"]."'  ";
        }
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                             AND ";
        $query .= "                                                 YEAR = '".$year."'  ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 STUDENT_ID, ";
        $query .= "                                                 TEACHING_CD, ";
        $query .= "                                                 YEAR ";
        $query .= "                                             ) ";
        
        return $query;
    }
    
    //実施回と配点等取得　右上　共通
    function getTestClass($year, $semester, $knjid, $field, $teachingcd)
    {
        $query .= " SELECT ";
        $query .= "     TEACHING_CD, ";
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
            $query .= "                                                     MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                     MIN(TAKE_CNT) as TAKE_CNT, ";
        }
        $query .= "                                                     TEACHING_CD ";
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     STUDENT_ID IN ( ";
        $query .= "                                                                     SELECT ";
        $query .= "                                                                         LOWER(LOGINID) ";
        $query .= "                                                                     FROM ";
        $query .= "                                                                         PV_SCHREG_MST ";
        $query .= "                                                                     WHERE ";
        $query .= "                                                                         SCHREGNO IN (SELECT ";
        $query .= "                                                                                         SCHREGNO ";
        $query .= "                                                                                     FROM ";
        $query .= "                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                     YEAR, ";
        $query .= "                                                                                                                                     SEMESTER, ";
        $query .= "                                                                                                                                     GRADE, ";
        $query .= "                                                                                                                                     HR_CLASS ";
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
        $query .= "                                                                                                                                 ) ";
        $query .= "                                                                                         ) ";
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
        $query .= "     YEAR = '".$year."' ";
        $query .= " AND ";
        $query .= "     TEACHING_CD IN (".$teachingcd.") ";
        $query .= " GROUP BY ";
        $query .= "     TEACHING_CD ";
        $query .= " ORDER BY ";
        $query .= "     TEACHING_CD ";
        
        return $query;
    }
    //学年平均・学年最高点取得　右上　共通
    function getTestGrade($year, $semester, $knjid, $field, $teachingcd)
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
            $query .= "                                                     MAX(TAKE_CNT) as TAKE_CNT, ";
        }else{
            $query .= "                                                     MIN(TAKE_CNT) as TAKE_CNT, ";
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
        $query .= "                                                                                         (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                         YEAR, ";
        $query .= "                                                                                                                         SEMESTER, ";
        $query .= "                                                                                                                         GRADE ";
        $query .= "                                                                                                                     FROM ";
        $query .= "                                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                                     WHERE ";
        $query .= "                                                                                                                         SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                         SCHREGNO ";
        $query .= "                                                                                                                                     FROM ";
        $query .= "                                                                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                                                                     WHERE ";
        $query .= "                                                                                                                                         KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                     ) AND ";
        $query .= "                                                                                                                         YEAR = '".$year."' AND ";
        $query .= "                                                                                                                         SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                      ) ";
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
        $query .= "     YEAR = '".$year."' ";
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
    
    
    //大分野別用-----------------------------------------------------------------------------
    //大分野名称と配点クラス平均・最高点取得
    function getPartClass($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "                 YEAR = '".$year."' AND ";
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
        $query .= "                                                                                  SCHREGNO IN (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                               FROM ";
        $query .= "                                                                                                   SCHREG_REGD_DAT ";
        $query .= "                                                                                               WHERE ";
        $query .= "                                                                                                   (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                               YEAR, ";
        $query .= "                                                                                                                                               SEMESTER, ";
        $query .= "                                                                                                                                               GRADE, ";
        $query .= "                                                                                                                                               HR_CLASS ";
        $query .= "                                                                                                                                           FROM ";
        $query .= "                                                                                                                                               SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                           WHERE ";
        $query .= "                                                                                                                                               SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                               SCHREGNO ";
        $query .= "                                                                                                                                                           FROM ";
        $query .= "                                                                                                                                                               PV_SCHREG_MST ";
        $query .= "                                                                                                                                                           WHERE ";
        $query .= "                                                                                                                                                               KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                           ) AND ";
        $query .= "                                                                                                                                               YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                               SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                           ) ";
        $query .= "                                                                                                   ) ";
        $query .= "                                                                             ) AND ";
        $query .= "                                                             YEAR = '".$year."' ";
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
        $query .= "                 YEAR = '".$year."' AND ";
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
        $query .= "                                                                                  SCHREGNO IN (SELECT ";
        $query .= "                                                                                                 SCHREGNO ";
        $query .= "                                                                                               FROM ";
        $query .= "                                                                                                   SCHREG_REGD_DAT ";
        $query .= "                                                                                               WHERE ";
        $query .= "                                                                                                   (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                               YEAR, ";
        $query .= "                                                                                                                                               SEMESTER, ";
        $query .= "                                                                                                                                               GRADE, ";
        $query .= "                                                                                                                                               HR_CLASS ";
        $query .= "                                                                                                                                           FROM ";
        $query .= "                                                                                                                                               SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                           WHERE ";
        $query .= "                                                                                                                                               SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                               SCHREGNO ";
        $query .= "                                                                                                                                                           FROM ";
        $query .= "                                                                                                                                                               PV_SCHREG_MST ";
        $query .= "                                                                                                                                                           WHERE ";
        $query .= "                                                                                                                                                               KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                           ) AND ";
        $query .= "                                                                                                                                               YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                               SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                           ) ";
        $query .= "                                                                                                   ) ";
        $query .= "                                                                             ) AND ";
        $query .= "                                                             YEAR = '".$year."' ";
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
    function getPartGrade($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "                 YEAR = '".$year."' AND ";
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
        $query .= "                                                                                             ) ";
        $query .= "                                                                             )  AND ";
        $query .= "                                                             YEAR = '".$year."'  ";
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
        $query .= "                 YEAR = '".$year."' AND ";
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
        $query .= "                                                                                             ) ";
        $query .= "                                                                             )  AND ";
        $query .= "                                                             YEAR = '".$year."'  ";
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
    //大分野別個人得点取得　左下データ
    function getPartSchreg($year, $knjid, $field, $teachingcd)
    {
        $query .= " SELECT ";
        $query .= "     FIELDNO, ";
        $query .= "     SUM(JUDGE) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEACHING_CD, ";
        $query .= "         TAKE_CNT, ";
        $query .= "         JUDGE, ";
        $query .= "         SUBSTR(FIELDNO, 1, 4) as FIELDNO ";
        $query .= "     FROM ";
        $query .= "         PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         (STUDENT_ID, TEACHING_CD, TAKE_CNT) in (SELECT ";
        $query .= "                                                     STUDENT_ID, ";
        $query .= "                                                     TEACHING_CD, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                     MAX(TAKE_CNT) as TAKE_CNT ";
        }else{
            $query .= "                                                     MIN(TAKE_CNT) as TAKE_CNT ";
        }
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     STUDENT_ID = '".$knjid."' AND ";
        $query .= "                                                     SITE_ID = '".$field["SITE"]."' AND ";
        $query .= "                                                     TEACHING_CD IN (".$teachingcd.") AND ";
        $query .= "                                                     YEAR = '".$year."'   ";
        if($field["F_DATE"] != "" && $field["T_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) BETWEEN '".$field["F_DATE"]."' AND '".$field["T_DATE"]."' ";
        }else if($field["F_DATE"] != ""){
            $query .= "                                                     AND ";
            $query .= "                                                         DATE(START_DATE) >= '".$field["F_DATE"]."' ";
        }
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     STUDENT_ID, ";
        $query .= "                                                     TEACHING_CD ";
        $query .= "                                                 ) ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    
    //大分野選択時右下のデータ取得
    //クラス平均・クラス最高点取得
    function getPartClassPoint($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "                             YEAR = '".$year."' AND ";
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
        $query .= "                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                                       YEAR, ";
        $query .= "                                                                                                                                                       SEMESTER, ";
        $query .= "                                                                                                                                                       GRADE, ";
        $query .= "                                                                                                                                                       HR_CLASS ";
        $query .= "                                                                                                                                                   FROM ";
        $query .= "                                                                                                                                                       SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                                   WHERE ";
        $query .= "                                                                                                                                                       SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                                     SCHREGNO ";
        $query .= "                                                                                                                                                                   FROM ";
        $query .= "                                                                                                                                                                      PV_SCHREG_MST ";
        $query .= "                                                                                                                                                                   WHERE ";
        $query .= "                                                                                                                                                                      KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                                  ) AND ";
        $query .= "                                                                                                                                                       YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                                       SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                                   ) ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                         )  AND ";
        $query .= "                                                                         YEAR = '".$year."' ";
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
        $query .= "                                 YEAR = '".$year."' AND ";
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
    function getPartGradePoint($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "                             YEAR = '".$year."' AND ";
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
        $query .= "                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                                             YEAR, ";
        $query .= "                                                                                                                                             SEMESTER, ";
        $query .= "                                                                                                                                             GRADE ";
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
        $query .= "                                                                                                                                          ) ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                         )  AND ";
        $query .= "                                                                         YEAR = '".$year."' ";
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
        $query .= "                                 YEAR = '".$year."' AND ";
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
    //大分野別、教材コード別個人成績　右下
    function getPartSchregPoint($year, $knjid, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     TEACHING_CD, ";
        $query .= "     FIELDNO, ";
        $query .= "     SUM(JUDGE) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEACHING_CD, ";
        $query .= "         TAKE_CNT, ";
        $query .= "         JUDGE, ";
        $query .= "         SUBSTR(FIELDNO, 1, 4) as FIELDNO ";
        $query .= "     FROM ";
        $query .= "         PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         (STUDENT_ID, TEACHING_CD, TAKE_CNT) in (SELECT ";
        $query .= "                                                     STUDENT_ID, ";
        $query .= "                                                     TEACHING_CD, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                     MAX(TAKE_CNT) as TAKE_CNT ";
        }else{
            $query .= "                                                     MIN(TAKE_CNT) as TAKE_CNT ";
        }
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     STUDENT_ID = (SELECT ";
        $query .= "                                                                     LOWER(LOGINID) ";
        $query .= "                                                                   FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                   WHERE ";
        $query .= "                                                                     KNJID = '".$knjid."' ";
        $query .= "                                                                   ) AND ";
        $query .= "                                                     SITE_ID = '".$field["SITE"]."' AND ";
        $query .= "                                                     TEACHING_CD IN (".$teachingcd.") AND ";
        $query .= "                                                     YEAR = '".$year."'   ";
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
        $query .= " GROUP BY ";
        $query .= "     TEACHING_CD, ";
        $query .= "     FIELDNO ";
        
        return $query;
    }
    
    //小分野別用--------------------------------------------------
    //大分野の名称とその大分野に付く小分野の個数取得
    function getPartCnt($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "             YEAR = '".$year."' AND ";
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
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE) in (SELECT ";
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
        $query .= "                                                                                                                          ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$year."' ";
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
    function getQuestNo($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "             YEAR = '".$year."' AND ";
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
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE) in (SELECT ";
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
        $query .= "                                                                                                                          ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$year."' ";
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
    function getQuestClass($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "             YEAR = '".$year."' AND ";
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
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                      YEAR, ";
        $query .= "                                                                                                                                      SEMESTER, ";
        $query .= "                                                                                                                                      GRADE, ";
        $query .= "                                                                                                                                      HR_CLASS ";
        $query .= "                                                                                                                                    FROM ";
        $query .= "                                                                                                                                      SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                    WHERE ";
        $query .= "                                                                                                                                        SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                        SCHREGNO ";
        $query .= "                                                                                                                                                    FROM ";
        $query .= "                                                                                                                                                        PV_SCHREG_MST ";
        $query .= "                                                                                                                                                    WHERE ";
        $query .= "                                                                                                                                                       KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                   ) AND ";
        $query .= "                                                                                                                                       YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                       SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                   ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$year."' ";
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
        $query .= "                         YEAR = '".$year."' AND ";
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
        $query .= "                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                     WHERE ";
        $query .= "                                                                                                         (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                                  YEAR, ";
        $query .= "                                                                                                                                                  SEMESTER, ";
        $query .= "                                                                                                                                                  GRADE, ";
        $query .= "                                                                                                                                                  HR_CLASS ";
        $query .= "                                                                                                                                                FROM ";
        $query .= "                                                                                                                                                  SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                                WHERE ";
        $query .= "                                                                                                                                                    SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                                    SCHREGNO ";
        $query .= "                                                                                                                                                                FROM ";
        $query .= "                                                                                                                                                                    PV_SCHREG_MST ";
        $query .= "                                                                                                                                                                WHERE ";
        $query .= "                                                                                                                                                                   KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                               ) AND ";
        $query .= "                                                                                                                                                   YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                                   SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                               ) ";
        $query .= "                                                                                                     ) ";
        $query .= "                                                                                     )  AND ";
        $query .= "                                                                     YEAR = '".$year."' ";
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
    function getQuestGrade($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "             YEAR = '".$year."' AND ";
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
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE) in (SELECT ";
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
        $query .= "                                                                                                                          ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$year."' ";
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
        $query .= "                         YEAR = '".$year."' AND ";
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
        $query .= "                                                                                                         SCHREG_REGD_DAT ";
        $query .= "                                                                                                     WHERE ";
        $query .= "                                                                                                         (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                                         YEAR, ";
        $query .= "                                                                                                                                         SEMESTER, ";
        $query .= "                                                                                                                                         GRADE ";
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
        $query .= "                                                                                                                                      ) ";
        $query .= "                                                                                                     ) ";
        $query .= "                                                                                     )  AND ";
        $query .= "                                                                     YEAR = '".$year."' ";
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
    //小分野別個人得点取得　左下
    function getQuestSchreg($year, $knjid, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO, ";
        $query .= "     SUM(JUDGE) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEACHING_CD, ";
        $query .= "         TAKE_CNT, ";
        $query .= "         JUDGE, ";
        $query .= "         SUBSTR(FIELDNO, 1, 4) as PART_FIELDNO, ";
        $query .= "         SUBSTR(FIELDNO, 1, 6) as QUESTION_FIELDNO ";
        $query .= "     FROM ";
        $query .= "         PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         (STUDENT_ID, TEACHING_CD, TAKE_CNT) in (SELECT ";
        $query .= "                                                     STUDENT_ID, ";
        $query .= "                                                     TEACHING_CD, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                     MAX(TAKE_CNT) as TAKE_CNT ";
        }else{
            $query .= "                                                     MIN(TAKE_CNT) as TAKE_CNT ";
        }
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     STUDENT_ID = (SELECT ";
        $query .= "                                                                     LOWER(LOGINID) ";
        $query .= "                                                                   FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                   WHERE ";
        $query .= "                                                                     KNJID = '".$knjid."' ";
        $query .= "                                                                   ) AND ";
        $query .= "                                                     SITE_ID = '".$field["SITE"]."' AND ";
        $query .= "                                                     TEACHING_CD IN (".$teachingcd.") AND ";
        $query .= "                                                     YEAR = '".$year."'   ";
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
        $query .= " GROUP BY ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        
        return $query;
    }
    
    
    //小分野ごと、実施回ごとのクラス平均・クラス最高点取得
    function getQuestClassPoint($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                      YEAR, ";
        $query .= "                                                                                                                                      SEMESTER, ";
        $query .= "                                                                                                                                      GRADE, ";
        $query .= "                                                                                                                                      HR_CLASS ";
        $query .= "                                                                                                                                    FROM ";
        $query .= "                                                                                                                                      SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                    WHERE ";
        $query .= "                                                                                                                                        SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                        SCHREGNO ";
        $query .= "                                                                                                                                                    FROM ";
        $query .= "                                                                                                                                                        PV_SCHREG_MST ";
        $query .= "                                                                                                                                                    WHERE ";
        $query .= "                                                                                                                                                       KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                   ) AND ";
        $query .= "                                                                                                                                       YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                       SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                   ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         ) AND ";
        $query .= "                                                         TEACHING_CD IN (".$teachingcd.") AND ";
        $query .= "                                                         YEAR = '".$year."' ";
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
        $query .= "                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             (YEAR, SEMESTER, GRADE, HR_CLASS) in (SELECT ";
        $query .= "                                                                                                                                                      YEAR, ";
        $query .= "                                                                                                                                                      SEMESTER, ";
        $query .= "                                                                                                                                                      GRADE, ";
        $query .= "                                                                                                                                                      HR_CLASS ";
        $query .= "                                                                                                                                                    FROM ";
        $query .= "                                                                                                                                                      SCHREG_REGD_DAT ";
        $query .= "                                                                                                                                                    WHERE ";
        $query .= "                                                                                                                                                        SCHREGNO = (SELECT ";
        $query .= "                                                                                                                                                                        SCHREGNO ";
        $query .= "                                                                                                                                                                    FROM ";
        $query .= "                                                                                                                                                                        PV_SCHREG_MST ";
        $query .= "                                                                                                                                                                    WHERE ";
        $query .= "                                                                                                                                                                       KNJID = '".$knjid."' ";
        $query .= "                                                                                                                                                                   ) AND ";
        $query .= "                                                                                                                                                       YEAR = '".$year."' AND ";
        $query .= "                                                                                                                                                       SEMESTER = '".$semester."' ";
        $query .= "                                                                                                                                                   ) ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                         ) AND ";
        $query .= "                                                                         TEACHING_CD IN (".$teachingcd.") AND ";
        $query .= "                                                                         YEAR = '".$year."' ";
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
    function getQuestGradePoint($year, $semester, $knjid, $field, $teachingcd)
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
        $query .= "                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                             SCHREGNO ";
        $query .= "                                                                                         FROM ";
        $query .= "                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                         WHERE ";
        $query .= "                                                                                             (YEAR, SEMESTER, GRADE) in (SELECT ";
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
        $query .= "                                                                                                                          ) ";
        $query .= "                                                                                         ) ";
        $query .= "                                                                         )  AND ";
        $query .= "                                                         YEAR = '".$year."' ";
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
        $query .= "                                                                                             SCHREGNO IN (SELECT ";
        $query .= "                                                                                                             SCHREGNO ";
        $query .= "                                                                                                         FROM ";
        $query .= "                                                                                                             SCHREG_REGD_DAT ";
        $query .= "                                                                                                         WHERE ";
        $query .= "                                                                                                             (YEAR, SEMESTER, GRADE) in (SELECT ";
        $query .= "                                                                                                                                             YEAR, ";
        $query .= "                                                                                                                                             SEMESTER, ";
        $query .= "                                                                                                                                             GRADE ";
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
        $query .= "                                                                                                                                          ) ";
        $query .= "                                                                                                         ) ";
        $query .= "                                                                                         )  AND ";
        $query .= "                                                                         YEAR = '".$year."' ";
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
    //小分野、教材CD別個人得点取得　左下
    function getQuestSchregPoint($year, $knjid, $field, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     TEACHING_CD, ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO, ";
        $query .= "     SUM(JUDGE) as SCHREG_SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         TEACHING_CD, ";
        $query .= "         TAKE_CNT, ";
        $query .= "         JUDGE, ";
        $query .= "         SUBSTR(FIELDNO, 1, 4) as PART_FIELDNO, ";
        $query .= "         SUBSTR(FIELDNO, 1, 6) as QUESTION_FIELDNO ";
        $query .= "     FROM ";
        $query .= "         PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         (STUDENT_ID, TEACHING_CD, TAKE_CNT) in (SELECT ";
        $query .= "                                                     STUDENT_ID, ";
        $query .= "                                                     TEACHING_CD, ";
        if($field["TESTCNT"] != "1"){
            $query .= "                                                     MAX(TAKE_CNT) as TAKE_CNT ";
        }else{
            $query .= "                                                     MIN(TAKE_CNT) as TAKE_CNT ";
        }
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     STUDENT_ID = (SELECT ";
        $query .= "                                                                     LOWER(LOGINID) ";
        $query .= "                                                                   FROM ";
        $query .= "                                                                     PV_SCHREG_MST ";
        $query .= "                                                                   WHERE ";
        $query .= "                                                                     KNJID = '".$knjid."' ";
        $query .= "                                                                   ) AND ";
        $query .= "                                                     SITE_ID = '".$field["SITE"]."' AND ";
        $query .= "                                                     TEACHING_CD IN (".$teachingcd.") AND ";
        $query .= "                                                     YEAR = '".$year."'   ";
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
        $query .= " GROUP BY ";
        $query .= "     TEACHING_CD, ";
        $query .= "     PART_FIELDNO, ";
        $query .= "     QUESTION_FIELDNO ";
        
        return $query;
    }
    
}
?>
