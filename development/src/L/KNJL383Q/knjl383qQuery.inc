<?php

require_once('for_php7.php');

class knjl383qQuery extends Query {
    
    //SAT_COMMENT_JUDGE_DATのデータ取得
    function getJudgeComment()
    {
        $query  = " SELECT";
        $query .= "    case when PREF_FLG = '1' then COMMENTNO";
        $query .= "    else null end as COMMENTNO, ";
        $query .= "    JUDGE, ";
        $query .= "    '1' as INOUT_KUBUN ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " AND ";
        $query .= "     TYPE = '1' ";
        $query .= " UNION   ";
        $query .= " SELECT ";
        $query .= "    case when PREF_FLG = '1' then COMMENTNO";
        $query .= "    else null end as COMMENTNO, ";
        $query .= "    JUDGE, ";
        $query .= "    '2' as INOUT_KUBUN ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " AND ";
        $query .= "     TYPE = '1' ";
        $query .= " UNION   ";
        $query .= " SELECT ";
        $query .= "    case when IN_FLG = '1' then COMMENTNO";
        $query .= "    else null end as COMMENTNO, ";
        $query .= "    JUDGE, ";
        $query .= "    '3' as INOUT_KUBUN ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " AND ";
        $query .= "     TYPE = '1' ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "    case when OUT_FLG = '1' then COMMENTNO";
        $query .= "    else null end as COMMENTNO, ";
        $query .= "    JUDGE, ";
        $query .= "    '4' as INOUT_KUBUN ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_JUDGE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " AND ";
        $query .= "     TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "    JUDGE, ";
        $query .= "    COMMENTNO, ";
        $query .= "    INOUT_KUBUN ";

        return $query;
    }
    
    //SAT_JUDGE_STANDARD_DATのCOMMENTNOがNULLのデータをCNT
    function getJudgeNullCnt()
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    SAT_JUDGE_STANDARD_DAT ";
        $query .= " WHERE ";
        $query .= "    (SCORE_FROM IS NOT NULL OR SCORE_TO IS NOT NULL) ";
        $query .= " AND ";
        $query .= "    COMMENTNO IS NULL ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //SAT_JUDGE_STANDARD_DATのCOMMENTNO更新後のデータ取得
    function getSatJudge()
    {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_JUDGE_STANDARD_DAT ";
        $query .= " WHERE ";
        $query .= "    SCORE_FROM IS NOT NULL ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    JUDGE, INOUT ";
        
        return $query;
    }
    
