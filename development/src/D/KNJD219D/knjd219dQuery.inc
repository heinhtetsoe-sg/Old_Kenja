<?php

require_once('for_php7.php');


class knjd219dQuery extends Query {

    //学期コンボ
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function setDelIns($db, $model) {
        $hrClassInState  = "('";
        $hrClassInState .= implode("', '", $model->selectdata);
        $hrClassInState .= "')";

        if ($model->field["SEMESTER"] == "9" && $model->Properties["unCreateJviewstatRecordDatSeme9"] != '1') {

        //削除（JVIEWSTAT_RECORD_DAT）
        $query  = " DELETE FROM JVIEWSTAT_RECORD_DAT D1 ";
        $query .= " WHERE ";
        $query .= "     D1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND D1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                SCHREG_REGD_DAT E1 ";
        $query .= "            WHERE ";
        $query .= "                E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                AND E1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["SELECT_DIV"] == "1") {
            $query .= "                AND E1.GRADE || E1.HR_CLASS IN ".$hrClassInState." ";
        } else {
            $query .= "                AND E1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= "                AND D1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";

        $db->query($query);

        //追加（JVIEWSTAT_RECORD_DAT）
        $query  = " INSERT INTO JVIEWSTAT_RECORD_DAT( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     VIEWCD, ";
        $query .= "     STATUS, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     '{$model->field["SEMESTER"]}' AS SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     L1.VIEWCD, ";
        $query .= "     SUM(L3.ASSESSLEVEL) AS TOTAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN JVIEWSTAT_RECORD_DAT L1 ON T1.YEAR = L1.YEAR ";
        if ($model->field["SEMESTER"] != "9") {
            $query .= "          AND L1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        }
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     INNER JOIN JVIEWSTAT_INPUTSEQ_DAT L2 ON L1.YEAR = L2.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD = L2.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = L2.SUBCLASSCD ";
        $query .= "          AND L1.VIEWCD = L2.VIEWCD ";
        $query .= "          AND T1.GRADE = L2.GRADE ";
        $query .= "          AND L1.SEMESTER = L2.SEMESTER ";
        $query .= "          AND L2.VIEWFLG = '1' ";
        if ($model->Properties["useJviewstatLevel"] == "JVIEWSTAT_LEVEL_SEMES_MST") {
            $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_SEMES_MST L3 ON L3.YEAR = L2.YEAR ";
            $query .= "               AND L3.SEMESTER = '{$model->field["SEMESTER"]}' ";
        } else {
            $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_MST L3 ON L3.YEAR = L2.YEAR ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND L3.CLASSCD = L2.CLASSCD ";
            $query .= "                                  AND L3.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "                                  AND L3.CURRICULUM_CD = L2.CURRICULUM_CD ";
        }
        $query .= "                                      AND L3.SUBCLASSCD  = L2.SUBCLASSCD ";
        $query .= "                                      AND L3.VIEWCD = L2.VIEWCD ";
        $query .= "                                      AND L3.DIV = '1' ";
        $query .= "                                      AND L3.GRADE = L2.GRADE ";
        $query .= "                                      AND L3.ASSESSMARK = L1.STATUS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["SELECT_DIV"] == "1") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS IN ".$hrClassInState." ";
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     L1.VIEWCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     L1.ASSESSMARK AS STATUS, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        if ($model->Properties["useJviewstatLevel"] == "JVIEWSTAT_LEVEL_SEMES_MST") {
            $query .= "     INNER JOIN JVIEWSTAT_LEVEL_SEMES_MST L1 ON L1.YEAR = T1.YEAR ";
            $query .= "               AND L1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        } else {
            $query .= "     INNER JOIN JVIEWSTAT_LEVEL_MST L1 ON L1.YEAR = T1.YEAR ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.VIEWCD = T1.VIEWCD ";
        $query .= "          AND L1.DIV = '1' ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= "          AND T1.TOTAL BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ";

        $db->query($query);

        }

        //削除（RECORD_SCORE_DAT）
        $query  = " DELETE FROM RECORD_SCORE_DAT D1 ";
        $query .= " WHERE ";
        $query .= "     D1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND D1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND D1.TESTKINDCD = '99' ";
        $query .= "     AND D1.TESTITEMCD = '00' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9') {
                $query .= "     AND D1.SCORE_DIV = '09' ";
            } else {
                $query .= "     AND D1.SCORE_DIV = '08' ";
            }
        } else {
            $query .= "     AND D1.SCORE_DIV = '00' ";
        }
        $query .= "     AND EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                SCHREG_REGD_DAT E1 ";
        $query .= "            WHERE ";
        $query .= "                E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                AND E1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["SELECT_DIV"] == "1") {
            $query .= "                AND E1.GRADE || E1.HR_CLASS IN ".$hrClassInState." ";
        } else {
            $query .= "                AND E1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= "                AND D1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";

        $db->query($query);

        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $setScoreField = "SCORE";
        } else {
            $setScoreField = "VALUE";
        }

