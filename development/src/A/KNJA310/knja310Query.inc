<?php

require_once('for_php7.php');


class knja310Query extends Query {

    //生徒一覧取得
    function getSchno($model,$year,$semester)
    {
		$query  = "SELECT T1.SCHREGNO, ";
		$query .= "       T1.SCHREGNO || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME AS NAME ";
		$query .= "FROM   SCHREG_REGD_DAT T1,SCHREG_BASE_MST T2 ";
		$query .= "WHERE  T1.YEAR       = '".$year."' AND ";
		$query .= "       T1.SEMESTER   = '".$semester."' AND ";
		$query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
		$query .= "       T2.SCHREGNO   = T1.SCHREGNO ";
		$query .= "ORDER BY T1.ATTENDNO ";

        return $query;
    }

}
?>