    //SAT_EXAM_DATのJUDGEとCOMMENTNOの更新
    function upSatExamJudge($row)
    {
        $query  = " UPDATE ";
        $query .= "    SAT_EXAM_DAT ";
        $query .= " SET ";
        $query .= "    (JUDGE_SAT, COMMENTNO, REGISTERCD, UPDATED) = (SELECT ";
        $query .= "                                                     JUDGE, ";
        $query .= "                                                     COMMENTNO, ";
        $query .= "                                                     '".STAFFCD."', ";
        $query .= "                                                     CURRENT TIMESTAMP ";
        $query .= "                                                   FROM ";
        $query .= "                                                     SAT_JUDGE_STANDARD_DAT ";
        $query .= "                                                   WHERE ";
        $query .= "                                                     JUDGE = '".$row["JUDGE"]."' AND ";
        $query .= "                                                     INOUT = '".$row["INOUT"]."' ";
        $query .= "                                                   AND ";
        $query .= "                                                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                                                   ) ";
        $query .= " WHERE ";
        $query .= "    SAT_NO IN (SELECT ";
        $query .= "                    SAT_NO ";
        $query .= "                FROM ";
        $query .= "                    SAT_EXAM_DAT ";
        $query .= "                WHERE ";
        $query .= "                    SCORE_TOTAL BETWEEN '".$row["SCORE_FROM"]."' AND '".$row["SCORE_TO"]."' AND ";
        $query .= "                    SAT_NO IN (SELECT ";
        $query .= "                                    SAT_NO ";
        $query .= "                                FROM ";
        $query .= "                                    SAT_APP_FORM_MST ";
        $query .= "                                WHERE ";
        $query .= "                                    INOUT_KUBUN = '".$row["INOUT"]."' ";
        $query .= "                                AND ";
        $query .= "                                    YEAR = '".CTRL_YEAR."' ";
        $query .= "                                ) ";
        $query .= "                AND ";
        $query .= "                    YEAR = '".CTRL_YEAR."' ";
        $query .= "                ) ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //SAT_EXAM_DATのCOMMENTNOを得点と平均点との差の条件でさらに書き換える
    function upSatExamJudegeComment()
    {
        $query  = " UPDATE ";
        $query .= "     SAT_EXAM_DAT U ";
        $query .= " SET ";
        $query .= "     COMMENTNO = COMMENTNO + 1 ";
        $query .= " WHERE ";
        $query .= "     (YEAR, SAT_NO) in (SELECT ";
        $query .= "                             YEAR, ";
        $query .= "                             SAT_NO ";
        $query .= "                         FROM ";
        $query .= "                             (SELECT ";
        $query .= "                                 YEAR, ";
        $query .= "                                 SAT_NO, ";
        $query .= "                                 COMMENTNO, ";
        $query .= "                                 SCORE_ENGLISH, ";
        $query .= "                                 SCORE_MATH, ";
        $query .= "                                 SCORE_JAPANESE, ";
        $query .= "                                 AVERAGE ";
        $query .= "                             FROM ";
        $query .= "                                 SAT_EXAM_DAT ";
        $query .= "                             WHERE ";
        $query .= "                                 YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                                 (SCORE_ENGLISH <= 40 OR SCORE_MATH <= 40 OR SCORE_JAPANESE <= 40) AND ";
        $query .= "                                 ((abs(SCORE_ENGLISH - AVERAGE) >= 20) OR (abs(SCORE_MATH - AVERAGE) >= 20) OR (abs(SCORE_JAPANESE - AVERAGE) >= 20)) ";
        $query .= "                             ) ";
        $query .= "                         ) ";
        
        return $query;
    }
    
    //COMMENTデータ取得
    function getData($table)
    {
        $query  = " SELECT ";
        $query .= "    SCORE_FROM, ";
        $query .= "    SCORE_TO ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_".$table."_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    SCORE_FROM ";
        
        
        return $query;
    }

    //教科ごとのコメント取得
    function getKyoukaComment($kyouka)
    {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_COMMENT_".$kyouka."_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    COMMENT_NO ";
        
        return $query;
    }
    
    //教科コメントをSAT_EXAM_DATに更新
    function upSatExamComment($kyouka, $row)
    {
        $query  = " UPDATE ";
        $query .= "    SAT_EXAM_DAT ";
        $query .= " SET ";
        $query .= "    (COMMENT_".$kyouka.", REGISTERCD, UPDATED) = (SELECT ";
        $query .= "                                                     COMMENT_NO, ";
        $query .= "                                                     '".STAFFCD."', ";
        $query .= "                                                     CURRENT TIMESTAMP ";
        $query .= "                                                  FROM ";
        $query .= "                                                     SAT_COMMENT_".$kyouka."_DAT ";
        $query .= "                                                  WHERE ";
        $query .= "                                                     COMMENT_NO = '".$row["COMMENT_NO"]."' ";
        $query .= "                                                  AND ";
        $query .= "                                                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                                                  ) ";
        $query .= " WHERE ";
        $query .= "    SAT_NO IN (SELECT ";
        $query .= "                    SAT_NO ";
        $query .= "                FROM ";
        $query .= "                    SAT_EXAM_DAT ";
        $query .= "                WHERE ";
        $query .= "                    SCORE_".$kyouka." BETWEEN '".$row["SCORE_FROM"]."' AND '".$row["SCORE_TO"]."' ";
        $query .= "                AND ";
        $query .= "                    YEAR = '".CTRL_YEAR."' ";
        $query .= "                ) ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
}
?>
