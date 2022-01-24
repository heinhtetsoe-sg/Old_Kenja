<?php

require_once('for_php7.php');

class knjd624Query extends Query {

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
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    CAST(SMALLINT(GRADE) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER='".$sem ."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE";

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
        $query .= "    YEAR='".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"] ."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //科目一覧取得
    function getSubclass($model)
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "WITH SCH_T AS ( ";
        $query .= "SELECT ";
        $query .= "    SCHREGNO ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$sem ."' ";
        $query .= "    AND GRADE = '".$model->field["GRADE"] ."' ";
        $query .= ") ";
        $query .= "SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "    T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "    CASE T1.SUBCLASSCD WHEN '333333' THEN '平均点(3科目)' ";
        $query .= "                       WHEN '555555' THEN '平均点(5科目)' ";
        $query .= "                       WHEN '999999' THEN '平均点(全科目)' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                       ELSE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME END AS LABEL ";
        } else {
            $query .= "                       ELSE T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME END AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "    RECORD_RANK_DAT T1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTCD"] ."' ";
        $query .= "    AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";

        return $query;
    }
}
?>
