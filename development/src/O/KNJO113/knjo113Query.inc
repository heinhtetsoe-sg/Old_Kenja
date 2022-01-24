<?php
class knjo113Query extends Query {
    //ƒf[ƒ^‚Ì—L–³
    function countData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     H_APP_HOKEN_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //‘—Mæî•ñ
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
        $query .= "         H_APP_HOKEN_TO_MST ";
        $query .= "     WHERE ";
        $query .= "         EDU_NNAME IS NOT NULL OR ";
        $query .= "         SCHOOL_NNAME IS NOT NULL ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //‘—MŒ³î•ñ
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
        $query .= "         H_APP_HOKEN_FROM_MST ";
        $query .= "     WHERE ";
        $query .= "         EDU_NNAME IS NOT NULL OR ";
        $query .= "         SCHOOL_NNAME IS NOT NULL ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //¶“kŒ’Nf’f•[î•ñ
    function getHokenMst($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     H_APP_HOKEN_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //¶“kŠw¶„Šî–{î•ñ„’ÊÌ–¼
    function getAlias($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_H_ALIAS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //¶“kŠw¶„Šî–{î•ñ„Œ»ZŠ_‚»‚Ì‘¼
    function getStAdd($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_H_OTHERADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //¶“kŠw¶„ÝŠwŠw”N
    function getZaigaku($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_H_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //Œ’Nf’f•[î•ñ„Œ’Nf’fˆê”Ê
    function getMedical($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_H_MEDICAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //Œ’Nf’f•[î•ñ„Ž•‰ÈŒŸ¸
    function getDental($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     H_APP_H_DENTAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //‘—M“úŽžŽæ“¾
    function getSendDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SEND_DATE ";
        $query .= " FROM ";
        $query .= "     H_APP_HOKEN_FROM_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //ŠwZ–¼Žæ“¾
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
