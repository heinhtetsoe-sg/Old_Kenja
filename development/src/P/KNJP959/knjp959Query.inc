<?php

require_once('for_php7.php');

class knjp959Query extends Query {

    //クラス取得
    function GetClass($grade) {

        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE || HR_CLASS AS CD, ";
        $query .= "    HR_NAME AS NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND GRADE = '{$grade}' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE || HR_CLASS ";

        return $query;
    }

    //学年取得
    function GetGrade() {

        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS CD, ";
        $query .= "    GRADE AS NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE ";

        return $query;
    }

    //学年またはクラス名
    function getHrAndStaffName($model, $gradeHr) {
        $query  = "SELECT ";
        $query .= "    HDAT.HR_NAME, ";
        $query .= "    STAFF.STAFFNAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT HDAT ";
        $query .= "    LEFT JOIN STAFF_MST STAFF ON HDAT.TR_CD1 = STAFF.STAFFCD ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND GRADE || HR_CLASS = '{$gradeHr}' ";

        return $query;
    }

    //CSV
    function getCsvData($model, $gradeHr) {
        $query .= "SELECT ";
        $query .= "    REGD.GRADE, ";
        $query .= "    REGD.HR_CLASS, ";
        $query .= "    REGD.ATTENDNO, ";
        $query .= "    REGD.SCHREGNO, ";
        $query .= "    BASE.NAME, ";
        foreach ($model->monthArray as $key => $month) {
            $query .= "    SUM(CASE WHEN MONTH(COL_M.PAY_DATE) = {$month} THEN VALUE(DUE_M.MONEY_DUE, 0) ELSE 0 END) AS MONEY_DUE{$month}, ";
            $query .= "    SUM(CASE WHEN MONTH(COL_M.PAY_DATE) = {$month} THEN VALUE(PAID_M.PAID_MONEY, 0) ELSE 0 END) AS PAID_MONEY{$month}, ";
            $query .= "    SUM(CASE WHEN MONTH(COL_M.PAY_DATE) = {$month} THEN VALUE(DUE_M.MONEY_DUE, 0) ELSE 0 END) ";
            $query .= "     - ";
            $query .= "    SUM(CASE WHEN MONTH(COL_M.PAY_DATE) = {$month} THEN VALUE(PAID_M.PAID_MONEY, 0) ELSE 0 END) ";
            $query .= "    AS MINOU{$month}, ";
        }
        $query .= "    '' AS REMARK ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT REGD ";
        $query .= "    INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "    LEFT JOIN COLLECT_MONEY_DUE_M_DAT DUE_M ON REGD.YEAR = DUE_M.YEAR ";
        $query .= "         AND REGD.SCHREGNO = DUE_M.SCHREGNO ";
        $query .= "    LEFT JOIN COLLECT_M_MST COL_M ON DUE_M.YEAR = COL_M.YEAR ";
        $query .= "         AND DUE_M.COLLECT_L_CD = COL_M.COLLECT_L_CD ";
        $query .= "         AND DUE_M.COLLECT_M_CD = COL_M.COLLECT_M_CD ";
        $query .= "    LEFT JOIN COLLECT_MONEY_PAID_M_DAT PAID_M ON DUE_M.YEAR = PAID_M.YEAR ";
        $query .= "         AND DUE_M.SCHREGNO = PAID_M.SCHREGNO ";
        $query .= "         AND DUE_M.COLLECT_GRP_CD = PAID_M.COLLECT_GRP_CD ";
        $query .= "         AND DUE_M.COLLECT_L_CD = PAID_M.COLLECT_L_CD ";
        $query .= "         AND DUE_M.COLLECT_M_CD = PAID_M.COLLECT_M_CD ";
        $query .= "WHERE ";
        $query .= "    REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND REGD.GRADE || REGD.HR_CLASS = '{$gradeHr}' ";
        $query .= "GROUP BY ";
        $query .= "    REGD.GRADE, ";
        $query .= "    REGD.HR_CLASS, ";
        $query .= "    REGD.ATTENDNO, ";
        $query .= "    REGD.SCHREGNO, ";
        $query .= "    BASE.NAME ";
        $query .= "ORDER BY ";
        $query .= "    REGD.GRADE, ";
        $query .= "    REGD.HR_CLASS, ";
        $query .= "    REGD.ATTENDNO, ";
        $query .= "    REGD.SCHREGNO ";

        return $query;
    }
}
?>
