<?php

require_once('for_php7.php');


class knjd327Query extends Query {


    //学期リスト取得
    function getSemester($model)
    {
        $query  = "SELECT semester,semestername,edate FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' and semester<>'9'";

        return $query;
    }


    //学年取得
    function getSelectGrade($model,$db)
    {
        //中学か高校かを判断
        $school = $db->getOne("SELECT COUNT(*) FROM SCHOOL_MST WHERE SCHOOLNAME1 LIKE '%中学%'");

        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
	//	３学期は、１・２年のみ表示。//中学は３年も表示
		if ($model->field["GAKKI"]=="3" && $school == 0)
       		$query .= "AND GRADE <> '03' ";
        $query .= "order by grade";

        return $query;
    }


}
?>
