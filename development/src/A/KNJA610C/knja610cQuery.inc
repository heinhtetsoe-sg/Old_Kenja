<?php

require_once('for_php7.php');

class knja610cQuery extends Query {

    //学期情報
    function getSemesterInfo($year, $semester) {
        $query .= " SELECT SEME.* ";
        $query .= " FROM SEMESTER_MST SEME ";
        $query .= " WHERE SEME.YEAR = '{$year}' ";
        $query .= "   AND SEME.SEMESTER = '{$semester}' ";

        return $query;
    }

    //校種取得
    function getSchoolkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE ";
        $query .= "   , ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 IN ('".SCHOOLKIND."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     ABBV3, NAME1 ";

        return $query;
    }

    //学期取得
    function getSemester($year) {
        $query  = "SELECT ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= "   , SEMESTERNAME AS LABEL ";
        $query .= "FROM SEMESTER_MST ";
        $query .= "WHERE YEAR = '{$year}' ";
        $query .= "  AND SEMESTER <> '9' ";
        $query .= "ORDER BY SEMESTER";

        return $query;
    }

    //テスト種別取得
    function getTestItem($model, $testKind = "") {
        $query = "";
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.TESTKINDCD ";
        $query .= "   , T1.TESTITEMCD ";
        $query .= "   , T2.TESTKINDNAME ";
        $query .= "   , T1.TESTKINDCD || '-' || T1.TESTITEMCD VALUE ";
        $query .= "   , T1.TESTKINDCD || T1.TESTITEMCD || ':' || T2.TESTKINDNAME LABEL ";

        $query .= " FROM ";
        $query .= "   TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "   INNER JOIN TESTKIND_MST T2 ";
        $query .= "     ON T1.TESTKINDCD = T2.TESTKINDCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "   AND T1.TESTKINDCD = '99' ";
        } else {
            $query .= "   AND T1.TESTKINDCD <> '99' ";
        }
        if ($testKind) {
            list($testKindCd, $testItemCd) = explode("-", $testKind);
            $query .= "   AND T1.TESTKINDCD = '{$testKindCd}' ";
            $query .= "   AND T1.TESTITEMCD = '{$testItemCd}' ";
        }

        $query .= " ORDER BY ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.TESTKINDCD ";
        $query .= "   , T1.TESTITEMCD ";

        return $query;
    }

    // 年組取得
    function getHrClass($model) {

        $query  = " SELECT ";
        $query .= "     REGDH.GRADE || '-' || REGDH.HR_CLASS AS VALUE ";
        $query .= "   , REGDH.HR_NAME AS LABEL ";
        $query .= " FROM SCHREG_REGD_HDAT REGDH ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "    ON REGDH.YEAR = REGDG.YEAR ";
        $query .= "   AND REGDH.GRADE = REGDG.GRADE ";

        $query .= " WHERE REGDH.YEAR='{$model->field["YEAR"]}'";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "   AND REGDH.SEMESTER='".CTRL_SEMESTER."'";
        } else {
            $query .= "   AND REGDH.SEMESTER='{$model->field["SEMESTER"]}'";
        }
        if ($model->field['SCHOOL_KIND']) {
            $query .= "   AND REGDG.SCHOOL_KIND='{$model->field['SCHOOL_KIND']}'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "   AND '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3) ";
        }

        return $query;
    }

    // CSV出力用データ取得
    function selectCsvQuery($model) {

        list($testKindCd, $testItemCd) = explode("-", $model->field["TESTKINDCD"]);

        $query  = "";
        $query .= " WITH REGD AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T3.SCHOOL_KIND ";
        $query .= "     , T3.GRADE_NAME1 GRADE_NAME ";
        $query .= "     , T1.HR_CLASS ";
        $query .= "     , T4.HR_NAME ";
        $query .= "     , T4.HR_CLASS_NAME1 HR_CLASS_NAME ";
        $query .= "     , T1.ATTENDNO ";
        $query .= "     , T2.NAME ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "       ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "       ON T1.YEAR = T3.YEAR ";
        $query .= "       AND T1.GRADE = T3.GRADE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "       ON T1.YEAR = T4.YEAR ";
        $query .= "       AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "       AND T1.GRADE = T4.GRADE ";
        $query .= "       AND T1.HR_CLASS = T4.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "     AND T1.SEMESTER='".CTRL_SEMESTER."'";
        } else {
            $query .= "     AND T1.SEMESTER='{$model->field["SEMESTER"]}'";
        }
        $query .= " ) ";

        $query .= " , SCORE_01 AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , SCHREGNO ";
        $query .= "     , SCORE ";
        $query .= "   FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$testKindCd}' ";
        $query .= "     AND TESTITEMCD = '{$testItemCd}' ";
        $query .= "     AND SCORE_DIV = '01' ";
        $query .= " ) ";
        $query .= " , SCORE_08 AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , SCHREGNO ";
        $query .= "     , SCORE ";
        $query .= "   FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$testKindCd}' ";
        $query .= "     AND TESTITEMCD = '{$testItemCd}' ";
        $query .= "     AND SCORE_DIV = '08' ";
        $query .= " ) ";
        $query .= " , SCORE_9908 AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , SCHREGNO ";
        $query .= "     , SCORE ";
        $query .= "   FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '08' ";
        $query .= " ) ";
        $query .= " , SCORE_9909 AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , SCHREGNO ";
        $query .= "     , SCORE ";
        $query .= "   FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '09' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.TESTKINDCD ";
        $query .= "   , T1.TESTITEMCD ";
        $query .= "   , T1.SCORE_DIV ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= "   , T2.SUBCLASSNAME ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , REGD.GRADE ";
        $query .= "   , REGD.GRADE_NAME ";
        $query .= "   , REGD.HR_CLASS ";
        $query .= "   , REGD.HR_NAME ";
        $query .= "   , REGD.HR_CLASS_NAME ";
        $query .= "   , REGD.ATTENDNO ";
        $query .= "   , REGD.NAME ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.CLASS_RANK ";
        $query .= "   , T1.COURSE_RANK ";
        $query .= "   , T1.GRADE_RANK ";

        $query .= "   , SCORE_01.SCORE SCORE_01 ";
        $query .= "   , SCORE_08.SCORE SCORE_08 ";
        $query .= "   , SCORE_9908.SCORE SCORE_9908 ";
        $query .= "   , SCORE_9909.SCORE SCORE_9909 ";

        $query .= " FROM ";
        $query .= "   RECORD_RANK_SDIV_DAT T1 ";
        $query .= "   INNER JOIN REGD ";
        $query .= "     ON T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   INNER JOIN SUBCLASS_MST T2 ";
        $query .= "     ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";

        $query .= "   LEFT JOIN SCORE_01 ";
        $query .= "     ON T1.YEAR = SCORE_01.YEAR ";
        $query .= "     AND T1.SEMESTER = SCORE_01.SEMESTER ";
        $query .= "     AND T1.CLASSCD = SCORE_01.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = SCORE_01.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = SCORE_01.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = SCORE_01.SUBCLASSCD ";
        $query .= "     AND T1.SCHREGNO = SCORE_01.SCHREGNO ";
        $query .= "   LEFT JOIN SCORE_08 ";
        $query .= "     ON T1.YEAR = SCORE_08.YEAR ";
        $query .= "     AND T1.SEMESTER = SCORE_08.SEMESTER ";
        $query .= "     AND T1.CLASSCD = SCORE_08.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = SCORE_08.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = SCORE_08.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = SCORE_08.SUBCLASSCD ";
        $query .= "     AND T1.SCHREGNO = SCORE_08.SCHREGNO ";
        $query .= "   LEFT JOIN SCORE_9908 ";
        $query .= "     ON T1.YEAR = SCORE_9908.YEAR ";
        $query .= "     AND T1.SEMESTER = SCORE_9908.SEMESTER ";
        $query .= "     AND T1.CLASSCD = SCORE_9908.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = SCORE_9908.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = SCORE_9908.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = SCORE_9908.SUBCLASSCD ";
        $query .= "     AND T1.SCHREGNO = SCORE_9908.SCHREGNO ";
        $query .= "   LEFT JOIN SCORE_9909 ";
        $query .= "     ON T1.YEAR = SCORE_9909.YEAR ";
        $query .= "     AND T1.SEMESTER = SCORE_9909.SEMESTER ";
        $query .= "     AND T1.CLASSCD = SCORE_9909.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = SCORE_9909.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = SCORE_9909.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = SCORE_9909.SUBCLASSCD ";
        $query .= "     AND T1.SCHREGNO = SCORE_9909.SCHREGNO ";
                        
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";

        $query .= "   AND T1.TESTKINDCD = '{$testKindCd}' ";
        $query .= "   AND T1.TESTITEMCD = '{$testItemCd}' ";

        $query .= "   AND T1.SCORE_DIV = '01' ";

        if (get_count($model->selectHrClass) > 0) {
            $query .= "   AND (REGD.GRADE, REGD.HR_CLASS) IN ( ";
            $classList = array();
            for ($i=0; $i < get_count($model->selectHrClass); $i++) { 
                list($grade, $hrClass) = explode("-", $model->selectHrClass[$i]);
                $classList[] = " VALUES('{$grade}', '{$hrClass}') ";
            }
            $query .= implode(" UNION ", $classList);
            $query .= "   ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE ";
        $query .= "   , REGD.HR_CLASS ";
        $query .= "   , REGD.ATTENDNO ";
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= "   , T1.SCHREGNO ";

        return $query;
    }

    // 講座取得
    function getChair($model, $classCd, $schoolKind, $curriculumCd, $subclassCd, $schregNo) {

        $appDate = str_replace("/", "-", $model->field["APPDATE"]);

        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.YEAR ";
        $query .= "    , T1.SEMESTER ";
        $query .= "    , T1.SCHREGNO ";
        $query .= "    , T2.CLASSCD ";
        $query .= "    , T2.SCHOOL_KIND ";
        $query .= "    , T2.CURRICULUM_CD ";
        $query .= "    , T2.SUBCLASSCD ";
        $query .= "    , T1.CHAIRCD ";
        $query .= "    , T2.CHAIRNAME ";
        $query .= "  FROM ";
        $query .= "    CHAIR_STD_DAT T1 ";
        $query .= "    INNER JOIN CHAIR_DAT T2 ";
        $query .= "      ON T1.YEAR = T2.YEAR ";
        $query .= "      AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "      AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "  WHERE ";
        $query .= "    T1.YEAR = '{$model->field["YEAR"]}' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "    AND T1.SEMESTER='".CTRL_SEMESTER."'";
        } else {
            $query .= "    AND T1.SEMESTER='{$model->field["SEMESTER"]}'";
        }

        $query .= "    AND '{$appDate}' BETWEEN T1.APPDATE AND VALUE (T1.APPENDDATE, '2006-12-31') ";
        $query .= "    AND T2.CLASSCD = '{$classCd}' ";
        $query .= "    AND T2.SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "    AND T2.CURRICULUM_CD = '{$curriculumCd}' ";
        $query .= "    AND T2.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "    AND T1.SCHREGNO = '{$schregNo}' ";

        $query .= "  ORDER BY ";
        $query .= "      T1.CHAIRCD ";

        return $query;
    }

    //職員取得
    function getStaff($model, $chairCd) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.STAFFCD ";
        $query .= "   , T2.STAFFNAME ";
        $query .= "   , CASE T1.CHARGEDIV WHEN '0' THEN '9' ELSE T1.CHARGEDIV END DIVORDER ";
        $query .= " FROM ";
        $query .= "   CHAIR_STF_DAT T1 ";
        $query .= "   INNER JOIN STAFF_MST T2 ";
        $query .= "     ON T1.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "      T1.YEAR = '{$model->field["YEAR"]}' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND T1.SEMESTER='".CTRL_SEMESTER."'";
        } else {
            $query .= " AND T1.SEMESTER='{$model->field["SEMESTER"]}'";
        }
        $query .= " AND  T1.CHAIRCD = '{$chairCd}' ";

        $query .= " ORDER BY ";
        $query .= "     DIVORDER ";
        $query .= "   , STAFFCD ";

        return $query;
    }

}
?>
