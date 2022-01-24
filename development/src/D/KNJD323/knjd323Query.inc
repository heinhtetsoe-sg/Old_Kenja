<?php

require_once('for_php7.php');


class knjd323Query extends Query {

    //学期リスト取得
    function getSemester($model)
    {
        $query  = "SELECT semester,semestername FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		if($model->field["GAKKI"] != 9){
			$query .= "AND SEMESTER='".$model->field["GAKKI"]."' ";
		}else {
			$query .= "AND SEMESTER='".CTRL_SEMESTER."' ";
		}
        //---2005.05.09
		if ($model->field["TESTKINDCD"] == "91" || $model->field["TESTKINDCD"] == "92") 
			$query .= "AND GRADE='03' ";
        $query .= "order by grade";

        return $query;
    }


}
?>
