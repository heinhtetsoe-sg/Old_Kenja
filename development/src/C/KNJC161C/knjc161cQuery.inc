<?php

require_once('for_php7.php');

class knjc161cQuery extends Query
{

    //学年
    public function getGrade($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = " SELECT DISTINCT ";
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR='" .CTRL_YEAR ."'";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     GRADE ";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT DISTINCT ";
            $query .= "     L1.GRADE AS VALUE, ";
            $query .= "     L1.GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT L1 ON T1.YEAR = L1.YEAR ";
            $query .= "           AND T1.GRADE = L1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR='" .CTRL_YEAR ."' ";
            $query .= "     AND T1.SEMESTER='".CTRL_SEMESTER ."' ";
            $query .= "     AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "     OR   T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR   T1.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "     OR   T1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "     OR   T1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR   T1.SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //年組取得（権限チェック）
    public function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = " SELECT DISTINCT ";
            $query .= "     HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .CTRL_YEAR ."' ";
            $query .= "     AND SEMESTER = '" .CTRL_SEMESTER ."' ";
            $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT DISTINCT ";
            $query .= "     HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .CTRL_YEAR ."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER ."' ";
            $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "     OR   TR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR   TR_CD3 = '" .STAFFCD ."' ";
            $query .= "     OR   SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "     OR   SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR   SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }
}
