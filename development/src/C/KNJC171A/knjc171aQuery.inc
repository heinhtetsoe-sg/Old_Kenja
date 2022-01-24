<?php

require_once('for_php7.php');

class knjc171aQuery extends Query {

    //年組取得（権限チェック）
    function getGrade($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR ";
            $query .= "                               AND T2.GRADE = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "         T1.YEAR  = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     INT(T1.GRADE) AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR ";
            $query .= "                               AND T2.GRADE = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "      TR_CD3 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
