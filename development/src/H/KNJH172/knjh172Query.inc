<?php

require_once('for_php7.php');

class knjh172Query extends Query {

    //年組を取得
    function getGradeHrclass() {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T2 ON ";
        $query .= "             T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER AND  ";
        $query .= "             T1.GRADE = T2.GRADE AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE DESC, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($grade, $hr_class) {

        $query  = " WITH ADDR_INFO AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ZIPCD, ";
        $query .= "         T1.ADDR1, ";
        $query .= "         T1.ADDR2 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.ISSUEDATE IN(SELECT ";
        $query .= "                             MAX(T2.ISSUEDATE) AS ISSUEDATE ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_ADDRESS_DAT T2 ";
        $query .= "                         WHERE ";
        $query .= "                             T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                             '".CTRL_DATE."' BETWEEN T2.ISSUEDATE AND ";
        $query .= "                             CASE WHEN T2.EXPIREDATE IS NULL THEN '9999-12-31' ELSE T2.EXPIREDATE END ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     '(' || T2.GO_HOME_GROUP_NO || ')' AS GO_HOME_GROUP_NO, ";
        $query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "     T3.NAME, ";
        $query .= "     T4.GUARD_NAME, ";
        $query .= "     T4.GUARD_ZIPCD, ";
        $query .= "     T4.GUARD_ADDR1, ";
        $query .= "     T4.GUARD_ADDR2, ";
        $query .= "     T4.GUARD_TELNO, ";
        $query .= "     T3.NAME AS NAME2, ";
        $query .= "     T5.ZIPCD, ";
        $query .= "     T5.ADDR1, ";
        $query .= "     T5.ADDR2, ";
        $query .= "     T3.EMERGENCYTELNO, ";
        $query .= "     CASE WHEN T3.GRD_DIV IS NOT NULL AND T3.GRD_DIV <> '4' AND VALUE(GRD_DATE, '9999-12-31') < '".CTRL_DATE."' THEN '1' END AS IS_GRD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_ENVIR_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO AND T2.GO_HOME_GROUP_NO <> '00' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT T4 ON T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "     LEFT JOIN ADDR_INFO T5 ON T1.SCHREGNO = T5.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        $query .= "     T1.HR_CLASS = '".$hr_class."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
