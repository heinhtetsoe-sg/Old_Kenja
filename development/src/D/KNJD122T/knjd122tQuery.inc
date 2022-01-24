<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd122tQuery.inc 56586 2017-10-22 12:52:35Z maeshiro $

class knjd122tquery extends Query {

    //テスト名取得
    function getTestName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTERNAME, ";
        $query .= "     T1.SEMESTER as VALUE_SEME, ";
        $query .= "     rtrim(ltrim(substr(T2.TESTITEMNAME,1,9))) as TESTITEMNAME, "; //頭全角３文字
        $query .= "     T1.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD AS VALUE_TEST ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1, ";
        $query .= "     ".$model->testTable." T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE_TEST ";

        return $query;
    }

    //RECORD_DATに存在しない生徒をinsert(履修学期が複数あれば複数レコード作成）
    function insertEx_Std_RecQuery($model){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " INSERT INTO RECORD_DAT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " (YEAR,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,TAKESEMES,SCHREGNO,CHAIRCD,REGISTERCD,UPDATED) ";
        } else {
            $query .= " (YEAR,SUBCLASSCD,TAKESEMES,SCHREGNO,CHAIRCD,REGISTERCD,UPDATED) ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "    t1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t2.CLASSCD, ";
            $query .= "     t2.SCHOOL_KIND, ";
            $query .= "     t2.CURRICULUM_CD, ";
        }
        $query .= "    t2.SUBCLASSCD, ";
