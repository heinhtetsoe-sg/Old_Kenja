<?php
class knjo152Query extends Query {
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
    function getGrade($schregno, $handicap)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.STAY_CNT, ";
        $query .= "     t2.CODENAME ";
        $query .= " FROM ";
        if($handicap != "003"){
            $query .= "     H_APP_Y_SHIDOU1_DAT t1  ";
        }else{
            $query .= "     H_APP_Y_SHIDOU2_DAT t1  ";
        }
        $query .= "     left join H_APP_NAME_MST t2 on t1.GRADE = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '01' and t2.DICTIONARY_NO in('03','05') ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.GRADE ";
        
        return $query;
    }
    //H_APP_Y_SHIDOU1or2_DATからデータ取得
    function getShidouData($schregno, $grade, $handicap)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.SYUBETU, ";
        if($handicap != "003"){
            $query .= "     t1.ABROAD_TANNI, ";
            $query .= "     t1.INTEGRATED_TANNI, ";
            $query .= "     t1.INTEGRATED_ACTION, ";
            $query .= "     t1.INTEGRATED_HYOUKA, ";
            $query .= "     t1.SPECIAL_ACTION, ";
            $query .= "     t1.ZIRITU_ACTION, ";
        }else{
            $query .= "     t1.RECORD, ";
            $query .= "     t1.TOTAL_CLASS_CNT, ";
            $query .= "     t1.ACTION, ";
            $query .= "     t1.HYOUKA, ";
        }
        $query .= "     t1.SYOKEN, ";
        $query .= "     t1.BIKOU, ";
        $query .= "     t2.CODENAME ";
        $query .= " FROM ";
        if($handicap != "003"){
            $query .= "     H_APP_Y_SHIDOU1_DAT t1 ";
        }else{
            $query .= "     H_APP_Y_SHIDOU2_DAT t1 ";
        }
        $query .= "     left join H_APP_NAME_MST t2 on t1.SYUBETU = t2.DICTIONARY_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '04' ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " AND  ";
        $query .= "     t1.GRADE = '".$grade."' ";
        
        return $query;
    }
    //教科取得
    function getKyouka($schregno, $grade)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     TYPE, ";
        $query .= "     CLASS_NAME, ";
        $query .= "     a2.CODENAME as KYOUKA ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         '1' as TYPE, ";
        $query .= "         t1.* ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_SHIDOU1_COMMON_SUBJ_DAT t1 ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         '2' as TYPE, ";
        $query .= "         t2.* ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_SHIDOU1_EXPERT_SUBJ_DAT t2 ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ) a1 ";
        $query .= "     left join (SELECT DISTINCT DICTIONARY_CODE, CODENAME FROM H_APP_CLASS_MST) a2 on a1.CLASS_NAME = a2.DICTIONARY_CODE  ";
        $query .= "     left join (SELECT DISTINCT DICTIONARY_CODE, CODENAME FROM H_APP_SUBCLASS_MST) a3 on a1.SUBCLASS_NAME = a3.DICTIONARY_CODE  ";
        $query .= " WHERE ";
        $query .= "     UPPER_DATA_ROW =  (SELECT ";
        $query .= "                             DATA_ROW ";
        $query .= "                         FROM ";
        $query .= "                             H_APP_Y_SHIDOU1_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO = '".$schregno."' AND ";
        $query .= "                             GRADE = '".$grade."' ";
        $query .= "                         ) ";
        $query .= " AND ";
        $query .= "     CLASS_NAME IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     TYPE,CLASS_NAME ";
        
        return $query;
    }
    //成績取得
    function getSeiseki($schregno, $grade, $kyouka="")
    {
        $query  = " SELECT ";
        $query .= "     CLASS_NAME, ";
        $query .= "     SUBCLASS_NAME, ";
        $query .= "     SUBCLASS_HYOUTEI, ";
        $query .= "     SUBCLASS_TANNI, ";
        $query .= "     SUBCLASS_TANNI_TOTAL, ";
        $query .= "     SUBCLASS_BIKOU, ";
        $query .= "     SCHOOL_CLASS_NAME, ";
        $query .= "     SCHOOL_SUBCLASS_NAME, ";
        $query .= "     SCHOOL_SUBCLASS_HYOUTEI, ";
        $query .= "     SCHOOL_SUBCLASS_TANNI, ";
        $query .= "     SCHOOL_SUBCLASS_TANNI_TOTAL, ";
        $query .= "     SCHOOL_SUBCLASS_BIKOU, ";
        $query .= "     case when SCHOOL_CLASS_NAME IS NOT NULL then SCHOOL_CLASS_NAME ";
        $query .= "     else a2.CODENAME end as KYOUKA, ";
        $query .= "     case when SCHOOL_SUBCLASS_NAME IS NOT NULL then SCHOOL_SUBCLASS_NAME ";
        $query .= "     else a3.CODENAME end as KAMOKU ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         '1' as TYPE, ";
        $query .= "         t1.* ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_SHIDOU1_COMMON_SUBJ_DAT t1 ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         '2' as TYPE, ";
        $query .= "         t2.* ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_SHIDOU1_EXPERT_SUBJ_DAT t2 ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ) a1 ";
        $query .= "     left join (SELECT DISTINCT DICTIONARY_CODE, CODENAME FROM H_APP_CLASS_MST) a2 on a1.CLASS_NAME = a2.DICTIONARY_CODE  ";
        $query .= "     left join (SELECT DISTINCT DICTIONARY_CODE, CODENAME FROM H_APP_SUBCLASS_MST) a3 on a1.SUBCLASS_NAME = a3.DICTIONARY_CODE  ";
        $query .= " WHERE ";
        $query .= "     UPPER_DATA_ROW =  (SELECT ";
        $query .= "                             DATA_ROW ";
        $query .= "                         FROM ";
        $query .= "                             H_APP_Y_SHIDOU1_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO = '".$schregno."' AND ";
        $query .= "                             GRADE = '".$grade."' ";
        $query .= "                         ) ";
        if($kyouka != ""){
            $query .= " AND ";
            if($kyouka != "99"){
                $query .= "     a1.CLASS_NAME = '".$kyouka."' ";
            }else{
                $query .= "     a1.CLASS_NAME IS NULL ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     TYPE,CLASS_NAME,SUBCLASS_NAME ";
        
        return $query;
    }
    
    //送信元情報取得
    function getSendFrom($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     H_APP_YOUROKU_FROM_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
}
?>
