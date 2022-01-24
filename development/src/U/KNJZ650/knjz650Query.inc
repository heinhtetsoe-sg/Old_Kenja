<?php

require_once('for_php7.php');

class knjz650Query extends Query {
    
    function getKyouka($mockcd, $kata)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t2.MOCK_SUBCLASS_CD, ";
        $query .= "    case when t2.SUBCLASS_ABBV IS NOT NULL then t2.SUBCLASS_ABBV ";
        $query .= "         when t2.SUBCLASS_ABBV IS NULL then t2.SUBCLASS_NAME ";
        $query .= "    end as SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "    MOCK_SCHOOL_COMP_DAT t1 left join MOCK_SUBCLASS_MST t2 on t1.SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "    t1.MOCKCD = '".$mockcd."' AND ";
        if($kata == "2"){
            $query .= "    t2.SUBCLASS_DIV = '1' ";
        }else{
            $query .= "    t2.SUBCLASS_DIV IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "    t2.MOCK_SUBCLASS_CD ";
        
        return $query;
    }
    
    //�w�Z���擾
    function getGakName($mockcd)
    {
        $query  = " SELECT ";
        $query .= "    GAKKONAME, ";
        $query .= "    MAX(ROW_NO) as ROW_NO ";
        $query .= " FROM ";
        $query .= "    MOCK_SCHOOL_COMP_DAT ";
        $query .= " WHERE ";
        $query .= "    MOCKCD = '".$mockcd."' ";
        $query .= " GROUP BY ";
        $query .= "    GAKKONAME ";
        $query .= " ORDER BY ";
        $query .= "    ROW_NO ";
        
        return $query;
    }
    
    //���Ȗ��擾
    function getKyoukamei($subclasscd)
    {
        $query  = " SELECT ";
        $query .= "    SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "    MOCK_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "    MOCK_SUBCLASS_CD = '".$subclasscd."' ";
        
        return $query;
    }
    
    //�f�[�^�擾
    function getData($mockcd, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "    GAKKONAME, ";
        $query .= "    SUBCLASS_CD, ";
        $query .= "    AVG_DEVIATION, ";
        $query .= "    SIMPLE_80, ";
        $query .= "    case when SIMPLE_75 IS NULL AND SIMPLE_70 IS NULL then NULL ";
        $query .= "         when SIMPLE_75 IS NULL AND SIMPLE_70 IS NOT NULL then SIMPLE_70 ";
        $query .= "         when SIMPLE_75 IS NOT NULL AND SIMPLE_70 IS NULL then SIMPLE_75 ";
        $query .= "         else (SIMPLE_75 + SIMPLE_70) end as SIMPLE_70, ";
        $query .= "    case when SIMPLE_65 IS NULL AND SIMPLE_60 IS NULL then NULL ";
        $query .= "         when SIMPLE_65 IS NULL AND SIMPLE_60 IS NOT NULL then SIMPLE_60 ";
        $query .= "         when SIMPLE_65 IS NOT NULL AND SIMPLE_60 IS NULL then SIMPLE_65 ";
        $query .= "         else (SIMPLE_65 + SIMPLE_60) end as SIMPLE_60, ";
        $query .= "    case when SIMPLE_55 IS NULL AND SIMPLE_50 IS NULL then NULL ";
        $query .= "         when SIMPLE_55 IS NULL AND SIMPLE_50 IS NOT NULL then SIMPLE_50 ";
        $query .= "         when SIMPLE_55 IS NOT NULL AND SIMPLE_50 IS NULL then SIMPLE_55 ";
        $query .= "         else (SIMPLE_55 + SIMPLE_50) end as SIMPLE_50, ";
        $query .= "    case when SIMPLE_45 IS NULL AND SIMPLE_40 IS NULL then NULL ";
        $query .= "         when SIMPLE_45 IS NULL AND SIMPLE_40 IS NOT NULL then SIMPLE_40 ";
        $query .= "         when SIMPLE_45 IS NOT NULL AND SIMPLE_40 IS NULL then SIMPLE_45 ";
        $query .= "         else (SIMPLE_45 + SIMPLE_40) end as SIMPLE_40, ";
        $query .= "    case when SIMPLE_35 IS NULL AND SIMPLE_30 IS NULL then NULL ";
        $query .= "         when SIMPLE_35 IS NULL AND SIMPLE_30 IS NOT NULL then SIMPLE_30 ";
        $query .= "         when SIMPLE_35 IS NOT NULL AND SIMPLE_30 IS NULL then SIMPLE_35 ";
        $query .= "         else (SIMPLE_35 + SIMPLE_30) end as SIMPLE_30, ";
        $query .= "    SIMPLE_LOW as SIMPLE_20, ";
        $query .= "    TOTAL_80, ";
        $query .= "    TOTAL_70, ";
        $query .= "    TOTAL_60, ";
        $query .= "    TOTAL_50, ";
        $query .= "    TOTAL_40, ";
        $query .= "    TOTAL_30, ";
        $query .= "    TOTAL_LOW as TOTAL_20 ";
        $query .= " FROM ";
        $query .= "    MOCK_SCHOOL_COMP_DAT ";
        $query .= " WHERE ";
        $query .= "    MOCKCD = '".$mockcd."' AND ";
        $query .= "    SUBCLASS_CD = '".$subclasscd."' ";
        $query .= " ORDER BY ";
        $query .= "    ROW_NO ";
        
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
