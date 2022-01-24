<?php

require_once('for_php7.php');

class knjh441cQuery extends Query {

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学校校種取得
    function getSchoolkindQuery($grade) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE = '".$grade."' ";

        return $query;
    }

    //模試情報
    function getMockName($model,$grade) {
        $gradeCd = substr($grade, -1);
        $mockCd = "1".CTRL_YEAR."_{$gradeCd}%";
    
        $query  = " SELECT ";
        $query .= "     T1.MOCKCD AS VALUE ";
        $query .= "   , T1.MOCKCD || ':' || T1.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.MOCKCD LIKE '{$mockCd}' ";


        return $query;
    }
    
    //テスト種別
    function getTest($model, $flg = 1) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        if ($flg == 1) {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR ";
            $query .= "                                   AND L2.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '{$model->semester}' ";
        $query .= "     AND T1.CLASSCD      = '00' ";
        if ($flg != 1) {
            $query .= "     AND T1.SCHOOL_KIND = '00' ";
        }
        $query .= "     AND T1.CURRICULUM_CD  = '00' ";
        $query .= "     AND T1.SUBCLASSCD  = '000000' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T1.GRADE = '00' ";
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || ':' || GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学科
    function getMajor($model) {
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " WITH MAIN_T AS ( ";
            $query .= " SELECT ";
            $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
            $query .= "     VCM.COURSECD || VCM.MAJORCD AS VALUE, ";
            $query .= "     A043.NAME1 ";
            $query .= " FROM ";
            $query .= "     V_COURSE_MAJOR_MST VCM ";
            $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
            $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
            $query .= " WHERE ";
            $query .= "     VCM.YEAR = '".CTRL_YEAR."' ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     MAIN_T ";
            $query .= " WHERE ";
            $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        
        } else {
            if ($model->Properties["knjh441cUseMajorcdKeta1"] == '1') {
                $query  = " WITH ALL(LABEL, VALUE) AS (VALUES('00:-すべて-', '00'))";
                $query .= " SELECT ";
                $query .= "     LABEL, ";
                $query .= "     VALUE ";
                $query .= " FROM ";
                $query .= "     ALL ";
                $query .= " UNION ALL ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.COURSECD || SUBSTR(T1.MAJORCD, 1, 1) || ':' || CASE WHEN 0 < LOCATE(T1.MAJORNAME, '　') THEN SUBSTR(T1.MAJORNAME, 1, LOCATE(T1.MAJORNAME, '　')) ELSE T1.MAJORNAME END AS LABEL, ";
                $query .= "     T1.COURSECD || SUBSTR(T1.MAJORCD, 1, 1) AS VALUE ";
            } else {
                $query  = " WITH ALL(LABEL, VALUE) AS (VALUES('0000:-すべて-', '0000'))";
                $query .= " SELECT ";
                $query .= "     LABEL, ";
                $query .= "     VALUE ";
                $query .= " FROM ";
                $query .= "     ALL ";
                $query .= " UNION ALL ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.COURSECD || T1.MAJORCD || ':' || T1.MAJORNAME AS LABEL, ";
                $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE ";
            }
            $query .= " FROM ";
            $query .= "     MAJOR_MST T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '". CTRL_YEAR ."' ";
            if ($model->semester == '9') {
                $query .= "         AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "         AND T2.SEMESTER = '".$model->semester."' ";
            }
            $query .= "         AND T2.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "         AND T2.COURSECD = T1.COURSECD ";
            $query .= "         AND T2.MAJORCD = T1.MAJORCD ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }
}
?>
