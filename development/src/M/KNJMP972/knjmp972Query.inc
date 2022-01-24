<?php

require_once('for_php7.php');

class knjmp972Query extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade() {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //収入科目取得
    function getIncomeLMst($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.INCOME_L_CD AS VALUE, ";
        $query .= "     T1.INCOME_L_CD || ':' || L1.LEVY_L_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.INCOME_L_CD = L1.LEVY_L_CD ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON T1.YEAR = INCOME_SCH.YEAR ";
        $query .= "          AND T1.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ";
        $query .= "          AND T1.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ";
        $query .= "          AND T1.REQUEST_NO = INCOME_SCH.REQUEST_NO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(T1.INCOME_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス取得
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND ";
            $query .= "    (TR_CD1      = '".STAFFCD."' OR ";
            $query .= "     TR_CD2      = '".STAFFCD."' OR ";
            $query .= "     TR_CD3      = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
