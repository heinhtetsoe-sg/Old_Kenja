<?php
class knjo103Query extends Query {
    //データの有無
    function countData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     H_APP_YOUROKU_SYUBETU_MST ";
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
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         H_APP_YOUROKU_TO_MST ";
        $query .= "     WHERE ";
        $query .= "         EDU_NNAME IS NOT NULL OR ";
        $query .= "         SCHOOL_NNAME IS NOT NULL ";
        $query .= "     ) ";
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
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         H_APP_YOUROKU_FROM_MST ";
        $query .= "     WHERE ";
        $query .= "         EDU_NNAME IS NOT NULL OR ";
        $query .= "         SCHOOL_NNAME IS NOT NULL ";
        $query .= "     ) ";
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
        $query .= "     H_APP_YOUROKU_SYUBETU_MST ";
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
        $query .= "     H_APP_Y_SCHOOL_CHANGE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞校長氏名
    function getPrincipal($schregno, $upper)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     H_APP_Y_PRINCIPAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞ホームルーム担任者氏名
    function getHrteacher($schregno, $upper)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     H_APP_Y_HRTEACHER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞生徒＞通称名
    function getAlias($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_STUDENT_ALIAS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='".$schregno."' ";
        
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞生徒＞現住所_その他
    function getStOtherAdd($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_STUDENT_OTHERADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞生徒＞保護者
    function getParent($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     H_APP_Y_PARENT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞生徒＞保護者＞現住所_その他
    function getParentOtrAdd($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_PARENT_OTHERADDR_DAT ";
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
        $query .= "     H_APP_Y_GRADE_DAT ";
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
        $query .= "     H_APP_Y_BACKGROUND_DAT ";
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
        $query .= "     H_APP_YOUROKU_IDOU_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞異動状況＞留学等
    function getAbroad($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_STAY_ABROAD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞各教科_科目等の修得単位数の記録＞各学科に共通する各教科_科目
    function getCmnSubj($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_COMMON_SUBJ_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞各教科_科目等の修得単位数の記録＞主として専門学科において開設される各教科_科目
    function getExpSubj($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_EXPERT_SUBJ_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録1
    function getShidou1($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_SHIDOU1_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録1＞各学科に共通する各教科_科目
    function getShi1CmnSubj($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_SHIDOU1_COMMON_SUBJ_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録1＞主として専門学科において開設される各教科_科目
    function getShi1ExpSubj($schregno, $upper, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_SHIDOU1_EXPERT_SUBJ_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     UPPER_DATA_ROW = '".$upper."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録2
    function getShidou2($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_Y_SHIDOU2_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."'  ";
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
        $query .= "     H_APP_YOUROKU_FROM_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //学校名取得
    function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
}
?>
