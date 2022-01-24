<?php

require_once('for_php7.php');

class knjh410_medicalQuery extends Query {
    //$model->yearの初期値取得
    function getYearMax($schregno, $table)
    {
        $query  = " SELECT ";
        $query .= "     MAX(YEAR) ";
        $query .= " FROM ";
        $query .= "     ".$table." ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //生徒名取得
    function getSchregData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //健康診断のデータがある年度取得
    function getmedYear($schregno, $table)
    {
        $query .= " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t2.BIRTHDAY, ";
        $query .= "     YEAR((DATE(t1.YEAR || '-04-01') - DATE(t2.BIRTHDAY))) as AGE ";
        $query .= " FROM ";
        $query .= "     ".$table." t1 ";
        $query .= "     left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.YEAR DESC ";
        
        return $query;
    }
    
    //健康診断情報取得
    function getMedexamData($schregno)
    {
        $query .= " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.DATE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DAT t1  ";
        $query .= "     left join MEDEXAM_HDAT t2 on t1.YEAR = t2.YEAR and t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.YEAR DESC ";
        
        return $query;
    }
    
    //歯科検査情報取得
    function getToothData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.TOOTH_DATE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_TOOTH_DAT t1  ";
        $query .= "     left join MEDEXAM_HDAT t2 on t1.YEAR = t2.YEAR and t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.YEAR DESC ";
        
        return $query;
    }
    
    //変換コード取得用
    function getCode($namecd)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 in ('".$namecd."') ";
        
        return $query;
    }
}
?>
