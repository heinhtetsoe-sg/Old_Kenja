<?php

require_once('for_php7.php');

class knjd126mQuery extends Query {

    function getD086() {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D086' ";
        $query .= "     AND NAMECD2 = '1' ";

        return $query;
    }

    function getD029() {
        $query  = " SELECT ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D029' ";

        return $query;
    }

    //評定マスタ
    function getAssess($semester, $setCalcRes) {
        $setAssessCd = $semester == "9" ? "3" : "2";
        $query  = " SELECT ";
        $query .= "     ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     ASSESSCD = '{$setAssessCd}' ";
        $query .= "     AND {$setCalcRes} BETWEEN ASSESSLOW AND ASSESSHIGH ";

        return $query;
    }

    //評定マスタ(科目別)
    function getSubclassAssess($model, $grade, $totalcd, $subclasscd, $setCalcRes) {
        $query  = " SELECT ";
        $query .= "     ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     ASSESS_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND COURSECD || MAJORCD || COURSECODE = '{$totalcd}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclasscd}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$subclasscd}' ";
        }
        $query .= "     AND {$setCalcRes} BETWEEN ASSESSLOW AND ASSESSHIGH ";

        return $query;
    }

    function getJviewStatLevel($model, $setCalcRes, $semester) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        list($classCd, $schoolKind, $curriculum, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);
        $query  = " SELECT ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     '1' AS SORT ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND VIEWCD = '0000' ";
        $query .= "     AND DIV = '2' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND {$setCalcRes} BETWEEN ASSESSLOW AND ASSESSHIGH ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     '2' AS SORT ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD = '{$classCd}' ";
        $query .= "     AND SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$schoolKind}-00-000000' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND VIEWCD = '0000' ";
        $query .= "     AND DIV = '2' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND {$setCalcRes} BETWEEN ASSESSLOW AND ASSESSHIGH ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     '3' AS SORT ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '00-{$schoolKind}-00-000000' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND VIEWCD = '0000' ";
        $query .= "     AND DIV = '2' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND {$setCalcRes} BETWEEN ASSESSLOW AND ASSESSHIGH ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    /**********/
    /* コンボ */
    /**********/

    /**
     ** 年組
     **/
    function selectGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS AS VALUE, ";
        $query .= "     REGD_H.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G, ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = REGD_H.YEAR ";
        $query .= "     AND REGD_G.GRADE = REGD_H.GRADE ";
        $query .= "     AND REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function selectSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     REGD_G.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_G.GRADE = '{$grade}' ";

        return $query;
    }

    /**
     ** 教科名
     **/
    function selectSubclasscd($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD   = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD= T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.UNIT_ASSESSHIGH > 1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座取得
    function getChair($model) {
        //教育課程対応
        $query  = " WITH SCH_T AS (";
        $query .= knjd126mQuery::getSchList($model, "CHAIR");
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T1.YEAR       = T3.YEAR ";
        $query .= "     AND T1.SEMESTER   = T3.SEMESTER ";
        $query .= "     AND T1.CHAIRCD    = T3.CHAIRCD ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "     AND T2.SCHREGNO IN (SELECT SCH_T.SCHREGNO FROM SCH_T) ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "       AND T3.STAFFCD = '".STAFFCD."' ";
        }
        //教育課程対応
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**
     ** 処理学期
     **     1～3学期を表示する。
     **/
    function selectSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties['KNJD126M_SEME9_DISP'] != '1') {
            $query .= "     AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**************/
    /* ヘッダ表示 */
    /**************/

    //テスト単元・観点取得
    function getUnitViewList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEQ, ";          //key1
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SORT, ";             //1行目
        $query .= "     T1.UNIT_L_NAME, ";      //2行目
        $query .= "     T1.UNIT_TEST_DATE, ";      //2行目(日付)
        $query .= "     S1.VIEWCD, ";       //key2
        $query .= "     L1.VIEWABBV, ";         //3行目
        $query .= "     S1.UNIT_ASSESSHIGH, ";   //4行目
        $query .= "     S1.WEIGHTING AS WEIGHTING_MST, ";
        $query .= "     S1.WEIGHTING_CALC, ";
        $query .= "     S1.WEIGHTING_EXE ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT T1 ";
        $query .= "     INNER JOIN UNIT_TEST_INPUTSEQ_DAT S1 ";
        $query .= "              ON S1.YEAR         = T1.YEAR ";
        $query .= "             AND S1.GRADE        = T1.GRADE ";
        $query .= "             AND S1.HR_CLASS     = T1.HR_CLASS ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND S1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND S1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "             AND S1.SEQ          = T1.SEQ ";
        $query .= "             AND S1.UNIT_ASSESSHIGH > 1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST L1 ";
        $query .= "              ON L1.GRADE        = S1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD      = S1.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND  = S1.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD = S1.CURRICULUM_CD ";
        }
        $query .= "             AND L1.SUBCLASSCD   = S1.SUBCLASSCD ";
        $query .= "             AND L1.VIEWCD       = S1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.DATA_DIV = '2' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     AND T1.SEMESTER IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.SORT, ";
        $query .= "     S1.VIEWCD ";

        return $query;
    }

    //学期・観点取得
    function getSemeViewList($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = "";
        $query .= " WITH SUM_UNIT_ASSESSHIGH AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         S1.VIEWCD, ";
        $query .= "         SUM(S1.UNIT_ASSESSHIGH) AS UNIT_ASSESSHIGH ";
        $query .= "     FROM ";
        $query .= "         UNIT_TEST_DAT T1 ";
        $query .= "         INNER JOIN UNIT_TEST_INPUTSEQ_DAT S1 ";
        $query .= "                  ON S1.YEAR         = T1.YEAR ";
        $query .= "                 AND S1.GRADE        = T1.GRADE ";
        $query .= "                 AND S1.HR_CLASS     = T1.HR_CLASS ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND S1.CLASSCD      = T1.CLASSCD ";
            $query .= "                 AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "                 AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                 AND S1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "                 AND S1.SEQ          = T1.SEQ ";
        $query .= "                 AND S1.UNIT_ASSESSHIGH > 1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.DATA_DIV = '2' AND ";
        $query .= "         T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "         T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         S1.VIEWCD ";
        $query .= "     ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SEMESTER, ";         //key1
        $query .= "     T1.SEMESTERNAME, ";     //1行目
        $query .= "     L1.VIEWCD, ";           //key2
        $query .= "     L1.VIEWABBV, ";         //3行目
        $query .= "     S1.UNIT_ASSESSHIGH ";   //4行目
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT L0 ";
        $query .= "              ON L0.YEAR         = T1.YEAR ";
        $query .= "             AND L0.GRADE        = '{$grade}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L0.CLASSCD || '-' || L0.SCHOOL_KIND || '-' || L0.CURRICULUM_CD || '-' || L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "             AND L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST L1 ";
        $query .= "              ON L1.GRADE        = L0.GRADE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD      = L0.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND  = L0.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD = L0.CURRICULUM_CD ";
        }
        $query .= "             AND L1.SUBCLASSCD   = L0.SUBCLASSCD ";
        $query .= "             AND L1.VIEWCD       = L0.VIEWCD ";
        $query .= "     LEFT JOIN SUM_UNIT_ASSESSHIGH S1 ";
        $query .= "              ON S1.SEMESTER     = T1.SEMESTER ";
        $query .= "             AND S1.VIEWCD       = L1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER <> '9' ";
        if ($model->Properties["KNJD126M_SEME9_DISP"] == '1') {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     '9' AS SEMESTER, ";     //key1
            $query .= "     '年度末' AS SEMESTERNAME, ";     //1行目
            $query .= "     L1.VIEWCD, ";           //key2
            $query .= "     L1.VIEWABBV, ";         //3行目
            $query .= "     SUM(VALUE(S1.UNIT_ASSESSHIGH, 0)) AS UNIT_ASSESSHIGH ";   //4行目
            $query .= " FROM ";
            $query .= "     SEMESTER_MST T1 ";
            $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT L0 ";
            $query .= "              ON L0.YEAR         = T1.YEAR ";
            $query .= "             AND L0.GRADE        = '{$grade}' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "             AND L0.CLASSCD || '-' || L0.SCHOOL_KIND || '-' || L0.CURRICULUM_CD || '-' || L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            } else {
                $query .= "             AND L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            }
            $query .= "     INNER JOIN JVIEWNAME_GRADE_MST L1 ";
            $query .= "              ON L1.GRADE        = L0.GRADE ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "             AND L1.CLASSCD      = L0.CLASSCD ";
                $query .= "             AND L1.SCHOOL_KIND  = L0.SCHOOL_KIND ";
                $query .= "             AND L1.CURRICULUM_CD = L0.CURRICULUM_CD ";
            }
            $query .= "             AND L1.SUBCLASSCD   = L0.SUBCLASSCD ";
            $query .= "             AND L1.VIEWCD       = L0.VIEWCD ";
            $query .= "     LEFT JOIN SUM_UNIT_ASSESSHIGH S1 ";
            $query .= "              ON S1.SEMESTER     = T1.SEMESTER ";
            $query .= "             AND S1.VIEWCD       = L1.VIEWCD ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER <> '9' ";
            $query .= " GROUP BY ";
            $query .= "     L1.VIEWCD, ";
            $query .= "     L1.VIEWABBV ";
            $query .= " ORDER BY ";
            $query .= "     SEMESTER, ";
            $query .= "     VIEWCD ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     L1.VIEWCD ";
        }

        return $query;
    }

    /**************/
    /* リスト表示 */
    /**************/

    //生徒一覧取得
    function getSchList($model, $div = "") {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS TOTALCD, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        if ($div != "CHAIR") {
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STD_DAT CHAIR_STD ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = CHAIR_STD.YEAR ";
            $query .= "             AND CHAIR_STD.CHAIRCD = '{$model->field["CHAIRCD"]}' ";
            $query .= "             AND T1.SCHREGNO = CHAIR_STD.SCHREGNO ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //テスト単元得点データ取得
    function getScoreData($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SEQ, ";
        $query .= "     L1.VIEWCD, ";
        $query .= "     L1.SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN UNIT_TEST_SCORE_DAT L1 ";
        $query .= "              ON L1.YEAR     = T1.YEAR ";
        $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "             AND L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SEQ, ";
        $query .= "     L1.VIEWCD ";

        return $query;
    }

    //観点データ（DB）取得
    function getJviewstatRecord($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SEMESTER, ";
        $query .= "     L1.VIEWCD, ";
        $query .= "     L1.STATUS, ";
        $query .= "     L2.REMARK3 "; //到達度
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN JVIEWSTAT_RECORD_DAT L1 ";
        $query .= "              ON L1.YEAR     = T1.YEAR ";
        $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "             AND L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     LEFT JOIN JVIEWSTAT_RECORD_DETAIL_DAT L2 ";
        $query .= "              ON L2.YEAR         = L1.YEAR ";
        $query .= "             AND L2.SEMESTER     = L1.SEMESTER ";
        $query .= "             AND L2.SCHREGNO     = L1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L2.CLASSCD      = L1.CLASSCD ";
            $query .= "             AND L2.SCHOOL_KIND  = L1.SCHOOL_KIND ";
            $query .= "             AND L2.CURRICULUM_CD= L1.CURRICULUM_CD ";
        }
        $query .= "             AND L2.SUBCLASSCD   = L1.SUBCLASSCD ";
        $query .= "             AND L2.VIEWCD       = L1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SEMESTER, ";
        $query .= "     L1.VIEWCD ";

        return $query;
    }

    function getUpdateWeightQuery($db, $model) {
        //変数
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->field["SUBCLASSCD"]);
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        foreach($model->unitarry as $val) {
            if ($val == "") {
                continue;
            }
            $valwk = array();
            $valwk = explode("_", $val);
            $data["WEIGHTING_CALC"][NUMBER] = $model->unitvals[$valwk[2]][$valwk[0]][$valwk[1]];
            if ($model->cmd == "update") {
                $data["WEIGHTING"][NUMBER] = $model->unitvals[$valwk[2]][$valwk[0]][$valwk[1]];
                $data["WEIGHTING_EXE"][NUMBER] = $model->unitvals[$valwk[2]][$valwk[0]][$valwk[1]];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            
            $where  = " WHERE ";
            $where .= "   YEAR = '".CTRL_YEAR."' ";
            $where .= "   AND GRADE = '{$grade}' ";
            $where .= "   AND HR_CLASS = '{$hrClass}' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $where .= "   AND CLASSCD = '{$classcd}' ";
                $where .= "   AND SCHOOL_KIND = '{$school_kind}' ";
                $where .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
                $where .= "   AND SUBCLASSCD = '{$subclasscd}' ";
            } else {
                $where .= "   AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            }
            $where .= "   AND SEQ = {$valwk[1]} ";
            $where .= "   AND VIEWCD = '{$valwk[2]}' ";

            $query = Query::updateSQL($data, "UNIT_TEST_INPUTSEQ_DAT", $where);
            $db->query($query);
        }
    }
    /**
     ** 観点データの更新
     **/
    function getUpdateQuery($db, $model) {
        //変数
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->field["SUBCLASSCD"]);

        //削除
        //JVIEWSTAT_RECORD_DAT
        $query = knjd126mQuery::getDelJviewstatRecordDat($model);
        $db->query($query);
        //JVIEWSTAT_RECORD_DETAIL_DAT
        $query = knjd126mQuery::getDelJviewstatRecordDetailDat($model);
        $db->query($query);

        //観点データ（集計）を取得
        $updDataList = array();
        $query = knjd126mQuery::getCalcJviewstatRecord($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $updDataList[] = $row;
        }
        $result->free();

        if (get_count($updDataList) == 0) {
            return false;
        }

        //追加
        foreach ($updDataList as $key => $setArray) {
            //JVIEWSTAT_RECORD_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $setArray["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $setArray["SCHREGNO"];
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            $data["VIEWCD"][TEXT]           = $setArray["VIEWCD"];
            $data["STATUS"][TEXT]           = $setArray["STATUS"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DAT");
            $db->query($query);
            //JVIEWSTAT_RECORD_DETAIL_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $setArray["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $setArray["SCHREGNO"];
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            $data["VIEWCD"][TEXT]           = $setArray["VIEWCD"];
            $data["REMARK1"][NUMBER]        = $setArray["REMARK1"];
            $data["REMARK2"][NUMBER]        = $setArray["REMARK2"];
            $data["REMARK3"][NUMBER]        = $setArray["REMARK3"];
            $data["REMARK4"][NUMBER]        = $setArray["REMARK4"];
            $data["REMARK5"][NUMBER]        = $setArray["REMARK5"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DETAIL_DAT");
            $db->query($query);
        }

        foreach ($model->totalSchregArray as $schregNo => $semeVal) {
            $query  = " DELETE ";
            $query .= " FROM ";
            $query .= "     RECORD_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND TESTKINDCD || TESTITEMCD || SCORE_DIV IN ('990008', '990009') ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            } else {
                $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            }
            $query .= "     AND SCHREGNO = '{$schregNo}' ";
            $db->query($query);

            foreach ($semeVal as $semester => $val) {
                $data = array();
                if ($semester == "9") {
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]         = $semester;
                    $data["TESTKINDCD"][TEXT]       = "99";
                    $data["TESTITEMCD"][TEXT]       = "00";
                    $data["SCORE_DIV"][TEXT]        = "09";
                    if ($model->Properties["useCurriculumcd"] == "1") {
                        $data["CLASSCD"][TEXT]          = $classcd;
                        $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                        $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                        $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                    } else {
                        $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
                    }
                    $data["SCHREGNO"][TEXT]         = $schregNo;
                    $data["SCORE"][NUMBER]          = $val["MARK"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD ;
                    $data["UPDATED"][FUNC]          = "sysdate()";
                    $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
                    $db->query($query);
                } else {
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]         = $semester;
                    $data["TESTKINDCD"][TEXT]       = "99";
                    $data["TESTITEMCD"][TEXT]       = "00";
                    $data["SCORE_DIV"][TEXT]        = "08";
                    if ($model->Properties["useCurriculumcd"] == "1") {
                        $data["CLASSCD"][TEXT]          = $classcd;
                        $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                        $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                        $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                    } else {
                        $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
                    }
                    $data["SCHREGNO"][TEXT]         = $schregNo;
                    $data["SCORE"][NUMBER]          = $val["MARK"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD ;
                    $data["UPDATED"][FUNC]          = "sysdate()";
                    $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
                    $db->query($query);
                }
            }
        }

        return true;
    }

    /**
     ** JVIEWSTAT_RECORD_DATの削除
     **/
    function getDelJviewstatRecordDat($model) {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST L1 ";
        $query .= "                      ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     ) ";

        return $query;
    }

    /**
     ** JVIEWSTAT_RECORD_DETAIL_DATの削除
     **/
    function getDelJviewstatRecordDetailDat($model) {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST L1 ";
        $query .= "                      ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     ) ";

        return $query;
    }

    /**
     ** JVIEWSTAT_RECORD_DAT、JVIEWSTAT_RECORD_DETAIL_DATの追加用
     **/
    //観点データ（集計）を取得
    function getCalcJviewstatRecord($model) {
        $regdSeme = $model->field["SEMESTER"] == '9' ? CTRL_SEMESTER : $model->field["SEMESTER"];
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = "";
        $query .= " WITH T_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->field["SEMESTER"] != '9') {
            $query .= "         L0.SEMESTER, ";
        } else {
            $query .= "         '9' AS SEMESTER, ";
        }
        $query .= "         L0.SEQ, ";
        $query .= "         L1.VIEWCD, ";
        $query .= "         L1.SCORE, ";
        $query .= "         L2.UNIT_ASSESSHIGH, ";
        $query .= "         L2.WEIGHTING_CALC, ";
        $query .= "         L2.WEIGHTING_EXE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "                  ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         INNER JOIN UNIT_TEST_DAT L0 ";
        $query .= "                  ON L0.YEAR     = T1.YEAR ";
        $query .= "                 AND L0.DATA_DIV = '2' ";
        $query .= "                 AND L0.GRADE || '-' || L0.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND L0.CLASSCD || '-' || L0.SCHOOL_KIND || '-' || L0.CURRICULUM_CD || '-' || L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "                 AND L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        if ($model->field["SEMESTER"] != '9') {
            $query .= "                 AND L0.SEMESTER = '{$model->field["SEMESTER"]}' ";
        }
        $query .= "         INNER JOIN UNIT_TEST_INPUTSEQ_DAT L2 ";
        $query .= "                  ON L2.YEAR     = T1.YEAR ";
        $query .= "                 AND L2.GRADE || '-' || L2.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "                 AND L2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "                 AND L2.SEQ = L0.SEQ ";
        $query .= "         INNER JOIN UNIT_TEST_SCORE_DAT L1 ";
        $query .= "                  ON L1.YEAR     = T1.YEAR ";
        $query .= "                 AND L1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "                 AND L1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "                 AND L1.SEQ = L2.SEQ ";
        $query .= "                 AND L1.VIEWCD = L2.VIEWCD ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".$regdSeme."' ";
        $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        $query .= " ), T_TOUTATUDO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.VIEWCD, ";
        $query .= "         SUM(T1.SCORE) AS SCORE_TOTAL, ";
        $query .= "         SUM(T1.UNIT_ASSESSHIGH) AS UNIT_ASSESSHIGH_TOTAL, ";
        if ($model->cmd == "update") {
            if ($model->d086 == "1") {
                $query .= "         DECIMAL(ROUND(SUM(FLOAT(T1.SCORE) * FLOAT(T1.WEIGHTING_EXE) / 100.0), 0),4,0) AS WEIGHT_SCORE, ";
                $query .= "         DECIMAL(ROUND(SUM(FLOAT(T1.UNIT_ASSESSHIGH) * FLOAT(T1.WEIGHTING_EXE) / 100.0), 0),4,0) AS WEIGHT_ASSESSHIGH ";
            } else {
                $query .= "         DECIMAL(ROUND(SUM(FLOAT(T1.SCORE) * FLOAT(T1.WEIGHTING_EXE) / 100.0), 1),4,1) AS WEIGHT_SCORE, ";
                $query .= "         DECIMAL(ROUND(SUM(FLOAT(T1.UNIT_ASSESSHIGH) * FLOAT(T1.WEIGHTING_EXE) / 100.0), 1),4,1) AS WEIGHT_ASSESSHIGH ";
            }
        } else {
            if ($model->d086 == "1") {
                $query .= "         DECIMAL(ROUND(SUM(FLOAT(T1.SCORE) * FLOAT(T1.WEIGHTING_CALC) / 100.0), 0),4,0) AS WEIGHT_SCORE, ";
                $query .= "         DECIMAL(ROUND(SUM(FLOAT(T1.UNIT_ASSESSHIGH) * FLOAT(T1.WEIGHTING_CALC) / 100.0), 0),4,0) AS WEIGHT_ASSESSHIGH ";
            } else {
                $query .= "         DECIMAL(ROUND(SUM(FLOAT(T1.SCORE) * FLOAT(T1.WEIGHTING_CALC) / 100.0), 1),4,1) AS WEIGHT_SCORE, ";
                $query .= "         DECIMAL(ROUND(SUM(FLOAT(T1.UNIT_ASSESSHIGH) * FLOAT(T1.WEIGHTING_CALC) / 100.0), 1),4,1) AS WEIGHT_ASSESSHIGH ";
            }
        }
        $query .= "     FROM ";
        $query .= "         T_SCORE T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.VIEWCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.ASSESSMARK AS STATUS, ";
        $query .= "     T1.SCORE_TOTAL AS REMARK1, ";
        $query .= "     T1.UNIT_ASSESSHIGH_TOTAL AS REMARK2, ";
        if ($model->d086 == "1") {
            $query .= "     DECIMAL(ROUND(T1.WEIGHT_SCORE / T1.WEIGHT_ASSESSHIGH, 3) * 100, 4, 0) AS REMARK3, ";
        } else {
            $query .= "     DECIMAL(ROUND(T1.WEIGHT_SCORE / T1.WEIGHT_ASSESSHIGH, 3) * 100, 4, 1) AS REMARK3, ";
        }
        $query .= "     T1.WEIGHT_SCORE AS REMARK4, ";
        $query .= "     T1.WEIGHT_ASSESSHIGH AS REMARK5 ";
        $query .= " FROM ";
        $query .= "     T_TOUTATUDO T1 ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_SEMES_MST T2 ";
        $query .= "              ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "             AND T2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        $query .= "             AND T2.DIV = '1' ";
        $query .= "             AND T2.GRADE = '{$grade}' ";
        if ($model->d086 == "1") {
            $query .= "             AND ROUND(DECIMAL(ROUND(T1.WEIGHT_SCORE / T1.WEIGHT_ASSESSHIGH, 3) * 100, 4, 0), 0) BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ";
        } else {
            $query .= "             AND ROUND(DECIMAL(ROUND(T1.WEIGHT_SCORE / T1.WEIGHT_ASSESSHIGH, 3) * 100, 4, 1), 0) BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

}
?>
