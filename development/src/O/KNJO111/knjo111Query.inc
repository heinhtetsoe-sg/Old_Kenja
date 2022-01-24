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

    //賢者コード取得
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
    
    //賢者データ作成前にコードチェック
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
    
    //MEDEXAM_DET_DAT用データ取得
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
    
    //MEDEXAM_TOOTH_DAT用データ取得
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
