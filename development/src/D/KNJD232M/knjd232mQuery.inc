<?php

require_once('for_php7.php');

class knjd232mQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

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

    //年組取得（権限チェック）
    function getAuth($model) {
        if ($model->field["SEMESTER"]=="9") {
            $semester = CTRL_SEMESTER;
        } else {
            $semester = $model->field["SEMESTER"];
        }
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME  AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '". CTRL_YEAR ."' ";
            $query .= " AND SEMESTER = '{$semester}' ";
            $query .= " AND GRADE    = '{$model->field["GRADE"]}'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE YEAR    = '" .$model->control["年度"] ."' ";
            $query .= " AND SEMESTER  = '{$semester}' ";
            $query .= " AND GRADE     = '{$model->field["GRADE"]}' ";
            $query .= " AND (   TR_CD1    = '" .STAFFCD ."' ";
            $query .= "      OR TR_CD2    = '" .STAFFCD ."' ";
            $query .= "      OR TR_CD3    = '" .STAFFCD ."' ";
            $query .= "      OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "      OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "      OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //テスト種別取得
    function GetName($seme) {
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
    
    //高校なのか中学なのか取得
    function getSchregRegdGdat($grade) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }
}
?>
