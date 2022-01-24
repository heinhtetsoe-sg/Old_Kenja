<?php

require_once('for_php7.php');

class knjl315bQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L009" && $model->field["TESTDIV"] === '1') {
            $query .= " AND NAMESPARE2 = '1' ";
        } else if ($namecd1 == "L009" && $model->field["TESTDIV"] === '2') {
            $query .= " AND NAMESPARE3 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