        //追加（RECORD_SCORE_DAT）
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  = " INSERT INTO RECORD_SCORE_DAT(YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, {$setScoreField}, REGISTERCD, UPDATED) ";
        } else {
            $query  = " INSERT INTO RECORD_SCORE_DAT(YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, SUBCLASSCD, SCHREGNO, {$setScoreField}, REGISTERCD, UPDATED) ";
        }
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     '{$model->field["SEMESTER"]}' AS SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     SUM(L3.ASSESSLEVEL) AS TOTAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN JVIEWSTAT_RECORD_DAT L1 ON T1.YEAR = L1.YEAR ";
        if ($model->field["SEMESTER"] != "9" || $model->Properties["unCreateJviewstatRecordDatSeme9"] == '1') {
            $query .= "          AND L1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        }
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        if ($model->field["SEMESTER"] != "9" || $model->Properties["unCreateJviewstatRecordDatSeme9"] != '1') {
            $query .= "     INNER JOIN JVIEWSTAT_INPUTSEQ_DAT L2 ON L1.YEAR = L2.YEAR ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND L1.CLASSCD = L2.CLASSCD ";
                $query .= "         AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ";
                $query .= "         AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ";
            }
            $query .= "          AND L1.SUBCLASSCD = L2.SUBCLASSCD ";
            $query .= "          AND L1.VIEWCD = L2.VIEWCD ";
            $query .= "          AND T1.GRADE = L2.GRADE ";
            $query .= "          AND L1.SEMESTER = L2.SEMESTER ";
            $query .= "          AND L2.VIEWFLG = '1' ";
        }
        if ($model->Properties["useJviewstatLevel"] == "JVIEWSTAT_LEVEL_SEMES_MST") {
            $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_SEMES_MST L3 ON L3.YEAR = L1.YEAR ";
            $query .= "               AND L3.SEMESTER = '{$model->field["SEMESTER"]}' ";
        } else {
            $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_MST L3 ON L3.YEAR = L1.YEAR ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND L3.CLASSCD = L1.CLASSCD ";
            $query .= "                                  AND L3.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "                                  AND L3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= "                                      AND L3.SUBCLASSCD  = L1.SUBCLASSCD ";
        $query .= "                                      AND L3.VIEWCD = L1.VIEWCD ";
        $query .= "                                      AND L3.DIV = '1' ";
        $query .= "                                      AND L3.GRADE = T1.GRADE ";
        $query .= "                                      AND L3.ASSESSMARK = L1.STATUS ";
        
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["SELECT_DIV"] == "1") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS IN ".$hrClassInState." ";
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     '{$model->field["SEMESTER"]}' AS SEMESTER, ";
        $query .= "     '99' AS TESTKINDCD, ";
        $query .= "     '00' AS TESTITEMCD, ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9') {
                $query .= "     '09' AS SCORE_DIV, ";
            } else {
                $query .= "     '08' AS SCORE_DIV, ";
            }
        } else {
            $query .= "     '00' AS SCORE_DIV, ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.ASSESSLEVEL AS VALUE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        if ($model->Properties["useJviewstatLevel"] == "JVIEWSTAT_LEVEL_SEMES_MST") {
            $query .= "     INNER JOIN JVIEWSTAT_LEVEL_SEMES_MST L1 ON L1.YEAR = T1.YEAR ";
            $query .= "               AND L1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        } else {
            $query .= "     INNER JOIN JVIEWSTAT_LEVEL_MST L1 ON L1.YEAR = T1.YEAR ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.VIEWCD = '0000' ";
        $query .= "          AND L1.DIV = '2' ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= "          AND T1.TOTAL BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ";

        $db->query($query);

    }

}
?>
