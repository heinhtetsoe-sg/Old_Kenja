<?php

require_once('for_php7.php');


class knjp392Query extends Query {
	//NO001
	function getGrant(){
		$query  = "SELECT DISTINCT ";
		$query .= "    GRANTCD,NAME1 ";
		$query .= "FROM ";
		$query .= "    SCHREG_GRANT_DAT ";
		$query .= "    LEFT JOIN NAME_MST ON NAMECD1 = 'G212' ";
		$query .= "    AND NAMECD2 = GRANTCD ";

		return $query;
	}
}
?>
