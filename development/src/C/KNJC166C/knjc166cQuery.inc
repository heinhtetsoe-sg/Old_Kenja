<?php

require_once('for_php7.php');


class knjc166cQuery extends Query {

    function getValidGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.GRADE_NAME1 AS LABEL, ";
        $query .= "     T3.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     KAIKIN_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "       ON T3.YEAR = T2.YEAR ";
        $query .= "      AND T3.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND KAIKIN_FLG = '1' ";
        $query .= "     AND VALUE(INVALID_FLG, '0') <> '1' ";

        return $query;
    }

    function getKaikinCdList($model) {
        $query  = " SELECT ";
        $query .= "     KAIKIN_CD || ':' || KAIKIN_NAME AS LABEL, ";
        $query .= "     KAIKIN_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     KAIKIN_MST ";
        $query .= " ORDER BY ";
        $query .= "     KAIKIN_DIV, ";
        $query .= "     KAIKIN_CD ";

        return $query;
    }
    
    function getNichiniKaikinCdList($model) {
        $query  = " SELECT ";
        $query .= "     T1.KAIKIN_CD AS VALUE ";
        $query .= "     , RIGHT ('0' || T1.KAIKIN_CD, 2) || ':' || T1.KAIKIN_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     KAIKIN_MST T1 ";
        $query .= " INNER JOIN ";
        $query .= "     KAIKIN_GRADE_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.KAIKIN_CD = T2.KAIKIN_CD ";
        $query .= " WHERE ";
        $query .= "     T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "  ORDER BY ";
        $query .= "    T1.KAIKIN_CD, T2.GRADE ";

        return $query;
    }

    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        
        return $query;
    }

}
?>
