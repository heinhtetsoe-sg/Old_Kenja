<?php

require_once('for_php7.php');

class knjh400_hanteiQuery extends Query {

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
    //業者名
    function getCompanycd()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 as VALUE, ";
        $query .= "     NAME1 as LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H313' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";
        
        
        return $query;
    }

    function getMockCnt($model, $ctrlyear, $gakuseki="", $gyousya, $mode="0", $grade="00", $hrClass="000", $attendno="000")
    {
        $query = "";
        if($mode == "1"){       //データ全体をカウントしたい
            $query  .= " SELECT ";
            $query .= "    COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }else if($mode == "2"){     //CNTのMAXと模試の数を取得したい
            $query  .= " SELECT ";
            $query .= "    MAX(CNT) as CNT_MAX, ";
            $query .= "    COUNT(*) as MOCK_CNT";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $sqlFlg = false;
        if($gyousya == "00000001" || $gyousya == ""){
            $query  .= " SELECT DISTINCT ";
            $query .= "    a1.MOCKCD, ";
            $query .= "    a2.MOCKNAME1, ";
            $query .= "    a2.SINROSIDOU_MOSI_NAME, ";
            $query .= "    a1.CNT, ";
            $query .= "    a2.MOSI_DATE ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "        t1.*, ";
            $query .= "        t2.MOCKCD ";
            $query .= "    FROM ";
            $query .= "        (SELECT ";
            $query .= "            YEAR, ";
            $query .= "            KYOUZAICD, ";
            $query .= "            BENEID, ";
            $query .= "            COUNT(SEQ) as CNT ";
            $query .= "        FROM ";
            $query .= "            MOCK_CSV_BENE_HOPE_DAT ";
            $query .= "        WHERE ";
            $query .= "            SCHOOL_CD IS NOT NULL AND ";
            $query .= "            YEAR = '".$ctrlyear."' ";
            $query .= "        GROUP BY ";
            $query .= "            YEAR, ";
            $query .= "            KYOUZAICD, ";
            $query .= "            BENEID ";
            $query .= "        ) t1 left join MOCK_CSV_BENE_HOPE_HDAT t2 on t1.YEAR = t2.YEAR and t1.KYOUZAICD = t2.KYOUZAICD and t1.BENEID = t2.BENEID ";
            $query .= "    WHERE ";
            $query .= "       t2.SCHREGNO = '".$gakuseki."' ";
            $query .= "    ) a1 left join MOCK_MST a2 on a1.MOCKCD = a2.MOCKCD ";
            $sqlFlg = true;
        }
        if($gyousya == "00000002" || $gyousya == ""){
            if($sqlFlg){
                $query .= " UNION ";
            }
            $query .= " SELECT DISTINCT ";
            $query .= "    b1.MOCKCD, ";
            $query .= "    b2.MOCKNAME1, ";
            $query .= "    b2.SINROSIDOU_MOSI_NAME, ";
            $query .= "    b1.CNT, ";
            $query .= "    b2.MOSI_DATE ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "        t1.*, ";
            $query .= "        t2.MOCKCD ";
            $query .= "    FROM ";
            $query .= "        (SELECT ";
            $query .= "            YEAR, ";
            $query .= "            MOSI_CD, ";
            $query .= "            EXAMNO, ";
            $query .= "            COUNT(SEQ) as CNT ";
            $query .= "        FROM ";
            $query .= "            MOCK_CSV_SUNDAI_HOPE_DAT ";
            $query .= "        WHERE ";
            $query .= "            SCHOOL_CD IS NOT NULL AND ";
            $query .= "            YEAR = '".$ctrlyear."' ";
            $query .= "        GROUP BY ";
            $query .= "            YEAR, ";
            $query .= "            MOSI_CD, ";
            $query .= "            EXAMNO ";
            $query .= "        ) t1 left join MOCK_CSV_SUNDAI_HDAT t2 on t1.YEAR = t2.YEAR and t1.MOSI_CD = t2.MOSI_CD and t1.EXAMNO = t2.EXAMNO ";
            $query .= "    WHERE ";
            $query .= "       t2.SCHREGNO = '".$gakuseki."' ";
            $query .= "    ) b1 left join MOCK_MST b2 on b1.MOCKCD = b2.MOCKCD ";
            $sqlFlg = true;
        }
        if($gyousya == "00000003" || $gyousya == ""){
            if($sqlFlg){
                $query .= " UNION ";
            }
            $query .= " SELECT DISTINCT ";
            $query .= "    b1.MOCKCD, ";
            $query .= "    b2.MOCKNAME1, ";
            $query .= "    b2.SINROSIDOU_MOSI_NAME, ";
            $query .= "    b1.CNT, ";
            $query .= "    b2.MOSI_DATE ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "        t1.*, ";
            $query .= "        t2.MOCKCD ";
            $query .= "    FROM ";
            $query .= "        (SELECT ";
            $query .= "            YEAR, ";
            $query .= "            MOSI_CD, ";
            $query .= "            HR_CLASS, ";
            $query .= "            ATTENDNO, ";
            $query .= "            COUNT(SEQ) as CNT ";
            $query .= "        FROM ";
            $query .= "            MOCK_CSV_ZKAI_HOPE_DAT ";
            $query .= "        WHERE ";
            $query .= "            SCHOOL_CD IS NOT NULL AND ";
            $query .= "            YEAR = '".$ctrlyear."' ";
            $query .= "        GROUP BY ";
            $query .= "            YEAR, ";
            $query .= "            MOSI_CD, ";
            $query .= "            HR_CLASS, ";
            $query .= "            ATTENDNO ";
            $query .= "        ) t1 left join MOCK_CSV_ZKAI_HDAT t2 on t1.YEAR = t2.YEAR and t1.MOSI_CD = t2.MOSI_CD and t1.HR_CLASS = t2.HR_CLASS and t1.ATTENDNO = t2.ATTENDNO ";
            $query .= "    WHERE ";
            $query .= "       RIGHT('000' || t2.HR_CLASS, 3) || RIGHT('000' || t2.ATTENDNO, 3) = '".$hrClass.$attendno."' ";
            $query .= "       and SUBSTR(t2.MOCKCD, 7, 1) = '".intval($grade)."' ";
            $query .= "    ) b1 left join MOCK_MST b2 on b1.MOCKCD = b2.MOCKCD ";
            $sqlFlg = true;
        }
        $query .= " ORDER BY ";
        $query .= "    MOSI_DATE DESC, ";
        $query .= "    MOCKCD ";
        
        if($mode != "0"){
        $query .= "    ) t1 ";
        }

        return $query;
    }
    
    function getHopeGaku($model, $mockcd, $gakuseki, $ctrlyear, $grade, $hrClass, $attendno)
    {
        $query  = " SELECT ";
        $query .= "    GYOUSYA, ";
        $query .= "    SCHOOL_CD, ";
        $query .= "    SEQ, ";
        $query .= "    GAKKO, ";
        $query .= "    NITTEI, ";
        $query .= "    JUDGE, ";
        $query .= "    JUDGE_SS ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        1 as GYOUSYA, ";
        $query .= "        t1.SCHOOL_CD, ";
        $query .= "        int(t1.SEQ) AS SEQ, ";
        //エラー回避したい　連結文字列の中にNULLがあってもSELECTされるように
        /*$query .= "        case when GAKUBU_NAME is NULL and GAKKA_NAME is NULL then SCHOOL_NAME  ";
        $query .= "             when GAKKA_NAME is NULL then SCHOOL_NAME || '<BR>' || GAKUBU_NAME ";
        $query .= "             else SCHOOL_NAME || '<BR>' || GAKUBU_NAME|| '<BR>' || GAKKA_NAME ";
        $query .= "             end AS GAKKO, ";*/
        //$query .= "        replace(rtrim(t1.BOSYUTANNI), ' ', '<BR>') AS GAKKO, ";
        $query .= "        t1.BOSYUTANNI AS GAKKO, ";
        $query .= "        replace(t1.NITTEI, '　', '') AS NITTEI, ";
        $query .= "        t1.ALL_JUDGE AS JUDGE, ";
        $query .= "        t1.ALL_JUDGE_SS as JUDGE_SS, ";
        $query .= "        t2.MOCKCD ";
        $query .= "    FROM ";
        $query .= "        MOCK_CSV_BENE_HOPE_DAT t1 left join MOCK_CSV_BENE_HOPE_HDAT t2 on t1.YEAR = t2.YEAR and t1.KYOUZAICD = t2.KYOUZAICD and t1.BENEID = t2.BENEID ";
        $query .= "    WHERE ";
        $query .= "        t1.SCHOOL_CD IS NOT NULL  AND ";
        $query .= "        t2.SCHREGNO = '".$gakuseki."' AND ";
        $query .= "        t1.YEAR = '".$ctrlyear."' ";
        $query .= "    UNION  ";
        $query .= "    SELECT ";
        $query .= "        2 as GYOUSYA, ";
        $query .= "        t1.SCHOOL_CD, ";
        $query .= "        int(t1.SEQ) AS SEQ, ";
        //$query .= "        replace(rtrim(t1.SCHOOL_NAME), '　', '<BR>') AS GAKKO, ";
        //分割して取得しないで半角スペースにして取得
        $query .= "        replace(rtrim(t1.SCHOOL_NAME), '　', ' ') AS GAKKO, ";
        $query .= "        replace(t1.NITTEI, '　', '') AS NITTEI, ";
        $query .= "        t1.JUDGE_HYOUKA AS JUDGE, ";
        $query .= "        char(t1.JUDGE_SUUTI) as JUDGE_SS, ";
        $query .= "        t2.MOCKCD ";
        $query .= "    FROM ";
        $query .= "        MOCK_CSV_SUNDAI_HOPE_DAT t1 left join MOCK_CSV_SUNDAI_HDAT t2 on t1.YEAR = t2.YEAR and t1.MOSI_CD = t2.MOSI_CD and t1.EXAMNO = t2.EXAMNO ";
        $query .= "    WHERE ";
        $query .= "        t1.SCHOOL_NAME IS NOT NULL  AND ";
        $query .= "        t2.SCHREGNO = '".$gakuseki."' AND ";
        $query .= "        t1.YEAR = '".$ctrlyear."' ";
        $query .= "    UNION  ";
        $query .= "    SELECT ";
        $query .= "        3 as GYOUSYA, ";
        $query .= "        t1.SCHOOL_CD, ";
        $query .= "        int(t1.SEQ) AS SEQ, ";
        //$query .= "        replace(rtrim(t1.SCHOOL_NAME), '　', '<BR>') AS GAKKO, ";
        //分割して取得しないで半角スペースにして取得
        $query .= "        replace(rtrim(t1.SCHOOL_NAME), '　', ' ') AS GAKKO, ";
        $query .= "        '' AS NITTEI, ";
        $query .= "        t1.JUDGE_HYOUKA AS JUDGE, ";
        $query .= "        char(t1.JUDGE_SUUTI) as JUDGE_SS, ";
        $query .= "        t2.MOCKCD ";
        $query .= "    FROM ";
        $query .= "        MOCK_CSV_ZKAI_HOPE_DAT t1 left join MOCK_CSV_ZKAI_HDAT t2 on t1.YEAR = t2.YEAR and t1.MOSI_CD = t2.MOSI_CD and t1.HR_CLASS = t2.HR_CLASS and t1.ATTENDNO = t2.ATTENDNO ";
        $query .= "    WHERE ";
        $query .= "        t1.SCHOOL_NAME IS NOT NULL  AND ";
        $query .= "       RIGHT('000' || t2.HR_CLASS, 3) || RIGHT('000' || t2.ATTENDNO, 3) = '".$hrClass.$attendno."' AND ";
        $query .= "       SUBSTR(t2.MOCKCD, 7, 1) = '".intval($grade)."' AND ";
        $query .= "        t1.YEAR = '".$ctrlyear."' ";
        $query .= "    ) MAIN_T ";
        $query .= " WHERE ";
        $query .= "    MOCKCD = '".$mockcd."' ";
        $query .= " ORDER BY ";
        $query .= "    MOCKCD, SEQ";
        

        return $query;
    }

}
?>