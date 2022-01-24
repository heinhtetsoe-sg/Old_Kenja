<?php

require_once('for_php7.php');


class knjp500Query extends Query {

	//��������p
	function GetJorH(){
		$query  = " SELECT ";
		$query .= "     COUNT(*) ";
		$query .= " FROM ";
		$query .= "     SCHOOL_MST ";
		$query .= " WHERE ";
		$query .= "     YEAR = '".CTRL_YEAR."' ";
		$query .= "     AND SCHOOLNAME1 LIKE '%�ߋE%���w%' ";

		return $query;
	}

}
?>
