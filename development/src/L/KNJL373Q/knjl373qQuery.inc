<?php

require_once('for_php7.php');

class knjl373qQuery extends Query {
    
    //会場コンボ
    function getPlace()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t2.PLACECD, ";
        $query .= "    t3.PLACENAME_SHORT ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_DAT t1  ";
        $query .= "    left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR  ";
        $query .= "    left join SAT_EXAM_PLACE_DAT t3 on t2.PLACECD = t3.PLACECD and t1.YEAR = t3.YEAR ";
        $query .= " WHERE ";
        $query .= "    t2.PLACECD IS NOT NULL ";
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    t2.PLACECD ";
        
        return $query;
    }
    
    
    //データ取得
    function getData($field)
    {
        $query  = " SELECT ";
        $query .= "    t1.SAT_NO, ";
        $query .= "    t1.NAME1, ";
        $query .= "    t1.ABSENCE, ";
        $query .= "    t1.PLACECD, ";
        $query .= "    t1.SCHOOLCD, ";
        $query .= "    t1.GROUPCD, ";
        $query .= "    t2.PLACENAME_SHORT, ";
        $query .= "    t3.FINSCHOOL_NAME, ";
        $query .= "    t4.GROUPNAME ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST t1  ";
        $query .= "    left join SAT_EXAM_PLACE_DAT t2 on t1.PLACECD = t2.PLACECD and t1.YEAR = t2.YEAR  ";
        $query .= "    left join FINSCHOOL_MST t3 on t1.SCHOOLCD = t3.FINSCHOOLCD  ";
        $query .= "    left join SAT_GROUP_DAT t4 on t1.GROUPCD = t4.GROUPCD and t1.YEAR = t4.YEAR ";
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        if($field["RADIO"] != "1"){
            $query .= " AND ";
            $query .= "    t1.ABSENCE = '0' ";
        }
        if($field["PLACECD"] != ""){
            $query .= " AND ";
            $query .= "    t1.PLACECD = '".$field["PLACECD"]."' ";
        }
        $query .= " ORDER BY  ";
        $query .= "    t1.SAT_NO ";
        //$query .= " fetch first 1 rows only";
        
        return $query;
    }
    
    //マークリーダ用名簿
    function getCsvData()
    {
        $query  = " SELECT ";
        $query .= "     case when GRADUATION = '09' then '3'  ";
        $query .= "          when GRADUATION = '08' then '2'  ";
        $query .= "          when GRADUATION = '07' then '1'  ";
        $query .= "          else NULL  ";
        $query .= "     end as GRADE, ";
        $query .= "     INOUT_KUBUN, ";
        $query .= "     SAT_NO, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     ABSENCE != '0' ";
        $query .= " ORDER BY ";
        $query .= "     INOUT_KUBUN, ";
        $query .= "     SAT_NO ";
        
        return $query;
    }
}
?>
