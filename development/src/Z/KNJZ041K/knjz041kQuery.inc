<?php
class knjz041kQuery extends Query {

	//中高判別
	function GetJorH(){
		$query  = " SELECT ";
		$query .= "     COUNT(*) ";
		$query .= " FROM ";
		$query .= "     SCHOOL_MST ";
		$query .= " WHERE ";
		$query .= "     YEAR = '".CTRL_YEAR."' AND ";
		$query .= "     SCHOOLNAME1 LIKE '%中学%' ";

		return $query;
	}

}
?>
