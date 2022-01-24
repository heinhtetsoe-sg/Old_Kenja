<?php

require_once('for_php7.php');

class knjd134Query extends Query {

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["knjd134PrintEachSemester"] == '1') {
            $query .= "     AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR  = T2.YEAR ";
        $query .= "     AND T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        if (!($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1")) {
            $query .= "     AND T1.GRADE < '11' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["SEMESTER"] == "9") {
            $query .= " AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= " AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $query .= "     AND (T2.TR_CD1      = '".STAFFCD."' OR ";
        $query .= "          T2.TR_CD2      = '".STAFFCD."' OR ";
        $query .= "          T2.TR_CD3      = '".STAFFCD."' OR ";
        $query .= "          T2.SUBTR_CD1   = '".STAFFCD."' OR ";
        $query .= "          T2.SUBTR_CD2   = '".STAFFCD."' OR ";
        $query .= "          T2.SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrclass($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            if ($model->field["SEMESTER"] == "9") {
                $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            } else {
                $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            }
            $query .= "     GRADE = '".$model->field["GRADE"]."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .CTRL_YEAR."' AND ";
            if ($model->field["SEMESTER"] == "9") {
                $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            } else {
                $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            }
            $query .= "     GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "    (TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "     TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "     TR_CD3 = '" .STAFFCD ."' OR ";
            $query .= "     SUBTR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "     SUBTR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "     SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }
}
?>
