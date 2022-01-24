<?php

require_once('for_php7.php');


class knjp184kQuery extends Query {

    function getExpenseM() {
        $query  = " SELECT ";
        $query .= "     EXPENSE_M_CD AS VALUE, ";
        $query .= "     EXPENSE_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     EXPENSE_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXPENSE_L_CD = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getHrCourse() {
        $query  = " SELECT ";
        $query .= "     SUBSTR(HR_CLASS, 1, 1) AS HR_COURSE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     SUBSTR(HR_CLASS, 1, 1) ";
        $query .= " ORDER BY ";
        $query .= "     HR_COURSE ";

        return $query;
    }

    function getGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    function getPaidMoney($model, $grade) {
        $query  = " SELECT ";
        $query .= "     T1.PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "     AND T1.PAID_MONEY_DATE = '".str_replace("/", "-", $model->field["PAID_MONEY_DATE"])."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.GRADE = '".$grade."' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.PAID_MONEY ";
        $query .= " ORDER BY ";
        $query .= "     T1.PAID_MONEY ";

        return $query;
    }

    function getKingakuBetuCnt($model, $grade, $hrCourse, $money) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS PAID_CNT ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "     AND T1.PAID_MONEY_DATE = '".str_replace("/", "-", $model->field["PAID_MONEY_DATE"])."' ";
        $query .= "     AND T1.PAID_MONEY = ".$money." ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.GRADE = '".$grade."' ";
        $query .= "             AND SUBSTR(E1.HR_CLASS, 1, 1) = '".$hrCourse."' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";

