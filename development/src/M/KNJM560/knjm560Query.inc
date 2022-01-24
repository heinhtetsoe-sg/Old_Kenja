<?php

require_once('for_php7.php');


class knjm560Query extends Query {

    //講座
    function GetChr($model)
    {
        $query = " SELECT ";
        $query .= "     CHAIRNAME AS LABEL, ";
        $query .= "     CHAIRCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     CHAIRCD <= '90' ";
        $query .= " ORDER BY ";
        $query .= "     CHAIRCD ";
        return $query;
    }

}
?>
