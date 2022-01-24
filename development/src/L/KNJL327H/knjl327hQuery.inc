<?php

require_once('for_php7.php');

class knjl327hQuery extends Query {

    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //入試区分取得
    function get_test_div($namecd,$year,$applicantdiv)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2,NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."'  ";
        if($applicantdiv != '1'){
            $query .= " AND NAMECD2 = '1'  ";
        }
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }
}
?>
