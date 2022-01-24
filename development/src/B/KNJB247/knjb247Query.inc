<?php

require_once('for_php7.php');

class knjb247Query extends Query {
    //学期取得
    function getSemester($semester="") {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE, ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($semester) {
            $query .= "     SEMESTER = '".$semester."' ";
        } else {
            $query .= "     SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス一覧取得
    function getHrClassList($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND ";
            $query .= "    (TR_CD1      = '".STAFFCD."' OR ";
            $query .= "     TR_CD2      = '".STAFFCD."' OR ";
            $query .= "     TR_CD3      = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
