<?php

require_once('for_php7.php');


class knjc151Query extends Query {

    function getNameMst($namecd1, $orderby) {
        $query  = " SELECT ";
        $query .= "    NAMECD2 AS VALUE,  ";
        $query .= "    NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        if ($orderby) {
            $query .=  $orderby;
        } else {
            $query .= " NAMECD2 ";
        }
        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if ($model->auth == DEF_REFERABLE || $model->auth == DEF_UPDATABLE) {
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
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
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
