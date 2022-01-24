<?php

require_once('for_php7.php');


class knjm290Query extends Query {

    //年度
    function Getyear()
    {
        $query  = "SELECT ";
        $query .= "    YEAR ";
        $query .= "FROM ";
        $query .= "    REP_PRESENT_DAT ";
        $query .= "GROUP BY ";
        $query .= "    YEAR ";

        return $query;
    }
    //開始日付/終了日付取得：年度内チェック用
    function getCheckseme($model)
    {
        $query  = "SELECT ";
        $query .= "    SDATE,EDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->field["YEAR"]."' ";
        $query .= "    AND SEMESTER = '9' ";

        return $query;
    }

    //担当者
    function Getstaff($model)
    {
        $query  = "SELECT ";
        $query .= "    t1.STAFFCD, ";
        $query .= "    t2.STAFFNAME ";
        $query .= "FROM ";
        $query .= "    REP_PRESENT_DAT t1 LEFT JOIN STAFF_MST t2 ON t1.STAFFCD = t2.STAFFCD ";
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "    t1.STAFFCD IS NOT NULL  AND t1.STAFFCD <> '' ";
        $query .= "GROUP BY ";
        $query .= "    t1.STAFFCD, ";
        $query .= "    t2.STAFFNAME ";

        return $query;
    }

}
?>
