<?php

require_once('for_php7.php');

class knjc052Query extends Query {

    //学期リスト取得
    function getSemester($model)
    {
        $query  = "SELECT semester,semestername FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' and semester<>'9'";

        return $query;
    }
    //学期リスト取得
    function getsdateedate($gakki)
    {
        $query  = "SELECT sdate,edate FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' and semester ='" .$gakki. "' ";

        return $query;
    }

}
?>
