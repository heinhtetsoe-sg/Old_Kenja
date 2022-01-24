<?php
class knjo112Query extends Query {
    //ƒGƒ‰[ƒ`ƒFƒbƒN
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
    //‘—MŒ³î•ñ„ŠwZ
    function getMotoSchool()
    {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME1, ";
        $query .= "     SCHOOLZIPCD, ";
        $query .= "     SCHOOLADDR1, ";
        $query .= "     SCHOOLADDR2, ";
        $query .= "     SCHOOLTELNO ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    
    //¶“kŒ’Nf’f•[î•ñ„¶“kŠw¶Šî–{î•ñ
    function getStudent($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.NAME, ";
        $query .= "     t1.NAME_SHOW, ";
        $query .= "     t1.NAME_KANA, ";
        $query .= "     t1.SEX, ";
        $query .= "     t1.BIRTHDAY, ";
        $query .= "     t1.NATIONALITY, ";
        $query .= "     t3.NAME1 as NATION, ";
        $query .= "     t2.ZIPCD, ";
        $query .= "     t2.ADDR1, ";
        $query .= "     t2.ADDR_FLG, ";
        $query .= "     t2.ADDR2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST t1 ";
        $query .= "     left join ( ";
        $query .= "                 SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_ADDRESS_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     EXPIREDATE = (SELECT ";
        $query .= "                                         MAX(EXPIREDATE) ";
        $query .= "                                     FROM ";
        $query .= "                                         SCHREG_ADDRESS_DAT ";
        $query .= "                                     WHERE ";
        $query .= "                                         SCHREGNO = '".$schregno."' ";
        $query .= "                                     ) ";
        $query .= "                 AND ";
        $query .= "                     SCHREGNO = '".$schregno."' ";
        $query .= "     ) t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "     left join NAME_MST t3 on t1.NATIONALITY = t3.NAMECD2 and t3.NAMECD1 = 'A024' ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //¶“kŒ’Nf’f•[î•ñ„ÝŠwŠw”N
    function getZaigaku($schregno, $mode = "0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "      SELECT DISTINCT ";
        $query .= "         t4.SCHOOLDIV, ";
        $query .= "         t5.DICTIONARY_CODE as SCHOOL_DIV, ";
        $query .= "         t1.SCHREGNO, ";
        $query .= "         t1.YEAR, ";
        $query .= "         t1.GRADE as GRADE0, ";
        $query .= "         t1.HR_CLASS, ";
        $query .= "         t1.ATTENDNO, ";
        $query .= "         t3.DICTIONARY_CODE as GRADE, ";
        $query .= "         t1.ANNUAL ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT t1 ";
        $query .= "        left join H_APP_NAME_MST t3 on t1.GRADE = t3.KENJA_CODE and t3.DICTIONARY_KIND = '01' and t3.DICTIONARY_NO = '03' ";
        $query .= "        left join SCHOOL_MST t4 on t1.YEAR = t4.YEAR ";
        $query .= "        left join H_APP_NAME_MST t5 on t4.SCHOOLDIV = t5.KENJA_CODE and t5.DICTIONARY_KIND = '01' and t5.DICTIONARY_NO = '02' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."'  AND ";
        $query .= "         t1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "     ORDER BY ";
        $query .= "         t1.YEAR ";
        if($mode != "0"){
            $query .= "     ) ";
        }
                
        return $query;
    }
    
    //¶“kŒ’Nf’f•[î•ñ„Œ’Nf’fˆê”Ê
    function getMedical($schregno, $mode = "0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.*, ";
        $query .= "         t2.NAME1 as NUTDISEASE, ";
        $query .= "         t3.NAME1 as SPINERIBDISEASE, ";
        $query .= "         t4.NAME1 as EYEDISEASE, ";
        $query .= "         t5.DET_REMARK7, ";
        $query .= "         t5.DET_REMARK8, ";
        $query .= "         t5.DET_REMARK9, ";
        $query .= "         t6.NAME1 as NOSEDISEASE, ";
        $query .= "         t7.NAME1 as SKINDISEASE, ";
        $query .= "         t8.NAME1 as TB_REMARK, ";
        $query .= "         t9.NAME1 as TB_OTHERTEST, ";
        $query .= "         t10.NAME1 as TB_NAME, ";
        $query .= "         t11.NAME1 as HEART_MEDEXAM_NAME, ";
        $query .= "         t12.NAME1 as HEARTDISEASE, ";
        $query .= "         t13.NAME1 as OTHERDISEASE, ";
        $query .= "         t14.NAME1 as DOCMESS, ";
        $query .= "         t15.NAME1 as TREAT ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DET_DAT t1 ";
        $query .= "         left join NAME_MST t2 on t1.NUTRITIONCD = t2.NAMECD2 and t2.NAMECD1 = 'F030' ";
        $query .= "         left join NAME_MST t3 on t1.SPINERIBCD = t3.NAMECD2 and t3.NAMECD1 = 'F040' ";
        $query .= "         left join NAME_MST t4 on t1.EYEDISEASECD = t4.NAMECD2 and t4.NAMECD1 = 'F050' ";
        $query .= "         left join MEDEXAM_DET_DETAIL_DAT t5 on t1.SCHREGNO = t5.SCHREGNO and t1.YEAR = t5.YEAR and t5.DET_SEQ = '003' ";
        $query .= "         left join NAME_MST t6 on t1.NOSEDISEASECD = t6.NAMECD2 and t6.NAMECD1 = 'F060' ";
        $query .= "         left join NAME_MST t7 on t1.SKINDISEASECD = t7.NAMECD2 and t7.NAMECD1 = 'F070' ";
        $query .= "         left join NAME_MST t8 on t1.TB_REMARKCD = t8.NAMECD2 and t8.NAMECD1 = 'F100' ";
        $query .= "         left join NAME_MST t9 on t1.TB_OTHERTESTCD = t9.NAMECD2 and t9.NAMECD1 = 'F110' ";
        $query .= "         left join NAME_MST t10 on t1.TB_NAMECD = t10.NAMECD2 and t10.NAMECD1 = 'F120' ";
        $query .= "         left join NAME_MST t11 on t1.HEART_MEDEXAM = t11.NAMECD2 and t11.NAMECD1 = 'F080' ";
        $query .= "         left join NAME_MST t12 on t1.HEARTDISEASECD = t12.NAMECD2 and t12.NAMECD1 = 'F090' ";
        $query .= "         left join NAME_MST t13 on t1.OTHERDISEASECD = t13.NAMECD2 and t13.NAMECD1 = 'F140' ";
        $query .= "         left join NAME_MST t14 on t1.DOC_CD = t14.NAMECD2 and t14.NAMECD1 = 'F144' ";
        $query .= "         left join NAME_MST t15 on t1.TREATCD = t15.NAMECD2 and t15.NAMECD1 = 'F150' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND ";
        $query .= "         t1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "     ORDER BY ";
        $query .= "         t1.YEAR ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //”AŒŸ¸ƒR[ƒh
    function getUricCD()
    {
        $query  = " SELECT ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        $query .= " FROM ";
        $query .= "     H_APP_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '03' AND ";
        $query .= "     DICTIONARY_NO = '06' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        
        return $query;
    }
    //”AŒŸ¸ƒ}[ƒNŽæ“¾
    function getUricMark()
    {
        $query  = " SELECT ";
        $query .= "     CODENAME, ";
        $query .= "     KENJA_CODE ";
        $query .= " FROM ";
        $query .= "     H_APP_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '03' AND ";
        $query .= "     DICTIONARY_NO = '06' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        
        return $query;
    }
    
    //Ž•‰ÈŒŸ¸
    function getTooth($schregno, $mode = "0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.*, ";
        $query .= "         t2.NAME1 as OTHERDISEASENAME, ";
        $query .= "         t3.NAME1 as DENTISTREMARK, ";
        $query .= "         t4.NAME1 as DENTISTTREAT ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_TOOTH_DAT t1 ";
        $query .= "         left join NAME_MST t2 on t1.OTHERDISEASECD = t2.NAMECD2 and t2.NAMECD1 = 'F530' ";
        $query .= "         left join NAME_MST t3 on t1.DENTISTREMARKCD = t3.NAMECD2 and t3.NAMECD1 = 'F540' ";
        $query .= "         left join NAME_MST t4 on t1.DENTISTTREATCD = t4.NAMECD2 and t4.NAMECD1 = 'F541' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND ";
        $query .= "         t1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "     ORDER BY ";
        $query .= "         t1.YEAR ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //ƒR[ƒhŽæ“¾
    function getCode($dicNo)
    {
        $query  = " SELECT ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        $query .= " FROM ";
        $query .= "     H_APP_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '03' AND ";
        $query .= "     DICTIONARY_NO = '".$dicNo."' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        
        return $query;
    }
    
}
?>
