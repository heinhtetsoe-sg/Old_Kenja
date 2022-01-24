<?php

require_once('for_php7.php');

class knjd615fQuery extends Query {

    //学校名取得
    function getZ010() {

        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }

    //学期取得
    function getSemester() {

        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学年取得
    function getGrade() {

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($semester, $testkindcd="")
    {
        $query  = " WITH MAIN_T(LABEL, VALUE, KINDCD, ITEMCD, SEMESTER_DETAIL) AS ( ";
        $query .= "     SELECT ";
        $query .= "         TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME, ";
        $query .= "         TESTKINDCD || TESTITEMCD, ";
        $query .= "         TESTKINDCD, ";
        $query .= "         TESTITEMCD, ";
        $query .= "         SEMESTER_DETAIL ";
        $query .= "     FROM ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$semester."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE, ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        if($testkindcd){
            $query .= " WHERE ";
            $query .= "     VALUE = '".$testkindcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     KINDCD, ";
        $query .= "     ITEMCD ";

        return $query;
    }

    //集計範囲取得
    function getSemesDetail($detail)
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER_DETAIL = '".$detail."' ";
        $query .= " ORDER BY ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";

        return $query;
    }


    //学期詳細マスタ（終了日付の取得）
    function getSemesterDetailMst($year, $semesterDetail) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTER_DETAIL, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER_DETAIL = '{$semesterDetail}' ";
        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {

        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     SEMESTER='".CTRL_SEMESTER ."' AND ";
        } else {
            $query .= "     SEMESTER='".$model->field["SEMESTER"] ."' AND ";
        }
        $query .= "     GRADE = '".$model->field["GRADE"]."' ";
        //制限付き
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }

        return $query;
    }
}
?>
