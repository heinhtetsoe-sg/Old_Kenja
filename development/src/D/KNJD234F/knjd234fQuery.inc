<?php

require_once('for_php7.php');

class knjd234fQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.GRADE AS VALUE, ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GDAT.YEAR = T2.YEAR ";
        $query .= "     AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND GDAT.GRADE = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T2.GRADE || '-' || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.GRADE = '{$model->field["GRADE"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

}
?>
