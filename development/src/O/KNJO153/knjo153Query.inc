<?php
class knjo153Query extends Query {
    //生徒基本情報取得
    function getStudent($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     NAME, ";
        $query .= "     HANDICAP ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //対象生徒の学年取得
    function getGrade($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.SYUBETU, ";
        $query .= "     t2.CODENAME as GRADENAME, ";
        $query .= "     t3.CODENAME as SYUBETUNAME ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SHIDOU_DAT t1 ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.GRADE = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '01' and t2.DICTIONARY_NO = '02' ";
        $query .= "     left join CE_APP_NAME_MST t3 on t1.SYUBETU = t3.DICTIONARY_CODE and t3.DICTIONARY_KIND = '02' and t3.DICTIONARY_NO = '09' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";
        
        return $query;
    }
    //CE_APP_Y_SHIDOU_DATからデータ取得
    function getShidou($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     JIRITU_ACT, ";
        $query .= "     GENERAL_FINDINGS, ";
        $query .= "     BIKOU ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SHIDOU_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND ";
        $query .= "     GRADE = '".$grade."' ";
        
        return $query;
    }
    //各教科の学習の記録のデータ
    function getPointKyouka($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.CODENAME as KYOUKA, ";
        $query .= "     t3.SCORE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         UPPER_DATA_ROW, ";
        $query .= "         SUBJECT, ";
        $query .= "         COUNT(*) as CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             UPPER_DATA_ROW, ";
        $query .= "             SCHREGNO, ";
        $query .= "             case when OTHER_SUBJECT IS NOT NULL then OTHER_SUBJECT else SUBJECT end as SUBJECT, ";
        $query .= "             case when OTHER_POINT IS NOT NULL then OTHER_POINT else POINT end as POINT, ";
        $query .= "             SCORE ";
        $query .= "         FROM ";
        $query .= "             CE_APP_Y_SUBJ_POINT_DAT ";
        $query .= "         WHERE ";
        $query .= "             SCHREGNO = '".$schregno."' AND ";
        $query .= "             UPPER_DATA_ROW = (SELECT ";
        $query .= "                                     DATA_ROW ";
        $query .= "                                 FROM ";
        $query .= "                                     CE_APP_Y_SHIDOU_DAT ";
        $query .= "                                 WHERE ";
        $query .= "                                     SCHREGNO = '".$schregno."' AND ";
        $query .= "                                     GRADE = '".$grade."' ";
        $query .= "                                 ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         UPPER_DATA_ROW, ";
        $query .= "         SUBJECT ";
        $query .= "     ) t1  ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.SUBJECT = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '02' ";
        $query .= "     left join CE_APP_Y_SUBJ_SCORE_DAT t3 on t1.UPPER_DATA_ROW = t3.UPPER_DATA_ROW and t1.SCHREGNO = t3.SCHREGNO and (t1.SUBJECT = t3.SUBJECT or t1.SUBJECT = t3.OTHER_SUBJECT) ";
                
        return $query;
    }
    function getPointNaiyo($schregno, $upper, $kyouka)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.CODENAME ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SUBJ_POINT_DAT t1 ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.POINT = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '03' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' AND ";
        $query .= "     (SUBJECT = '".$kyouka."' OR OTHER_SUBJECT = '".$kyouka."') ";
        
        return $query;
    }
    //外国語活動
    function getForeign($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.CODENAME ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_FOR_LANG_DAT t1 ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.POINT = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '03' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW =  (SELECT ";
        $query .= "                             DATA_ROW ";
        $query .= "                         FROM ";
        $query .= "                             CE_APP_Y_SHIDOU_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO = '".$schregno."' AND ";
        $query .= "                             GRADE = '".$grade."' ";
        $query .= "                         ) ";
        $query .= " ORDER BY  ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    //総合的な学習の時間の記録
    function getInteg($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_INTEG_STUDY_DAT t1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     SCHREGNO, ";
        $query .= "                     UPPER_DATA_ROW, ";
        $query .= "                     NEXT_DATA_ROW, ";
        $query .= "                     COUNT(*) as CNT ";
        $query .= "                 FROM ";
        $query .= "                     CE_APP_Y_INTEG_STUDY_POINT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHREGNO = '".$schregno."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO, ";
        $query .= "                     UPPER_DATA_ROW, ";
        $query .= "                     NEXT_DATA_ROW ";
        $query .= "                 ) t2 on t1.UPPER_DATA_ROW = t2.UPPER_DATA_ROW and t1.DATA_ROW = t2.NEXT_DATA_ROW ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     t1.UPPER_DATA_ROW =  (SELECT ";
        $query .= "                                 DATA_ROW ";
        $query .= "                             FROM ";
        $query .= "                                 CE_APP_Y_SHIDOU_DAT ";
        $query .= "                             WHERE ";
        $query .= "                                 SCHREGNO = '".$schregno."' AND ";
        $query .= "                                 GRADE = '".$grade."' ";
        $query .= "                             ) ";
        
        return $query;
    }
    function getIntegPoint($schregno, $upper, $next)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_INTEG_STUDY_POINT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' AND ";
        $query .= "     NEXT_DATA_ROW = '".$next."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    //特別活動の記録
    function getSpeAct($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.CODENAME ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SP_ACT_DAT t1 ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.ACTION = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '04' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW =  (SELECT ";
        $query .= "                             DATA_ROW ";
        $query .= "                         FROM ";
        $query .= "                             CE_APP_Y_SHIDOU_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO = '".$schregno."' AND ";
        $query .= "                             GRADE = '".$grade."' ";
        $query .= "                         ) ";
        
        return $query;
    }
    //行動の記録
    function getBehavior($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.CODENAME ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_BEHAVIOR_DAT t1 ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.ITEM = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '02' AND t2.DICTIONARY_NO = '05' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW =  (SELECT ";
        $query .= "                             DATA_ROW ";
        $query .= "                         FROM ";
        $query .= "                             CE_APP_Y_SHIDOU_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO = '".$schregno."' AND ";
        $query .= "                             GRADE = '".$grade."' ";
        $query .= "                         ) ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    //各教科_特別活動_自立活動の記録
    function getAll($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.CODENAME ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_ALL_ACT_DAT t1 ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.SUBJECT = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '02'  ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."'AND ";
        $query .= "     UPPER_DATA_ROW =  (SELECT ";
        $query .= "                             DATA_ROW ";
        $query .= "                         FROM ";
        $query .= "                             CE_APP_Y_SHIDOU_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO = '".$schregno."' AND ";
        $query .= "                             GRADE = '".$grade."' ";
        $query .= "                         ) ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //送信元情報取得
    function getSendFrom($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_FROM_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }

    //参考様式コード取得
    function getStyleVersion($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     STYLE_VER ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SCHOOL_CHANGE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRADE = '".$grade."' ";
        
        return $query;
    }

    //特別な教科_道徳評価
    function getMoral($schregno, $grade)
    {
        $query  = " SELECT ";
        $query .= "     POINT ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_MORAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."'AND ";
        $query .= "     UPPER_DATA_ROW =  (SELECT ";
        $query .= "                             DATA_ROW ";
        $query .= "                         FROM ";
        $query .= "                             CE_APP_Y_SHIDOU_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO = '".$schregno."' AND ";
        $query .= "                             GRADE = '".$grade."' ";
        $query .= "                         ) ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }

}
?>
