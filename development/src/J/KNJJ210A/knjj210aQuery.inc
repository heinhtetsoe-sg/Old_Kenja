<?php

require_once('for_php7.php');


class knjj210aQuery extends Query {

    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME_M.NAME1 AS VALUE, ";
        $query .= "     NAME_M.ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST NAME_M ";
        $query .= " WHERE ";
        $query .= "     NAME_M.NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME_M.NAME1 IN (SELECT ";
                $query .= "                              REGD_G.SCHOOL_KIND ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME_M.NAME1 IN (SELECT ";
            $query .= "                              REGD_G.SCHOOL_KIND ";
            $query .= "                          FROM ";
            $query .= "                              SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                              REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME_M.NAMECD2 ";

        return $query;
    }

    function getGradeHrclass($model)
    {
        $query  = " SELECT ";
        $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
        $query .= "     REGD_H.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H, ";
        $query .= "     SCHREG_REGD_GDAT REGD_G ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD_H.YEAR = REGD_G.YEAR ";
        $query .= "     AND REGD_H.GRADE = REGD_G.GRADE ";
        $query .= "     AND REGD_G.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (REGD_H.TR_CD1 = '" .$staffcd ."' ";
            $query .= "      OR REGD_H.TR_CD2 = '" .$staffcd ."' ";
            $query .= "      OR REGD_H.TR_CD3 = '" .$staffcd ."' ";
            $query .= "      OR REGD_H.SUBTR_CD1 = '" .$staffcd ."' ";
            $query .= "      OR REGD_H.SUBTR_CD2 = '" .$staffcd ."' ";
            $query .= "      OR REGD_H.SUBTR_CD3 = '" .$staffcd ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
