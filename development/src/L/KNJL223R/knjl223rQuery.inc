<?php

require_once('for_php7.php');

class knjl223rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        if ($namecd1 == 'L004') {
            $query .= "     NAMECD2 || ':' || ABBV2 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
