<?php

require_once('for_php7.php');

class knjl326fQuery extends Query {

    //学校コードを取得：五条・和歌山を判断するため
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
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

    //入試制度取得
    function getApctDiv($namecd, $year, $model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 = '1' "; // 中学のみ
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
