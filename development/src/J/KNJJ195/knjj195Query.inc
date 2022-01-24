<?php

require_once('for_php7.php');

class knjj195query extends Query {
    //支部一覧取得
    function getBranchMst() {
        $query  = " SELECT ";
        $query .= "     BRANCHCD AS VALUE, ";
        $query .= "     BRANCHNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BRANCH_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //支部毎の件数取得
    function getBranchCnt() {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHOOL_KIND, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT S1 ";
        $query .= "              ON T1.YEAR     = S1.YEAR ";
        $query .= "             AND T1.GRADE    = S1.GRADE ";
        $query .= "             AND S1.SCHOOL_KIND IN ('H','J') ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT S2 ";
        $query .= "              ON T1.YEAR     = S2.YEAR ";
        $query .= "             AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "             AND T1.GRADE    = S2.GRADE ";
        $query .= "             AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S3 ";
        $query .= "              ON T1.SCHREGNO = S3.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     S1.BRANCHCD, ";
        $query .= "     COUNT(T1.SCHREGNO) AS BRANCH_CNT ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     INNER JOIN SCHREG_BRANCH_DAT S1 ";
        $query .= "          ON S1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "         AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND S1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SCHREGNO     = S1.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     S1.BRANCHCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     S1.BRANCHCD ";

        return $query;
    }

    //年組一覧・学年ごとの件数・校種ごとの件数取得
    function getHrclassList($model, $flg) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         S2.SCHOOL_KIND, ";
        $query .= "         S2.GRADE_NAME1, ";
        $query .= "         S3.HR_NAME, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S1 ";
        $query .= "              ON T1.SCHREGNO     = S1.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT S2 ";
        $query .= "              ON T1.YEAR         = S2.YEAR ";
        $query .= "             AND T1.GRADE        = S2.GRADE ";
        $query .= "             AND S2.SCHOOL_KIND IN ('H','J') ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT S3 ";
        $query .= "              ON T1.YEAR         = S3.YEAR ";
        $query .= "             AND T1.SEMESTER     = S3.SEMESTER ";
        $query .= "             AND T1.GRADE        = S3.GRADE ";
        $query .= "             AND T1.HR_CLASS     = S3.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     ORDER BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS ";
        $query .= " ) ";

        if ($flg == "list") {
            $query .= " SELECT ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     GRADE_NAME1, ";
            $query .= "     HR_NAME, ";
            $query .= "     COUNT(*) AS CLASS_CNT ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " GROUP BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     GRADE_NAME1, ";
            $query .= "     HR_NAME ";
            $query .= " ORDER BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS ";
        } else if ($flg == "cntG") {
            $query .= " SELECT ";
            $query .= "     T1.GRADE, ";
            $query .= "     COUNT(*) AS HR_CLASS_CNT ";
            $query .= " FROM ";
            $query .= "     (SELECT DISTINCT ";
            $query .= "         GRADE, ";
            $query .= "         HR_CLASS ";
            $query .= "     FROM ";
            $query .= "         MAIN ) T1 ";
            $query .= " GROUP BY ";
            $query .= "     T1.GRADE ";
        } else if ($flg == "cntS") {
            $query .= " SELECT ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     COUNT(*) AS GHR_CNT ";
            $query .= " FROM ";
            $query .= "     (SELECT DISTINCT ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         GRADE, ";
            $query .= "         HR_CLASS ";
            $query .= "     FROM ";
            $query .= "         MAIN ) T1 ";
            $query .= " GROUP BY ";
            $query .= "     SCHOOL_KIND ";
        }

        return $query;
    }
}
?>
