<?php

require_once('for_php7.php');

class knje366Query extends Query {
    //CSV出力データ
    function getCsvQuery() {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         L1.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT L1 ";
        $query .= "                     ON T1.YEAR      = L1.YEAR ";
        $query .= "                    AND T1.GRADE     = L1.GRADE ";
        $query .= "                    AND T1.HR_CLASS  = L1.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= " ), SCH_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.*, ";
        $query .= "         L1.ATTENDNO, ";
        $query .= "         L1.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         LEFT JOIN SCH_INFO L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SENKOU_KIND  = '0' AND ";
        $query .= "         T1.DECISION     = '1' AND ";
        $query .= "         T1.SCHOOL_GROUP IN ('01', '02', '03', '04', '05', '08') ";
        $query .= " ), PASS_SCHOOL_CNT1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD ";
        $query .= " ), PASS_SCHOOL_CNT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         ATTENDNO IS NULL ";
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD ";
        $query .= " ), PASS_FACULTY_CNT1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD ";
        $query .= " ), PASS_FACULTY_CNT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         ATTENDNO IS NULL ";
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD ";
        $query .= " ), GO_FACULTY_CNT1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         PLANSTAT = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD ";
        $query .= " ), GO_FACULTY_CNT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         PLANSTAT = '1' AND ";
        $query .= "         ATTENDNO IS NULL ";
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     CASE WHEN (T1.SCHOOL_GROUP = '01' OR T1.SCHOOL_GROUP = '02' OR T1.SCHOOL_GROUP = '04' OR T1.SCHOOL_GROUP = '08') THEN '1' ";
        $query .= "          WHEN (T1.SCHOOL_GROUP = '03' OR T1.SCHOOL_GROUP = '05') THEN '2' ";
        $query .= "          ELSE '9' END AS SCHOOL_GROUP, ";
        $query .= "     CASE WHEN (T1.SCHOOL_GROUP = '01' OR T1.SCHOOL_GROUP = '02' OR T1.SCHOOL_GROUP = '04' OR T1.SCHOOL_GROUP = '08') THEN '国公立大学' ";
        $query .= "          WHEN (T1.SCHOOL_GROUP = '03' OR T1.SCHOOL_GROUP = '05') THEN '私立大学' ";
        $query .= "          ELSE '' END AS SCHOOL_GROUP_NAME, ";
        $query .= "     T1.STAT_CD, ";
        $query .= "     L1.SCHOOL_NAME_SHOW1 AS SCHOOL_NAME, ";
        $query .= "     VALUE(RTRIM(CHAR(C1.CNT)),'0') AS PASS_SCHOOL_CNT1, ";
        $query .= "     VALUE(RTRIM(CHAR(C2.CNT)),'0') AS PASS_SCHOOL_CNT2, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     L2.FACULTYNAME_SHOW1 AS FACULTYNAME, ";
        $query .= "     VALUE(RTRIM(CHAR(C3.CNT)),'0') AS PASS_FACULTY_CNT1, ";
        $query .= "     VALUE(RTRIM(CHAR(C4.CNT)),'0') AS PASS_FACULTY_CNT2, ";
        $query .= "     VALUE(RTRIM(CHAR(C5.CNT)),'0') AS GO_FACULTY_CNT1, ";
        $query .= "     VALUE(RTRIM(CHAR(C6.CNT)),'0') AS GO_FACULTY_CNT2, ";
        $query .= "     T1.DEPARTMENTCD, ";
        $query .= "     L3.DEPARTMENTNAME_SHOW1 AS DEPARTMENTNAME, ";
        $query .= "     CASE WHEN T1.PLANSTAT = '1' THEN '進学' ELSE '*' END AS PLANSTAT, ";
        $query .= "     CASE WHEN T1.ATTENDNO IS NULL THEN '卒' ELSE '' END AS REGD_DIV, ";
        $query .= "     S1.NAME, ";
        $query .= "     Z002.NAME1 as SEX, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCH_LIST T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ";
        $query .= "             ON T1.SCHREGNO      = S1.SCHREGNO ";
        $query .= "     LEFT JOIN COLLEGE_MST L1 ";
        $query .= "             ON T1.STAT_CD       = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L2 ";
        $query .= "             ON T1.STAT_CD       = L2.SCHOOL_CD ";
        $query .= "            AND T1.FACULTYCD     = L2.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ";
        $query .= "             ON T1.STAT_CD       = L3.SCHOOL_CD ";
        $query .= "            AND T1.FACULTYCD     = L3.FACULTYCD ";
        $query .= "            AND T1.DEPARTMENTCD  = L3.DEPARTMENTCD ";
        $query .= "     LEFT JOIN PASS_SCHOOL_CNT1 C1 ";
        $query .= "             ON T1.STAT_CD       = C1.STAT_CD ";
        $query .= "     LEFT JOIN PASS_SCHOOL_CNT2 C2 ";
        $query .= "             ON T1.STAT_CD       = C2.STAT_CD ";
        $query .= "     LEFT JOIN PASS_FACULTY_CNT1 C3 ";
        $query .= "             ON T1.STAT_CD       = C3.STAT_CD ";
        $query .= "            AND T1.FACULTYCD     = C3.FACULTYCD ";
        $query .= "     LEFT JOIN PASS_FACULTY_CNT2 C4 ";
        $query .= "             ON T1.STAT_CD       = C4.STAT_CD ";
        $query .= "            AND T1.FACULTYCD     = C4.FACULTYCD ";
        $query .= "     LEFT JOIN GO_FACULTY_CNT1 C5 ";
        $query .= "             ON T1.STAT_CD       = C5.STAT_CD ";
        $query .= "            AND T1.FACULTYCD     = C5.FACULTYCD ";
        $query .= "     LEFT JOIN GO_FACULTY_CNT2 C6 ";
        $query .= "             ON T1.STAT_CD       = C6.STAT_CD ";
        $query .= "            AND T1.FACULTYCD     = C6.FACULTYCD ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "             ON Z002.YEAR        = '".CTRL_YEAR."' ";
        $query .= "            AND Z002.NAMECD1     = 'Z002' ";
        $query .= "            AND Z002.NAMECD2     = S1.SEX ";
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_GROUP, ";
        $query .= "     T1.STAT_CD, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     T1.DEPARTMENTCD, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
?>
