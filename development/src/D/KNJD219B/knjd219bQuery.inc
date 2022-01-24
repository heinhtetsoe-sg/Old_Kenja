<?php

require_once('for_php7.php');

class knjd219bQuery extends Query {

    //学年コンボ
    function getGrade() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND (SCHOOL_KIND = 'J' OR SCHOOL_KIND = 'H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期コンボ
    function getSemester() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースグループコンボ
    function getGroupCd($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GROUP_CD || ':' || GROUP_NAME AS LABEL, ";
        $query .= "     GROUP_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclasscd($model) {
        $year = CTRL_YEAR;
        $staffcd = STAFFCD;

        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD || ':' || V1.SUBCLASSNAME AS LABEL, ";
            $query .= "     V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     V1.SUBCLASSCD || ':' || V1.SUBCLASSNAME AS LABEL, ";
            $query .= "     V1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST V1";
        $query .= " WHERE ";
        $query .= "     V1.YEAR = '{$year}' ";
        //成績データにある科目
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD IN ( ";
        } else {
            $query .= "     AND V1.SUBCLASSCD IN ( ";
        }
        $query .= "         SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD ";
        } else {
            $query .= "             L1.SUBCLASSCD ";
        }
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "             INNER JOIN RECORD_SCORE_DAT L1 ";
        $query .= "                 ON  L1.YEAR         = T1.YEAR ";
        $query .= "                 AND L1.SEMESTER     = T1.SEMESTER ";
        $query .= "                 AND L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "                 T1.YEAR     = '{$year}' ";
        $query .= "             AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "             AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "             AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ( ";
        $query .= "                 SELECT ";
        $query .= "                     G1.COURSECD || G1.MAJORCD || G1.COURSECODE AS COURSE ";
        $query .= "                 FROM ";
        $query .= "                     COURSE_GROUP_CD_DAT G1 ";
        $query .= "                 WHERE ";
        $query .= "                         G1.YEAR     = '{$year}' ";
        $query .= "                     AND G1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "                     AND G1.GROUP_CD = '{$model->field["GROUP_CD"]}' ";
        $query .= "                 ) ";
        $query .= "         GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             L1.CLASSCD, ";
            $query .= "             L1.SCHOOL_KIND, ";
            $query .= "             L1.CURRICULUM_CD, ";
        }
        $query .= "             L1.SUBCLASSCD ";

