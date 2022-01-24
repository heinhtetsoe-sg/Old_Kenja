<?php

require_once('for_php7.php');

class knjl338qQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2    = '".$namecd2."' ";
        }
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得L004
    function getNameMstL004($year) {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL, ";
        $query .= "     T3.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ABBV3 AS NAMECD2 ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L004' ";
        $query .= "         GROUP BY ";
        $query .= "             ABBV3 ";
        $query .= "     ) T2 ON T2.NAMECD2 = T1.NAMECD2 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ABBV3 AS NAMECD2, ";
        $query .= "             '1' AS NAMESPARE2 ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L004' ";
        $query .= "             AND NAMESPARE2 = '1' ";
        $query .= "         GROUP BY ";
        $query .= "             ABBV3 ";
        $query .= "     ) T3 ON T3.NAMECD2 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.NAMECD1 = 'L045' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
