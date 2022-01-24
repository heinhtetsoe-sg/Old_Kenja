<?php

require_once('for_php7.php');


class knjm810Query extends Query {

    //年度取得
    function getYear($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年（入学年度）取得（権限チェック）
    function getGrade($model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        //$query .= "     SCHOOL_KIND IN ('H','J') ";
        //参照・更新可（制限付き）
        /*if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND GRADE IN ( ";
            $query .= "         SELECT DISTINCT  ";
            $query .= "             GRADE ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_HDAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR='".$model->field["YEAR"]."' AND ";
            $query .= "             SEMESTER='".$model->field["SEMESTER"]."' AND ";
            $query .= "            (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "             TR_CD2 = '".STAFFCD."' OR ";
            $query .= "             TR_CD3 = '".STAFFCD."')) ";
        }*/
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
