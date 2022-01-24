<?php

require_once('for_php7.php');

class knjl379qQuery extends Query {

    //試験データ取得
    function getExam()
    {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_INFO_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //判定の種類とって来る
    function getJudge()
    {
        $query .= " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_YDAT t1 ";
        $query .= "     left join NAME_MST t2 on t1.NAMECD1 = t2.NAMECD1 and t1.NAMECD2 = t2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     t1.NAMECD1 = 'L200' ";
        
        return $query;
    }

    //データ取得
    function getData()
    {
        $query  = " SELECT ";
        $query .= "    t1.JUDGE, ";
        $query .= "    t1.SCORE_FROM as FROM_0, ";
        $query .= "    t1.SCORE_TO as TO_0, ";
        $query .= "    t2.SCORE_FROM as FROM_1, ";
        $query .= "    t2.SCORE_TO as TO_1, ";
        $query .= "    t3.SCORE_FROM as FROM_2, ";
        $query .= "    t3.SCORE_TO AS TO_2, ";
        $query .= "    t4.SCORE_FROM as FROM_3, ";
        $query .= "    t4.SCORE_TO as TO_3 ";
        $query .= " FROM ";
        $query .= "    SAT_JUDGE_STANDARD_DAT t1 ";
        $query .= "    left join SAT_JUDGE_STANDARD_DAT t2 on t1.JUDGE = t2.JUDGE and t2.INOUT = 2 and t1.YEAR = t2.YEAR ";
        $query .= "    left join SAT_JUDGE_STANDARD_DAT t3 on t1.JUDGE = t3.JUDGE and t3.INOUT = 3 and t1.YEAR = t3.YEAR ";
        $query .= "    left join SAT_JUDGE_STANDARD_DAT t4 on t1.JUDGE = t4.JUDGE and t4.INOUT = 4 and t1.YEAR = t4.YEAR ";
        $query .= " WHERE  ";
        $query .= "    t1.INOUT = 1 ";
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY  ";
        $query .= "    t1.JUDGE ";
        
        return $query;
    }

    

    //年度取得
    function getNendo()
    {
        $query  = " SELECT ";
        $query .= "    YEAR ";
        $query .= " FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "    YEAR ";
        
        return $query;
    }
    
    //コピー年度
    function getCopyYear()
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SAT_JUDGE_STANDARD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR != '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        
        return $query;
    }
    //コピーデータ作成前にログイン年度データ削除
    function delYear()
    {
        $query  = " DELETE FROM ";
        $query .= "     SAT_JUDGE_STANDARD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //コピー時元データ取得(左)
    function getCopyBase($year)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SAT_JUDGE_STANDARD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     JUDGE, INOUT ";
        
        return $query;
    }

}
?>
