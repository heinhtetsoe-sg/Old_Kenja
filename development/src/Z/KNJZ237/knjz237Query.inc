<?php

require_once('for_php7.php');

class knjz237Query extends Query {

    //リスト内情報取得
    function getListdata($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }         
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.PASS_SCORE, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L2.COURSECODENAME, ";
        $query .= "     L3.MAJORNAME, ";
        $query .= "     L4.COURSENAME ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST   L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                 AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                 AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }         
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L2 ON L2.COURSECODE = T1.COURSECODE ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST      L3 ON L3.COURSECD = T1.COURSECD ";
        $query .= "                      AND L3.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST     L4 ON L4.COURSECD = T1.COURSECD ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND TESTKINDCD || ':' || TESTITEMCD = '{$model->field["TEST"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        return $query;
    }

    //学期名の取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER";

        return $query;
    }

    //学期名取得
    function getSemesterName($semester) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }

    //テストコンボ
    function getTest($table, $semester) {
        $query  = " SELECT ";
        if ($table == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ' ' || TESTITEMNAME AS LABEL, ";
        } else {
            $query .= "     TESTKINDCD || TESTITEMCD || ' ' || TESTITEMNAME AS LABEL, ";
        }
        $query .= "     TESTKINDCD || ':' || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND (TESTKINDCD = '01' OR TESTKINDCD = '02' OR TESTKINDCD = '99') ";
        if ($table == "TESTITEM_MST_COUNTFLG_NEW" || $table == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     AND SEMESTER = '{$semester}' ";
        }
        if ($table == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     AND SCORE_DIV = '01' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";

        return $query;
    }

    //テスト名取得
    function getTestName($table, $test, $semester) {
        $query  = " SELECT ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND TESTKINDCD || ':' || TESTITEMCD = '{$test}' ";
        if ($table == "TESTITEM_MST_COUNTFLG_NEW" || $table == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     AND SEMESTER = '{$semester}' ";
        }
        if ($table == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     AND SCORE_DIV = '01' ";
        }

        return $query;
    }

    //科目コンボ
    function getSubclass($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年コンボ
    function getGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE || '学年' AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //課程コンボ
    function getCourse() {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || ' ' || L1.COURSENAME AS LABEL, ";
        $query .= "     T1.COURSECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSE_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学科コンボ
    function getMajor($coursece) {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ' ' || L1.MAJORNAME AS LABEL, ";
        $query .= "     T1.COURSECD || ':' || T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAJOR_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= "                 AND L1.MAJORCD  = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.COURSECD = '{$coursece}' ";

        return $query;
    }

    //コースコンボ
    function getCoursecode() {
        $query  = " SELECT ";
        $query .= "     T1.COURSECODE || ' ' || L1.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSECODE_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L1 ON L1.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //前年度からのコピーの件数カウント
    function getCopyCountQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "      COUNT(*) ";
        $query .= "  FROM ";
        $query .= "      PERFECT_RECORD_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "     not exists ( "; //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM PERFECT_RECORD_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                 T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.SEMESTER   = T0.SEMESTER AND ";
        $query .= "                 T1.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                 T1.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                 T1.CLASSCD    = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                 T1.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                 T1.SUBCLASSCD = T0.SUBCLASSCD AND ";
        $query .= "                 T1.DIV        = T0.DIV AND ";
        $query .= "                 T1.GRADE      = T0.GRADE AND ";
        $query .= "                 T1.COURSECD   = T0.COURSECD AND ";
        $query .= "                 T1.MAJORCD    = T0.MAJORCD AND ";
        $query .= "                 T1.COURSECODE = T0.COURSECODE ";
        $query .= "                 )  AND ";
        $query .= "    (( ";
/******************************************************************/
/* 今年度にない教科や課程、学科、コース、学年のものはコピーしない */
/******************************************************************/
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSE_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.COURSECD = T0.COURSECD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MAJOR_YDAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T3.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T3.COURSECD = T0.COURSECD AND ";
        $query .= "                 T3.MAJORCD  = T0.MAJORCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSECODE_YDAT T4 ";
        $query .= "             WHERE ";
        $query .= "                 T4.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                 T4.COURSECODE = T0.COURSECODE ";
        $query .= "             ) ";
        $query .= "     ) OR T0.COURSECD = '0' ) ";
        $query .= "     AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_YDAT T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T5.CLASSCD       = T0.CLASSCD AND ";
            $query .= "                 T5.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                 T5.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                 T5.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= "             ) ";
        $query .= "     AND ";
        $query .= "     (EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT T6 ";
        $query .= "             WHERE ";
        $query .= "                 T6.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "                 T6.GRADE = T0.GRADE ";
        $query .= "             ) ";
        $query .= "      OR T0.GRADE = '00') ";
        $query .= "     AND ";
/*********************************************************************/
/* 今年度にすでに設定されている範囲のものはコピーしない              */
/* たとえば区分1がすでに存在していたら重複ではなくても区分2も区分3も */
/* 区分1とかぶってしまうためコピー対象ではない。                     */
/*********************************************************************/
        $query .= "     ((T0.DIV = '01' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T7 ";
        $query .= "                 WHERE ";
        $query .= "                     T7.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                     T7.SEMESTER   = T0.SEMESTER AND ";
        $query .= "                     T7.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                     T7.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                     T7.CLASSCD    = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T7.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                     T7.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                     T7.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '02' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER   = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                     T8.CLASSCD    = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                     T8.SUBCLASSCD = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE     = T0.GRADE OR ";
        $query .= "                     T8.GRADE      = '00') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '03' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER   = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                     T8.CLASSCD    = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                     T8.SUBCLASSCD = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE     = T0.GRADE OR ";
        $query .= "                     T8.GRADE      = '00') AND ";
        $query .= "                     ((T8.COURSECD = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD    = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE = T0.COURSECODE) OR ";
        $query .= "                     T8.COURSECD   = '0') ";
        $query .= "                 ))) ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO PERFECT_RECORD_DAT( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }         
        $query .= "     SUBCLASSCD, ";
        $query .= "     DIV, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     PERFECT, ";
        $query .= "     PASS_SCORE, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      SEMESTER, ";
        $query .= "      TESTKINDCD, ";
        $query .= "      TESTITEMCD, ";
        $query .= "      CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }         
        $query .= "      SUBCLASSCD, ";
        $query .= "      DIV, ";
        $query .= "      GRADE, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      COURSECODE, ";
        $query .= "      PERFECT, ";
        $query .= "      PASS_SCORE, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      PERFECT_RECORD_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "     not exists ( "; //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM PERFECT_RECORD_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                 T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.SEMESTER   = T0.SEMESTER AND ";
        $query .= "                 T1.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                 T1.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                 T1.CLASSCD    = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                 T1.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                 T1.SUBCLASSCD = T0.SUBCLASSCD AND ";
        $query .= "                 T1.DIV        = T0.DIV AND ";
        $query .= "                 T1.GRADE      = T0.GRADE AND ";
        $query .= "                 T1.COURSECD   = T0.COURSECD AND ";
        $query .= "                 T1.MAJORCD    = T0.MAJORCD AND ";
        $query .= "                 T1.COURSECODE = T0.COURSECODE ";
        $query .= "                 )  AND ";
        $query .= "    (( ";
/******************************************************************/
/* 今年度にない教科や課程、学科、コース、学年のものはコピーしない */
/******************************************************************/
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSE_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.COURSECD = T0.COURSECD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MAJOR_YDAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T3.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T3.COURSECD = T0.COURSECD AND ";
        $query .= "                 T3.MAJORCD  = T0.MAJORCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSECODE_YDAT T4 ";
        $query .= "             WHERE ";
        $query .= "                 T4.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                 T4.COURSECODE = T0.COURSECODE ";
        $query .= "             ) ";
        $query .= "     ) OR T0.COURSECD = '0' ) ";
        $query .= "     AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_YDAT T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T5.CLASSCD       = T0.CLASSCD AND ";
            $query .= "                 T5.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                 T5.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                 T5.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= "             ) ";
        $query .= "     AND ";
        $query .= "     (EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT T6 ";
        $query .= "             WHERE ";
        $query .= "                 T6.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "                 T6.GRADE = T0.GRADE ";
        $query .= "             ) ";
        $query .= "      OR T0.GRADE = '00') ";
        $query .= "     AND ";
/*********************************************************************/
/* 今年度にすでに設定されている範囲のものはコピーしない              */
/* たとえば区分1がすでに存在していたら重複ではなくても区分2も区分3も */
/* 区分1とかぶってしまうためコピー対象ではない。                     */
/*********************************************************************/
        $query .= "     ((T0.DIV = '01' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T7 ";
        $query .= "                 WHERE ";
        $query .= "                     T7.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                     T7.SEMESTER   = T0.SEMESTER AND ";
        $query .= "                     T7.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                     T7.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                     T7.CLASSCD    = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T7.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                     T7.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                     T7.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '02' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER   = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                     T8.CLASSCD    = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                     T8.SUBCLASSCD = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE     = T0.GRADE OR ";
        $query .= "                     T8.GRADE      = '00') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '03' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER   = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                     T8.CLASSCD    = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.SCHOOL_KIND   = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }         
        $query .= "                     T8.SUBCLASSCD = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE     = T0.GRADE OR ";
        $query .= "                     T8.GRADE      = '00') AND ";
        $query .= "                     ((T8.COURSECD = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD    = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE = T0.COURSECODE) OR ";
        $query .= "                     T8.COURSECD   = '0') ";
        $query .= "                 ))) ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //存在チェック(区分1)
    function getExistsDiv1($model) {
        list($testkindcd, $testitemcd) = explode(":", $model->field["TEST"]);
        $classcd = substr($model->field["SUBCLASSCD"], 0, 2);

        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$testkindcd}' ";
        $query .= "     AND TESTITEMCD = '{$testitemcd}' ";
        $query .= "     AND CLASSCD    = '{$classcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = substr('{$model->field["SUBCLASSCD"]}',7,6) ";
        } else {         
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        return $query;
    }

    //存在チェック(区分2)
    function getExistsDiv2($model) {
        list($testkindcd, $testitemcd) = explode(":", $model->field["TEST"]);
        $classcd = substr($model->field["SUBCLASSCD"], 0, 2);

        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$testkindcd}' ";
        $query .= "     AND TESTITEMCD = '{$testitemcd}' ";
        $query .= "     AND CLASSCD    = '{$classcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = substr('{$model->field["SUBCLASSCD"]}',7,6) ";
        } else {         
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND (GRADE     = '{$model->field["GRADE"]}' ";
        $query .= "     OR  GRADE      = '00') ";

        return $query;
    }

    //存在チェック(区分3)
    function getExistsDiv3($model) {
        list($testkindcd, $testitemcd) = explode(":", $model->field["TEST"]);
        $classcd = substr($model->field["SUBCLASSCD"], 0, 2);
        list($dummy, $majorcd) = explode(":", $model->field["MAJORCD"]); //dummyは使わない

        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$testkindcd}' ";
        $query .= "     AND TESTITEMCD = '{$testitemcd}' ";
        $query .= "     AND CLASSCD    = '{$classcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = substr('{$model->field["SUBCLASSCD"]}',7,6) ";
        } else {         
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND (GRADE     = '{$model->field["GRADE"]}' ";
        $query .= "     OR  GRADE      = '00') ";
        $query .= "     AND ((COURSECD = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD    = '{$majorcd}' ";
        $query .= "     AND COURSECODE = '{$model->field["COURSECODE"]}') ";
        $query .= "     OR  COURSECD   = '0') ";

        return $query;
    }

    //存在チェック
    function getExists($model) {
        $db = Query::dbCheckOut();
        list($testkindcd, $testitemcd) = explode(":", $model->field["TEST"]);
        $classcd = substr($model->field["SUBCLASSCD"], 0, 2);
        $div = sprintf("%02d", $model->field["DIV"]);
        list($dummy, $majorcd) = explode(":", $model->field["MAJORCD"]); //dummyは使わない
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $suclasscd = substr($model->field["SUBCLASSCD"], 7, 6);
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$testkindcd}' ";
        $query .= "     AND TESTITEMCD = '{$testitemcd}' ";
        $query .= "     AND CLASSCD    = '{$classcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = '{$suclasscd}' ";
        } else {         
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND DIV        = '{$div}' ";
        $query .= "     AND GRADE      = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD   = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD    = '{$majorcd}' ";
        $query .= "     AND COURSECODE = '{$model->field["COURSECODE"]}' ";

        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);
        return $ret_val;
    }

    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model) {
        list($testkindcd, $testitemcd) = explode(":", $model->field["TEST"]);
        $classcd = substr($model->field["SUBCLASSCD"], 0, 2);
        $div = sprintf("%02d", $model->field["DIV"]);
        list($dummy, $majorcd) = explode(":", $model->field["MAJORCD"]); //dummyは使わない
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $suclasscd = substr($model->field["SUBCLASSCD"], 7, 6);
        }


        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query .= " DELETE FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$testkindcd}' ";
        $query .= "     AND TESTITEMCD = '{$testitemcd}' ";
        $query .= "     AND CLASSCD    = '{$classcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = '{$suclasscd}' ";
        } else {         
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND DIV        = '{$div}' ";
        $query .= "     AND GRADE      = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD   = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD    = '{$majorcd}' ";
        $query .= "     AND COURSECODE = '{$model->field["COURSECODE"]}' ";

        $db->query($query);

        $query = "";

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]   = $testkindcd;
        $data["TESTITEMCD"][TEXT]   = $testitemcd;
        $data["CLASSCD"][TEXT]      = $classcd;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]    = $model->field["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]  = $model->field["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]     = $suclasscd;
        } else {         
            $data["SUBCLASSCD"][TEXT]     = $model->field["SUBCLASSCD"];
        }
        $data["DIV"][TEXT]          = $div;
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["COURSECD"][TEXT]     = $model->field["COURSECD"];
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
        $data["PERFECT"][NUMBER]    = $model->field["PERFECT"];
        $data["PASS_SCORE"][NUMBER] = $model->field["PASS_SCORE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "PERFECT_RECORD_DAT");

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
        list($testkindcd, $testitemcd) = explode(":", $model->field["TEST"]);
        $classcd = substr($model->field["SUBCLASSCD"], 0, 2);
        $div = sprintf("%02d", $model->field["DIV"]);
        list($dummy, $majorcd) = explode(":", $model->field["MAJORCD"]); //dummyは使わない
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $suclasscd = substr($model->field["SUBCLASSCD"], 7, 6);
        }

        $db = Query::dbCheckOut();

        $query .= " DELETE FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$testkindcd}' ";
        $query .= "     AND TESTITEMCD = '{$testitemcd}' ";
        $query .= "     AND CLASSCD    = '{$classcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = '$suclasscd' ";
        } else {         
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND DIV        = '{$div}' ";
        $query .= "     AND GRADE      = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD   = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD    = '{$majorcd}' ";
        $query .= "     AND COURSECODE = '{$model->field["COURSECODE"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
