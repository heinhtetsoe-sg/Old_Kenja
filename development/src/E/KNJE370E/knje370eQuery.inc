<?php

require_once('for_php7.php');


class knje370eQuery extends Query {

    //���̃}�X�^�擾
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     T1.NAME1   AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

}
?>
