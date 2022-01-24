<?php

require_once('for_php7.php');

class knjl388qQuery extends Query {
    
    //受験番号帯のデータ取得
    function getExamNo()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     INOUT_KUBUN = '4' AND ";
        $query .= "     SEND_KUBUN = '1' AND  ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     PLACECD, ";
        $query .= "     IND_KUBUN ";
        
        return $query;
    }
    
    //学年
    function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L205' ";
        
        return $query;
    }
    
    //中学コード
    function getFinschool()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOL_TYPE = '3' AND ";
        $query .= "     FINSCHOOL_PREF_CD = '48' ";
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOLCD ";
        
        return $query;
    }
    
    //会場コード
    function getPlace()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     PLACECD ";
        
        return $query;
    }
    
    //団体コード
    function getGroup()
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD ";
        
        return $query;
    }
    
    //SAT_APP_FORM_MST削除
    function delSatAppForm()
    {
        $query  = " DELETE FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     PREFCD = '48'  AND ";
        $query .= "     INOUT_KUBUN = '4' AND ";
        $query .= "     SEND_KUBUN = '1' ";
        
        return $query;
    }
    //SAT_EXAM_DAT削除
    function deleteExam()
    {
        $query  = " DELETE FROM ";
        $query .= "     SAT_EXAM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SAT_NO in ( ";
        $query .= "                 SELECT ";
        $query .= "                     SAT_NO ";
        $query .= "                 FROM ";
        $query .= "                     SAT_APP_FORM_MST ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     PREFCD = '48'  AND ";
        $query .= "                     INOUT_KUBUN = '4' AND ";
        $query .= "                     SEND_KUBUN = '1' ";
        $query .= "                 ) ";
        
        return $query;
    }
}
?>