        return $query;
    }

    function getKingakuBetuKuniCnt($model, $grade, $hrCourse, $money) {
        $yearMonth = "";
        if ($model->field["EXPENSE_M_CD"] == "11") {
            $yearMonth = CTRL_YEAR."04";
        } else if ($model->field["EXPENSE_M_CD"] == "12") {
            $yearMonth = CTRL_YEAR."09";
        } else if ($model->field["EXPENSE_M_CD"] == "13") {
            $yearMonth = CTRL_YEAR."12";
        }
        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "     AND T1.PAID_MONEY_DATE = '".str_replace("/", "-", $model->field["PAID_MONEY_DATE"])."' ";
        $query .= "     AND T1.PAID_MONEY = ".$money." ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.GRADE = '".$grade."' ";
        $query .= "             AND SUBSTR(E1.HR_CLASS, 1, 1) = '".$hrCourse."' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN T1.PAID_YEARMONTH = '".$yearMonth."' ";
        $query .= "              THEN T1.PAID_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "         + ";
        $query .= "         CASE WHEN T1.ADD_PAID_YEARMONTH = '".$yearMonth."' ";
        $query .= "              THEN T1.ADD_PAID_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCH_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";

        return $query;
    }

    function getKingakuBetuFukenCnt($model, $grade, $hrCourse, $money) {
        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "     AND T1.PAID_MONEY_DATE = '".str_replace("/", "-", $model->field["PAID_MONEY_DATE"])."' ";
        $query .= "     AND T1.PAID_MONEY = ".$money." ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.GRADE = '".$grade."' ";
        $query .= "             AND SUBSTR(E1.HR_CLASS, 1, 1) = '".$hrCourse."' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN T1.REDUC_DEC_FLG_1 = '1' AND '13' = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "              THEN T1.REDUCTIONMONEY_1 ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "         + ";
        $query .= "         CASE WHEN T1.REDUC_DEC_FLG_2 = '1' AND '13' = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "              THEN T1.REDUCTIONMONEY_2 ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS REDUCTIONMONEY_SUM ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCH_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";

        return $query;
    }

    function getKingakuBetuAdjustCnt($model, $grade, $hrCourse, $money) {
        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "     AND T1.PAID_MONEY_DATE = '".str_replace("/", "-", $model->field["PAID_MONEY_DATE"])."' ";
        $query .= "     AND T1.PAID_MONEY = ".$money." ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.GRADE = '".$grade."' ";
        $query .= "             AND SUBSTR(E1.HR_CLASS, 1, 1) = '".$hrCourse."' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SUM(TOTAL_ADJUSTMENT_MONEY) AS TOTAL_ADJUSTMENT_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_ADJUSTMENT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCH_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";

        return $query;
    }

    function getKingakuBetuBurdenCnt($model, $grade, $hrCourse, $money) {
        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "     AND T1.PAID_MONEY_DATE = '".str_replace("/", "-", $model->field["PAID_MONEY_DATE"])."' ";
        $query .= "     AND T1.PAID_MONEY = ".$money." ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.GRADE = '".$grade."' ";
        $query .= "             AND SUBSTR(E1.HR_CLASS, 1, 1) = '".$hrCourse."' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SUM(TOTAL_BURDEN_CHARGE) AS TOTAL_BURDEN_CHARGE ";
        $query .= " FROM ";
        $query .= "     REDUCTION_BURDEN_CHARGE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCH_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";

        return $query;
    }

    function getSeitoBetuMoney($model) {
        $monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE_T.NAME, ";
        $query .= "     I1.GRADE, ";
        $query .= "     SUBSTR(I1.HR_CLASS, 1, 1) AS HR_CLASS1, ";
        $query .= "     SUBSTR(I1.HR_CLASS, 2) AS HR_CLASS2, ";
        $query .= "     I1.ATTENDNO, ";
        $query .= "     T1.PAID_MONEY, ";
        $query .= "     T1.PAID_MONEY_DATE, ";
        $query .= "     L1.PASSNO, ";
        $query .= "     GUARD_T.GUARD_NAME, ";
        $query .= "     RED_C.OFFSET_FLG AS OFFSET_FLG_COUNTRY, ";
        foreach ($monthArray as $month) {
            $query .= "     REDUC_P".$month.".PAID_MONEY AS KIHON_".$month.", ";
            $query .= "     REDUC_P".$month.".ADD_PAID_MONEY AS KASAN_".$month.", ";
        }
        $query .= "     REDUC_D.PREF, ";
        $query .= "     REDUC_D.OFFSET_FLG, ";
        $query .= "     REDUC_D.LOCK_FLG, ";
        $query .= "     REDUC_D.REDUCTIONMONEY_1, ";
        $query .= "     REDUC_D.REDUC_DEC_FLG_1, ";
        $query .= "     REDUC_D.REDUCTIONMONEY_2, ";
        $query .= "     REDUC_D.REDUC_DEC_FLG_2, ";
        $query .= "     REDUC_D.REDUC_REMARK, ";
        $query .= "     ADJUST.TOTAL_ADJUSTMENT_MONEY, ";
        $query .= "     BURDEN.TOTAL_BURDEN_CHARGE ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT I1 ON  I1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           AND I1.YEAR = T1.YEAR ";
        $query .= "           AND I1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L1.DATA_DIV = '1' ";
        $query .= "          AND L1.DATA_DIV_SUB = '1' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE_T ON  BASE_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT GUARD_T ON  GUARD_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_DAT RED_C ON  RED_C.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND RED_C.YEAR = T1.YEAR ";
        $query .= "     LEFT JOIN REDUCTION_ADJUSTMENT_DAT ADJUST ON T1.YEAR = ADJUST.YEAR ";
        $query .= "          AND T1.SCHREGNO = ADJUST.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT BURDEN ON T1.YEAR = BURDEN.YEAR ";
        $query .= "          AND T1.SCHREGNO = BURDEN.SCHREGNO ";

        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             LT1.SCHREGNO, ";
        $query .= "             LL1.NAME1 AS PREF, ";
        $query .= "             LT1.OFFSET_FLG, ";
        $query .= "             LT1.LOCK_FLG, ";
        $query .= "             CASE WHEN LT1.REDUC_DEC_FLG_1 = '1' AND '13' = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "                  THEN LT1.REDUCTIONMONEY_1 ";
        $query .= "                  ELSE 0 ";
        $query .= "             END AS REDUCTIONMONEY_1, ";
        $query .= "             LT1.REDUC_DEC_FLG_1, ";
        $query .= "             CASE WHEN LT1.REDUC_DEC_FLG_2 = '1' AND '13' = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "                  THEN LT1.REDUCTIONMONEY_2 ";
        $query .= "                  ELSE 0 ";
        $query .= "             END AS REDUCTIONMONEY_2, ";
        $query .= "             LT1.REDUC_DEC_FLG_2, ";
        $query .= "             LT1.REDUC_REMARK ";
        $query .= "         FROM ";
        $query .= "             REDUCTION_DAT LT1 ";
        $query .= "             LEFT JOIN NAME_MST LL1 ON LL1.NAMECD1 = 'G202' ";
        $query .= "                  AND LL1.NAMECD2 = LT1.PREFECTURESCD ";
        $query .= "         WHERE ";
        $query .= "             LT1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     ) REDUC_D ON REDUC_D.SCHREGNO = T1.SCHREGNO ";

        $yearMonth = "";
        if ($model->field["EXPENSE_M_CD"] == "11") {
            $yearMonth = CTRL_YEAR."04";
        } else if ($model->field["EXPENSE_M_CD"] == "12") {
            $yearMonth = CTRL_YEAR."09";
        } else if ($model->field["EXPENSE_M_CD"] == "13") {
            $yearMonth = CTRL_YEAR."12";
        }

        foreach ($monthArray as $month) {
            $setPlanYear = $month > 3 ? CTRL_YEAR : CTRL_YEAR + 1;
            $query .= "     LEFT JOIN ( ";
            $query .= "         SELECT ";
            $query .= "             C_P".$month.".SCHREGNO, ";
            $query .= "             CASE WHEN VALUE(PAID_YEARMONTH, '999999') = '$yearMonth' ";
            $query .= "                  THEN C_P".$month.".PAID_MONEY ";
            $query .= "                  ELSE NULL ";
            $query .= "             END AS PAID_MONEY, ";
            $query .= "             CASE WHEN VALUE(ADD_PAID_YEARMONTH, '999999') = '$yearMonth' ";
            $query .= "                  THEN C_P".$month.".ADD_PAID_MONEY ";
            $query .= "                  ELSE NULL ";
            $query .= "             END AS ADD_PAID_MONEY ";
            $query .= "         FROM ";
            $query .= "             REDUCTION_COUNTRY_PLAN_DAT C_P".$month." ";
            $query .= "         WHERE ";
            $query .= "             C_P".$month.".YEAR = '".CTRL_YEAR."' ";
            $query .= "             AND C_P".$month.".PLAN_YEAR = '".$setPlanYear."' ";
            $query .= "             AND C_P".$month.".PLAN_MONTH = '".$month."' ";
            $query .= "     ) REDUC_P".$month." ON REDUC_P".$month.".SCHREGNO = T1.SCHREGNO ";
        }

        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "     AND T1.PAID_MONEY_DATE = '".str_replace("/", "-", $model->field["PAID_MONEY_DATE"])."' ";
        $query .= " ORDER BY ";
        $query .= "     I1.GRADE, ";
        $query .= "     SUBSTR(I1.HR_CLASS, 1, 1), ";
        $query .= "     SUBSTR(I1.HR_CLASS, 2), ";
        $query .= "     I1.ATTENDNO ";

        return $query;
    }

}
?>
