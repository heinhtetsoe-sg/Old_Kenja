<?php

require_once('for_php7.php');

class knjj196query extends Query {
    //出身学校一覧取得
    function getFinschoolList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.FINSCHOOLCD AS VALUE, ";
        $query .= "     S3.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT S1 ";
        $query .= "          ON T1.SCHREGNO     = S1.SCHREGNO ";
        $query .= "         AND S1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND S1.SEMESTER     = '".CTRL_SEMESTER."' ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
        $query .= "          ON S1.YEAR         = S2.YEAR ";
        $query .= "         AND S1.GRADE        = S2.GRADE ";
        $query .= "         AND S2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     INNER JOIN FINSCHOOL_MST S3 ";
        $query .= "          ON T1.FINSCHOOLCD = S3.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOLCD IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校毎の件数取得
    function getFinschoolCnt($model) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         S2.FINSCHOOLCD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT S1 ";
        $query .= "              ON T1.YEAR         = S1.YEAR ";
        $query .= "             AND T1.GRADE        = S1.GRADE ";
        $query .= "             AND S1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S2 ";
        $query .= "              ON T1.SCHREGNO     = S2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     ORDER BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     FINSCHOOLCD, ";
        $query .= "     COUNT(*) AS FINSCH_CNT ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     FINSCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     FINSCHOOLCD ";

        return $query;
    }

    //年組一覧・学年ごとのクラス件数取得
    function getHrclassList($model, $flg) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
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
        $query .= "             AND S2.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
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
            $query .= "     GRADE_NAME1, ";
            $query .= "     HR_NAME, ";
            $query .= "     COUNT(*) AS CLASS_CNT ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " GROUP BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     GRADE_NAME1, ";
            $query .= "     HR_NAME ";
            $query .= " ORDER BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS ";
        } else if ($flg == "cnt") {
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
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE ";
        }

        return $query;
    }
}
?>
