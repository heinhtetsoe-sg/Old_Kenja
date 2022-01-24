<?php

require_once('for_php7.php');


class knjp807Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

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
            $query .= "     REGD_H.YEAR = '".CTRL_YEAR."'";
            $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."'";
            $query .= "     AND REGD_H.GRADE IN (SELECT ";
            $query .= "                             REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                             AND REGD_G.SCHOOL_KIND = '{$model->schoolKind}') ";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND REGD_H.GRADE IN (SELECT ";
            $query .= "                             REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                             AND REGD_G.SCHOOL_KIND = '{$model->schoolKind}') ";
            $query .= "     AND (REGD_H.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }

    //項目
    function getCollectM($model)
    {
        $query .= " SELECT ";
        $query .= "     COL_M.COLLECT_L_CD || COL_M.COLLECT_M_CD AS VALUE, ";
        $query .= "     COL_M.COLLECT_L_CD || COL_M.COLLECT_M_CD || ':' || COL_M.COLLECT_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST COL_M ";
        $query .= " WHERE ";
        $query .= "     COL_M.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND COL_M.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND COL_M.YEAR = '".CTRL_YEAR ."' ";
        $query .= "     AND COL_M.TEXTBOOKDIV IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
