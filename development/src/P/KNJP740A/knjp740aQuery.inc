<?php

require_once('for_php7.php');

class knjp740aQuery extends Query
{

    //校種取得
    public function getSchkind($model)
    {
        $query  = " WITH A023(NAMECD2, NAME1, ABBV1) AS ( ";
        $query .= " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " UNION ";
        $query .= "     VALUES('0', '99', '-- 共通 --') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     A023.NAME1 AS VALUE, ";
        $query .= "     A023.ABBV1 AS LABEL, ";
        $query .= "     A023.NAMECD2 ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST T1 ";
        $query .= "     INNER JOIN A023 ON T1.SCHOOL_KIND = A023.NAME1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND T1.YEAR        = '".$model->year."' ";
        $query .= "     AND T1.FORMAT_DIV  = '".$model->field["OUTPUT"]."'";
        $query .= "     AND T1.SEQ         = '{$model->seq}' ";
        $query .= " GROUP BY ";
        $query .= "     A023.NAME1, ";
        $query .= "     A023.ABBV1, ";
        $query .= "     A023.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     A023.NAMECD2 ";

        return $query;
    }

    //銀行取得
    public function getTargetBank($model)
    {
        $query  = " SELECT ";
        $query .= "     SBANK.BANK_CD AS VALUE, ";
        $query .= "     SBANK.BANKNAME_KANA AS LABEL, ";
        $query .= "     CASE WHEN SBANK.TARGET_BANK_DIV = '1' ";
        $query .= "          THEN 'ALL' ";
        $query .= "          ELSE SBANK.TARGET_BANK_CD ";
        $query .= "     END AS TARGET_BANK_CD ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST SBANK ";
        $query .= "     LEFT JOIN (SELECT BANKCD, MAX(BANKNAME) AS TARGET_BANK FROM BANK_MST GROUP BY BANKCD) L_BANK ";
        $query .= "           ON SBANK.TARGET_BANK_CD = L_BANK.BANKCD ";
        $query .= " WHERE ";
        $query .= "         SBANK.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SBANK.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."'";
        $query .= "     AND SBANK.YEAR        = '".$model->year."' ";
        $query .= "     AND SBANK.FORMAT_DIV  = '".$model->field["OUTPUT"]."'";
        $query .= "     AND SBANK.SEQ         = '{$model->seq}' ";
        $query .= " GROUP BY ";
        $query .= "     SBANK.BANK_CD, ";
        $query .= "     SBANK.BANKNAME_KANA, ";
        $query .= "     CASE WHEN SBANK.TARGET_BANK_DIV = '1' ";
        $query .= "          THEN 'ALL' ";
        $query .= "          ELSE SBANK.TARGET_BANK_CD ";
        $query .= "     END ";
        $query .= " ORDER BY ";
        $query .= "     SBANK.BANK_CD ";

        return $query;
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    //学校銀行情報取得
    public function getCollectSchoolBankMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND YEAR        = '".$model->year."' ";
        $query .= "     AND FORMAT_DIV  = '".$model->field["OUTPUT"]."'";
        $query .= "     AND SEQ         = '{$model->seq}' ";
        $query .= "     AND BANK_CD     = '{$model->field["BANK_CD"]}' ";

        return $query;
    }

