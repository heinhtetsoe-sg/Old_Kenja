<?php

require_once('for_php7.php');

class knjh451Query extends Query {
    
    function getKyouka($mockcd, $kata)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t2.MOCK_SUBCLASS_CD, ";
        $query .= "    t2.SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 left join MOCK_SUBCLASS_MST t2 on t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "    t1.MOCKCD = '".$mockcd."' AND ";
        $query .= "    t2.MOCK_SUBCLASS_CD != '900000' AND ";
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
        $query .= "    t1.MOCK_SUBCLASS_CD, ";
        $query .= "    COUNT(t1.SCHREGNO) as COUNT ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){  //���O����
            $query .= "    left join SCHREG_REGD_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = t2.YEAR and t2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    left join NAME_MST t3 on t2.COURSECODE = t3.NAMECD2 and t3.NAMECD1 = 'H316' ";
        }
        $query .= " WHERE ";
        $query .= "    t1.MOCKCD = '".$request["MOCKCD"]."' AND ";
        $query .= "    t1.RANK_RANGE = 1 AND ";
        
        if($hensati_from != "" && $hensati_to != ""){
            $query .= "    t1.DEVIATION >= '".$hensati_from."' AND t1.DEVIATION < '".$hensati_to."' AND ";
        }else if($hensati_to == ""){
            $query .= "    t1.DEVIATION >= '".$hensati_from."' AND ";
        }else if($hensati_from == ""){
            $query .= "    t1.DEVIATION < '".$hensati_to."' AND ";
        }
        
        $query .= "    t1.MOCK_SUBCLASS_CD IN ('".$kyouka."') ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){
            $query .= "    AND t3.NAME1 IS NULL ";
        }
        $query .= " GROUP BY  ";
        $query .= "    t1.MOCK_SUBCLASS_CD ";
        
        
        return $query;
    }
    
    
    function getAverage($request, $kyouka)
    {
        $query  = " SELECT ";
        $query .= "    t1.MOCK_SUBCLASS_CD, ";
        $query .= "    AVG(t1.DEVIATION) AS AVG ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){  //���O����
            $query .= "    left join SCHREG_REGD_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = t2.YEAR and t2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    left join NAME_MST t3 on t2.COURSECODE = t3.NAMECD2 and t3.NAMECD1 = 'H316' ";
        }
        $query .= " WHERE ";
        $query .= "    t1.MOCKCD = '".$request["MOCKCD"]."' ";
        $query .= " AND ";
        $query .= "    t1.RANK_RANGE = 1 ";
        $query .= " AND ";
        $query .= "    t1.MOCK_SUBCLASS_CD IN ('".$kyouka."') ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){
            $query .= "    AND t3.NAME1 IS NULL ";
        }
        
        $query .= " GROUP BY ";
        $query .= "    t1.MOCK_SUBCLASS_CD ";
        $query .= " ORDER BY ";
        $query .= "    t1.MOCK_SUBCLASS_CD ";
        
        return $query;
    }
    
    //CSV�o�͗p
    function getJogai($jogai)
    {
        $query  = " SELECT ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'H315' ";
        $query .= " AND ";
        $query .= "    NAMECD2 = '".$jogai."' ";
        $query .= " ORDER BY ";
        $query .= "    NAMECD2 ";
        
        return $query;
    }
    function getNendo($year)
    {
        $query  = " SELECT ";
        $query .= "    PRESENT_EST ";
        $query .= " FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "    YEAR DESC ";
        
        return $query;
    }
    
    //�͎�����
    function getMockName($mockcd)
    {
        $query  = " SELECT ";
        $query .= "     t1.SINROSIDOU_MOSI_NAME, ";
        $query .= "     t2.NAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_MST t1  ";
        $query .= "     left join NAME_MST t2 on t1.COMPANYCD = t2.NAMESPARE1 ";
        $query .= " WHERE ";
        $query .= "     t1.MOCKCD = '".$mockcd."' ";
        
        return $query;
    }
}
?>
