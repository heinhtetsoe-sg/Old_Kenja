<?php

require_once('for_php7.php');

class knjh400_seisekiQuery extends Query {

    //業者のコンボボックス
    function getGyousya()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    substr(t1.NAMESPARE1,8,1) as value, ";
        $query .= "    t1.NAME1 as label";
        $query .= " FROM ";
        $query .= "    NAME_MST t1 ";
        $query .= "    left join MOCK_MST t2 on t1.NAMESPARE1 = t2.COMPANYCD ";
        $query .= " WHERE ";
        $query .= "    t1.NAMECD1 = 'H313' ";
        $query .= " ORDER BY ";
        $query .= "     value ";

        return $query;
    }
    
    //模試種別
    function getSyubetu()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.NAMECD2 as value, ";
        $query .= "    t1.NAME1 as label";
        $query .= " FROM ";
        $query .= "    NAME_MST t1 ";
        $query .= "    left join MOCK_MST t2 on t1.NAMESPARE1 = t2.COMPANYCD ";
        $query .= " WHERE ";
        $query .= "    t1.NAMECD1 = 'H314' ";

        return $query;
    }

    function getSchreg($year, $schreg)
    {
        $query  = " SELECT ";
        $query .= "    t1.SCHREGNO, ";
        $query .= "    t1.YEAR, ";
        $query .= "    t1.GRADE, ";
        $query .= "    GDAT.GRADE_CD, ";
        $query .= "    t1.HR_CLASS, ";
        $query .= "    t1.ATTENDNO, ";
        $query .= "    t2.NAME_SHOW, ";
        $query .= "    t3.GRADE_NAME, ";
        $query .= "    t3.HR_CLASS_NAME1 ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT t1 ";
        $query .= "    left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "    left join SCHREG_REGD_HDAT t3 on t1.YEAR = t3.YEAR and t1.GRADE = t3.GRADE and t1.HR_CLASS = t3.HR_CLASS and t1.SEMESTER = t3.SEMESTER ";
        $query .= "    left join SCHREG_REGD_GDAT GDAT on t1.YEAR = GDAT.YEAR and t1.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "    t1.SCHREGNO = '".$schreg."' AND ";
        $query .= "    t1.YEAR = '".$year."' AND ";
        $query .= "    t1.SEMESTER = '1' ";
        
        return $query;
    }
    
    //期取得
    function getPeriod($year)
    {
        $query  = " SELECT ";
        $query .= "    PRESENT_EST ";
        $query .= " FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".$year."' ";
        
        return $query;
    }

    //チェックボックス用の教科名取得
    function getKyouka($schreg, $kata="0", $gyousya="0", $syubetu="0")
    {
/*        $query  = " SELECT ";
        $query .= "    MOCK_SUBCLASS_CD, ";
        $query .= "    SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "    MOCK_SUBCLASS_MST ";
        $query .= " WHERE ";
        if($kata == '1'){
            $query .= "    SUBCLASS_DIV = '1' ";
        }else{
            $query .= "    SUBCLASS_DIV IS NULL ";
        }
        if($gyousya != "0"){
            $query .= " AND ";
            $query .= "    MOCK_SUBCLASS_CD LIKE '".$gyousya."%' ";
        }
        $query .= " OR ";
        $query .= "    MOCK_SUBCLASS_CD IN ('000010','000020','000030') ";
        $query .= " ORDER BY ";
        $query .= "    MOCK_SUBCLASS_CD ";*/
        
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.MOCK_SUBCLASS_CD, ";
        $query .= "    case when t3.SUBCLASS_ABBV IS NOT NULL then t3.SUBCLASS_ABBV ";
        $query .= "         when t3.SUBCLASS_ABBV IS NULL then t3.SUBCLASS_NAME ";
        $query .= "    end as SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT t1 left join MOCK_MST t2 on t1.MOCKCD = t2.MOCKCD ";
        $query .= "    left join MOCK_SUBCLASS_MST t3 on t1.MOCK_SUBCLASS_CD = t3.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "    t1.SCHREGNO = '".$schreg."' ";
        if($kata == '1'){
            $query .= " AND ";
               $query .= "    t3.SUBCLASS_DIV = '1' ";
        }else{
            $query .= " AND ";
            $query .= "    t3.SUBCLASS_DIV IS NULL ";
        }
        if($syubetu != "0"){
            $query .= " AND";
            $query .= "    t2.MOSI_DIV = '".$syubetu."' ";
        }
        if($gyousya != "0"){
            $query .= " AND ";
            $query .= "    t2.COMPANYCD = '0000000".$gyousya."' ";
        }
        $query .= " AND ";
        $query .= "    t3.MOCK_SUBCLASS_CD != '999999' ";
        $query .= " AND ";
        $query .= "    t3.MOCK_SUBCLASS_CD != '900000' ";
        $query .= " ORDER BY ";
        $query .= "    t1.MOCK_SUBCLASS_CD ";
        
        return $query;
    }
    
    //対象生徒の受験した模試データ取得
    function getMock($gakuseki, $gyousya, $kyouka, $syubetu="0", $kata = "0", $mode="0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "    COUNT(*) ";
            $query .= " FROM ";
            $query .= "    ( ";
        }else{
            $query = "";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "    t2.YEAR, ";
        $query .= "    t1.MOCKCD, ";
        $query .= "    substr(t1.MOCKCD,7,1) as GRADE, ";
        $query .= "    t1.MOCKNAME1, ";
        $query .= "    t1.SINROSIDOU_MOSI_NAME, ";
        $query .= "    t1.MOSI_DATE ";
        $query .= " FROM ";
        $query .= "    MOCK_MST t1 left join MOCK_RANK_RANGE_DAT t2 on t1.MOCKCD = t2.MOCKCD ";
        $query .= "    left join MOCK_SUBCLASS_MST t3 on t2.MOCK_SUBCLASS_CD = t3.MOCK_SUBCLASS_CD  ";
        $query .= " WHERE ";
        if($gyousya != ""){
            $query .= "    COMPANYCD = '".$gyousya."' ";
            $query .= " AND ";
        }
        if($syubetu != "0"){
            $query .= "    t1.MOSI_DIV = '".$syubetu."' ";
            $query .= " AND ";
        }
        if($kata == "0"){
            $query .= "    t3.SUBCLASS_DIV IS NULL ";
            $query .= " AND ";
        }else{
            $query .= "    t3.SUBCLASS_DIV = '1' ";
            $query .= " AND ";
        }
        $query .= "    t2.SCHREGNO = '".$gakuseki."' ";
        $query .= " AND ";
        $query .= "    t2.MOCK_SUBCLASS_CD IN ('".$kyouka."') ";
        $query .= " ORDER BY ";
        $query .= "    t2.YEAR DESC, t1.MOSI_DATE DESC ";
        if($mode != "0"){
            $query .= "    ) t1 ";
        }

        return $query;
    }
    
    //模試ごとの成績取得
    function getSeiseki($mockcd, $gakuseki, $kata, $kyouka, $syubetu)
    {
        $query  = " SELECT ";
        $query .= "    MOCK_SUBCLASS_CD, ";
        $query .= "    DEVIATION ";
        $query .= " FROM ";
        $query .= "    MOCK_RANK_RANGE_DAT ";
        $query .= " WHERE ";
        $query .= "    MOCKCD = '".$mockcd."' AND ";
        $query .= "    SCHREGNO = '".$gakuseki."' AND ";
        $query .= "    RANK_RANGE = '1' ";
        $query .= " AND ";
        $query .= "    MOCK_SUBCLASS_CD IN ('".$kyouka."') ";
        $query .= " ORDER BY ";
        $query .= "    MOCK_SUBCLASS_CD ";
        
        return $query;
    }
}
?>
