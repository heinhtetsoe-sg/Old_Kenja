<?php

require_once('for_php7.php');

class knjd622Query extends Query {

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
        $query .= "    AND SEMESTER < '9' ";
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
        $query .= "    AND SEMESTER = '".$sem ."' ";
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
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"] ."' ";
        $query .= "    AND TESTKINDCD || TESTITEMCD <> '9900' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //クラス一覧取得
    function getCourse($model)
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || '　' || L1.MAJORNAME || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= "          AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST L2 ON L2.COURSECODE = T1.COURSECODE  ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$sem ."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L1.MAJORNAME, ";
        $query .= "     L2.COURSECODENAME ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        return $query;
    }
}
?>
