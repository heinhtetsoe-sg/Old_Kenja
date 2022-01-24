<?php

require_once('for_php7.php');


class knjb100Query extends Query {

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('H','J') ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND GRADE IN ( ";
            $query .= "         SELECT DISTINCT  ";
            $query .= "             GRADE ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_HDAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR='".CTRL_YEAR."' AND ";
            $query .= "             SEMESTER='".$model->field["SEMESTER"]."' AND ";
            $query .= "            (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "             TR_CD2 = '".STAFFCD."' OR ";
            $query .= "             TR_CD3 = '".STAFFCD."')) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期日付範囲取得
    function getSemeDate($model)
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER='".$model->field["SEMESTER"]."' ";

        return $query;
    }
}
?>
