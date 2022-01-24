<?php

require_once('for_php7.php');


class knjm060wQuery extends Query {

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJM060W' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度
    function GetYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //科目
    function GetSubClass($model)
    {
        $query  = " with ATABLE as (SELECT DISTINCT ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS VALUE, ";
        $query .= "     t2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATABLE t1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST t2 ON t1.CLASSCD = t2.CLASSCD ";
        $query .= "          AND t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= "          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
        $query .= "          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //テスト
    function getTest($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV AS VALUE, ";
        $query .= "     SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV || ':' || I1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT SCORE ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ON SCORE.YEAR = I1.YEAR ";
        $query .= "           AND SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV = I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     SCORE.YEAR = '{$model->field["YEAR"]}' ";
        if ($model->field["SUBCLASSNAME"]) {
            $query .= "     AND SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD = '{$model->field["SUBCLASSNAME"]}' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["TESTCD"]) {
            $query .= "     AND SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SCORE.CLASSCD, ";
        $query .= "     SCORE.SCHOOL_KIND, ";
        $query .= "     SCORE.CURRICULUM_CD, ";
        $query .= "     SCORE.SUBCLASSCD, ";
        $query .= "     SCORE.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT SCORE ";
        $query .= " WHERE ";
        $query .= "     SCORE.YEAR = '{$model->field["YEAR"]}' ";
        if ($model->field["SUBCLASSNAME"]) {
            $query .= "     AND SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD = '{$model->field["SUBCLASSNAME"]}' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["TESTCD"]) {
            $query .= "     AND SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.CLASSCD, ";
        $query .= "     MAIN_T.SCHOOL_KIND, ";
        $query .= "     MAIN_T.CURRICULUM_CD, ";
        $query .= "     MAIN_T.SUBCLASSCD, ";
        $query .= "     SUBM.SUBCLASSNAME, ";
        $query .= "     MAIN_T.SCHREGNO, ";
        $query .= "     BASE.NAME ";
        foreach ($model->testArray as $testCd => $testName) {
            $setTestCd = str_replace("-", "_", $testCd);
            $query .= "     ,SCORE{$setTestCd}.SCORE AS SCORE{$setTestCd} ";
        }
        if ($this->field["OUTDIV"] == 0 || $this->field["OUTDIV"] == 2) {
            $query .= "     ,SCORE08.SCORE AS SCORE08 ";
            $query .= "     ,SCORE09.SCORE AS SCORE09 ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBM ON MAIN_T.SUBCLASSCD = SUBM.SUBCLASSCD ";
        $query .= "          AND MAIN_T.CLASSCD = SUBM.CLASSCD ";
        $query .= "          AND MAIN_T.SCHOOL_KIND = SUBM.SCHOOL_KIND ";
        $query .= "          AND MAIN_T.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON MAIN_T.SCHREGNO = BASE.SCHREGNO ";
        foreach ($model->testArray as $testCd => $testName) {
            $setTestCd = str_replace("-", "_", $testCd);
            $query .= "     LEFT JOIN V_RECORD_SCORE_HIST_DAT SCORE{$setTestCd} ON SCORE{$setTestCd}.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "          AND SCORE{$setTestCd}.SEMESTER || '-' || SCORE{$setTestCd}.TESTKINDCD || '-' || SCORE{$setTestCd}.TESTITEMCD || '-' || SCORE{$setTestCd}.SCORE_DIV = '{$testCd}' ";
            $query .= "          AND SCORE{$setTestCd}.CLASSCD = MAIN_T.CLASSCD ";
            $query .= "          AND SCORE{$setTestCd}.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ";
            $query .= "          AND SCORE{$setTestCd}.CURRICULUM_CD = MAIN_T.CURRICULUM_CD ";
            $query .= "          AND SCORE{$setTestCd}.SUBCLASSCD = MAIN_T.SUBCLASSCD ";
            $query .= "          AND SCORE{$setTestCd}.SCHREGNO = MAIN_T.SCHREGNO ";
        }
        if ($this->field["OUTDIV"] == 0 || $this->field["OUTDIV"] == 2) {
            $query .= "     LEFT JOIN V_RECORD_SCORE_HIST_DAT SCORE08 ON SCORE08.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "          AND SCORE08.SEMESTER || '-' || SCORE08.TESTKINDCD || '-' || SCORE08.TESTITEMCD || '-' || SCORE08.SCORE_DIV = '9-99-00-08' ";
            $query .= "          AND SCORE08.CLASSCD = MAIN_T.CLASSCD ";
            $query .= "          AND SCORE08.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ";
            $query .= "          AND SCORE08.CURRICULUM_CD = MAIN_T.CURRICULUM_CD ";
            $query .= "          AND SCORE08.SUBCLASSCD = MAIN_T.SUBCLASSCD ";
            $query .= "          AND SCORE08.SCHREGNO = MAIN_T.SCHREGNO ";
            $query .= "     LEFT JOIN V_RECORD_SCORE_HIST_DAT SCORE09 ON SCORE09.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "          AND SCORE09.SEMESTER || '-' || SCORE09.TESTKINDCD || '-' || SCORE09.TESTITEMCD || '-' || SCORE09.SCORE_DIV = '9-99-00-09' ";
            $query .= "          AND SCORE09.CLASSCD = MAIN_T.CLASSCD ";
            $query .= "          AND SCORE09.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ";
            $query .= "          AND SCORE09.CURRICULUM_CD = MAIN_T.CURRICULUM_CD ";
            $query .= "          AND SCORE09.SUBCLASSCD = MAIN_T.SUBCLASSCD ";
            $query .= "          AND SCORE09.SCHREGNO = MAIN_T.SCHREGNO ";
        }
        $query .= " ORDER BY ";
        $query .= "     MAIN_T.SUBCLASSCD, ";
        $query .= "     MAIN_T.SCHREGNO ";

        return $query;
    }

}
?>
