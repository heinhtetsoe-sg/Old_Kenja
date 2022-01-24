<?php

require_once('for_php7.php');


class knjd322Query extends Query {


    //学年取得
    function getSelectGrade()
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
        $query .= "order by grade";

        return $query;
    }


}
?>
