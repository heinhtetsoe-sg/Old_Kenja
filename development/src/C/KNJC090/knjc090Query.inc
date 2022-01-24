<?php

require_once('for_php7.php');

class knjc090Query extends Query {

    //学年取得
    function getSelectGrade($year)
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '" .$year ."' ";
        $query .= "order by grade";

        return $query;
    }
}
?>
