<?php

require_once('for_php7.php');

class knjd624bQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $setSeme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE";

        return $query;
    }

    //テスト種別取得
    function getTestcd($model)
    {
        $query  = "SELECT ";
        $query .= "    TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "    TESTITEMNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //科目一覧取得
    function getSubclass($model)
    {
        $setScoreDiv = substr($model->field["TESTCD"], 0, 2) == "99" ? "00" : "01";
        $setSeme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "WITH SCH_T AS ( ";
        $query .= "SELECT ";
        $query .= "    SCHREGNO ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER = '".$setSeme."' AND ";
        $query .= "    GRADE = '".$model->field["GRADE"] ."' ";
        $query .= ") ";

        $query .= "SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "    T1.SUBCLASSCD AS VALUE, ";
            $query .= "    T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "    RECORD_SCORE_DAT T1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "    T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTCD"]."' AND ";
        $query .= "    T1.SCORE_DIV = '".$setScoreDiv."' AND ";
        $query .= "    T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";

        return $query;
    }
}
?>
