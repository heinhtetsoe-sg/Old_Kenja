<?php

require_once('for_php7.php');

class knjl306jQuery extends Query {

    //��ʁE�A�����o����Ԏ擾
    function get_test_date_range($namecd,$year,$cd2)
    {
        $query  = " SELECT DISTINCT ABBV2,ABBV3 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 = '".$cd2."' ";

        return $query;
    }
    
}
?>
