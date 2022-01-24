<?php

require_once('for_php7.php');


class knjd129bQuery extends Query {

    //学年取得
    function getGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //テスト種別
    function getTest($model) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || '：' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //教科
    function getClassMst($model) {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || ':' || T3.CLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.CLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '：' || T3.CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     CLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T4.YEAR       = T1.YEAR ";
        $query .= "     AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD    = T1.CHAIRCD ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND T4.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "     AND T3.CLASSCD <= '90' ";
        $query .= "     AND T3.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1 ,2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //科目データの取得
    function getSubclassMst($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T4.YEAR       = T1.YEAR ";
        $query .= "     AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD    = T1.CHAIRCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD          = '".$class_array[0]."' ";
            $query .= "     AND T3.SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     AND SUBSTR(T3.SUBCLASSCD, 1 ,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.chaircd, ";
        $query .= "     T1.chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //テスト名取得
    function getTestName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTERNAME, ";
        $query .= "     T1.SEMESTER as VALUE_SEME, ";
        $query .= "     rtrim(ltrim(substr(T2.TESTITEMNAME,1,15))) as TESTITEMNAME, "; //頭全角３文字
        $query .= "     T1.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD AS VALUE_TEST ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1, ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER <> '9' AND ";
        $query .= "     T2.YEAR = T1.YEAR AND ";
        $query .= "     T2.SEMESTER = T1.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE_TEST ";

        return $query;
    }

    //時間割講座テストより試験日を抽出
    function selectExecuteDateQuery($model, $opt_chair_cmb) {
        $query  = " SELECT DISTINCT ";
        $query .= "    EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '".$model->field["TESTKIND"]."' AND ";
        $query .= "     CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') ";
        $query .= " ORDER BY EXECUTEDATE ";
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date, $opt_chair_cmb) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //定期考査の出欠の表１
        $query  = " WITH TEST_ATTEND_A AS ( ";
        $query .= "     SELECT  '1' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '1' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '1' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '1' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '2' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '2' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '2' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '2' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '3' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '3' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '3' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '3' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "        )  ";
        //定期考査の出欠の表２
        $query .= " ,TEST_ATTEND_B AS ( ";
        $query .= "     SELECT  SCHREGNO,  ";
        $query .= "             MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM1_INTR_SCORE_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM1_TERM_SCORE_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM2_INTR_SCORE_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM2_TERM_SCORE_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM3_INTR_SCORE_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM3_TERM_SCORE_ATTEND  ";
        $query .= "     FROM    TEST_ATTEND_A  ";
        $query .= "     GROUP BY SCHREGNO  ";
        $query .= "     ) ";
        //成績データの表
        $query .= " ,RECORD_SCORE AS ( ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SELECT  YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ,SCHREGNO as SCHREGNO2 ";
        } else {
            $query .= "     SELECT  YEAR ,SUBCLASSCD ,SCHREGNO as SCHREGNO2 ";
        }
        //素点
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1010101' THEN SCORE END) as SEM1_INTR_SCORE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1020101' THEN SCORE END) as SEM1_TERM_SCORE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2010101' THEN SCORE END) as SEM2_INTR_SCORE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2020101' THEN SCORE END) as SEM2_TERM_SCORE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3010101' THEN SCORE END) as SEM3_INTR_SCORE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3020101' THEN SCORE END) as SEM3_TERM_SCORE ";
        //学期成績
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1990000' THEN VALUE END) as SEM1_VALUE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2990000' THEN VALUE END) as SEM2_VALUE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3990000' THEN VALUE END) as SEM3_VALUE ";
        $query .= "     FROM   RECORD_SCORE_DAT ";
        $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "       AND  SUBCLASSCD       = '".$subclass_array[3]."' ";
            $query .= "     GROUP BY YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ,SCHREGNO ";
        } else {
            $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            $query .= "     GROUP BY YEAR ,SUBCLASSCD ,SCHREGNO ";
        }
        $query .= "     ) ";
        //見込点データの表
        $query .= " ,SUPP_EXA AS ( ";
        $query .= "     SELECT SCHREGNO AS EXA_SCHNO ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10101' THEN SCORE END) AS SEM1_INTR_SCORE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10201' THEN SCORE END) AS SEM1_TERM_SCORE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20101' THEN SCORE END) AS SEM2_INTR_SCORE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20201' THEN SCORE END) AS SEM2_TERM_SCORE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '30101' THEN SCORE END) AS SEM3_INTR_SCORE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '30201' THEN SCORE END) AS SEM3_TERM_SCORE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '19900' THEN SCORE END) as SEM1_VALUE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '29900' THEN SCORE END) as SEM2_VALUE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '39900' THEN SCORE END) as SEM3_VALUE_SCORE ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10101' THEN SCORE_PASS END) AS SEM1_INTR_SCORE_PASS ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10201' THEN SCORE_PASS END) AS SEM1_TERM_SCORE_PASS ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20101' THEN SCORE_PASS END) AS SEM2_INTR_SCORE_PASS ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20201' THEN SCORE_PASS END) AS SEM2_TERM_SCORE_PASS ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '30101' THEN SCORE_PASS END) AS SEM3_INTR_SCORE_PASS ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '30201' THEN SCORE_PASS END) AS SEM3_TERM_SCORE_PASS ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '19900' THEN SCORE_PASS END) as SEM1_VALUE_PASS ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '29900' THEN SCORE_PASS END) as SEM2_VALUE_PASS ";
        $query .= "           ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '39900' THEN SCORE_PASS END) as SEM3_VALUE_PASS ";
        $query .= "     FROM   SUPP_EXA_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "       AND  SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "       AND  SUBCLASSCD='".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "       AND  SCORE_FLG = '2' ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     ) ";
        //成績データNULL生徒の表
        $scoreDiv = substr($model->field["TESTKIND"], 0, 2) != "99" ? "01" : "00"; //01:中間・期末 00:学期末
        $scoreField = substr($model->field["TESTKIND"], 0, 2) != "99" ? "SCORE" : "VALUE"; //SCORE:中間・期末 VALUE:学期末
        $query .= " ,RECORD_SCORE_NULL AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "     FROM   RECORD_SCORE_DAT ";
        $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND  SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "       AND  TESTKINDCD||TESTITEMCD = '".$model->field["TESTKIND"]."' ";
        $query .= "       AND  SCORE_DIV = '{$scoreDiv}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "       AND  SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     HAVING SUM({$scoreField}) IS NULL ";
        $query .= "     ) ";

        $query .= " SELECT W0.*";
        $query .= "       ,W4.*";
        $query .= "       ,W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W5.chair_sem1,W6.chair_sem2,W33.chair_sem3 ";
        $query .= "       ,W7.SEM1_INTR_SCORE_ATTEND,W7.SEM1_TERM_SCORE_ATTEND ";
        $query .= "       ,W7.SEM2_INTR_SCORE_ATTEND,W7.SEM2_TERM_SCORE_ATTEND ";
        $query .= "       ,W7.SEM3_INTR_SCORE_ATTEND,W7.SEM3_TERM_SCORE_ATTEND ";
        $query .= "   FROM ";
        $query .= "    (SELECT SCHREGNO ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd IN ('".implode("','", $opt_chair_cmb)."')  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "    ) W1 ";
        /* 成績情報 */
        $query .= " INNER JOIN RECORD_SCORE_NULL R0 ON R0.SCHREGNO = W1.SCHREGNO ";
        $query .= " LEFT JOIN RECORD_SCORE W0 ON W0.SCHREGNO2 = W1.schregno ";
        /* 見込点情報 */
        $query .= " LEFT OUTER JOIN SUPP_EXA W4 ON W4.EXA_SCHNO = W1.schregno ";
        /* 基礎情報 */
        $query .= " INNER JOIN ";
        $query .= "    (SELECT T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.name_show,T2.schregno, T2.grd_date, T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE ";
        $query .= "       FROM schreg_regd_hdat T0, schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "      WHERE T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "        AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".$model->field["SEMESTER"]."' ";
        $query .= "        AND T1.grade    = '".$model->field["GRADE"]."' ";
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W1.schregno = W2.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 在籍情報(１学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem1";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  IN ('".implode("','", $opt_chair_cmb)."') ";
        $query .= "        AND semester = '1') W5";
        $query .= "   ON W5.schregno = W1.schregno ";
        /* 在籍情報(２学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem2";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  IN ('".implode("','", $opt_chair_cmb)."') ";
        $query .= "        AND semester = '2') W6";
        $query .= "   ON W6.schregno = W1.schregno ";
        /* 在籍情報(３学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem3";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  IN ('".implode("','", $opt_chair_cmb)."') ";
        $query .= "        AND semester = '3') W33";
        $query .= "   ON W33.schregno = W1.schregno ";
        /* 出欠情報 */
        $query .= " LEFT OUTER JOIN TEST_ATTEND_B W7 ON W7.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    //科目平均
    function getAverage($year, $testcd, $subclasscd, $grade, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     DECIMAL(ROUND(AVG * 10, 0) / 10, 5, 1) AS AVG ";
        $query .= " FROM ";
        $query .= "     RECORD_AVERAGE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "     AVG_DIV = '1' AND "; //学年
        $query .= "     GRADE = '{$grade}' ";
        return $query;
    }

    //考査満点マスタ
    function getPerfect($year, $subclasscd, $testcd, $grade, $course, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= "SELECT ";
        $query .= "    case when T1.DIV IS NULL then 100 else T1.PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_DAT T1 ";
        $query .= "    LEFT JOIN COURSE_GROUP_CD_DAT G1 ";
        $query .= "         ON G1.YEAR = T1.YEAR ";
        $query .= "        AND G1.GRADE = '{$grade}' ";
        $query .= "        AND G1.COURSECD || G1.MAJORCD || G1.COURSECODE = '{$course}' ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "    T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '{$testcd}' AND ";
        $query .= "    T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') THEN '00000000' WHEN T1.DIV = '04' THEN '0' || G1.GROUP_CD || '0000' ELSE '{$course}' END ";
        return $query;
    }

    //見込
    function updateSuppExaDat($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();

        //年度
        $year = CTRL_YEAR;
        //指定学期テスト
        $testcd = $model->field["SEMESTER"].$model->field["TESTKIND"];

        for ($i = 0; $i < $model->field["COUNT"]; $i++) {
            //見込点指定チェック
            if ($model->fields["CHK_BOX"][$i] != "on") {
                continue;//処理しない
            }

            $schno = $model->fields["SCHREGNO"][$i];

            //削除
            $query = knjd129bQuery::delSuppExaDat($year, $testcd, $model->field["SUBCLASSCD"], $schno, $model);
            $db->query($query);
            //追加
            $score      = $model->fields["SCORE"][$i];
            $score_pass = $model->fields["SCORE_PASS"][$i];
            if (!strlen($score_pass)) {
                continue;//追加しない
            }
            $data = array();
            $data["YEAR"][TEXT]         = $year;
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]   = substr($model->field["TESTKIND"], 0, 2);
            $data["TESTITEMCD"][TEXT]   = substr($model->field["TESTKIND"], 2, 2);
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
            }
            $data["SCHREGNO"][TEXT]     = $schno;
            $data["SCORE"][NUMBER]      = $score;
            $data["SCORE_PASS"][NUMBER] = $score_pass;
            $data["SCORE_FLG"][TEXT]    = "2";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SUPP_EXA_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    function delSuppExaDat($year, $testcd, $subclasscd, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   SUPP_EXA_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "  AND  SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "  AND  CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "  AND  SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '{$subclasscd}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }
}
?>
