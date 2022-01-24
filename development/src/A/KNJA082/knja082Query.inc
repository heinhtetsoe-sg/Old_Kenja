<?php

require_once('for_php7.php');

class knja082Query extends Query {

    //学年取得
    function GetGrade($model)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS CD, ";
        $query .= "     T2.GRADE_NAME1 AS NAME ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->nextyear."' AND ";
        $query .= "     T1.SEMESTER = '1' AND ";
        $query .= "     T1.GRADE    = T2.GRADE ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE ";

        return $query;
    }

	//中高判定用
	function GetJorH(){
		$query  = " SELECT ";
		$query .= "     COUNT(*) ";
		$query .= " FROM ";
		$query .= "     SCHOOL_MST ";
		$query .= " WHERE ";
		$query .= "     YEAR = '".CTRL_YEAR."' ";
		$query .= "     AND SCHOOLNAME1 LIKE '%近畿%中学%' ";

		return $query;
	}

}
?>
