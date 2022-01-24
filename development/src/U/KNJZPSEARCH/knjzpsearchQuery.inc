<?php

require_once('for_php7.php');

class knjzpsearchQuery extends Query {
    
    function getNendo($from, $to)
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
    
    function getMockName($year, $gakunen, $gyousya, $analysis, $prgid)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.MOCKCD, ";
        $query .= "    t1.MOSI_DATE, ";
        $query .= "    t1.MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "    MOCK_MST t1 ";
        if($prgid != "knjz650"){
            $query .= "    left join MOCK_RANK_RANGE_DAT t2 on t1.MOCKCD = t2.MOCKCD ";
            $query .= "    left join MOCK_CSV_SCORE_FIELD_DAT t3 on t1.MOCKCD = t3.MOCKCD and t2.YEAR = t3.YEAR ";
            $query .= " WHERE ";
            $query .= "    t2.YEAR = '".$year."' AND ";
            $query .= "    t1.COMPANYCD = '0000000".$gyousya."' ";
            if($analysis != 1){
                $query .= " AND ";
                $query .= "    t2.DEVIATION >= '50' ";
            }
            $query .= " AND ";
            $query .= "    t3.GRADE = '".$gakunen."' ";
        }else{
            $query .= "    left join MOCK_SCHOOL_COMP_DAT t2 on t1.MOCKCD = t2.MOCKCD ";
            $query .= " WHERE ";
            $query .= "    t2.YEAR = '".$year."' AND ";
            $query .= "    t1.COMPANYCD = '0000000".$gyousya."' AND ";
            $query .= "    t2.GRADE = '".$gakunen."'  ";
            if($analysis != 1){
                $query .= "    AND ";
                $query .= "    (t2.SIMPLE_50 IS NOT NULL OR ";
                $query .= "     t2.SIMPLE_55 IS NOT NULL OR ";
                $query .= "     t2.SIMPLE_60 IS NOT NULL OR ";
                $query .= "     t2.SIMPLE_65 IS NOT NULL OR ";
                $query .= "     t2.SIMPLE_70 IS NOT NULL OR ";
                $query .= "     t2.SIMPLE_75 IS NOT NULL OR ";
                $query .= "     t2.SIMPLE_80 IS NOT NULL) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "    t1.MOSI_DATE ";
        
        return $query;
    }
}
?>
