<?php

require_once('for_php7.php');


class knjp806Query extends Query {

    //年組取得（権限チェック）
    function getHrClass($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR='" .CTRL_YEAR ."'";
            $query .= "     AND REGD_H.SEMESTER='".CTRL_SEMESTER ."'";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                             REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                             AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                             REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR='" .CTRL_YEAR ."' ";
            $query .= "     AND REGD_H.SEMESTER='".CTRL_SEMESTER ."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                             REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                             AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                             REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            $query .= "     AND (REGD_H.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR REGD_H.SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

}
?>
