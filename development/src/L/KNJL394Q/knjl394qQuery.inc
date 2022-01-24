<?php

require_once('for_php7.php');

class knjl394qQuery extends Query {
    
    //試験日取得
    function getExamDate()
    {
        $query  = " SELECT ";
        $query .= "     EXAM_DATE ";
        $query .= " FROM ";
        $query .= "     SAT_INFO_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
}
?>
