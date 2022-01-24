<?php

require_once('for_php7.php');

class knje080Query extends Query {

    //年組取得（権限チェック）
    function getAuth($year, $semester) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '".$year."' AND ";
            $query .= "     SEMESTER = '".$semester."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$year."' AND ";
            $query .= "     SEMESTER    = '".$semester."' AND ";
            $query .= "     (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "      TR_CD2     = '".STAFFCD."' OR ";
            $query .= "      TR_CD3     = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧
    function getList($model, $schregno="") {
        $query  = " SELECT ";
        $query .= "     T2.GRADE || '-' || T2.HR_CLASS || '-' || T2.ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR     = T2.YEAR ";
        $query .= "                                  AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "                                  AND T3.GRADE    = T2.GRADE ";
        $query .= "                                  AND T3.HR_CLASS = T2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        if ($schregno) {
            $query .= "     T1.SCHREGNO IN ('".implode("','", $schregno)."') ";
        } else {
            $query .= "     T2.GRADE || T2.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職員取得
    function getStaffList() {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || '　' || STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1, ";
        $query .= "     STAFF_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD  = T2.STAFFCD  AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
