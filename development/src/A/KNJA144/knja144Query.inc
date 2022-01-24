<?php

require_once('for_php7.php');


class knja144Query extends Query {
    //学期マスタ
    function getSemeMst($year, $semester, $grade) {
		$query  = " SELECT ";
		$query .= "     * ";
		$query .= " FROM ";
		$query .= "     V_SEMESTER_GRADE_MST ";
		$query .= " WHERE ";
		$query .= "     YEAR        = '".$year."' AND ";
		$query .= "     SEMESTER    = '".$semester."' AND ";
		$query .= "     GRADE       = '".$grade."' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."'";
			$query .= "AND SEMESTER='".$semester ."'";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."' ";
			$query .= "AND SEMESTER='".$semester ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }

    //生徒取得
    function getSchno($model,$year,$semester)
    {
		$query  = " SELECT ";
		$query .= "     T1.SCHREGNO AS VALUE, ";
		$query .= "     L1.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME AS LABEL ";
		$query .= " FROM ";
		$query .= "     SCHREG_REGD_DAT T1 ";
		$query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
		$query .= "          AND L1.SEMESTER = T1.SEMESTER ";
		$query .= "          AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS, ";
		$query .= "     SCHREG_BASE_MST T2 ";
		$query .= " WHERE ";
		$query .= "     T1.YEAR       = '".$year."' AND ";
		$query .= "     T1.SEMESTER   = '".$semester."' AND ";
		$query .= "     T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
		$query .= "     T2.SCHREGNO   = T1.SCHREGNO ";
		$query .= " ORDER BY ";
		$query .= "     T1.ATTENDNO ";

        return $query;
    }

    //名称マスタ---NO001
    function getNameMst($year)
    {
		$query  = " SELECT ";
		$query .= "     NAMECD2 || '　' || NAME1 AS LABEL, ";
		$query .= "     NAMECD2 AS VALUE ";
		$query .= " FROM ";
		$query .= "     V_NAME_MST ";
		$query .= " WHERE ";
		$query .= "     YEAR='".$year."' ";
		$query .= "     AND NAMECD1='A019' ";
		$query .= " ORDER BY ";
		$query .= "     NAMECD2 ";

        return $query;
    }

}
?>
