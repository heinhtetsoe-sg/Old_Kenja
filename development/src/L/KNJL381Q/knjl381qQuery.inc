<?php

require_once('for_php7.php');

class knjl381qQuery extends Query {

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
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    JUDGE, ";
        $query .= "    COMMENTNO ";
        
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
            $query .= "     OUT_FLG = '1' ";
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
            $query .= "    COMMENTNO != '".$commentno."' ";
        }

        return $query;
    }
    
    //SAT_COMMENT_JUDGE_DATのCOMMENTNOの最大値取得
    function getCmtMax($table)
    {
        $query  = " SELECT ";
        $query .= "    MAX(COMMENT_NO) ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_".$table."_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //COMMENTデータ取得
    function getData($table)
    {
        $query  = " SELECT ";
        $query .= "    SCORE_FROM, ";
        $query .= "    SCORE_TO ";
        $query .= " FROM ";
        $query .= "    ".$table;
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    SCORE_FROM ";
        
        
        return $query;
    }

    //COMMENTデータ取得(下フレーム)
    function getComment($table)
    {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    ".$table;
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    SCORE_FROM ";
        
        return $query;
    }
    
    //SAT_COMMENT_教科_DAT削除
    function deleteComment($table, $commentno)
    {
        $query  = " DELETE FROM ";
        $query .= "     ".$table;
        $query .= " WHERE ";
        $query .= "    COMMENT_NO = '".$commentno."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    
    //コピー年度
    function getCopyYear($table)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     ".$table;
        $query .= " WHERE ";
        $query .= "     YEAR != '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        
        return $query;
    }
    //コピーデータ作成前にログイン年度データ削除
    function delYear($table)
    {
        $query  = " DELETE FROM ";
        $query .= "     ".$table;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //コピー時元データ取得(左)
    function getCopyBase($year,$table)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ".$table;
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     COMMENT_NO ";
        
        return $query;
    }

}
?>
