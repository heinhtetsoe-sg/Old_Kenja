<?php
class knjo123Query extends Query {
    //データの有無
    function countData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_SYUBETU_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //送信先情報
    function getToSchool($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_TO_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //送信元情報
    function getFromSchool($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_FROM_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //指導要録情報
    function getSyubetu($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_SYUBETU_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //指導要録情報＞学校変更状況
    function getSchChange($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SCHOOL_CHANGE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞校長氏名
    function getPrincipal($schregno, $upper)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_PRINCIPAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞学級担任者氏名
    function getHrteacher($schregno, $upper)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_HRTEACHER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //指導要録情報＞学籍に関する記録＞児童生徒＞通称名
    function getAlias($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_ALIAS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='".$schregno."' ";
        
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞児童生徒＞現住所_その他
    function getStOtherAdd($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_OTHERADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞児童生徒＞保護者
    function getParent($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_PARENT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞児童生徒＞保護者＞現住所_その他
    function getParentOtrAdd($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_PARENT_OTHERADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞在学状況＞在学学年
    function getZaigaku($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞異動状況＞入学前の経歴
    function getKeireki($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_BACKGROUND_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞異動状況
    function getIdouMst($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_IDOU_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    
    //指導要録情報＞指導に関する記録
    function getShidou($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SHIDOU_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞各教科の学習の記録＞観点別学習状況・評定情報のデータの有無確認
    function getSubjCnt($schregno, $upper)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         UPPER_DATA_ROW, ";
        $query .= "         COUNT(*) as POINT_CNT ";
        $query .= "     FROM ";
        $query .= "         CE_APP_Y_SUBJ_POINT_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' AND ";
        $query .= "         UPPER_DATA_ROW = '".$upper."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         UPPER_DATA_ROW ";
        $query .= "     ) t1 left join  (SELECT ";
        $query .= "                         SCHREGNO as S_SCHREGNO, ";
        $query .= "                         UPPER_DATA_ROW as S_UPPER_DATA_ROW, ";
        $query .= "                         COUNT(*) as SCORE_CNT ";
        $query .= "                     FROM ";
        $query .= "                         CE_APP_Y_SUBJ_SCORE_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         SCHREGNO = '".$schregno."' AND ";
        $query .= "                         UPPER_DATA_ROW = '".$upper."' ";
        $query .= "                     GROUP BY ";
        $query .= "                         SCHREGNO, ";
        $query .= "                         UPPER_DATA_ROW ";
        $query .= "                     ) t2 on t1.SCHREGNO = t2.S_SCHREGNO and t1.UPPER_DATA_ROW = t2.S_UPPER_DATA_ROW ";
        $query .= " WHERE  ";
        $query .= "     t1.POINT_CNT >0 OR ";
        $query .= "     t2.SCORE_CNT > 0 ";
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞各教科の学習の記録＞観点別学習状況
    function getSubjPoint($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SUBJ_POINT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞各教科の学習の記録＞評定情報
    function getSubjScore($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SUBJ_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
     
        return $query;
    }

    //指導要録情報＞指導に関する記録＞特別の教科_道徳
    function getMoral($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_MORAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }

    //指導要録情報＞指導に関する記録＞外国語活動の記録
    function getForLang($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_FOR_LANG_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞総合的な学習の時間の記録＞学習活動の状況
    function getIntegrated($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_INTEG_STUDY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞総合的な学習の時間の記録＞学習活動の状況＞観点
    function getIntegPoint($schregno, $upper, $next, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_INTEG_STUDY_POINT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' AND ";
        $query .= "     NEXT_DATA_ROW = '".$next."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞特別活動の記録
    function getSpAct($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SP_ACT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞行動の記録
    function getBehave($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞各教科_特別活動_自立活動の記録
    function getAllAct($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_Y_ALL_ACT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }

    //送信日時取得
    function getSendDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SEND_DATE ";
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_FROM_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //学校名取得
    function getSchoolName($schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolKind."' ";
        
        return $query;
    }

function getCheck($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1  ";
        $query .= "     left join SCHREG_REGD_GDAT t2 on t1.YEAR = t2.YEAR and t1.GRADE = t2.GRADE ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     t1.YEAR, ";
        $query .= "     t2.SCHOOL_KIND ";
        
        return $query;
    }

    
    //在校生検索ボタン
    function getSchregregd_data($model, $ych, $gch, $sch) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.NAME AS STUDENTNAME, ";
        $query .= "     T3.HR_NAME || '-' || T1.ATTENDNO || '番' AS NENKUMI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON T3.YEAR         = G1.YEAR ";
            $query .= "     AND T3.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON T3.YEAR         = G1.YEAR ";
            $query .= "     AND T3.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = '".$ych."' AND ";
        $query .= "     T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$gch."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$sch."' AND ";
        $query .= "     T1.GRADE        = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS     = T3.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }


    //１レコード取得（学籍在籍データ）
    public function getSchregregdData($model)
    {
        $db = Query::dbCheckOut();

        $header = knjo123Query::getJudgeTable($model->SCHREGNO, $db);

        $query  = " SELECT ";
        $query .= " T1.YEAR, ";
        $query .= " T1.GRADE, ";
        $query .= " T1.SEMESTER, ";
        $query .= " T1.HR_CLASS, ";
        $query .= "    T3.STAFFNAME_SHOW AS HR_TEARCH,";
        $query .= " T4.COURSENAME, ";
        $query .= " T5.MAJORNAME, ";
        $query .= " T6.HR_NAME ";
        $query .= " FROM ";
        $query .= "   (";
        $query .= "   SELECT ";
        $query .= "     SUBSTR(TT1.WORK,1,4) AS YEAR, ";
        $query .= "     SUBSTR(TT1.WORK,5,1) AS SEMESTER, ";
        $query .= "     TT2.GRADE, ";
        $query .= "     TT2.HR_CLASS, ";
        $query .= "     TT2.COURSECD, ";
        $query .= "     TT2.MAJORCD ";
        $query .= "   FROM ";
        $query .= "     (SELECT SCHREGNO,MAX(YEAR || SEMESTER) AS WORK FROM ". $header ."_REGD_DAT WHERE SCHREGNO = '" .$model->SCHREGNO ."' GROUP BY SCHREGNO) TT1, ";
        $query .= "     ". $header ."_REGD_DAT TT2 ";
        $query .= "   WHERE ";
        $query .= "     TT1.SCHREGNO = '" .$model->SCHREGNO ."'";
        $query .= "     AND   TT1.SCHREGNO = TT2.SCHREGNO ";
        $query .= "     AND   SUBSTR(TT1.WORK,1,4) = TT2.YEAR ";
        $query .= "     AND   SUBSTR(TT1.WORK,5,1) = TT2.SEMESTER ";
        $query .= "   ) T1";
        $query .= "   LEFT JOIN ";
        $query .= "     ". $header ."_REGD_HDAT T6 ";
        $query .= "   ON T1.YEAR     = T6.YEAR ";
        $query .= "     AND   T1.SEMESTER = T6.SEMESTER ";
        $query .= "     AND   T1.GRADE = T6.GRADE ";
        $query .= "     AND   T1.HR_CLASS = T6.HR_CLASS ";
        $query .= "   LEFT JOIN STAFF_MST T3 ON  T6.TR_CD1 = T3.STAFFCD ";
        $query .= "   LEFT JOIN  COURSE_MST T4 ON  T1.COURSECD = T4.COURSECD ";
        $query .= "   LEFT JOIN MAJOR_MST T5 ON  T1.COURSECD = T5.COURSECD AND T1.MAJORCD = T5.MAJORCD ";
        $query .= " ORDER BY T1.YEAR DESC, T1.GRADE DESC ,T1.SEMESTER DESC ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //参照するテーブルを判断：卒業用(GRD_XXX)、在籍用(SCH_XXX)
    public function getJudgeTable($schregno, $db)
    {
        $query  = "SELECT COUNT(*) FROM GRD_BASE_MST WHERE SCHREGNO = '".$schregno."' ";

        $ret_val = $db->getOne($query);
        return ($ret_val) ? "GRD" : "SCHREG" ;
    }
}
?>
