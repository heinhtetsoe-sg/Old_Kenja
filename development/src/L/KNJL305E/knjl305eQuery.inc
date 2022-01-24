<?php

require_once('for_php7.php');

class knjl305eQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $nspare1 = "", $notnspare1 = "") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($nspare1 != "") {
            $query .= " AND NAMESPARE1 = '".$nspare1."' ";
        } else if ($notnspare1 != "") {
            $query .= " AND VALUE(NAMESPARE1, '') <> '".$notnspare1."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
