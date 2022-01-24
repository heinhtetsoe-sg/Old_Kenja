<?php

require_once('for_php7.php');


class knjm021Query extends Query {

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
    function getSchreg($model)
    {
		$query  = " SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
		$query .= "        SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ";
		$query .= " FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
		$query .= " WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') AND ";
		$query .= "       ((SCHREG_REGD_DAT.SEMESTER)='" .$model->control["学期"]. "') AND ";
		$query .= "       ((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."')) ";
		$query .= " ORDER BY ATTENDNO ";

        return $query;
    }
}
?>
