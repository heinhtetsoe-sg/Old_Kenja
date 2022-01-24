<?php
class knjh805Query extends Query {
    
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
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD  ";
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
    
    //教科取得
    function getKyouka($kyouka="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CLASSCD as VALUE, ";
        $query .= "     CLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SUBCLASS_MST ";
        if($kyouka != ""){
            $query .= " WHERE ";
            $query .= "     CLASSCD = '".$kyouka."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLASSCD ";
        
        return $query;
    }
    //科目取得
    function getKamoku($year, $kyouka, $kamoku="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t1.SUBCLASSCD as VALUE, ";
        $query .= "     t1.SUBCLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SUBCLASS_MST t1 ";
        $query .= "     left join PV_CBT_SCORE_COUNT_DAT t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     t1.CLASSCD = '".$kyouka."' ";
        if($kamoku != ""){
            $query .= " AND ";
            $query .= "     t2.SUBCLASSCD = '".$kamoku."' ";
        }else{
            $query .= " AND ";
            $query .= "     t2.SUBCLASSCD IS NOT NULL ";
        }
        $query .= " AND ";
        $query .= "     t2.YEAR = '".$year."' ";
        
        return $query;
    }
    //テストレベル
    function getTestLevel($year, $kyouka="", $kamoku="", $testkind="")
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     t3.FIELD1 as VALUE, ";
        $query .= "     t3.FIELD2 as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_CBT_SCORE_COUNT_DAT t2 ";
        $query .= "     left join PV_CBT_SUBCLASS_MST t1 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= "     left join PV_CBT_GENERAL_MST t3 on t2.TEST_LEVEL_CD = t3.FIELD1 and t3.ID = 'M002' ";
        
        $where = "WHERE";
        if($kyouka != ""){
            $query .= " ".$where."  ";
            $query .= "     t1.CLASSCD = '".$kyouka."' ";
            $where = "AND";
        }
        if($kamoku != ""){
            $query .= " ".$where." ";
            $query .= "     t2.SUBCLASSCD = '".$kamoku."' ";
            $where = "AND";
        }
        if($testkind != ""){
            $query .= " ".$where." ";
            $query .= "     t3.FIELD1 = '".$testkind."' ";
        }
        $query .= " AND ";
        $query .= "     t2.YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     t3.FIELD1 ";
        
        return $query;
    }
    
    //対象データ取得
    function getKnjid($topfield, $field, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            if($mode == "1"){
                $query .= " DISTINCT ";
                $query .= "     a1.TEST_ID, ";
                $query .= "     a1.TAKE_TURN, ";
                $query .= "     a3.TEST_KIND_NAME ";
            }else{
                $query .= "     COUNT(*) ";
            }
            $query .= " FROM ";
            $query .= "     PV_CBT_SCORE_COUNT_DAT a1  ";
            $query .= "     left join PV_CBT_SUBCLASS_MST a2 on a1.SUBCLASSCD = a2.SUBCLASSCD ";
            $query .= "     left join PV_CBT_TESTKIND_MST a3 on a1.TEST_ID = a3.TEST_ID ";
            $query .= " WHERE ";
            $query .= "     a1.KNJID IN ( ";
            $query .= "                 SELECT ";
            $query .= "                     KNJID ";
            $query .= "                 FROM ";
            $query .= "                     ( ";
        }
        
