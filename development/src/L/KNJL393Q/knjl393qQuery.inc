<?php

require_once('for_php7.php');

class knjl393qQuery extends Query {
    
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
    
    //中学校コンボ
    function getExamFinschool()
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD as VALUE, ";
        $query .= "     FINSCHOOLCD || '：' || FINSCHOOL_NAME as LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOL_TYPE = '3' AND ";
        $query .= "     FINSCHOOLCD IN (SELECT DISTINCT ";
        $query .= "                         SCHOOLCD ";
        $query .= "                     FROM ";
        $query .= "                         SAT_APP_FORM_MST ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOLCD ";
        
        return $query;
    }
    
    //団体コンボ
    function getExamGroup()
    {
        $query  = " SELECT ";
        $query .= "     GROUPCD as VALUE, ";
        $query .= "     GROUPCD || '：' || GROUPNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     GROUPCD IN (SELECT DISTINCT ";
        $query .= "                     GROUPCD ";
        $query .= "                 FROM ";
        $query .= "                     SAT_APP_FORM_MST ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD ";
        
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
}
?>
