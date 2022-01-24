<?php
class knjo132Query extends Query {
    //エラーチェック
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

    //送信元情報＞学校
    function getMotoSchool($schoolKind)
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
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolKind."' ";

        return $query;
    }
    
    //生徒健康診断票情報＞生徒学生基本情報
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
    
    //生徒健康診断票情報＞在学学年
    function getZaigaku($schregno, $mode = "0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "      SELECT DISTINCT ";
        $query .= "         t1.SCHREGNO, ";
        $query .= "         t1.YEAR, ";
        $query .= "         t1.GRADE as GRADE0, ";
        $query .= "         t1.HR_CLASS, ";
        $query .= "         t1.ATTENDNO, ";
        $query .= "         t3.DICTIONARY_CODE as GRADE, ";
        $query .= "         t4.HR_CLASS_NAME1 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT t1 ";
        $query .= "         left join CE_APP_NAME_MST t3 on t1.GRADE = t3.KENJA_CODE and t3.DICTIONARY_KIND = '01' and t3.DICTIONARY_NO = '02' ";
        $query .= "         left join SCHREG_REGD_GDAT t2 on t1.YEAR = t2.YEAR and t1.GRADE = t2.GRADE ";
        $query .= "         left join ( ";
        $query .= "                 SELECT ";
        $query .= "                     a2.YEAR, ";
        $query .= "                     a2.hr_class_name1, ";
        $query .= "                     a2.GRADE, ";
        $query .= "                     a2.HR_CLASS ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         year, ";
        $query .= "                         max(semester) as semester, ";
        $query .= "                         grade, ";
        $query .= "                         hr_class ";
        $query .= "                     FROM ";
        $query .= "                         schreg_regd_hdat ";
        $query .= "                     GROUP BY ";
        $query .= "                         year, ";
        $query .= "                         grade, ";
        $query .= "                         hr_class ";
        $query .= "                     ) as a1 left join schreg_regd_hdat a2 on a1.year = a2.year and a1.semester = a2.semester and a1.grade = a2.grade and a1.hr_class = a2.hr_class ";
        $query .= "  ";
        $query .= "     ) t4 on t1.YEAR = t4.YEAR and t1.GRADE = t4.GRADE and t1.HR_CLASS = t4.HR_CLASS  ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."'  AND ";
        $query .= "         t1.YEAR <= '".CTRL_YEAR."' AND ";
        $query .= "         t3.KENJA_CODE IS NOT NULL ";
        $query .= "     AND  ";
        $query .= "         t2.SCHOOL_KIND != 'H' ";
        $query .= "     ORDER BY ";
        $query .= "         t1.YEAR ";
        if($mode != "0"){
            $query .= "     ) ";
        }
                
        return $query;
    }
    
    //生徒健康診断票情報＞健康診断一般
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
        $query .= "         t15.NAME1 as TREAT ,";
        $query .= "         t16.DICTIONARY_CODE as TB_ADVISE, ";
        $query .= "         t17.DET_REMARK1 as TREATREMARK ";  
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
        $query .= "         left join CE_APP_NAME_MST t16 on t1.TB_ADVISECD = t16.KENJA_CODE and t16.DICTIONARY_KIND = '03' and DICTIONARY_NO = '05'";
        $query .= "         left join MEDEXAM_DET_DETAIL_DAT t17 on t1.YEAR = t17.YEAR and t1.SCHREGNO = t17.SCHREGNO and t17.DET_SEQ = '004' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' ";
        $query .= "     ORDER BY ";
        $query .= "         t1.YEAR ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //尿検査マーク取得
    function getUricMark()
    {
        $query  = " SELECT ";
        $query .= "     CODENAME, ";
        $query .= "     KENJA_CODE ";
        $query .= " FROM ";
        $query .= "     CE_APP_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '03' AND ";
        $query .= "     DICTIONARY_NO = '06' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        
        return $query;
    }
    
    //歯科検査
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
        $query .= "         t3.NAME1 as DENTISTREMARKNAME, ";
        $query .= "         t4.NAME1 as DENTISTTREATNAME ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_TOOTH_DAT t1 ";
        $query .= "         left join NAME_MST t2 on t1.OTHERDISEASECD = t2.NAMECD2 and t2.NAMECD1 = 'F530' ";
        $query .= "         left join NAME_MST t3 on t1.DENTISTREMARKCD = t3.NAMECD2 and t3.NAMECD1 = 'F540' ";
        $query .= "         left join NAME_MST t4 on t1.DENTISTTREATCD = t4.NAMECD2 and t4.NAMECD1 = 'F541' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' ";
        $query .= "     ORDER BY ";
        $query .= "         t1.YEAR ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //コード取得
    function getCode($dicNo)
    {
        $query  = " SELECT ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        $query .= " FROM ";
        $query .= "     CE_APP_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '03' AND ";
        $query .= "     DICTIONARY_NO = '".$dicNo."' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        
        return $query;
    }

    //NAME_MSTのNAMESPARE2取得(テキストボックス利用か判定)
    function getNameSpare2($NAMECD1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$NAMECD1."' AND ";
        $query .= "     NAMESPARE2 = '1'";
        return $query;
    }

    //１レコード取得（学籍在籍データ）
    public function getSchregregdData($model)
    {
        $db = Query::dbCheckOut();

        $header = knjo132Query::getJudgeTable($model->SCHREGNO, $db);

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
