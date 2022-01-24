<?php

require_once('for_php7.php');

class knjd062Query extends Query {

    //年組取得（権限チェック）
    function getAuth($model)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = " SELECT ";
			$query .= "     GRADE || HR_CLASS AS VALUE, ";
			$query .= "     HR_NAME AS LABEL ";
			$query .= " FROM ";
			$query .= "     SCHREG_REGD_HDAT ";
			$query .= " WHERE ";
			$query .= "     YEAR = '" .$model->control["年度"]."' AND ";
			if ($model->field["SEMESTER"]=="9") {
				$query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
			} else {
				$query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
			}
			$query .= "     GRADE = '".$model->field["GRADE"]."' ";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = " SELECT ";
			$query .= "     GRADE || HR_CLASS AS VALUE, ";
			$query .= "     HR_NAME AS LABEL ";
			$query .= " FROM ";
			$query .= "     SCHREG_REGD_HDAT ";
			$query .= " WHERE ";
			$query .= "     YEAR = '".$model->control["年度"]."' AND ";
			if ($model->field["SEMESTER"] == "9") {
                $query .= "     SEMESTER = '".CTRL_SEMESTER ."' AND ";
			} else {
				$query .= "     SEMESTER = '".$model->field["SEMESTER"] ."' AND ";
			}
			$query .= "     GRADE = '".$model->field["GRADE"]."' AND ";
	    	$query .= "     (TR_CD1 = '" .STAFFCD ."' OR ";
	    	$query .= "	     TR_CD2 = '" .STAFFCD ."' OR ";
	    	$query .= "	     TR_CD3 = '" .STAFFCD ."' ) ";
		}

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
		if ($model->field["SEMESTER"] == "9") {
            $query .= " AND SEMESTER='".CTRL_SEMESTER ."' ";
		} else {
            $query .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
		}
        $query .= "ORDER BY GRADE ";

        return $query;
    }

    //テスト種別取得
    function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || '  ' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            if ($model->field["SEMESTER"] == "9") {
                $query .= "     AND TESTITEMCD = '00' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
