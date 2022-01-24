<?php

require_once('for_php7.php');

class knjz413aQuery extends Query {

    function getSchoolGroup($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL,  ";
        $query .= "     NAMECD2 AS VALUE  ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = 'E012' ";

        return $query;
    }
}
?>
