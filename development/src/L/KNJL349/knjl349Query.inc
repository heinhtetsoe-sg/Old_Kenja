<?php

require_once('for_php7.php');

class knjl349Query extends Query {

	function GetJorH(){
		$query  = " SELECT ";
		$query .= "     COUNT(*) ";
		$query .= " FROM ";
		$query .= "     SCHOOL_MST ";
		$query .= " WHERE ";
		$query .= "     YEAR = '".CTRL_YEAR."' AND ";
		$query .= "     SCHOOLNAME1 LIKE '%近畿%中学%' ";

		return $query;
	}
}
?>
