<?php

require_once('for_php7.php');

class knjd231tQuery extends Query
{
    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     AND GRADE IN(SELECT  ";
            $query .= "                     GRADE ";
            $query .= "                  FROM ";
            $query .= "                     SCHREG_REGD_HDAT ";
            $query .= "                  WHERE ";
            $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "                     (TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "                      TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "                      TR_CD3 = '" .STAFFCD ."' OR ";
            $query .= "                      SUBTR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "                      SUBTR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "                      SUBTR_CD3 = '" .STAFFCD ."')) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || '  ' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     SEMESTER = '".$model->field["SEMESTER"] ."' AND ";
        }
        $query .= "     TESTKINDCD IN('01','02') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "          TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "          TR_CD3 = '" .STAFFCD ."' OR ";
            $query .= "          SUBTR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "          SUBTR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "          SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
