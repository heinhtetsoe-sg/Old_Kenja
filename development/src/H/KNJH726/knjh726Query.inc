<?php

require_once('for_php7.php');

class knjh726Query extends Query
{

    //入試区分取得
    public function getTestDiv($year)
    {
        $query .= " SELECT ";
        $query .= "   T2.NAMECD2 AS VALUE, ";
        $query .= "   T2.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   V_NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "   T2.YEAR = '{$year}' ";
        $query .= "   AND T2.NAMECD1 = 'H320' ";

        return $query;
    }
}
