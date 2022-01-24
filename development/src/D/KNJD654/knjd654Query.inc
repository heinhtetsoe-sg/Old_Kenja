<?php

require_once('for_php7.php');

class knjd654Query extends Query {

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
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
    function getTestKind($semester) {
        $query  = " WITH MAIN_T(LABEL, VALUE, KINDCD, ITEMCD) AS ( ";
        $query .= " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || '　' || TESTITEMNAME, ";
        $query .= "     TESTKINDCD || TESTITEMCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     KINDCD, ";
        $query .= "     ITEMCD ";

        return $query;
    }
}
?>
