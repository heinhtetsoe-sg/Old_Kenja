<?php

require_once('for_php7.php');

class knjh452Query extends Query {
    
    function getKyouka($mockcd, $request)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t2.MOCK_SUBCLASS_CD, ";
        $query .= "    t2.SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 left join MOCK_SUBCLASS_MST t2 on t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        //$query .= "    t1.MOCKCD = '".$mockcd."' AND ";
        $query .= "    t1.MOCKCD IN ('".$mockcd."') AND ";
        $query .= "    t2.MOCK_SUBCLASS_CD != '900000' AND ";
        $query .= "    t1.DEVIATION IS NOT NULL AND ";
        if($request["ANALYSIS"] != 1){      //(��)
            $query .= "    t1.DEVIATION >= '50' ";
            $query .= " AND ";
        }
        $query .= "    t1.RANK_RANGE = 1 AND ";
        if($request["KATA"] == "2"){
            $query .= "    t2.SUBCLASS_DIV = '1' ";
        }else{
            $query .= "    t2.SUBCLASS_DIV IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "    t2.MOCK_SUBCLASS_CD ";
        
        return $query;
    }
    
    //���擾
    function getPeriod($properties, $from, $to)
    {
        $query  = " SELECT ";
        $query .= "    YEAR, ";
        $query .= "    PRESENT_EST ";
        $query .= " FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR BETWEEN '".$from."' AND '".$to."' ";
        if($properties["useSchool_KindField"] == "1"){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
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
    function getData($request, $mockcd, $subclasscd, $yearfrom, $yearto, $devfrom, $devto, $min, $max)
    {
        $query  = " SELECT ";
        $query .= "    t1.YEAR, ";
        $query .= "    COUNT(t1.SCHREGNO) as COUNT ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){  //���O����
            $query .= "    left join SCHREG_REGD_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = t2.YEAR and t2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    left join NAME_MST t3 on t2.COURSECODE = t3.NAMECD2 and t3.NAMECD1 = 'H316' ";
        }
        $query .= " WHERE ";
        $query .= "    substr(t1.mockcd,6,4) = '".$mockcd."' AND ";
        $query .= "    t1.year between '".$yearfrom."' AND '".$yearto."' ";
        $query .= " AND ";
        $query .= "    t1.RANK_RANGE = 1 AND ";
        if($devfrom != $max && $devfrom != $min){
            $query .= "    t1.DEVIATION >= ".$devfrom." AND t1.DEVIATION < ".$devto." ";
        }else if($devfrom == $max){
            $query .= "    t1.DEVIATION >= ".$devfrom;
        }else{
            $query .= "    t1.DEVIATION < ".$devto;
        }
        $query .= " AND ";
        $query .= "    t1.MOCK_SUBCLASS_CD = '".$subclasscd."' ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){
            $query .= "    AND t3.NAME1 IS NULL ";
        }
        $query .= " GROUP BY ";
        $query .= "    t1.YEAR ";
        $query .= " ORDER BY ";
        $query .= "    t1.YEAR DESC ";
        

        return $query;
    }
    
    function getAverage($request, $mockcd, $subclasscd, $yearfrom, $yearto)
    {
        $query  = " SELECT ";
        $query .= "    t1.YEAR, ";
        $query .= "    AVG(t1.DEVIATION) AS AVG ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){  //���O����
            $query .= "    left join SCHREG_REGD_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = t2.YEAR and t2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    left join NAME_MST t3 on t2.COURSECODE = t3.NAMECD2 and t3.NAMECD1 = 'H316' ";
        }
        $query .= " WHERE ";
        $query .= "    substr(t1.mockcd,6,4) = '".$mockcd."' AND ";
        $query .= "    t1.year between '".$yearfrom."' AND '".$yearto."' ";
        $query .= " AND ";
        $query .= "    t1.RANK_RANGE = 1 AND ";
        $query .= "    t1.MOCK_SUBCLASS_CD = '".$subclasscd."' ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){
            $query .= "    AND t3.NAME1 IS NULL ";
        }
        $query .= " GROUP BY ";
        $query .= "    t1.YEAR ";
        $query .= " ORDER BY ";
        $query .= "    t1.YEAR DESC ";
        
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
