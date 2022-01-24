<?php

require_once('for_php7.php');

class knjm702Query extends Query {

    //年組取得(権限チェック)
    function getHrClass() {
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
}
?>
