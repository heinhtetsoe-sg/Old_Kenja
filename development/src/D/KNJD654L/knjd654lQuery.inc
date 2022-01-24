<?php

require_once('for_php7.php');

class knjd654lQuery extends Query {

    //学期取得
    function getSemester($semester = '')
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($semester) {
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年末は、最新の学期取得
    function getMaxSemester()
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学校種別取得
    function getSchregRegdGdat()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($semester)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "     TR_CD2 = '".STAFFCD."' OR ";
            $query .= "     TR_CD3 = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD3 = '".STAFFCD."') AND ";
        }
        $query .= "     GRADE < '11' "; // 11学年以上は表示しない。
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTestKind($semester, $grade, $testkindcd = '') {
        $query  = " WITH MAIN_T(LABEL, VALUE, NAME) AS ( ";        
        $query .= " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ' ' || L1.TESTITEMNAME AS LABEL, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     L1.TESTITEMNAME AS NAME ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                   AND L2.GRADE = '".$grade."' ";
        $query .= "                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$semester."' ";
        if ($testkindcd) {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$testkindcd."' ";
        }
        $query .= "     AND T1.CLASSCD      = '00' ";
        $query .= "     AND T1.CURRICULUM_CD  = '00' ";
        $query .= "     AND T1.SUBCLASSCD  = '000000' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE, ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    function getCourse($model) {
        $query = "";
        $query .= "SELECT DISTINCT ";
        $query .= "   T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE ";
        $query .= "  ,T4.COURSECODENAME AS NAME ";
        $query .= "FROM SCHREG_REGD_DAT T1 "; 
        $query .= " INNER JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ";
        $query .= " INNER JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD AND T3.MAJORCD = T1.MAJORCD ";
        $query .= " INNER JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ";
        $query .= "WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.GRADE = '".$model->field["GRADE"]."' ";
        return $query;
    }

    function getPrintGroup($model, $course) {
        $query = "";
        $query .= " SELECT DISTINCT T1.HR_CLASS || '00000000' AS CODE, ";
        $query .= " T2.HR_NAME AS NAME, T2.HR_NAMEABBV AS ABBV, ";
        $query .= " T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE ";
        $query .= " FROM SCHREG_REGD_DAT T1";
        $query .= " INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "     AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE T1.YEAR = '".CTRL_YEAR."' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if ($course) {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$course."' ";
        }
        $query .= " ORDER BY CODE ";
        return $query;
    }

    function getSubclassList($model, $course) {
        $query = "";
        $query .= " WITH RECORD AS (";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, T1.SEMESTER, ";
        $query .= "     T2.GRADE, T2.HR_CLASS, T2.COURSECD, T2.MAJORCD, T2.COURSECODE, ";
        $query .= "     T1.SCHREGNO, ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= " CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN SUBCLASSCD ELSE ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD END AS SUBCLASSCD, T1.SCORE, T1.AVG ";
        $query .= " FROM RECORD_RANK_SDIV_DAT T1";
        $query .= " INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "    AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "    AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        $query .= "    AND T2.GRADE= '".$model->field["GRADE"]."' ";
        if ($course) {
            $query .= "    AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '".$course."' ";
        }
        $query .= " ) ";
        $query .= " SELECT DISTINCT VALUE(T4.SHOWORDER4, 999), ";
        $query .= "      T1.SUBCLASSCD, T2.SUBCLASSNAME, T2.SUBCLASSABBV ";
        $query .= " FROM RECORD T1 ";
        $query .= " INNER JOIN SUBCLASS_MST T2 ON ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " LEFT JOIN CLASS_MST T4 ON ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "     T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
        } else {
            $query .= "     T4.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ";
        }
        $query .= " ORDER BY VALUE(T4.SHOWORDER4, 999), T1.SUBCLASSCD ";
        return $query;
    }

    function getAverageList($model) {
        $query = "";
        $query .= " SELECT ";
        $query .= " CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN SUBCLASSCD ELSE ";
        if ("1"== $model->Properties["useCurriculumcd"]) {
            $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
        }
        $query .= "   SUBCLASSCD END AS SUBCLASSCD, AVG_DIV, GRADE, HR_CLASS || COURSECD || MAJORCD || COURSECODE AS CODE, SCORE, AVG ";
        $query .= " FROM ";
        $query .= "   RECORD_AVERAGE_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "   AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        $query .= "   AND (AVG_DIV IN ('1', '2', '3')) ";
        return $query;
    }

}
?>
