<?php

require_once('for_php7.php');


class knje373Query extends Query {

    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT T2 ON  T1.YEAR = T2.YEAR ";
        $query .= "                         AND T1.GRADE = T2.GRADE ";
        $query .= "                         AND T2.SCHOOL_KIND IN ('H') ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
