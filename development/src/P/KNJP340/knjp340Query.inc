<?php

require_once('for_php7.php');

class knjp340Query extends Query {

    //クラス
    function GetExamClasscd($model){

        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE || T1.HR_CLASS AS CD, ";
        $query .= "    T1.HR_NAME AS NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1, ";
        $query .= "    SCHREG_REGD_DAT T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS = T2.GRADE || T2.HR_CLASS ";
        $query .= "    AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T2.SCHREGNO IN ( ";
        //帳票種別 1:国 2:府県 3:合計・・・補助金がある生徒
        if ($model->outdiv == 2 || $model->outdiv == 3) {
            $query .= "        SELECT ";
            $query .= "            SCHREGNO ";
            $query .= "        FROM ";
            $query .= "            REDUCTION_DAT ";
            $query .= "        WHERE ";
            $query .= "            YEAR = '".CTRL_YEAR."' ";
            $query .= "            AND (REDUCTIONMONEY_1 is not null ";
            $query .= "                OR ";
            $query .= "                 REDUCTIONMONEY_2 is not null) ";
        }
        if ($model->outdiv == 3) {
            $query .= "        UNION ";
        }
        if ($model->outdiv == 1 || $model->outdiv == 3) {
            $query .= "        SELECT ";
            $query .= "            SCHREGNO ";
            $query .= "        FROM ";
            $query .= "            REDUCTION_COUNTRY_PLAN_DAT ";
            $query .= "        WHERE ";
            $query .= "            YEAR = '".CTRL_YEAR."' ";
            $query .= "            AND (PAID_MONEY is not null ";
            $query .= "                OR ";
            $query .= "                 ADD_PAID_MONEY is not null) ";
        }
        $query .= "    ) ";
        $query .= "ORDER BY ";
        $query .= "    T1.GRADE || T1.HR_CLASS ";

        return $query;
    }

    //相殺年月
    function getYearMonth()
    {
        $year = CTRL_YEAR;
//        $query  = " SELECT DISTINCT ";
        $query  = " SELECT ";
        $query .= "     PAID_YEARMONTH AS VALUE, ";
        $query .= "     substr(PAID_YEARMONTH, 1, 4) || '年' || substr(PAID_YEARMONTH, 5) || '月' AS LABEL ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND PAID_YEARMONTH IS NOT NULL ";
        $query .= "     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     ADD_PAID_YEARMONTH AS VALUE, ";
        $query .= "     substr(ADD_PAID_YEARMONTH, 1, 4) || '年' || substr(ADD_PAID_YEARMONTH, 5) || '月' AS LABEL ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND ADD_PAID_YEARMONTH IS NOT NULL ";
        $query .= "     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        return $query;
    }

    //調整金実行日付
    function getAdjustmentDate() {
        $query = " SELECT MAX(UPDATED) AS ADJUSTMENT_DATE FROM REDUCTION_ADJUSTMENT_DAT WHERE YEAR='".CTRL_YEAR."' ";
        return $query;
    }

