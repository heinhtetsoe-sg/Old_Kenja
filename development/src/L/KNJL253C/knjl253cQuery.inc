<?php

require_once('for_php7.php');


class knjl253cQuery extends Query {

    //入試制度取得
    function getApctDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L103' AND ";
        $query .= "     NAMECD2 = '1' ";

        return $query;
    }

    //名称マスタよりデータを取得
    function getVnameMst($year, $namecd1)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
