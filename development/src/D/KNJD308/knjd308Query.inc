<?php

require_once('for_php7.php');


class knjd308Query extends Query {

    //学期リスト取得
    function getSemester($model)
    {
        $query  = "SELECT semester,semestername FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' and semester<>'9'";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
	//	３学期は、１・２年のみ表示。
		if ($model->field["GAKKI"]=="3")
       		$query .= "AND GRADE <> '03' ";
        $query .= "order by grade";

        return $query;
    }


}
?>
