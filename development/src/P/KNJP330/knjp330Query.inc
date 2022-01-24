<?php

require_once('for_php7.php');


class knjp330Query extends Query {

    //対象者リストToリスト
    function GetTaisyouWith() {
        $query  = " WITH REDUCTION_DAT_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT t1 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ((REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null) ";
        $query .= "           OR ";
        $query .= "          (REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null)) ";
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), BASE_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND PAID_YEARMONTH IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ), ADD_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND ADD_PAID_YEARMONTH IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ), SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT_SUM ";
        $query .= " UNION ";
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

    //都道府県取得
    function GetPrefecturescd($select)
    {

        $query  = knjp330Query::GetTaisyouWith();
        $query .= "SELECT DISTINCT ";
        if ($select == "COMB") {
            $query .= "    L3.NAMECD2 AS VALUE, ";
            $query .= "    L3.NAME1 AS LABEL ";
        } else {
            $query .= "    L3.NAMECD2 AS VALUE, ";
            $query .= "    L3.NAMECD2 || '　' || L3.NAME1 AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "    SCH_T ";
        $query .= "    LEFT JOIN GUARDIAN_DAT L1 ON SCH_T.SCHREGNO = L1.SCHREGNO ";
        $query .= "    LEFT JOIN ZIPCD_MST L2 ON L1.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ";
        $query .= "    LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'G202' ";
        $query .= "         AND SUBSTR(L2.CITYCD, 1, 2) = L3.NAMECD2 ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //クラス取得
    function GetClass($model, $select)
    {

        $query  = knjp330Query::GetTaisyouWith();
        $query .= "SELECT DISTINCT ";
        if ($select == "COMB") {
            $query .= "    T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "    T1.HR_NAME AS LABEL ";
        } else {
            $query .= "    T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "    T1.GRADE || T1.HR_CLASS || '　' || T1.HR_NAME AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1, ";
        $query .= "    SCHREG_REGD_DAT T2 ";
        $query .= "    LEFT JOIN GUARDIAN_DAT L1 ON T2.SCHREGNO = L1.SCHREGNO ";
        $query .= "    LEFT JOIN ZIPCD_MST L2 ON L1.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS = T2.GRADE || T2.HR_CLASS ";
        $query .= "    AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T2.SCHREGNO IN ( ";
        $query .= "        SELECT ";
        $query .= "            SCH_T.SCHREGNO ";
        $query .= "        FROM ";
        $query .= "            SCH_T ";
        $query .= "        ) ";
        if ($model->todoufuken != 99){
            $query .= "    AND SUBSTR(L2.CITYCD, 1, 2) = '".$model->todoufuken."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    T1.GRADE || T1.HR_CLASS ";

        return $query;
    }

    //個人取得
    function GetSchreg($model)
    {

        $query  = knjp330Query::GetTaisyouWith();
        $query .= "SELECT DISTINCT ";
        $query .= "    T1.SCHREGNO AS VALUE, ";
        $query .= "    T1.ATTENDNO || '番　' || T2.NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN GUARDIAN_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "    LEFT JOIN ZIPCD_MST L2 ON L1.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.SCHREGNO IN ( ";
        $query .= "        SELECT ";
        $query .= "            SCH_T.SCHREGNO ";
        $query .= "        FROM ";
        $query .= "            SCH_T ";
        $query .= "        ) ";
        if ($model->todoufuken != 99){
            $query .= "    AND SUBSTR(L2.CITYCD, 1, 2) = '".$model->todoufuken."' ";
        }
        if ($model->hrclass != 99){
            $query .= "    AND T1.GRADE || T1.HR_CLASS = '".$model->hrclass."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    LABEL ";

        return $query;
    }

    function todoufukenMeisai($model, $inState) {
        $query  = " WITH REDUCTION_DAT_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.PREFECTURESCD, ";
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "     0 AS REDUCTIONMONEY ";
        } else {
            $query .= "     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null ";
            $query .= "              THEN t1.REDUCTIONMONEY_1 ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "         + ";
            $query .= "         CASE WHEN REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null ";
            $query .= "              THEN t1.REDUCTIONMONEY_2 ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "     ) AS REDUCTIONMONEY ";
        }
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT t1 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ((REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null) ";
        $query .= "           OR ";
        $query .= "          (REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null)) ";
        $query .= " GROUP BY ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.PREFECTURESCD ";
        $query .= " ), BASE_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS B_MIN, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS B_MAX, ";
        $query .= "     VALUE(SUM(PAID_MONEY), 0) AS B_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND PAID_YEARMONTH IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), ADD_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS A_MIN, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS A_MAX, ";
        $query .= "     VALUE(SUM(ADD_PAID_MONEY), 0) AS A_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND ADD_PAID_YEARMONTH IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT_SUM ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     BASE_MONEY ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ADD_MONEY ";
        $query .= " ), MONEYTBL AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     t5.GUARANTOR_NAME, ";
        $query .= "     t3.HR_NAME, ";
        $query .= "     t2.ATTENDNO, ";
        $query .= "     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ";
        $query .= "     t4.NAME, ";
        $query .= "     L3.NAME1, ";
        $query .= "     t7.MONEY_DUE, ";
        $query .= "     VALUE(t1.REDUCTIONMONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) AS REDUCTIONMONEY, ";
        $query .= "     BASE_M.B_PAID_MONEY, ";
        $query .= "     SUBSTR(BASE_M.B_MIN, 5) AS B_MIN, ";
        $query .= "     SUBSTR(BASE_M.B_MAX, 5) AS B_MAX, ";
        $query .= "     ADD_M.A_PAID_MONEY, ";
        $query .= "     SUBSTR(ADD_M.A_MIN, 5) AS A_MIN, ";
        $query .= "     SUBSTR(ADD_M.A_MAX, 5) AS A_MAX, ";
        $query .= "     '162' AS FEE, ";
        $query .= "     VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) AS ADJUSTMENT_MONEY, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN '1' ELSE '0' END FRMID, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)  THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END MONEY, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) + 162 ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END TOTAL_MONEY ";
        $query .= " FROM ";
        $query .= "     SCH_T ";
        $query .= "     LEFT JOIN REDUCTION_DAT_SUM t1 ON SCH_T.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ";
        $query .= "     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "          AND t2.YEAR = SCH_T.YEAR ";
        $query .= "          AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ";
        $query .= "          AND t3.YEAR = SCH_T.YEAR ";
        $query .= "          AND t3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     INNER JOIN ZIPCD_MST L2 ON t5.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ";
        $query .= "           AND SUBSTR(L2.CITYCD, 1, 2) IN ".$inState." ";
        $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'G202' ";
        $query .= "          AND SUBSTR(L2.CITYCD, 1, 2) = L3.NAMECD2 ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT t7 ON t7.YEAR = SCH_T.YEAR ";
        $query .= "          AND t7.SCHREGNO = SCH_T.SCHREGNO ";
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "          AND t7.EXPENSE_M_CD = '12' ";
        } else {
            $query .= "          AND t7.EXPENSE_M_CD = '13' ";
        }
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "     LEFT JOIN (SELECT L_ADJUST.YEAR, L_ADJUST.SCHREGNO, 0 AS TOTAL_ADJUSTMENT_MONEY FROM REDUCTION_ADJUSTMENT_DAT L_ADJUST) ADJUST ON SCH_T.YEAR = ADJUST.YEAR ";
            $query .= "          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ";
        } else {
            $query .= "     LEFT JOIN REDUCTION_ADJUSTMENT_DAT ADJUST ON SCH_T.YEAR = ADJUST.YEAR ";
            $query .= "          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ";
        }
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT BURDEN ON BURDEN.YEAR = SCH_T.YEAR ";
        $query .= "          AND BURDEN.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MONEYTBL ";
        $query .= " ORDER BY ";
        $query .= "     GRD_CLASS ";

        return $query;
    }

    function classMeisai($model, $inState) {
        $query  = " WITH REDUCTION_DAT_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.PREFECTURESCD, ";
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "     0 AS REDUCTIONMONEY ";
        } else {
            $query .= "     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null ";
            $query .= "              THEN t1.REDUCTIONMONEY_1 ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "         + ";
            $query .= "         CASE WHEN REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null ";
            $query .= "              THEN t1.REDUCTIONMONEY_2 ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "     ) AS REDUCTIONMONEY ";
        }
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT t1 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ((REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null) ";
        $query .= "           OR ";
        $query .= "          (REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null)) ";
        $query .= " GROUP BY ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.PREFECTURESCD ";
        $query .= " ), BASE_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS B_MIN, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS B_MAX, ";
        $query .= "     VALUE(SUM(PAID_MONEY), 0) AS B_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND PAID_YEARMONTH IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), ADD_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS A_MIN, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS A_MAX, ";
        $query .= "     VALUE(SUM(ADD_PAID_MONEY), 0) AS A_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND ADD_PAID_YEARMONTH IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT_SUM ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     BASE_MONEY ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ADD_MONEY ";
        $query .= " ), MONEYTBL AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     t5.GUARANTOR_NAME, ";
        $query .= "     t3.HR_NAME, ";
        $query .= "     t2.ATTENDNO, ";
        $query .= "     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ";
        $query .= "     t4.NAME, ";
        $query .= "     L3.NAME1, ";
        $query .= "     t7.MONEY_DUE, ";
        $query .= "     VALUE(t1.REDUCTIONMONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) AS REDUCTIONMONEY, ";
        $query .= "     BASE_M.B_PAID_MONEY, ";
        $query .= "     SUBSTR(BASE_M.B_MIN, 5) AS B_MIN, ";
        $query .= "     SUBSTR(BASE_M.B_MAX, 5) AS B_MAX, ";
        $query .= "     ADD_M.A_PAID_MONEY, ";
        $query .= "     SUBSTR(ADD_M.A_MIN, 5) AS A_MIN, ";
        $query .= "     SUBSTR(ADD_M.A_MAX, 5) AS A_MAX, ";
        $query .= "     '162' AS FEE, ";
        $query .= "     VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) AS ADJUSTMENT_MONEY, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN '1' ELSE '0' END FRMID, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END MONEY, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) + 162 ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END TOTAL_MONEY ";
        $query .= " FROM ";
        $query .= "     SCH_T ";
        $query .= "     LEFT JOIN REDUCTION_DAT_SUM t1 ON SCH_T.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ";
        $query .= "     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "           AND t2.YEAR = SCH_T.YEAR ";
        $query .= "           AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "           AND t2.GRADE || t2.HR_CLASS IN ".$inState." ";
        $query .= "           AND t2.YEAR = SCH_T.YEAR ";
        $query .= "           AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ";
        $query .= "          AND t3.YEAR = SCH_T.YEAR ";
        $query .= "          AND t3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     INNER JOIN ZIPCD_MST L2 ON t5.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ";
        if ($model->todoufuken != "99") {
            $query .= "           AND SUBSTR(L2.CITYCD, 1, 2) = '".$model->todoufuken."' ";
        }
        $query .= "     INNER JOIN NAME_MST L3 ON L3.NAMECD1 = 'G202' ";
        $query .= "           AND SUBSTR(L2.CITYCD, 1, 2) = L3.NAMECD2 ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT t7 ON t7.YEAR = SCH_T.YEAR ";
        $query .= "          AND t7.SCHREGNO = SCH_T.SCHREGNO ";
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "          AND t7.EXPENSE_M_CD = '12' ";
        } else {
            $query .= "          AND t7.EXPENSE_M_CD = '13' ";
        }
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "     LEFT JOIN (SELECT L_ADJUST.YEAR, L_ADJUST.SCHREGNO, 0 AS TOTAL_ADJUSTMENT_MONEY FROM REDUCTION_ADJUSTMENT_DAT L_ADJUST) ADJUST ON SCH_T.YEAR = ADJUST.YEAR ";
            $query .= "          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ";
        } else {
            $query .= "     LEFT JOIN REDUCTION_ADJUSTMENT_DAT ADJUST ON SCH_T.YEAR = ADJUST.YEAR ";
            $query .= "          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ";
        }
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT BURDEN ON BURDEN.YEAR = SCH_T.YEAR ";
        $query .= "          AND BURDEN.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MONEYTBL ";
        $query .= " ORDER BY ";
        $query .= "     GRD_CLASS ";

        return $query;
    }

    function kojinMeisai($model, $inState) {
        $query  = " WITH REDUCTION_DAT_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.PREFECTURESCD, ";
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "     0 AS REDUCTIONMONEY ";
        } else {
            $query .= "     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null ";
            $query .= "              THEN t1.REDUCTIONMONEY_1 ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "         + ";
            $query .= "         CASE WHEN REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null ";
            $query .= "              THEN t1.REDUCTIONMONEY_2 ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "     ) AS REDUCTIONMONEY ";
        }
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT t1 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t1.SCHREGNO IN ".$inState." ";
        $query .= "     AND ((REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null) ";
        $query .= "           OR ";
        $query .= "          (REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null)) ";
        $query .= " GROUP BY ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.PREFECTURESCD ";
        $query .= " ), BASE_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS B_MIN, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS B_MAX, ";
        $query .= "     VALUE(SUM(PAID_MONEY), 0) AS B_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN ".$inState." ";
        $query .= "     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND PAID_YEARMONTH IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), ADD_MONEY AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS A_MIN, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS A_MAX, ";
        $query .= "     VALUE(SUM(ADD_PAID_MONEY), 0) AS A_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN ".$inState." ";
        $query .= "     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND ADD_PAID_YEARMONTH IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT_SUM ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     BASE_MONEY ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ADD_MONEY ";
        $query .= " ), MONEYTBL AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     t5.GUARANTOR_NAME, ";
        $query .= "     t3.HR_NAME, ";
        $query .= "     t2.ATTENDNO, ";
        $query .= "     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ";
        $query .= "     t4.NAME, ";
        $query .= "     L3.NAME1, ";
        $query .= "     t7.MONEY_DUE, ";
        $query .= "     VALUE(t1.REDUCTIONMONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) AS REDUCTIONMONEY, ";
        $query .= "     BASE_M.B_PAID_MONEY, ";
        $query .= "     SUBSTR(BASE_M.B_MIN, 5) AS B_MIN, ";
        $query .= "     SUBSTR(BASE_M.B_MAX, 5) AS B_MAX, ";
        $query .= "     ADD_M.A_PAID_MONEY, ";
        $query .= "     SUBSTR(ADD_M.A_MIN, 5) AS A_MIN, ";
        $query .= "     SUBSTR(ADD_M.A_MAX, 5) AS A_MAX, ";
        $query .= "     '162' AS FEE, ";
        $query .= "     VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) AS ADJUSTMENT_MONEY, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN '1' ELSE '0' END FRMID, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END MONEY, ";
        $query .= "     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) + 162 ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END TOTAL_MONEY ";
        $query .= " FROM ";
        $query .= "     SCH_T ";
        $query .= "     LEFT JOIN REDUCTION_DAT_SUM t1 ON SCH_T.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ";
        $query .= "     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "          AND t2.YEAR = SCH_T.YEAR ";
        $query .= "          AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ";
        $query .= "          AND t3.YEAR = SCH_T.YEAR ";
        $query .= "          AND t3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     INNER JOIN ZIPCD_MST L2 ON t5.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ";
        $query .= "     INNER JOIN NAME_MST L3 ON L3.NAMECD1 = 'G202' ";
        $query .= "           AND SUBSTR(L2.CITYCD, 1, 2) = L3.NAMECD2 ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT t7 ON t7.YEAR = SCH_T.YEAR ";
        $query .= "          AND t7.SCHREGNO = SCH_T.SCHREGNO ";
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "          AND t7.EXPENSE_M_CD = '12' ";
        } else {
            $query .= "          AND t7.EXPENSE_M_CD = '13' ";
        }
        if ($model->field["YOUSIKI"] == "1") {
            $query .= "     LEFT JOIN (SELECT L_ADJUST.YEAR, L_ADJUST.SCHREGNO, 0 AS TOTAL_ADJUSTMENT_MONEY FROM REDUCTION_ADJUSTMENT_DAT L_ADJUST) ADJUST ON SCH_T.YEAR = ADJUST.YEAR ";
            $query .= "          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ";
        } else {
            $query .= "     LEFT JOIN REDUCTION_ADJUSTMENT_DAT ADJUST ON SCH_T.YEAR = ADJUST.YEAR ";
            $query .= "          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ";
        }
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT BURDEN ON BURDEN.YEAR = SCH_T.YEAR ";
        $query .= "          AND BURDEN.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MONEYTBL ";
        $query .= " ORDER BY ";
        $query .= "     GRD_CLASS ";

        return $query;
    }
}
?>
