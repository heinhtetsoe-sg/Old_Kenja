<?php

require_once('for_php7.php');


class knjc170Query extends Query {

    //学年取得
    function GetDate($model)
    {
        $query  = "SELECT SDATE,EDATE ";
        $query .= "FROM SEMESTER_MST ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		$query .= "AND SEMESTER='9' ";

        return $query;
    }

    //学期リスト取得 NO002
    function getSemester($model)
    {
        $query  = "SELECT semester,semestername FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' and semester<>'9'";

        return $query;
    }

}
?>
