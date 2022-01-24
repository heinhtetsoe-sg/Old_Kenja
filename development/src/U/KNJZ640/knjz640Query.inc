<?php

require_once('for_php7.php');

class knjz640Query extends Query {
    
    function getKyouka($mockcd, $kata)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t2.MOCK_SUBCLASS_CD, ";
        $query .= "    t2.SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 left join MOCK_SUBCLASS_MST t2 on t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "    t1.MOCKCD = '".$mockcd."' AND ";
        $query .= "    t1.DEVIATION IS NOT NULL AND ";
        $query .= "    t1.RANK_RANGE = 1 AND ";
        if($kata == "2"){
            $query .= "    t2.SUBCLASS_DIV = '1' ";
        }else{
            $query .= "    t2.SUBCLASS_DIV IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "    t2.MOCK_SUBCLASS_CD ";
        
        return $query;
    }
    
    
    function getDevData($request, $kyouka,  $hensati_from = "", $hensati_to = "")
    {
        $query  = " SELECT ";
        $query .= "    MOCK_SUBCLASS_CD, ";
        $query .= "    COUNT(*) as COUNT ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT ";
        $query .= " WHERE ";
        $query .= "    MOCKCD = '".$request["MOCKCD"]."' AND ";
        $query .= "    RANK_RANGE = 1 AND ";
        
        if($hensati_from != "" && $hensati_to != ""){
            $query .= "    DEVIATION >= '".$hensati_from."' AND DEVIATION < '".$hensati_to."' AND ";
        }else if($hensati_to == ""){
            $query .= "    DEVIATION >= '".$hensati_from."' AND ";
        }else if($hensati_from == ""){
            $query .= "    DEVIATION <= '".$hensati_to."' AND ";
        }
        
        $query .= "    MOCK_SUBCLASS_CD IN ('".$kyouka."') ";
        $query .= " GROUP BY ";
        $query .= "    MOCK_SUBCLASS_CD ";
        
        return $query;
    }
    
    
    function getAverage($request, $kyouka)
    {
        $query  = " SELECT ";
        $query .= "    MOCK_SUBCLASS_CD, ";
        $query .= "    AVG(DEVIATION) AS AVG ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT ";
        $query .= " WHERE ";
        $query .= "    MOCKCD = '".$request["MOCKCD"]."' ";
        $query .= " AND ";
        $query .= "    RANK_RANGE = 1 ";
        $query .= " AND ";
        $query .= "    MOCK_SUBCLASS_CD IN ('".$kyouka."') ";
        
        if($request["ANALYSIS"] != 1){      //(��)
            $query .= " AND ";
            $query .= "    DEVIATION >= '50' ";
        }
        
        $query .= " GROUP BY ";
        $query .= "    MOCK_SUBCLASS_CD ";
        $query .= " ORDER BY ";
        $query .= "    MOCK_SUBCLASS_CD ";
        
        return $query;
    }
}
?>
