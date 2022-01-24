<?php

require_once('for_php7.php');

class knja113aQuery extends Query {
/**********/
/** 画面 **/
/**********/
    function getClassName($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //校種取得
    function getSchKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //減免マスタ取得
    function getScholarshipMst($model, $schoolkind) {
        $query  = " SELECT ";
        $query .= "    REDUCTION_DIV_CD || ':' || REDUCTION_DIV_NAME AS LABEL, ";
        $query .= "    REDUCTION_DIV_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolkind."' AND ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        if ($model->field["STD_DIV"] == 1) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     '新入生' || T1.GRADE AS LABEL ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.ENTERYEAR = T2.YEAR AND T1.GRADE = T2.GRADE AND T2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= " WHERE ";
            $query .= "     T1.ENTERYEAR = '".$model->year."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$model->year."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

}
?>
