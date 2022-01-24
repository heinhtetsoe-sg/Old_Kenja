<?php

require_once('for_php7.php');

class knjz630Query extends Query {

    function getSchreg($year, $schreg)
    {
        $query  = " SELECT ";
        $query .= "    t1.SCHREGNO, ";
        $query .= "    t1.YEAR, ";
        $query .= "    t1.GRADE, ";
        $query .= "    t1.HR_CLASS, ";
        $query .= "    t1.ATTENDNO, ";
        $query .= "    t2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT t1 ";
        $query .= "    left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "    t1.SCHREGNO = '".$schreg."' AND ";
        $query .= "    t1.YEAR = '".$year."' AND ";
        $query .= "    t1.SEMESTER = '1' ";
        
        return $query;
    }
    
    function getMockName($gakuseki, $year, $gyousya)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.MOCKCD, ";
        $query .= "    t1.MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "    MOCK_MST t1 left join MOCK_RANK_RANGE_DAT t2 on t1.MOCKCD = t2.MOCKCD ";
        $query .= " WHERE ";
        $query .= "    t2.SCHREGNO = '".$gakuseki."' ";
        $query .= " AND ";
        $query .= "    t2.YEAR = '".$year."' ";
        if($gyousya != "0"){
            $query .= " AND ";
            $query .= "    COMPANYCD = '0000000".$gyousya."' ";
        }else{
            $query .= " AND ";
            $query .= "    COMPANYCD IN ('00000001','00000002') ";
        }
        
        return $query;
    }
    
    function getMock($mockcd)
    {
        $query  = " SELECT ";
        $query .= "    MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "    MOCK_MST ";
        $query .= " WHERE ";
        $query .= "    MOCKCD = '".$mockcd."' ";
        
        return $query;
    }
    
    function getKyouka($gakuseki, $year, $mockcd, $kata)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t2.MOCK_SUBCLASS_CD, ";
        $query .= "    case when t2.SUBCLASS_ABBV IS NOT NULL then t2.SUBCLASS_ABBV ";
        $query .= "         when t2.SUBCLASS_ABBV IS NULL then t2.SUBCLASS_NAME ";
        $query .= "    end as SUBCLASS_ABBV, ";
        $query .= "    t1.DEVIATION ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 left join MOCK_SUBCLASS_MST t2 on t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "    t1.SCHREGNO = '".$gakuseki."' AND ";
        $query .= "    t1.YEAR = '".$year."'  AND ";
        $query .= "    t1.MOCKCD = '".$mockcd."' AND ";
        $query .= "    t1.DEVIATION IS NOT NULL AND ";
        $query .= "    t1.RANK_RANGE = 1 AND ";
        if($kata == "1"){
            $query .= "    t2.SUBCLASS_DIV = '1' ";
        }else{
            $query .= "    t2.SUBCLASS_DIV IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "    t2.MOCK_SUBCLASS_CD ";
        
        return $query;
    }
    
}
?>