    //CSV出力データ(引落)
    public function getCsvQuery($model, $setLimitDay, $div = "")
    {
        $sep = "";
        $setMonthIn = "(";
        foreach ($model->monthArray as $month) {
            $setMonthIn .= $sep."'".$month."'";
            $sep = ",";
            if ($month == $model->field["MONTH"]) {
                break;
            }
        }
        $setMonthIn .= ")";

        $query  = " WITH MAIN_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         PLAN.SCHOOLCD, ";
        $query .= "         PLAN.SCHOOL_KIND, ";
        $query .= "         PLAN.YEAR, ";
        $query .= "         PLAN.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         CUST.CUSTOMER_NUMBER, ";
        $query .= "         case ";
        $query .= "             when substr(cast(LMIT.PAID_LIMIT_DATE as varchar(10)), 6, 2) = '".$model->field["MONTH"]."' ";
        $query .= "             then substr(cast(LMIT.PAID_LIMIT_DATE as varchar(10)), 6, 5) ";
        $query .= "             else '{$setLimitDay}' ";
        $query .= "         end as LIMIT_DATE, ";
        $query .= "         SLIP.PAY_DIV, ";
        $query .= "         PLAN.PLAN_YEAR, ";
        $query .= "         PLAN.PLAN_MONTH, ";
        $query .= "         sum(PLAN.PLAN_MONEY) as PLAN_MONEY, ";
        $query .= "         max(VALUE(CASE WHEN REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0)) AS COUNTRY_MONEY, ";
        $query .= "         max(VALUE(CASE WHEN REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0)) AS COUNTRY_ADDMONEY, ";
        $query .= "         max(VALUE(CASE WHEN REDUC_D1.OFFSET_FLG = '1' THEN REDUC_D1.DECISION_MONEY ELSE 0 END, 0)) AS PREF_MONEY1, ";
        $query .= "         max(VALUE(CASE WHEN REDUC_D2.OFFSET_FLG = '1' THEN REDUC_D2.DECISION_MONEY ELSE 0 END, 0)) AS PREF_MONEY2, ";
        $query .= "         max(VALUE(BURDEN_1.BURDEN_CHARGE, 0)) AS BURDEN_CHARGE1, ";
        $query .= "         max(VALUE(BURDEN_2.BURDEN_CHARGE, 0)) AS BURDEN_CHARGE2, ";
        $query .= "         max(VALUE(SCHOOL_1.DECISION_MONEY, 0)) AS SCHOOL_1, ";
        $query .= "         max(VALUE(SCHOOL_2.DECISION_MONEY, 0)) AS SCHOOL_2, ";
        $query .= "         BANK.BANKCD, ";
        $query .= "         BMST.BANKNAME_KANA, ";
        $query .= "         BANK.BRANCHCD, ";
        $query .= "         BMST.BRANCHNAME_KANA, ";
        $query .= "         BANK.DEPOSIT_ITEM, ";
        $query .= "         BANK.ACCOUNTNO, ";
        $query .= "         BANK.ACCOUNTNAME ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT PLAN ";
        $query .= "         LEFT JOIN COLLECT_SLIP_DAT SLIP ON PLAN.SCHOOLCD    = SLIP.SCHOOLCD ";
        $query .= "                                        AND PLAN.SCHOOL_KIND = SLIP.SCHOOL_KIND ";
        $query .= "                                        AND PLAN.YEAR        = SLIP.YEAR ";
        $query .= "                                        AND PLAN.SLIP_NO     = SLIP.SLIP_NO ";
        $query .= "         LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LMIT ON PLAN.SCHOOLCD    = LMIT.SCHOOLCD ";
        $query .= "                                                       AND PLAN.SCHOOL_KIND = LMIT.SCHOOL_KIND ";
        $query .= "                                                       AND PLAN.YEAR        = LMIT.YEAR ";
        $query .= "                                                       AND PLAN.SCHREGNO    = LMIT.SCHREGNO ";
        $query .= "                                                       AND PLAN.SLIP_NO     = LMIT.SLIP_NO ";
        $query .= "                                                       AND PLAN.PLAN_YEAR   = LMIT.PLAN_YEAR ";
        $query .= "                                                       AND PLAN.PLAN_MONTH  = LMIT.PLAN_MONTH ";
        $query .= "         LEFT JOIN REGISTBANK_DAT BANK ON PLAN.SCHOOLCD    = BANK.SCHOOLCD ";
        $query .= "                                      AND PLAN.SCHREGNO    = BANK.SCHREGNO ";
        $query .= "                                      AND SLIP.PAY_DIV     = BANK.SEQ ";
        $query .= "         LEFT JOIN BANK_MST BMST ON BMST.BANKCD   = BANK.BANKCD ";
        $query .= "                                AND BMST.BRANCHCD = BANK.BRANCHCD ";
        $query .= "         LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = PLAN.SCHREGNO ";
        $query .= "                                       AND REGD.YEAR     = PLAN.YEAR ";
        $query .= "                                       AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         LEFT JOIN COLLECT_SCHREG_CUSTOMER_DAT CUST ON CUST.SCHREGNO = PLAN.SCHREGNO ";
        $query .= "         LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ON LMIT.SCHOOLCD         = REDUC_C.SCHOOLCD ";
        $query .= "                                                     AND LMIT.SCHOOL_KIND      = REDUC_C.SCHOOL_KIND ";
        $query .= "                                                     AND LMIT.YEAR             = REDUC_C.YEAR ";
        $query .= "                                                     AND LMIT.SLIP_NO          = REDUC_C.SLIP_NO ";
        $query .= "                                                     AND PLAN.PLAN_MONTH       = REDUC_C.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_PLAN_DAT REDUC_D1 ON LMIT.SCHOOLCD         = REDUC_D1.SCHOOLCD ";
        $query .= "                                             AND LMIT.SCHOOL_KIND       = REDUC_D1.SCHOOL_KIND ";
        $query .= "                                             AND LMIT.YEAR              = REDUC_D1.YEAR ";
        $query .= "                                             AND REDUC_D1.REDUCTION_TARGET = '1' ";
        $query .= "                                             AND LMIT.SLIP_NO           = REDUC_D1.SLIP_NO ";
        $query .= "                                             AND PLAN.PLAN_MONTH        = REDUC_D1.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_PLAN_DAT REDUC_D2 ON LMIT.SCHOOLCD         = REDUC_D2.SCHOOLCD ";
        $query .= "                                             AND LMIT.SCHOOL_KIND       = REDUC_D2.SCHOOL_KIND ";
        $query .= "                                             AND LMIT.YEAR              = REDUC_D2.YEAR ";
        $query .= "                                             AND REDUC_D2.REDUCTION_TARGET = '2' ";
        $query .= "                                             AND LMIT.SLIP_NO           = REDUC_D2.SLIP_NO ";
        $query .= "                                             AND PLAN.PLAN_MONTH        = REDUC_D2.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_1 ON LMIT.SCHOOLCD        = BURDEN_1.SCHOOLCD ";
        $query .= "                                                          AND LMIT.SCHOOL_KIND       = BURDEN_1.SCHOOL_KIND ";
        $query .= "                                                          AND LMIT.YEAR              = BURDEN_1.YEAR ";
        $query .= "                                                          AND BURDEN_1.REDUCTION_TARGET = '1' ";
        $query .= "                                                          AND LMIT.SLIP_NO           = BURDEN_1.SLIP_NO ";
        $query .= "                                                          AND PLAN.PLAN_MONTH        = BURDEN_1.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_2 ON LMIT.SCHOOLCD        = BURDEN_2.SCHOOLCD ";
        $query .= "                                                          AND LMIT.SCHOOL_KIND       = BURDEN_2.SCHOOL_KIND ";
        $query .= "                                                          AND LMIT.YEAR              = BURDEN_2.YEAR ";
        $query .= "                                                          AND BURDEN_2.REDUCTION_TARGET = '2' ";
        $query .= "                                                          AND LMIT.SLIP_NO           = BURDEN_2.SLIP_NO ";
        $query .= "                                                          AND PLAN.PLAN_MONTH        = BURDEN_2.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_1 ON LMIT.SCHOOLCD          = SCHOOL_1.SCHOOLCD ";
        $query .= "                                                     AND LMIT.SCHOOL_KIND       = SCHOOL_1.SCHOOL_KIND ";
        $query .= "                                                     AND LMIT.YEAR              = SCHOOL_1.YEAR ";
        $query .= "                                                     AND SCHOOL_1.REDUCTION_TARGET = '1' ";
        $query .= "                                                     AND LMIT.SLIP_NO           = SCHOOL_1.SLIP_NO ";
        $query .= "                                                     AND PLAN.PLAN_MONTH        = SCHOOL_1.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_2 ON LMIT.SCHOOLCD          = SCHOOL_2.SCHOOLCD ";
        $query .= "                                                     AND LMIT.SCHOOL_KIND       = SCHOOL_2.SCHOOL_KIND ";
        $query .= "                                                     AND LMIT.YEAR              = SCHOOL_2.YEAR ";
        $query .= "                                                     AND SCHOOL_2.REDUCTION_TARGET = '2' ";
        $query .= "                                                     AND LMIT.SLIP_NO           = SCHOOL_2.SLIP_NO ";
        $query .= "                                                     AND PLAN.PLAN_MONTH        = SCHOOL_2.PLAN_MONTH ";
        $query .= "     WHERE ";
        $query .= "             PLAN.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        if ($model->field["SCHOOL_KIND"] != "99") {
            $query .= "         AND PLAN.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "         AND PLAN.YEAR        = '".$model->year."' ";
        $query .= "         AND PLAN.PLAN_MONTH  IN {$setMonthIn} ";
        $query .= "         AND PLAN.PLAN_MONEY  <> 0 ";
        $query .= "         AND PLAN.PAID_MONEY  IS NULL ";
        $query .= "         AND SLIP.CANCEL_DATE IS NULL ";
        $query .= "         AND SLIP.PAY_DIV     IN ('1', '2') ";
        if ($div == 'group') {
            $query .= "         AND LMIT.PAID_LIMIT_MONTH = '{$model->field["MONTH"]}' ";
        } else {
            $query .= "         AND (LMIT.PAID_LIMIT_DATE IS NOT NULL AND substr(cast(LMIT.PAID_LIMIT_DATE as varchar(10)), 6, 5) = '{$model->field["MONTH_DATE"]}') ";
        }
        if ($model->targetBankCd[$model->field["BANK_CD"]] !== "ALL") {
            //桁埋めして比較
            $query .= "         AND right('0000' || rtrim(BANK.BANKCD),4) = right('0000' || rtrim('{$model->targetBankCd[$model->field["BANK_CD"]]}'),4) ";
        }
        $query .= "     GROUP BY ";
        $query .= "         PLAN.SCHOOLCD, ";
        $query .= "         PLAN.SCHOOL_KIND, ";
        $query .= "         PLAN.YEAR, ";
        $query .= "         PLAN.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         CUST.CUSTOMER_NUMBER, ";
        $query .= "         SLIP.PAY_DIV, ";
        $query .= "         PLAN.PLAN_YEAR, ";
        $query .= "         PLAN.PLAN_MONTH, ";
        $query .= "         LMIT.PAID_LIMIT_DATE, ";
        $query .= "         BANK.BANKCD, ";
        $query .= "         BMST.BANKNAME_KANA, ";
        $query .= "         BANK.BRANCHCD, ";
        $query .= "         BMST.BRANCHNAME_KANA, ";
        $query .= "         BANK.DEPOSIT_ITEM, ";
        $query .= "         BANK.ACCOUNTNO, ";
        $query .= "         BANK.ACCOUNTNAME ";
        $query .= " ) ";
        $query .= " SELECT ";
        if ($div == 'group') {
            $query .= "     LIMIT_DATE AS VALUE, ";
            $query .= "     LIMIT_DATE AS LABEL ";
            $query .= " FROM ";
            $query .= "     MAIN_DATA ";
            $query .= " GROUP BY ";
            $query .= "     LIMIT_DATE ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= "     SCHOOLCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     YEAR, ";
            $query .= "     SCHREGNO, ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
            $query .= "     CUSTOMER_NUMBER, ";
            $query .= "     LIMIT_DATE, ";
            $query .= "     PAY_DIV, ";
            $query .= "     PLAN_YEAR, ";
            $query .= "     PLAN_MONTH, ";
            $query .= "     sum(PLAN_MONEY) as PLAN_MONEY, ";
            $query .= "     sum(COUNTRY_MONEY) as COUNTRY_MONEY, ";
            $query .= "     sum(COUNTRY_ADDMONEY) as COUNTRY_ADDMONEY, ";
            $query .= "     sum(PREF_MONEY1) as PREF_MONEY1, ";
            $query .= "     sum(PREF_MONEY2) as PREF_MONEY2, ";
            $query .= "     sum(BURDEN_CHARGE1) as BURDEN_CHARGE1, ";
            $query .= "     sum(BURDEN_CHARGE2) as BURDEN_CHARGE2, ";
            $query .= "     sum(SCHOOL_1) as SCHOOL_1, ";
            $query .= "     sum(SCHOOL_2) as SCHOOL_2, ";
            $query .= "     BANKCD, ";
            $query .= "     BANKNAME_KANA, ";
            $query .= "     BRANCHCD, ";
            $query .= "     BRANCHNAME_KANA, ";
            $query .= "     DEPOSIT_ITEM, ";
            $query .= "     ACCOUNTNO, ";
            $query .= "     ACCOUNTNAME ";
            $query .= " FROM ";
            $query .= "     MAIN_DATA ";
            $query .= " WHERE ";
            $query .= "     LIMIT_DATE = '{$model->field["MONTH_DATE"]}' ";
            $query .= " GROUP BY ";
            $query .= "     SCHOOLCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     YEAR, ";
            $query .= "     SCHREGNO, ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
            $query .= "     CUSTOMER_NUMBER, ";
            $query .= "     LIMIT_DATE, ";
            $query .= "     PAY_DIV, ";
            $query .= "     PLAN_YEAR, ";
            $query .= "     PLAN_MONTH, ";
            $query .= "     BANKCD, ";
            $query .= "     BANKNAME_KANA, ";
            $query .= "     BRANCHCD, ";
            $query .= "     BRANCHNAME_KANA, ";
            $query .= "     DEPOSIT_ITEM, ";
            $query .= "     ACCOUNTNO, ";
            $query .= "     ACCOUNTNAME ";
            $query .= " ORDER BY ";
            $query .= "     LIMIT_DATE, ";
            $query .= "     PLAN_YEAR, ";
            $query .= "     PLAN_MONTH, ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO ";
        }

        return $query;
    }

