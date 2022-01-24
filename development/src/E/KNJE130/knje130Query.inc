<?php

require_once('for_php7.php');


class knje130Query extends Query {


    //学年取得
    function getSelectGrade()
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
		//参照・更新可（制限付き）---2005.07.12
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."' ";
	    	$query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD3 = '" .STAFFCD ."') ";
		}
        $query .= "order by grade";

        return $query;
    }


    //年組取得（権限チェック）---2005.07.12
    function getAuth($model)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
			$query .= "AND GRADE='".$model->field["GRADE"] ."' ";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
			$query .= "AND GRADE='".$model->field["GRADE"] ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."' ";
	    	$query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }


}
?>
