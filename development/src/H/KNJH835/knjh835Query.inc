<?php
class knjh835Query extends Query {
    
    //年度
    function getYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR as VALUE, ";
        $query .= "     YEAR as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT ";

        return $query;
    }
    //学期
    function getSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER as VALUE, ";
        $query .= "     SEMESTERNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER != '9' AND ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }

    //科目データの取得
    function getSubclassMst($model) {
        $query  = "";
        $query .= " WITH REPLACE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1' AS DIV, ";
        $query .= "     COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$model->topfield["EXP_YEAR"]."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.SUBCLASSCD AS VALUE, ";
        $query .= "     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= "     LEFT JOIN REPLACE L1 ON L1.DIV = '1' ";
        $query .= "         AND T3.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     (T1.YEAR, T1.SEMESTER, T1.SUBCLASSCD, T1.CURRICULUM_CD) in (SELECT ";
        $query .= "                                                                     YEAR, ";
        $query .= "                                                                     SEMESTER, ";
        $query .= "                                                                     SUBCLASSCD, ";
        $query .= "                                                                     MAX(CURRICULUM_CD) ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     CHAIR_DAT ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     YEAR = '".$model->topfield["EXP_YEAR"]."' AND ";
        $query .= "                                                                     SEMESTER = '".$model->topfield["SEMESTER"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "                                                                 AND SCHOOL_KIND       = '".SCHOOLKIND."' ";
        }
        $query .= "                                                                 GROUP BY ";
        $query .= "                                                                     YEAR, ";
        $query .= "                                                                     SEMESTER, ";
        $query .= "                                                                     SUBCLASSCD ";
        $query .= "                                                                 ) ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T4.YEAR       = T1.YEAR ";
        $query .= "     AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";

        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "     AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "     AND T1.SCHOOL_KIND       = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || '：' || T1.chairname AS LABEL ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".$model->topfield["EXP_YEAR"]."' AND ";
        $query .= "     T1.semester   = '".$model->topfield["SEMESTER"]."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        $query .= "     T1.subclasscd = '".$model->topfield["SUBCLASSCD"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "     AND T1.SCHOOL_KIND       = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //サイト名取得
    function getSite($mnid = "")
    {
        $query .= " SELECT ";
        if($mnid == ""){
            $query .= "     NAME1, ";
        }
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H800' ";
        if($mnid != ""){
            $query .= " AND  ";
            $query .= "     NAMECD2 = '".$mnid."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        
        return $query;
    }
    
    //教科取得
    function getKyouka($year, $site, $kyouka="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CLASSCD as VALUE, ";
        $query .= "     CLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '".$year."' ";
        if($kyouka != ""){
            $query .= " AND  ";
            $query .= "     CLASSCD = '".$kyouka."' ";
        }
        $query .= " AND ";
        $query .= "     SITE_ID = '".$site."' ";
        $query .= " ORDER BY ";
        $query .= "     CLASSCD ";
        
        return $query;
    }
    //科目取得
    function getKamoku($year, $site, $kyouka, $kamoku="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SUBCLASSCD as VALUE, ";
        $query .= "     SUBCLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     CLASSCD = '".$kyouka."' ";
        if($kamoku != ""){
            $query .= " AND ";
            $query .= "     SUBCLASSCD = '".$kamoku."' ";
        }
        $query .= " AND ";
        $query .= "     SITE_ID = '".$site."' ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD ";
        
        return $query;
    }
    
    //対象データ取得
    function getKnjid($topfield, $field, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            if($mode == "1"){
                $query .= "     DISTINCT TEACHING_CD, TEACHING_NAME ";
            }else{
                $query .= "     COUNT(*) ";
            }
            $query .= " FROM ";
            $query .= "     PV_MBT_SCORE_COUNT_DAT ";
            $query .= " WHERE ";
            $query .= "     STUDENT_ID IN ( ";
            $query .= "                         SELECT ";
            $query .= "                             LOWER(LOGINID) ";
            $query .= "                         FROM ";
            $query .= "                     ( ";
        }
        
        $query .= "                     SELECT ";
        $query .= "                         t1.LOGINID, ";
        $query .= "                         t1.SCHREGNO, ";
        $query .= "                         t2.NAME, ";
        $query .= "                         t3.GRADE, ";
        $query .= "                         t3.HR_CLASS, ";
        $query .= "                         t3.ATTENDNO ";
        $query .= "                     FROM ";
        $query .= "                         PV_SCHREG_MST t1  ";
        $query .= "                         left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO  ";
        $query .= "                         left join SCHREG_REGD_DAT t3 on t1.SCHREGNO = t3.SCHREGNO and t3.YEAR = '".$topfield["EXP_YEAR"]."' and t3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                     WHERE ";
        $query .= "                         t1.SCHREGNO IN (SELECT ";
        $query .= "                                             SCHREGNO ";
        $query .= "                                         FROM ";
        $query .= "                                             CHAIR_STD_DAT ";
        $query .= "                                         WHERE ";
        $query .= "                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                             SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                         ) ";
        if($mode != ""){
            $query .= "                     ) ";
            $query .= "                 ) ";
            $query .= "  AND ";
            $query .= "     SITE_ID = '".$field["SITE"]."' ";
            $query .= "  AND ";
            $query .= "     YEAR = '".$topfield["EXP_YEAR"]."' ";
            if($field["KYOKA"] != ""){
                $query .= "  AND ";
                $query .= "     CLASSCD = '".$field["KYOKA"]."' ";
            }
            if($field["KAMOKU"] != ""){
                $query .= "  AND ";
                $query .= "     SUBCLASSCD = '".$field["KAMOKU"]."' ";
            }
            if($field["DATE"] != "" && $field["T_HOUR"] != ""){
                $query .= " AND ";
                $query .= "     DATE(START_DATE) = DATE('".$field["DATE"]."') ";
                $query .= " AND ";
                $query .= "    TIME(START_DATE) >= TIME('".sprintf("%02d", $field["F_HOUR"]).":".sprintf("%02d", $field["F_MIN"]).":00')";
                $query .= " AND ";
                $query .= "     DATE(END_DATE) = DATE('".$field["DATE"]."') ";
                $query .= " AND ";
                $query .= "     TIME(END_DATE) <= TIME('".sprintf("%02d", $field["T_HOUR"]).":".sprintf("%02d", $field["T_MIN"]).":00') ";
            }else if($field["DATE"] != ""){
                $query .= " AND ";
                $query .= "     DATE(START_DATE) >= DATE('".$field["DATE"]."') ";
                if($field["F_HOUR"] != ""){
                    $query .= " AND ";
                    $query .= "    TIME(START_DATE) >= TIME('".sprintf("%02d", $field["F_HOUR"]).":".sprintf("%02d", $field["F_MIN"]).":00')";
                }
            }
        }
        
        return $query;
    }

    //対象クラス全員の情報取得
    function getAllKnjid($topfield)
    {
        $query  = " SELECT ";
        $query .= "     LOWER(t1.LOGINID) as KNJID, ";
        $query .= "     t2.SCHREGNO, ";
        $query .= "     t2.NAME, ";
        $query .= "     t6.GRADE_NAME as GRADE, ";
        $query .= "     t6.HR_CLASS_NAME as HR_CLASS, ";
        $query .= "     t3.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST t2 ";
        $query .= "     left join PV_SCHREG_MST t1 on t2.SCHREGNO = t1.SCHREGNO  ";
        $query .= "     left join SCHREG_REGD_DAT t3 on t2.SCHREGNO = t3.SCHREGNO and t3.YEAR = '".$topfield["EXP_YEAR"]."' and t3.SEMESTER = '".$topfield["SEMESTER"]."' ";
        $query .= "     left join SCHREG_REGD_GDAT t4 on t3.YEAR = t4.YEAR AND t3.GRADE = t4.GRADE ";
        $query .= "     left join (SELECT ";
        $query .= "                     YEAR, ";
        $query .= "                     GRADE, ";
        $query .= "                     SEMESTER, ";
        $query .= "                     HR_CLASS, ";
        $query .= "                     to_single_byte(GRADE_NAME) as GRADE_NAME, ";
        $query .= "                     to_single_byte(HR_CLASS_NAME1) as HR_CLASS_NAME ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_HDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                 ) t6 on t3.YEAR = t6.YEAR and t3.GRADE = t6.GRADE and t3.HR_CLASS = t6.HR_CLASS and t3.SEMESTER = t6.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     t2.SCHREGNO IN (SELECT ";
        $query .= "                         SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         CHAIR_STD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                         SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                         CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     t4.GRADE_CD, t3.HR_CLASS, t3.ATTENDNO ";

        return $query;
    }
    
    
    //対象生徒の成績取得
    function getScore($topfield, $field, $studentid, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     replace(SUBSTR(START_DATE,1,10), '-', '/') as TAKEDATE, ";
        $query .= "     ALL_CNT, ";
        $query .= "     RIGHT_CNT     ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "     WHERE ";
        $query .= "         (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                     STUDENT_ID, ";
        $query .= "                                                     MAX(TAKE_CNT) as TAKE_CNT , ";
        $query .= "                                                     TEACHING_CD ";
        $query .= "                                                 FROM ";
        $query .= "                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                 WHERE ";
        $query .= "                                                     STUDENT_ID = '".$studentid."' ";
        $query .= "                                                 AND ";
        $query .= "                                                     SITE_ID = '".$field["SITE"]."' ";
        if($field["DATE"] != "" && $field["T_HOUR"] != ""){
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(START_DATE) = DATE('".$field["DATE"]."') ";
            $query .= "                                                 AND ";
            $query .= "                                                     TIME(START_DATE) >= TIME('".sprintf("%02d", $field["F_HOUR"]).":".sprintf("%02d", $field["F_MIN"]).":00')";
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(END_DATE) = DATE('".$field["DATE"]."') ";
            $query .= "                                                 AND ";
            $query .= "                                                     TIME(END_DATE) <= TIME('".sprintf("%02d", $field["T_HOUR"]).":".sprintf("%02d", $field["T_MIN"]).":00') ";
        }else if($field["DATE"] != ""){
            $query .= "                                                 AND ";
            $query .= "                                                     DATE(START_DATE) >= DATE('".$field["DATE"]."') ";
            if($field["F_HOUR"] != ""){
                $query .= "                                                 AND ";
                $query .= "                                                     TIME(START_DATE) >= TIME('".sprintf("%02d", $field["F_HOUR"]).":".sprintf("%02d", $field["F_MIN"]).":00')";
            }
        }
        $query .= "                                         AND ";
        $query .= "                                             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                         GROUP BY ";
        $query .= "                                             STUDENT_ID, ";
        $query .= "                                             TEACHING_CD ";
        $query .= "                                         ) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     CLASSCD = '".$field["KYOKA"]."'  ";
        $query .= " AND ";
        $query .= "     SITE_ID = '".$field["SITE"]."' ";
        if($field["KAMOKU"] != ""){
            $query .= " AND ";
            $query .= "     SUBCLASSCD = '".$field["KAMOKU"]."'  ";
        }
        $query .= " AND  ";
        $query .= "     TEACHING_CD in (".$teachingcd.")  ";
        $query .= " ORDER BY ";
        $query .= "     TEACHING_CD ";
        
        return $query;
    }

}
?>
