<?php
class knjo103Query extends Query {
    //�f�[�^�̗L��
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
    //���M����
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
    //���M�����
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
    
    //�w���v�^���
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
    //�w���v�^��񁄊w�Z�ύX��
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
    
    //�w���v�^��񁄊w�Z�ύX�󋵁��Z������
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
    
    //�w���v�^��񁄊w�Z�ύX�󋵁��z�[�����[���S�C�Ҏ���
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
    
    //�w���v�^��񁄊w�Ђ̋L�^�����k���ʏ̖�
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
    
    //�w���v�^��񁄊w�Ђ̋L�^�����k�����Z��_���̑�
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
    
    //�w���v�^��񁄊w�Ђ̋L�^�����k���ی��
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
    
    //�w���v�^��񁄊w�Ђ̋L�^�����k���ی�ҁ����Z��_���̑�
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
    
    //�w���v�^��񁄊w�Ђ̋L�^���݊w�󋵁��݊w�w�N
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
    
    //�w���v�^��񁄊w�Ђ̋L�^���ٓ��󋵁����w�O�̌o��
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
    
    //�w���v�^��񁄊w�Ђ̋L�^���ٓ���
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
    
    //�w���v�^��񁄊w�Ђ̋L�^���ٓ��󋵁����w��
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
    
    //�w���v�^��񁄊e����_�Ȗړ��̏C���P�ʐ��̋L�^���e�w�Ȃɋ��ʂ���e����_�Ȗ�
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
    
    //�w���v�^��񁄊e����_�Ȗړ��̏C���P�ʐ��̋L�^����Ƃ��Đ��w�Ȃɂ����ĊJ�݂����e����_�Ȗ�
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
    
    //�w���v�^��񁄎w���Ɋւ���L�^1
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
    
    //�w���v�^��񁄎w���Ɋւ���L�^1���e�w�Ȃɋ��ʂ���e����_�Ȗ�
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
    
    //�w���v�^��񁄎w���Ɋւ���L�^1����Ƃ��Đ��w�Ȃɂ����ĊJ�݂����e����_�Ȗ�
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
    
    //�w���v�^��񁄎w���Ɋւ���L�^2
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
    
    //���M�����擾
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
    
    //�w�Z���擾
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