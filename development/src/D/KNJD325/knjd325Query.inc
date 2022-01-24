<?php

require_once('for_php7.php');


class knjd325Query extends Query {

    //学期リスト取得
    function getSemester($model)
    {
        $query  = "SELECT semester,semestername FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' ";

        return $query;
    }

    //年組取得（権限チェック）---2005.05.18
    function getAuth($model)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."'";
			if ($model->field["GAKKI"]=="9") {
				$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
			} else {
				$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
			}
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."'";
			if ($model->field["GAKKI"]=="9") {
				$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
			} else {
				$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
			}
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."' ";
            //---2005.06.01
	    	$query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD3 = '" .STAFFCD ."') ";
		}
		if ($model->field["TESTKINDCD"] == "91" || $model->field["TESTKINDCD"] == "92") 
			$query .= "AND GRADE='03' ";
        $query .= "ORDER BY 1";

        return $query;
    }

//    //年組取得
//    function getSelectClass($model)
//    {
//        $query  = "SELECT GRADE,HR_CLASS,HR_NAME FROM SCHREG_REGD_HDAT ";
//        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
//		if($model->field["GAKKI"] != 9){
//			$query .= "AND SEMESTER='".$model->field["GAKKI"]."' ";
//		}else {
//			$query .= "AND SEMESTER='".CTRL_SEMESTER."' ";
//		}
//		$query .= "AND GRADE='".$model->field["GRADE"]."' ";//---2005.05.18
//        $query .= "ORDER BY GRADE,HR_CLASS";//---2005.05.18

//        return $query;
//    }

    //コース取得
    function getSelectCorse($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    W1.COURSECODE, ";
        $query .= "    W2.COURSECODENAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT W1, ";
        $query .= "    COURSECODE_MST W2 ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' AND ";
		if($model->field["GAKKI"] != 9){
			$query .= "SEMESTER='".$model->field["GAKKI"]."' AND ";
		}else {
			$query .= "SEMESTER='".CTRL_SEMESTER."' AND ";
		}
        $query .= "    GRADE='".substr($model->field["CLASS"],0,2)."' AND ";
        $query .= "    W1.COURSECODE=W2.COURSECODE ";
        $query .= "ORDER BY ";
        $query .= "    W1.COURSECODE ";

        return $query;
    }

    //コース取得表示用
    function getSelectCorseShow($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    W1.COURSECODE, ";
        $query .= "    W2.COURSECODENAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT W1, ";
        $query .= "    COURSECODE_MST W2 ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' AND ";
		if($model->field["GAKKI"] != 9){
			$query .= "SEMESTER='".$model->field["GAKKI"]."' AND ";
		}else {
			$query .= "SEMESTER='".CTRL_SEMESTER."' AND ";
		}
        $query .= "    GRADE||HR_CLASS = '".$model->field["CLASS"]."' AND ";
        $query .= "    W1.COURSECODE=W2.COURSECODE ";
        $query .= "ORDER BY ";
        $query .= "    W1.COURSECODE ";

        return $query;
    }

}
?>
