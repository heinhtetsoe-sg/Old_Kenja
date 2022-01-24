<?php

require_once('for_php7.php');

class knjl301jQuery extends Query {

    //入試区分取得
    function get_test_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2,NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( ";
        $query .= "   SELECT ";
        $query .= "        TESTDIV ";
        $query .= "   FROM ";
        $query .= "        ENTEXAM_DESIRE_DAT ";
        $query .= "   WHERE ";
        $query .= "        ENTEXAMYEAR = '".$year."' ";
        $query .= "   GROUP BY ";
        $query .= "        TESTDIV ";
        $query .= " )";	
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }
}
?>
