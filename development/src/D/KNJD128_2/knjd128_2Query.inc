<?php

require_once('for_php7.php');


class knjd128_2query extends Query
{

    //テスト名取得
    public function getTestName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTERNAME, ";
        $query .= "     T1.SEMESTER as VALUE_SEME, ";
        $query .= "     rtrim(ltrim(substr(T2.TESTITEMNAME,1,15))) as TESTITEMNAME, "; //頭全角３文字
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

    //編集可能学期の判別
    public function selectContolCodeQuery($model)
    {
        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '1' ";

        return $query;
    }

    //時間割講座テストより試験日を抽出
    public function selectExecuteDateQuery($model, $semester, $testkindcd, $testitemcd)
    {
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
    public function selectSubclassQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
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
        if ($model->auth != DEF_UPDATABLE) {
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD  AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND  AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD  AND ";
        }
        $query .= "     T3.subclasscd = T1.subclasscd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "     T3.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "     T3.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "     T3.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     T3.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
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
        if ($model->auth != DEF_UPDATABLE) {
            $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     T1.chaircd = '".$model->field["CHAIRCD"]."' ";
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
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
        $query  = "SELECT assessmark,assesslevel";
        $query .= "  FROM assess_mst ";
        $query .= " WHERE assesscd = '3'";
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
        //定期考査の出欠の表
        $query  = " WITH TEST_ATTEND_B AS ( ";
        $query .= "     SELECT  T2.SCHREGNO,  ";
        $query .= "             MIN(SMALLINT(CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END)) AS TST_SCORE_ATTEND ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTCD"]."' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     GROUP BY T2.SCHREGNO  ";
        $query .= "     ) ";
        //成績データの表
        $query .= " ,RECORD_SCORE AS ( ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SELECT  YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ,SCHREGNO as SCHREGNO2 ";
        } else {
            $query .= "     SELECT  YEAR ,SUBCLASSCD ,SCHREGNO as SCHREGNO2 ";
        }
        //講座
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '".$model->field["TESTCD"]."' THEN CHAIRCD END) as TST_CHAIRCD ";
        if ($model->field["RECORD_DIV"] == "1") {
            //素点
            $query .= "         ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '".$model->field["TESTCD"]."' THEN SCORE END) as TST_SCORE ";
            //出欠
            $query .= "         ,cast(null as varchar(2)) as TST_SCORE_DI ";
        } else {
            //評価
            $query .= "         ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '".$model->field["TESTCD"]."' THEN VALUE END) as TST_SCORE ";
            //出欠
            $query .= "         ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '".$model->field["TESTCD"]."' THEN VALUE_DI END) as TST_SCORE_DI ";
        }

        $query .= "     FROM   RECORD_SCORE_DAT ";
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
        $query .= "       AND  SEMESTER || TESTKINDCD || TESTITEMCD = '".$model->field["TESTCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     GROUP BY YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ,SCHREGNO ";
        } else {
            $query .= "     GROUP BY YEAR ,SUBCLASSCD ,SCHREGNO ";
        }
        $query .= "     ) ";

        $query .= " SELECT W0.*";
        $query .= "       ,W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W5.chair_sem ";
        $query .= "       ,W7.TST_SCORE_ATTEND ";
        if ($model->isJitu) {
            $query .= "   ,value(L1.COMP_ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        } else {
            $query .= "   ,value(W8.ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        }
        $query .= "       ,W8.CREDITS,W8.AUTHORIZE_FLG ";
        $query .= "       ,F1.SLUMP ";
        $query .= "       ,F1.REMARK ";
        $query .= "   FROM ";
        $query .= "    (SELECT schregno ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd = '".$model->field["CHAIRCD"]."'  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
        $query .= "    ) W1 ";
        /* 成績情報 */
        $query .= " LEFT JOIN RECORD_SCORE W0 ON W0.SCHREGNO2 = W1.schregno ";
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
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 在籍情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '".substr($model->field["TESTCD"], 0, 1)."') W5";
        $query .= "   ON W5.schregno = W1.schregno ";
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

        /* 不振 */
        $query .= " LEFT JOIN (";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SLUMP, ";
        $query .= "         REMARK ";
        $query .= "     FROM ";
        $query .= "         RECORD_SLUMP_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '".$model->field["TESTCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD       = '".$substr_classcd."' ";
            $query .= "       AND  SCHOOL_KIND   = '".$substr_school_kind."' ";
            $query .= "       AND  CURRICULUM_CD = '".$substr_curriculum_cd."' ";
            $query .= "       AND  SUBCLASSCD    = '".$substr_subclasscd."' ";
        } else {
            $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ) F1 ON F1.schregno = W1.schregno ";

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

    // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
    // 名称区分：D015　名称コード：01　名称予備１：「Y」もしくは「NULL」
    public function getNameMst()
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
    public function getOffdaysFlg($year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$year}' ";

        return $query;
    }

    public function GetAttendData($chaircd, $subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg, $model)
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
        if ($offdaysFlg["SUB_OFFDAYS"]  == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($offdaysFlg["SUB_ABSENT"]   == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($offdaysFlg["SUB_SUSPEND"]  == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($offdaysFlg["SUB_VIRUS"]    == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_KOUDOME"]  == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query  = "SELECT schregno ";
        $query .= "      ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0))";
        if ($offdaysFlg["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        $query .= "       AS LESSON ";

        $query .= "      ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0)) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0))";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " - SUM(VALUE(VIRUS,0))";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " - SUM(VALUE(KOUDOME,0))";
        }
        if ($offdaysFlg["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        if ($offdaysFlg["SUB_SUSPEND"]  == "1") {
            $query .= " + SUM(VALUE(SUSPEND,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") {
            $query .= " + SUM(VALUE(MOURNING,0))";
        }
        if ($offdaysFlg["SUB_VIRUS"]    == "1") {
            $query .= " + SUM(VALUE(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_KOUDOME"]  == "1") {
            $query .= " + SUM(VALUE(KOUDOME,0))";
        }
        $query .= "       AS MLESSON ";

        $query .= "       ,".$notice."  AS T_NOTICE ";
        //小数点ありAdd
        if ($absent_cov == "4" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "5" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$offdaysFlg["AMARI_KURIAGE"]} ";
            $query .= "         THEN 0 ";
            $query .= "         ELSE MOD({$late_early},{$absent_cov_late}) ";
            $query .= "    END AS T_LATEEARLY ";
            $query .= "   ,(({$late_early}) / {$absent_cov_late}) + ({$notice}) + (CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$offdaysFlg["AMARI_KURIAGE"]} ";
            $query .= "                                                  THEN 1 ";
            $query .= "                                                  ELSE 0 ";
            $query .= "                                             END) AS NOTICE_LATE ";
        } elseif ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "   ,".$late_early."     AS T_LATEEARLY ";
            $query .= "   ,".$notice."  AS NOTICE_LATE ";
        }
        $query .= "       ,SUM(value(ABSENT,0))   AS ABSENT ";
        $query .= "       ,SUM(value(MOURNING,0)) AS MOURNING ";
        $query .= "       ,SUM(value(SUSPEND,0))  AS SUSPEND ";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " ,SUM(value(VIRUS,0))  AS VIRUS ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " ,SUM(value(KOUDOME,0))  AS KOUDOME ";
        }
        $query .= "       ,".$late_early."     AS LATE_EARLY ";
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
    public function GetAttendData2($chaircd, $subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg, $model)
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
        if ($offdaysFlg["SUB_OFFDAYS"]  == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($offdaysFlg["SUB_ABSENT"]   == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($offdaysFlg["SUB_SUSPEND"]  == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($offdaysFlg["SUB_VIRUS"]    == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_KOUDOME"]  == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(LESSON)      AS LESSON";
        $query .= "       ,SUM(MLESSON)     AS MLESSON";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "       ,SUM(value(ABSENT,0))   AS ABSENT ";
        $query .= "       ,SUM(value(MOURNING,0)) AS MOURNING ";
        $query .= "       ,SUM(value(SUSPEND,0))  AS SUSPEND ";
        $query .= "       ,SUM(LATE_EARLY)  AS LATE_EARLY";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno ";
        $query .= "               ,T1.semester ";

        $query .= "               ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0))";
        if ($offdaysFlg["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        $query .= "                AS LESSON ";

        $query .= "               ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0)) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0))";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " - SUM(VALUE(VIRUS,0))";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " - SUM(VALUE(KOUDOME,0))";
        }
        if ($offdaysFlg["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        if ($offdaysFlg["SUB_SUSPEND"]  == "1") {
            $query .= " + SUM(VALUE(SUSPEND,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") {
            $query .= " + SUM(VALUE(MOURNING,0))";
        }
        if ($offdaysFlg["SUB_VIRUS"]    == "1") {
            $query .= " + SUM(VALUE(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_KOUDOME"]  == "1") {
            $query .= " + SUM(VALUE(KOUDOME,0))";
        }
        $query .= "                AS MLESSON ";

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
        $query .= "               ,SUM(value(ABSENT,0))   AS ABSENT ";
        $query .= "               ,SUM(value(MOURNING,0)) AS MOURNING ";
        $query .= "               ,SUM(value(SUSPEND,0))  AS SUSPEND ";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " ,SUM(value(VIRUS,0))  AS VIRUS ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " ,SUM(value(KOUDOME,0))  AS KOUDOME ";
        }
        $query .= "               ,".$late_early."    AS LATE_EARLY ";
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
        $query .= "                          AND T1.semester = T2.semester ";
        $query .= "                          AND T1.schregno = T2.schregno ";
        $query .= "                          AND T2.chaircd  = '".$chaircd."')";
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " GROUP BY schregno ";

        return $query;
    }

    /* 不振データ更新処理 */
    public function updateSlump($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < $model->field["COUNT"]; $i++) {
            //変数
            $schno = $model->fields["SCHREGNO"][$i];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
                $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
                $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
                $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
            } else {
                $subcd = $model->field["SUBCLASSCD"];
            }
            $year = CTRL_YEAR;
            $semcd  = substr($model->field["TESTCD"], 0, 1);
            $kindcd = substr($model->field["TESTCD"], 1, 2);
            $itemcd = substr($model->field["TESTCD"], 3, 2);
            //削除
            $query  = "DELETE ";
            $query .= "FROM   RECORD_SLUMP_DAT ";
            $query .= " WHERE YEAR          = '{$year}'";
            $query .= "   AND SEMESTER      = '{$semcd}'";
            $query .= "   AND TESTKINDCD    = '{$kindcd}'";
            $query .= "   AND TESTITEMCD    = '{$itemcd}'";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND CLASSCD          = '".$substr_classcd."' ";
                $query .= "   AND SCHOOL_KIND      = '".$substr_school_kind."' ";
                $query .= "   AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                $query .= "   AND SUBCLASSCD       = '".$substr_subclasscd."' ";
            } else {
                $query .= "   AND SUBCLASSCD    = '{$subcd}'";
            }
            $query .= "   AND SCHREGNO      = '{$schno}'";
            $db->query($query);
            //不振チェックありまたは備考(不振理由等)入力ありのデータのみ作成
            if ($model->fields["SLUMP"][$i] != '1' && !strlen($model->fields["REMARK"][$i])) {
                continue;
            }
            //項目
            $data = array();
            $data["YEAR"][TEXT]         = $year;
            $data["SEMESTER"][TEXT]     = $semcd;
            $data["TESTKINDCD"][TEXT]   = $kindcd;
            $data["TESTITEMCD"][TEXT]   = $itemcd;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $substr_classcd;
                $data["SCHOOL_KIND"][TEXT]    = $substr_school_kind;
                $data["CURRICULUM_CD"][TEXT]  = $substr_curriculum_cd;
                $data["SUBCLASSCD"][TEXT]     = $substr_subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]     = $subcd;
            }
            $data["SCHREGNO"][TEXT]     = $schno;
            $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];
            $data["SLUMP"][TEXT]        = $model->fields["SLUMP"][$i];
            $data["REMARK"][TEXT]       = $model->fields["REMARK"][$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "RECORD_SLUMP_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    /* データ更新処理 */
    public function update($model)
    {
        $db = Query::dbCheckOut();
        //管理者コントロール
        $admin_key = array();
        $results = $db->query(knjd128_2Query::selectContolCodeQuery($model));
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]]=$row_ct["CONTROL_CODE"];
        }
        $results->free();

        $testcdArray  = array("1111" => "10101"
                             ,"1211" => "10201"
                             ,"1221" => "10202"
                             ,"2111" => "20101"
                             ,"2121" => "20102"
                             ,"2211" => "20201"
                             ,"2221" => "20202"
                             ,"3111" => "30101"
                             ,"3211" => "30201"
                             ,"1902" => "19900"
                             ,"1912" => "19901"
                             ,"2902" => "29900"
                             ,"2912" => "29901"
                             ,"3902" => "39900"
                             ,"9912" => "99901");

        for ($i = 0; $i < $model->field["COUNT"]; $i++) {
            $data = array();
            $schno = $model->fields["SCHREGNO"][$i];

            foreach ($testcdArray as $testkey => $testval) {
                $sem  = substr($testval, 0, 1);
                $kind = substr($testval, 1, 2);
                $item = substr($testval, 3, 2);
                $div  = ($model->field["RECORD_DIV"] == "1") ? "01" : "00";
                if (isset($admin_key[$testkey]) && $model->field["TESTCD"] == $testval) {
                    knjd128_2Query::insertUpdateRecordScore(CTRL_YEAR, $sem, $kind, $item, $div, $model, $schno, $i, $db);
                }
            }
        }
        Query::dbCheckIn($db);
    }

    public function updateRecordChkfin($model, $recordDiv)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        $testcdArray = array("1111" => "10101"
                            ,"1211" => "10201"
                            ,"1221" => "10202"
                            ,"2111" => "20101"
                            ,"2121" => "20102"
                            ,"2211" => "20201"
                            ,"2221" => "20202"
                            ,"3111" => "30101"
                            ,"3211" => "30201"
                            ,"1902" => "19900"
                            ,"1912" => "19901"
                            ,"2902" => "29900"
                            ,"2912" => "29901"
                            ,"3902" => "39900"
                            ,"9912" => "99901");

        //管理者コントロール
        $query = knjd128_2Query::selectContolCodeQuery($model);
        $results = $db->query($query);
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]] = $row_ct["CONTROL_CODE"];
        }
        $results->free();
        //初期化
        $j = 1;
        $strChkComp = "CHK_COMP";
        foreach ($testcdArray as $testkey => $testval) {
            if ($testval != $model->field["TESTCD"]) {
                continue;
            }

            $query = knjd128_2Query::getRecordChkfinDat($model, $testval, $recordDiv);
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
                    $where .= "  AND  CLASSCD          = '".$substr_classcd."' ";
                    $where .= "  AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
                    $where .= "  AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                    $where .= "  AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
                } else {
                    $where .= "  AND  SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";
                $where .= "     AND RECORD_DIV = '{$recordDiv}' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_DAT", $where);

                $db->query($query);
            }
