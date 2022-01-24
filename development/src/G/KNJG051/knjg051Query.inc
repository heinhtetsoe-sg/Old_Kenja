<?php

require_once('for_php7.php');


class knjg051Query extends Query {

    //卒業年度取得
    function selectYear()
    {
		$query  = "SELECT DISTINCT ";
		$query .= "    year ";
		$query .= "FROM ";
		$query .= "    schreg_regd_hdat ";
		//$query .= "WHERE ";
		//$query .= "    grade='03' AND ";
		//$query .= "    ((year='".CTRL_YEAR."' AND semester='".CTRL_SEMESTER."') OR ";
		//$query .= "     (year<'".CTRL_YEAR."' AND semester='3')) ";
		$query .= "ORDER BY ";
		$query .= "    year desc";

        return $query;
    }


    //学期制・学年数上限取得
    function selectGradeSemesterDiv($model)
    {
		$query  = "SELECT grade_hval, semesterdiv FROM school_mst WHERE year='".$model->field["YEAR"]."' ";

        return $query;
    }

    //年組取得
    function getAuth($model,$opt_Grade)
    {
		$query  = "SELECT ";
		$query .= "    GRADE || HR_CLASS AS VALUE, ";
		$query .= "    HR_NAME AS LABEL ";
		$query .= "FROM ";
		$query .= "    SCHREG_REGD_HDAT ";
		$query .= "WHERE ";
		$query .= "    int(GRADE)=int('".$opt_Grade."') AND ";
		$query .= "    YEAR='".$model->field["YEAR"]."' AND ";
		$query .= "    SEMESTER='".$model->field["GAKKI"]."' ";
		$query .= "ORDER BY ";
		$query .= "    GRADE || HR_CLASS ";

        return $query;
    }

    //生徒取得
    function getstudent($model)
    {
		$query  = "WITH now_sch (SCHREGNO,ATTENDNO) AS (SELECT ";
		$query .= "    SCHREGNO, ";
		$query .= "    ATTENDNO ";
		$query .= "FROM ";
		$query .= "    SCHREG_REGD_DAT ";
		$query .= "WHERE ";
		$query .= "    YEAR='".$model->field["YEAR"]."' AND ";//NO001
//		$query .= "    YEAR = '2004' AND ";
		$query .= "    GRADE || HR_CLASS = '" .$model->field["CMBCLASS"]. "' AND ";
		$query .= "    SEMESTER = '" .$model->field["GAKKI"]. "' ) ";
		$query .= "SELECT ";
		$query .= "    SCHREG_BASE_MST.SCHREGNO || '　' || now_sch.ATTENDNO || '番　' || SCHREG_BASE_MST.NAME_SHOW as LABEL, ";
		$query .= "    now_sch.ATTENDNO as VALUE ";
		$query .= "FROM ";
		$query .= "    SCHREG_BASE_MST,now_sch ";
		$query .= "WHERE ";
		$query .= "    SCHREG_BASE_MST.SCHREGNO = now_sch.SCHREGNO ";
		$query .= " ORDER BY ATTENDNO ";

        return $query;
    }
}
?>
