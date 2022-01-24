<?php

require_once('for_php7.php');

class knjl392qQuery extends Query {
    
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
    
    //試験会場コンボ
    function getExamPlace()
    {
        $query  = " SELECT ";
        $query .= "     PLACECD as VALUE, ";
        $query .= "     PLACECD || '：' || PLACENAME_SHORT as LABEL ";
        $query .= " FROM ";
        $query .= "     SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     PLACECD ";
        
        return $query;
    }
    
    //駿中生の受験番号範囲取得
    function getExamArea()
    {
        $query  = " SELECT ";
        $query .= "     JUKEN_NO_FROM, ";
        $query .= "     JUKEN_NO_TO ";
        $query .= " FROM ";
        $query .= "     SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     PLACECD = '80' ";
        
        return $query;
    }
}
?>
