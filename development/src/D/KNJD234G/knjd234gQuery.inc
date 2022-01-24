<?php

require_once('for_php7.php');

class knjd234gQuery extends Query {

    //名称マスタ
    function getNameMst($model, $namecd1) {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //テスト種別
    function getTest($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //評定マスタ
    function getAssessHigh() {
        $query  = " SELECT ";
        $query .= "     INT(T1.ASSESSHIGH) AS ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     ASSESS_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ASSESSCD = '3' ";
        $query .= "     AND T1.ASSESSLEVEL = 1 ";

        return $query;
    }

    //学期詳細マスタ(日付の取得)
    function getSemesterDetailMst($semesterDetail) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTER_DETAIL, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     VALUE(SDATE, '9999-12-31') AS SDATE, ";
        $query .= "     VALUE(EDATE, '9999-12-31') AS EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER_DETAIL = '{$semesterDetail}' ";

        return $query;
    }

    function get_semester_detail($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->test_cd}' ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || ':' || T1.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND T1.GRADE IN ";
            $query .= "           (SELECT ";
            $query .= "                FIELD2 ";
            $query .= "            FROM ";
            $query .= "                STAFF_DETAIL_MST ST ";
            $query .= "            WHERE ";
            $query .= "                T1.YEAR = ST.YEAR ";
            $query .= "                AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                AND ST.FIELD1 = '0200') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
