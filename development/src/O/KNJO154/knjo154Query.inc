<?php
class knjo154Query extends Query {
    //生徒基本情報取得
    function getStudent($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     NAME, ";
        $query .= "     BIRTHDAY ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //対象生徒の学年取得
    function getYear($schregno, $schoolKind)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= "     FROM ";
        if($schoolKind != "H"){
            $query .= "         CE_APP_H_MEDICAL_DAT ";
        }else{
            $query .= "         H_APP_H_MEDICAL_DAT ";
        }
        $query .= "     UNION   ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= "     FROM ";
        if($schoolKind != "H"){
            $query .= "         CE_APP_H_DENTAL_DAT ";
        }else{
            $query .= "         H_APP_H_DENTAL_DAT ";
        }
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        
        return $query;
    }
    //対象生徒のSCHOOLKIND取得
    function getSchoolKind($schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1  ";
        $query .= "     left join SCHREG_REGD_GDAT t2 on t1.YEAR = t2.YEAR and t1.GRADE = t2.GRADE ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //変換コード取得
    function getCode($code, $schoolKind)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     CODENAME ";
        $query .= " FROM ";
        if($schoolKind != "H"){
            $query .= "     CE_APP_NAME_MST ";
        }else{
            $query .= "     H_APP_NAME_MST ";
        }
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '03' AND ";
        $query .= "     DICTIONARY_NO = '".$code."' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE ";
        
        return $query;
    }
    
    //対象のデータ取得
    function getMedicalData($schregno, $year, $schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        if($schoolKind != "H"){
            $query .= "     CE_APP_H_MEDICAL_DAT ";
        }else{
            $query .= "     H_APP_H_MEDICAL_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND  ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }
    //歯科検査データ
    function getDentalData($schregno, $year, $schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     OTHER_DISORDER, ";
        $query .= "     OTHER_DISEASE_GNAME, ";
        $query .= "     OTHER_DISEASE_NNAME, ";
        $query .= "     SCH_DENTIST_SYOKEN, ";
        $query .= "     SCH_DENTIST_DATE, ";
        $query .= "     AFTERCARE ";
        $query .= " FROM ";
        if($schoolKind != "H"){
            $query .= "     CE_APP_H_DENTAL_DAT ";
        }else{
            $query .= "     H_APP_H_DENTAL_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND  ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }
}
?>
