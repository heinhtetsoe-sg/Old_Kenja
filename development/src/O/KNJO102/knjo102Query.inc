<?php
class knjo102Query extends Query {
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
    
    //障害取得
    function getHandicap($schregno)
    {
        $query  = " SELECT ";
        $query .= "     HANDICAP ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //指導要録情報＞学校変更状況＞年度・教育課程の区分
    function getChangeYear($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t2.SCHOOLDIV, ";
        $query .= "     t3.DICTIONARY_CODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1  ";
        $query .= "     left join SCHOOL_MST t2 on t1.YEAR = t2.YEAR  ";
        $query .= "     left join H_APP_NAME_MST t3 on t2.SCHOOLDIV = t3.KENJA_CODE and t3.DICTIONARY_KIND = '01' and t3.DICTIONARY_NO = '02' ";
        $query .= " WHERE ";
        $query .= "     (t1.SCHREGNO, t1.YEAR, t1.SEMESTER) in (SELECT ";
        $query .= "                                                 SCHREGNO, ";
        $query .= "                                                 YEAR, ";
        $query .= "                                                 MAX(SEMESTER) ";
        $query .= "                                             FROM ";
        $query .= "                                                 SCHREG_REGD_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 SCHREGNO = '".$schregno."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 SCHREGNO, ";
        $query .= "                                                 YEAR ";
        $query .= "                                             ) ";
        $query .= " AND ";
        $query .= "     t1.YEAR <= '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞学年・原級留置回数
    function getChangeGrade($schregno, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.GRADE as GRADE0, ";
        $query .= "     t3.DICTIONARY_CODE as GRADE, ";
        $query .= "     t1.ANNUAL, ";
        $query .= "     t2.COUNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1  ";
        $query .= "     left join ( ";
        $query .= "                 SELECT ";
        $query .= "                     schregno, ";
        $query .= "                     annual, ";
        $query .= "                     count(*) as count ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT DISTINCT ";
        $query .= "                         schregno, ";
        $query .= "                         year, ";
        $query .= "                         annual ";
        $query .= "                     FROM ";
        $query .= "                         schreg_regd_dat ";
        $query .= "                     WHERE ";
        $query .= "                         year <= '".CTRL_YEAR."' ";
        $query .= "                     AND ";
        $query .= "                         SCHREGNO = '".$schregno."' ";
        $query .= "                     ) ";
        $query .= "                 GROUP BY ";
        $query .= "                     schregno, ";
        $query .= "                     annual ";
        $query .= "                 ) t2 on t1.ANNUAL = t2.ANNUAL ";
        $query .= "     left join H_APP_NAME_MST t3 on t1.GRADE = t3.KENJA_CODE and t3.DICTIONARY_KIND = '01' and t3.DICTIONARY_NO = '03' ";
        $query .= "  ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " AND  ";
        $query .= "     t1.YEAR = '".$year."' ";
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞校長氏名
    function getPrincipal($year, $mode = "")
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
        $query .= "         SCHOOL_KIND = 'H' ";
        
        if($mode == ""){
            $query .= "     ORDER BY ";
            $query .= "         FROM_DATE ";
        }
        
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学校変更状況＞校長氏名(PRINCIPAL_HIST_DATにデータがない場合)
    function getCertif($year)
    {
        $query  = " SELECT ";
        $query .= "     PRINCIPAL_NAME ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     CERTIF_KINDCD = '107'  ";
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
    
    //指導要録情報＞学籍の記録＞生徒＞氏名～国籍まで
    function getSchregData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.NAME, ";
        $query .= "     t1.NAME_KANA, ";
        $query .= "     t1.NAME_SHOW, ";
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
    
    //指導要録情報＞学籍の記録＞生徒＞保護者
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
    
    //指導要録情報＞学籍の記録＞在学状況＞在学学年
    function getZaigakuGrade($schregno)
    {
        $query  = " SELECT DISTINCT  ";
        $query .= "    t1.SCHREGNO,  ";
        $query .= "    t1.YEAR,  ";
        $query .= "    t1.GRADE as GRADECODE,  ";
        $query .= "    t1.HR_CLASS,  ";
        $query .= "    t1.ATTENDNO,  ";
        $query .= "    t1.ANNUAL,  ";
        $query .= "    t3.DICTIONARY_CODE as GRADE,  ";
        $query .= "    t4.HR_CLASS_NAME1,  ";
        $query .= "    t5.NAME1  ";
        $query .= " FROM  ";
        $query .= "    SCHREG_REGD_DAT t1  ";
        $query .= "    left join SCHREG_REGD_GDAT t2 on t1.YEAR = t2.YEAR and t1.GRADE = t2.GRADE and SCHOOL_KIND = 'H'  ";
        $query .= "    left join H_APP_NAME_MST t3 on t2.GRADE_CD = t3.KENJA_CODE and t3.DICTIONARY_KIND = '01' and t3.DICTIONARY_NO = '03'  ";
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
        $query .= " ORDER BY ";
        $query .= "     t1.YEAR ";
        
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞異動状況＞入学～卒業
    function getIdou($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.*, ";
        $query .= "     t3.BASE_REMARK1 ";
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
        $query .= "                     left join H_APP_NAME_MST a2 on a1.GRADE = a2.KENJA_CODE and a2.DICTIONARY_KIND = '01' and a2.DICTIONARY_NO = '03' ";
        $query .= "                 WHERE ";
        $query .= "                     a1.SCHREGNO = '".$schregno."' ";
        $query .= "     ) t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = t2.YEAR ";
        $query .= "     left join SCHREG_BASE_DETAIL_MST t3 on t1.SCHREGNO = t3.SCHREGNO and t3.BASE_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞異動状況＞留学等
    //指導要録情報＞各教科_科目等の修得単位数の記録＞留学(カウントのみ)
    function getAbroad($schregno, $mode="")
    {
        $start = CTRL_YEAR."-04-01";
        $end = CTRL_DATE;
        
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "      SELECT ";
        $query .= "         * ";
        $query .= "      FROM ";
        $query .= "         SCHREG_TRANSFER_DAT ";
        $query .= "      WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "      AND ";
        $query .= "         TRANSFERCD = '1' ";
        $query .= "      AND  ";
        $query .= "         TRANSFER_EDATE <= '".$end."'  ";

        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導要録情報＞学籍の記録＞異動状況＞進学先就職先等
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
    
    //指導要録情報＞学籍の記録＞異動状況＞進学先
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
    
    //指導要録情報＞学籍の記録＞異動状況＞就職先
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
    
    //指導要録情報＞学籍の記録＞課程名・学科名
    function getCourseMajor($schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.COURSECD, ";
        $query .= "     t2.DICTIONARY_CODE as COURSE, ";
        $query .= "     t1.MAJORCD, ";
        $query .= "     t3.DICTIONARY_CODE as MAJOR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     left join H_APP_NAME_MST t2 on t1.COURSECD = t2.KENJA_CODE and t2.DICTIONARY_KIND = '02' and t2.DICTIONARY_NO = '02' ";
        $query .= "     left join H_APP_NAME_MST t3 on t1.MAJORCD = t3.KENJA_CODE and t3.DICTIONARY_KIND = '02' and t3.DICTIONARY_NO = '03' ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " AND  ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    
    //指導要録情報＞各教科･科目等の修得単位数の記録＞各教科に共通する各教科･科目・主として専門学科において開設される各教科･科目
    function getCredit($schregno, $kamoku, $mode = "")      //$kamoku->"":共通科目、1:専門科目
    {

        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "      SELECT  ";
        $query .= "          CLASS,  ";
        $query .= "          SUBCLASS,  ";
        $query .= "          SUBCLASSCD,  ";
        $query .= "          SUM(GET_CREDIT+ADD_CREDIT) as SUM_CREDIT, ";
        $query .= "          CLASSNAME, ";
        $query .= "          SUBCLASSNAME,  ";
        $query .= "          SPECIALDIV  ";
        $query .= "      FROM  ";
        $query .= "          (  ";
        $query .= "          SELECT  ";
        $query .= "              t1.SCHREGNO,  ";
        $query .= "              t1.ANNUAL,  ";
        $query .= "              t1.CLASSCD,  ";
        $query .= "              t4.DICTIONARY_CODE as CLASS,  ";
        $query .= "              t1.CURRICULUM_CD,  ";
        $query .= "              t1.SUBCLASSCD,  ";
        $query .= "              t1.GET_CREDIT,  ";
        $query .= "              case when t1.ADD_CREDIT IS NULL then 0  ";
        $query .= "              else t1.ADD_CREDIT end as ADD_CREDIT,  ";
        $query .= "              t3.DICTIONARY_CODE as SUBCLASS, ";
        $query .= "              t2.CLASSNAME, ";
        $query .= "              t5.SUBCLASSNAME, ";
        $query .= "              t2.SPECIALDIV ";
        $query .= "          FROM  ";
        $query .= "              SCHREG_STUDYREC_DAT t1  ";
        $query .= "              left join CLASS_MST t2 on t1.CLASSCD = t2.CLASSCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND  ";
        $query .= "              left join H_APP_CLASS_MST t4 on t2.CLASSCD = t4.KENJA_CODE  ";
        $query .= "              left join H_APP_SUBCLASS_MST t3 on t1.SUBCLASSCD = t3.KENJA_CODE and t1.CURRICULUM_CD = t3.CURRICULUM_CD  ";
        $query .= "              left join SUBCLASS_MST t5 on t1.SUBCLASSCD = t5.SUBCLASSCD and t1.CURRICULUM_CD = t5.CURRICULUM_CD and t1.SCHOOL_KIND = t5.SCHOOL_KIND  ";
        $query .= "         WHERE ";
        $query .= "             t1.SCHREGNO = '".$schregno."' ";

        if($kamoku != ""){
            $query .= "         AND  ";
            $query .= "             t2.SPECIALDIV = '1' ";
        }else{
            $query .= "         AND  ";
            $query .= "             (t2.SPECIALDIV != '1' OR t2.SPECIALDIV IS NULL) ";
        }

        $query .= "         AND  ";
        $query .= "             t1.CLASSCD < 90 ";
        $query .= "         AND  ";
        $query .= "             t1.YEAR <= '".CTRL_YEAR."'  ";
        $query .= "         ORDER BY  ";
        $query .= "             t1.CLASSCD,  ";
        $query .= "             t1.SUBCLASSCD  ";
        $query .= "         ) ";
        $query .= "      GROUP BY  ";
        $query .= "          CLASS,  ";
        $query .= "          SUBCLASS,  ";
        $query .= "          SUBCLASSCD,  ";
        $query .= "          CLASSNAME, ";
        $query .= "          SUBCLASSNAME, ";
        $query .= "          SPECIALDIV ";
        $query .= "     ORDER BY ";
        $query .= "         SUBCLASS ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導要録情報＞各教科_科目等の修得単位数の記録＞留学(修得単位数のトータルを計算する)
    function getAbroadCredit($schregno)
    {
        $end = CTRL_DATE;

        $query  = " SELECT ";
        $query .= "     SUM(ABROAD_CREDITS) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= "      AND  ";
        $query .= "         TRANSFER_EDATE <= '".$end."'  ";
        
        return $query;
    }
    
    //指導要録情報＞各教科_科目等の修得単位数の記録＞総合的な学習の時間(修得単位数のトータルを計算する)
    function getIntegrated($schregno, $mode="")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     t1.*, ";
            $query .= "     t2.CLASSNAME ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         SUM(GET_CREDIT) as CREDIT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             SCHREG_STUDYREC_DAT ";
        $query .= "         WHERE ";
        $query .= "             CLASSCD = '90'       AND ";
        $query .= "             SUBCLASSCD = '900100'      AND ";
        $query .= "             SCHREGNO = '".$schregno."'      AND ";
        $query .= "             YEAR <= '".CTRL_YEAR."' ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND ";
        $query .= "     )  ";
        if($mode == ""){
            $query .= "     t1  ";
            $query .= "     left join CLASS_MST t2 on t1.CLASSCD = t2.CLASSCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
        }
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録1＞学年
    //指導要録情報＞指導に関する記録2＞学年
    function getShidouGrade($schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t2.DICTIONARY_CODE as GRADE, ";
        $query .= "     t1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         ANNUAL ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."'  ";
        $query .= "     AND ";
        $query .= "         YEAR <= '".CTRL_YEAR."' ";
        $query .= "     ) t1 ";
        $query .= "     left join H_APP_NAME_MST t2 on t1.GRADE = t2.KENJA_CODE and t2.DICTIONARY_KIND = '01' and t2.DICTIONARY_NO = '03' ";
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録1＞原級留置回数
    function getStayCntTotal($schregno, $annual)
    {
        $query .= "     SELECT ";
        $query .= "         schregno, ";
        $query .= "         annual, ";
        $query .= "         count(*) as count ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             schregno, ";
        $query .= "             year, ";
        $query .= "             annual ";
        $query .= "         FROM ";
        $query .= "             schreg_regd_dat ";
        $query .= "         WHERE  ";
        $query .= "             YEAR <= '".CTRL_YEAR."' ";
        $query .= "         ) ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     AND ";
        $query .= "         ANNUAL = '".$annual."' ";
        $query .= "     GROUP BY ";
        $query .= "         schregno, ";
        $query .= "         annual ";
        
        return $query;
    }
    
    //指導要録情報＞指導に関する記録1＞各学科に共通する各教科_科目
    function getCreditTotal($schregno, $annual, $kamoku, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        
        $query .= "      SELECT  ";
        $query .= "          t1.YEAR,  ";
        $query .= "          t1.SCHREGNO,  ";
        $query .= "          t1.ANNUAL,  ";
        $query .= "          t1.CLASSCD,  ";
        $query .= "          t1.SCHOOL_KIND,  ";
        $query .= "          t1.CURRICULUM_CD,  ";
        $query .= "          t1.SUBCLASSCD,  ";
        $query .= "          t1.VALUATION,  ";
        $query .= "          case when t1.ADD_CREDIT IS NOT NULL and t1.GET_CREDIT IS NOT NULL then t1.ADD_CREDIT + t1.GET_CREDIT ";
        $query .= "               when t1.ADD_CREDIT IS NOT NULL and t1.GET_CREDIT IS NULL then t1.ADD_CREDIT  ";
        $query .= "          else t1.GET_CREDIT end as GET_CREDIT, ";
        $query .= "          t2.CLASSNAME,  ";
        $query .= "          t4.DICTIONARY_CODE as CLASS,  ";
        $query .= "          t2.SPECIALDIV,  ";
        $query .= "          t3.DICTIONARY_CODE as SUBCLASS,  ";
        $query .= "          t5.SUBCLASSNAME, ";
        $query .= "          t6.TOTAL, ";
        $query .= "          t1.COMP_CREDIT, ";
        $query .= "          t7.REMARK  ";
        $query .= "      FROM  ";
        $query .= "          ( ";
        $query .= "           SELECT ";
        $query .= "               YEAR, ";
        $query .= "               SCHREGNO, ";
        $query .= "               ANNUAL, ";
        $query .= "               CLASSCD, ";
        $query .= "               SCHOOL_KIND, ";
        $query .= "               CURRICULUM_CD, ";
        $query .= "               SUBCLASSCD, ";
        $query .= "               MAX(VALUATION) as VALUATION, ";
        $query .= "               SUM(GET_CREDIT) as GET_CREDIT, ";
        $query .= "               SUM(ADD_CREDIT) as ADD_CREDIT, ";
        $query .= "               SUM(COMP_CREDIT) as COMP_CREDIT ";
        $query .= "           FROM ";
        $query .= "               SCHREG_STUDYREC_DAT ";
        $query .= "           WHERE ";
        $query .= "               SCHREGNO = '".$schregno."' ";
        $query .= "           AND  ";
        $query .= "               ANNUAL = '".$annual."' ";
        $query .= "           GROUP BY ";
        $query .= "               YEAR, ";
        $query .= "               SCHREGNO, ";
        $query .= "               ANNUAL, ";
        $query .= "               CLASSCD, ";
        $query .= "               SCHOOL_KIND, ";
        $query .= "               CURRICULUM_CD, ";
        $query .= "               SUBCLASSCD ";
        $query .= "           ORDER BY ";
        $query .= "               CLASSCD, ";
        $query .= "               SUBCLASSCD ";
        $query .= "          ) t1  ";
        $query .= "          left join CLASS_MST t2 on t1.CLASSCD = t2.CLASSCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND  ";
        $query .= "          left join H_APP_CLASS_MST t4 on t2.CLASSCD = t4.KENJA_CODE  ";
        $query .= "          left join H_APP_SUBCLASS_MST t3 on t1.SUBCLASSCD = t3.KENJA_CODE and t1.CURRICULUM_CD = t3.CURRICULUM_CD  ";
        $query .= "          left join SUBCLASS_MST t5 on t1.SUBCLASSCD = t5.SUBCLASSCD and t1.CURRICULUM_CD = t5.CURRICULUM_CD and t1.SCHOOL_KIND = t5.SCHOOL_KIND   ";
        $query .= "          left join  ";
        $query .= "          ( ";
        $query .= "           SELECT ";
        $query .= "               CLASSCD, ";
        $query .= "               SCHOOL_KIND, ";
        $query .= "               CURRICULUM_CD, ";
        $query .= "               SUBCLASSCD, ";
        $query .= "               SUM(GET_CREDIT) as TOTAL ";
        $query .= "           FROM ";
        $query .= "               ( ";
        $query .= "                SELECT  ";
        $query .= "                    t1.YEAR,  ";
        $query .= "                    t1.SCHREGNO,  ";
        $query .= "                    t1.ANNUAL,  ";
        $query .= "                    t1.CLASSCD,  ";
        $query .= "                    t1.SCHOOL_KIND,  ";
        $query .= "                    t1.CURRICULUM_CD,  ";
        $query .= "                    t1.SUBCLASSCD,  ";
        $query .= "                    case when t1.ADD_CREDIT IS NOT NULL and t1.GET_CREDIT IS NOT NULL then t1.ADD_CREDIT + t1.GET_CREDIT ";
        $query .= "                         when t1.ADD_CREDIT IS NOT NULL and t1.GET_CREDIT IS NULL then t1.ADD_CREDIT  ";
        $query .= "                    else t1.GET_CREDIT end as GET_CREDIT ";
        $query .= "                FROM  ";
        $query .= "                    SCHREG_STUDYREC_DAT t1  ";
        $query .= "                    left join CLASS_MST t2 on t1.CLASSCD = t2.CLASSCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND  ";
        $query .= "                WHERE ";
        $query .= "                    t1.SCHREGNO = '".$schregno."' ";

        if($kamoku != ""){
            $query .= "               AND ";
            $query .= "                   t2.SPECIALDIV = '1' ";
        }else{
            $query .= "               AND ";
            $query .= "                   (t2.SPECIALDIV != '1' or t2.SPECIALDIV IS NULL) ";
        }
        $query .= "                AND   ";
        $query .= "                    t1.CLASSCD <= 90  ";
        $query .= "                AND  ";
        $query .= "                    t1.YEAR <= '".CTRL_YEAR."'  ";
        $query .= "                ORDER BY  ";
        $query .= "                   t1.CLASSCD,t1.SUBCLASSCD  ";
        $query .= "               ) ";
        $query .= "           GROUP BY ";
        $query .= "               CLASSCD, ";
        $query .= "               SCHOOL_KIND, ";
        $query .= "               CURRICULUM_CD, ";
        $query .= "               SUBCLASSCD ";
        $query .= "           ) t6 on t1.CLASSCD = t6.CLASSCD and t1.SUBCLASSCD = t6.SUBCLASSCD and t1.SCHOOL_KIND = t6.SCHOOL_KIND and t1.CURRICULUM_CD = t6.CURRICULUM_CD ";
        $query .= "           left join STUDYRECREMARK_DAT t7 on t1.YEAR = t7.YEAR and t1.SCHREGNO = t7.SCHREGNO and t1.CLASSCD = t7.CLASSCD and t1.SCHOOL_KIND = t7.SCHOOL_KIND and t1.CURRICULUM_CD = t7.CURRICULUM_CD and t1.SUBCLASSCD = t7.SUBCLASSCD ";
        $query .= "      WHERE ";
        $query .= "          t1.SCHREGNO = '".$schregno."' ";
        $query .= "      AND  ";
        $query .= "          t1.ANNUAL = '".$annual."' ";

        if($kamoku != ""){
            $query .= "     AND ";
            $query .= "         t2.SPECIALDIV = '1' ";
        }else{
            $query .= "     AND ";
            $query .= "         (t2.SPECIALDIV != '1' or t2.SPECIALDIV IS NULL) ";
        }
        $query .= "      AND   ";
        $query .= "         t1.CLASSCD <= 90  ";
        $query .= "      AND  ";
        $query .= "         t1.YEAR <= '".CTRL_YEAR."'  ";
        $query .= "      ORDER BY  ";
        $query .= "         t1.CLASSCD,t1.SUBCLASSCD  ";

        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }

    //指導要録情報＞指導に関する記録1＞留学
    function getAbroadCnt($schregno, $mode="0")
    {
        $query  = " SELECT ";
        if($mode != "0"){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    function getAnnalYear($schregno, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }

    //指導に関する記録1＞総合的な学習の時間の記録
    function getIntegratedCredit($schregno, $annual, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     t1.SUM_CREDIT, ";
            $query .= "     t3.TOTALSTUDYACT, ";
            $query .= "     t3.TOTALSTUDYVAL ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(GET_CREDIT) as SUM_CREDIT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT ";
        $query .= "     WHERE ";
        $query .= "         CLASSCD >= 90 AND ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     AND  ";
        $query .= "         YEAR <= '".CTRL_YEAR."'  ";
        $query .= "     AND  ";
        $query .= "         ANNUAL = '".$annual."'  ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= "     ) t1  ";
        $query .= "     left join HTRAINREMARK_DAT t3 on t1.SCHREGNO = t3.SCHREGNO and t1.YEAR = t3.YEAR ";
        
        return $query;
    }
    
    //指導に関する記録1＞特別活動の記録
    function getSpecialAct($schregno, $annual, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         replace(SPECIALACTREMARK, CHR(13)||CHR(10), ' ') as SPECIALACTREMARK ";
        $query .= "     FROM ";
        $query .= "         HTRAINREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' AND ";
        $query .= "         YEAR <= '".CTRL_YEAR."' AND ";
        $query .= "         ANNUAL = '".$annual."' ";
        
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導に関する記録1＞自立活動の記録
    function getZirituAct($schregno, $mode="")
    {
        
        //return $query;
    }
    
    //指導に関する記録1＞総合所見及び指導上参考となる諸事項
    function getTotalSyoken($schregno, $annual, $mode="")
    {
        if($mode != ""){
            $query .= " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        
        $query .= "      SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         replace(TOTALREMARK, CHR(13)||CHR(10), '\n') as TOTALREMARK ";
        $query .= "      FROM ";
        $query .= "         HTRAINREMARK_DAT ";
        $query .= "      WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "      AND  ";
        $query .= "         YEAR <= '".CTRL_YEAR."' ";
        $query .= "      AND  ";
        $query .= "         ANNUAL = '".$annual."' ";
        
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導に関する記録1＞出欠の記録
    //指導に関する記録2＞出欠の記録
    function getAttendRec($schregno, $annual)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     CLASSDAYS, ";
        $query .= "     SICK, ";
        $query .= "     SUSPEND, ";
        $query .= "     MOURNING, ";
        $query .= "     ABROAD, ";
        $query .= "     ACCIDENTNOTICE, ";
        $query .= "     NOACCIDENTNOTICE, ";
        $query .= "     PRESENT ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     ANNUAL = '".$annual."' AND ";
        $query .= "     YEAR = (SELECT ";
        $query .= "                 MAX(YEAR) ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_ATTENDREC_DAT ";
        $query .= "             WHERE ";
        $query .= "                 ANNUAL = '".$annual."' AND ";
        $query .= "                 SCHREGNO = '".$schregno."' ";
        $query .= "             ) ";
        
        return $query;
    }
    function getAttendRemark($schregno, $annual, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         HTRAINREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' AND ";
        $query .= "         ANNUAL = '".$annual."' ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //指導に関する記録2＞原級留置回数
    function getStayCntShidou2($schregno, $annual)
    {
        $query  = " SELECT ";
        $query .= "     COUNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         schregno, ";
        $query .= "         annual, ";
        $query .= "         count(*) as count ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             schregno, ";
        $query .= "             year, ";
        $query .= "             annual ";
        $query .= "         FROM ";
        $query .= "             schreg_regd_dat ";
        $query .= "         ) ";
        $query .= "     WHERE ";
        $query .= "         YEAR <= '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         schregno, ";
        $query .= "         annual ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     count > 1 AND ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     ANNUAL = '".$annual."' ";

        return $query;
    }
    
    //指導に関する記録2＞各教科_特別活動_自立活動の記録
    function getShidou2Record($schregno, $annual, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        
        $query .= "      SELECT ";
        $query .= "         t1.SCHREGNO, ";
        $query .= "         t1.SUBCLASSCD, ";
        $query .= "         t1.REMARK1, ";
        $query .= "         t2.SUM ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DETAIL_DAT t1 ";
        $query .= "         left join (SELECT ";
        $query .= "                         SCHREGNO, ";
        $query .= "                         YEAR, ";
        $query .= "                         SUM(CNT_DECIMAL) as SUM ";
        $query .= "                     FROM ";
        $query .= "                         (SELECT ";
        $query .= "                             SCHREGNO, ";
        $query .= "                             YEAR, ";
        $query .= "                             SEMESTER, ";
        $query .= "                             case when CNT_DECIMAL is null then 0.0 else CNT_DECIMAL end as CNT_DECIMAL ";
        $query .= "                         FROM ";
        $query .= "                             ATTEND_SEMES_DETAIL_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SEQ = '101' ";
        $query .= "                         )  ";
        $query .= "                     WHERE ";
        $query .= "                         SCHREGNO = '".$schregno."' ";
        $query .= "                     AND ";
        $query .= "                         YEAR <= '".CTRL_YEAR."' ";
        $query .= "                     GROUP BY ";
        $query .= "                         SCHREGNO, ";
        $query .= "                         YEAR ";
        $query .= "                     ) t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = t2.YEAR ";
        $query .= "     WHERE ";
        $query .= "         t1.YEAR in (SELECT DISTINCT ";
        $query .= "                         YEAR ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         SCHREGNO = '".$schregno."' ";
        $query .= "                     AND ";
        $query .= "                         ANNUAL = '".$annual."' ";
        $query .= "                     AND ";
        $query .= "                         YEAR <= '".CTRL_YEAR."' ";
        $query .= "                     ) ";
        $query .= "     AND ";
        $query .= "         t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "         t1.SEQ = '001' AND ";
        $query .= "         t1.SCHOOL_KIND = 'H' ";
        
        if($mode != ""){
            $query .= "     ) ";
        }

        return $query;
    }
    
    //指導に関する記録2＞総合的な学習の時間の記録
    function getShidou2Integrated($schregno, $annual, $mode = "")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         TOTALSTUDYACT, ";
        $query .= "         TOTALSTUDYVAL, ";
        $query .= "         TOTALREMARK ";
        $query .= "     FROM ";
        $query .= "         HTRAINREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' AND ";
        $query .= "         ANNUAL = '".$annual."' ";
        $query .= "     AND ";
        $query .= "         YEAR <= '".CTRL_YEAR."' ";
        
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
}
?>
