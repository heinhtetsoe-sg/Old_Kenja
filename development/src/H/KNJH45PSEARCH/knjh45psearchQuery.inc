<?php

require_once('for_php7.php');

class knjh45psearchQuery extends Query {
    function getJogai()
    {
        $query  = " SELECT ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'H315' ";
        $query .= " ORDER BY ";
        $query .= "    NAMECD2 ";
        
        return $query;
    }
    
    function getNendo($from, $to)
    {
        $query  = " SELECT ";
        $query .= "    YEAR, ";
        $query .= "    PRESENT_EST ";
        $query .= " FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR BETWEEN '".$from."' AND '".$to."' ";
        $query .= " ORDER BY ";
        $query .= "    YEAR DESC ";
        
        return $query;
    }
    
    //�ƎҖ�
    function getCompanycd()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 as VALUE, ";
        $query .= "     NAME1 as LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H313' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";
        
        
        return $query;
    }
}
?>
