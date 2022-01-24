<?php

require_once('for_php7.php');
class knjl397qQuery extends Query {
    
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
    
    //受験区分取得
    function getKubun()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L201' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        
        return $query;
    }
    
    //表示対象生徒取得
    function getListData($kubun, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.NAME1, ";
        $query .= "         t1.SCHOOLCD, ";
        $query .= "         t2.FINSCHOOL_NAME, ";
        $query .= "         t1.INOUT_KUBUN, ";
        $query .= "         t3.NAME1 as KUBUN_NAME, ";
        $query .= "         t4.SCORE_TOTAL ";
        $query .= "     FROM ";
        $query .= "         SAT_APP_FORM_MST t1  ";
        $query .= "         left join FINSCHOOL_MST t2 on t1.SCHOOLCD = t2.FINSCHOOLCD and t2.FINSCHOOL_TYPE = '3' ";
        $query .= "         left join NAME_MST t3 on t1.INOUT_KUBUN = t3.NAMECD2 and t3.NAMECD1 = 'L201' ";
        $query .= "         left join SAT_EXAM_DAT t4 on t1.SAT_NO = t4.SAT_NO AND t1.YEAR = t4.YEAR ";
        $query .= "     WHERE  ";
        $query .= "         t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND  ";
        $query .= "         t4.JUDGE_SAT = '1' ";
        if($kubun != ""){
            if($kubun == "1"){
                $query .= "     AND  ";
                $query .= "         (t1.INOUT_KUBUN = '".$kubun."' ";
                $query .= "     OR  ";
                $query .= "         t1.PREFCD = '19') ";
            } else {
                $query .= "     AND  ";
                $query .= "         t1.INOUT_KUBUN = '".$kubun."' ";
                $query .= "     AND  ";
                $query .= "         t1.PREFCD != '19' ";
            }
        }
        $query .= "     ORDER BY ";
        $query .= "         t1.INOUT_KUBUN, ";
        $query .= "         t1.SAT_NO ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
}
?>