//        $query .= "    T2.TAKESEMES, ";
        $query .= "    '0', ";//NO003
        $query .= "    t1.SCHREGNO, ";
        $query .= "    t1.CHAIRCD, ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    sysdate() ";
        $query .= " FROM ";
        $query .= "    CHAIR_STD_DAT t1, ";
        $query .= "    CHAIR_DAT t2 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR       = t2.YEAR AND ";
        $query .= "     t1.CHAIRCD    = t2.CHAIRCD AND ";
        $query .= "     T1.semester   = T2.semester AND ";
        $query .= "     t1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     t1.CHAIRCD    = '".$model->field["CHAIRCD"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t2.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "     t2.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "     t2.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "     t2.SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "     t2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     t1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             RECORD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR='".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND CLASSCD        = '".$substr_classcd."' ";
            $query .= "                               AND SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "                               AND CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "                               AND SUBCLASSCD     = '".$substr_subclasscd."') ";
        } else {
            $query .= "                               AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."') ";
        }

      return $query;
    }

    //編集可能学期の判別
    function selectContolCodeQuery($model){
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$schoolKind."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '1' ";

      return $query;
    }

    //時間割講座テストより試験日を抽出
    function selectExecuteDateQuery($model, $semester, $testkindcd, $testitemcd){

        $query  = " SELECT DISTINCT ";
        $query .= "    EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     TESTKINDCD = '".$testkindcd."' AND ";
        $query .= "     TESTITEMCD = '".$testitemcd."' AND ";
        $query .= "     CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= " ORDER BY EXECUTEDATE ";
      return $query;
    }

    //科目データの取得
    function selectSubclassQuery($gen_ed, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     subclass_mst T3, ";
        $query .= "     chair_stf_dat T4 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T1.year       = T4.year AND ";
        $query .= "     T1.semester   = T4.semester AND ";
        $query .= "     T1.chaircd    = T4.chaircd AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "    (T3.subclasscd NOT LIKE '9%' OR T3.subclasscd LIKE '90%' ) AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD  AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND  AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD  AND ";
        }
        $query .= "     T3.subclasscd = T1.subclasscd ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd ";
        return $query;
    }
    
    //講座データの取得
    function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
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
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //遅刻何回で欠課とするかの指数取得
    function getScAbsentCov()
    {
        return "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '".CTRL_YEAR."'";
    }

    //累積最大日取得
    function GetMax($subclasscd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "SELECT DISTINCT year ";
        $query .= "              ,month ";
        $query .= "              ,CASE MONTH WHEN '01' THEN rtrim(CHAR(SMALLINT(year) + 1))  ";
        $query .= "                          WHEN '02' THEN rtrim(CHAR(SMALLINT(year) + 1))  ";
        $query .= "                          WHEN '03' THEN rtrim(CHAR(SMALLINT(year) + 1)) ELSE year END AS nen_year ";
        $query .= "              ,CASE MONTH WHEN '01' THEN rtrim(CHAR(SMALLINT(month) + 12))  ";
        $query .= "                          WHEN '02' THEN rtrim(CHAR(SMALLINT(month) + 12))  ";
        $query .= "                          WHEN '03' THEN rtrim(CHAR(SMALLINT(month) + 12)) ELSE month END AS tmp_month ";
        $query .= "              ,semester ";
        $query .= "              ,appointed_day ";
        $query .= "         FROM attend_subclass_dat ";
        $query .= "        WHERE COPYCD     = '0' ";
        $query .= "          AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND CLASSCD        = '".$substr_classcd."' ";
            $query .= "          AND SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "          AND CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "          AND SUBCLASSCD     = '".$substr_subclasscd."' ";
        } else {
            $query .= "          AND subclasscd = '".$subclasscd."' ";
        }
        $query .= "          AND semester   = (SELECT MAX(semester) FROM attend_subclass_dat ";
        $query .= "                             WHERE COPYCD     = '0' ";
        $query .= "                               AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND CLASSCD        = '".$substr_classcd."' ";
            $query .= "                               AND SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "                               AND CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "                               AND SUBCLASSCD     = '".$substr_subclasscd."') ";
        } else {
            $query .= "                               AND subclasscd = '".$subclasscd."') ";
        }
        $query .= "ORDER BY tmp_month DESC ";
        return $query;
    }

    //評定マスタ
    function GetAssessMark()
    {
        $query  = "SELECT assessmark,assesslevel";
        $query .= "  FROM assess_mst ";
        $query .= " WHERE assesscd = '3'";
        return $query;
    }

    //合併先科目の単位固定／加算フラグ
    function getCalculateCreditFlg($subclasscd, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  = "SELECT ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD, CALCULATE_CREDIT_FLG ";
        } else {
            $query  = "SELECT ATTEND_SUBCLASSCD, CALCULATE_CREDIT_FLG ";
        }
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  COMBINED_CLASSCD        = '".$substr_classcd."' ";
            $query .= "  AND  COMBINED_SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "  AND  COMBINED_CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "  AND  COMBINED_SUBCLASSCD     = '".$substr_subclasscd."' ";
            $query .= "ORDER BY COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, ATTEND_SUBCLASSCD ";
        } else {
            $query .= "  AND  COMBINED_SUBCLASSCD = '".$subclasscd."' ";
            $query .= "ORDER BY ATTEND_SUBCLASSCD ";
        }
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "";
        $query .= " WITH T_CHAIRCD AS ( ";
        $query .= "     SELECT CHAIRCD ";
        $query .= "       FROM CHAIR_DAT ";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND CLASSCD        = '".$substr_classcd."' ";
            $query .= "         AND SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "         AND CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "         AND SUBCLASSCD     = '".$substr_subclasscd."' ";
        } else {
            $query .= "         AND SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     GROUP BY CHAIRCD ";
        $query .= "     ) ";
        //定期考査の出欠の表１
        $query .= " , TEST_ATTEND_A AS ( ";
        $query .= "     SELECT  '1' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '1' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD IN (SELECT CHAIRCD FROM T_CHAIRCD) AND  ";
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
        $query .= "             T1.CHAIRCD IN (SELECT CHAIRCD FROM T_CHAIRCD) AND  ";
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
        $query .= "             T1.CHAIRCD IN (SELECT CHAIRCD FROM T_CHAIRCD) AND  ";
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
        $query .= "             T1.CHAIRCD IN (SELECT CHAIRCD FROM T_CHAIRCD) AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";

        $query .= "     UNION  ";
        $query .= "     SELECT  '2' AS SEMES, '03' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '2' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '02' AND  ";
        $query .= "             T1.CHAIRCD IN (SELECT CHAIRCD FROM T_CHAIRCD) AND  ";
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
        $query .= "             T1.CHAIRCD IN (SELECT CHAIRCD FROM T_CHAIRCD) AND  ";
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
        $query .= "             T1.CHAIRCD IN (SELECT CHAIRCD FROM T_CHAIRCD) AND  ";
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
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '03' THEN INT(DI_CD) END) AS SEM2_TERM2_SCORE_ATTEND,  ";//Add
        $query .= "             MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM3_INTR_SCORE_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM3_TERM_SCORE_ATTEND  ";
        $query .= "     FROM    TEST_ATTEND_A  ";
        $query .= "     GROUP BY SCHREGNO  ";
        $query .= "     ) ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " ,RECORD_PROV_FLG AS ( ";
            $query .= "     SELECT  SCHREGNO, PROV_FLG ";
            $query .= "     FROM   RECORD_PROV_FLG_DAT ";
            $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       AND  CLASSCD          = '".$substr_classcd."' ";
                $query .= "       AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
                $query .= "       AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                $query .= "       AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
            } else {
                $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "     ) ";
        }

        $query .= " SELECT W1.*";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "       ,P0.PROV_FLG";
        }
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W5.chair_sem1 ";
        $query .= "       ,W6.chair_sem2 ";
        $query .= "       ,W7.SEM1_INTR_SCORE_ATTEND,W7.SEM1_TERM_SCORE_ATTEND ";
        $query .= "       ,W7.SEM2_INTR_SCORE_ATTEND,W7.SEM2_TERM_SCORE_ATTEND,W7.SEM2_TERM2_SCORE_ATTEND ";//Add
        $query .= "       ,W7.SEM3_INTR_SCORE_ATTEND,W7.SEM3_TERM_SCORE_ATTEND ";
        if ($model->isJitu) {
            $query .= "   ,value(L1.COMP_ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        } else {
            $query .= "   ,value(W8.ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        }
        $query .= "       ,W8.CREDITS,W8.AUTHORIZE_FLG ";
        $query .= "       ,W9.COMP_CREDIT_SUM,W9.GET_CREDIT_SUM,W9.GET_CREDIT_SUM_Y ";//合併先科目の単位を抽出
        $query .= "       ,value(W9.MOTO_CNT,0) as MOTO_CNT,value(W9.CREDIT_CNT,0) as CREDIT_CNT ";
        $query .= "   FROM ";
        $query .= "    (SELECT * ";
        $query .= "       FROM record_dat ";
        $query .= "      WHERE year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD          = '".$substr_classcd."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "       AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "       AND  subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "        AND SCHREGNO IN(SELECT SCHREGNO  ";
        $query .= "                          FROM CHAIR_STD_DAT  ";
        $query .= "                         WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "                           AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= "                           AND '".$execute_date."' BETWEEN APPDATE AND APPENDDATE) ";
        $query .= "    ) W1 ";
        /* 基礎情報 */
        $query .= " INNER JOIN ";
        $query .= "    (SELECT T1.year,T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.inoutcd,T2.name_show,T2.schregno, T2.grd_date, T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE ";
        $query .= "       FROM schreg_regd_hdat T0, schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "      WHERE T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "        AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".CTRL_SEMESTER."' ";
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W1.year = W2.year AND W1.schregno = W2.schregno ";
        /* 仮評定情報 */
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " LEFT JOIN RECORD_PROV_FLG P0 ON P0.SCHREGNO = W1.SCHREGNO ";
        }
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 在籍情報(前期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem1";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '1') W5";
        $query .= "   ON W5.schregno = W1.schregno ";
        /* 在籍情報(後期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem2";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '2') W6";
        $query .= "   ON W6.schregno = W1.schregno ";
        /* 出欠情報 */
        $query .= " LEFT OUTER JOIN TEST_ATTEND_B W7 ON W7.schregno = W1.schregno ";

        /* 単位マスタ情報(単位数、欠課数上限値、半期認定フラグ) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.SCHREGNO, T2.CREDITS, T2.ABSENCE_HIGH, T2.AUTHORIZE_FLG ";
        $query .= "     FROM   CREDIT_MST T2, SCHREG_REGD_DAT T1 ";
        $query .= "     WHERE  T1.YEAR='".CTRL_YEAR."' AND T1.SEMESTER='".CTRL_SEMESTER."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            T2.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "            T2.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "            T2.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "            T2.SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "            T2.SUBCLASSCD='".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "            T2.CREDITS IS NOT NULL AND ";
        $query .= "            T2.YEAR=T1.YEAR AND ";
        $query .= "            T2.COURSECD=T1.COURSECD AND ";
        $query .= "            T2.MAJORCD=T1.MAJORCD AND ";
        $query .= "            T2.COURSECODE=T1.COURSECODE AND ";
        $query .= "            T2.GRADE=T1.GRADE) W8 ";
        $query .= "   ON W8.schregno = W1.schregno ";
        if ($model->isJitu) {
            $query .= "        LEFT JOIN SCHREG_ABSENCE_HIGH_DAT L1 ON L1.YEAR = '".CTRL_YEAR."' ";
            $query .= "             AND L1.DIV = '".$model->absenceDiv."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             AND L1.CLASSCD          = '".$substr_classcd."' ";
                $query .= "             AND L1.SCHOOL_KIND      = '".$substr_school_kind."' ";
                $query .= "             AND L1.CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                $query .= "             AND L1.SUBCLASSCD       = '".$substr_subclasscd."' ";
            } else {
                $query .= "             AND L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "             AND L1.SCHREGNO = W1.SCHREGNO ";
        }

        /* 合併先科目の単位を抽出 */
        /* 合併先科目の単位は、生徒が選択した合併元科目の単位数を加算した値である */
        /* 「名称予備１＝Y」の場合、合併元科目の「評定＝１（履修のみ）」を合併先科目では「修得」として扱う */
        // MOTO_CNT・・・合併元科目のレコード数
        // CREDIT_CNT・・単位数が登録されている合併元科目のレコード数
        $query .= " LEFT JOIN (";
        $query .= "SELECT T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       COUNT(T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD) AS MOTO_CNT, ";
        } else {
            $query .= "       COUNT(T2.SUBCLASSCD) AS MOTO_CNT, ";
        }
        $query .= "       SUM(case when T2.COMP_CREDIT IS NOT NULL OR T2.GET_CREDIT IS NOT NULL then 1 else 0 end) AS CREDIT_CNT, ";
        $query .= "       SUM(T2.COMP_CREDIT) AS COMP_CREDIT_SUM, ";
        $query .= "       SUM(case when 1 < T2.GRAD_VALUE then T2.GET_CREDIT ";
        $query .= "                when 1 = T2.GRAD_VALUE then T2.COMP_CREDIT end) AS GET_CREDIT_SUM_Y, ";
        $query .= "       SUM(T2.GET_CREDIT) AS GET_CREDIT_SUM ";
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "       RECORD_DAT T2 ";
        $query .= "WHERE  T1.YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  T1.COMBINED_CLASSCD       = '".$substr_classcd."' ";
            $query .= "       AND  T1.COMBINED_SCHOOL_KIND   = '".$substr_school_kind."' ";
            $query .= "       AND  T1.COMBINED_CURRICULUM_CD = '".$substr_curriculum_cd."' ";
            $query .= "       AND  T1.COMBINED_SUBCLASSCD    = '".$substr_subclasscd."' ";
        } else {
            $query .= "       AND  T1.COMBINED_SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "  AND  T2.YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  T2.CLASSCD       = T1.ATTEND_CLASSCD ";
            $query .= "       AND  T2.SCHOOL_KIND   = T1.ATTEND_SCHOOL_KIND ";
            $query .= "       AND  T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
            $query .= "       AND  T2.SUBCLASSCD    = T1.ATTEND_SUBCLASSCD ";
        } else {
            $query .= "       AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        }
//      $query .= "  AND (T2.COMP_CREDIT IS NOT NULL OR T2.GET_CREDIT IS NOT NULL) ";
        $query .= "GROUP BY T2.SCHREGNO ";
        $query .= ") W9 ON W9.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    // 学校詳細マスタ
    function getSchoolMst()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //考査満点マスタ
    function getPerfect($year, $subclasscd, $testcd, $grade, $course, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "";
        $query .= "SELECT ";
        $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "    SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "    CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "    SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "    SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$course}' END ";
        return $query;
    }

    // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
    // 名称区分：D015　名称コード：01　名称予備１：「Y」もしくは「NULL」
    function getNameMst()
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAMESPARE1 "; // Y:, null:
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' AND ";
        $query .= "    NAMECD1='D015' AND ";
        $query .= "    NAMECD2='01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
    function getOffdaysFlg($year) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUB_OFFDAYS,SUB_ABSENT,SUB_SUSPEND,SUB_MOURNING,SUB_VIRUS,SUB_KOUDOME ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$year}' ";
        return $query;
    }

    function GetAttendData($chaircd,$subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($offdaysFlg["SUB_OFFDAYS"] == "1")  $notice .= " + SUM(value(OFFDAYS,0))";
        if ($offdaysFlg["SUB_ABSENT"] == "1")   $notice .= " + SUM(value(ABSENT,0))";
        if ($offdaysFlg["SUB_SUSPEND"] == "1")  $notice .= " + SUM(value(SUSPEND,0))";
        if ($offdaysFlg["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        if ($offdaysFlg["SUB_VIRUS"] == "1")    $notice .= " + SUM(value(VIRUS,0))";
        if ($offdaysFlg["SUB_KOUDOME"] == "1")  $notice .= " + SUM(value(KOUDOME,0))";
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query  = "SELECT schregno ";
        $query .= "       ,".$notice."  AS T_NOTICE ";

        //小数点ありAdd
        if ($absent_cov == "4" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";

        } elseif ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "   ,".$late_early."     AS T_LATEEARLY ";
            $query .= "   ,".$notice."  AS NOTICE_LATE ";
        }
        $query .= "  FROM attend_subclass_dat  ";
        $query .= " WHERE COPYCD     = '0' ";
        $query .= "   AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND CLASSCD          = '".$substr_classcd."' ";
            $query .= "   AND SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "   AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "   AND SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "   AND subclasscd = '".$subclasscd."' ";
        }
        $query .= "   AND EXISTS (SELECT 'X' FROM chair_std_dat ";
        $query .= "                WHERE year    = '".CTRL_YEAR."'";
        $query .= "                  AND chaircd = '".$chaircd."')";
        $query .= " GROUP BY schregno";
        return $query;
    }

    //(学期ごとに集計）する場合
    function GetAttendData2($chaircd,$subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($offdaysFlg["SUB_OFFDAYS"] == "1")  $notice .= " + SUM(value(OFFDAYS,0))";
        if ($offdaysFlg["SUB_ABSENT"] == "1")   $notice .= " + SUM(value(ABSENT,0))";
        if ($offdaysFlg["SUB_SUSPEND"] == "1")  $notice .= " + SUM(value(SUSPEND,0))";
        if ($offdaysFlg["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        if ($offdaysFlg["SUB_VIRUS"] == "1")    $notice .= " + SUM(value(VIRUS,0))";
        if ($offdaysFlg["SUB_KOUDOME"] == "1")  $notice .= " + SUM(value(KOUDOME,0))";
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno";
        $query .= "               ,T1.semester ";
        $query .= "               ,".$notice." AS T_NOTICE ";

        //小数点ありAdd
        if ($absent_cov == "3" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";

        } elseif ($absent_cov == "1" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "           ,".$late_early."    AS T_LATEEARLY ";
            $query .= "           ,".$notice." AS NOTICE_LATE ";
        }
        $query .= "          FROM attend_subclass_dat T1   ";
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND T1.CLASSCD          = '".$substr_classcd."' ";
            $query .= "           AND T1.SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "           AND T1.CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "           AND T1.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "           AND T1.subclasscd = '".$subclasscd."' ";
        }
        $query .= "           AND EXISTS (SELECT 'X' FROM chair_std_dat T2 ";
        $query .= "                        WHERE T1.year     = T2.year ";
        $query .= "                          AND T1.schregno = T2.schregno "; 
        $query .= "                          AND T2.chaircd  = '".$chaircd."')";
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " GROUP BY schregno ";
        return $query;
    }

    function getNameMstD008($model, $schoolKind) {
        if ($model->Properties["useClassDetailDat"] == "1") {
            $query  = "";
            $query .= " SELECT ";
            $query .= "     CLASSCD AS NAMECD2 ";
            $query .= " FROM ";
            $query .= "     CLASS_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "     AND CLASS_SEQ = '003' ";
        } else {
            $query  = "";
            $query .= " SELECT ";
            $query .= "     NAMECD2 ";
            $query .= " FROM ";
            $query .= "     V_NAME_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND NAMECD1 = 'D008' ";
        }

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        //管理者コントロール
        $admin_key = array();
        $results = $db->query(knjd122tQuery::selectContolCodeQuery($model));
        while( $row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $admin_key[$row_ct["CONTROL_CODE"]]=$row_ct["CONTROL_CODE"];
        }
        $results->free();
        //「評定を指定しない科目」の教科コードを配列にセット
        $classcdD008 = array();
        $results = $db->query(knjd122tQuery::getNameMstD008($model, $substr_school_kind));
        while( $row = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $classcdD008[] = $row["NAMECD2"];
        }
        $results->free();
        
        for($i=0;$i<$model->field["COUNT"];$i++){

            $data = array();
            $val = explode("-", $model->fields["SCHREGNO"][$i]);

            //履修／修得単位を一旦セットする
            $data["COMP_CREDIT"][NUMBER] = $model->fields["COMP_CREDIT"][$i];
            $data["GET_CREDIT"][NUMBER]  = $model->fields["GET_CREDIT"][$i];

            /* 履修単位・修得単位の自動計算処理 始め */

            //$notice_val[0]:欠課数(画面表示)
            //$notice_val[1]:単位数
            //$notice_val[2]:欠課数上限値
            //$notice_val[3]:半期認定フラグ
            $notice_val = explode("-", $model->fields["NOTICE_LATE"][$i]);

            //$combined_val[0]:合併元科目の履修単位の合計
            //$combined_val[1]:合併元科目の修得単位の合計
            //$combined_val[2]:合併元科目の修得単位の合計「名称予備１＝Y」の場合
            //$combined_val[3]:合併元科目のレコード数
            //$combined_val[4]:単位数が登録されている合併元科目のレコード数
            $combined_val = explode("-", $model->fields["COMBINED_CREDIT_SUM"][$i]);

            // 履修単位・修得単位が入力可で、学年評定が入力されていて、履修単位・修得単位がNULLの時
            if (isset($admin_key["0882"]) && 
                ($model->fields["GRAD_VALUE"][$i] != "" || $model->fields["GRAD_VALUE_DI"][$i] != "") && 
                ($model->fields["COMP_CREDIT"][$i] == "" && $model->fields["GET_CREDIT"][$i] == ""))
            {
                // 合併先科目である
                if (isset($model->creditflg)) {
                    // $model->creditflg ---> 1:単位固定タイプ 2:単位加算タイプ
                    // 学年評定:２以上、履修・修得に登録
                    if ($model->fields["GRAD_VALUE"][$i] > "1") {
                        $getCredit = ($model->repGetCredit == "Y") ? $combined_val[2] : $combined_val[1];
                        $data["COMP_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                        $data["GET_CREDIT"][NUMBER]  = ($model->creditflg == "2") ? $getCredit : $notice_val[1];
                    }
                    // 学年評定:１、履修のみに登録
                    else if ($model->fields["GRAD_VALUE"][$i] == "1") {
                        $data["COMP_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                        $data["GET_CREDIT"][NUMBER]  = 0;
                    }

                // 合併先科目でない--->今までと同じ計算処理をする
                } else {
                    //単位数・学年評定がある場合のみ処理
                    if ($notice_val[1] != "" && ($model->fields["GRAD_VALUE"][$i] != "" || $model->fields["GRAD_VALUE_DI"][$i] != "")) {
                        $hanki_credit = $notice_val[1] / 2;
                        //①学年評定:２以上、欠課数上限値オーバー
                        if ($model->fields["GRAD_VALUE"][$i] > "1" && $notice_val[0] > $notice_val[2]) {
                            //半期認定科目
                            if ($notice_val[3] == "1") {
                                $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                                $data["GET_CREDIT"][NUMBER]  = floor($hanki_credit);
                                if ($model->fields["SEM1_VALUE"][$i] != "") $data["COMP_TAKESEMES"][TEXT] = "1";
                                if ($model->fields["SEM2_VALUE"][$i] != "") $data["COMP_TAKESEMES"][TEXT] = "2";
                            } else {
                                $data["COMP_CREDIT"][NUMBER] = $notice_val[1];//NO007
                                $data["GET_CREDIT"][NUMBER]  = $notice_val[1];//NO007
                                $data["COMP_TAKESEMES"][TEXT] = "";
                            }
                        }
                        //②学年評定:２以上、欠課数上限値内
                        elseif ($model->fields["GRAD_VALUE"][$i] > "1" && $notice_val[0] <= $notice_val[2]) {
                                $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                                $data["GET_CREDIT"][NUMBER]  = $notice_val[1];
                                $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                        //③学年評定:１、欠課数上限値オーバー
                        elseif ($model->fields["GRAD_VALUE"][$i] == "1" && $notice_val[0] > $notice_val[2]) {
                            //半期認定科目
                            if ($notice_val[3] == "1") {
                                $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                                $data["GET_CREDIT"][NUMBER]  = 0;
                                if ($model->fields["SEM1_VALUE"][$i] != "") $data["COMP_TAKESEMES"][TEXT] = "1";
                                if ($model->fields["SEM2_VALUE"][$i] != "") $data["COMP_TAKESEMES"][TEXT] = "2";
                            } else {
                                $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                                $data["GET_CREDIT"][NUMBER]  = 0;
                                $data["COMP_TAKESEMES"][TEXT] = "";
                            }
                        }
                        //④学年評定:１、欠課数上限値内
                        elseif ($model->fields["GRAD_VALUE"][$i] == "1" && $notice_val[0] <= $notice_val[2]) {
                                $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                                $data["GET_CREDIT"][NUMBER]  = 0;
                                $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                        //⑤学年評定:'-''='、欠課数上限値オーバー
                        elseif ($model->fields["GRAD_VALUE_DI"][$i] != "" && $notice_val[0] > $notice_val[2]) {
                                $data["COMP_CREDIT"][NUMBER] = 0;
                                $data["GET_CREDIT"][NUMBER]  = 0;
                                $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                        //⑥その他---NO006
                        else {
                                $data["COMP_CREDIT"][NUMBER] = "";
                                $data["GET_CREDIT"][NUMBER]  = "";
                                $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    }
                }
            }
            // 履修単位・修得単位が入力可で、学年評定が入力されていない
            elseif (isset($admin_key["0882"]) && $model->fields["GRAD_VALUE"][$i] == "") {
                // 「評定を指定しない科目」の合併先科目である
                $classcd = substr($model->field["SUBCLASSCD"], 0, 2);
                if (isset($model->creditflg) && (in_array($classcd, $classcdD008) || $classcd == "90")) {
                    // 1:単位固定タイプ
                    // ・RECORD_DATに合併元科目のレコードが「全て」存在している
                    // ・そのレコードの中に単位数が登録されているレコードが「最低でも１件」存在している
                    if ($model->creditflg == "1" && $model->motoCnt == $combined_val[3] && 0 < $combined_val[4]) {
                        // 単位マスタの単位数
                        $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                        $data["GET_CREDIT"][NUMBER]  = $notice_val[1];
                    }
                    // 2:単位加算タイプ
                    // ・RECORD_DATに合併元科目のレコードが「最低でも１件」存在している
                    // ・そのレコードの中に単位数が登録されているレコードが「最低でも１件」存在している
                    if ($model->creditflg == "2" && 0 < $combined_val[3] && 0 < $combined_val[4]) {
                        // 合併元科目の単位数の合計
                        $data["COMP_CREDIT"][NUMBER] = $combined_val[0];
                        $data["GET_CREDIT"][NUMBER]  = $combined_val[1];
                    }
                }
            }

            /* 履修単位・修得単位の自動計算処理 終わり */

            //講座情報 NO001Add*
            if ($model->testcd == "10101") $data["SEM1_INTR_CHAIRCD"][TEXT] = $model->field["CHAIRCD"];//前期－中間
            if ($model->testcd == "10201") $data["SEM1_TERM_CHAIRCD"][TEXT] = $model->field["CHAIRCD"];//前期－期末
            if ($model->testcd == "20101") $data["SEM2_INTR_CHAIRCD"][TEXT] = $model->field["CHAIRCD"];//後期－中間
            if ($model->testcd == "20201") $data["SEM2_TERM_CHAIRCD"][TEXT] = $model->field["CHAIRCD"];//後期－期末1
            if ($model->testcd == "20202") $data["SEM2_TERM2_CHAIRCD"][TEXT] = $model->field["CHAIRCD"];//後期－期末2

            //出欠情報
            $data["SEM1_INTR_VALUE_DI"][TEXT] = $model->fields["SEM1_INTR_VALUE_DI"][$i];
            $data["SEM1_TERM_VALUE_DI"][TEXT] = $model->fields["SEM1_TERM_VALUE_DI"][$i];
            $data["SEM1_VALUE_DI"][TEXT]      = $model->fields["SEM1_VALUE_DI"][$i];
            $data["SEM2_INTR_VALUE_DI"][TEXT] = $model->fields["SEM2_INTR_VALUE_DI"][$i];
            $data["SEM2_TERM_VALUE_DI"][TEXT] = $model->fields["SEM2_TERM_VALUE_DI"][$i];
            $data["SEM2_TERM2_VALUE_DI"][TEXT] = $model->fields["SEM2_TERM2_VALUE_DI"][$i];//Modify
            $data["SEM2_VALUE_DI"][TEXT]      = $model->fields["SEM2_VALUE_DI"][$i];
            $data["GRAD_VALUE_DI"][TEXT]      = $model->fields["GRAD_VALUE_DI"][$i];

            //前期
            $data["SEM1_INTR_SCORE"][NUMBER]  = $model->fields["SEM1_INTR_SCORE"][$i];
            $data["SEM1_INTR_VALUE"][NUMBER]  = $model->fields["SEM1_INTR_VALUE"][$i];
            $data["SEM1_TERM_SCORE"][NUMBER]  = $model->fields["SEM1_TERM_SCORE"][$i];
            $data["SEM1_TERM_VALUE"][NUMBER]  = $model->fields["SEM1_TERM_VALUE"][$i];
            $data["SEM1_VALUE"][NUMBER]       = $model->fields["SEM1_VALUE"][$i];     

            //後期
            $data["SEM2_INTR_SCORE"][NUMBER]  = $model->fields["SEM2_INTR_SCORE"][$i];
            $data["SEM2_INTR_VALUE"][NUMBER]  = $model->fields["SEM2_INTR_VALUE"][$i];
            $data["SEM2_TERM_SCORE"][NUMBER]  = $model->fields["SEM2_TERM_SCORE"][$i];
            $data["SEM2_TERM_VALUE"][NUMBER]  = $model->fields["SEM2_TERM_VALUE"][$i];
            $data["SEM2_TERM2_SCORE"][NUMBER] = $model->fields["SEM2_TERM2_SCORE"][$i];//Modify
            $data["SEM2_TERM2_VALUE"][NUMBER] = $model->fields["SEM2_TERM2_VALUE"][$i];//Modify
            $data["SEM2_VALUE"][NUMBER]       = $model->fields["SEM2_VALUE"][$i];     

            $data["GRAD_VALUE"][NUMBER]       = $model->fields["GRAD_VALUE"][$i];

            $data["REGISTERCD"][TEXT]        = STAFFCD ;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE YEAR           = '".CTRL_YEAR."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "     CLASSCD          = '".$substr_classcd."' AND ";
                $where .= "     SCHOOL_KIND      = '".$substr_school_kind."' AND ";
                $where .= "     CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
                $where .= "     SUBCLASSCD       = '".$substr_subclasscd."' AND ";
            } else {
                $where .= "     SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' AND ";
            }
            $where .= "       SCHREGNO       = '".$val[0]."' AND ";
            $where .= "       TAKESEMES      = '".$val[1]."' ";

            $query = Query::updateSQL($data, "RECORD_DAT",$where);
            $db->query($query);
            
            //評定フラグ・・・仮評定(1)、本評定(NULL)
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                $query = knjd122tQuery::delRecordProvFlgDat(CTRL_YEAR, $model->field["SUBCLASSCD"], $val[0], $model);
                $db->query($query);
                if ($model->fields["PROV_FLG"][$i] == '1') {
                    $query = knjd122tQuery::insRecordProvFlgDat(CTRL_YEAR, $model->field["SUBCLASSCD"], $val[0], $model);
                    $db->query($query);
                }
            }
        }

        Query::dbCheckIn($db);

    }

    //テーブルRECORD_PROV_FLG_DATのレコードを削除
    function delRecordProvFlgDat($year, $subclass, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclass, 0, 2);
            $substr_school_kind     = substr($subclass, 3, 1);
            $substr_curriculum_cd   = substr($subclass, 5, 1);
            $substr_subclasscd      = substr($subclass, 7, 6);
        }
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_PROV_FLG_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  CLASSCD       = '{$substr_classcd}' ";
            $query .= "  AND  SCHOOL_KIND   = '{$substr_school_kind}' ";
            $query .= "  AND  CURRICULUM_CD = '{$substr_curriculum_cd}' ";
            $query .= "  AND  SUBCLASSCD    = '{$substr_subclasscd}' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '{$subclass}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    function insRecordProvFlgDat($year, $subclass, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclass, 0, 2);
            $substr_school_kind     = substr($subclass, 3, 1);
            $substr_curriculum_cd   = substr($subclass, 5, 1);
            $substr_subclasscd      = substr($subclass, 7, 6);
        }
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]   = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT] = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]    = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]    = $subclass;
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        //評定フラグ・・・仮評定(1)、本評定(NULL)
        $data["PROV_FLG"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }

    // "成績入力完了"処理 //

    function updateRecordChkfin($model, $recordDiv) {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();
        if ($recordDiv == "1") {
            $testcdArray = array("0111" => "10101"
                                ,"0121" => "10201"
                                ,"0211" => "20101"
                                ,"0221" => "20201"
                                ,"0231" => "20202");
        } else {
            $testcdArray = array("0112" => "10101"
                                ,"0122" => "10201"
                                ,"0182" => "19900"
                                ,"0212" => "20101"
                                ,"0222" => "20201"
                                ,"0232" => "20202"
                                ,"0282" => "29900"
                                ,"0882" => "99900");
        }

        //管理者コントロール
        $query = knjd122tQuery::selectContolCodeQuery($model);
        $results = $db->query($query);
        while( $row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]] = $row_ct["CONTROL_CODE"];
        }
        $results->free();
        //初期化
        $j = 1;
        $strChkComp = ($recordDiv == "1") ? "CHK_COMP" : "CHK_COMP_VALUE";
        foreach ($testcdArray as $testkey => $testval) {
            $query = knjd122tQuery::getRecordChkfinDat($model, $testval, $recordDiv);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow) && isset($admin_key[$testkey])) {
                $chkfg = ($model->field[$strChkComp.$j] == 'on') ? '1' : '0';

                $data = array();
                $data["EXECUTED"][TEXT]   = $chkfg ;     //成績入力区分
                $data["REGISTERCD"][TEXT] = STAFFCD ;    //登録者コード
                $data["UPDATED"][FUNC]    = "sysdate()"; //更新日付

                $where  = " WHERE ";
                $where .= "         YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$testval}' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     AND CLASSCD          = '".$substr_classcd."' ";
                    $where .= "     AND SCHOOL_KIND      = '".$substr_school_kind."' ";
                    $where .= "     AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                    $where .= "     AND SUBCLASSCD       = '".$substr_subclasscd."' ";
                } else {
                    $where .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";
                $where .= "     AND RECORD_DIV = '{$recordDiv}' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_DAT", $where);

                $db->query($query);
            }
            $j++;
        }
        Query::dbCheckIn($db);
    }

    function updateRecordSchChrTest($model) {
        $db = Query::dbCheckOut();
        $testcdArray = array("0111" => "10101"
                            ,"0121" => "10201"
                            ,"0211" => "20101"
                            ,"0221" => "20201"
                            ,"0231" => "20202");

        //管理者コントロール
        $query = knjd122tQuery::selectContolCodeQuery($model);
        $results = $db->query($query);
        while( $row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]] = $row_ct["CONTROL_CODE"];
        }
        $results->free();
        //初期化
        $j = 1;
        foreach ($testcdArray as $testkey => $testval) {
            $query = knjd122tQuery::getRecordSchChrTest($model, $testval);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow) && isset($admin_key[$testkey])) {
                $chkfg = ($model->field["CHK_COMP".$j] == 'on') ? '1' : '0';

                $data = array();
                $data["EXECUTED"][TEXT]   = $chkfg ;     //成績入力区分
                $data["REGISTERCD"][TEXT] = STAFFCD ;    //登録者コード
                $data["UPDATED"][FUNC]    = "sysdate()"; //更新日付

                $where  = " WHERE ";
                $where .= "         YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$testval}' ";
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

                $query = Query::updateSQL($data, "SCH_CHR_TEST", $where);

                $db->query($query);
            }
            $j++;
        }
        Query::dbCheckIn($db);
    }

    //未入力チェックのテーブルにレコードがあるのか
    function getRecordChkfinDat($model, $testcd, $recordDiv) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD          = '".$substr_classcd."' ";
            $query .= "     AND SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "     AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "     AND SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";
        $query .= "     AND RECORD_DIV = '{$recordDiv}' ";

        return $query;
    }

    //未入力チェックのテーブルにレコードがあるのか
    function getRecordSchChrTest($model, $testcd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' ";
        $query .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

        return $query;
    }

    // "成績入力完了"処理 おわり //
}
?>
