<?php

require_once('for_php7.php');


class knjp331Query extends Query {

    //対象者リストToリスト
    function GetTaisyouWith() {
        $query  = " WITH BASE_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(PAID_MONEY) AS PAID_MONEY, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS MIN_MONTH, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS MAX_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND PAID_YEARMONTH = '".(CTRL_YEAR)."12' ";
        $query .= "     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ), ADD_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(ADD_PAID_MONEY) AS ADD_PAID_MONEY, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS MIN_MONTH, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS MAX_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ADD_PAID_YEARMONTH = '".(CTRL_YEAR)."12' ";
        $query .= "     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        $query .= " ), MONEY_DUE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(MONEY_DUE) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND EXPENSE_M_CD IN ('11', '12', '13') ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ), REDUC_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ";
        $query .= "              THEN VALUE(REDUCTIONMONEY_1, 0) ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "         + ";
        $query .= "         CASE WHEN REDUC_DEC_FLG_2 = '1' ";
        $query .= "              THEN VALUE(REDUCTIONMONEY_2, 0) ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS REDUCTIONMONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND (REDUC_DEC_FLG_1 = '1' ";
        $query .= "         OR ";
        $query .= "         REDUC_DEC_FLG_2 = '1') ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ), REDUC_COUNTRY_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "              THEN VALUE(T1.PLAN_MONEY, 0) ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "         + ";
        $query .= "         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS REDUCTION_C_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "         OR ";
        $query .= "         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ";
        $query .= "    AND EXISTS( ";
        $query .= "           SELECT ";
        $query .= "               'x' ";
        $query .= "           FROM ";
        $query .= "               REDUC_T E1 ";
        $query .= "           WHERE ";
        $query .= "               E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "    ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ), SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     BASE_MONEY ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ADD_MONEY ";
        $query .= " ) ";

        return $query;
    }

    //クラス取得
    function GetClass($model, $select)
    {
        $query  = knjp331Query::GetTaisyouWith();
        $query .= " SELECT DISTINCT ";
        if ($select == "COMB") {
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.GRADE || T1.HR_CLASS || '　' || T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = T2.GRADE || T2.HR_CLASS ";
        $query .= "     AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             SCH_T.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCH_T ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE || T1.HR_CLASS ";

        return $query;
    }

    //個人取得
    function GetSchreg($model)
    {
        $query  = knjp331Query::GetTaisyouWith();
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番　' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             SCH_T.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCH_T ";
        $query .= "         ) ";
        if ($model->hrclass != 99){
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->hrclass."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

    function classMeisai($model, $inState) {
        $query  = knjp331Query::GetTaisyouWith();
        $query .= " SELECT ";
        $query .= "     t5.GUARANTOR_NAME, ";
        $query .= "     t3.HR_NAME, ";
        $query .= "     t2.ATTENDNO, ";
        $query .= "     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ";
        $query .= "     t4.NAME, ";
        $query .= "     VALUE(BASE_M.PAID_MONEY, 0) AS PAID_MONEY, ";
        $query .= "     VALUE(ADD_M.ADD_PAID_MONEY, 0) AS ADD_PAID_MONEY, ";
        $query .= "     VALUE(OVER_M.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY, ";
        $query .= "     VALUE(BASE_M.PAID_MONEY, 0) + VALUE(ADD_M.ADD_PAID_MONEY, 0) + VALUE(OVER_M.TOTAL_BURDEN_CHARGE, 0) AS TOTAL, ";
        $query .= "     BASE_M.MIN_MONTH AS B_MIN, ";
        $query .= "     BASE_M.MAX_MONTH AS B_MAX, ";
        $query .= "     ADD_M.MIN_MONTH AS A_MIN, ";
        $query .= "     ADD_M.MAX_MONTH AS A_MAX ";
        $query .= " FROM ";
        $query .= "     SCH_T ";
        $query .= "     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ";
        $query .= "     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT OVER_M ON OVER_M.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND SCH_T.SCHREGNO = OVER_M.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "           AND t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "           AND t2.GRADE || t2.HR_CLASS IN ".$inState." ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ";
        $query .= "          AND t3.YEAR = t2.YEAR ";
        $query .= "          AND t3.SEMESTER = t2.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     GRD_CLASS ";

        return $query;
    }

    function kojinMeisai($model, $inState) {
        $query  = knjp331Query::GetTaisyouWith();
        $query .= " SELECT ";
        $query .= "     t5.GUARANTOR_NAME, ";
        $query .= "     t3.HR_NAME, ";
        $query .= "     t2.ATTENDNO, ";
        $query .= "     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ";
        $query .= "     t4.NAME, ";
        $query .= "     VALUE(BASE_M.PAID_MONEY, 0) AS PAID_MONEY, ";
        $query .= "     VALUE(ADD_M.ADD_PAID_MONEY, 0) AS ADD_PAID_MONEY, ";
        $query .= "     VALUE(OVER_M.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY, ";
        $query .= "     VALUE(BASE_M.PAID_MONEY, 0) + VALUE(ADD_M.ADD_PAID_MONEY, 0) + VALUE(OVER_M.TOTAL_BURDEN_CHARGE, 0) AS TOTAL, ";
        $query .= "     BASE_M.MIN_MONTH AS B_MIN, ";
        $query .= "     BASE_M.MAX_MONTH AS B_MAX, ";
        $query .= "     ADD_M.MIN_MONTH AS A_MIN, ";
        $query .= "     ADD_M.MAX_MONTH AS A_MAX ";
        $query .= " FROM ";
        $query .= "     SCH_T ";
        $query .= "     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ";
        $query .= "     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT OVER_M ON OVER_M.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND SCH_T.SCHREGNO = OVER_M.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "           AND t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ";
        $query .= "          AND t3.YEAR = t2.YEAR ";
        $query .= "          AND t3.SEMESTER = t2.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     SCH_T.SCHREGNO IN ".$inState." ";
        $query .= " ORDER BY ";
        $query .= "     GRD_CLASS ";

        return $query;
    }
}
?>
