<?php

require_once('for_php7.php');

class knjh540aQuery extends Query {

    function getSemester($div = "") {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($div == "") {
            $query .= "     AND SEMESTER < '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$div."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getProficiencyDiv() {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getGrade($model) {
        $query .= " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト内情報取得
    function getListdata($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.PASS_SCORE, ";
        $query .= "     T1.WEIGHTING, ";
        $query .= "     L1.SUBCLASS_NAME, ";
        $query .= "     L2.COURSENAME, ";
        $query .= "     L3.MAJORNAME, ";
        $query .= "     L4.COURSECODENAME, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || '：' || L3.MAJORNAME || '　' || L4.COURSECODENAME AS SETNAME";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     PROFICIENCY_SUBCLASS_MST L1 ON L1.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L2 ON L2.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST L3 ON  L3.COURSECD = T1.COURSECD ";
        $query .= "                  AND L3.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L4 ON L4.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND T1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.PASS_SCORE, ";
        $query .= "     T1.WEIGHTING ";

        return $query;
    }

    //実力コード年間平均
    function getProficiencyAndAvg($model) {
        $div = $model->field["PROFICIENCYDIV"] ? $model->field["PROFICIENCYDIV"] : $model->proficiencydiv;
        $query  = " WITH PROFICIENCY_AND_AVG AS ( ";
        $query .= " SELECT ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     PROFICIENCYNAME1 ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '".$div."' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '9999' AS PROFICIENCYCD, ";
        $query .= "     L1.NAME1 || '年間平均' AS PROFICIENCYNAME1 ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H508' AND L1.NAMECD2 = T1.PROFICIENCYDIV ";
        $query .= " WHERE ";
        $query .= "     T1.PROFICIENCYDIV = '".$div."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     L1.NAME1 ";
        $query .= " ) ";

        return $query;
    }

    //実力コードコンボ
    function getProficiencycd($model) {
        $query  = knjh540aQuery::getProficiencyAndAvg($model);
        $query .= " SELECT ";
        $query .= "     PROFICIENCYCD AS VALUE, ";
        $query .= "     PROFICIENCYCD || ':' || PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_AND_AVG ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実力名取得
    function getProficiencyName($model) {
        $query  = knjh540aQuery::getProficiencyAndAvg($model);
        $query .= " SELECT ";
        $query .= "     PROFICIENCYNAME1 ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_AND_AVG ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYCD = '{$model->proficiencycd}' ";

        return $query;
    }

    //科目コンボ
    function getProficiencySubclassCd() {
        $query  = " SELECT ";
        $query .= "     PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD || ':' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     PROFICIENCY_SUBCLASS_CD ";

        return $query;
    }

    //課程コンボ
    function getCourse() {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || ':' || L1.COURSENAME AS LABEL, ";
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
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || L1.MAJORNAME AS LABEL, ";
        $query .= "     T1.MAJORCD AS VALUE ";
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

    //コースグループコンボ
    function getCourseGroup($grade) {
        $query  = " SELECT ";
        $query .= "     GROUP_CD || ':' || GROUP_NAME AS LABEL, ";
        $query .= "     GROUP_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //コースコンボ
    function getCoursecode() {
        $query  = " SELECT ";
        $query .= "     T1.COURSECODE || ':' || L1.COURSECODENAME AS LABEL, ";
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
        $query .= "      PROFICIENCY_PERFECT_COURSE_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "     not exists ( "; //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                 T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                 T1.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                 T1.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                 T1.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD AND ";
        $query .= "                 T1.DIV              = T0.DIV AND ";
        $query .= "                 T1.GRADE            = T0.GRADE AND ";
        $query .= "                 T1.COURSECD         = T0.COURSECD AND ";
        $query .= "                 T1.MAJORCD          = T0.MAJORCD AND ";
        $query .= "                 T1.COURSECODE       = T0.COURSECODE ";
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
        $query .= "                 PROFICIENCY_SUBCLASS_MST T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD ";
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
        $query .= "                     PROFICIENCY_PERFECT_COURSE_DAT T7 ";
        $query .= "                 WHERE ";
        $query .= "                     T7.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T7.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                     T7.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                     T7.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                     T7.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '02' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PROFICIENCY_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                     T8.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                     T8.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                     T8.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '03' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PROFICIENCY_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                     T8.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                     T8.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                     T8.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') AND ";
        $query .= "                     ((T8.COURSECD       = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD          = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE       = T0.COURSECODE) OR ";
        $query .= "                     T8.COURSECD         = '0') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '04' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PROFICIENCY_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                     T8.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                     T8.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                     T8.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') AND ";
        $query .= "                     (T8.MAJORCD         = T0.MAJORCD OR ";
        $query .= "                     T8.MAJORCD          = '000') ";
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

        $query  = " INSERT INTO PROFICIENCY_PERFECT_COURSE_DAT( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     DIV, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     PERFECT, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      SEMESTER, ";
        $query .= "      PROFICIENCYDIV, ";
        $query .= "      PROFICIENCYCD, ";
        $query .= "      PROFICIENCY_SUBCLASS_CD, ";
        $query .= "      DIV, ";
        $query .= "      GRADE, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      COURSECODE, ";
        $query .= "      PERFECT, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      PROFICIENCY_PERFECT_COURSE_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "     not exists ( "; //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                 T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                 T1.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                 T1.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                 T1.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD AND ";
        $query .= "                 T1.DIV              = T0.DIV AND ";
        $query .= "                 T1.GRADE            = T0.GRADE AND ";
        $query .= "                 T1.COURSECD         = T0.COURSECD AND ";
        $query .= "                 T1.MAJORCD          = T0.MAJORCD AND ";
        $query .= "                 T1.COURSECODE       = T0.COURSECODE ";
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
        $query .= "                 PROFICIENCY_SUBCLASS_MST T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD ";
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
        $query .= "                     PROFICIENCY_PERFECT_COURSE_DAT T7 ";
        $query .= "                 WHERE ";
        $query .= "                     T7.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T7.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                     T7.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                     T7.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                     T7.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '02' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PROFICIENCY_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                     T8.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                     T8.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                     T8.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '03' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PROFICIENCY_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                     T8.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                     T8.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                     T8.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') AND ";
        $query .= "                     ((T8.COURSECD       = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD          = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE       = T0.COURSECODE) OR ";
        $query .= "                     T8.COURSECD         = '0') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '04' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PROFICIENCY_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER         = T0.SEMESTER AND ";
        $query .= "                     T8.PROFICIENCYDIV   = T0.PROFICIENCYDIV AND ";
        $query .= "                     T8.PROFICIENCYCD    = T0.PROFICIENCYCD AND ";
        $query .= "                     T8.PROFICIENCY_SUBCLASS_CD = T0.PROFICIENCY_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') AND ";
        $query .= "                     (T8.MAJORCD          = T0.MAJORCD OR ";
        $query .= "                     T8.MAJORCD         = '000') ";
        $query .= "                 ))) ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //存在チェック
    function getExists($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PERFECT_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR                     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER                 = '{$model->semester}' ";
        $query .= "     AND PROFICIENCYDIV           = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND PROFICIENCYCD            = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     AND PROFICIENCY_SUBCLASS_CD  = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' ";
        $query .= "     AND DIV                      = '{$model->field["DIV"]}' ";
        $query .= "     AND GRADE                    = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD                 = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD                  = '{$model->field["MAJORCD"]}' ";
        $query .= "     AND COURSECODE               = '{$model->field["COURSECODE"]}' ";

        return $query;
    }

    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["DIV"] == '01') {
            $query  = " DELETE FROM ";
            $query .= "     PROFICIENCY_PERFECT_COURSE_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR             = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER         = '{$model->semester}' ";
            $query .= "     AND PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
            $query .= "     AND PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
            $query .= "     AND PROFICIENCY_SUBCLASS_CD = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' ";

            $db->query($query);
        } else {
            $query  = " DELETE FROM ";
            $query .= "     PROFICIENCY_PERFECT_COURSE_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR             = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER         = '{$model->semester}' ";
            $query .= "     AND PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
            $query .= "     AND PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
            $query .= "     AND PROFICIENCY_SUBCLASS_CD = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' ";
            $query .= "     AND DIV = '01' ";

            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     PROFICIENCY_PERFECT_COURSE_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR             = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER         = '{$model->semester}' ";
            $query .= "     AND PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
            $query .= "     AND PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
            $query .= "     AND PROFICIENCY_SUBCLASS_CD = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' ";
            $query .= "     AND DIV             != '{$model->field["DIV"]}' ";
            $query .= "     AND GRADE            = '{$model->field["GRADE"]}' ";

            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     PROFICIENCY_PERFECT_COURSE_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR                    = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER                = '{$model->semester}' ";
            $query .= "     AND PROFICIENCYDIV          = '{$model->field["PROFICIENCYDIV"]}' ";
            $query .= "     AND PROFICIENCYCD           = '{$model->field["PROFICIENCYCD"]}' ";
            $query .= "     AND PROFICIENCY_SUBCLASS_CD = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' ";
            $query .= "     AND DIV                     = '{$model->field["DIV"]}' ";
            $query .= "     AND GRADE                   = '{$model->field["GRADE"]}' ";
            $query .= "     AND COURSECD                = '{$model->field["COURSECD"]}' ";
            $query .= "     AND MAJORCD                 = '{$model->field["MAJORCD"]}' ";
            $query .= "     AND COURSECODE              = '{$model->field["COURSECODE"]}' ";

            $db->query($query);
        }

        $query = "";
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["PROFICIENCYDIV"][TEXT]   = $model->field["PROFICIENCYDIV"];
        $data["PROFICIENCYCD"][TEXT]    = $model->field["PROFICIENCYCD"];
        $data["PROFICIENCY_SUBCLASS_CD"][TEXT] = $model->field["PROFICIENCY_SUBCLASS_CD"];
        $data["DIV"][TEXT]              = $model->field["DIV"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["COURSECD"][TEXT]         = $model->field["COURSECD"];
        $data["MAJORCD"][TEXT]          = $model->field["MAJORCD"];
        $data["COURSECODE"][TEXT]       = $model->field["COURSECODE"];

        $data["PERFECT"][NUMBER]        = $model->field["PERFECT"];
        $data["PASS_SCORE"][NUMBER]     = $model->field["PASS_SCORE"];
        $data["WEIGHTING"][NUMBER]      = $model->field["WEIGHTING"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "PROFICIENCY_PERFECT_COURSE_DAT");

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     PROFICIENCY_PERFECT_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR                    = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER                = '{$model->semester}' ";
        $query .= "     AND PROFICIENCYDIV          = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND PROFICIENCYCD           = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     AND PROFICIENCY_SUBCLASS_CD = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' ";
        $query .= "     AND DIV                     = '{$model->field["DIV"]}' ";
        $query .= "     AND GRADE                   = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD                = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD                 = '{$model->field["MAJORCD"]}' ";
        $query .= "     AND COURSECODE              = '{$model->field["COURSECODE"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //コピー元をカウント
    function &getFromCopyCountQuery($model, $flg) {
        $db = Query::dbCheckOut();
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        if ($flg == "to") {
            $query .= "     AND T1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' ";
        } else {
            $query .= "     AND T1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD_FROMCOPY"]}' ";
        }
        $checkcnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $checkcnt;
    }

    //実力テストコピー
    function fromCopyInsertCopy($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
    
        $query  = " INSERT INTO PROFICIENCY_PERFECT_COURSE_DAT ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     '{$model->field["PROFICIENCYCD"]}' AS PROFICIENCYCD, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.PASS_SCORE, ";
        $query .= "     T1.WEIGHTING, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND T1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD_FROMCOPY"]}' ";
        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
    
}
?>
