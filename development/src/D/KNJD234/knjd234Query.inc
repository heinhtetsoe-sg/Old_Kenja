<?php

require_once('for_php7.php');


class knjd234Query extends Query {

    //学年取得
    function getSelectGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SCHOOL_KIND IN ('H', 'J') ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE";

        return $query;
    }

    //学期取得
    function getSelectSeme()
    {
        $query  = "SELECT DISTINCT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "     SEMESTER";

        return $query;
    }

}

?>
