<?php
class knjo122Query extends Query {
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
    
    //指導要録情報＞学校変更状況＞年度・学年
    function getChangeYear($schregno, $schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t2.GRADE, ";
        $query .= "     t3.DICTIONARY_CODE as GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST t1  ";
        $query .= "     left join SCHREG_REGD_DAT t2 on t1.YEAR = t2.YEAR and t2.SCHREGNO = '".$schregno."'  ";
        $query .= "     left join CE_APP_NAME_MST t3 on t2.GRADE = t3.KENJA_CODE and t3.DICTIONARY_KIND = '01' and t3.DICTIONARY_NO = '02' ";
        $query .= " WHERE ";
        $query .= "     t1.SCHOOL_KIND = '".$schoolKind."' AND ";
        $query .= "     (t2.YEAR, t2.SEMESTER) in (SELECT ";
        $query .= "                                     YEAR, ";
        $query .= "                                     MAX(SEMESTER) ";
        $query .= "                                 FROM ";
        $query .= "                                     SCHREG_REGD_DAT ";
        $query .= "                                 WHERE ";
        $query .= "                                     SCHREGNO = '".$schregno."' ";
        $query .= "                                 GROUP BY ";
        $query .= "                                     YEAR ";
        $query .= "                                 ) ";
        $query .= " ORDER BY ";
        $query .= "     t1.YEAR ";
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞様式種別_版を取得
    function getStyleVer($year, $schoolkind)
    {
        $query  = " SELECT DISTINCT   ";
        $query .= "     STYLE_VER   ";
        $query .= " FROM   ";
        $query .= "     REFERENCE_STYLE_VER   ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '".$year."'  ";
        $query .= " AND  ";
        $query .= "     SCHOOL_KIND = '".$schoolkind."' "; 
        
        return $query;
    }

    //指導要録情報＞学校変更状況＞校長氏名
    function getPrincipal($syubetu, $year, $mode = "")
    {
        $start = $year."-04-01";
        $end = ($year+1)."-03-31";
        
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        
        $query .= "     SELECT ";
        $query .= "         t2.STAFFNAME, ";
        $query .= "         t2.STAFFNAME_KANA ";
        $query .= "     FROM ";
        $query .= "         STAFF_PRINCIPAL_HIST_DAT t1 ";
        $query .= "         left join STAFF_MST t2 on t1.STAFFCD = t2.STAFFCD ";
        $query .= "     WHERE ";
        $query .= "         (TO_DATE > '".$start."' OR TO_DATE IS NULL)  ";
        $query .= "     AND ";
        $query .= "         FROM_DATE < '".$end."'  ";
        $query .= "     AND ";
        $query .= "         SCHOOL_KIND = '".$syubetu."' ";
        
        if($mode == ""){
            $query .= "     ORDER BY ";
            $query .= "         FROM_DATE DESC ";
        }
        
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞校長氏名(PRINCIPAL_HIST_DATにデータがない場合)
    function getCertif($syubetu, $year)
    {
        $query  = " SELECT ";
        $query .= "     PRINCIPAL_NAME ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     CERTIF_KINDCD = '".$syubetu."'  ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞ホームルーム担任者氏名
    function getHRteacher($schregno, $year)
    {
        $query  = "      SELECT ";
        $query .= "         a1.TR_CD1, ";
        $query .= "         a2.STAFFNAME, ";
        $query .= "         a2.STAFFNAME_KANA ";
        $query .= "      FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             t2.TR_CD1 ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT t1 ";
        $query .= "             left join SCHREG_REGD_HDAT t2 on t1.YEAR = t2.YEAR and t1.SEMESTER = t2.SEMESTER and t1.GRADE = t2.GRADE and t1.HR_CLASS = t2.HR_CLASS ";
        $query .= "         WHERE ";
        $query .= "             t1.SCHREGNO = '".$schregno."' ";
        $query .= "         AND ";
        $query .= "             t1.YEAR = '".$year."' ";
        $query .= "         ) a1 ";
        $query .= "      left join STAFF_MST a2 on a1.TR_CD1 = a2.STAFFCD ";
        
        return $query;
    }
    
    //指導要録情報＞学籍に関する記録＞児童生徒＞氏名～国籍まで
    function getSchregData($schregno)
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
    
    //指導要録情報＞学籍に関する記録＞児童生徒＞保護者
    function getGuardianData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     GUARD_NAME, ";
        $query .= "     GUARD_KANA, ";
        $query .= "     GUARD_ADDR1, ";
        $query .= "     GUARD_ADDR2, ";
        $query .= "     GUARD_ZIPCD ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         GUARDIAN_DAT ";
        $query .= "     UNION   ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         GUARDIAN2_DAT ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     RELATIONSHIP ";
        
        return $query;
    }
    
    //指導要録情報＞学籍に関する記録＞在学状況＞在学学年
    function getZaigakuGrade($schregno, $syubetu)
    {
        $query  = " SELECT DISTINCT  ";
        $query .= "    t1.SCHREGNO,  ";
        $query .= "    t1.YEAR,  ";
        $query .= "    t1.GRADE as GRADECODE,  ";
        $query .= "    t1.HR_CLASS,  ";
        $query .= "    t1.ATTENDNO,  ";
        $query .= "    t3.DICTIONARY_CODE as GRADE,  ";
        $query .= "    t4.HR_CLASS_NAME1,  ";
        $query .= "    t5.NAME1  ";
        $query .= " FROM  ";
        $query .= "    SCHREG_REGD_DAT t1  ";
        $query .= "    left join SCHREG_REGD_GDAT t2 on t1.YEAR = t2.YEAR and t1.GRADE = t2.GRADE and SCHOOL_KIND = '".$syubetu."'  ";
        $query .= "    left join CE_APP_NAME_MST t3 on t2.GRADE = t3.KENJA_CODE and t3.DICTIONARY_KIND = '01' and t3.DICTIONARY_NO = '02'  and substr(t3.DICTIONARY_CODE, 1, 1) = '".$syubetu."' ";
        $query .= "    left join ( ";
        $query .= "                 SELECT ";
        $query .= "                     T2.YEAR, ";
        $query .= "                     T2.hr_class_name1, ";
        $query .= "                     T2.GRADE, ";
        $query .= "                     T2.HR_CLASS ";
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
        $query .= "                     ) as T1 left join schreg_regd_hdat T2 on T1.year = T2.year and T1.semester = T2.semester and T1.grade = T2.grade and T1.hr_class = T2.hr_class ";
        $query .= "  ";
        $query .= "     ) t4 on t1.YEAR = t4.YEAR and t1.GRADE = t4.GRADE and t1.HR_CLASS = t4.HR_CLASS  ";
        $query .= "    left join NAME_MST t5 on t1.HR_CLASS = t5.NAMECD2 and t5.NAMECD1 = 'A021'  ";
        $query .= " WHERE  ";
        $query .= "    t1.SCHREGNO = '".$schregno."'  ";
        $query .= " AND  ";
        $query .= "    t1.YEAR <= '".CTRL_YEAR."'  ";
        $query .= " AND ";
        $query .= "     t3.KENJA_CODE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     t1.YEAR ";
        
        
        return $query;
    }
    
    //指導要録情報＞学籍に関する記録＞異動状況＞入学～卒業
    function getIdou($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.*, ";
        $query .= "     t3.BASE_REMARK1 ,";
        $query .= "     t4.FINSCHOOL_NAME ,";
        $query .= "     t4.FINSCHOOL_TYPE ,";
        $query .= "     t4.FINSCHOOLCD ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         NAME, ";
        $query .= "         case when SUBSTR(ENT_DATE,6,2) > '03' then SUBSTR(ENT_DATE, 1, 4) ";
        $query .= "         else int(SUBSTR(ENT_DATE, 1, 4)) - 1 end as YEAR, ";
        $query .= "         ENT_DATE, ";
        $query .= "         ENT_DIV, ";
        $query .= "         ENT_REASON, ";
        $query .= "         GRD_DIV, ";
        $query .= "         GRD_DATE, ";
        $query .= "         GRD_REASON ";
        $query .= "      FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "      WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ) t1 ";
        $query .= "     left join ( ";
        $query .= "                 SELECT DISTINCT ";
        $query .= "                     a1.YEAR, ";
        $query .= "                     a1.SCHREGNO, ";
        $query .= "                     a2.DICTIONARY_CODE as GRADE, ";
        $query .= "                     a1.ANNUAL ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT a1 ";
        $query .= "                     left join CE_APP_NAME_MST a2 on a1.GRADE = a2.KENJA_CODE and a2.DICTIONARY_KIND = '01' and a2.DICTIONARY_NO = '02' ";
        $query .= "                 WHERE ";
        $query .= "                     a1.SCHREGNO = '".$schregno."' ";
        $query .= "     ) t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = t2.YEAR ";
        $query .= "     left join SCHREG_BASE_DETAIL_MST t3 on t1.SCHREGNO = t3.SCHREGNO and t3.BASE_SEQ = '001' ";
        $query .= "     left join ( ";
        $query .= "                 SELECT DISTINCT ";
        $query .= "                     b1.SCHREGNO, ";
        $query .= "                     b1.FINSCHOOLCD ,";
        $query .= "                     b2.FINSCHOOL_NAME, ";
        $query .= "                     b2.FINSCHOOL_TYPE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_ENT_GRD_HIST_DAT b1 ";
        $query .= "                     left join FINSCHOOL_MST b2 on b1.FINSCHOOLCD = b2.FINSCHOOLCD  ";
        $query .= "                 WHERE ";
        $query .= "                     b1.SCHREGNO = '".$schregno."' ";
        $query .= "     ) t4 on t1.SCHREGNO = t4.SCHREGNO";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //指導要録情報＞学籍に関する記録＞異動状況＞進学先就職先等
    function getGrdAftCnt($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SENKOU_KIND, ";
        $query .= "     COUNT(*) as COUNT ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND ";
        $query .= "     PLANSTAT = '1' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO,  ";
        $query .= "     SENKOU_KIND ";
        
        return $query;
    }
    
    //指導要録情報＞学籍に関する記録＞異動状況＞進学先
    function getGrdAftSch($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.STAT_CD, ";
        $query .= "     t2.SCHOOL_NAME, ";
        $query .= "     t3.FACULTYNAME, ";
        $query .= "     t4.DEPARTMENTNAME, ";
        $query .= "     replace(t1.THINKEXAM, CHR(13)||CHR(10), ' ') as THINKEXAM ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT t1 ";
        $query .= "     left join COLLEGE_MST t2 on t1.STAT_CD = t2.SCHOOL_CD ";
        $query .= "     left join COLLEGE_FACULTY_MST t3 on t1.STAT_CD = t3.SCHOOL_CD and t1.FACULTYCD = t3.FACULTYCD ";
        $query .= "     left join COLLEGE_DEPARTMENT_MST t4 on t1.STAT_CD = t4.SCHOOL_CD and t1.FACULTYCD = t4.FACULTYCD and t1.DEPARTMENTCD = t4.DEPARTMENTCD ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     t1.SENKOU_KIND = '0' AND ";
        $query .= "     t1.PLANSTAT = '1' ";

        return $query;
    }
    
    //指導要録情報＞学籍に関する記録＞異動状況＞就職先
    function getGrdAftComp($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.STAT_CD, ";
        $query .= "     t2.COMPANY_NAME, ";
        $query .= "     t1.JOB_THINK ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT t1 ";
        $query .= "     left join COMPANY_MST t2 on t1.STAT_CD = t2.COMPANY_CD ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."'  ";
        $query .= " AND ";
        $query .= "     t1.SENKOU_KIND = '1'  ";
        $query .= " AND ";
        $query .= "     t1.PLANSTAT = '1' ";

        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞学年とYEARを取得
    function getGrade($schregno, $syubetu)
    {
        $query .= " SELECT DISTINCT   ";
        $query .= "     t1.SCHREGNO,   ";
        $query .= "     t1.YEAR,   ";
        $query .= "     t1.GRADE,   ";
        $query .= "     t2.DICTIONARY_CODE as GRADE_CD, ";
        $query .= "     t4.HANDICAP ";
        $query .= " FROM   ";
        $query .= "     SCHREG_REGD_DAT t1   ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.GRADE = t2.KENJA_CODE and t2.DICTIONARY_KIND = '01' and t2.DICTIONARY_NO = '02' and substr(t2.DICTIONARY_CODE, 1, 1) = '".$syubetu."'  ";
        $query .= "     left join SCHREG_REGD_GDAT t3 on t1.YEAR = t3.YEAR and t1.GRADE = t3.GRADE  ";
        $query .= "     left join SCHREG_BASE_MST t4 on t1.SCHREGNO = t4.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     t1.SCHREGNO = '".$schregno."'  ";
        $query .= " AND  ";
        $query .= "     t2.KENJA_CODE IS NOT NULL  ";
        $query .= " AND  ";
        $query .= "     t3.SCHOOL_KIND = '".$syubetu."' ";  //念のため
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞各教科の学習の記録＞観点別学習状況
    function getPoint($year, $schregno, $schoolkind)
    {
        $query  = " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t1.VIEWCD, ";
        $query .= "     t1.STATUS, ";
        $query .= "     t2.VIEWNAME, ";
        $query .= "     t3.DICTIONARY_CODE as CLASSCD, ";
        $query .= "     t4.DICTIONARY_CODE as VIEWCODE, ";
        $query .= "     t5.CLASSNAME , ";
        $query .= "     t5.ELECTDIV ";//必修選択追加20210118~
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUB_DAT t1  ";
        $query .= "     left join JVIEWNAME_SUB_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD and t1.VIEWCD = t2.VIEWCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND";
        $query .= "     left join CE_APP_NAME_MST t3 on t1.SUBCLASSCD = t3.KENJA_CODE and t3.DICTIONARY_KIND = '02' and t3.DICTIONARY_NO = '02' and substr(t3.DICTIONARY_CODE, 1, 1) = '".$schoolkind."' ";
        $query .= "     left join CE_APP_NAME_MST t4 on t1.VIEWCD = t4.KENJA_CODE and t4.DICTIONARY_KIND = '02' and t4.DICTIONARY_NO = '03' and substr(t3.DICTIONARY_CODE, 1, 1) = '".$schoolkind."' ";
        $query .= "     left join CLASS_MST t5 on t1.CLASSCD = t5.CLASSCD and t1.SCHOOL_KIND = t5.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     t1.YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t1.VIEWCD ";
        
        return $query;
    }

    //指導要録情報＞指導に関する記録＞各教科の学習の記録＞評定情報
    function getCurriculumRating($year, $schregno, $schoolkind)
    {
        $query  = " SELECT ";
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t1.VALUATION, ";
        $query .= "     t2.DICTIONARY_CODE as CLASSCD, ";
        $query .= "     t3.CLASSNAME , ";
        $query .= "     t3.ELECTDIV ";//必修選択追加20210118~
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT t1  ";
        $query .= "     left join CE_APP_NAME_MST t2 on t1.SUBCLASSCD = t2.KENJA_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '02' and substr(t2.DICTIONARY_CODE, 1, 1) = '".$schoolkind."' ";
        $query .= "     left join CLASS_MST t3 on t1.CLASSCD = t3.CLASSCD and t1.SCHOOL_KIND = t3.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     t1.YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SUBCLASSCD ";
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞特別の教科_道徳
    function getMoral($year, $schregno)
    {
        $query  = "     SELECT ";
        $query .= "         FOREIGNLANGACT4 ";
        $query .= "     FROM ";
        $query .= "         HTRAINREMARK_P_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' AND ";
        $query .= "         YEAR = '".$year."' ";

        return $query;
    }

    //指導要録情報＞指導に関する記録＞外国語活動の記録
    function getForeignAct($year, $schregno, $mode="0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         FOREIGNLANGACT1, ";
        $query .= "         FOREIGNLANGACT2, ";
        $query .= "         FOREIGNLANGACT3 ";
        $query .= "     FROM ";
        $query .= "         HTRAINREMARK_P_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' AND ";
        $query .= "         YEAR = '".$year."' ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞総合的な学習の時間の記録
    function getIntegAct($year, $schregno, $table, $mode="0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         TOTALSTUDYACT, ";
        $query .= "         replace(replace(VIEWREMARK, CHR(10),'/'),CHR(13),'/') as VIEWREMARK, ";
        $query .= "         TOTALSTUDYVAL ";
        $query .= "     FROM ";
        $query .= "         ".$table." ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' AND ";
        $query .= "         YEAR = '".$year."' ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞特別活動の記録
    function getSpAct($year, $schregno, $table, $schoolkind, $mode="0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.*, ";
        $query .= "         t2.DICTIONARY_CODE as POINT, ";
        $query .= "         t3.NAME1, ";
        //$query .= "         replace(replace(replace(t4.SPECIALACTREMARK, CHR(10),''),CHR(13),''), '/', '') as SPECIALACTREMARK, ";
        $query .= "         t4.SPECIALACTREMARK, ";
        $query .= "         t5.DICTIONARY_CODE as RECORDCD ";
        $query .= "     FROM ";
        $query .= "         BEHAVIOR_DAT t1  ";
        $query .= "         left join CE_APP_NAME_MST t2 on t1.CODE = t2.KENJA_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '04' ";
        $query .= "         left join NAME_MST t3 on t1.CODE = t3.NAMECD2 and t3.NAMECD1 = 'D034' ";
        $query .= "         left join ".$table." t4 on t1.SCHREGNO = t4.SCHREGNO and t1.YEAR = t4.YEAR ";
        $query .= "         left join CE_APP_NAME_MST t5 on t1.RECORD = t5.KENJA_CODE and t5.DICTIONARY_KIND = '02' and t5.DICTIONARY_NO = '08' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "         t1.YEAR = '".$year."' AND ";
        if($schoolkind == "J"){
            $query .= "         t1.DIV = '2'  ";
        }else{
            $query .= "         t1.DIV = '4' ";
        }
        //2020-01上記取得だと、無しの場合がとれないのでコメントアウト。
        // $query .= "      AND t1.RECORD = '1' ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞行動の記録
    function getBehavior($year, $schregno, $schoolkind, $mode="0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.*, ";
        $query .= "         t2.DICTIONARY_CODE as POINT, ";
        $query .= "         t3.CODENAME, ";
        $query .= "         t5.DICTIONARY_CODE as RECORDCD ";
        $query .= "     FROM ";
        $query .= "         BEHAVIOR_DAT t1  ";
        $query .= "         left join CE_APP_NAME_MST t2 on t1.CODE = t2.KENJA_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '05' ";
        $query .= "         left join BEHAVIOR_SEMES_MST t3 on t1.YEAR = t3.YEAR and t1.ANNUAL = t3.GRADE and t1.CODE = t3.CODE ";
        $query .= "         left join CE_APP_NAME_MST t5 on t1.RECORD = t5.KENJA_CODE and t5.DICTIONARY_KIND = '02' and t5.DICTIONARY_NO = '08' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "         t1.YEAR = '".$year."' AND ";
        if($schoolkind == "J"){
            $query .= "         t1.DIV = '1' ";
        }else{
            $query .= "         t1.DIV = '3' ";
        }
        // $query .= "       AND  t1.RECORD = '1' ";
        $query .= "     ORDER BY t1.CODE ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }

    //指導要録情報＞指導に関する記録＞自立活動の記録＞自立の状況(様式種別02のみ)
    function getActIndependence($schregno, $year){
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     HTRAIN_SEQ = '001' ";

        return $query;
    }

    //指導要録情報＞指導に関する記録＞行動の記録＞評価(様式種別03のみ)
    function getActScoreSp($schregno, $year){
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     HTRAIN_SEQ = '002' ";

        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞各教科_特別活動_自立活動の記録
    //テストテーブルで代用(APPLIC_TEST_TABLE1)開始。
    function getAllAct($year, $schregno, $table, $schoolkind, $mode="0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         * ,";
        $query .= "         t2.CLASSNAME ,";
        $query .= "         t3.DICTIONARY_CODE as CLASSCD ";
        $query .= "     FROM ";
        $query .= "     APPLIC_TEST_TABLE1 t1";
        $query .= "     left join CLASS_MST t2 on t1.CLASSCD = t2.CLASSCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
        $query .= "     left join CE_APP_NAME_MST t3 on t1.SUBCLASSCD = t3.KENJA_CODE AND t3.DICTIONARY_KIND = '02' and t3.DICTIONARY_NO = '02' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "         t1.YEAR = '".$year."' AND";
        $query .= "         t1.SCHOOL_KIND = '".$schoolkind."'";
        if($mode != "0"){
            $query .= "     ) ";
        }
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞総合的所見及び指導上参考となる諸事項
    function getSyoken($year, $schregno, $table, $mode="0")
    {
        if($mode != "0"){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         replace(TOTALREMARK, CHR(13)||CHR(10),'　') as TOTALREMARK ";
        $query .= "     FROM ";
        $query .= "         ".$table." ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' AND ";
        $query .= "         YEAR = '".$year."' AND  ";
        $query .= "         TOTALREMARK IS NOT NULL ";
        if($mode != "0"){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録＞出欠の記録
    function getAttend($year, $schregno, $table)
    {
        $query  = " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.ANNUAL, ";
        $query .= "     t1.CLASSDAYS, ";
        $query .= "     t1.SUSPEND + t1.MOURNING as STOPCNT, ";
        $query .= "     t1.CLASSDAYS - (t1.SUSPEND + t1.MOURNING) as MUSTCNT, ";
        $query .= "     t1.SICK + t1.ACCIDENTNOTICE + t1.NOACCIDENTNOTICE as ABSECNT, ";
        $query .= "     t1.PRESENT, ";
        $query .= "     t2.ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT t1 ";
        $query .= "     left join ".$table." t2 on t1.YEAR = t2.YEAR and t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$year."' AND ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //指導要録情報＞入学時の障害の状態＞障害の状態
    function getEntCondition($schregno){
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     HTRAIN_SEQ = '001' ";

        return $query;
    }

    //１レコード取得（学籍在籍データ）
    public function getSchregregdData($model)
    {
        $db = Query::dbCheckOut();

        $header = knjo122Query::getJudgeTable($model->SCHREGNO, $db);

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
