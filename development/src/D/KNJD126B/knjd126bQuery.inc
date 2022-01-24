<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd126bQuery.inc 56581 2017-10-22 12:37:16Z maeshiro $

class knjd126bquery extends Query
{

    //編集可能学期の判別
    public function selectContolCodeQuery()
    {
        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     CONTROL_FLG = '1' ";
        return $query;
    }

    //時間割講座テストより試験日を抽出
    public function selectExecuteDateQuery($model, $semester, $testkindcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     TESTKINDCD = '".$testkindcd."' AND ";
        $query .= "     CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= " ORDER BY EXECUTEDATE ";
        return $query;
    }

    //科目データの取得
    public function selectSubclassQuery($model)
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
        $query .= "     T3.subclasscd NOT LIKE '9%' AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= " t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD  AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND  AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD  AND ";
        }
        $query .= "     T3.subclasscd = T1.subclasscd ";
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
    public function selectChairQuery($model)
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
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= " t3.STAFFCD = '".STAFFCD."' AND ";
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
    public function getScAbsentCov()
    {
        return "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '".CTRL_YEAR."'";
    }

    //累積最大日取得
    public function getMax($subclasscd, $model)
    {
        $query  = "SELECT DISTINCT year ";
        $query .= "              ,month ";
        $query .= "              ,CASE MONTH WHEN '01' THEN CHAR(SMALLINT(month) + 12)  ";
        $query .= "                          WHEN '02' THEN CHAR(SMALLINT(month) + 12)  ";
        $query .= "                          WHEN '03' THEN CHAR(SMALLINT(month) + 12) ELSE month END AS tmp_month ";
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
    public function getAssessMark()
    {
        $query  = "SELECT  ASSESSCD, ASSESSLEVEL, ASSESSMARK ";
        $query .= "FROM    ASSESS_MST ";
        $query .= "WHERE   ASSESSCD in('2','3') ";
        $query .= "ORDER BY  ASSESSCD, ASSESSLEVEL ";
        return $query;
    }

    //合併先科目の単位固定／加算フラグ
    public function getCalculateCreditFlg($subclasscd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "SELECT DISTINCT CALCULATE_CREDIT_FLG ";
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  COMBINED_CLASSCD        = '".$substr_classcd."' ";
            $query .= "  AND  COMBINED_SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "  AND  COMBINED_CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "  AND  COMBINED_SUBCLASSCD     = '".$substr_subclasscd."' ";
        } else {
            $query .= "  AND  COMBINED_SUBCLASSCD = '".$subclasscd."' ";
        }
        return $query;
    }

    //実技科目の判断フラグ
    public function getJitugiFlg($subclasscd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        if ($model->Properties["useClassDetailDat"] == "1") {
            $query  = "";
            $query .= " SELECT ";
            $query .= "     COUNT(*) FLG ";
            $query .= " FROM ";
            $query .= "     SUBCLASS_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND CLASSCD        = '".$substr_classcd."' ";
            $query .= "     AND SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "     AND CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "     AND SUBCLASSCD     = '".$substr_subclasscd."' ";
            $query .= "     AND SUBCLASS_SEQ = '001' ";
        } else {
            $query  = "SELECT COUNT(*) FLG ";
            $query .= "FROM   V_NAME_MST ";
            $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
            $query .= "  AND  NAMECD1 = 'D004' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "  AND  NAME1 = '".$substr_subclasscd."' ";
            } else {
                $query .= "  AND  NAME1 = '".$subclasscd."' ";
            }
        }


        return $query;
    }

    //調整点基準点の取得
    public function getSubclassAdjustValue($subclasscd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "SELECT  SEMESTER, GRADE, VALUE ";
        $query .= "FROM    SUBCLASS_ADJUST_DAT ";
        $query .= "WHERE   YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND   CLASSCD        = '".$substr_classcd."' ";
            $query .= "  AND   SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "  AND   CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "  AND   SUBCLASSCD     = '".$substr_subclasscd."' ";
        } else {
            $query .= "  AND   SUBCLASSCD = '".$subclasscd."' ";
        }
        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
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
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '1' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO, ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '1' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '2' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO, ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '2' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '2' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO, ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '2' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '3' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO, ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '3' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "        )  ";
        //定期考査の出欠の表２
        $query .= " ,TEST_ATTEND_B AS ( ";
        $query .= "     SELECT  SCHREGNO,  ";
        $query .= "             MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SCORE1111_ATTEND, ";
        $query .= "             MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SCORE1211_ATTEND, ";
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SCORE2111_ATTEND, ";
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SCORE2211_ATTEND, ";
        $query .= "             MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SCORE3211_ATTEND  ";
        $query .= "     FROM    TEST_ATTEND_A  ";
        $query .= "     GROUP BY SCHREGNO  ";
        $query .= "     ) ";
        //成績データの表
        $query .= " ,RECORD_SCORE AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '10101' THEN SCORE END) AS SCORE1111 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '10201' THEN SCORE END) AS SCORE1211 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '19901' THEN SCORE END) AS SCORE1901 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '19900' THEN VALUE END) AS VALUE1900 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '20101' THEN SCORE END) AS SCORE2111 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '20201' THEN SCORE END) AS SCORE2211 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '29901' THEN SCORE END) AS SCORE2901 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '29900' THEN VALUE END) AS VALUE2900 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '30201' THEN SCORE END) AS SCORE3211 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '39901' THEN SCORE END) AS SCORE3901 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '39900' THEN VALUE END) AS VALUE3900 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '99900' THEN VALUE END) AS GRAD_VALUE ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '99900' THEN GET_CREDIT END) AS GET_CREDIT ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '99900' THEN COMP_CREDIT END) AS COMP_CREDIT ";
        $query .= "     FROM   RECORD_SCORE_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD          = '".$substr_classcd."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "       AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "       AND  SCORE_DIV IN('00','01') ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     ) ";
        //見込点データの表
        $query .= " ,SUPP_EXA AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10101' THEN SCORE_PASS END) AS SCORE1111_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10201' THEN SCORE_PASS END) AS SCORE1211_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20101' THEN SCORE_PASS END) AS SCORE2111_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20201' THEN SCORE_PASS END) AS SCORE2211_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '30201' THEN SCORE_PASS END) AS SCORE3211_PASS ";
        $query .= "     FROM   SUPP_EXA_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD          = '".$substr_classcd."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "       AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "       AND  SCORE_FLG = '2' ";
        $query .= "     GROUP BY SCHREGNO ";
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

        $query .= " SELECT W1.schregno";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "       ,P0.PROV_FLG";
        }
        $query .= "       ,W0.SCORE1111 ,W0.SCORE1211 ,W0.SCORE1901 ,W0.VALUE1900 ";
        $query .= "       ,W0.SCORE2111 ,W0.SCORE2211 ,W0.SCORE2901 ,W0.VALUE2900 ";
        $query .= "                     ,W0.SCORE3211 ,W0.SCORE3901 ,W0.VALUE3900 ";
        $query .= "       ,W0.GRAD_VALUE ,W0.GET_CREDIT ,W0.COMP_CREDIT ";
        $query .= "       ,W4.SCORE1111_PASS,W4.SCORE1211_PASS,W4.SCORE2111_PASS,W4.SCORE2211_PASS,W4.SCORE3211_PASS";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W5.chair_sem1 ,W6.chair_sem2 ,W10.chair_sem3 ";
        $query .= "       ,W7.SCORE1111_ATTEND,W7.SCORE1211_ATTEND ";
        $query .= "       ,W7.SCORE2111_ATTEND,W7.SCORE2211_ATTEND ";
        $query .= "       ,W7.SCORE3211_ATTEND ";
        if ($model->isJitu) {
            $query .= "   ,value(L1.COMP_ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        } else {
            $query .= "   ,value(W8.ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        }
        $query .= "       ,W8.CREDITS,W8.AUTHORIZE_FLG ";
        $query .= "       ,W9.COMP_CREDIT_SUM,W9.GET_CREDIT_SUM ";
        $query .= "   FROM ";
        $query .= "    (SELECT schregno ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd = '".$model->field["CHAIRCD"]."'  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
        $query .= "    ) W1 ";
        /* 成績情報 */
        $query .= " LEFT OUTER JOIN RECORD_SCORE W0 ON W0.SCHREGNO = W1.schregno ";
        /* 仮評定情報 */
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " LEFT JOIN RECORD_PROV_FLG P0 ON P0.SCHREGNO = W1.SCHREGNO ";
        }
        /* 追試・再試情報 */
        $query .= " LEFT OUTER JOIN SUPP_EXA W4 ON W4.SCHREGNO = W1.schregno ";
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
        $query .= "   ON W1.schregno = W2.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "        AND T1.transfercd not in ('3') "; //「3:出停」は除く。※現象：インフルエンザで停止になった生徒が、現在黄色になっている。
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 在籍情報(１学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem1";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '1') W5";
        $query .= "   ON W5.schregno = W1.schregno ";
        /* 在籍情報(２学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem2";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '2') W6";
        $query .= "   ON W6.schregno = W1.schregno ";
        /* 在籍情報(３学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem3";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '3') W10";
        $query .= "   ON W10.schregno = W1.schregno ";
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

        /* 合併先科目の単位を抽出（2:加算タイプ）*/
        /* 合併先科目の単位は、生徒が選択した合併元科目の単位数を加算した値である */
        $query .= " LEFT JOIN (";
        $query .= "SELECT T2.SCHREGNO, ";
        $query .= "       SUM(T2.COMP_CREDIT) AS COMP_CREDIT_SUM, ";
        $query .= "       SUM(T2.GET_CREDIT) AS GET_CREDIT_SUM ";
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "       RECORD_SCORE_DAT T2 ";
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
        $query .= "  AND  T1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= "  AND  T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  T2.SEMESTER = '9' ";//学年末
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  T2.CLASSCD       = T1.ATTEND_CLASSCD ";
            $query .= "       AND  T2.SCHOOL_KIND   = T1.ATTEND_SCHOOL_KIND ";
            $query .= "       AND  T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
            $query .= "       AND  T2.SUBCLASSCD    = T1.ATTEND_SUBCLASSCD ";
        } else {
            $query .= "       AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        }
        $query .= "  AND (T2.COMP_CREDIT IS NOT NULL OR T2.GET_CREDIT IS NOT NULL) ";
        $query .= "GROUP BY T2.SCHREGNO ";
        $query .= ") W9 ON W9.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    // 学校詳細マスタ
    public function getSchoolMst()
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
    public function getPerfect($year, $subclasscd, $testcd, $grade, $course, $model)
    {
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

    //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
    public function getOffdaysFlg($year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUB_OFFDAYS,SUB_ABSENT,SUB_SUSPEND,SUB_MOURNING,SUB_VIRUS,SUB_KOUDOME ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$year}' ";
        return $query;
    }

    public function getAttendData($chaircd, $subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg, $model)
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
        if ($offdaysFlg["SUB_OFFDAYS"] == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($offdaysFlg["SUB_ABSENT"] == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($offdaysFlg["SUB_SUSPEND"] == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($offdaysFlg["SUB_VIRUS"] == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_KOUDOME"] == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query  = "SELECT schregno ";
        $query .= "       ,".$notice."  AS T_NOTICE ";
        $query .= "       ,".$late_early."     AS T_LATEEARLY ";
        //小数点ありAdd
        if ($absent_cov == "4" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "   ,".$notice."  AS NOTICE_LATE ";
        }
        $query .= "  FROM attend_subclass_dat T1 ";
        $query .= " WHERE COPYCD        = '0' ";
        $query .= "   AND T1.year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND T1.CLASSCD          = '".$substr_classcd."' ";
            $query .= "   AND T1.SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "   AND T1.CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "   AND T1.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "   AND T1.subclasscd = '".$subclasscd."' ";
        }
        $query .= "   AND EXISTS (SELECT 'X' FROM chair_std_dat T2 ";
        $query .= "                WHERE T2.year    = '".CTRL_YEAR."'";
        $query .= "                  AND T2.chaircd = '".$chaircd."'";
        $query .= "                  AND T2.schregno = T1.schregno) ";
        $query .= " GROUP BY schregno";
        return $query;
    }

    //ABSENT_COVが１(学期ごとに集計）する場合
    public function getAttendData2($chaircd, $subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg, $model)
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
        if ($offdaysFlg["SUB_OFFDAYS"] == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($offdaysFlg["SUB_ABSENT"] == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($offdaysFlg["SUB_SUSPEND"] == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($offdaysFlg["SUB_VIRUS"] == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_KOUDOME"] == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno";
        $query .= "               ,T1.semester ";
        $query .= "               ,".$notice." AS T_NOTICE ";
        $query .= "               ,".$late_early."    AS T_LATEEARLY ";
        //小数点ありAdd
        if ($absent_cov == "3" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "1" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
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

    //テーブルSchChrTestのEXECUTEDデータ更新SQL
    public function updateExecuted($model)
    {
        $db = Query::dbCheckOut();

        $chkDbValue = array( "1" => array("TESTKINDCD" => "01", "TESTITEMCD" => "01","SEMETER" => "1") ,
                             "2" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "1") ,
                             "3" => array("TESTKINDCD" => "01", "TESTITEMCD" => "01","SEMETER" => "2") ,
                             "4" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "2") ,
                             "5" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "3") );

        //管理者コントロール
        $query = knjd126bQuery::selectContolCodeQuery();
        $results = $db->query($query);
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]]=$row_ct["CONTROL_CODE"];
        }
        $results->free();

        //disable flag
        $ableflg[1] = (isset($admin_key["0111"]))? "" : "disabled" ;
        $ableflg[2] = (isset($admin_key["0121"]))? "" : "disabled" ;
        $ableflg[3] = (isset($admin_key["0211"]))? "" : "disabled" ;
        $ableflg[4] = (isset($admin_key["0221"]))? "" : "disabled" ;
        $ableflg[5] = (isset($admin_key["0321"]))? "" : "disabled" ;

        for ($i=1; $i<=5; $i++) {
            if ($ableflg[$i] == "disabled") {
                continue;
            }

            $chkfg = ($model->field["CHK_COMP".$i] == "on")? "1" : "0" ;

            $data = array();
            $data["EXECUTED"][TEXT]     = $chkfg ;       //成績入力区分
            $data["REGISTERCD"][TEXT]   = STAFFCD ;      //登録者コード
            $data["UPDATED"][FUNC]      = "sysdate()";   //更新日付

            $where  = "";
            $where  = " WHERE YEAR       = '".CTRL_YEAR."' AND ";
            $where .= "       TESTKINDCD = '".$chkDbValue[$i]["TESTKINDCD"]."' AND ";
            $where .= "       TESTITEMCD = '".$chkDbValue[$i]["TESTITEMCD"]."' AND ";
            $where .= "       CHAIRCD    = '".$model->field["CHAIRCD"]."' AND ";
            $where .= "       SEMESTER   = '".$chkDbValue[$i]["SEMETER"]."' ";

            $query = "";
            $query = Query::updateSQL($data, "SCH_CHR_TEST", $where);

            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    public function updateExecuted2($model)
    {
        $db = Query::dbCheckOut();
        $testcdArray = array("0182" => "19900"
                            ,"0282" => "29900"
                            ,"0382" => "39900"
                            ,"0882" => "99900");

        //管理者コントロール
        $query = knjd126bQuery::selectContolCodeQuery();
        $results = $db->query($query);
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]] = $row_ct["CONTROL_CODE"];
        }
        $results->free();
        //初期化
        $j = 1;
        foreach ($testcdArray as $testkey => $testval) {
            if (!$model->valueSmesterFlg[substr($testval, 0, 1)] && substr($testval, 0, 1) != '9') {
                $model->field["CHK_COMP_VALUE".$j] = '';
            }
            $query = knjd126bQuery::getRecordChkfinDat($model, $testval);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow) && isset($admin_key[$testkey])) {
                $chkfg = ($model->field["CHK_COMP_VALUE".$j] == 'on') ? '1' : '0';

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
                $where .= "     AND RECORD_DIV = '2' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_DAT", $where);

                $db->query($query);
            }
            $j++;
        }
        Query::dbCheckIn($db);
    }

    //テーブルSchChrTestのデータ抽出SQL
    public function getSchChrTestData($charcd)
    {
        $query = "
            SELECT SEMESTER || TESTKINDCD || TESTITEMCD as TESTKIND, EXECUTED FROM SCH_CHR_TEST
               WHERE CHAIRCD = '".$charcd."'
               AND YEAR = '".CTRL_YEAR."'
               AND ((TESTKINDCD = '01' AND TESTITEMCD = '01') OR (TESTKINDCD = '02' AND TESTITEMCD = '01') )
        ";

        return $query;
    }


    //テーブルRECORD_SCORE_DATの存在チェック用SQL
    public function existRecordNew($year, $sem, $kind, $item, $div, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "";
        $query .= "SELECT COUNT(*) ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '$year' ";
        $query .= "  AND  SEMESTER      = '$sem' ";
        $query .= "  AND  TESTKINDCD    = '$kind' ";
        $query .= "  AND  TESTITEMCD    = '$item' ";
        $query .= "  AND  SCORE_DIV     = '$div' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD          = '".$substr_classcd."' ";
            $query .= "     AND SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "     AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "     AND SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     AND  SUBCLASSCD    = '$subclass' ";
        }
        $query .= "  AND  SCHREGNO      = '$schno' ";

        return $query;
    }


    //追加および更新
    public function insertUpdateRecordNew($year, $sem, $kind, $item, $div, $model, $schno, $score, $db)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        $data["SCORE_DIV"][TEXT]    = $div;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]    = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT]  = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]     = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        if ($model->testcd == $sem.$kind.$item) {
            $data["CHAIRCD"][TEXT]  = $model->field["CHAIRCD"];
        } elseif ($div == "00") {
            $data["CHAIRCD"][TEXT]  = "";
        }
        $data["SCORE"][NUMBER]  = ($div == "01") ? $score : "";
        $data["VALUE"][NUMBER]  = ($div == "00") ? $score : "";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        //追加および更新を判定
        $query = knjd126bQuery::existRecordNew($year, $sem, $kind, $item, $div, $model->field["SUBCLASSCD"], $schno, $model);
        $existflg = $db->getOne($query);
        if (0 < $existflg) {
            $where  = "WHERE  YEAR          = '$year' ";
            $where .= "  AND  SEMESTER      = '$sem' ";
            $where .= "  AND  TESTKINDCD    = '$kind' ";
            $where .= "  AND  TESTITEMCD    = '$item' ";
            $where .= "  AND  SCORE_DIV     = '$div' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "  AND  CLASSCD          = '".$substr_classcd."' ";
                $where .= "  AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
                $where .= "  AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                $where .= "  AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
            } else {
                $where .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
            }
            $where .= "  AND  SCHREGNO      = '$schno' ";
            $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        }

        $db->query($query);
    }


    //成績が全てNULLの場合、レコードを削除する（つまり、レコードを作成しない）
    public function deleteRecordNew($year, $sem, $kind, $item, $div, $model, $schno, $db)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($subclass, 0, 2);
            $substr_school_kind      = substr($subclass, 3, 1);
            $substr_curriculum_cd    = substr($subclass, 5, 1);
            $substr_subclasscd       = substr($subclass, 7, 6);
        }
        //削除
        $query  = "DELETE ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '$year' ";
        $query .= "  AND  SEMESTER      = '$sem' ";
        $query .= "  AND  TESTKINDCD    = '$kind' ";
        $query .= "  AND  TESTITEMCD    = '$item' ";
        $query .= "  AND  SCORE_DIV     = '$div' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND CLASSCD          = '".$substr_classcd."' ";
            $query .= "  AND SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "  AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "  AND SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "  AND  SCHREGNO      = '$schno' ";
        $db->query($query);
    }


    //見込点の追加・・・公欠、出停・忌引、病欠・欠課
    public function insertSuppExaDat($year, $sem, $kind, $item, $model, $schno, $score, $db)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($subclass, 0, 2);
            $substr_school_kind      = substr($subclass, 3, 1);
            $substr_curriculum_cd    = substr($subclass, 5, 1);
            $substr_subclasscd       = substr($subclass, 7, 6);
        }
        //削除
        $query  = "DELETE ";
        $query .= "FROM   SUPP_EXA_DAT ";
        $query .= "WHERE  YEAR          = '$year' ";
        $query .= "  AND  SEMESTER      = '$sem' ";
        $query .= "  AND  TESTKINDCD    = '$kind' ";
        $query .= "  AND  TESTITEMCD    = '$item' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  CLASSCD          = '".$substr_classcd."' ";
            $query .= "  AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "  AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "  AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "  AND  SCHREGNO      = '$schno' ";
        $db->query($query);
        //追加
        if (strlen($score)) {
            $data = array();
            $data["YEAR"][TEXT]         = $year;
            $data["SEMESTER"][TEXT]     = $sem;
            $data["TESTKINDCD"][TEXT]   = $kind;
            $data["TESTITEMCD"][TEXT]   = $item;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $substr_classcd;
                $data["SCHOOL_KIND"][TEXT]    = $substr_school_kind;
                $data["CURRICULUM_CD"][TEXT]  = $substr_curriculum_cd;
                $data["SUBCLASSCD"][TEXT]     = $substr_subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
            }
            $data["SCHREGNO"][TEXT]     = $schno;
            $data["SCORE"][NUMBER]      = $score;
            $data["SCORE_PASS"][NUMBER] = $score;
            $data["SCORE_FLG"][TEXT]    = "2";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SUPP_EXA_DAT");
            $db->query($query);
        }
    }


    /* データ更新処理 */
    public function updateRecord(&$model)
    {
        $db = Query::dbCheckOut();

        //試験名称
        $ctrl_name = array("0111" => "SCORE1111"
                          ,"0121" => "SCORE1211"
                          ,"0181" => "SCORE1901"
                          ,"0211" => "SCORE2111"
                          ,"0221" => "SCORE2211"
                          ,"0281" => "SCORE2901"
                          ,"0321" => "SCORE3211"
                          ,"0381" => "SCORE3901");
        //学期評定
        $ctrl_seme = array("0182" => "VALUE1900"
                          ,"0282" => "VALUE2900"
                          ,"0382" => "VALUE3900");

        //管理者コントロール
        $admin_key = $admin_key_sem = array();
        $results = $db->query(knjd126bquery::selectContolCodeQuery());
        while ($row = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            //履修単位／修得単位の仮コードをセットする(0884はないので明示的にセット)
            if ($row["CONTROL_CODE"] == "0885") {
                $admin_key[] = "0884";
            }
            $admin_key[] = $row["CONTROL_CODE"];

            foreach ($ctrl_name as $code => $col) {
                if (in_array($code, $admin_key)) {
                    $admin_key_sem[substr($col, 5, 1)] = substr($col, 5, 1);
                }
            }
        }
        $results->free();

        for ($i = 0; $i < $model->field["COUNT"]; $i++) {
            $term_data = $term_data2 = $jitugi = array();
            $schno = $model->fields["SCHREGNO"][$i];
            $grade = $model->fields["GRADE"][$i];
            $value_flg = false;//学年評定更新フラグ

            //★素点---------->
            //各項目を作成
            foreach ($ctrl_name as $code => $col) {
                $sem   = substr($col, 5, 1);
                $kind  = (substr($col, 6, 1) != "9") ? sprintf("%02d", substr($col, 6, 1)) : "99";
                $item  = sprintf("%02d", substr($col, 7, 1));
                $div   = sprintf("%02d", substr($col, 8, 1));
                $score = $model->fields[$col][$i];

                if (strlen($score)) {
                    $term_data[$sem][] = ($sem == "3" && $kind == "02") ? ($score * 2) : $score;
                }//値を保持
                if ($kind == "02") {
                    $jitugi[$sem] = $score;
                }//期末欄の値を保持
                //ここで、管理者コントロールを見る-->(0111,0121,0181,0211,0221,0281,0321,0381)
                if (in_array($code, $admin_key)) {
                    //実技科目の場合、中間および調整点はレコードを作成しない（期末のみ処理する）
                    if ($model->jitugi_flg && $kind != "02") {
                        continue;
                    }
                    //見込点の追加・・・公欠、出停・忌引、病欠・欠課
                    if (strlen($model->fields[$col."_ATTEND"][$i]) && $kind != "99") {
                        knjd126bQuery::insertSuppExaDat(CTRL_YEAR, $sem, $kind, $item, $model, $schno, $score, $db);
                        $score = "";//成績データはNULLで更新
                    }
                    knjd126bQuery::insertUpdateRecordNew(CTRL_YEAR, $sem, $kind, $item, $div, $model, $schno, $score, $db);
                    //成績が全てNULLの場合、レコードを削除する（つまり、レコードを作成しない）
                    if (!$model->isScoreAllNull[$col]) {
                        knjd126bQuery::deleteRecordNew(CTRL_YEAR, $sem, $kind, $item, $div, $model, $schno, $db);
                    }
                }//if
            }//foreach

            //★学期末---------->
            //各項目を作成
            foreach ($ctrl_seme as $code => $col) {
                $sem   = substr($col, 5, 1);
                $kind  = (substr($col, 6, 1) != "9") ? sprintf("%02d", substr($col, 6, 1)) : "99";
                $item  = sprintf("%02d", substr($col, 7, 1));
                $div   = sprintf("%02d", substr($col, 8, 1));
                $term_data2[$sem] = $model->fields[$col][$i];//値を保持
                $adjust = $model->fields["ADJUST".$sem][$i];

                //ここで、管理者コントロールを見る-->(0182,0282,0382)
                //ここで、管理者コントロールを見る-->(0111,0121,0181,0211,0221,0281,0321,0381)
                if (in_array($code, $admin_key) || in_array($sem, $admin_key_sem)) {
                    //「学期評定」が入力可かつ自動計算しない場合、「手入力した値」をセット
                    if (in_array($code, $admin_key) && $model->value_cal_flg == "false") {
                        //上記以外は「自動計算した値」で更新する。
                    //実技科目・・・期末欄のみ入力のため、期末欄の値をセット
                    } elseif ($model->jitugi_flg) {
                        $term_data2[$sem] = $jitugi[$sem];
                    //普通科目・・・未入力欄のある生徒は、自動計算しない
                    } elseif (($sem < 3  &&get_count($term_data[$sem]) == 3) || ($sem == 3 &&get_count($term_data[$sem]) == 2)) {
                        $tmpVal = array_sum($term_data[$sem]) / (200 + $adjust) * 100;
                        $tmpSql = "values(smallint(round(".$tmpVal.",0)))";
                        $term_data2[$sem] = $db->getOne($tmpSql);
                    } else {
                        $term_data2[$sem] = "";
                    }
                    knjd126bQuery::insertUpdateRecordNew(CTRL_YEAR, $sem, $kind, $item, $div, $model, $schno, $term_data2[$sem], $db);
                    $value_flg = true;//学年評定更新フラグ

                    //自動計算の時はjavascriptで未入力チェックのチェックを行っていないため
                    //評定を更新する値が全てnullだった場合未入力チェックは更新しない
                    //そのためのフラグ
                    if ($term_data2[$sem]) {
                        $model->valueSmesterFlg[$sem] = true;
                    }
                }
            }//foreach

            //★学年末---------->
            $value = $model->fields["GRAD_VALUE"][$i];//値を保持
            //ここで、管理者コントロールを見る-->(0111,0121,0181,0211,0221,0281,0321,0381)
            if (in_array("0882", $admin_key) || $value_flg) {
                //「学年評定」が入力可かつNULLでない場合、「手入力した値」をセット
                if (in_array("0882", $admin_key) && strlen($value)) {
                    //上記以外は「自動計算した値」で更新する。
                //１～５学年
                } elseif ($grade < "06" && strlen($term_data2[1]) && strlen($term_data2[2]) && strlen($term_data2[3])) {
                    $value = round(array_sum($term_data2) / 3);
                //６学年
                } elseif ($grade == "06" && (strlen($term_data2[1]) || strlen($term_data2[2]))) {
                    if (strlen($term_data2[1]) && strlen($term_data2[2])) {
                        $value = round(($term_data2[1] + $term_data2[2]) / 2);
                    } elseif (strlen($term_data2[1])) {
                        $value = $term_data2[1];
                    } elseif (strlen($term_data2[2])) {
                        $value = $term_data2[2];
                    }
                } else {
                    $value = "";
                }
                knjd126bQuery::insertUpdateRecordNew(CTRL_YEAR, "9", "99", "00", "00", $model, $schno, $value, $db);
                
                //評定フラグ・・・仮評定(1)、本評定(NULL)
                //仮評定フラグ対応
                if ($model->Properties["useProvFlg"] == '1') {
                    $query = knjd126bQuery::delRecordProvFlgDat(CTRL_YEAR, $model->field["SUBCLASSCD"], $schno, $model);
                    $db->query($query);
                    if ($model->fields["PROV_FLG"][$i] == '1') {
                        $query = knjd126bQuery::insRecordProvFlgDat(CTRL_YEAR, $model->field["SUBCLASSCD"], $schno, $model);
                        $db->query($query);
                    }
                }
            }//if

            //★履修・修得単位---------->
            knjd126bQuery::updateCredit($term_data2, $value, $model, $schno, $i, $db, $admin_key);
        }//for

        Query::dbCheckIn($db);
    }

    //テーブルRECORD_PROV_FLG_DATのレコードを削除
    public function delRecordProvFlgDat($year, $subclass, $schno, $model)
    {
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
    public function insRecordProvFlgDat($year, $subclass, $schno, $model)
    {
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

    /* データ更新処理 */
    public function updateCredit($term_data2, $value, $model, $schno, $i, $db, $admin_key)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $data = array();

        /* 履修単位・修得単位の自動計算処理 始め */

        //$notice_val[0]:欠課数(画面表示)
        //$notice_val[1]:単位数
        //$notice_val[2]:欠課数上限値
        //$notice_val[3]:半期認定フラグ
        $notice_val = explode("-", $model->fields["NOTICE_LATE"][$i]);

        //$combined_val[0]:合併元科目の履修単位の合計
        //$combined_val[1]:合併元科目の修得単位の合計
        $combined_val = explode("-", $model->fields["COMBINED_CREDIT_SUM"][$i]);

        if (in_array("0885", $admin_key) &&
        //「履修・修得単位」が入力可かつNULLでない場合、「手入力した値」をセット
        (strlen($model->fields["COMP_CREDIT"][$i]) || strlen($model->fields["GET_CREDIT"][$i]))) {
            $data["COMP_CREDIT"][NUMBER] = $model->fields["COMP_CREDIT"][$i];
            $data["GET_CREDIT"][NUMBER]  = $model->fields["GET_CREDIT"][$i];
        } else {
            // 自動計算処理
            // 一旦クリア
            $data["COMP_CREDIT"][NUMBER] = "";
            $data["GET_CREDIT"][NUMBER]  = "";
            $data["COMP_TAKESEMES"][TEXT] = "";

            if (isset($model->creditflg)) {
                // 合併先科目である
                // $model->creditflg ---> 1:単位固定タイプ 2:単位加算タイプ
                if ($value > "19") {
                    // 学年評定:２０以上、履修・修得に登録
                    $data["COMP_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                    $data["GET_CREDIT"][NUMBER]  = ($model->creditflg == "2") ? $combined_val[1] : $notice_val[1];
                } elseif ($value > "0" && $value < "20") {
                    // 学年評定:１～１９、履修のみに登録
                    $data["COMP_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                    $data["GET_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                }
            } else {
                // 合併先科目でない--->今までと同じ計算処理をする
                if ($notice_val[1] != "" && $value != "") {
                    //単位数・学年評定がある場合のみ処理
                    $hanki_credit = $notice_val[1] / 2;
                    if ($value > "19" && $notice_val[0] > $notice_val[2]) {
                        //①学年評定:２０以上、欠課数上限値オーバー
                        if ($notice_val[3] == "1") {
                            //半期認定科目
                            $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                            $data["GET_CREDIT"][NUMBER]  = floor($hanki_credit);
                            if ($term_data2[1] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "1";
                            }
                            if ($term_data2[2] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "2";
                            }
                            if ($term_data2[3] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "3";
                            }
                        } else {
                            $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                            $data["GET_CREDIT"][NUMBER]  = $notice_val[1];
                            $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    } elseif ($value > "19" && $notice_val[0] <= $notice_val[2]) {
                        //②学年評定:２０以上、欠課数上限値内
                        $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                        $data["GET_CREDIT"][NUMBER]  = $notice_val[1];
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    } elseif ($value > "0" && $value < "20" && $notice_val[0] > $notice_val[2]) {
                        //③学年評定:１～１９、欠課数上限値オーバー
                        if ($notice_val[3] == "1") {
                            //半期認定科目
                            $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                            $data["GET_CREDIT"][NUMBER] = floor($hanki_credit);
                            if ($term_data2[1] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "1";
                            }
                            if ($term_data2[2] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "2";
                            }
                            if ($term_data2[3] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "3";
                            }
                        } else {
                            $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                            $data["GET_CREDIT"][NUMBER] = $notice_val[1];
                            $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    } elseif ($value > "0" && $value < "20" && $notice_val[0] <= $notice_val[2]) {
                        //④学年評定:１～１９、欠課数上限値内
                        $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                        $data["GET_CREDIT"][NUMBER] = $notice_val[1];
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    } else {
                        //⑥その他---NO006
                        $data["COMP_CREDIT"][NUMBER] = "";
                        $data["GET_CREDIT"][NUMBER]  = "";
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    }
                }//if3
            }//if2
        }//if1

        /* 履修単位・修得単位の自動計算処理 終わり */

        $data["REGISTERCD"][TEXT]        = STAFFCD ;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $where  = "WHERE  YEAR          = '".CTRL_YEAR."' ";
        $where .= "  AND  SEMESTER      = '9' ";
        $where .= "  AND  TESTKINDCD    = '99' ";
        $where .= "  AND  TESTITEMCD    = '00' ";
        $where .= "  AND  SCORE_DIV     = '00' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "  AND  CLASSCD          = '".$substr_classcd."' ";
            $where .= "  AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
            $where .= "  AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $where .= "  AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $where .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
        }
        $where .= "  AND  SCHREGNO      = '".$schno."' ";
        $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);

        $db->query($query);
    }

    //未入力チェックのテーブルにレコードがあるのか
    public function getRecordChkfinDat($model, $testcd)
    {
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
        $query .= "     AND RECORD_DIV = '2' ";

        return $query;
    }
}
