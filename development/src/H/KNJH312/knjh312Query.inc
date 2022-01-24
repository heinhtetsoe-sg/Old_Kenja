<?php

require_once('for_php7.php');

class knjh312Query extends Query {

    //欠課数換算用データ取得
    function getSchoolData($model)
    {
        $query  = "SELECT ";
        $query .= "    ABSENT_COV, ";
        $query .= "    VALUE(ABSENT_COV_LATE,0) AS ABSENT_COV_LATE ";
        $query .= "FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";

        return $query;
    }

    //去年度取得
    function getLastYear($model)
    {
        $query  = "WITH YEART AS ( ";
        $query .= "SELECT ";
        $query .= "    MAX(YEAR) AS YEART ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR < '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    MAX(YEAR) AS YEAR, ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = (SELECT YEART FROM YEART) ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //次年度取得
    function getNextYear($model)
    {
        $query  = "WITH YEART AS ( ";
        $query .= "SELECT ";
        $query .= "    MIN(YEAR) AS YEART ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR > '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    MIN(YEAR) AS YEAR, ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = (SELECT YEART FROM YEART) ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //生徒情報取得
    function getName($model)
    {
        $query  = "SELECT ";
        $query .= "    L1.GRADE, ";
        $query .= "    L2.HR_NAME, ";
        $query .= "    L1.ATTENDNO, ";
        $query .= "    T1.NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.SEMESTER = '".$model->semester."' ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = L1.YEAR ";
        $query .= "         AND L2.SEMESTER = L1.SEMESTER ";
        $query .= "         AND L2.GRADE = L1.GRADE ";
        $query .= "         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //科目取得
    function getTestSubclass($model)
    {
        $query  = "WITH CHAIR_STD_T AS ( ";
        $query .= "SELECT DISTINCT ";
        $query .= "    CHAIRCD ";
        $query .= "FROM ";
        $query .= "    CHAIR_STD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= "), TESTITEM_T AS ( ";
        $query .= "SELECT DISTINCT ";
        $query .= "    CHAIRCD ";
        $query .= "FROM ";
        $query .= "    SCH_CHR_TEST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= ") ";
        $query .= "SELECT DISTINCT ";
        $query .= "    T1.SUBCLASSCD AS VALUE, ";
        $query .= "    L1.SUBCLASSABBV AS LABEL ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT T1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->year."' ";
        $query .= "    AND T1.CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_STD_T) ";
        $query .= "    AND T1.CHAIRCD IN (SELECT CHAIRCD FROM TESTITEM_T) ";
        $query .= "ORDER BY ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    L1.SUBCLASSABBV ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    SEMESTERNAME, ";
        $query .= "    SDATE, ";
        $query .= "    EDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト種別取得
    function getTestKind($model, $semester)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    TESTKINDCD || TESTITEMCD AS TESTKINDCD, ";
        $query .= "    TESTITEMNAME ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$semester."' ";
        $query .= "    AND TESTKINDCD < '99' ";
        $query .= "ORDER BY ";
        $query .= "    YEAR, ";
        $query .= "    SEMESTER, ";
        $query .= "    TESTKINDCD, ";
        $query .= "    TESTITEMCD ";

        return $query;
    }

    //得点データ取得
    function getScore($semester, $testkind, $subclasscd, $model)
    {
        $field = "SEM".$semester;
        if ($testkind == "0101") {
            $field .= "_INTR_SCORE";
        } else if ($testkind == "0102") {
            $field .= "_INTR2_SCORE";
        } else if ($testkind == "0201") {
            $field .= "_TERM_SCORE";
        } else if ($testkind == "0202") {
            $field .= "_TERM2_SCORE";
        }

        $query  = "SELECT ";
        $query .= "    ".$field." ";
        $query .= "FROM ";
        $query .= "    RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SUBCLASSCD = '".$subclasscd."' ";
        $query .= "    AND TAKESEMES = '0' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //順位取得
    function getRank($semester, $testkind, $subclasscd, $model, $grade, $div)
    {
        if ($grade > 3) {
            $field = "COURSE_".$div;
        } else {
            $field = "GRADE_".$div;
        }

        $query  = "SELECT ";
        $query .= $field." ";
        $query .= "FROM ";
        $query .= "    RECORD_RANK_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$semester."' ";
        $query .= "    AND TESTKINDCD || TESTITEMCD = '".$testkind."' ";
        $query .= "    AND SUBCLASSCD = '".$subclasscd."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //欠課数取得
    function getAbsent($semester, $subclasscd, $model)
    {
        $query  = "SELECT ";
        if ($model->absent_cov == 0 || $model->absent_cov_late == 0) {
            $query .= "    (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE)) AS KEKKA ";
        } else if ($model->absent_cov < 3) {
            $query .= "    (((SUM(LATE) + SUM(EARLY)) / ".$model->absent_cov_late.") + (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE))) AS KEKKA ";
        } else {
            $query .= "    DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / ".$model->absent_cov_late.") + (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE)),4,1) AS KEKKA ";
        }
        $query .= "FROM ";
        $query .= "    ATTEND_SUBCLASS_DAT ";
        $query .= "WHERE ";
        $query .= "    COPYCD = '0' ";
        $query .= "    AND YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$semester."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND SUBCLASSCD = '".$subclasscd."' ";

        return $query;
    }

}
?>
