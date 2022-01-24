<?php

require_once('for_php7.php');

class knjl375qQuery extends Query {
    
    //対象取得
    function getData($kyouka, $field)
    {
        if($field["KAISU"] != "1"){
            $kaisu = "2";
        }else{
            $kaisu = "";
        }
        
        $query  = " SELECT ";
        $query .= "    t1.SAT_NO, ";
        $query .= "    t2.NAME1, ";
        $query .= "    t1.SCORE_".$kyouka[$field["KAMOKU"]].$kaisu.", ";
        $query .= "    t1.ABSENCE_".$kyouka[$field["KAMOKU"]]." ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST t2 ";
        $query .= "    left join SAT_EXAM_DAT t1 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ";
        $query .= " WHERE ";
        $query .= "    t2.ABSENCE != 0 ";
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        if($field["KUBUN"] == "2"){
            $query .= " AND ";
            $query .= "    t2.INOUT_KUBUN != 4 ";
        }else if($field["KUBUN"] == "3"){
            $query .= " AND ";
            $query .= "    t2.INOUT_KUBUN = 4 ";
        }
        $query .= " ORDER BY ";
        $query .= "    t1.SAT_NO ";
        
        return $query;
    }
    
    //SAT_EXAM_DATに対象データかあるか
    function checkSatNo($satno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SAT_EXAM_DAT ";
        $query .= " WHERE ";
        $query .= "     SAT_NO = '".$satno."' AND ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
}
?>
