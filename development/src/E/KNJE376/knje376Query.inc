<?php

require_once('for_php7.php');

class knje376Query extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得
    function getGrade($model)  {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_H.GRADE AS VALUE, ";
        $query .= "     REGD_G.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT REGD_G ";
        $query .= "          ON REGD_H.YEAR  = REGD_G.YEAR  ";
        $query .= "         AND REGD_H.GRADE = REGD_G.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD_H.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "          AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
            $query .= "          AND REGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