        $query .= "                     SELECT ";
        $query .= "                         t1.KNJID, ";
        $query .= "                         t1.SCHREGNO, ";
        $query .= "                         t2.NAME, ";
        $query .= "                         t3.GRADE, ";
        $query .= "                         t3.HR_CLASS, ";
        $query .= "                         t3.ATTENDNO ";
        $query .= "                     FROM ";
        $query .= "                         PV_SCHREG_MST t1  ";
        $query .= "                         left join (SELECT ";
        $query .= "                                         b1.* ";
        $query .= "                                     FROM ";
        $query .= "                                         SCHREG_REGD_DAT b1  ";
        $query .= "                                         left join SCHREG_REGD_GDAT b2 on b1.GRADE = b2.GRADE and b1.YEAR = b2.YEAR ";
        $query .= "                                     WHERE ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "                                         b2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                                     AND ";
        }
        $query .= "                                         b1.YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                     AND ";
        $query .= "                                         b1.SEMESTER = '".$topfield["SEMESTER"]."' ";
        $query .= "                                     ) t3 on t1.SCHREGNO = t3.SCHREGNO ";
        $query .= "                         left join SCHREG_BASE_MST t2 on t3.SCHREGNO = t2.SCHREGNO  ";
        $query .= "                     WHERE ";
        $query .= "                         t1.SCHREGNO IN (SELECT ";
        $query .= "                                             SCHREGNO ";
        $query .= "                                         FROM ";
        $query .= "                                             CHAIR_STD_DAT ";
        $query .= "                                         WHERE ";
        $query .= "                                             YEAR = '".$topfield["EXP_YEAR"]."' AND ";
        $query .= "                                             SEMESTER = '".$topfield["SEMESTER"]."' AND ";
        $query .= "                                             CHAIRCD = '".$topfield["CHAIRCD"]."' ";
        $query .= "                                         ) ";
        if($mode != ""){
            $query .= "                     ) ";
            $query .= "                 ) ";
            $query .= " AND ";
            $query .= "     a1.YEAR = '".$topfield["EXP_YEAR"]."' ";
            if($field["KYOKA"] != ""){
                $query .= "  AND ";
                $query .= "     a2.CLASSCD = '".$field["KYOKA"]."' ";
            }
            if($field["KAMOKU"] != ""){
                $query .= "  AND ";
                $query .= "     a1.SUBCLASSCD = '".$field["KAMOKU"]."' ";
            }
            if($field["TEST_KIND"] != ""){
                $query .= "  AND ";
                $query .= "     a1.TEST_LEVEL_CD = '".$field["TEST_KIND"]."' ";
            }
            if($field["DATE"] != "" && $field["T_HOUR"] != ""){
                $query .= " AND ";
                $query .= "     DATE(a1.START_DATE) = DATE('".$field["DATE"]."') ";
                $query .= " AND ";
                $query .= "    TIME(a1.START_DATE) >= TIME('".sprintf("%02d", $field["F_HOUR"]).":".sprintf("%02d", $field["F_MIN"]).":00')";
                $query .= " AND ";
                $query .= "     DATE(a1.END_DATE) = DATE('".$field["DATE"]."') ";
                $query .= " AND ";
                $query .= "     TIME(a1.END_DATE) <= TIME('".sprintf("%02d", $field["T_HOUR"]).":".sprintf("%02d", $field["T_MIN"]).":00') ";
            }else if($field["DATE"] != ""){
                $query .= " AND ";
                $query .= "     DATE(a1.START_DATE) >= DATE('".$field["DATE"]."') ";
                if($field["F_HOUR"] != ""){
                    $query .= " AND ";
                    $query .= "    TIME(a1.START_DATE) >= TIME('".sprintf("%02d", $field["F_HOUR"]).":".sprintf("%02d", $field["F_MIN"]).":00')";
                }
            }
            if($mode == "1"){
                $query .= " ORDER BY ";
                $query .= "     a1.TEST_ID  ";
            }
        }
        
        return $query;
    }
    
    //対象クラス全員の情報取得
    function getAllKnjid($topfield)
    {
        $query  = " SELECT ";
        $query .= "     t1.KNJID, ";
        $query .= "     t2.SCHREGNO, ";
        $query .= "     t2.NAME, ";
        $query .= "     t6.GRADE_NAME as GRADE, ";
        $query .= "     t6.HR_CLASS_NAME as HR_CLASS, ";
        $query .= "     t3.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST t2 ";
        $query .= "     left join PV_SCHREG_MST t1 on t2.SCHREGNO = t1.SCHREGNO  ";
        $query .= "     left join SCHREG_REGD_DAT t3 on t2.SCHREGNO = t3.SCHREGNO and t3.YEAR = '".$topfield["EXP_YEAR"]."' and t3.SEMESTER = '".$topfield["SEMESTER"]."' ";
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
        $query .= "     t3.GRADE, t3.HR_CLASS, t3.ATTENDNO ";
        
        return $query;
    }
    
    //対象生徒の成績取得
    function getScore($topfield, $field, $knjid, $taketurn)
    {
        $query  = " SELECT ";
        $query .= "     TEST_ID, ";
        $query .= "     TAKE_TURN, ";
        $query .= "     replace(SUBSTR(START_DATE,1,10), '-', '/') as TAKEDATE, ";
        $query .= "     POINT, ";
        $query .= "     GET_POINT     ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         PV_CBT_SCORE_COUNT_DAT ";
        $query .= "     WHERE ";
        $query .= "         (KNJID, END_DATE, TEST_ID) IN (SELECT ";
        $query .= "                                             KNJID, ";
        $query .= "                                             MAX(END_DATE) as END_DATE , ";
        $query .= "                                             TEST_ID ";
        $query .= "                                         FROM ";
        $query .= "                                             PV_CBT_SCORE_COUNT_DAT ";
        $query .= "                                         WHERE ";
        $query .= "                                             KNJID = '".$knjid."' ";
        if($field["DATE"] != "" && $field["T_HOUR"] != ""){
            $query .= "                                         AND ";
            $query .= "                                             DATE(START_DATE) = DATE('".$field["DATE"]."') ";
            $query .= "                                         AND ";
            $query .= "                                             TIME(START_DATE) >= TIME('".sprintf("%02d", $field["F_HOUR"]).":".sprintf("%02d", $field["F_MIN"]).":00')";
            $query .= "                                         AND ";
            $query .= "                                             DATE(END_DATE) = DATE('".$field["DATE"]."') ";
            $query .= "                                         AND ";
            $query .= "                                             TIME(END_DATE) <= TIME('".sprintf("%02d", $field["T_HOUR"]).":".sprintf("%02d", $field["T_MIN"]).":00') ";
        }else if($field["DATE"] != ""){
            $query .= "                                         AND ";
            $query .= "                                             DATE(START_DATE) >= DATE('".$field["DATE"]."') ";
            if($field["F_HOUR"] != ""){
                $query .= "                                         AND ";
                $query .= "                                             TIME(START_DATE) >= TIME('".sprintf("%02d", $field["F_HOUR"]).":".sprintf("%02d", $field["F_MIN"]).":00')";
            }
        }
        $query .= "                                         AND ";
        $query .= "                                             YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "                                         GROUP BY ";
        $query .= "                                             KNJID, ";
        $query .= "                                             TEST_ID ";
        $query .= "                                         ) ";
        $query .= "     AND ";
        $query .= "         YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= "     ) t1";
        $query .= "     left join PV_CBT_SUBCLASS_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     t2.CLASSCD = '".$field["KYOKA"]."'  ";
        if($field["KAMOKU"] != ""){
            $query .= " AND ";
            $query .= "     t1.SUBCLASSCD = '".$field["KAMOKU"]."'  ";
        }
        if($field["TEST_KIND"] != ""){
            $query .= " AND ";
            $query .= "     t1.TEST_LEVEL_CD = '".$field["TEST_KIND"]."'  ";
        }
        $query .= " AND  ";
        $query .= "     t1.TEST_ID in (".$taketurn.")  ";
        $query .= " AND ";
        $query .= "     t1.YEAR = '".$topfield["EXP_YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     TEST_ID ";
        

        return $query;
    }

}
?>
