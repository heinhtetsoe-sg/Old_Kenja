<?php

require_once('for_php7.php');

class knjl373mQuery extends Query {
    //�������x�擾
    function getApplicantdiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMECD2 ";

        return $query;
    }
}
?>
