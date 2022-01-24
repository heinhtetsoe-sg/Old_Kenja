<?php
class knjo111Query extends Query {
    //DELETE
    function delete($schregno, $table)
    {
        $query  = " DELETE FROM ";
        $query .= "     ".$table." ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }

    //���҃R�[�h�擾
    function getKenjaCode($no)
    {
        $query  = " SELECT ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        $query .= " FROM ";
        $query .= "     H_APP_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '03' AND ";
        $query .= "     DICTIONARY_NO = '".$no."' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        
        return $query;
    }
    
    //���҃f�[�^�쐬�O�ɃR�[�h�`�F�b�N
    function getKubunCheck($schregno)
    {
        $query  = " SELECT ";
        $query .= "     KUBUN, ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     H_APP_H_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //MEDEXAM_DET_DAT�p�f�[�^�擾
    function getMedexamData($schregno, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     H_APP_H_MEDICAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "    YEAR ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //MEDEXAM_TOOTH_DAT�p�f�[�^�擾
    function getToothData($schregno, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         H_APP_H_DENTAL_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ORDER BY ";
        $query .= "         YEAR ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
}
?>