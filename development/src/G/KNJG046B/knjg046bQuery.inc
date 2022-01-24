<?php

require_once('for_php7.php');

class knjg046bQuery extends Query {

    //校種
    function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

}
?>
