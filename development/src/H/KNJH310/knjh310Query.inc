<?php

require_once('for_php7.php');

class knjh310Query extends Query {

    //欠課数換算用データ取得
    function getSchoolData($model)
    {
        $query  = "SELECT ";
        $query .= "    ABSENT_COV, ";
        $query .= "    VALUE(ABSENT_COV_LATE,0) AS ABSENT_COV_LATE ";
        $query .= "FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";

        return $query;
    }

    //去年度取得
    function getLastYear($model)
    {
        $query  = "WITH YEART AS ( ";
        $query .= "SELECT ";
        $query .= "    MAX(YEAR) AS YEART ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR < '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    MAX(YEAR) AS YEAR, ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = (SELECT YEART FROM YEART) ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //次年度取得
    function getNextYear($model)
    {
        $query  = "WITH YEART AS ( ";
        $query .= "SELECT ";
        $query .= "    MIN(YEAR) AS YEART ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR > '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    MIN(YEAR) AS YEAR, ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = (SELECT YEART FROM YEART) ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //生徒情報取得
    function getName($model)
    {
        $query  = "SELECT ";
        $query .= "    L1.GRADE, ";
        $query .= "    L2.HR_NAME, ";
        $query .= "    L1.ATTENDNO, ";
        $query .= "    T1.NAME, ";
        $query .= "    L1.HR_CLASS, ";
        $query .= "    L1.COURSECODE ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.SEMESTER = '".$model->semester."' ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = L1.YEAR ";
        $query .= "         AND L2.SEMESTER = L1.SEMESTER ";
        $query .= "         AND L2.GRADE = L1.GRADE ";
        $query .= "         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }
    
    //RECORD_SCORE_DATのデータ検索
    function getRecScore($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     SEMESTER != '9' AND ";
        $query .= "     TESTKINDCD != '99' AND ";
        $query .= "     TESTITEMCD != '00' AND ";
        $query .= "     SCORE_DIV = '08' ";
        
        return $query;
    }

    //科目取得
    function getTestSubclass($model)
    {
        $query  = "WITH CHAIR_STD_T AS ( ";
        $query .= "SELECT DISTINCT ";
        $query .= "    CHAIRCD ";
        $query .= "FROM ";
        $query .= "    CHAIR_STD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= "), TESTITEM_T AS ( ";
        $query .= "SELECT DISTINCT ";
        $query .= "    CHAIRCD ";
        $query .= "FROM ";
        $query .= "    SCH_CHR_TEST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= ") ";
        $query .= "SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "    T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "    L1.SUBCLASSABBV AS LABEL ";
        $query .= "FROM ";
        if ($model->Properties["UnUseSchChrTest"] == "1") {
            if($model->recordTable != "RECORD_SCORE_DAT"){
                $query .= "    ".$model->recordTable." T1 ";
            }else{
                $query .= "     ( ";
                $query .= "     SELECT ";
                $query .= "         t1.YEAR, ";
                $query .= "         t1.SEMESTER, ";
                $query .= "         t1.CLASSCD, ";
                $query .= "         t1.SCHOOL_KIND, ";
                $query .= "         t1.CURRICULUM_CD, ";
                $query .= "         t1.SUBCLASSCD, ";
                $query .= "         t1.SCHREGNO, ";
                $query .= "         t3.CHAIRCD, ";
                $query .= "         t1.SCORE ";
                $query .= "     FROM ";
                $query .= "         ".$model->recordTable." t1 ";
                $query .= "        left join CHAIR_DAT t2 on t1.SUBCLASSCD = t2.SUBCLASSCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND and t1.CURRICULUM_CD = t2.CURRICULUM_CD and t1.SEMESTER = t2.SEMESTER and t1.YEAR = t2.YEAR ";
                $query .= "        left join CHAIR_STD_DAT t3 on t2.CHAIRCD = t3.CHAIRCD and t1.SCHREGNO = t3.SCHREGNO and t1.YEAR = t3.YEAR and t1.SEMESTER = t3.SEMESTER ";
                $query .= "     WHERE ";
                $query .= "         t1.SCHREGNO = '".$model->schregno."' AND ";
                $query .= "         t1.YEAR = '".$model->year."' AND ";
                $query .= "         t1.SEMESTER != '9'     AND ";
                $query .= "         t3.YEAR IS NOT NULL ";
                $query .= "     ORDER BY ";
                $query .= "         t1.CLASSCD, ";
                $query .= "         t1.SUBCLASSCD ";
                $query .= "     ) T1";
            }
        } else {
            $query .= "    CHAIR_DAT T1 ";
        }
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND L1.CLASSCD          = T1.CLASSCD ";
            $query .= "                         AND L1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "                         AND L1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->year."' ";
        $query .= "    AND T1.CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_STD_T) ";
        if ($model->Properties["UnUseSchChrTest"] != "1") {
            $query .= "    AND T1.CHAIRCD IN (SELECT CHAIRCD FROM TESTITEM_T) ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE, ";
        $query .= "    L1.SUBCLASSABBV ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    SEMESTERNAME, ";
        $query .= "    SDATE, ";
        $query .= "    EDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト種別取得
    function getTestKind($model, $semester)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    TESTKINDCD || TESTITEMCD AS TESTKINDCD, ";
        $query .= "    TESTITEMNAME ";
        $query .= "FROM ";
        if($model->testCountTable != "TESTITEM_MST_COUNTFLG_NEW_SDIV"){
            $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        } else {
            $query .= "    TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        }
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$semester."' ";
        $query .= "    AND TESTKINDCD < '99' ";
        if($model->testCountTable == "TESTITEM_MST_COUNTFLG_NEW_SDIV"){
            if($model->TRadio == 1){
                $query .= "    AND SCORE_DIV = '01' ";
            }else{
                $query .= "    AND SCORE_DIV = '08' ";
            }
        }
        $query .= "ORDER BY ";
        $query .= "    YEAR, ";
        $query .= "    SEMESTER, ";
        $query .= "    TESTKINDCD, ";
        $query .= "    TESTITEMCD ";

        return $query;
    }

    //得点データ取得
    function getScore($semester, $testkind, $subclasscd, $model)
    {
        $subclasscd = $subclasscd ? $subclasscd : "''";
        if ($model->recordTable == "RECORD_DAT") {
            $field = "SEM".$semester;
            if ($testkind == "0101") {
                $field .= "_INTR_SCORE";
            } else if ($testkind == "0102") {
                $field .= "_INTR2_SCORE";
            } else if ($testkind == "0201") {
                $field .= "_TERM_SCORE";
            } else if ($testkind == "0202") {
                $field .= "_TERM2_SCORE";
            }

            $query  = "SELECT ";
            $query .= "    ".$field." ";
            $query .= "FROM ";
            $query .= "    RECORD_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".$model->year."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN (".$subclasscd.") ";
            } else {
                $query .= "    AND SUBCLASSCD IN (".$subclasscd.") ";
            }
            $query .= "    AND TAKESEMES = '0' ";
            $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        } else if ($model->recordTable == "RECORD_SCORE_DAT") {
            $query  = " SELECT ";
            $query .= "    t1.YEAR, ";
            $query .= "    t1.SEMESTER, ";
            $query .= "    t1.TESTKINDCD, ";
            $query .= "    t1.TESTITEMCD, ";
            $query .= "    t1.SCORE_DIV, ";
            $query .= "    t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD as SUBCLASSCD, ";
            $query .= "    t1.SCHREGNO, ";
            $query .= "    t5.CHAIRCD, ";
            $query .= "    t1.SCORE ";
            if($model->avg_div != 9){
                $query .= "    , T3.AVG ";
            }
            $query .= " FROM ";
            $query .= "    SUBCLASS_MST t2  ";
            $query .= "    left join RECORD_SCORE_DAT t1 on t1.subclasscd = t2.subclasscd and t1.curriculum_cd = t2.curriculum_cd ";
            $query .= "     left join CHAIR_DAT t4 on t1.SUBCLASSCD = t4.SUBCLASSCD and t1.SCHOOL_KIND = t4.SCHOOL_KIND and t1.CURRICULUM_CD = t4.CURRICULUM_CD and t1.SEMESTER = t4.SEMESTER and t1.YEAR = t4.YEAR ";
            $query .= "     left join CHAIR_STD_DAT t5 on t4.CHAIRCD = t5.CHAIRCD and t1.SCHREGNO = t5.SCHREGNO and t1.YEAR = t5.YEAR and t1.SEMESTER = t5.SEMESTER ";

            if($model->avg_div != 9){
                if($model->testCountTable != "TESTITEM_MST_COUNTFLG_NEW_SDIV"){
                    $query .= "    left join RECORD_AVERAGE_DAT t3 on t1.year = t3.year and t1.semester = t3.semester and t1.testkindcd = t3.testkindcd and t1.testitemcd = t3.testitemcd and t1.school_kind = t3.school_kind and t1.curriculum_cd = t3.curriculum_cd and t1.subclasscd = t3.subclasscd and t3.avg_div = '".$model->avg_div."' ";
                }else{
                    if($model->Properties["use_school_detail_gcm_dat"] != 1 || $model->avg_div != 1){
                        $query .= "    left join RECORD_AVERAGE_SDIV_DAT t3 on t1.year = t3.year and t1.semester = t3.semester and t1.testkindcd = t3.testkindcd and t1.testitemcd = t3.testitemcd and t1.school_kind = t3.school_kind and t1.curriculum_cd = t3.curriculum_cd and t1.subclasscd = t3.subclasscd and t1.score_div = t3.score_div and t3.avg_div = '".$model->avg_div."' ";
                    }else{
                        $query .= " left join (SELECT ";
                        $query .= "                 a2.* ";
                        $query .= "             FROM ";
                        $query .= "                 (SELECT ";
                        $query .= "                     SCHREGNO, ";
                        $query .= "                     YEAR, ";
                        $query .= "                     SEMESTER, ";
                        $query .= "                     GRADE, ";
                        $query .= "                     COURSECD, ";
                        $query .= "                     MAJORCD, ";
                        $query .= "                     COURSECODE ";
                        $query .= "                 FROM ";
                        $query .= "                     SCHREG_REGD_DAT ";
                        $query .= "                 WHERE ";
                        $query .= "                     YEAR = '".$model->year."' AND ";
                        $query .= "                     SCHREGNO = '".$model->schregno."' ";
                        $query .= "                 UNION  ";
                        $query .= "                 SELECT ";
                        $query .= "                     SCHREGNO, ";
                        $query .= "                     YEAR, ";
                        $query .= "                     '9', ";
                        $query .= "                     GRADE, ";
                        $query .= "                     COURSECD, ";
                        $query .= "                     MAJORCD, ";
                        $query .= "                     COURSECODE ";
                        $query .= "                 FROM ";
                        $query .= "                     SCHREG_REGD_DAT ";
                        $query .= "                 WHERE ";
                        $query .= "                     YEAR = '".$model->year."' AND ";
                        $query .= "                     SCHREGNO = '".$model->schregno."' AND ";
                        $query .= "                     SEMESTER = (SELECT ";
                        $query .= "                                     MAX(SEMESTER) ";
                        $query .= "                                 FROM ";
                        $query .= "                                     SCHREG_REGD_DAT ";
                        $query .= "                                 WHERE ";
                        $query .= "                                     YEAR = '".$model->year."' AND ";
                        $query .= "                                     SCHREGNO = '".$model->schregno."' ";
                        $query .= "                                 ) ";
                        $query .= "                 ) a1  ";
                        $query .= "                 left join RECORD_AVERAGE_SDIV_DAT a2 on a1.YEAR = a2.YEAR and a1.SEMESTER = a2.SEMESTER and a1.GRADE = a2.GRADE  ";
                        $query .= "                 and a1.MAJORCD = a2.MAJORCD and a1.COURSECD = a2.COURSECD and a2.COURSECODE = '0000' ";
                        $query .= "             WHERE ";
                        $query .= "                 a2.SEMESTER IS NOT NULL ";
                        $query .= "    ) t3 on t1.year = t3.year and t1.semester = t3.semester and t1.testkindcd = t3.testkindcd and t1.testitemcd = t3.testitemcd and t1.school_kind = t3.school_kind and t1.curriculum_cd = t3.curriculum_cd and t1.subclasscd = t3.subclasscd and t1.score_div = t3.score_div and t3.avg_div = '4' ";
                    }
                }
                if($model->avg_div != 1 ){
                    $query .= "  and t3.grade = '".$model->grade."' and ";
                    if($model->avg_div == 2){
                        $query .= "  t3.hr_class = '".$model->limit."' ";
                    }else{
                        $query .= "  t3.coursecode = '".$model->limit."' ";
                    }
                }
            }
            $query .= " WHERE ";
            $query .= "    t1.YEAR='".$model->year."' AND ";
            $query .= "    t1.SEMESTER='".$semester."' AND ";
            $query .= "    t1.TESTKINDCD || t1.TESTITEMCD = '".$testkind."' and ";
/*            if($model->testCountTable != "TESTITEM_MST_COUNTFLG_NEW_SDIV"){
                $query .= "    t1.SCORE_DIV='01'  ";
            }else{
                $query .= "    t1.SCORE_DIV='01'  ";
            }*/
            if($model->TRadio == 1){
                $query .= "    t1.SCORE_DIV = '01' ";
            }else{
                $query .= "    t1.SCORE_DIV = '08' ";
            }
            
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD IN (".$subclasscd.") ";
            } else {
                $query .= "    AND t1.SUBCLASSCD IN (".$subclasscd.") ";
            }
            $query .= "    AND t1.SCHREGNO = '".$model->schregno."' ";
            $query .= "    AND t5.CHAIRCD IS NOT NULL ";
            $query .= " order by ";
            $query .= "    t1.semester,t1.testkindcd,t1.subclasscd ";

/*            $query  = "SELECT ";
            $query .= "    SCORE ";
            $query .= "FROM ";
            $query .= "    RECORD_SCORE_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".$model->year."' ";
            $query .= "    AND SEMESTER = '".$semester."' ";
            $query .= "    AND TESTKINDCD || TESTITEMCD = '".$testkind."' ";
            $query .= "    AND SCORE_DIV = '01' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN (".$subclasscd.") ";
            } else {
                $query .= "    AND SUBCLASSCD IN (".$subclasscd.") ";
            }
            $query .= "    AND SCHREGNO = '".$model->schregno."' ";*/
        } else {
            $field = "SEM".$semester;
            if ($testkind == "0101") {
                $field .= "_INTER_REC";
            } else if ($testkind == "0201") {
                $field .= "_TERM_REC";
            }

            $query  = "SELECT ";
            $query .= "    ".$field." ";
            $query .= "FROM ";
            $query .= "    KIN_RECORD_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".$model->year."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN (".$subclasscd.") ";
            } else {
                $query .= "    AND SUBCLASSCD IN (".$subclasscd.") ";
            }
            $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        }

        return $query;
    }

    //順位取得
    function getRank($semester, $testkind, $subclasscd, $model, $grade, $div)
    {
        $subclasscd = $subclasscd ? $subclasscd : "''";
        if ($model->recordTable == "KIN_RECORD_DAT") {
            $field = "SEM".$semester;
            if ($testkind == "0101") {
                $field .= "_INTER_REC_RANK";
            } else if ($testkind == "0201") {
                $field .= "_TERM_REC_RANK";
            }

            $query  = "SELECT ";
            $query .= $field." as SCORE ";
            $query .= "FROM ";
            $query .= "    RECORD_RANK_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".$model->year."' ";
            $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        } else {
            if($model->Radio == "1"){   //学年
                if($model->Properties["use_school_detail_gcm_dat"] == "1"){
                    $field = "MAJOR_".$div;
                }else{
                    $field = "GRADE_".$div;
                }
            }else if($model->Radio == "2"){     //クラス
                $field = "CLASS_".$div;
            }else if($model->Radio == "3"){     //コース
                $field = "COURSE_".$div;
            }else{      //平均なし
                if ($grade > 3) {
                    $field = "COURSE_".$div;
                } else {
                    $field = "GRADE_".$div;
                }
            }
            

            $query  = "SELECT ";
            $query .= "t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD as SUBCLASSCD, ";
            $query .= $field." as SCORE ";
            $query .= "FROM ";
            $query .= "    SUBCLASS_MST t2  ";
            if($model->testCountTable != "TESTITEM_MST_COUNTFLG_NEW_SDIV"){
                $query .= "    left join RECORD_RANK_DAT t1 on t1.subclasscd = t2.subclasscd and t1.curriculum_cd = t2.curriculum_cd ";
            }else{
                //$query .= "    left join RECORD_RANK_SDIV_DAT t1 on t1.subclasscd = t2.subclasscd and t1.curriculum_cd = t2.curriculum_cd and t1.score_div = '01' ";
                $query .= "    left join RECORD_RANK_SDIV_DAT t1 on t1.subclasscd = t2.subclasscd and t1.curriculum_cd = t2.curriculum_cd ";
                if($model->TRadio == 1){
                    $query .= "    AND t1.SCORE_DIV = '01' ";
                }else{
                    $query .= "    AND t1.SCORE_DIV = '08' ";
                }
            }
            $query .= "WHERE ";
            $query .= "    t1.YEAR = '".$model->year."' ";
            $query .= "    AND t1.SEMESTER = '".$semester."' ";
            $query .= "    AND t1.TESTKINDCD || t1.TESTITEMCD = '".$testkind."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD IN (".$subclasscd.") ";
            } else {
                $query .= "    AND t1.SUBCLASSCD IN (".$subclasscd.") ";
            }
            $query .= "    AND t1.SCHREGNO = '".$model->schregno."' ";
        }

        return $query;
    }

