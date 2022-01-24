<?php

require_once('for_php7.php');

class knje377Query extends Query {
    //学期名取得
    function getSemesterName() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

/******リストtoリスト*******/
    //会社取得(右側に表示する生徒)
    function getCompany() {
        $query  = " WITH SENKOU_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         GRAD.YEAR, ";
        $query .= "         GRAD.SENKOU_NO ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT GRAD ";
        $query .= "     WHERE ";
        $query .= "             GRAD.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND GRAD.SENKOU_NO IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         GRAD.YEAR, ";
        $query .= "         GRAD.SENKOU_NO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SENKOU.SENKOU_NO AS VALUE, ";
        $query .= "     COMP.COMPANY_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SENKOU_DATA SENKOU ";
        $query .= "     LEFT JOIN JOB_OFFER_DAT OFFER ON SENKOU.YEAR      = OFFER.YEAR ";
        $query .= "                                  AND SENKOU.SENKOU_NO = OFFER.SENKOU_NO ";
        $query .= "     LEFT JOIN COMPANY_MST COMP ON OFFER.COMPANY_CD = COMP.COMPANY_CD ";
        $query .= " WHERE ";
        $query .= "     COMP.COMPANY_CD IS NOT NULL ";

        return $query;
    }
}
?>
