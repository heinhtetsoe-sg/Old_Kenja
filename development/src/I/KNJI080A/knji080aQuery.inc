<?php

require_once('for_php7.php');


class knji080aQuery extends Query {

    //卒業年度取得
    function selectYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     Fiscalyear(GRD_DATE) as GRADUATE_YEAR ";
        $query .= " FROM ";
        $query .= "     GRD_BASE_MST ";
        $query .= " ORDER BY ";
        $query .= "     GRADUATE_YEAR DESC ";

        return $query;
    }

    //ＣＳＶ
    function selectCsvQuery($model) {

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     N1.NAME2 AS SEX, ";
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.ENT_DATE ";
        $query .= " FROM ";
        $query .= "     GRD_BASE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T1.SEX = N1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     Fiscalyear(T1.GRD_DATE) = '{$model->field["GRADUATE_YEAR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

}
?>