    //学校マスタの取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //欠課数取得
    function getAbsent($semester, $subclasscd, $model, $knjSchoolMst)
    {
        $subclasscd = $subclasscd != "" ? $subclasscd : "''";
        $query  = "SELECT ";
        $query .= "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD as SUBCLASSCD, ";
        if ($model->absent_cov == 0 || $model->absent_cov_late == 0) {
            $query .= "    (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE) + SUM(NURSEOFF) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + SUM(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + SUM(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + SUM(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + SUM(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + SUM(ABSENT) ";
            }
            $query .= "    ) AS SCORE ";
        } else if ($model->absent_cov < 3) {
            $query .= "    (((SUM(LATE) + SUM(EARLY)) / ".$model->absent_cov_late.") + (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE) + SUM(NURSEOFF) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + SUM(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + SUM(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + SUM(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + SUM(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + SUM(ABSENT) ";
            }
            $query .= "    )) AS SCORE ";
        } else {
            $query .= "    DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / ".$model->absent_cov_late.") + (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE) + SUM(NURSEOFF) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + SUM(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + SUM(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + SUM(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + SUM(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + SUM(ABSENT) ";
            }
            $query .= "    ),4,1) AS SCORE ";
        }
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "          COPYCD, ";
        $query .= "          YEAR, ";
        $query .= "          MONTH, ";
        $query .= "          SEMESTER, ";
        $query .= "          SCHREGNO, ";
        $query .= "          CLASSCD, ";
        $query .= "          SCHOOL_KIND, ";
        $query .= "          CURRICULUM_CD, ";
        $query .= "          SUBCLASSCD, ";
        $query .= "          APPOINTED_DAY, ";
        $query .= "          LESSON, ";
        $query .= "          case when OFFDAYS IS NULL then 0 else OFFDAYS end as OFFDAYS, ";
        $query .= "          case when ABSENT IS NULL then 0 else ABSENT end as ABSENT, ";
        $query .= "          case when SUSPEND IS NULL then 0 else SUSPEND end as SUSPEND, ";
        $query .= "          case when MOURNING IS NULL then 0 else MOURNING end as MOURNING, ";
        $query .= "          case when ABROAD IS NULL then 0 else ABROAD end as ABROAD, ";
        $query .= "          case when SICK IS NULL then 0 else SICK end as SICK, ";
        $query .= "          case when NOTICE IS NULL then 0 else NOTICE end as NOTICE, ";
        $query .= "          case when NONOTICE IS NULL then 0 else NONOTICE end as NONOTICE, ";
        $query .= "          case when NURSEOFF IS NULL then 0 else NURSEOFF end as NURSEOFF, ";
        $query .= "          case when LATE IS NULL then 0 else LATE end as LATE, ";
        $query .= "          case when EARLY IS NULL then 0 else EARLY end as EARLY, ";
        $query .= "          case when VIRUS IS NULL then 0 else VIRUS end as VIRUS, ";
        $query .= "          case when KOUDOME IS NULL then 0 else KOUDOME end as KOUDOME ";
        $query .= "     FROM ";
        $query .= "          ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "          COPYCD = '0' ";
        $query .= "     AND YEAR = '".$model->year."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN (".$subclasscd.") ";
        } else {
            $query .= "    AND SUBCLASSCD IN (".$subclasscd.") )";
        }

        $query .= "    ) T1 ";
        $query .= "GROUP BY ";
        $query .= "    CLASSCD, SCHOOL_KIND , CURRICULUM_CD, SUBCLASSCD ";

        return $query;
    }

}
?>
