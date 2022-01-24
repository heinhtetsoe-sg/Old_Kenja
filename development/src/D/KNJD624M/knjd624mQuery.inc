<?php

require_once('for_php7.php');

class knjd624mQuery extends Query {
    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (SCHOOL_KIND = 'J' OR SCHOOL_KIND = 'H') ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト種別取得
    function getName($seme) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD||TESTITEMCD AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '$seme' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= " ORDER BY ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";

        return $query;
    }
}
?>
