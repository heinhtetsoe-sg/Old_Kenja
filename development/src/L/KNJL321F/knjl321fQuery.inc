<?php

require_once('for_php7.php');

class knjl321fQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //受験型
    function getExamType($year, $testDiv)
    {
        $query  = " WITH TMP AS ( ";
        $query .= " SELECT  ";
        $query .= "      T2.NAMECD2 AS VALUE ";
        $query .= "    , T2.NAMECD2 || ':' || T2.NAME1 AS LABEL";
        $query .= "    , T2.NAME1 ";
        $query .= " FROM V_NAME_MST T1 ";
        $query .= " INNER JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.NAMECD1 = 'L005' ";
        $query .= "     AND (T1.ABBV2 = '1' AND T2.ABBV2 = '1' ";
        $query .= "       OR VALUE(T1.ABBV2, '') <> '1' AND VALUE(T2.ABBV2, '') <> '1' ";
        $query .= "         ) ";
        $query .= " WHERE T1.YEAR = '".$year."' ";
        $query .= "    AND T1.NAMECD1 = 'L024' ";
        $query .= "    AND T1.NAMECD2 = '".$testDiv."' ";
        $query .= " ) ";
        $query .= " SELECT  ";
        $query .= "      VALUE ";
        $query .= "    , LABEL";
        $query .= " FROM TMP T1 ";
        $query .= " UNION ";
        $query .= " SELECT  ";
        $query .= "      'X' AS VALUE ";
        $query .= "    , 'X:' || (SELECT NAME1 FROM TMP WHERE VALUE = '1') || 'と' || (SELECT NAME1 FROM TMP WHERE VALUE = '2') AS LABEL ";
        $query .= " FROM SYSIBM.SYSDUMMY1 T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

}
?>
