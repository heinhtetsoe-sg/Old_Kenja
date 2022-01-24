<?php

require_once('for_php7.php');

class knjp400jQuery extends Query {

    //中高判定用
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLNAME1 LIKE '%近畿%中学%' ";

        return $query;
    }

}
?>
