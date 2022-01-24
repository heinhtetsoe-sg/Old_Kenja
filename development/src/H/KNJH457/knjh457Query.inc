<?php

require_once('for_php7.php');

class knjh457Query extends Query {
    
    function getSyubetu($companycd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t2.NAMECD2, ";
        $query .= "    t2.NAME1 ";
        $query .= " FROM ";
        $query .= "    MOCK_MST t1 left join NAME_MST t2 on t1.MOSI_DIV = t2.NAMECD2 and t2.NAMECD1 = 'H314' ";
        $query .= " WHERE ";
        $query .= "    t1.COMPANYCD = '".$companycd."' ";
        
        return $query;
    }
    
    
    function getKyouka($year,$gyousya, $syubetu, $kata, $minhensa)
    {
        $first = $year;
        $second = (int)$year+1;
        $third = (int)$year+2;
        
        $hensati = (int)$minhensa + 10;
        
        $gyousyacd = substr($gyousya,-1);
        
        $query  = " SELECT DISTINCT ";
        $query .= "    t2.MOCK_SUBCLASS_CD, ";
        $query .= "    t2.SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 left join MOCK_SUBCLASS_MST t2 on t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ";
        $query .= "    left join MOCK_MST t3 on t1.MOCKCD = t3.MOCKCD ";
        $query .= " WHERE ";
        $query .= "    t3.COMPANYCD = '".$gyousya."' AND";
        if($syubetu != "0"){
            $query .= "    t3.MOSI_DIV = '".$syubetu."' AND";
        }
        $query .= "    t1.DEVIATION IS NOT NULL AND";
        //$query .= "    t1.DEVIATION >= '".$hensati."' AND";
        $query .= "    t1.RANK_RANGE = 1 AND";
        $query .= "    t2.MOCK_SUBCLASS_CD != '900000' AND ";
        $query .= "        ( ";
        $query .= "         (year = '".$third."' AND ";
        $query .= "          substr(t1.mockcd,6,1) = '".$gyousyacd."' AND ";
        $query .= "          substr(t1.mockcd,7,1) = '3') ";
        $query .= "         or ";
        $query .= "         (year = '".$second."' AND ";
        $query .= "          substr(t1.mockcd,6,1) = '".$gyousyacd."' AND ";
        $query .= "          substr(t1.mockcd,7,1) = '2') ";
        $query .= "         or ";
        $query .= "         (year = '".$first."' AND ";
        $query .= "          substr(t1.mockcd,6,1) = '".$gyousyacd."' AND ";
        $query .= "          substr(t1.mockcd,7,1) = '1') ";
        $query .= "         ) ";
        $query .= "    AND ";
        if($kata == "2"){
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
        $query .= "    YEAR ";
        
        return $query;
    }
    
    function getMockName($request, $year, $gyousya, $syubetu, $subclasscd, $deviation)
    {
        $first = (int)$year;
        $second = (int)$year+1;
        $third = (int)$year+2;
        
        //�O���5�N�ȓ��ɓ����Ă��狳�ȕ\������悤�ɂ�����
        //�N�x�𕶎��A������
        $cnm = "";
        $fyear = "";
        for($i=$first-2;$i<$first+3;$i++){
            $fyear .= $cnm.$i;
            $cnm = "','";
        }
        $cnm = "";
        $syear = "";
        for($i=$second-2;$i<$second+3;$i++){
            $syear .= $cnm.$i;
            $cnm = "','";
        }
        $cnm = "";
        $tyear = "";
        for($i=$third-2;$i<$third+3;$i++){
            $tyear .= $cnm.$i;
            $cnm = "','";
        }

        $query  = " SELECT DISTINCT ";
        $query .= "     b1.MOCKCD, ";
        $query .= "     b1.GRADE, ";
        $query .= "     b1.SINROSIDOU_MOSI_NAME, ";
        $query .= "     b2.MOSI_DATE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         substr(t1.MOCKCD,6,4) as MOCKCD, ";
        $query .= "         t1.MOCK_SUBCLASS_CD, ";
        $query .= "         t1.GRADE, ";
        $query .= "         t1.COUNT, ";
        $query .= "         t2.MOCKNAME1, ";
        $query .= "         t2.SINROSIDOU_MOSI_NAME ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             a1.mockcd, ";
        $query .= "             a1.mock_subclass_cd, ";
        $query .= "             substr(a1.mockcd,7,1) as grade, ";
        $query .= "             COUNT(a1.SCHREGNO) as count ";
        $query .= "         FROM ";
        $query .= "             MOCK_RANK_RANGE_DAT a1 ";
        $query .= "         WHERE ";
        $query .= "        ( ";
        $query .= "         (a1.year in ('".$tyear."') AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '3') ";
        $query .= "         or ";
        $query .= "         (a1.year in ('".$syear."') AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '2') ";
        $query .= "         or ";
        $query .= "         (a1.year in ('".$fyear."') AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '1') ";
        $query .= "         ) ";
        $query .= "    AND ";
        $query .= "        a1.mock_subclass_cd = '".$subclasscd."' ";
        if($deviation != ""){
            $query .= "    AND ";
            $query .= "        a1.DEVIATION >= '".$deviation."' ";
        }
        $query .= "         GROUP BY ";
        $query .= "             a1.mockcd, ";
        $query .= "             a1.mock_subclass_cd ";
        $query .= "         ORDER BY ";
        $query .= "             a1.mockcd desc, ";
        $query .= "             a1.mock_subclass_cd ";
        $query .= "         ) as t1 left join mock_mst t2 on t1.mockcd = t2.mockcd ";
        $query .= "     ORDER BY ";
        $query .= "         mosi_date desc ";
        $query .= "     ) b1 left join (SELECT ";
        $query .= "                         substr(MOCKCD,6,4) as MOCKCD, ";
        $query .= "                         MOSI_DATE ";
        $query .= "                     FROM ";
        $query .= "                         mock_mst ";
        $query .= "                     WHERE ";
        $query .= "                         ( ";
        $query .= "                          (substr(mockcd,2,4) = '".$first."' and substr(mockcd,7,1) = '1') OR ";
        $query .= "                          (substr(mockcd,2,4) = '".$second."' and substr(mockcd,7,1) = '2') OR ";
        $query .= "                          (substr(mockcd,2,4) = '".$third."' and substr(mockcd,7,1) = '3') ";
        $query .= "                         ) AND ";
        $query .= "                         companycd = '0000000".$gyousya."' ";
        $query .= "                     ORDER BY ";
        $query .= "                         mosi_date desc ";
        $query .= "                     ) b2 on b1.MOCKCD = b2.MOCKCD ";
        $query .= " ORDER BY ";
        $query .= "     GRADE desc, ";
        $query .= "     mosi_date desc ";

        return $query;
    }
    //$from,$to�������ĂȂ�������͎������擾���邾��
    function getMosi($request, $year, $gyousya, $syubetu, $subclasscd, $deviation)
    {
        $first = $year;
        $second = (int)$year+1;
        $third = (int)$year+2;
        
        $query  = " SELECT ";
        $query .= "    substr(t1.MOCKCD,6,4) as MOCKCD, ";
        $query .= "    t1.MOCK_SUBCLASS_CD, ";
        $query .= "    t1.GRADE, ";
        $query .= "    t1.COUNT, ";
        $query .= "    t2.MOCKNAME1, ";
        $query .= "    t2.SINROSIDOU_MOSI_NAME ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        a1.mockcd, ";
        $query .= "        a1.mock_subclass_cd, ";
        $query .= "        substr(a1.mockcd,7,1) as grade, ";
        $query .= "        COUNT(a1.SCHREGNO) as count ";
        $query .= "    FROM ";
        $query .= "        MOCK_RANK_RANGE_DAT a1 ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){  //���O����
            $query .= "    left join SCHREG_REGD_DAT a2 on a1.SCHREGNO = a2.SCHREGNO and a1.YEAR = a2.YEAR and a2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    left join NAME_MST a3 on a2.COURSECODE = a3.NAMECD2 and a3.NAMECD1 = 'H316' ";
        }
        $query .= "    WHERE ";
        $query .= "        ( ";
        $query .= "         (a1.year = '".$third."' AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '3') ";
        $query .= "         or ";
        $query .= "         (a1.year = '".$second."' AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '2') ";
        $query .= "         or ";
        $query .= "         (a1.year = '".$first."' AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '1') ";
        $query .= "         ) ";
        $query .= "    AND ";
        $query .= "        a1.mock_subclass_cd = '".$subclasscd."' ";
        $query .= "    AND ";
        $query .= "        a1.rank_range = '1' ";
        if($deviation != ""){
            $query .= "    AND ";
            $query .= "        a1.DEVIATION >= '".$deviation."' ";
        }
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){
            $query .= "    AND a3.NAME1 IS NULL ";
        }
        $query .= "    GROUP BY ";
        $query .= "        a1.mockcd, ";
        $query .= "        a1.mock_subclass_cd ";
        $query .= "    ORDER BY ";
        $query .= "        a1.mockcd desc, ";
        $query .= "        a1.mock_subclass_cd ";
        $query .= "    ) as t1 left join mock_mst t2 on t1.mockcd = t2.mockcd ";
        if($syubetu != "0"){
            $query .= "    WHERE ";
            $query .= "        t2.MOSI_DIV = '".$syubetu."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    mosi_date desc ";
        
        return $query;
    }
    
    
    function getAverage($request, $year, $gyousya, $syubetu, $subclasscd)
    {
        $first = $year;
        $second = (int)$year+1;
        $third = (int)$year+2;
        

        $query .= " SELECT ";
        $query .= "    substr(t1.MOCKCD,6,4) as MOCKCD, ";
        $query .= "    t1.mock_subclass_cd, ";
        $query .= "    t1.AVG ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        a1.mockcd, ";
        $query .= "        a1.mock_subclass_cd, ";
        $query .= "        AVG(a1.DEVIATION) as AVG ";
        $query .= "    FROM ";
        $query .= "        mock_rank_range_dat a1 ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){  //���O����
            $query .= "    left join SCHREG_REGD_DAT a2 on a1.SCHREGNO = a2.SCHREGNO and a1.YEAR = a2.YEAR and a2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    left join NAME_MST a3 on a2.COURSECODE = a3.NAMECD2 and a3.NAMECD1 = 'H316' ";
        }
        $query .= "    WHERE ";
        $query .= "        ( ";
        $query .= "         (a1.year = '".$third."' AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '3') ";
        $query .= "         or ";
        $query .= "         (a1.year = '".$second."' AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '2') ";
        $query .= "         or ";
        $query .= "         (a1.year = '".$first."' AND ";
        $query .= "          substr(a1.mockcd,6,1) = '".$gyousya."' AND ";
        $query .= "          substr(a1.mockcd,7,1) = '1') ";
        $query .= "         ) ";
        $query .= "    AND ";
        $query .= "        a1.mock_subclass_cd = '".$subclasscd."' ";
        $query .= "    AND ";
        $query .= "        a1.rank_range = '1' ";
        if($request["SPORTS"] != 1 || $request["ANALYSIS"] != 1){
            $query .= "    AND a3.NAME1 IS NULL ";
        }
        $query .= "    GROUP BY ";
        $query .= "        a1.mockcd, ";
        $query .= "        a1.mock_subclass_cd ";
        $query .= "    ORDER BY ";
        $query .= "        a1.mockcd desc, ";
        $query .= "        a1.mock_subclass_cd ";
        $query .= "    ) as t1 left join mock_mst t2 on t1.mockcd = t2.mockcd ";
        if($syubetu != "0"){
            $query .= "    WHERE ";
            $query .= "        t2.MOSI_DIV = '".$syubetu."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    mosi_date desc ";

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
    function getGyousya($gyousya)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H313' ";
        $query .= " AND ";
        $query .= "     NAMESPARE1 = '".$gyousya."' ";
        
        return $query;
    }
    
    //���Ȗ��擾
    function getkyoukaName($subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     MOCK_SUBCLASS_CD = '".$subclasscd."' ";
        
        return $query;
    }
}
?>
