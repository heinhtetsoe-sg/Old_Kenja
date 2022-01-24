<?php

require_once('for_php7.php');


class knji092Query extends Query {

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


    //学年数上限取得 NO001
    function selectMaxGrade($model)
    {
        $query  = "SELECT MAX(GRADE) AS GRADE FROM SCHREG_REGD_HDAT WHERE YEAR='".$model->field["YEAR"]."' AND SEMESTER = '".$model->field["GAKKI"]."' ";

        return $query;
    }

    //学年数上限取得(中高一貫)
    function selectMaxGrade2()
    {
        $query  = "SELECT VALUE(NAMESPARE2, '0') AS VAL FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }


    //年組取得
    function getAuth($model, $opt_Grade, $opt_Grade2)
    {
        $query  = "SELECT ";
        $query .= "    GRADE || HR_CLASS AS VALUE, ";
        $query .= "    HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        if ($opt_Grade2) {
            $query .= "    int(GRADE) IN (".$opt_Grade.", ".$opt_Grade2.") AND ";
        } else {
            $query .= "    int(GRADE) IN (".$opt_Grade.") AND ";
        }
        $query .= "    YEAR='".$model->field["YEAR"]."' AND ";
        $query .= "    SEMESTER='".$model->field["GAKKI"]."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE || HR_CLASS ";

        return $query;
    }


}
?>
