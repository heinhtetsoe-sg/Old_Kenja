<?php

require_once('for_php7.php');


class knja180Query extends Query {

    //学期取得
    function getSelectSeme($year)
    {
        $query = "SELECT semester,semestername FROM semester_mst WHERE year='" .$year ."' AND not semester='9'";

        return $query;
    }

    //クラス一覧取得
    function getSql_GradeHrClass($model)
    {

		$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
		$query .= "FROM SCHREG_REGD_HDAT ";
		$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
		$query .= "AND SEMESTER='".$model->field["GAKKI"] ."'";

        return $query;

    }


    //課程学科取得
    function GetSql_Course_Major($year)
    {

		$query  = "SELECT ";
		$query .= "COURSECD || MAJORCD AS VALUE,";
		$query .= "COURSECD || MAJORCD || ' ' || VALUE(COURSENAME,'') || VALUE(MAJORNAME,'') AS LABEL ";
		$query .= "FROM V_COURSE_MAJOR_MST ";
		$query .= "WHERE YEAR='" .$year ."' ";

        return $query;

    }


    //コース取得
    function GetSql_CourseCode($year)
    {

		$query  = "SELECT ";
		$query .= "COURSECODE AS VALUE,";
		$query .= "COURSECODE || ' ' || VALUE(COURSECODENAME,'') AS LABEL ";
		$query .= "FROM V_COURSECODE_MST ";
		$query .= "WHERE YEAR='" .$year ."' ";

        return $query;

    }


}
?>
