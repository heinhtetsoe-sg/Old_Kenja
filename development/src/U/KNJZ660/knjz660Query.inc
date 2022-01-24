<?php

require_once('for_php7.php');

class knjz660Query extends Query {
    
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
        if($request["ANALYSIS"] != 1){      //(��)
            $query .= "    t1.DEVIATION >= '50' ";
            $query .= " AND ";
        }
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
    
    //�w�Z���擾
    function getPeriod($from, $to)
    {
        $query  = " SELECT ";
        $query .= "    YEAR, ";
        $query .= "    PRESENT_EST ";
        $query .= " FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR BETWEEN '".$from."' AND '".$to."' ";
        $query .= " ORDER BY ";
        $query .= "    YEAR DESC ";
        
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
    function getData($mockcd, $subclasscd, $yearfrom, $yearto, $devfrom, $devto)
    {
        $query  = " SELECT ";
        $query .= "    YEAR, ";
        $query .= "    COUNT(*) as COUNT ";
        $query .= " FROM ";
        $query .= "    mock_rank_range_dat ";
        $query .= " WHERE ";
        $query .= "    substr(mockcd,6,4) = '".$mockcd."' AND ";
        $query .= "    year between '".$yearfrom."' AND '".$yearto."' ";
        $query .= " AND ";
        $query .= "    RANK_RANGE = 1 AND ";
        if($devfrom != 70 && $devfrom != 20){
            $query .= "    DEVIATION >= ".$devfrom." AND DEVIATION < ".$devto." ";
        }else if($devfrom == 70){
            $query .= "    DEVIATION >= ".$devfrom;
        }else{
            $query .= "    DEVIATION < ".$devto;
        }
        $query .= " AND ";
        $query .= "    MOCK_SUBCLASS_CD = '".$subclasscd."' ";
        $query .= " GROUP BY ";
        $query .= "    YEAR ";
        $query .= " ORDER BY ";
        $query .= "    YEAR DESC ";
        

        return $query;
    }
    
    function getAverage($mockcd, $subclasscd, $yearfrom, $yearto)
    {
        $query  = " SELECT ";
        $query .= "    YEAR, ";
        $query .= "    AVG(DEVIATION) AS AVG ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT ";
        $query .= " WHERE ";
        $query .= "    substr(mockcd,6,4) = '".$mockcd."' AND ";
        $query .= "    year between '".$yearfrom."' AND '".$yearto."' ";
        $query .= " AND ";
        $query .= "    RANK_RANGE = 1 AND ";
        $query .= "    MOCK_SUBCLASS_CD = '".$subclasscd."' ";
        $query .= " GROUP BY ";
        $query .= "    YEAR ";
        $query .= " ORDER BY ";
        $query .= "    YEAR DESC ";
        
        return $query;
    }
}
?>