//            $j++;
        }
        Query::dbCheckIn($db);
    }

    public function updateRecordSchChrTest($model)
    {
        $db = Query::dbCheckOut();

        $testcdArray = array("1111" => "10101"
                            ,"1211" => "10201"
                            ,"1221" => "10202"
                            ,"2111" => "20101"
                            ,"2121" => "20102"
                            ,"2211" => "20201"
                            ,"2221" => "20202"
                            ,"3111" => "30101"
                            ,"3211" => "30201");

        //管理者コントロール
        $query = knjd128_2Query::selectContolCodeQuery($model);
        $results = $db->query($query);
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]] = $row_ct["CONTROL_CODE"];
        }
        $results->free();
        //初期化
        $j = 1;
        foreach ($testcdArray as $testkey => $testval) {
            if ($testval != $model->field["TESTCD"]) {
                continue;
            }

            $query = knjd128_2Query::getRecordSchChrTest($model, $testval);
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
//            $j++;
        }
        Query::dbCheckIn($db);
    }

    //未入力チェックのテーブルにレコードがあるのか
    public function getRecordChkfinDat($model, $testcd, $recordDiv)
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
        $query .= "     AND RECORD_DIV = '{$recordDiv}' ";

        return $query;
    }

    //未入力チェックのテーブルにレコードがあるのか
    public function getRecordSchChrTest($model, $testcd)
    {
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

    //追加および更新
    public function insertUpdateRecordScore($year, $sem, $kind, $item, $div, $model, $schno, $i, $db)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $data = array();

        $sem_kind_item = $sem.$kind.$item;
        $deleteFlg = false; //レコード削除フラグ

        //１学期中間
        //１学期期末
        //２学期中間
        //２学期期末
        //３学期期末
        if ($model->field["RECORD_DIV"] == "1") {
            if (!$model->isScoreAllNull["TST_SCORE"]) {
                $deleteFlg = true;
            }
            $data["SCORE"][NUMBER]  = $model->fields["TST_SCORE"][$i];
            if ($model->testcd == $sem_kind_item) {
                $data["CHAIRCD"][TEXT] = $model->field["CHAIRCD"];
            }
            //１学期
        //２学期
        } else {
            if (!$model->isScoreAllNull["TST_SCORE"]) {
                $deleteFlg = true;
            }
            $data["VALUE"][NUMBER]  = $model->fields["TST_SCORE"][$i];
            $data["VALUE_DI"][TEXT] = $model->fields["TST_SCORE_DI"][$i];
        }

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
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($deleteFlg) {
            //成績が全てNULLの場合、レコードを削除する（つまり、レコードを作成しない）
            $query = knjd128_2Query::deleteRecordScore($year, $sem, $kind, $item, $div, $model->field["SUBCLASSCD"], $schno, $model);
            $db->query($query);
        } else {
            //追加および更新を判定
            $query = knjd128_2Query::existRecordScore($year, $sem, $kind, $item, $div, $model->field["SUBCLASSCD"], $schno, $model);
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
    }

    //テーブルRECORD_SCORE_DATのレコードを削除
    public function deleteRecordScore($year, $sem, $kind, $item, $div, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($subclass, 0, 2);
            $substr_school_kind      = substr($subclass, 3, 1);
            $substr_curriculum_cd    = substr($subclass, 5, 1);
            $substr_subclasscd       = substr($subclass, 7, 6);
        }
        $query  = "";
        $query .= "DELETE ";
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
            $query .= "  AND  SUBCLASSCD    = '$subclass' ";
        }
        $query .= "  AND  SCHREGNO      = '$schno' ";
        return $query;
    }

    //テーブルRECORD_SCORE_DATの存在チェック用SQL
    public function existRecordScore($year, $sem, $kind, $item, $div, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($subclass, 0, 2);
            $substr_school_kind      = substr($subclass, 3, 1);
            $substr_curriculum_cd    = substr($subclass, 5, 1);
            $substr_subclasscd       = substr($subclass, 7, 6);
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
            $query .= "  AND CLASSCD          = '".$substr_classcd."' ";
            $query .= "  AND SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "  AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "  AND SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '$subclass' ";
        }
        $query .= "  AND  SCHREGNO      = '$schno' ";
        return $query;
    }

    //評定マスタ
    public function GetAssessLevel($assesscd, $score)
    {
        $query  = "SELECT  ASSESSLEVEL ";
        $query .= "FROM    ASSESS_MST ";
        $query .= "WHERE   ASSESSCD = '$assesscd' ";
        $query .= "  AND   $score BETWEEN ASSESSLOW AND ASSESSHIGH ";
        return $query;
    }
}
