<?php

require_once('for_php7.php');


class knjd320Query extends Query {


    //学年取得
    function getSelectGrade()
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
/*
	//	今学期が３学期の場合、１・２年のみ表示。
		if (CTRL_SEMESTER=="3")
       		$query .= "AND GRADE in ('01','02') ";
	//	今学期が２学期の場合、３年のみ表示。
		if (CTRL_SEMESTER=="2")
       		$query .= "AND GRADE in ('03') ";
	//	今学期が１学期の場合、表示しない。
		if (CTRL_SEMESTER=="1")
       		$query .= "AND GRADE not in ('01','02','03') ";
*/
        $query .= "order by grade";

        return $query;
    }


}
?>