    //CSV出力
    function getCsvQuery($model) {
        //変数
        $year = CTRL_YEAR;
        $semester = CTRL_SEMESTER;
        $isKuniOnly  = ($model->outdiv == "1") ? true : false;
        $isFukenOnly = ($model->outdiv == "2") ? true : false;
        $isRyohou    = ($model->outdiv == "3") ? true : false;
        $month3Flg = substr($model->paidYearMonth, 4) == "03" ? true : false;
        $setDate = str_replace("/", "-", $model->grdDate);

        $query  = "";
        $query .= " WITH T_SCHREGNO AS ( ";
        $query .= "SELECT ";
        $query .= "    t2.YEAR, ";
        $query .= "    t2.SEMESTER, ";
        $query .= "    t2.SCHREGNO, ";
        $query .= "    t2.GRADE, ";
        $query .= "    t2.HR_CLASS, ";
        $query .= "    t2.ATTENDNO ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT t2 ";
        $query .= "WHERE ";
        $query .= "        t2.YEAR = '{$year}' ";
        $query .= "    AND t2.SEMESTER = '{$semester}' ";
        $query .= "    AND t2.GRADE || t2.HR_CLASS IN ('".implode($model->class_select["selectdata"],"','")."') ";
        $query .= " ), GRANT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(VALUE(GRANT_MONEY, 0)) AS GRANT_MONEY ";
        $query .= " FROM ";
        $query .= "     SCHREG_GRANT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ), MONEY_DUE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(MONEY_DUE) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '{$year}' ";
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
        $query .= "    YEAR = '{$year}' ";
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
        $query .= "    T1.YEAR = '{$year}' ";
        $query .= "    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "         OR ";
        $query .= "         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";

        if ($isFukenOnly || $isRyohou) {
            $query .= " , REDUCTION_DAT_SUM AS ( ";
            $query .= " SELECT ";
            $query .= "     t1.SCHREGNO, ";
            $query .= "     CASE WHEN t1.REDUC_DEC_FLG_1 = '1' OR t1.REDUC_DEC_FLG_2 = '1' ";
            $query .= "          THEN '1' ";
            $query .= "          ELSE '0' ";
            $query .= "     END AS REDUC_DEC_FLG, ";
            $query .= "     CASE WHEN t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ";
            $query .= "          THEN '1' ";
            $query .= "          ELSE '0' ";
            $query .= "     END AS OFFSET_LOCK_FLG, ";
            if ($model->output == "2") {
                $query .= "     SUM(CASE WHEN t1.REDUC_DEC_FLG_1 = '1' AND t1.REDUCTIONMONEY_1 is not null ";
                $query .= "              THEN t1.REDUCTIONMONEY_1 ";
                $query .= "              ELSE 0 ";
                $query .= "         END ";
                $query .= "         + ";
                $query .= "         CASE WHEN t1.REDUC_DEC_FLG_2 = '1' AND t1.REDUCTIONMONEY_2 is not null ";
                $query .= "              THEN t1.REDUCTIONMONEY_2 ";
                $query .= "              ELSE 0 ";
                $query .= "         END) ";
            } else if ($model->output == "3") {
                $query .= "     SUM(CASE WHEN t1.REDUC_DEC_FLG_1 = '1' AND t1.REDUCTIONMONEY_1 is not null ";
                $query .= "               AND t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ";
                $query .= "              THEN t1.REDUCTIONMONEY_1 ";
                $query .= "              ELSE 0 ";
                $query .= "         END ";
                $query .= "         + ";
                $query .= "         CASE WHEN t1.REDUC_DEC_FLG_2 = '1' AND t1.REDUCTIONMONEY_2 is not null ";
                $query .= "               AND t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ";
                $query .= "              THEN t1.REDUCTIONMONEY_2 ";
                $query .= "              ELSE 0 ";
                $query .= "         END) ";
            } else {
                $query .= "     SUM(CASE WHEN t1.REDUCTIONMONEY_1 is not null OR t1.REDUCTIONMONEY_2 is not null ";
                $query .= "              THEN VALUE(t1.REDUCTIONMONEY_1,0) + VALUE(t1.REDUCTIONMONEY_2,0) ";
                $query .= "              ELSE NULL ";
                $query .= "         END) ";
            }
            $query .= "     AS REDUCTIONMONEY ";
            $query .= " FROM ";
            $query .= "     REDUCTION_DAT t1 ";
            $query .= " WHERE ";
            $query .= "    t1.YEAR = '{$year}' ";
            if ($model->output == "2") {
                $query .= "    AND (t1.REDUC_DEC_FLG_1 = '1' ";
                $query .= "         OR ";
                $query .= "         t1.REDUC_DEC_FLG_2 = '1') ";
            }
            if ($model->output == "3") {
                $query .= "    AND t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ";
                $query .= "    AND (t1.REDUC_DEC_FLG_1 = '1' ";
                $query .= "         OR ";
                $query .= "         t1.REDUC_DEC_FLG_2 = '1') ";
            }
            if ($model->output != "4") {
                $query .= "    AND NOT EXISTS (SELECT ";
                $query .= "                        'x' ";
                $query .= "                    FROM ";
                $query .= "                        REDUCTION_DAT E1 ";
                $query .= "                    WHERE ";
                $query .= "                    E1.YEAR = '{$year}' AND ";
                $query .= "                    E1.REDUC_RARE_CASE_CD = 'B' AND ";
                $query .= "                    t1.SCHREGNO = E1.SCHREGNO) ";
            }
            $query .= " GROUP BY ";
            $query .= "     t1.SCHREGNO, ";
            $query .= "     CASE WHEN t1.REDUC_DEC_FLG_1 = '1' OR t1.REDUC_DEC_FLG_2 = '1' ";
            $query .= "          THEN '1' ";
            $query .= "          ELSE '0' ";
            $query .= "     END, ";
            $query .= "     CASE WHEN t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ";
            $query .= "          THEN '1' ";
            $query .= "          ELSE '0' ";
            $query .= "     END ";
            $query .= " ) ";
        }

        if ($isKuniOnly || $isRyohou) {
            $query .= " , PAID_MONEY_SUM AS ( ";
            $query .= "     SELECT ";
            $query .= "         t1.SCHREGNO, ";
            $query .= "         SUM(CASE WHEN t1.PAID_YEARMONTH = '{$model->paidYearMonth}' AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "                  THEN t1.PAID_MONEY ";
            $query .= "                  ELSE 0 ";
            $query .= "             END ";
            $query .= "             + ";
            $query .= "             CASE WHEN t1.ADD_PAID_YEARMONTH = '{$model->paidYearMonth}' AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "                  THEN t1.ADD_PAID_MONEY ";
            $query .= "                  ELSE 0 ";
            $query .= "             END) AS PAID_MONEY ";
            $query .= "     FROM ";
            $query .= "         REDUCTION_COUNTRY_PLAN_DAT t1 ";
            $query .= "     WHERE ";
            $query .= "         t1.YEAR='{$year}' ";
            $query .= "         AND ((t1.PAID_YEARMONTH = '{$model->paidYearMonth}' AND VALUE(PLAN_CANCEL_FLG, '0') = '0' AND VALUE(PLAN_LOCK_FLG, '0') = '0') ";
            $query .= "           OR (t1.ADD_PAID_YEARMONTH = '{$model->paidYearMonth}' AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0')) ";
            $query .= "          AND NOT EXISTS (SELECT ";
            $query .= "                             'x' ";
            $query .= "                          FROM ";
            $query .= "                             REDUCTION_COUNTRY_DAT E1 ";
            $query .= "                          WHERE ";
            $query .= "                             E1.YEAR = '{$year}' ";
            $query .= "                             AND t1.SCHREGNO = E1.SCHREGNO ";
            $query .= "                             AND VALUE(E1.OFFSET_FLG, '0') = '0') ";
            $query .= "     GROUP BY ";
            $query .= "         t1.SCHREGNO ";
            $query .= " ) ";
        }

        $query .= " , MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "    t2.SCHREGNO, ";
        $query .= "    t6.HR_NAME, ";
        $query .= "    t2.ATTENDNO, ";
        $query .= "    t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ";
        $query .= "    t2.GRADE || t2.HR_CLASS AS CHANGEPAGE, ";
        $query .= "    t4.NAME, ";
        $query .= "    t8.PREF, ";
        if ($isRyohou) {
            $query .= "    t1.REDUC_DEC_FLG, ";
            if ($model->output == "3") {
                $query .= "    t1.OFFSET_LOCK_FLG, ";
                $query .= "    CASE WHEN t1.OFFSET_LOCK_FLG = '1' ";
                $query .= "         THEN value(tt1.PAID_MONEY, 0) + value(t1.REDUCTIONMONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) - value(AD.TOTAL_ADJUSTMENT_MONEY, 0) ";
                $query .= "         ELSE value(tt1.PAID_MONEY, 0) + value(t1.REDUCTIONMONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) ";
                $query .= "         END AS REDUCTIONMONEY, ";
            } else {
                $query .= "    '' AS OFFSET_LOCK_FLG, ";
                $query .= "    value(tt1.PAID_MONEY, 0) + value(t1.REDUCTIONMONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) AS REDUCTIONMONEY, ";
            }
        } else if ($isKuniOnly) {
            $query .= "    '' AS REDUC_DEC_FLG, ";
            $query .= "    '' AS OFFSET_LOCK_FLG, ";
            $query .= "    VALUE(tt1.PAID_MONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) AS REDUCTIONMONEY, ";
        } else {
            $query .= "    t1.REDUC_DEC_FLG, ";
            if ($model->output == "3") {
                $query .= "    t1.OFFSET_LOCK_FLG, ";
                $query .= "    CASE WHEN t1.OFFSET_LOCK_FLG = '1' ";
                $query .= "         THEN value(t1.REDUCTIONMONEY,0) + value(GRANT_T.GRANT_MONEY, 0) - value(AD.TOTAL_ADJUSTMENT_MONEY,0) ";
                $query .= "         ELSE value(t1.REDUCTIONMONEY,0) + value(GRANT_T.GRANT_MONEY, 0) ";
                $query .= "         END AS REDUCTIONMONEY, ";
            } else {
                $query .= "    '' AS OFFSET_LOCK_FLG, ";
                $query .= "    value(t1.REDUCTIONMONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) AS REDUCTIONMONEY, ";
            }
        }
        $query .= "    t2.GRADE, ";
        $query .= "    t2.HR_CLASS, ";
        $query .= "    t7.BANK_HR_CLASS, ";
        $query .= "    t7.BANK_MAJORCD, ";
        $query .= "    AD.TOTAL_ADJUSTMENT_MONEY, ";
        $query .= "    VALUE(OVER_MONEY.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY ";
        $query .= "FROM ";
        $query .= "    T_SCHREGNO t2 ";
        $query .= "    LEFT JOIN REDUCTION_ADJUSTMENT_DAT AD ON AD.YEAR = t2.YEAR AND AD.SCHREGNO = t2.SCHREGNO ";
        $query .= "    LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT OVER_MONEY ON OVER_MONEY.YEAR = '{$year}' ";
        $query .= "         AND OVER_MONEY.SCHREGNO = t2.SCHREGNO ";
        if ($isKuniOnly || $isRyohou) {
            $query .= "    LEFT JOIN PAID_MONEY_SUM tt1 ON tt1.SCHREGNO = t2.SCHREGNO ";
        }
        if ($isFukenOnly || $isRyohou) {
            $query .= "    LEFT JOIN REDUCTION_DAT_SUM t1 ON t1.SCHREGNO = t2.SCHREGNO ";
        }
        $query .= "    LEFT JOIN GRANT_T GRANT_T ON GRANT_T.SCHREGNO = t2.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = t2.SCHREGNO ";
        $query .= "    LEFT JOIN GUARDIAN_DAT t9 ON t9.SCHREGNO = t2.SCHREGNO ";
        $query .= "    LEFT JOIN ( ";
        $query .= "        SELECT ";
        $query .= "            ZIP.NEW_ZIPCD, ";
        $query .= "            MAX(ZIP.PREF) AS PREF ";
        $query .= "        FROM ";
        $query .= "            ZIPCD_MST ZIP ";
        $query .= "        GROUP BY ZIP.NEW_ZIPCD ";
        $query .= "    ) t8 ON t9.GUARANTOR_ZIPCD = t8.NEW_ZIPCD ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT t6 ON t6.GRADE || t6.HR_CLASS = t2.GRADE || t2.HR_CLASS ";
        $query .= "    AND t6.YEAR = t2.YEAR ";
        $query .= "    AND t6.SEMESTER = t2.SEMESTER ";
        $query .= "    LEFT JOIN BANK_CLASS_MST t7 ON t7.GRADE || t7.HR_CLASS = t2.GRADE || t2.HR_CLASS ";
        $query .= "    AND t7.YEAR = t2.YEAR ";
        $query .= "WHERE ";
        $query .= "     NOT EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1, ";
        $query .= "             (SELECT ";
        $query .= "                  BASE.SCHREGNO ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_BASE_MST BASE ";
        $query .= "              WHERE ";
        $query .= "                  BASE.GRD_DIV IN ('2', '3') ";
        $query .= "                  AND BASE.GRD_DATE <= '{$setDate}') BASE_GRD, ";
        $query .= "             (SELECT ";
        $query .= "                  TRANSFER.SCHREGNO ";
        $query .= "                  FROM ";
        $query .= "                      SCHREG_TRANSFER_DAT TRANSFER ";
        $query .= "                  WHERE ";
        $query .= "                      TRANSFER.TRANSFERCD IN ('2') ";
        $query .= "                      AND TRANSFER.TRANSFER_SDATE >= '".CTRL_YEAR."-04-01' ";
        $query .= "                      AND TRANSFER.TRANSFER_SDATE <= '{$setDate}' ";
        $query .= "                  ) SCH_TRANS ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND (E1.SCHREGNO = BASE_GRD.SCHREGNO OR E1.SCHREGNO = SCH_TRANS.SCHREGNO) ";
        $query .= "             AND t2.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) AND ( ";
        $query .= "     GRANT_T.SCHREGNO = t2.SCHREGNO ";
        if ($isKuniOnly || $isRyohou) {
            $query .= "  OR t2.SCHREGNO = tt1.SCHREGNO ";
        }
        if ($isFukenOnly || $isRyohou) {
            $query .= "  OR t2.SCHREGNO = t1.SCHREGNO ";
        }
        if ($month3Flg) {
            $query .= "  OR t2.SCHREGNO = OVER_MONEY.SCHREGNO ";
        }
        $query .= "    ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     REDUCTIONMONEY + OVER_MONEY > 0 ";
        $query .= " ORDER BY ";
        $query .= "     GRD_CLASS ";

        return $query;
    }

}
?>