    //銀行休業日取得
    public function getBankHolidayDat($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BANK_HOLIDAY_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        if ($model->field["SCHOOL_KIND"] != "99") {
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }

        return $query;
    }

    //引き落とし日取得
    public function getDirectDebitDate($model)
    {
        $query  = " SELECT ";
        $query .= "     max(DIRECT_DEBIT_DATE) ";
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        if ($model->field["SCHOOL_KIND"] != "99") {
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "     AND YEAR        = '".$model->year."' ";
        $query .= " GROUP BY ";
        $query .= "     DIRECT_DEBIT_DATE ";

        return $query;
    }

    //CSV出力データ(返金)
    public function getCsvQueryHenkin($model)
    {
        $query  = " WITH MAX_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         max(REGD.SEMESTER) as SEMESTER, ";
        $query .= "         REGD.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".$model->year."' ";
        $query .= "     GROUP BY ";
        $query .= "         REGD.SCHREGNO ";
        $query .= " ), REGD_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         REGD.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD, ";
        $query .= "         MAX_SEM ";
        $query .= "     WHERE ";
        $query .= "             REGD.YEAR     = '".$model->year."' ";
        $query .= "         AND REGD.SEMESTER = MAX_SEM.SEMESTER ";
        $query .= "         AND REGD.SCHREGNO = MAX_SEM.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     BANK.BRANCHCD, ";
        $query .= "     BANK.ACCOUNTNO, ";
        $query .= "     BANK.ACCOUNTNAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     SCHO.SCHREGNO, ";
        $query .= "     sum(OUTG.REQUEST_GK) AS REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT SCHO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON SCHO.SCHOOLCD    = OUTG.SCHOOLCD ";
        $query .= "                                           AND SCHO.SCHOOL_KIND = OUTG.SCHOOL_KIND ";
        $query .= "                                           AND SCHO.YEAR        = OUTG.YEAR ";
        $query .= "                                           AND SCHO.REQUEST_NO  = OUTG.REQUEST_NO ";
        $query .= "                                           AND SCHO.OUTGO_L_CD  = OUTG.OUTGO_L_CD ";
        $query .= "                                           AND SCHO.OUTGO_M_CD  = OUTG.OUTGO_M_CD ";
        $query .= "     LEFT JOIN REGISTBANK_DAT BANK ON BANK.SCHOOLCD    = SCHO.SCHOOLCD ";
        $query .= "                                  AND BANK.SCHREGNO    = SCHO.SCHREGNO ";
        $query .= "                                  AND BANK.SEQ         = OUTG.PAY_DIV ";
        $query .= "     LEFT JOIN REGD_INFO REGD ON SCHO.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         SCHO.SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' ";
        if ($model->field["SCHOOL_KIND"] != "99") {
            $query .= "     AND SCHO.SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "     AND SCHO.YEAR            = '".$model->year."' ";
        $query .= "     AND SCHO.LINE_NO         = 1 ";
        $query .= "     AND SCHO.OUTGO_L_CD      = '99' ";
        $query .= "     AND SCHO.OUTGO_M_CD      = '99' ";
        $query .= "     AND SCHO.OUTGO_S_CD      = '999' ";
        $query .= "     AND OUTG.HENKIN_FLG      = '1' ";
        $query .= "     AND value(OUTG.HENKIN_APPROVAL, '0') = '0' ";
        $query .= "     AND OUTG.OUTGO_CANCEL    IS NULL ";
        $query .= "     AND OUTG.HENKIN_DATE     IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     BANK.BRANCHCD, ";
        $query .= "     BANK.ACCOUNTNO, ";
        $query .= "     BANK.ACCOUNTNAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     SCHO.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    /**************/
    /** 更新処理 **/
    /**************/
    //マスタの追加（ＣＳＶデータより読込）
    public function updeteQueryCsvDat($db, $model, $row, $planMoney)
    {
        $data = array();

        $db->autoCommit(false);

        $schoolCd   = $row["SCHOOLCD"];
        $schoolKind = $row["SCHOOL_KIND"];
        $year       = $row["YEAR"];
        $schregNo   = $row["SCHREGNO"];
        $planYear   = $row["PLAN_YEAR"];
        $planMonth  = $row["PLAN_MONTH"];
        $payDiv     = $row["PAY_DIV"];

        //カウント取得
        $query = knjp740aQuery::getCountCsvDat($schoolCd, $schoolKind, $year, $schregNo, $planYear, $planMonth, $payDiv);
        $dataCnt = $db->getOne($query);

        $data["PLAN_MONEY"][NUMBER]     = $planMoney;

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($dataCnt < 1) {
            $data["SCHOOLCD"][TEXT]     = $schoolCd;
            $data["SCHOOL_KIND"][TEXT]  = $schoolKind;
            $data["YEAR"][TEXT]         = $year;
            $data["SCHREGNO"][TEXT]     = $schregNo;
            $data["PLAN_YEAR"][TEXT]    = $planYear;
            $data["PLAN_MONTH"][TEXT]   = $planMonth;
            $data["PAY_DIV"][TEXT]      = $payDiv;

            $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_CSV_DAT");
        } else {
            $where  = " WHERE   SCHOOLCD    = '{$schoolCd}' ";
            $where .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
            $where .= "     AND YEAR        = '{$year}' ";
            $where .= "     AND SCHREGNO    = '{$schregNo}' ";
            $where .= "     AND PLAN_YEAR   = '{$planYear}' ";
            $where .= "     AND PLAN_MONTH  = '{$planMonth}' ";
            $where .= "     AND PAY_DIV     = '{$payDiv}' ";

            $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_CSV_DAT", $where);
        }
        $db->query($query);

        $db->commit();

        return;
    }

    //COLLECT_SLIP_PLAN_CSV_DAT
    public function getCountCsvDat($schCd, $schKind, $year, $schregno, $pYear, $pMonth, $payDiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_CSV_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '{$schCd}' ";
        $query .= "     AND SCHOOL_KIND = '{$schKind}' ";
        $query .= "     AND YEAR        = '{$year}' ";
        $query .= "     AND SCHREGNO    = '{$schregno}' ";
        $query .= "     AND PLAN_YEAR   = '{$pYear}' ";
        $query .= "     AND PLAN_MONTH  = '{$pMonth}' ";
        $query .= "     AND PAY_DIV     = '{$payDiv}' ";

        return $query;
    }
}
