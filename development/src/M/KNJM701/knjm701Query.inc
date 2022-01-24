<?php

require_once('for_php7.php');

class knjm701Query extends Query {

    //年組取得(権限チェック)
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT ";
            $query .= "    GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR         = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT ";
            $query .= "    GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR         = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "    AND (   TR_CD1 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || L1.NAME AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
