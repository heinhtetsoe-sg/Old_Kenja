<?php

require_once('for_php7.php');

class knjc122Query extends Query {

    //学期名取得
    function getSemester() {
        $query .= " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT DISTINCT ";
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SCHOOL_KIND IN ('H','J') ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T1.GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT T1, ";
            $query .= "     SCHREG_REGD_HDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.GRADE = T2.GRADE AND ";
            $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.SCHOOL_KIND IN ('H','J') AND";
            $query .= "    (T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "     T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "     T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "     T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "     T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "     T2.SUBTR_CD3 = '".STAFFCD."' ) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrclass($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE = '".$model->field["GRADE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "    AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "         TR_CD2 = '".STAFFCD."' OR ";
            $query .= "         TR_CD3 = '".STAFFCD."' OR ";
            $query .= "         SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "         SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "         SUBTR_CD3 = '".STAFFCD."' ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
