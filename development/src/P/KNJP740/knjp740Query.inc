<?php

require_once('for_php7.php');
class knjp740Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //共通手数料取得
    function getBankTransferFee99($model) {
        $query  = " SELECT ";
        $query .= "     BANK_TRANSFER_FEE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '99' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND FORMAT_DIV  = '{$model->field["OUTPUT"]}' ";   // 1:引落 2:返金
        $query .= "     AND SEQ         = '001' "; // 固定

        return $query;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM COLLECT_P740_STAFF_WORK_DAT ";
        $db->query($query);

        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data = array();
            $data["YUCHO_CD"][TEXT]         = $data_arr[$i]["YUCHO_CD"];
            $data["YUCHO_NAME"][TEXT]       = $data_arr[$i]["YUCHO_NAME"];
            $data["BRANCHCD"][TEXT]         = $data_arr[$i]["BRANCHCD"];
            $data["ACCOUNTNO"][TEXT]        = $data_arr[$i]["ACCOUNTNO"];
            $data["STAFFCD"][TEXT]          = $data_arr[$i]["STAFFCD"];
            $data["ACCOUNTNAME"][TEXT]      = $data_arr[$i]["ACCOUNTNAME"];
            $data["PLAN_MONEY"][TEXT]       = $data_arr[$i]["PLAN_MONEY"];
            $data["YEAR_MONTH"][TEXT]       = $data_arr[$i]["YEAR_MONTH"];
            $data["SYORI_CD"][TEXT]         = $data_arr[$i]["SYORI_CD"];
            $data["HOJO"][TEXT]             = $data_arr[$i]["HOJO"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "COLLECT_P740_STAFF_WORK_DAT");
            $db->query($query);

        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //データ削除
    function deleteQuery() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM COLLECT_P740_STAFF_WORK_DAT ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

/***** CSV出力で使用 *****/
    //CSV出力データ(引落)
    function getCsvQuery($model, $setLimitDay, $update="", $div = "") {
        $sep = "";
        $setMonthIn = "(";
        foreach ($model->monthArray as $month) {
            $setMonthIn .= $sep."'".$month."'";
            $sep = ",";
            if ($month == $model->field["MONTH"]) break;
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
        if ($model->field["TRANSFER_DATE"] != "") {
            $query .= "         '".substr(str_replace("/", "", $model->field["TRANSFER_DATE"]), 4)."' as LIMIT_DATE, ";
        } else {
            $query .= "         case ";
            $query .= "             when substr(cast(LMIT.PAID_LIMIT_DATE as varchar(10)), 6, 2) = '".$model->field["MONTH"]."' ";
            $query .= "             then substr(cast(LMIT.PAID_LIMIT_DATE as varchar(10)), 6, 5) ";
            $query .= "             else '{$setLimitDay}' ";
            $query .= "         end as LIMIT_DATE, ";
        }
        $query .= "         SLIP.PAY_DIV, ";
        $query .= "         PLAN.SLIP_NO, ";
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
        $query .= "         BANK.BRANCHCD, ";
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
        $query .= "         LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = PLAN.SCHREGNO ";
        $query .= "                                       AND REGD.YEAR     = PLAN.YEAR ";
        $query .= "                                       AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
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
        $query .= "         AND (LMIT.PAID_LIMIT_DATE IS NULL OR LMIT.PAID_LIMIT_MONTH IN {$setMonthIn}) ";
        $query .= "     GROUP BY ";
        $query .= "         PLAN.SCHOOLCD, ";
        $query .= "         PLAN.SCHOOL_KIND, ";
        $query .= "         PLAN.YEAR, ";
        $query .= "         PLAN.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         SLIP.PAY_DIV, ";
        $query .= "         PLAN.SLIP_NO, ";
        $query .= "         PLAN.PLAN_YEAR, ";
        $query .= "         PLAN.PLAN_MONTH, ";
        $query .= "         LMIT.PAID_LIMIT_DATE, ";
        $query .= "         BANK.BRANCHCD, ";
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
            $query .= "     LIMIT_DATE, ";
            $query .= "     PAY_DIV, ";
            if ($update != "") {
                $query .= "     SLIP_NO, ";
            }
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
            $query .= "     BRANCHCD, ";
            $query .= "     ACCOUNTNO, ";
            $query .= "     ACCOUNTNAME ";
            $query .= " FROM ";
            $query .= "     MAIN_DATA ";
            if ($model->Properties["useOutputOverMonth"] != "1") {
                $query .= " WHERE ";
                $query .= "     LIMIT_DATE = '{$model->field["MONTH_DATE"]}' ";
            } else {
                $query .= " WHERE ";
                $query .= "     LIMIT_DATE <= '{$model->field["MONTH_DATE"]}' ";
            }
            $query .= " GROUP BY ";
            $query .= "     SCHOOLCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     YEAR, ";
            $query .= "     SCHREGNO, ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
            $query .= "     LIMIT_DATE, ";
            $query .= "     PAY_DIV, ";
            if ($update != "") {
                $query .= "     SLIP_NO, ";
            }
            $query .= "     PLAN_YEAR, ";
            $query .= "     PLAN_MONTH, ";
            $query .= "     BRANCHCD, ";
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
    function getBankHolidayDat($model) {
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
    function getDirectDebitDate($model) {
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
    function getCsvQueryHenkin($model) {
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
        $query .= " ), KYUHU_DATA AS ( ";
        $query .= "   SELECT ";
        $query .= "     OUTGO_SCH.SCHREGNO, ";
        $query .= "     sum(VALUE (T1.REQUEST_GK, 0)) AS KYUFU_MONEY ";
        $query .= "   FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "       ON T1.YEAR = OUTGO_SCH.YEAR ";
        $query .= "       AND T1.SCHOOLCD = OUTGO_SCH.SCHOOLCD ";
        $query .= "       AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ";
        $query .= "       AND T1.OUTGO_L_CD = OUTGO_SCH.OUTGO_L_CD ";
        $query .= "       AND T1.OUTGO_M_CD = OUTGO_SCH.OUTGO_M_CD ";
        $query .= "       AND T1.REQUEST_NO = OUTGO_SCH.REQUEST_NO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        if ($model->field["SCHOOL_KIND"] != "99") {
            $query .= "     AND T1.SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "     AND VALUE (T1.OUTGO_CANCEL, '0') = '0' ";
        $query .= "     AND T1.OUTGO_L_CD <> '99' ";
        $query .= "     AND T1.INCOME_L_CD = '98' ";
        $query .= "     AND T1.HENKIN_FLG = '1' ";
        $query .= "     AND VALUE(T1.HENKIN_APPROVAL, '0') = '0' ";
        $query .= "   GROUP BY ";
        $query .= "     OUTGO_SCH.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     BANK.BRANCHCD, ";
        $query .= "     BANK.ACCOUNTNO, ";
        $query .= "     BANK.ACCOUNTNAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     SCHO.SCHREGNO, ";
        $query .= "     sum(OUTG.REQUEST_GK) AS REQUEST_GK, ";
        $query .= "     SCHOOL_BANK.BANK_TRANSFER_FEE, ";
        $query .= "     VALUE(KYUHU.KYUFU_MONEY, 0) AS KYUFU_MONEY";
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
        $query .= "     LEFT JOIN COLLECT_SCHOOL_BANK_MST SCHOOL_BANK ON SCHO.SCHOOLCD    = SCHOOL_BANK.SCHOOLCD ";
        $query .= "          AND SCHO.SCHOOL_KIND = SCHOOL_BANK.SCHOOL_KIND ";
        $query .= "          AND SCHO.YEAR        = SCHOOL_BANK.YEAR ";
        $query .= "          AND SCHOOL_BANK.FORMAT_DIV  = '{$model->field["OUTPUT"]}' ";
        $query .= "          AND SCHOOL_BANK.SEQ  = '001' ";
        $query .= "     LEFT JOIN KYUHU_DATA KYUHU ON KYUHU.SCHREGNO = SCHO.SCHREGNO ";
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
        $query .= "     SCHO.SCHREGNO, ";
        $query .= "     SCHOOL_BANK.BANK_TRANSFER_FEE, ";
        $query .= "     KYUHU.KYUFU_MONEY ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //学校銀行情報取得
    function getCollectSchoolBankMst($model) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND YEAR        = '".$model->year."' ";
        $query .= "     AND FORMAT_DIV  = '".$model->field["OUTPUT"]."'";
        $query .= "     AND SEQ         = '001' ";

        return $query;
    }
}
?>
