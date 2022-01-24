<?php

require_once('for_php7.php');


class knjd232Query extends Query {

    //学年取得
    function getSelectGrade($model)
    {
        $query  = "SELECT GRADE, GRADE_NAME1 FROM SCHREG_REGD_GDAT T1 ";
        $query .= "WHERE T1.YEAR = '" .CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "ORDER BY GRADE";

        return $query;
    }

    //学期取得
    function getSelectSeme()
    {
        $query  = "SELECT DISTINCT SEMESTER,SEMESTERNAME FROM SEMESTER_MST ";
        $query .= "WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= "ORDER BY SEMESTER";

        return $query;
    }

}
?>
