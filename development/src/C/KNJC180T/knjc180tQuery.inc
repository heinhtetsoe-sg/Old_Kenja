<?php

require_once('for_php7.php');

class knjc180tQuery extends Query {

    //クラス取得
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "	ORDER BY ";
        $query .= "	    VALUE ";

        return $query;
    }

    //クラス取得
    function getSelectClass($model)
    {
        $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR	 = '" .CTRL_YEAR ."' ";
        $query .= "  AND T1.SEMESTER = '" .substr($model->month,0,1)."' ";
        $query .= "  AND T1.GRADE = '" .$model->field["GRADE"]."' ";

        return $query;
    }

    //学期の間の月を取得
    function getSemesterMonth()
    {
        return "SELECT * FROM semester_mst WHERE year = '" .CTRL_YEAR ."' AND semester <> '9'";
    }

}
?>
