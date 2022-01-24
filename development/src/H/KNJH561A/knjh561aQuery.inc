<?php

require_once('for_php7.php');

class knjh561aQuery extends Query {

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

    //データ種別取得
    function getDataDiv() {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getProName($model) {

        $query  = " SELECT ";
        $query .= "     PROFICIENCYCD AS VALUE, ";
        $query .= "     PROFICIENCYCD || ':' || PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGrade($semester, $model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR ";
        $query .= "                                  AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T2.SCHOOL_KIND <> 'P' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1      = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2      = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3      = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {

        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得（権限チェック）
    function getCourse($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || '　' || S1.COURSENAME || S2.MAJORNAME || S3.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_MST S1 ON T1.COURSECD = S1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST S2 ON T1.COURSECD = S2.COURSECD AND T1.MAJORCD = S2.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST S3 ON T1.COURSECODE = S3.COURSECODE, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T1.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T2.TR_CD1      = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2      = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3      = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function chuKoIkkan() {

        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='Z010' ";

        return $query;
    }

    function getTestName($model) {

        $query .= " SELECT ";
        $query .= "     VALUE(PROFICIENCYNAME1, '') AS PROFICIENCYNAME1 ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' AND ";
        $query .= "     PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' ";

        return $query;
    }

    function getStudentSql($model, $selected, $PRINT_TYPE_CLASS, $PRINT_TYPE_GRADE, $PRINT_TYPE_COURSECD, $rankDataDiv, $RANK_DIV_GRADE, $RANK_DIV_HRCLASS, $RANK_DIV_COURSE, $RANK_DIV_COURSEGROUP, $RANK_DATA_DIV_SCORE) {

        $gradeClass = "";
        $courseCdMajorCdCourseCode = "";
        
        if ($model->field["SELECT_DIV"] == $PRINT_TYPE_CLASS) {
            $gradeClass = $selected;
        } else if ($model->field["SELECT_DIV"] == $PRINT_TYPE_COURSECD) { 
            $courseCdMajorCdCourseCode = $selected;
        }

        $query  = "";
        $query .= " WITH RANK_T AS ( ";
        $query .= " select ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SEMESTER, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.PROFICIENCYDIV, ";
        $query .= "     t1.PROFICIENCYCD AS TESTCD, ";
        $query .= "     t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ";
        if ("03" == $rankDataDiv) {
            $query .= "     M1.SCORE, ";
            $query .= "     M1.AVG, ";
        } else {
            $query .= "     t1.SCORE, ";
            $query .= "     t1.AVG, ";
        }
        $query .= "     CAST(NULL AS VARCHAR(2)) AS SCORE_DI, ";
        $query .= "     L2.RANK AS CLASS_RANK, ";
        $query .= "     t1.RANK AS GRADE_RANK, ";
        $query .= "     L3.RANK AS COURSE_RANK, ";
        $query .= "     L4.RANK AS COURSEGROUP_RANK, ";
        $query .= "     L2.DEVIATION AS CLASS_DEVIATION, ";
        $query .= "     t1.DEVIATION AS GRADE_DEVIATION, ";
        $query .= "     L3.DEVIATION AS COURSE_DEVIATION, ";
        $query .= "     L4.DEVIATION AS COURSEGROUP_DEVIATION, ";
        $query .= "     t4.GRADE || t3.HR_CLASS AS CLASS1, ";
        $query .= "     t4.HR_NAME AS HR_NAME, ";
        $query .= "     t3.COURSECD || t3.MAJORCD || t3.COURSECODE AS COURSECODE1, ";
        $query .= "     t7.COURSECODENAME, ";
        $query .= "     L5.GROUP_CD, ";
        $query .= "     L6.GROUP_NAME, ";
        $query .= "     t4.GRADE, ";
        $query .= "     t5.NAME, ";
        $query .= "     t6.ABBV1 AS SEXNAME, ";
        $query .= "     t3.ATTENDNO ";
        $query .= " from PROFICIENCY_RANK_DAT t1 ";
        $query .= "     inner join SCHREG_REGD_DAT t3 on ";
        $query .= "         t1.YEAR = t3.YEAR and ";
        $query .= "         t1.SEMESTER = t3.SEMESTER and ";
        $query .= "         t1.SCHREGNO = t3.SCHREGNO ";
        $query .= "     left join SCHREG_REGD_HDAT t4 on ";
        $query .= "         t3.YEAR = t4.YEAR and ";
        $query .= "         t3.SEMESTER = t4.SEMESTER and ";
        $query .= "         t3.GRADE = t4.GRADE and ";
        $query .= "         t3.HR_CLASS = t4.HR_CLASS ";
        $query .= "     left join SCHREG_BASE_MST t5 on ";
        $query .= "         t1.SCHREGNO = t5.SCHREGNO ";
        $query .= "     left join NAME_MST t6 on ";
        $query .= "         t6.NAMECD1 = 'Z002' and ";
        $query .= "         t6.NAMECD2 = t5.SEX ";
        $query .= "     left join COURSECODE_MST t7 on ";
        $query .= "         t3.COURSECODE = t7.COURSECODE ";
        $query .= "     LEFT JOIN PROFICIENCY_RANK_DAT M1 ON  M1.YEAR = t1.YEAR ";
        $query .= "          AND M1.SEMESTER = t1.SEMESTER ";
        $query .= "          AND M1.PROFICIENCYCD = t1.PROFICIENCYCD ";
        $query .= "          AND M1.PROFICIENCYDIV = t1.PROFICIENCYDIV ";
        $query .= "          AND M1.SCHREGNO = t1.SCHREGNO ";
        $query .= "          AND M1.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ";
        $query .= "          AND M1.RANK_DATA_DIV = '{$RANK_DIV_GRADE}' ";
        $query .= "     LEFT JOIN PROFICIENCY_RANK_DAT L2 ON  L2.YEAR = t1.YEAR ";
        $query .= "          AND L2.SEMESTER = t1.SEMESTER ";
        $query .= "          AND L2.PROFICIENCYCD = t1.PROFICIENCYCD ";
        $query .= "          AND L2.PROFICIENCYDIV = t1.PROFICIENCYDIV ";
        $query .= "          AND L2.SCHREGNO = t1.SCHREGNO ";
        $query .= "          AND L2.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ";
        $query .= "          AND L2.RANK_DATA_DIV = t1.RANK_DATA_DIV ";
        $query .= "          AND L2.RANK_DIV = '{$RANK_DIV_HRCLASS}' ";
        $query .= "     LEFT JOIN PROFICIENCY_RANK_DAT L3 ON  L3.YEAR = t1.YEAR ";
        $query .= "          AND L3.SEMESTER = t1.SEMESTER ";
        $query .= "          AND L3.PROFICIENCYCD = t1.PROFICIENCYCD ";
        $query .= "          AND L3.PROFICIENCYDIV = t1.PROFICIENCYDIV ";
        $query .= "          AND L3.SCHREGNO = t1.SCHREGNO ";
        $query .= "          AND L3.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ";
        $query .= "          AND L3.RANK_DATA_DIV = t1.RANK_DATA_DIV ";
        $query .= "          AND L3.RANK_DIV = '{$RANK_DIV_COURSE}' ";
        $query .= "     LEFT JOIN PROFICIENCY_RANK_DAT L4 ON  L4.YEAR = t1.YEAR ";
        $query .= "          AND L4.SEMESTER = t1.SEMESTER ";
        $query .= "          AND L4.PROFICIENCYCD = t1.PROFICIENCYCD ";
        $query .= "          AND L4.PROFICIENCYDIV = t1.PROFICIENCYDIV ";
        $query .= "          AND L4.SCHREGNO = t1.SCHREGNO ";
        $query .= "          AND L4.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ";
        $query .= "          AND L4.RANK_DATA_DIV = t1.RANK_DATA_DIV ";
        $query .= "          AND L4.RANK_DIV = '{$RANK_DIV_COURSEGROUP}' ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT L5 ON  L5.YEAR = t3.YEAR ";
        $query .= "          AND L5.GRADE = t3.GRADE ";
        $query .= "          AND L5.COURSECD = t3.COURSECD ";
        $query .= "          AND L5.MAJORCD = t3.MAJORCD ";
        $query .= "          AND L5.COURSECODE = t3.COURSECODE ";
        $query .= "    LEFT JOIN COURSE_GROUP_CD_HDAT L6 ON  L6.YEAR = L5.YEAR ";
        $query .= "          AND L6.GRADE = L5.GRADE ";
        $query .= "          AND L6.GROUP_CD = L5.GROUP_CD ";
        $query .= " where ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     and t1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     and t1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     and t1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     and t1.RANK_DATA_DIV = '{$rankDataDiv}' ";
        $query .= "     and t1.RANK_DIV = '{$RANK_DATA_DIV_SCORE}' ";
        $query .= "     and t3.GRADE = '{$model->field["GRADE"]}' ";
        if ($PRINT_TYPE_CLASS == $model->field["SELECT_DIV"]) {
            $query .= "     and t4.GRADE || t3.HR_CLASS = '{$gradeClass}' ";
        }
        if ($PRINT_TYPE_COURSECD == $model->field["SELECT_DIV"]) {
            $query .= "     and t3.COURSECD || t3.MAJORCD || t3.COURSECODE = '{$courseCdMajorCdCourseCode}' ";
        }
        $query .= " ), MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "    RANK_T ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD AS TESTCD, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ";
        $query .= "     CAST(NULL AS SMALLINT) AS SCORE, ";
        $query .= "     CAST(NULL AS DECIMAL) AS AVG, ";
        $query .= "     T1.SCORE_DI, ";
        $query .= "     CAST(NULL AS SMALLINT) AS CLASS_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS GRADE_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS COURSE_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS COURSEGROUP_RANK, ";
        $query .= "     CAST(NULL AS DECIMAL) AS CLASS_DEVIATION, ";
        $query .= "     CAST(NULL AS DECIMAL) AS GRADE_DEVIATION, ";
        $query .= "     CAST(NULL AS DECIMAL) AS COURSE_DEVIATION, ";
        $query .= "     CAST(NULL AS DECIMAL) AS COURSEGROUP_DEVIATION, ";
        $query .= "     T3.GRADE || T3.HR_CLASS AS CLASS1, ";
        $query .= "     T4.HR_NAME AS HR_NAME, ";
        $query .= "     T3.COURSECD || T3.MAJORCD || T3.COURSECODE AS COURSECODE1, ";
        $query .= "     T7.COURSECODENAME, ";
        $query .= "     L5.GROUP_CD, ";
        $query .= "     L6.GROUP_NAME, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T5.NAME, ";
        $query .= "     T6.ABBV1 AS SEXNAME, ";
        $query .= "     T3.ATTENDNO ";
        $query .= " FROM ";
        $query .= "    PROFICIENCY_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_REGD_DAT T3 on ";
        $query .= "         T1.YEAR = T3.YEAR and ";
        $query .= "         T1.SEMESTER = T3.SEMESTER and ";
        $query .= "         T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT T4 on ";
        $query .= "         T3.YEAR = T4.YEAR and ";
        $query .= "         T3.SEMESTER = T4.SEMESTER and ";
        $query .= "         T3.GRADE = T4.GRADE and ";
        $query .= "         T3.HR_CLASS = T4.HR_CLASS ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST T5 on ";
        $query .= "         T1.SCHREGNO = T5.SCHREGNO ";
        $query .= "    LEFT JOIN NAME_MST T6 on ";
        $query .= "         T6.NAMECD1 = 'Z002' and ";
        $query .= "         T6.NAMECD2 = T5.SEX ";
        $query .= "    LEFT JOIN COURSECODE_MST T7 on ";
        $query .= "         T3.COURSECODE = T7.COURSECODE ";
        $query .= "    LEFT JOIN COURSE_GROUP_CD_DAT L5 ON  L5.YEAR = t3.YEAR ";
        $query .= "          AND L5.GRADE = t3.GRADE ";
        $query .= "          AND L5.COURSECD = t3.COURSECD ";
        $query .= "          AND L5.MAJORCD = t3.MAJORCD ";
        $query .= "          AND L5.COURSECODE = t3.COURSECODE ";
        $query .= "    LEFT JOIN COURSE_GROUP_CD_HDAT L6 ON  L6.YEAR = L5.YEAR ";
        $query .= "          AND L6.GRADE = L5.GRADE ";
        $query .= "          AND L6.GROUP_CD = L5.GROUP_CD ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "    AND T1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "    AND T1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "    AND T3.GRADE = '{$model->field["GRADE"]}' ";
        if ($PRINT_TYPE_CLASS == $model->field["SELECT_DIV"]) {
            $query .= "     AND T3.GRADE || T3.HR_CLASS = '{$gradeClass}' ";
        }
        if ($PRINT_TYPE_COURSECD == $model->field["SELECT_DIV"]) {
            $query .= "     AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = '{$courseCdMajorCdCourseCode}' ";
        }
        $query .= "    AND NOT EXISTS( ";
        $query .= "        SELECT ";
        $query .= "            * ";
        $query .= "        FROM ";
        $query .= "            RANK_T E1 ";
        $query .= "        WHERE ";
        $query .= "            T1.YEAR = E1.YEAR ";
        $query .= "            AND T1.SEMESTER = E1.SEMESTER ";
        $query .= "            AND T1.PROFICIENCYDIV = E1.PROFICIENCYDIV ";
        $query .= "            AND T1.PROFICIENCYCD = E1.TESTCD ";
        $query .= "            AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "            AND T1.PROFICIENCY_SUBCLASS_CD = E1.SUBCLASSCD ";
        $query .= "    ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";
        if($PRINT_TYPE_CLASS == $model->field["SELECT_DIV"] || $PRINT_TYPE_GRADE == $model->field["SELECT_DIV"]) {
            $query .= "     , CLASS1 ";
        }
        if($PRINT_TYPE_COURSECD == $model->field["SELECT_DIV"]) {
            $query .= "     , COURSECODE1 ";
        }

        return $query;
    }

    function getSubclassSql($model, $grade, $code, $PRINT_TYPE_CLASS, $PRINT_TYPE_COURSECD, $PRINT_TYPE_GRADE) {

            $isHighschool = !$isChuKoIkkan || $isChuKoIkkan && ($grade * 1) >= 4;
            $outputBefore = false;

            $query = "";
            if ($PRINT_TYPE_CLASS == $model->field["SELECT_DIV"]) {
                if ($outputBefore) {
                    $query .= " , ";
                } else {
                    $query .= " with ";
                }
                $outputBefore = true;
                $query .= " T_SCHREG AS ( ";
                $query .= "   select  ";
                $query .= "       t1.GRADE, ";
                $query .= "       t1.COURSECD || t1.MAJORCD || t1.COURSECODE as code ";
                $query .= "   from ";
                $query .= "       SCHREG_REGD_DAT t1 ";
                $query .= "   where ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' and ";
                $query .= "     t1.SEMESTER = '{$model->field["SEMESTER"]}' and ";
                $query .= "     t1.GRADE || t1.HR_CLASS = '{$code}' ";
                $query .= "   group by ";
                $query .= "     t1.GRADE, t1.COURSECD || t1.MAJORCD || t1.COURSECODE ";
                $query .= " ) ";
            }
            if ($PRINT_TYPE_COURSECD == $model->field["SELECT_DIV"] || $PRINT_TYPE_CLASS == $model->field["SELECT_DIV"]) {
                if ($outputBefore) {
                    $query .= " , ";
                } else {
                    $query .= " with ";
                }
                $outputBefore = true;
                $query .= " TA AS ( ";
                $query .= "   select  ";
                $query .= "       t1.GRADE, ";
                $query .= "       t1.GROUP_DIV, ";
                $query .= "       t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD ";
                $query .= "   from ";
                $query .= "       PROFICIENCY_SUBCLASS_GROUP_DAT t1 ";
                $query .= "   where ";
                $query .= "     t1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' and ";
                $query .= "     t1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' and ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' and ";
                $query .= "     t1.SEMESTER = '{$model->field["SEMESTER"]}' and ";
                if ($PRINT_TYPE_CLASS == $model->field["SELECT_DIV"]) {
                    $query .= "    exists (select 'X' from T_SCHREG where grade = t1.GRADE and code = t1.COURSECD || t1.MAJORCD || t1.COURSECODE) ";
                } else if ($PRINT_TYPE_COURSECD == $model->field["SELECT_DIV"]) {
                    $query .= "    t1.COURSECD || t1.MAJORCD || t1.COURSECODE = '{$code}' ";
                }
                $query .= "   group by ";
                $query .= "     t1.GRADE, t1.GROUP_DIV, t1.PROFICIENCY_SUBCLASS_CD ";
                $query .= " ) ";
            }

            if ($outputBefore) {
                $query .= " , ";
            } else {
                $query .= " with ";
            }
            $outputBefore = true;
            $query .= " T_HIGH AS ( ";
            $query .= "   select ";
            $query .= "     t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD ";
            $query .= "   from ";
            $query .= "     PROFICIENCY_RANK_DAT t1 ";
            $query .= "     inner join SCHREG_REGD_DAT t2 on ";
            $query .= "        t1.YEAR = t2.YEAR and ";
            $query .= "        t1.SEMESTER = t2.SEMESTER and ";
            $query .= "        t2.SCHREGNO = t1.SCHREGNO and ";
            $query .= "        t2.GRADE = '{$model->field["GRADE"]}' ";
            $query .= "   where ";
            $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     and t1.SEMESTER = '{$model->field["SEMESTER"]}' ";
            if ($PRINT_TYPE_CLASS == $model->field["SELECT_DIV"]) {
                $query .= "    and t2.GRADE || t2.HR_CLASS = '{$code}' ";
            } else if ($PRINT_TYPE_COURSECD == $model->field["SELECT_DIV"]) {
                $query .= "    and t2.COURSECD || t2.MAJORCD || t2.COURSECODE = '{$code}' ";
            }
            $query .= "   group by ";
            $query .= "     t1.PROFICIENCY_SUBCLASS_CD ";
            $query .= " ) ";

            $query .= " select ";
            $query .= "   t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ";
            $query .= "   t1.GROUP_DIV, ";
            $query .= "   t2.SUBCLASS_ABBV AS SUBCLASS_NAME ";
            $query .= " from PROFICIENCY_SUBCLASS_GROUP_DAT t1 ";
            if ($PRINT_TYPE_COURSECD == $model->field["SELECT_DIV"] || $PRINT_TYPE_CLASS == $model->field["SELECT_DIV"]) {
                $query .= "  inner join TA on t1.PROFICIENCY_SUBCLASS_CD = TA.SUBCLASSCD and t1.GROUP_DIV = TA.GROUP_DIV ";
            }
            $query .= "  inner join T_HIGH on ";
            $query .= "    t1.PROFICIENCY_SUBCLASS_CD = T_HIGH.SUBCLASSCD ";
            $query .= " left join PROFICIENCY_SUBCLASS_MST t2 on ";
            $query .= "   t1.PROFICIENCY_SUBCLASS_CD = t2.PROFICIENCY_SUBCLASS_CD ";
            $query .= " where ";
            $query .= "   t1.GRADE = '{$model->field["GRADE"]}' ";
            $query .= " group by ";
            $query .= "     t1.PROFICIENCY_SUBCLASS_CD, t2.SUBCLASS_ABBV, t1.GROUP_DIV ";
            $query .= " order by ";
            $query .= "     t1.PROFICIENCY_SUBCLASS_CD ";

            return $query;
    }

    function getAverageSql($model, $gradehrclass, $grade, $coursecode, $avgDataDiv, $avgDiv) {
        $whereGradeClass = $PRINT_TYPE_CLASS == $model->field["SELECT_DIV"] ? $gradehrclass : $grade + "000";
        $whereCourseCdMajorCdCourseCode = $PRINT_TYPE_COURSECD == $model->field["SELECT_DIV"] ? $coursecode : "00000000";

        $query  = "";
        $query .= " select ";
        $query .= "     PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ";
        $query .= "     SCORE, ";
        $query .= "     AVG ";
        $query .= " from ";
        $query .= "     PROFICIENCY_AVERAGE_DAT T1 ";
        $query .= " where ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     and SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     and t1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     and t1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     and DATA_DIV = '{$avgDataDiv}' ";
        $query .= "     and AVG_DIV = '{$avgDiv}' ";
        if ($PRINT_TYPE_GRADE != $model->field["SELECT_DIV"]) {
            $query .= "     and GRADE = '{$grade}' ";
        }
        $query .= "     and GRADE || HR_CLASS = '{$whereGradeClass}' ";
        $query .= "     and COURSECD || MAJORCD || COURSECODE = '{$whereCourseCdMajorCdCourseCode}' ";

        return $query;
    }

    function getSubclassGroupMstSql($model, $coursecode) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     GROUP_DIV, GROUP_NAME "; 
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_MST ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' AND ";
        $query .= "   SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "   PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' AND ";
        $query .= "   PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' AND ";
        $query .= "   GRADE = '{$model->field["GRADE"]}' AND ";
        $query .= "   COURSECD || MAJORCD || COURSECODE = '{$coursecode}' ";
        return $query;
    }
}
?>
