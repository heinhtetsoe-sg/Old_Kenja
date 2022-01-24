<?php

require_once('for_php7.php');


class knje372Query extends Query {

    //調査名取得
    function getQuestionnaire()
    {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE, ";
        $query .= "     T1.QUESTIONNAIRECD || ' ' || T2.QUESTIONNAIRENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1 ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST T2 ON T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //進学
    function getPrintSingaku($model) {
        //変数
        $year       = CTRL_YEAR;
        $semester   = CTRL_SEMESTER;
        $yearGrd1   = CTRL_YEAR - 1;
        $yearGrd2   = CTRL_YEAR - 2;
        //SQL
        $query  = "";
        $query .= " WITH T_SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T2.SEX ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T4 ";
        $query .= "                  ON T4.YEAR = T1.YEAR ";
        $query .= "                 AND T4.GRADE = T1.GRADE ";
        $query .= "                 AND T4.SCHOOL_KIND = 'H' ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "                  ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '{$year}' ";
        $query .= "         AND T1.SEMESTER = '{$semester}' ";
        //既卒者（1浪，2浪）
        if ($model->field["KISOTU_DIV"] == "1") {
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         '99' AS GRADE, ";
            $query .= "         '991' AS HR_CLASS, ";
            $query .= "         SEX ";
            $query .= "     FROM ";
            $query .= "         GRD_BASE_MST ";
            $query .= "     WHERE ";
            $query .= "         Fiscalyear(GRD_DATE) = '{$yearGrd1}' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         '99' AS GRADE, ";
            $query .= "         '992' AS HR_CLASS, ";
            $query .= "         SEX ";
            $query .= "     FROM ";
            $query .= "         GRD_BASE_MST ";
            $query .= "     WHERE ";
            $query .= "         Fiscalyear(GRD_DATE) <= '{$yearGrd2}' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         '99' AS GRADE, ";
            $query .= "         '991' AS HR_CLASS, ";
            $query .= "         SEX ";
            $query .= "     FROM ";
            $query .= "         SCHREG_BASE_MST ";
            $query .= "     WHERE ";
            $query .= "         Fiscalyear(GRD_DATE) = '{$yearGrd1}' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         '99' AS GRADE, ";
            $query .= "         '992' AS HR_CLASS, ";
            $query .= "         SEX ";
            $query .= "     FROM ";
            $query .= "         SCHREG_BASE_MST ";
            $query .= "     WHERE ";
            $query .= "         Fiscalyear(GRD_DATE) <= '{$yearGrd2}' ";
        }
        $query .= "     ) ";
        $query .= " , T_CNT_CLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         VALUE(L1.FACULTYCD,'999') AS FACULTYCD, ";
        $query .= "         VALUE(L1.DEPARTMENTCD,'999') AS DEPARTMENTCD, ";
        //受験方式
        if ($model->field["JUKEN_DIV"] == "1") {
            $query .= "         VALUE(L1.HOWTOEXAM,'99') AS HOWTOEXAM, ";
        }
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SEX, ";
        //延べ人数
        if ($model->field["CNT_NOBE"] == "1") {
            $query .= "         COUNT(L1.SCHREGNO) AS CNT_CLASS ";
        } else {
            $query .= "         COUNT(distinct L1.SCHREGNO) AS CNT_CLASS ";
        }
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT L1  ";
        $query .= "         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             L1.YEAR = '{$year}' ";
        $query .= "         AND L1.SENKOU_KIND = '0' "; //進学
        if ($model->field["OUT_DIV"] == "1") {
            $query .= "     AND L1.DECISION = '1' "; //合格
        } else {
            $query .= "     AND L1.PLANSTAT = '1' "; //最終進路
        }
        $query .= "     GROUP BY ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         VALUE(L1.FACULTYCD,'999'), ";
        $query .= "         VALUE(L1.DEPARTMENTCD,'999'), ";
        //受験方式
        if ($model->field["JUKEN_DIV"] == "1") {
            $query .= "         VALUE(L1.HOWTOEXAM,'99'), ";
        }
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SEX ";
        $query .= "     ) ";
        $query .= " , T_CNT_SCHOOL AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_SCHOOL ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     GROUP BY ";
        $query .= "         L1.STAT_CD ";
        $query .= "     ) ";
        $query .= " , T_CNT_SCHOOL_BOY AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_SCHOOL_BOY ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     WHERE ";
        $query .= "         L1.SEX = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         L1.STAT_CD ";
        $query .= "     ) ";
        $query .= " , T_CNT_SCHOOL_GIRL AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_SCHOOL_GIRL ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     WHERE ";
        $query .= "         L1.SEX = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         L1.STAT_CD ";
        $query .= "     ) ";
        $query .= " , T_CNT_FACULTY AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         L1.FACULTYCD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_FACULTY ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     GROUP BY ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         L1.FACULTYCD ";
        $query .= "     ) ";
        $query .= " , T_CNT_DEPARTMENT AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         L1.FACULTYCD, ";
        $query .= "         L1.DEPARTMENTCD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_DEPARTMENT ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     GROUP BY ";
        $query .= "         L1.STAT_CD, ";
        $query .= "         L1.FACULTYCD, ";
        $query .= "         L1.DEPARTMENTCD ";
        $query .= "     ) ";
        //受験方式
        if ($model->field["JUKEN_DIV"] == "1") {
            $query .= " , T_CNT_HOWTOEXAM AS ( ";
            $query .= "     SELECT ";
            $query .= "         L1.STAT_CD, ";
            $query .= "         L1.FACULTYCD, ";
            $query .= "         L1.DEPARTMENTCD, ";
            $query .= "         L1.HOWTOEXAM, ";
            $query .= "         SUM(L1.CNT_CLASS) AS CNT_HOWTOEXAM ";
            $query .= "     FROM ";
            $query .= "         T_CNT_CLASS L1 ";
            $query .= "     GROUP BY ";
            $query .= "         L1.STAT_CD, ";
            $query .= "         L1.FACULTYCD, ";
            $query .= "         L1.DEPARTMENTCD, ";
            $query .= "         L1.HOWTOEXAM ";
            $query .= "     ) ";
        }

        $query .= " SELECT ";
        $query .= "     E012.NAME1 AS SCHOOL_GROUP_NAME, ";
        $query .= "     L1.STAT_CD, ";
        $query .= "     L2.SCHOOL_NAME, ";
        $query .= "     L1.FACULTYCD, ";
        $query .= "     L3.FACULTYNAME, ";
        $query .= "     L1.DEPARTMENTCD, ";
        $query .= "     L4.DEPARTMENTNAME, ";
        //受験方式
        if ($model->field["JUKEN_DIV"] == "1") {
            $query .= "     L1.HOWTOEXAM, ";
            $query .= "     E002.NAME1 AS HOWTOEXAM_NAME, ";
            $query .= "     C4.CNT_HOWTOEXAM, ";
        }
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     L1.CNT_CLASS, ";
        $query .= "     C1.CNT_SCHOOL, ";
        $query .= "     CC1.CNT_SCHOOL_BOY, ";
        $query .= "     CC2.CNT_SCHOOL_GIRL, ";
        $query .= "     C2.CNT_FACULTY, ";
        $query .= "     C3.CNT_DEPARTMENT ";
        $query .= " FROM ";
        $query .= "     T_CNT_CLASS L1  ";
        $query .= "     LEFT JOIN T_CNT_SCHOOL C1 ON C1.STAT_CD = L1.STAT_CD ";
        $query .= "     LEFT JOIN T_CNT_SCHOOL_BOY CC1 ON CC1.STAT_CD = L1.STAT_CD ";
        $query .= "     LEFT JOIN T_CNT_SCHOOL_GIRL CC2 ON CC2.STAT_CD = L1.STAT_CD ";
        $query .= "     LEFT JOIN T_CNT_FACULTY C2 ON C2.STAT_CD = L1.STAT_CD ";
        $query .= "                               AND C2.FACULTYCD = L1.FACULTYCD ";
        $query .= "     LEFT JOIN T_CNT_DEPARTMENT C3 ON C3.STAT_CD = L1.STAT_CD ";
        $query .= "                                  AND C3.FACULTYCD = L1.FACULTYCD ";
        $query .= "                                  AND C3.DEPARTMENTCD = L1.DEPARTMENTCD ";
        //受験方式
        if ($model->field["JUKEN_DIV"] == "1") {
            $query .= "     LEFT JOIN T_CNT_HOWTOEXAM C4 ON C4.STAT_CD = L1.STAT_CD ";
            $query .= "                                 AND C4.FACULTYCD = L1.FACULTYCD ";
            $query .= "                                 AND C4.DEPARTMENTCD = L1.DEPARTMENTCD ";
            $query .= "                                 AND C4.HOWTOEXAM = L1.HOWTOEXAM ";
            $query .= "     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' ";
            $query .= "                            AND E002.NAMECD2 = L1.HOWTOEXAM ";
        }
        $query .= "     LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.STAT_CD ";
        $query .= "     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' ";
        $query .= "                            AND E012.NAMECD2 = L2.SCHOOL_GROUP ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L3 ON L3.SCHOOL_CD = L1.STAT_CD ";
        $query .= "                                     AND L3.FACULTYCD = L1.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L4 ON L4.SCHOOL_CD = L1.STAT_CD ";
        $query .= "                                        AND L4.FACULTYCD = L1.FACULTYCD ";
        $query .= "                                        AND L4.DEPARTMENTCD = L1.DEPARTMENTCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T1 ON T1.YEAR = '{$year}' ";
        $query .= "                                  AND T1.SEMESTER = '{$semester}' ";
        $query .= "                                  AND T1.GRADE = L1.GRADE ";
        $query .= "                                  AND T1.HR_CLASS = L1.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     L1.STAT_CD, ";
        $query .= "     L1.FACULTYCD, ";
        $query .= "     L1.DEPARTMENTCD, ";
        //受験方式
        if ($model->field["JUKEN_DIV"] == "1") {
            $query .= "     L1.HOWTOEXAM, ";
        }
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS ";
        return $query;
    }

    //受験方式
    function getNameMst() {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS HOWTOEXAM, ";
        $query .= "     NAME1 AS HOWTOEXAM_NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST  ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E002'  ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //進路調査
    function getShinroCsv($model) {
        //変数
        $year       = CTRL_YEAR;
        $semester   = CTRL_SEMESTER;
        $yearGrd1   = CTRL_YEAR - 1;
        $yearGrd2   = CTRL_YEAR - 2;

        $school_cd      = "SCHOOL_CD".$model->field["CHOICE"];
        $facultycd      = "FACULTYCD".$model->field["CHOICE"];
        $departmentcd   = "DEPARTMENTCD".$model->field["CHOICE"];
        $school_group   = "SCHOOL_GROUP".$model->field["CHOICE"];

        $query  = "";
        $query .= " WITH T_SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T2.SEX ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T4 ";
        $query .= "                  ON T4.YEAR = T1.YEAR ";
        $query .= "                 AND T4.GRADE = T1.GRADE ";
        $query .= "                 AND T4.SCHOOL_KIND = 'H' ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "                  ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '{$year}' ";
        $query .= "         AND T1.SEMESTER = '{$semester}' ";
        $query .= "     ) ";
        $query .= " , MAX_ENTRYDATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         MAX(L1.ENTRYDATE) AS ENTRYDATE, ";
        $query .= "         L1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         COURSE_HOPE_DAT L1  ";
        $query .= "     WHERE ";
        $query .= "             L1.YEAR = '{$year}' ";
        $query .= "         AND L1.COURSE_KIND = '1' ";
        $query .= "         AND L1.QUESTIONNAIRECD = '{$model->field["QUESTIONNAIRECD"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         L1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " , MAX_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         MAX(L1.SEQ) AS SEQ, ";
        $query .= "         T1.ENTRYDATE, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         MAX_ENTRYDATE T1  ";
        $query .= "         LEFT JOIN COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ";
        $query .= "                                     AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.ENTRYDATE, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " , T_CNT_CLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.{$school_cd} AS SCHOOL_CD, ";
        $query .= "         VALUE(L1.{$facultycd},'999') AS FACULTYCD, ";
        $query .= "         VALUE(L1.{$departmentcd},'999') AS DEPARTMENTCD, ";
        $query .= "         L1.{$school_group} AS SCHOOL_GROUP, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SEX, ";
        $query .= "         COUNT(DISTINCT L1.SCHREGNO) AS CNT_CLASS ";
        $query .= "     FROM ";
        $query .= "         COURSE_HOPE_DAT L1 ";
        $query .= "         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ";
        $query .= "                              AND T2.SEQ = L1.SEQ ";
        $query .= "                              AND T2.SCHREGNO = L1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         L1.{$school_cd} IS NOT NULL ";
        if ($model->field["SCHOOL_GROUP"] == "1") {
            $query .= "         AND L1.{$school_group} = '06' ";
        } else if ($model->field["SCHOOL_GROUP"] == "2") {
            $query .= "         AND L1.{$school_group} <> '06' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         L1.{$school_cd}, ";
        $query .= "         VALUE(L1.{$facultycd},'999'), ";
        $query .= "         VALUE(L1.{$departmentcd},'999'), ";
        $query .= "         L1.{$school_group}, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SEX ";
        $query .= "     ) ";
        $query .= " , T_CNT_SCHOOL AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.SCHOOL_CD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_SCHOOL ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     GROUP BY ";
        $query .= "         L1.SCHOOL_CD ";
        $query .= "     ) ";
        $query .= " , T_CNT_SCHOOL_BOY AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.SCHOOL_CD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_SCHOOL_BOY ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     WHERE ";
        $query .= "         L1.SEX = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         L1.SCHOOL_CD ";
        $query .= "     ) ";
        $query .= " , T_CNT_SCHOOL_GIRL AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.SCHOOL_CD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_SCHOOL_GIRL ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     WHERE ";
        $query .= "         L1.SEX = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         L1.SCHOOL_CD ";
        $query .= "     ) ";
        $query .= " , T_CNT_FACULTY AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.SCHOOL_CD, ";
        $query .= "         L1.FACULTYCD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_FACULTY ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     GROUP BY ";
        $query .= "         L1.SCHOOL_CD, ";
        $query .= "         L1.FACULTYCD ";
        $query .= "     ) ";
        $query .= " , T_CNT_DEPARTMENT AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.SCHOOL_CD, ";
        $query .= "         L1.FACULTYCD, ";
        $query .= "         L1.DEPARTMENTCD, ";
        $query .= "         SUM(L1.CNT_CLASS) AS CNT_DEPARTMENT ";
        $query .= "     FROM ";
        $query .= "         T_CNT_CLASS L1 ";
        $query .= "     GROUP BY ";
        $query .= "         L1.SCHOOL_CD, ";
        $query .= "         L1.FACULTYCD, ";
        $query .= "         L1.DEPARTMENTCD ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     E012.NAME1 AS SCHOOL_GROUP_NAME, ";
        $query .= "     L1.SCHOOL_CD AS STAT_CD, ";
        $query .= "     L2.SCHOOL_NAME, ";
        $query .= "     L1.FACULTYCD, ";
        $query .= "     L3.FACULTYNAME, ";
        $query .= "     L1.DEPARTMENTCD, ";
        $query .= "     L4.DEPARTMENTNAME, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     L1.CNT_CLASS, ";
        $query .= "     C1.CNT_SCHOOL, ";
        $query .= "     CC1.CNT_SCHOOL_BOY, ";
        $query .= "     CC2.CNT_SCHOOL_GIRL, ";
        $query .= "     C2.CNT_FACULTY, ";
        $query .= "     C3.CNT_DEPARTMENT ";
        $query .= " FROM ";
        $query .= "     T_CNT_CLASS L1  ";
        $query .= "     LEFT JOIN T_CNT_SCHOOL C1 ON C1.SCHOOL_CD = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN T_CNT_SCHOOL_BOY CC1 ON CC1.SCHOOL_CD = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN T_CNT_SCHOOL_GIRL CC2 ON CC2.SCHOOL_CD = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN T_CNT_FACULTY C2 ON C2.SCHOOL_CD = L1.SCHOOL_CD ";
        $query .= "                               AND C2.FACULTYCD = L1.FACULTYCD ";
        $query .= "     LEFT JOIN T_CNT_DEPARTMENT C3 ON C3.SCHOOL_CD = L1.SCHOOL_CD ";
        $query .= "                                  AND C3.FACULTYCD = L1.FACULTYCD ";
        $query .= "                                  AND C3.DEPARTMENTCD = L1.DEPARTMENTCD ";
        $query .= "     LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' ";
        $query .= "                            AND E012.NAMECD2 = L1.SCHOOL_GROUP ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L3 ON L3.SCHOOL_CD = L1.SCHOOL_CD ";
        $query .= "                                     AND L3.FACULTYCD = L1.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L4 ON L4.SCHOOL_CD = L1.SCHOOL_CD ";
        $query .= "                                        AND L4.FACULTYCD = L1.FACULTYCD ";
        $query .= "                                        AND L4.DEPARTMENTCD = L1.DEPARTMENTCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T1 ON T1.YEAR = '{$year}' ";
        $query .= "                                  AND T1.SEMESTER = '{$semester}' ";
        $query .= "                                  AND T1.GRADE = L1.GRADE ";
        $query .= "                                  AND T1.HR_CLASS = L1.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     L1.SCHOOL_CD, ";
        $query .= "     L1.FACULTYCD, ";
        $query .= "     L1.DEPARTMENTCD, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS ";

        return $query;
    }

}
?>