        $query .= "     ) ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD IN ( ";
            } else {
                $query .= "     AND V1.SUBCLASSCD IN ( ";
            }
            $query .= "         SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            } else {
                $query .= "             T1.SUBCLASSCD ";
            }
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1, ";
            $query .= "             CHAIR_STD_DAT T2, ";
            $query .= "             CHAIR_STF_DAT T4 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '{$year}' ";
            $query .= "             AND T1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
            $query .= "             AND SUBSTR(T1.SUBCLASSCD, 1 ,2) <= '90' ";
            $query .= "             AND T2.YEAR       = T1.YEAR ";
            $query .= "             AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.YEAR       = T1.YEAR ";
            $query .= "             AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.STAFFCD    = '{$staffcd}' "; //ログインした先生
            $query .= "         GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         T1.CLASSCD, ";
                $query .= "         T1.SCHOOL_KIND, ";
                $query .= "         T1.CURRICULUM_CD, ";
            }
            $query .= "             T1.SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別リスト
    function getTestkindcd($model, $testcd = "") {
        $year = CTRL_YEAR;

        $query  = " WITH T_HEIJOTEN(TESTKINDCD, TESTITEMCD, TESTITEMNAME) AS ( ";
        $query .= "     VALUES('99', '01', '平常点') ";
        $query .= "     ) ";

        if ($testcd != "") {
            $query .= " , T_TESTITEM AS ( ";
        }
        $query .= " SELECT ";
        $query .= "     '1' || '-' || T1.TESTKINDCD || T1.TESTITEMCD || ':' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     '1' || '-' || T1.TESTKINDCD || T1.TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD IN ('0101','0201') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '1' || '-' || T1.TESTKINDCD || T1.TESTITEMCD || ':' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     '1' || '-' || T1.TESTKINDCD || T1.TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     T_HEIJOTEN T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        if ($testcd != "") {
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     T_TESTITEM ";
            $query .= " WHERE ";
            $query .= "     VALUE = '{$testcd}' ";
        }

        return $query;
    }

    //テスト種別リスト（実力）
    function getProficiencyCd($model, $testcd = "") {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     '2' || '-' || T1.PROFICIENCYDIV || T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     '2' || '-' || T1.PROFICIENCYDIV || T1.PROFICIENCYCD || ':' || L1.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     INNER JOIN PROFICIENCY_MST L1 ";
        $query .= "         ON  T1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
        $query .= "         AND T1.PROFICIENCYCD  = L1.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        if ($testcd != "") {
            $query .= "     AND T1.PROFICIENCYDIV || T1.PROFICIENCYCD = '{$testcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別数
    function getPerTest($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.TESTDIV || '-' || T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.GROUP_CD = '{$model->field["GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "     AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND T1.TESTDIV  = '1' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.TESTDIV || '-' || T1.PROFICIENCYDIV || T1.PROFICIENCYCD AS TESTCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.GROUP_CD = '{$model->field["GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "     AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND T1.TESTDIV  = '2' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //考査満点マスタ(DIV = 01:科目, 02:学年, 04:コースグループ) //「DIV = 03:コース」は設定されていないこと
    function getPerfect($model, $testcd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query  = "SELECT ";
        $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' AND ";
        }
        $query .= "    SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "    TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$model->field["GRADE"]}' END AND ";
        $query .= "    COURSECD = '0' AND ";
        $query .= "    MAJORCD = CASE WHEN DIV IN ('01','02') THEN '000' ELSE '{$model->field["GROUP_CD"]}' END AND ";
        $query .= "    COURSECODE = '0000' ";
        return $query;
    }

    //実力科目コードを取得
    function getProficiencySubclasscd($model, $testcd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     MIN(PROFICIENCY_SUBCLASS_CD) AS PROFICIENCY_SUBCLASS_CD ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' AND ";
        }
        $query .= "     SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     PROFICIENCYDIV || PROFICIENCYCD = '{$testcd}' AND ";
        $query .= "     DIV = '04' AND ";
        $query .= "     GRADE = '{$model->field["GRADE"]}' AND ";
        $query .= "     COURSECD = '0' AND ";
        $query .= "     MAJORCD = '{$model->field["GROUP_CD"]}' AND ";
        $query .= "     COURSECODE = '0000' ";
        return $query;
    }

    //実力満点マスタ(DIV = 01:科目, 02:学年, 04:コースグループ) //「DIV = 03:コース」は設定されていないこと
    function getProficiencyPerfect($model, $testcd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query  = "SELECT ";
        $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PROFICIENCY_PERFECT_COURSE_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    PROFICIENCY_SUBCLASS_CD = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' AND ";
        $query .= "    SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "    PROFICIENCYDIV || PROFICIENCYCD = '{$testcd}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$model->field["GRADE"]}' END AND ";
        $query .= "    COURSECD = '0' AND ";
        $query .= "    MAJORCD = CASE WHEN DIV IN ('01','02') THEN '000' ELSE '{$model->field["GROUP_CD"]}' END AND ";
        $query .= "    COURSECODE = '0000' ";
        return $query;
    }

    //リスト取得
    function getListPercent($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.PERCENT, ";
        $query .= "     T2.TOTAL, ";
        $query .= "     T2.ADJUST ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_SEMVAL_ADJUST_DAT T2 ";
        $query .= "         ON  T2.YEAR     = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.GRADE    = T1.GRADE ";
        $query .= "         AND T2.GROUP_CD = T1.GROUP_CD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.GROUP_CD = '{$model->field["GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND T1.CLASSCD           = '".$subclass_array[0]."' ";
            $query .= "    AND T1.SCHOOL_KIND       = '".$subclass_array[1]."' ";
            $query .= "    AND T1.CURRICULUM_CD     = '".$subclass_array[2]."' ";
            $query .= "    AND T1.SUBCLASSCD        = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //学年名(照会)
    function getGradeName($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS GRADE_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //成績種別名(照会)
    function getTestName($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T2.SEMESTERNAME || '　' || T1.TESTITEMNAME AS TEST_NAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";

        return $query;
    }

    //リスト取得(照会)
    function getListInquiry($model) {
        $year = CTRL_YEAR;
        $schregSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];

        $query  = " WITH T_ASSESS_LEVEL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.TESTKINDCD, ";
        $query .= "         T1.TESTITEMCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         COUNT(T1.ASSESSLEVEL) AS CNT_DANKAI ";
        $query .= "     FROM ";
        $query .= "         ASSESS_LEVEL_MST T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$year}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "         AND T1.DIV = '{$model->field["DIV"]}' ";
        $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.TESTKINDCD, ";
        $query .= "         T1.TESTITEMCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     S1.SEMESTERNAME, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD, ";
        $query .= "     S2.TESTITEMNAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.GRADE_NAME1 AS GRADE_NAME, ";
        $query .= "     T1.DIV, ";
        $query .= "     CASE WHEN T1.DIV = '1' THEN '学年' ";
        $query .= "          WHEN T1.DIV = '2' THEN 'クラス' ";
        $query .= "          WHEN T1.DIV = '3' THEN 'コース' ";
        $query .= "          ELSE NULL END AS DIV_NAME, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     L2.HR_NAME, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
        $query .= "     L3.COURSENAME || L3.MAJORNAME || L4.COURSECODENAME AS COURSE_NAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L5.SUBCLASSNAME, ";
        $query .= "     T1.CNT_DANKAI ";
        $query .= " FROM ";
        $query .= "     T_ASSESS_LEVEL T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST S1 ";
        $query .= "         ON  S1.YEAR = T1.YEAR ";
        $query .= "         AND S1.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW S2 ";
        $query .= "         ON  S2.YEAR = T1.YEAR ";
        $query .= "         AND S2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND S2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND S2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ";
        $query .= "         ON  L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ";
        $query .= "         ON  L2.YEAR = T1.YEAR ";
        $query .= "         AND L2.SEMESTER = '{$schregSemester}' ";
        $query .= "         AND L2.GRADE = T1.GRADE ";
        $query .= "         AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L3 ";
        $query .= "         ON  L3.YEAR = T1.YEAR ";
        $query .= "         AND L3.COURSECD = T1.COURSECD ";
        $query .= "         AND L3.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L4 ";
        $query .= "         ON  L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECODE = T1.COURSECODE ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST L5 ";
        $query .= "         ON  L5.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L5.CLASSCD = T1.CLASSCD ";
            $query .= "         AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND L5.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //学期成績自動算出
    function getCalcSemValSql($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query  = "";
        //調整点を抽出
        $query .= " WITH T_ADJUST AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         ADJUST ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_SEMVAL_ADJUST_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '{$year}' ";
        $query .= "         AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "         AND GROUP_CD = '{$model->field["GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND CLASSCD         = '".$subclass_array[0]."' ";
            $query .= "         AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
            $query .= "         AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
            $query .= "         AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     ) ";
        //指定されたテストの情報を抽出
        $query .= " , T_PERCENT AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_SEMVAL_PERCENT_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '{$year}' ";
        $query .= "         AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "         AND GROUP_CD = '{$model->field["GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND CLASSCD         = '".$subclass_array[0]."' ";
            $query .= "         AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
            $query .= "         AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
            $query .= "         AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     ) ";
        //指定されたテストをカウント
        $query .= " , T_PERCENT_CNT AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         COUNT(*) AS TEST_CNT ";
        $query .= "     FROM ";
        $query .= "         T_PERCENT ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= "     ) ";
        //指定されたテストの成績を抽出
        $query .= " , T_RECORD AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L2.CLASSCD, ";
            $query .= "         L2.SCHOOL_KIND, ";
            $query .= "         L2.CURRICULUM_CD, ";
        }
        $query .= "         L2.SUBCLASSCD, ";
        $query .= "         L1.SCHREGNO, ";
        $query .= "         VALUE(L3.SCORE_PASS, L1.SCORE) * L2.PERCENT AS BUNSI, ";
        $query .= "         L2.PERFECT AS BUNBO ";
        $query .= "     FROM ";
        $query .= "         T_PERCENT L2 ";
        $query .= "         LEFT JOIN RECORD_SCORE_DAT L1 ";
        $query .= "             ON  L1.YEAR         = L2.YEAR ";
        $query .= "             AND L1.SEMESTER     = L2.SEMESTER ";
        $query .= "             AND L1.TESTKINDCD   = L2.TESTKINDCD ";
        $query .= "             AND L1.TESTITEMCD   = L2.TESTITEMCD ";
        $query .= "             AND L1.SCORE_DIV    = CASE WHEN L2.TESTKINDCD = '99' THEN '00' ELSE '01' END ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L1.CLASSCD = L2.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        }
        $query .= "             AND L1.SUBCLASSCD   = L2.SUBCLASSCD ";
        $query .= "         LEFT JOIN SUPP_EXA_DAT L3 ";
        $query .= "             ON  L3.YEAR         = L1.YEAR ";
        $query .= "             AND L3.SEMESTER     = L1.SEMESTER ";
        $query .= "             AND L3.TESTKINDCD   = L1.TESTKINDCD ";
        $query .= "             AND L3.TESTITEMCD   = L1.TESTITEMCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L3.CLASSCD = L1.CLASSCD ";
            $query .= "             AND L3.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "             AND L3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= "             AND L3.SUBCLASSCD   = L1.SUBCLASSCD ";
        $query .= "             AND L3.SCHREGNO     = L1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         L2.TESTDIV  = '1' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L2.CLASSCD, ";
            $query .= "         L2.SCHOOL_KIND, ";
            $query .= "         L2.CURRICULUM_CD, ";
        }
        $query .= "         L2.SUBCLASSCD, ";
        $query .= "         L1.SCHREGNO, ";
        $query .= "         L1.SCORE * L2.PERCENT AS BUNSI, ";
        $query .= "         L2.PERFECT AS BUNBO ";
        $query .= "     FROM ";
        $query .= "         T_PERCENT L2 ";
        $query .= "         LEFT JOIN PROFICIENCY_DAT L1 ";
        $query .= "             ON  L1.YEAR             = L2.YEAR ";
        $query .= "             AND L1.SEMESTER         = L2.SEMESTER ";
        $query .= "             AND L1.PROFICIENCYDIV   = L2.PROFICIENCYDIV ";
        $query .= "             AND L1.PROFICIENCYCD    = L2.PROFICIENCYCD ";
        $query .= "             AND L1.PROFICIENCY_SUBCLASS_CD  = L2.PROFICIENCY_SUBCLASS_CD ";
        $query .= "     WHERE ";
        $query .= "             L2.TESTDIV  = '2' ";
        $query .= "         AND L1.SCHREGNO IN (SELECT ";
        $query .= "                                 T1.SCHREGNO ";
        $query .= "                             FROM ";
        $query .= "                                 CHAIR_STD_DAT T1, ";
        $query .= "                                 CHAIR_DAT T2 ";
        $query .= "                             WHERE ";
        $query .= "                                     T1.YEAR     = T2.YEAR ";
        $query .= "                                 AND T1.YEAR     = L2.YEAR ";
        $query .= "                                 AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "                                 AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "                                 AND T1.CHAIRCD  = T2.CHAIRCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T2.CLASSCD          = L2.CLASSCD ";
            $query .= "                                 AND T2.SCHOOL_KIND      = L2.SCHOOL_KIND ";
            $query .= "                                 AND T2.CURRICULUM_CD    = L2.CURRICULUM_CD ";
        }
        $query .= "                                 AND T2.SUBCLASSCD = L2.SUBCLASSCD) ";
        $query .= "     ) ";
        //指定されたテストの成績をカウント
        $query .= " , T_BUNSI_CNT AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         COUNT(BUNSI) AS BUNSI_CNT, ";
        $query .= "         SMALLINT(ROUND(FLOAT(SUM(BUNSI))/100,0)) AS VALUE ";
        $query .= "     FROM ";
        $query .= "         T_RECORD ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO ";
        $query .= "     ) ";
        //指定されたテストの成績が全てある場合、学期成績を算出
        $query .= " , T_VALUE AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L2.CLASSCD, ";
            $query .= "         L2.SCHOOL_KIND, ";
            $query .= "         L2.CURRICULUM_CD, ";
        }
        $query .= "         L2.SUBCLASSCD, ";
        $query .= "         L2.SCHREGNO, ";
        $query .= "         CASE WHEN L2.BUNSI_CNT = L3.TEST_CNT THEN L2.VALUE + VALUE(L4.ADJUST, 0) END AS VALUE ";
        $query .= "     FROM ";
        $query .= "         T_BUNSI_CNT L2 ";
        $query .= "         LEFT JOIN T_PERCENT_CNT L3 ON L3.SUBCLASSCD = L2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND L3.CLASSCD = L2.CLASSCD ";
            $query .= "                                 AND L3.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "                                 AND L3.CURRICULUM_CD = L2.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN T_ADJUST L4 ON L4.SUBCLASSCD = L2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND L4.CLASSCD = L2.CLASSCD ";
            $query .= "                                 AND L4.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "                                 AND L4.CURRICULUM_CD = L2.CURRICULUM_CD ";
        }
        $query .= "     ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     L1.VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN T_VALUE L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ( ";
        $query .= "         SELECT ";
        $query .= "             G1.COURSECD || G1.MAJORCD || G1.COURSECODE AS COURSE ";
        $query .= "         FROM ";
        $query .= "             COURSE_GROUP_CD_DAT G1 ";
        $query .= "         WHERE ";
        $query .= "                 G1.YEAR     = '{$year}' ";
        $query .= "             AND G1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "             AND G1.GROUP_CD = '{$model->field["GROUP_CD"]}' ";
        $query .= "         ) ";

        return $query;
    }

    //学年成績・学年評定・履修単位・修得単位を作成
    function insRecordScore($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjd219bQuery::getCalcSemValSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sem        = $model->field["SEMESTER"];
            $subCd      = $model->field["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $semValue   = $row["VALUE"];
            //学期成績を作成
            $query = knjd219bQuery::getDeleteRecordDat(CTRL_YEAR, $sem, "99", "00", "00", $subCd, $schno, $model);
            $db->query($query);
            $query = knjd219bQuery::getInsertRecordDat(CTRL_YEAR, $sem, "99", "00", "00", $subCd, $schno, $semValue, "", "", $model);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //RECORD_SCORE_DATのレコードを削除。
    function getDeleteRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subCd);
        }
        $query  = "";
        $query .= " DELETE FROM RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND TESTKINDCD = '{$kind}' ";
        $query .= "     AND TESTITEMCD = '{$item}' ";
        $query .= "     AND SCORE_DIV = '{$div}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND CLASSCD         = '".$subclass_array[0]."' ";
            $query .= "         AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
            $query .= "         AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
            $query .= "         AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$subCd}' ";
        }
        $query .= "     AND SCHREGNO = '{$schno}' ";
        return $query;
    }

    //RECORD_SCORE_DATのレコードを追加
    function getInsertRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subCd);
        }
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        $data["SCORE_DIV"][TEXT]    = $div;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]   = $subCd;
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["VALUE"][NUMBER]      = $gradValue;
        $data["COMP_CREDIT"][NUMBER]= $compCredit;
        $data["GET_CREDIT"][NUMBER] = $getCredit;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        return $query;
    }

    //Insert作成
    function insAssessLevel($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;

        //削除(合計点・調整点)
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_SEMVAL_ADJUST_DAT T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE        = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.GROUP_CD     = '{$model->field["GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND T1.SUBCLASSCD       = '{$model->field["SUBCLASSCD"]}' ";
        }
        $db->query($query);
        //追加
        $data = array();
        $data["YEAR"][TEXT]             = $year;
        $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["GROUP_CD"][TEXT]         = $model->field["GROUP_CD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["TOTAL"][NUMBER]          = $model->field["TOTAL".$i];
        $data["ADJUST"][NUMBER]         = $model->field["ADJUST".$i];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "SUBCLASS_SEMVAL_ADJUST_DAT");
        $db->query($query);

        //削除（テスト種別・割合・満点）
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_DAT T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE        = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.GROUP_CD     = '{$model->field["GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND T1.SUBCLASSCD       = '{$model->field["SUBCLASSCD"]}' ";
        }
        $db->query($query);
        //追加
        for ($i = 1; $i <= $model->field["TEST_COUNT"]; $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["GROUP_CD"][TEXT]         = $model->field["GROUP_CD"];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            $data["SEQ"][NUMBER]            = $i;
            $ar = explode("-", $model->field["TESTCD".$i]);
            $testdiv = $ar[0];
            $testcd  = $ar[1];
            $data["TESTDIV"][TEXT]          = $testdiv;
            $data["TESTKINDCD"][TEXT]       = ($testdiv == "1") ? substr($testcd,0,2) : "";
            $data["TESTITEMCD"][TEXT]       = ($testdiv == "1") ? substr($testcd,2) : "";
            $data["PROFICIENCYDIV"][TEXT]   = ($testdiv == "2") ? substr($testcd,0,2) : "";
            $data["PROFICIENCYCD"][TEXT]    = ($testdiv == "2") ? substr($testcd,2) : "";
            $data["PROFICIENCY_SUBCLASS_CD"][TEXT] = ($testdiv == "2") ? $model->field["PROFICIENCY_SUBCLASS_CD"] : "";
            $data["PERCENT"][NUMBER]        = $model->field["PERCENT".$i];
            $data["PERFECT"][NUMBER]        = $model->field["PERFECT".$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $query = Query::insertSQL($data, "SUBCLASS_SEMVAL_PERCENT_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
