<?php

require_once('for_php7.php');

class knjd154kQuery extends Query {
    //学期を取得
    function getSemester() {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得(権限チェック)
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT ";
            $query .= "    GRADE || '-' || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR         = '".CTRL_YEAR."' ";
            if ($model->semester == '9') {
                $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            } else {
                $query .= "    AND SEMESTER = '".$model->semester."' ";
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT ";
            $query .= "    GRADE || '-' || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR           = '".CTRL_YEAR."' ";
            if ($model->semester == '9') {
                $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            } else {
                $query .= "    AND SEMESTER = '".$model->semester."' ";
            }
            $query .= "    AND (   TR_CD1 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."' AND ";
        if ($model->semester == '9') {
            $query .= "     T1.SEMESTER         = '". CTRL_SEMESTER ."' AND ";
        } else {
            $query .= "     T1.SEMESTER         = '".$model->semester."' AND ";
        }
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->hrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    function getTest($semester) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD <> '9900' ";

        return $query;
    }

    function getTestSql($model) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD <= '".$model->semester.$model->test_cd."' ";
        $query .= "     AND TESTKINDCD || TESTITEMCD <> '9900' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";

        return $query;
    }

    function getSchoolMstSql() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";

        return $query;
    }

    function getStudentInfo($model) {
        $selected = explode(",", $model->selectdata);

        $query  = " SELECT ";
        $query .= "     VSCH.SCHREGNO, ";
        $query .= "     VSCH.GRADE, ";
        $query .= "     VSCH.HR_CLASS, ";
        $query .= "     VSCH.ATTENDNO, ";
        $query .= "     VSCH.HR_NAME, ";
        $query .= "     VSCH.HR_NAMEABBV, ";
        $query .= "     VSCH.NAME, ";
        $query .= "     BASE.REAL_NAME, ";
        $query .= "     CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ";
        $query .= "     VSCH.COURSECD, ";
        $query .= "     L1.COURSENAME, ";
        $query .= "     VSCH.MAJORCD, ";
        $query .= "     L1.MAJORNAME, ";
        $query .= "     VSCH.COURSECODE, ";
        $query .= "     L2.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     V_SCHREG_INFO VSCH ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = VSCH.SCHREGNO ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ";
        $query .= "          AND VSCH.COURSECD = L1.COURSECD ";
        $query .= "          AND VSCH.MAJORCD = L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ";
        $query .= "          AND VSCH.COURSECODE = L2.COURSECODE ";
        $query .= "     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = VSCH.SCHREGNO AND L4.DIV = '04' ";
        $query .= " WHERE ";
        $query .= "     VSCH.YEAR = '".CTRL_YEAR."' ";
        if ($model->semester == '9') {
            $query .= "     AND VSCH.SEMESTER = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= "     AND VSCH.SEMESTER = '".$model->semester."' ";
        }
        $query .= "     AND VSCH.GRADE || '-' || VSCH.HR_CLASS = '".$model->hrClass."' ";
        $query .= "     AND VSCH.SCHREGNO IN ('".implode($selected,"','")."') ";
        $query .= " ORDER BY ";
        $query .= "     VSCH.GRADE, ";
        $query .= "     VSCH.HR_CLASS, ";
        $query .= "     VSCH.ATTENDNO ";

        return $query;
    }

    function getRecordScoreTestAppointSql($model, $testVal, $schregNo) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L1.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND T1.CLASSCD         = L1.CLASSCD ";
            $query .= "                          AND T1.SCHOOL_KIND     = L1.SCHOOL_KIND ";
            $query .= "                          AND T1.CURRICULUM_CD   = L1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$testVal["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$testVal["TESTKINDCD"].$testVal["TESTITEMCD"]."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    function getRecordScoreSubClassAllSql($model, $schregNo) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L1.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND T1.CLASSCD         = L1.CLASSCD ";
            $query .= "                          AND T1.SCHOOL_KIND     = L1.SCHOOL_KIND ";
            $query .= "                          AND T1.CURRICULUM_CD   = L1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '".$model->semester.$model->test_cd."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    function getRecordRankAppointSql($model, $testVal, $schregNo) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$testVal["SEMESTER"]."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$testVal["TESTKINDCD"].$testVal["TESTITEMCD"]."' ";

        return $query;
    }

    function getRecordAverageTestAppointSql($model, $testVal, $schInfo, $avgDiv) {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     RECORD_AVERAGE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$testVal["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$testVal["TESTKINDCD"].$testVal["TESTITEMCD"]."' ";
        $query .= "     AND T1.AVG_DIV = '".$avgDiv."' ";
        if ("1" == $avgDiv) {
            $query .= "     AND T1.GRADE = '".$schInfo["GRADE"]."' ";
        } else if ("2" == $avgDiv) {
            $query .= "     AND T1.GRADE = '".$schInfo["GRADE"]."' ";
            $query .= "     AND T1.HR_CLASS = '".$schInfo["HR_CLASS"]."' ";
        } else if ("3" == $avgDiv) {
            $query .= "     AND T1.GRADE = '".$schInfo["GRADE"]."' ";
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$schInfo["COURSECD"].$schInfo["MAJORCD"].$schInfo["COURSECODE"]."' ";
        } else {
            $query .= "     AND T1.GRADE = '".$schInfo["GRADE"]."' ";
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$schInfo["COURSECD"].$schInfo["MAJORCD"]."' ";
        }

        return $query;
    }

    function getKetuSql($model, $schInfo, $testVal) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     '欠' AS KETU ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$testVal["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$testVal["TESTKINDCD"].$testVal["TESTITEMCD"]."' ";
        $query .= "     AND T1.SCHREGNO = '".$schInfo["SCHREGNO"]."' ";
        $query .= "     AND T1.SCORE IS NULL ";

        return $query;
    }

    function getMockData($schInfo) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     L1.MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_DAT T1 ";
        $query .= "     LEFT JOIN MOCK_MST L1 ON T1.MOCKCD = L1.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '".$schInfo["SCHREGNO"]."' ";
        $query .= "     AND T1.MOCKDIV = '2' ";
        $query .= " ORDER BY ";
        $query .= "     T1.MOCKCD ";

        return $query;
    }

    function getMockRank($mockCd, $schInfo) {

        $query  = " SELECT ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     L1.SUBCLASS_NAME, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.GRADE_RANK, ";
        $query .= "     T1.GRADE_DEVIATION, ";
        $query .= "     T1.CLASS_RANK, ";
        $query .= "     T1.CLASS_DEVIATION, ";
        $query .= "     T1.COURSE_RANK, ";
        $query .= "     T1.COURSE_DEVIATION, ";
        $query .= "     GR.AVG AS G_AVG, ";
        $query .= "     GR.AVG_KANSAN AS G_AVG_KANSAN, ";
        $query .= "     GR.COUNT AS G_COUNT, ";
        $query .= "     HR.AVG AS H_AVG, ";
        $query .= "     HR.AVG_KANSAN AS H_AVG_KANSAN, ";
        $query .= "     HR.COUNT AS H_COUNT, ";
        $query .= "     CR.AVG AS C_AVG, ";
        $query .= "     CR.AVG_KANSAN AS C_AVG_KANSAN, ";
        $query .= "     CR.COUNT AS C_COUNT ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_DAT T1 ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_MST L1 ON T1.MOCK_SUBCLASS_CD = L1.MOCK_SUBCLASS_CD ";
        $query .= "     LEFT JOIN MOCK_AVERAGE_DAT GR ON T1.YEAR = GR.YEAR ";
        $query .= "          AND T1.MOCKCD = GR.MOCKCD ";
        $query .= "          AND T1.MOCK_SUBCLASS_CD = GR.MOCK_SUBCLASS_CD ";
        $query .= "          AND GR.AVG_DIV = '1' ";
        $query .= "          AND GR.GRADE = '".$schInfo["GRADE"]."' ";
        $query .= "     LEFT JOIN MOCK_AVERAGE_DAT HR ON T1.YEAR = HR.YEAR ";
        $query .= "          AND T1.MOCKCD = HR.MOCKCD ";
        $query .= "          AND T1.MOCK_SUBCLASS_CD = HR.MOCK_SUBCLASS_CD ";
        $query .= "          AND HR.AVG_DIV = '2' ";
        $query .= "          AND HR.GRADE = '".$schInfo["GRADE"]."' ";
        $query .= "     LEFT JOIN MOCK_AVERAGE_DAT CR ON T1.YEAR = CR.YEAR ";
        $query .= "          AND T1.MOCKCD = CR.MOCKCD ";
        $query .= "          AND T1.MOCK_SUBCLASS_CD = CR.MOCK_SUBCLASS_CD ";
        $query .= "          AND CR.AVG_DIV = '3' ";
        $query .= "          AND CR.GRADE = '".$schInfo["GRADE"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.MOCKCD = '".$mockCd."' ";
        $query .= "     AND T1.SCHREGNO = '".$schInfo["SCHREGNO"]."' ";
        $query .= "     AND T1.MOCKDIV = '2' ";
        $query .= " ORDER BY ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";

        return $query;
    }

}
?>
