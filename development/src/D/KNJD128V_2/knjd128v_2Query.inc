<?php

require_once('for_php7.php');


class knjd128v_2Query extends Query
{

    //学校名取得
    public function getNameMstZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtn = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtn;
    }

    //課程学科コンボ取得
    public function getCourseMajor($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV TEST_GCM ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST VCM ON TEST_GCM.YEAR = VCM.YEAR ";
        $query .= "           AND TEST_GCM.COURSECD = VCM.COURSECD ";
        $query .= "           AND TEST_GCM.MAJORCD = VCM.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     TEST_GCM.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TEST_GCM.SCHOOLCD = '".SCHOOLCD."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND TEST_GCM.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND TEST_GCM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND TEST_GCM.COURSECD || '-' || TEST_GCM.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //「評価にリンクを付けるか？」のフラグを取得
    public function getNameMstD048()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D048' AND ";
        $query .= "     NAMECD2 = '01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //指導（1:マーク）
    public function getMarkList()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D054' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目別設定があるか
    public function getTestSubCnt($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$subclass_array[1]."' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }

        return $query;
    }

    //「ＣＳＶは、非表示とする？」のフラグを取得
    public function getNameMstD058()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 "; // 1:非表示
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D058' AND ";
        $query .= "     NAMECD2 = '01' ";

        return $query;
    }
    //CSV取込不可チェック
    public function getNotusecsvCnt($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L1 ON L1.YEAR = T1.YEAR ";
        } else {
            $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON L1.YEAR = T1.YEAR ";
        }
        $query .= "                                                AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                                                AND L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                                AND L1.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                                AND L1.SCORE_DIV = T1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "                                            AND L1.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "                                            AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L1.GRADE = T1.GRADE ";
            $query .= "                                            AND L1.COURSECD || L1.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.CONTROL_FLG = '1' ";
        $query .= " AND L1.NOT_USE_CSV_FLG = '1' ";
        $query .= " AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }

        return $query;
    }

    //科目名の取得
    public function getSubclassName($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        return $query;
    }
    //講座名の取得
    public function getChairName($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        return $query;
    }

    //名称マスタA023に登録されているの学校校種を取得
    public function getSchoolkindQuery()
    {
        $query  = " SELECT ";
        $query .= "     MIN(NAME1) AS SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";

        return $query;
    }

    //管理者コントロールの基本のSCHOOL_KINDについて、混在は、MAXを取得する。
    //例
    //「00-00-00-000000」・・・これはゴミ
    //「00-H-00-000000」・・・これを参照する
    public function getAdminSchoolKind($schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SCHOOL_KIND) ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     ADMIN_CONTROL_GCM_SDIV_DAT ";
        } else {
            $query .= "     ADMIN_CONTROL_SDIV_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD         = '00' ";
        $query .= "     AND SCHOOL_KIND     IN ('00','{$schoolKind}') "; //宮城県は'00'がある(古いデータ)
        $query .= "     AND CURRICULUM_CD   = '00' ";
        $query .= "     AND SUBCLASSCD      = '000000' ";
        return $query;
    }

    //テスト名取得
    public function getTestName($model, $testSubCnt)
    {
        //科目別設定がある場合は、科目別設定。それ以外は基本設定されたパーツのみ表示する。
        $subclasscd = (0 < $testSubCnt) ? $model->field["SUBCLASSCD"] : $model->subclassAll;
        $subclass_array = array();
        $subclass_array = explode("-", $subclasscd);
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE, ";
        $query .= "     T3.TESTITEMABBV1 AS TESTITEMNAME, ";
        $query .= "     T3.SIDOU_INPUT, ";
        $query .= "     T3.SIDOU_INPUT_INF, ";
        $query .= "     T1.CONTROL_FLG ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
        }
        $query .= "     LEFT JOIN SEMESTER_MST T2 ";
        $query .= "         ON  T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T3 ";
        } else {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T3 ";
        }
        $query .= "         ON  T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    = T1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T3.SCHOOLCD     = T1.SCHOOLCD ";
            $query .= "     AND T3.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "     AND T3.GRADE        = T1.GRADE ";
            $query .= "     AND T3.COURSECD || T3.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND T1.SUBCLASSCD     = '".$subclasscd."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "          AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "          AND T1.SCHOOL_KIND = '".$subclass_array[1]."' ";
            $query .= "          AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //科目データの取得
    public function getSubclassMst($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " WITH REPLACE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1' AS DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "     COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        }
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= "     LEFT JOIN REPLACE L1 ON L1.DIV = '1' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = L1.SUBCLASSCD ";
        } else {
            $query .= "         AND T3.SUBCLASSCD = L1.SUBCLASSCD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T4.YEAR       = T1.YEAR ";
        $query .= "     AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD    = T1.CHAIRCD ";
        //更新可能(制限付)
        if ($model->auth != DEF_UPDATABLE) {
            $query .= "     AND T4.STAFFCD = '".STAFFCD."' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "     AND T3.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "     AND T3.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "     AND T3.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T3.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || '：' || T1.chairname AS LABEL ";
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
            $query .= "     T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     T1.chaircd = '".$model->field["CHAIRCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //学期マスタ
    public function getSemesterMst()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //追指導回数MAX値
    public function getMaxSlumpSeq($model, $execute_date)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd128v_2Query::selectQuery($model, $execute_date).") ";
        $query .= " SELECT ";
        $query .= "     MAX(T1.SLUMP_SEQ) AS MAX_SLUMP_SEQ ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_SEQ_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";

        return $query;
    }

    //追指導回数コンボ
    public function selectSlumpSeq($model, $maxSlumpSeq)
    {
        //追指導回数MAX値+1
        $maxSlumpSeq = strlen($maxSlumpSeq) ? $maxSlumpSeq + 1 : 1;
        //関西学院の場合、追指導回数MAX値は２つまでと制限し、文言を「追指導1 → 2年再試」「追指導2 → 3年再試」に変更
        if ($model->z010name1 == "kwansei" && $maxSlumpSeq > 2) {
            $maxSlumpSeq = 2;
        }

        //追指導回数コンボ
        $query  = " WITH SELECT_T (VALUE, LABEL) AS ( ";
        for ($i = 1; $i <= $maxSlumpSeq; $i++) {
            if ($i > 1) {
                $query .= "     UNION ALL ";
            }
            if ($model->z010name1 == "kwansei") {
                $ii = $i + 1;
                $query .= "     VALUES({$i}, '{$ii}年再試') ";
            } else {
                $query .= "     VALUES({$i}, '追指導{$i}') ";
            }
        }
        $query .= " ) ";
        $query .= " SELECT * FROM SELECT_T ORDER BY VALUE ";

        return $query;
    }

    //累積最大日取得
    public function getMax($subclasscd, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
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
            $query .= "          AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "          AND semester   = (SELECT MAX(semester) FROM attend_subclass_dat ";
        $query .= "                             WHERE COPYCD     = '0' ";
        $query .= "                               AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "                               AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "                               AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "                               AND SUBCLASSCD     = '".$subclass_array[3]."') ";
        } else {
            $query .= "                               AND subclasscd = '".$model->field["SUBCLASSCD"]."') ";
        }
        $query .= "ORDER BY tmp_month DESC ";
        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date)
    {
        $query  = "";
        $query .= " SELECT W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "   FROM ";
        /* 生徒一覧 */
        $query .= "    (SELECT schregno ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd = '".$model->field["CHAIRCD"]."'  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
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
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "        AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W2.schregno = W1.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    //成績データ
    public function getScore($model, $execute_date)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd128v_2Query::selectQuery($model, $execute_date).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.COMP_CREDIT, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.VALUE_DI "; //欠試'*'
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";
        return $query;
    }

    //追指導データ
    public function getSlump($model, $execute_date)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd128v_2Query::selectQuery($model, $execute_date).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.MARK, ";
        if ($model->Properties["useSlumpSeq"] == '2' && $model->field["TESTCD"] == "9990008") {
            $query .= "     T1.PASS_FLG, ";
        }
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";

        return $query;
    }

    //追指導データ
    public function getSlumpSeq($model, $execute_date)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd128v_2Query::selectQuery($model, $execute_date).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLUMP_SEQ, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.MARK, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_SEQ_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";

        return $query;
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
        $testcd2 = substr($testcd, 0, 5);
        $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd2}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$course}' END ";
        return $query;
    }

    //成績入力完了チェック
    public function getRecordChkfinDat($model, $testcd)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || RECORD_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

        return $query;
    }

    //成績入力完了更新
    public function updateRecordChkfin($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $db = Query::dbCheckOut();

        //初期化
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];

            $query = knjd128v_2Query::getRecordChkfinDat($model, $testcd);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow) && $controlFlg == "1") {
                $chkfg = ($model->field["CHK_COMP".$testcd] == 'on') ? '1' : '0';

                $data = array();
                $data["EXECUTED"][TEXT]   = $chkfg ;     //成績入力区分
                $data["REGISTERCD"][TEXT] = STAFFCD ;    //登録者コード
                $data["UPDATED"][FUNC]    = "sysdate()"; //更新日付

                $where  = " WHERE ";
                $where .= "         YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || RECORD_DIV = '{$testcd}' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     AND CLASSCD          = '".$subclass_array[0]."' ";
                    $where .= "     AND SCHOOL_KIND      = '".$subclass_array[1]."' ";
                    $where .= "     AND CURRICULUM_CD    = '".$subclass_array[2]."' ";
                    $where .= "     AND SUBCLASSCD       = '".$subclass_array[3]."' ";
                } else {
                    $where .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_SDIV_DAT", $where);
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    //指導データ更新
    public function updateSlump($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        //年度
        $year = CTRL_YEAR;

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $schno = $model->fields["SCHREGNO"][$i];

            foreach ($model->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];
                $controlFlg = $codeArray["CONTROL_FLG"];

                $data = array();
                $data["YEAR"][TEXT]         = $year;
                $data["SEMESTER"][TEXT]     = substr($testcd, 0, 1);
                $data["TESTKINDCD"][TEXT]   = substr($testcd, 1, 2);
                $data["TESTITEMCD"][TEXT]   = substr($testcd, 3, 2);
                $data["SCORE_DIV"][TEXT]    = substr($testcd, 5, 2);
                if ($model->Properties["useSlumpSeq"] == '1') {
                    $data["SLUMP_SEQ"][NUMBER]  = $model->field["SLUMP_SEQ"];
                }
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = $subclass_array[0];
                    $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                    $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                    $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
                } else {
                    $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
                }
                $data["SCHREGNO"][TEXT]     = $schno;
                $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];

                //指導（1:マーク 2:点数）
                $score = $model->fields["SCORE_MARK"][$i];
                $data["SCORE"][NUMBER]      = ($codeArray["SIDOU_INPUT_INF"] == "1") ? "" : $score;
                $data["MARK"][TEXT]         = ($codeArray["SIDOU_INPUT_INF"] == "1") ? $score : "";
                $data["REMARK"][TEXT]       = $model->fields["REMARK"][$i];
                //合格フラグ
                if ($model->Properties["useSlumpSeq"] == '2' && $testcd == "9990008") {
                    $data["PASS_FLG"][TEXT]     = $model->fields["PASS_FLG"][$i];
                }
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                //削除
                $query = knjd128v_2Query::deleteRecordSlump($year, $testcd, $schno, $model);
                $db->query($query);
                //追加
                if ($model->Properties["useSlumpSeq"] == '1' && strlen($score)) {
                    $query = Query::insertSQL($data, "RECORD_SLUMP_SEQ_SDIV_DAT");
                    $db->query($query);
                } elseif ($model->Properties["useSlumpSeq"] != '1' && (strlen($score) || strlen($model->fields["REMARK"][$i]))) {
                    $query = Query::insertSQL($data, "RECORD_SLUMP_SDIV_DAT");
                    $db->query($query);
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //RECORD_SLUMP_SDIV_DAT削除
    public function deleteRecordSlump($year, $testcd, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = "";
        $query .= "DELETE ";
        if ($model->Properties["useSlumpSeq"] == '1') {
            $query .= "FROM   RECORD_SLUMP_SEQ_SDIV_DAT ";
        } else {
            $query .= "FROM   RECORD_SLUMP_SDIV_DAT ";
        }
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        if ($model->Properties["useSlumpSeq"] == '1') {
            $query .= "  AND  SLUMP_SEQ     = {$model->field["SLUMP_SEQ"]} ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //RECORD_SLUMP_SDIV_DATへ反映（追指導回数のMAXの物）
    public function updateSlumpMax($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        //年度
        $year = CTRL_YEAR;

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $schno = $model->fields["SCHREGNO"][$i];

            foreach ($model->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];

                //追指導回数のMAXの物を取得
                $insRow = $db->getRow(knjd128v_2Query::getInsertRecordSlumpMax($year, $testcd, $schno, $model), DB_FETCHMODE_ASSOC);

                if ($model->Properties["useSlumpSeqMaxScore"] == '1') {
                    //成績データと追指導データのMAX値を取得
                    $insRowMaxScore = $db->getRow(knjd128v_2Query::getInsertRecordSlumpMaxScore($year, $testcd, $schno, $model), DB_FETCHMODE_ASSOC);
                    $insRow["SCORE"] = $insRowMaxScore["SCORE"];
                }
    
                $data = array();
                $data["YEAR"][TEXT]         = $insRow["YEAR"];
                $data["SEMESTER"][TEXT]     = $insRow["SEMESTER"];
                $data["TESTKINDCD"][TEXT]   = $insRow["TESTKINDCD"];
                $data["TESTITEMCD"][TEXT]   = $insRow["TESTITEMCD"];
                $data["SCORE_DIV"][TEXT]    = $insRow["SCORE_DIV"];
                $data["CLASSCD"][TEXT]      = $insRow["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]  = $insRow["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]= $insRow["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]   = $insRow["SUBCLASSCD"];
                $data["SCHREGNO"][TEXT]     = $insRow["SCHREGNO"];
                $data["CHAIRCD"][TEXT]      = $insRow["CHAIRCD"];
                $data["SCORE"][NUMBER]      = $insRow["SCORE"];
                $data["MARK"][TEXT]         = $insRow["MARK"];
                $data["REMARK"][TEXT]       = $insRow["REMARK"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                //削除
                $query = knjd128v_2Query::deleteRecordSlumpMax($year, $testcd, $schno, $model);
                $db->query($query);
                //追加
                if (strlen($insRow["SCORE"]) || strlen($insRow["MARK"])) {
                    $query = Query::insertSQL($data, "RECORD_SLUMP_SDIV_DAT");
                    $db->query($query);
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //RECORD_SLUMP_SDIV_DAT削除（追指導回数のMAXの物）
    public function deleteRecordSlumpMax($year, $testcd, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_SLUMP_SDIV_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= "  AND SCHREGNO       = '{$schno}' ";
        return $query;
    }

    //追指導回数のMAXの物を取得
    public function getInsertRecordSlumpMax($year, $testcd, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH MAX_SLUMP_SEQ AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(T1.SLUMP_SEQ) AS SLUMP_SEQ ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_SEQ_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$testcd}' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO      = '{$schno}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_SEQ_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$testcd}' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO      = '{$schno}' ";
        $query .= "     AND T1.SLUMP_SEQ     IN (SELECT W1.SLUMP_SEQ FROM MAX_SLUMP_SEQ W1) ";

        return $query;
    }

    //成績データと追指導データのMAX値を取得
    public function getInsertRecordSlumpMaxScore($year, $testcd, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH RECORD_SLUMP_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         MAX(T1.SCORE) AS SCORE ";
        $query .= "     FROM ";
        $query .= "         RECORD_SLUMP_SEQ_SDIV_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '{$year}' ";
        $query .= "         AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$testcd}' ";
        $query .= "         AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "         AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "         AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "         AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "         AND T1.SCHREGNO      = '{$schno}' ";
        $query .= "         AND T1.SCORE IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " , RECORD_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SCORE ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '{$year}' ";
        $query .= "         AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$testcd}' ";
        $query .= "         AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "         AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "         AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "         AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "         AND T1.SCHREGNO      = '{$schno}' ";
        $query .= "         AND T1.SCORE IS NOT NULL ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CASE WHEN T1.SCORE > T2.SCORE THEN T1.SCORE ELSE T2.SCORE END AS SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_SEQ T1 ";
        $query .= "     LEFT JOIN RECORD_SCORE T2 ON T2.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }

    /* 成績データ更新処理 */
    public function update($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $year = CTRL_YEAR;

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $schno = $model->fields["SCHREGNO"][$i];

            foreach ($model->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];
                $controlFlg = $codeArray["CONTROL_FLG"];
                $score = $model->fields["SCORE".$testcd][$i];

                //管理者コントロール
                if ($controlFlg == "1") {
                    knjd128v_2Query::delInsRecordScore($year, $testcd, $model, $schno, $score, $db);
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //更新
    public function delInsRecordScore($year, $testcd, $model, $schno, $score, $db)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = substr($testcd, 0, 1);
        $data["TESTKINDCD"][TEXT]   = substr($testcd, 1, 2);
        $data["TESTITEMCD"][TEXT]   = substr($testcd, 3, 2);
        $data["SCORE_DIV"][TEXT]    = substr($testcd, 5, 2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        $diFlg = ($score == "*" || $score == "**") ? "1" : "";
        if ($model->prgid == "KNJD129L") {
            if ($score == "+" || $score == "-") {
                $diFlg = "1";
            }
        }
        $data["SCORE"][NUMBER]      = $diFlg == "1" ? "" : $score;
        $data["VALUE_DI"][TEXT]     = $diFlg == "1" ? $score : "";
        $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        //削除
        $query = knjd128v_2Query::deleteRecordScore($year, $testcd, $schno, $model);
        $db->query($query);
        //成績が1件も入力されていない場合、RECORD_SCORE_DATのレコードは作成しない（考査ごと）
        //testInsArray：成績が１つでも入力された考査コードを配列に保管
        if (in_array($testcd, $model->testInsArray)) {
            //追加
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }
    }

    //テーブルRECORD_SCORE_DATのレコードを削除
    public function deleteRecordScore($year, $testcd, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }
}
