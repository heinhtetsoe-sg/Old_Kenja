<?php

require_once('for_php7.php');

class knjl347Query extends Query {

    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     SHOWORDER, ";
        $query .= "     NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$ObjYear}' ";
        $query .= "     AND SHOWDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
