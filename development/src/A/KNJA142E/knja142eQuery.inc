<?php

require_once('for_php7.php');


class knja142eQuery extends Query {

    //学期マスタ
    function getSemeMst()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";
        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //名称マスタ
    function getNameMstZ010()
    {
		$query  = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year,$semester,$schoolKind)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT DISTINCT ";
			$query .= "  T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT T1 ";
		    $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
		    $query .= "             ON T2.YEAR        = T1.YEAR ";
		    $query .= "            AND T2.GRADE       = T1.GRADE ";
		    $query .= "            AND T2.SCHOOL_KIND = '".$schoolKind."' ";
			$query .= "WHERE T1.YEAR='" .$year ."'";
		    $query .= "ORDER BY VALUE ";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT DISTINCT ";
			$query .= "  T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT T1 ";
		    $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
		    $query .= "             ON T2.YEAR        = T1.YEAR ";
		    $query .= "            AND T2.GRADE       = T1.GRADE ";
		    $query .= "            AND T2.SCHOOL_KIND = '".$schoolKind."' ";
			$query .= "WHERE T1.YEAR     = '" .$year ."' ";
			$query .= "  AND T1.SEMESTER = '".$semester ."' ";
	    	$query .= "  AND (T1.TR_CD1    = '" .STAFFCD ."' ";
	    	$query .= "       OR T1.TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "       OR T1.TR_CD3 = '" .STAFFCD ."') ";
	    	$query .= "ORDER BY VALUE ";
		}

        return $query;
    }

    //生徒取得
    function getSchno($model,$year,$semester)
    {
		$query  = "SELECT T1.GRADE||T1.HR_CLASS||T1.SCHREGNO AS VALUE, ";
		$query .= "       L1.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME AS LABEL ";
		$query .= "FROM   SCHREG_REGD_DAT T1 ";
		$query .= "       INNER JOIN SCHREG_BASE_MST T2 ";
		$query .= "               ON T2.SCHREGNO   = T1.SCHREGNO ";
		$query .= "       INNER JOIN SCHREG_REGD_GDAT T3 ";
		$query .= "               ON T3.YEAR        = T1.YEAR ";
		$query .= "              AND T3.GRADE       = T1.GRADE ";
		$query .= "              AND T3.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
		$query .= "       LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
		$query .= "            AND L1.SEMESTER = T1.SEMESTER ";
		$query .= "            AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ";
		$query .= "WHERE  T1.YEAR       = '".$year."' AND ";
		$query .= "       T1.SEMESTER   = '".$semester."' AND ";
		$query .= "       T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
		$query .= "ORDER BY ";
		$query .= "       T1.GRADE, ";
		$query .= "       T1.HR_CLASS, ";
		$query .= "       T1.ATTENDNO ";

        return $query;
    }

}
?>
