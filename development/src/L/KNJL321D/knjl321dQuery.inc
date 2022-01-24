<?php

require_once('for_php7.php');

class knjl321dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    function getNameMstSpare($model, $spare) {
        $query  = " SELECT DISTINCT ";
        if ($spare != "4") {
            $query .= "     NAMESPARE{$spare} ";
        } else {
            $query .= "     ABBV3 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->examyear."' ";
        $query .= "     AND NAMECD1 = 'L045' ";
        $query .= "     AND NAMECD2 = '".$model->testdiv."' ";

        return $query;
    }
}
?>
