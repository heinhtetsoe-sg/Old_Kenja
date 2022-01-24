<?php

require_once('for_php7.php');

class knjl380qQuery extends Query {

    //判定取得
    function getJudge()
    {
        $query  = " SELECT ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'L200' ";
        
        return $query;
    }

    //データ取得
    function getJudgeComment()
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.COMMENTNO2, ";
        $query .= "     t2.COMMENT2_1, ";
        $query .= "     t2.COMMENT2_2, ";
        $query .= "     t2.COMMENT2_3, ";
        $query .= "     t2.COMMENT2_4 ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         JUDGE, ";
        $query .= "         COMMENTNO, ";
        $query .= "         COMMENT1, ";
        $query .= "         COMMENT2, ";
        $query .= "         COMMENT3, ";
        $query .= "         NOTE, ";
        $query .= "         PREF_FLG, ";
        $query .= "         IN_FLG, ";
        $query .= "         OUT_FLG ";
        $query .= "     FROM ";
        $query .= "         SAT_COMMENT_JUDGE_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         TYPE = '1' ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     YEAR, ";
        $query .= "                     JUDGE, ";
        $query .= "                     COMMENTNO as COMMENTNO2, ";
        $query .= "                     COMMENT1 as COMMENT2_1, ";
        $query .= "                     COMMENT2 as COMMENT2_2, ";
        $query .= "                     COMMENT3 as COMMENT2_3, ";
        $query .= "                     COMMENT4 as COMMENT2_4, ";
        $query .= "                     PREF_FLG, ";
        $query .= "                     IN_FLG, ";
        $query .= "                     OUT_FLG ";
        $query .= "                 FROM ";
        $query .= "                     SAT_COMMENT_JUDGE_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     TYPE = '2' ";
        $query .= "                 ) t2 on t1.YEAR = t2.YEAR and t1.JUDGE = t2.JUDGE and t1.PREF_FLG = t2.PREF_FLG and t1.IN_FLG = t2.IN_FLG and t1.OUT_FLG = t2.OUT_FLG ";
        $query .= " ORDER BY ";
        $query .= "     JUDGE, ";
        $query .= "     COMMENTNO ";
        
        return $query;
    }
    
    //重複チェック
    function getJudgeCnt($judge, $pref, $in, $out, $commentno="")
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "    JUDGE = '".$judge."' AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    ( ";
        if($pref != ""){
            $query .= "     PREF_FLG = '1' ";
        }
        if($pref != "" && ($in != "" || $out != "")){
            $query .= "     OR ";
        }
        if($in != ""){
            $query .= "     IN_FLG = '1' ";
        }
        if(($pref != "" || $in != "") && $out != ""){
            $query .= "     OR ";
        }
        if($out != ""){
            $query .= "     OUT_FLG = '1' ";
        }
        $query .= "    ) ";
        if($commentno != ""){
            $query .= " AND ";
            $query .= "    COMMENTNO NOT IN (".$commentno.") ";
        }

        return $query;
    }
    
    //SAT_COMMENT_JUDGE_DATのCOMMENTNOの最大値取得
    function getCmtMax()
    {
        $query  = " SELECT ";
        $query .= "    MAX(COMMENTNO) ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //SAT_COMMENT_JUDGE_DAT削除
    function deleteComment($commentno)
    {
        $query  = " DELETE FROM ";
        $query .= "    SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "    COMMENTNO in (".$commentno.") ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //コピー年度
    function getCopyYear()
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SAT_COMMENT_JUDGE_DAT ";
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
        $query .= "     SAT_COMMENT_JUDGE_DAT ";
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
        $query .= "     SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     COMMENTNO ";
        
        return $query;
    }

}
?>
