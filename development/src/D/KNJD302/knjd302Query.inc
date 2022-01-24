<?php

require_once('for_php7.php');


class knjd302Query extends Query {

    //学年取得/* NO004 */
    function getSelectGrade($model)
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		//	学年末は、今学期を使用	/* NO003 */
		if ($model->field["GAKKI"]=="9") {
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
		} else {
			$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
		}
		//	３学期は、１・２年のみ表示。
		if ($model->field["GAKKI"]=="3")
       		$query .= "AND GRADE <> '03' ";
        $query .= "order by grade";

        return $query;
    }


    //年組取得（権限チェック）
    function getAuth($model)
    {
		$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
		$query .= "FROM SCHREG_REGD_HDAT ";
		$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
   		$query .= "AND GRADE = '".$model->field["GRADE"] ."' ";/* NO004 */
		//	学年末は、今学期を使用	/* NO003 */
		if ($model->field["GAKKI"]=="9") {
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
		} else {
			$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		}
        $query .= "order by 1";

        return $query;
/*
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
		//	学年末は、今学期を使用
			if ($model->field["GAKKI"]=="9") {
				$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
			} else {
				$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
			}
		//	３学期は、１・２年のみ表示。
			if ($model->field["GAKKI"]=="3")
        		$query .= "AND GRADE <> '03' ";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
		//	学年末は、今学期を使用
			if ($model->field["GAKKI"]=="9") {
				$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
			} else {
				$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
			}
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		//	３学期は、１・２年のみ表示。
			if ($model->field["GAKKI"]=="3")
        		$query .= "AND GRADE <> '03' ";
		}
*/
    }


    //学期リスト取得
    function getSemester($model)
    {
        $query  = "SELECT semester,semestername FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' ";	/* NO003 */
        $query .= "order by semester";

        return $query;
    }

}
?>
