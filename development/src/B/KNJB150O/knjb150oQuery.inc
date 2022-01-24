<?php

require_once('for_php7.php');


class knjb150oQuery extends Query {

    //年組取得
    function getHrClass($year,$semester)
    {
		$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
		$query .= "FROM SCHREG_REGD_HDAT ";
		$query .= "WHERE YEAR='" .$year ."'";
		$query .= "AND SEMESTER='".$semester ."'";

        return $query;
    }


    //生徒取得
    function getSchregno($year,$semester,$hr_class)
    {
		$query  = "SELECT ";
		$query .= "		T2.SCHREGNO AS SCHREGNO, ";
		$query .= "		T2.SCHREGNO || '　' || T2.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS NAME ";
		$query .= "FROM ";
		$query .= "		SCHREG_BASE_MST T1, ";
		$query .= "		SCHREG_REGD_DAT T2 ";
		$query .= "WHERE ";
		$query .= "			T2.YEAR		= '" .$year ."' ";
		$query .= "		AND T2.SEMESTER	= '" .$semester ."' ";
		$query .= "		AND T2.GRADE || T2.HR_CLASS = '" .$hr_class ."' ";
		$query .= "		AND T1.SCHREGNO = T2.SCHREGNO ";
		$query .= "ORDER BY ";
		$query .= "		T2.ATTENDNO";

        return $query;
    }



}
?>
