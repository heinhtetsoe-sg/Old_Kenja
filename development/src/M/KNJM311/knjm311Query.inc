<?php

require_once('for_php7.php');


class knjm311Query extends Query {

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

}
?>
