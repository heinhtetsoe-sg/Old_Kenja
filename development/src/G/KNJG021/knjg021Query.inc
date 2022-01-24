<?php

require_once('for_php7.php');


class knjg021Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //卒業年度取得
    function selectYear($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    t1.year ";
        $query .= "FROM ";
        $query .= "    schreg_regd_hdat t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR        = T1.YEAR ";
            $query .= "                               AND T2.GRADE       = T1.GRADE ";
            $query .= "                               AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //$query .= "WHERE ";
        //$query .= "    grade='03' AND ";
        //$query .= "    ((year='".CTRL_YEAR."' AND semester='".CTRL_SEMESTER."') OR ";
        //$query .= "     (year<'".CTRL_YEAR."' AND semester='3')) ";
        $query .= "ORDER BY ";
        $query .= "    t1.year desc";

        return $query;
    }

    //学期制・学年数上限取得
    function selectGradeSemesterDiv($model)
    {
        $query  = "SELECT grade_hval, semesterdiv FROM school_mst WHERE year='".$model->field["YEAR"]."' ";

        return $query;
    }

}
?>
