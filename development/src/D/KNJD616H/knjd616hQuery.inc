<?php

require_once('for_php7.php');

class knjd616hQuery extends Query {

    //学期取得
    function getSelectSeme() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER";

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE || '-' || SCHOOL_KIND AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
