<?php

require_once('for_php7.php');

class knjp742query extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/
    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJP742' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    /******************/
    /**  校種取得    **/
    /******************/
    function getSchkind($model) {
        $query  = " WITH A023(NAMECD2, NAME1, ABBV1) AS ( ";
        $query .= " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
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
        $query .= "     AND T1.YEAR        = '".CTRL_YEAR."' ";
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

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //SCHREG_BASE_MST
    function checkBaseMstSql($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

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
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND FORMAT_DIV  = '".$model->field["INPUT"]."'";
        $query .= "     AND SEQ         = '{$model->seq}' ";

        return $query;
    }

    //計画金額取得
    function checkPlanMdat($model, $data) {
        $schoolKind  = $model->schKindArr[CTRL_YEAR.$data["GRADE"]];
        $planYear    = "20".mb_substr($data["PLAN_YEAR_MONTH"], 0, 2);
        $planMonth   = mb_substr($data["PLAN_YEAR_MONTH"], 2, 2);
        $setLimitM   = mb_substr($data["PAID_DATE"], 0, 2);

        $setMonthIn = "(";
        foreach ($model->monthArray as $month) {
            $setMonthIn .= $sep."'".$month."'";
            $sep = ",";
            if ($month == $setLimitM) break;
        }
        $setMonthIn .= ")";

        $query  = " SELECT ";
        $query .= "     sum(PLAN.PLAN_MONEY) ";
        $query .= "     - max(VALUE(CASE WHEN REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0)) ";
        $query .= "     - max(VALUE(CASE WHEN REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0)) ";
        $query .= "     - max(VALUE(CASE WHEN REDUC_D1.OFFSET_FLG = '1' THEN REDUC_D1.DECISION_MONEY ELSE 0 END, 0)) ";
        $query .= "     - max(VALUE(CASE WHEN REDUC_D2.OFFSET_FLG = '1' THEN REDUC_D2.DECISION_MONEY ELSE 0 END, 0)) ";
        $query .= "     - max(VALUE(BURDEN_1.BURDEN_CHARGE, 0)) ";
        $query .= "     - max(VALUE(BURDEN_2.BURDEN_CHARGE, 0)) ";
        $query .= "     - max(VALUE(SCHOOL_1.PLAN_MONEY, 0)) ";
        $query .= "     - max(VALUE(SCHOOL_2.PLAN_MONEY, 0)) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT PLAN ";
        $query .= "     LEFT JOIN COLLECT_SLIP_DAT SLIP ON PLAN.SCHOOLCD    = SLIP.SCHOOLCD ";
        $query .= "                                    AND PLAN.SCHOOL_KIND = SLIP.SCHOOL_KIND ";
        $query .= "                                    AND PLAN.YEAR        = SLIP.YEAR ";
        $query .= "                                    AND PLAN.SLIP_NO     = SLIP.SLIP_NO ";
        $query .= "     LEFT JOIN REGISTBANK_DAT BANK ON PLAN.SCHOOLCD    = BANK.SCHOOLCD ";
        $query .= "                                  AND PLAN.SCHREGNO    = BANK.SCHREGNO ";
        $query .= "                                  AND SLIP.PAY_DIV     = BANK.SEQ ";
        $query .= "     LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LMIT ON PLAN.SCHOOLCD    = LMIT.SCHOOLCD ";
        $query .= "                                                   AND PLAN.SCHOOL_KIND = LMIT.SCHOOL_KIND ";
        $query .= "                                                   AND PLAN.YEAR        = LMIT.YEAR ";
        $query .= "                                                   AND PLAN.SCHREGNO    = LMIT.SCHREGNO ";
        $query .= "                                                   AND PLAN.SLIP_NO     = LMIT.SLIP_NO ";
        $query .= "                                                   AND PLAN.PLAN_YEAR   = LMIT.PLAN_YEAR ";
        $query .= "                                                   AND PLAN.PLAN_MONTH  = LMIT.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ON LMIT.SCHOOLCD         = REDUC_C.SCHOOLCD ";
        $query .= "                                                 AND LMIT.SCHOOL_KIND      = REDUC_C.SCHOOL_KIND ";
        $query .= "                                                 AND LMIT.YEAR             = REDUC_C.YEAR ";
        $query .= "                                                 AND LMIT.SLIP_NO          = REDUC_C.SLIP_NO ";
        $query .= "                                                 AND PLAN.PLAN_MONTH       = REDUC_C.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D1 ON LMIT.SCHOOLCD         = REDUC_D1.SCHOOLCD ";
        $query .= "                                         AND LMIT.SCHOOL_KIND       = REDUC_D1.SCHOOL_KIND ";
        $query .= "                                         AND LMIT.YEAR              = REDUC_D1.YEAR ";
        $query .= "                                         AND REDUC_D1.REDUCTION_TARGET = '1' ";
        $query .= "                                         AND LMIT.SLIP_NO           = REDUC_D1.SLIP_NO ";
        $query .= "                                         AND PLAN.PLAN_MONTH        = REDUC_D1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D2 ON LMIT.SCHOOLCD         = REDUC_D2.SCHOOLCD ";
        $query .= "                                         AND LMIT.SCHOOL_KIND       = REDUC_D2.SCHOOL_KIND ";
        $query .= "                                         AND LMIT.YEAR              = REDUC_D2.YEAR ";
        $query .= "                                         AND REDUC_D2.REDUCTION_TARGET = '2' ";
        $query .= "                                         AND LMIT.SLIP_NO           = REDUC_D2.SLIP_NO ";
        $query .= "                                         AND PLAN.PLAN_MONTH        = REDUC_D2.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_1 ON LMIT.SCHOOLCD        = BURDEN_1.SCHOOLCD ";
        $query .= "                                                      AND LMIT.SCHOOL_KIND       = BURDEN_1.SCHOOL_KIND ";
        $query .= "                                                      AND LMIT.YEAR              = BURDEN_1.YEAR ";
        $query .= "                                                      AND BURDEN_1.REDUCTION_TARGET = '1' ";
        $query .= "                                                      AND LMIT.SLIP_NO           = BURDEN_1.SLIP_NO ";
        $query .= "                                                      AND PLAN.PLAN_MONTH        = BURDEN_1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_2 ON LMIT.SCHOOLCD        = BURDEN_2.SCHOOLCD ";
        $query .= "                                                      AND LMIT.SCHOOL_KIND       = BURDEN_2.SCHOOL_KIND ";
        $query .= "                                                      AND LMIT.YEAR              = BURDEN_2.YEAR ";
        $query .= "                                                      AND BURDEN_2.REDUCTION_TARGET = '2' ";
        $query .= "                                                      AND LMIT.SLIP_NO           = BURDEN_2.SLIP_NO ";
        $query .= "                                                      AND PLAN.PLAN_MONTH        = BURDEN_2.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_1 ON LMIT.SCHOOLCD          = SCHOOL_1.SCHOOLCD ";
        $query .= "                                                 AND LMIT.SCHOOL_KIND       = SCHOOL_1.SCHOOL_KIND ";
        $query .= "                                                 AND LMIT.YEAR              = SCHOOL_1.YEAR ";
        $query .= "                                                 AND SCHOOL_1.REDUCTION_TARGET = '1' ";
        $query .= "                                                 AND LMIT.SLIP_NO           = SCHOOL_1.SLIP_NO ";
        $query .= "                                                 AND PLAN.PLAN_MONTH        = SCHOOL_1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_2 ON LMIT.SCHOOLCD          = SCHOOL_2.SCHOOLCD ";
        $query .= "                                                 AND LMIT.SCHOOL_KIND       = SCHOOL_2.SCHOOL_KIND ";
        $query .= "                                                 AND LMIT.YEAR              = SCHOOL_2.YEAR ";
        $query .= "                                                 AND SCHOOL_2.REDUCTION_TARGET = '2' ";
        $query .= "                                                 AND LMIT.SLIP_NO           = SCHOOL_2.SLIP_NO ";
        $query .= "                                                 AND PLAN.PLAN_MONTH        = SCHOOL_2.PLAN_MONTH ";
        $query .= " WHERE ";
        $query .= "         PLAN.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND PLAN.SCHOOL_KIND     = '".$schoolKind."' ";
        $query .= "     AND PLAN.YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND PLAN.SCHREGNO        = '".$data["SCHREGNO"]."' ";
        $query .= "     AND PLAN.PLAN_YEAR       = '".$planYear."' ";
        $query .= "     AND PLAN.PLAN_MONTH      = '".$planMonth."' ";
        $query .= "     AND PLAN.PLAN_MONEY      <> 0 ";
        $query .= "     AND PLAN.PAID_MONEY      IS NULL ";
        $query .= "     AND SLIP.CANCEL_DATE     IS NULL ";
        $query .= "     AND SLIP.PAY_DIV         IN ('1', '2') ";
        $query .= "     AND (LMIT.PAID_LIMIT_DATE IS NULL OR LMIT.PAID_LIMIT_MONTH IN {$setMonthIn}) ";
        $query .= "     AND BANK.ACCOUNTNO       = '".sprintf("%07d", $data["ACCOUNTNO"])."' ";
        $query .= "     AND BANK.ACCOUNTNAME  like '".rtrim($data["ACCOUNTNAME"])."%' ";

        return $query;
    }

    //返金予定金額取得
    function checkHenkinDat($model, $data) {
        $schoolKind  = $model->schKindArr[CTRL_YEAR.$data["GRADE"]];

        $query  = " SELECT ";
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
        $query .= " WHERE ";
        $query .= "         SCHO.SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHO.SCHOOL_KIND     = '".$schoolKind."' ";
        $query .= "     AND SCHO.YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND SCHO.SCHREGNO        = '".$data["SCHREGNO"]."' ";
        $query .= "     AND SCHO.LINE_NO         = 1 ";
        $query .= "     AND SCHO.OUTGO_L_CD      = '99' ";
        $query .= "     AND SCHO.OUTGO_M_CD      = '99' ";
        $query .= "     AND SCHO.OUTGO_S_CD      = '999' ";
        $query .= "     AND OUTG.HENKIN_FLG      = '1' ";
        $query .= "     AND value(OUTG.HENKIN_APPROVAL, '0') = '0' ";
        $query .= "     AND OUTG.OUTGO_CANCEL    IS NULL ";
        $query .= "     AND OUTG.HENKIN_DATE     IS NULL ";
        $query .= "     AND BANK.ACCOUNTNO       = '".sprintf("%07d", $data["ACCOUNTNO"])."' ";
        $query .= "     AND BANK.ACCOUNTNAME  like '".rtrim($data["ACCOUNTNAME"])."%' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //入金フォーマット
            if ($model->field["INPUT"] == "1") {
                //2:データレコード以外は、処理しない。※1:ヘッダーレコード 8:トレーラレコード 9:エンドレコード
                if ($data_arr[$i]["RECORD_DIV"] != "2") {
                    continue;
                }

                //振替結果コードが"0"以外は、処理しない。
                if ($data_arr[$i]["RESULT_CD"] !== "0") {
                    continue;
                }

                //教職員データは、処理しない。2018/09/12現在
                if ($data_arr[$i]["GRADE"] == "00") {
                    continue;
                }

                $setPadeYear    = (mb_substr($data_arr[$i]["PAID_DATE"], 0, 2) <= 3) ? CTRL_YEAR + 1: CTRL_YEAR;
                $setPadeYM      = $setPadeYear.mb_substr($data_arr[$i]["PAID_DATE"], 0, 2);
                $setPadeDate    = $setPadeYear."-".mb_substr($data_arr[$i]["PAID_DATE"], 0, 2)."-".mb_substr($data_arr[$i]["PAID_DATE"], 2, 2);
                $setPlanYear    = "20".mb_substr($data_arr[$i]["PLAN_YEAR_MONTH"], 0, 2);
                $setPlanMonth   = mb_substr($data_arr[$i]["PLAN_YEAR_MONTH"], 2, 2);
                $setLimitMonth  = mb_substr($data_arr[$i]["PAID_DATE"], 0, 2);
                $setSchKind     = $model->schKindArr[CTRL_YEAR.$data_arr[$i]["GRADE"]];
                $befSlipNo      = "";
                $totalPlanMoney = 0;

                $query = knjp742Query::getCollectSlipPlanMDat($model, $data_arr[$i]["SCHREGNO"], $setSchKind, CTRL_YEAR, $setPlanYear, $setPlanMonth, $setLimitMonth, $data_arr[$i]["ACCOUNTNO"], $data_arr[$i]["ACCOUNTNAME"]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //COLLECT_SLIP_PLAN_M_DAT
                    $data = array();
                    $data["PAID_MONEY"][NUMBER]     = $row["PLAN_MONEY"];
                    $data["PAID_YEARMONTH"][TEXT]   = $setPadeYM;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                    $where .= "   AND SCHOOL_KIND  = '".$setSchKind."' ";
                    $where .= "   AND YEAR         = '".CTRL_YEAR."' ";
                    $where .= "   AND SCHREGNO     = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "   AND SLIP_NO      = '".$row["SLIP_NO"]."' ";
                    $where .= "   AND COLLECT_L_CD = '".$row["COLLECT_L_CD"]."' ";
                    $where .= "   AND COLLECT_M_CD = '".$row["COLLECT_M_CD"]."' ";
                    $where .= "   AND PLAN_YEAR    = '".$setPlanYear."' ";
                    $where .= "   AND PLAN_MONTH   = '".$setPlanMonth."' ";

                    $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                    $db->query($query);

                    //COLLECT_SLIP_PLAN_PAID_M_DAT
                    //maxSEQ取得
                    $maxSeq = $db->getOne(knjp742Query::getMaxSeqMdat($setSchKind, CTRL_YEAR, $data_arr[$i]["SCHREGNO"], $row["SLIP_NO"], $row["COLLECT_L_CD"], $row["COLLECT_M_CD"], $setPlanYear, $setPlanMonth));
                    $data = array();
                    $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]          = $setSchKind;
                    $data["YEAR"][TEXT]                 = CTRL_YEAR;
                    $data["SLIP_NO"][TEXT]              = $row["SLIP_NO"];
                    $data["COLLECT_L_CD"][TEXT]         = $row["COLLECT_L_CD"];
                    $data["COLLECT_M_CD"][TEXT]         = $row["COLLECT_M_CD"];
                    $data["PLAN_YEAR"][TEXT]            = $setPlanYear;
                    $data["PLAN_MONTH"][TEXT]           = $setPlanMonth;
                    $data["SEQ"][TEXT]                  = $maxSeq + 1;
                    $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                    $data["PLAN_PAID_MONEY_DATE"][DATE] = $setPadeDate;
                    $data["PLAN_PAID_MONEY"][NUMBER]    = $row["PLAN_MONEY"];
                    $data["PLAN_PAID_MONEY_DIV"][TEXT]  = "1"; // 1:引落
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_M_DAT");
                    $db->query($query);

                    //COLLECT_SLIP_PLAN_PAID_DAT
                    if ($befSlipNo != "" && $befSlipNo != $row["SLIP_NO"]) {
                        $maxSeq = $db->getOne(knjp742Query::getMaxSeqPaidDat($setSchKind, CTRL_YEAR, $data_arr[$i]["SCHREGNO"], $befSlipNo, $setPlanYear, $setPlanMonth));
                        $data = array();
                        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]          = $setSchKind;
                        $data["YEAR"][TEXT]                 = CTRL_YEAR;
                        $data["SLIP_NO"][TEXT]              = $befSlipNo;
                        $data["PLAN_YEAR"][TEXT]            = $setPlanYear;
                        $data["PLAN_MONTH"][TEXT]           = $setPlanMonth;
                        $data["SEQ"][TEXT]                  = $maxSeq + 1;
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["PLAN_PAID_MONEY_DATE"][DATE] = $setPadeDate;
                        $data["PLAN_PAID_MONEY"][NUMBER]    = $totalPlanMoney - $befReductionMoney;
                        $data["PLAN_PAID_MONEY_DIV"][TEXT]  = "1"; // 1:引落
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][NUMBER]            = "sysdate()";

                        $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_DAT");
                        $db->query($query);
                        $totalPlanMoney = 0;
                    }

                    $befSlipNo         = $row["SLIP_NO"];
                    $totalPlanMoney   += $row["PLAN_MONEY"];
                    $befReductionMoney = $row["COUNTRY_MONEY"] + $row["COUNTRY_ADDMONEY"] + $row["PREF_MONEY1"] + $row["PREF_MONEY2"] + $row["BURDEN_CHARGE1"] + $row["BURDEN_CHARGE2"] + $row["SCHOOL_1"] + $row["SCHOOL_2"];
                }

                //COLLECT_SLIP_PLAN_PAID_DAT(最後の伝票分を更新、上の処理内では最後の伝票が更新されないため)
                if ($totalPlanMoney != 0) {
                    $maxSeq = $db->getOne(knjp742Query::getMaxSeqPaidDat($setSchKind, CTRL_YEAR, $data_arr[$i]["SCHREGNO"], $befSlipNo, $setPlanYear, $setPlanMonth));
                    $data = array();
                    $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]          = $setSchKind;
                    $data["YEAR"][TEXT]                 = CTRL_YEAR;
                    $data["SLIP_NO"][TEXT]              = $befSlipNo;
                    $data["PLAN_YEAR"][TEXT]            = $setPlanYear;
                    $data["PLAN_MONTH"][TEXT]           = $setPlanMonth;
                    $data["SEQ"][TEXT]                  = $maxSeq + 1;
                    $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                    $data["PLAN_PAID_MONEY_DATE"][DATE] = $setPadeDate;
                    $data["PLAN_PAID_MONEY"][NUMBER]    = $totalPlanMoney - $befReductionMoney;
                    $data["PLAN_PAID_MONEY_DIV"][TEXT]  = "1"; // 1:引落
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_DAT");
                    $db->query($query);
                }

            //返金フォーマット
            } else {

                //2:データレコード以外は、処理しない。※1:ヘッダーレコード 8:トレーラレコード 9:エンドレコード
                if ($data_arr[$i]["RECORD_DIV"] != "2") continue;

                //教職員データは、処理しない。2018/12/13現在
                if ($data_arr[$i]["GRADE"] == "00") continue;

                $setSchKind     = $model->schKindArr[CTRL_YEAR.$data_arr[$i]["GRADE"]];
                $setHenkinYear  = (mb_substr($data_arr[$i]["HENKIN_DATE"], 0, 2) <= 3) ? CTRL_YEAR + 1: CTRL_YEAR;
                $setHenkinDate  = $setHenkinYear."-".mb_substr($data_arr[$i]["HENKIN_DATE"], 0, 2)."-".mb_substr($data_arr[$i]["HENKIN_DATE"], 2, 2);

                //LEVY_REQUEST_OUTGO_DAT更新
                $query = knjp742Query::getLevyRequestOutGoDat($model, $data_arr[$i]["SCHREGNO"], $setSchKind, CTRL_YEAR, $data_arr[$i]["ACCOUNTNO"], $data_arr[$i]["ACCOUNTNAME"]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $data = array();
                    $data["HENKIN_APPROVAL"][TEXT]  = '1';
                    $data["HENKIN_DATE"][DATE]      = $setHenkinDate;

                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "SYSDATE()";

                    $where  = " WHERE SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                    $where .= "   AND SCHOOL_KIND = '".$setSchKind."' ";
                    $where .= "   AND YEAR        = '".CTRL_YEAR."' ";
                    $where .= "   AND REQUEST_NO  = '".$row["REQUEST_NO"]."' ";
                    $where .= "   AND OUTGO_L_CD  = '".$row["OUTGO_L_CD"]."' ";
                    $where .= "   AND OUTGO_L_CD  = '".$row["OUTGO_L_CD"]."' ";

                    $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
                    $db->query($query);
                }
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //校種取得
    function getSchoolKind() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";

        return $query;
    }

    //COLLECT_SLIP_PLAN_M_DAT取得
    function getCollectSlipPlanMDat($model, $schregNo, $schKind, $year, $planYear, $planMonth, $limitMonth, $accountNo, $accountName) {

        $setMonthIn = "(";
        foreach ($model->monthArray as $month) {
            $setMonthIn .= $sep."'".$month."'";
            $sep = ",";
            if ($month == $limitMonth) break;
        }
        $setMonthIn .= ")";

        $query  = " SELECT ";
        $query .= "     PLAN.*, ";
        $query .= "     VALUE(CASE WHEN REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0) AS COUNTRY_MONEY, ";
        $query .= "     VALUE(CASE WHEN REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0) AS COUNTRY_ADDMONEY, ";
        $query .= "     VALUE(CASE WHEN REDUC_D1.OFFSET_FLG = '1' THEN REDUC_D1.DECISION_MONEY ELSE 0 END, 0) AS PREF_MONEY1, ";
        $query .= "     VALUE(CASE WHEN REDUC_D2.OFFSET_FLG = '1' THEN REDUC_D2.DECISION_MONEY ELSE 0 END, 0) AS PREF_MONEY2, ";
        $query .= "     VALUE(BURDEN_1.BURDEN_CHARGE, 0) AS BURDEN_CHARGE1, ";
        $query .= "     VALUE(BURDEN_2.BURDEN_CHARGE, 0) AS BURDEN_CHARGE2, ";
        $query .= "     VALUE(SCHOOL_1.PLAN_MONEY, 0) AS SCHOOL_1, ";
        $query .= "     VALUE(SCHOOL_2.PLAN_MONEY, 0) AS SCHOOL_2 ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT PLAN ";
        $query .= "     LEFT JOIN COLLECT_SLIP_DAT SLIP ON PLAN.SCHOOLCD    = SLIP.SCHOOLCD ";
        $query .= "                                    AND PLAN.SCHOOL_KIND = SLIP.SCHOOL_KIND ";
        $query .= "                                    AND PLAN.YEAR        = SLIP.YEAR ";
        $query .= "                                    AND PLAN.SLIP_NO     = SLIP.SLIP_NO ";
        $query .= "     LEFT JOIN REGISTBANK_DAT BANK ON PLAN.SCHOOLCD    = BANK.SCHOOLCD ";
        $query .= "                                  AND PLAN.SCHREGNO    = BANK.SCHREGNO ";
        $query .= "                                  AND SLIP.PAY_DIV     = BANK.SEQ ";
        $query .= "     LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LMIT ON PLAN.SCHOOLCD    = LMIT.SCHOOLCD ";
        $query .= "                                                   AND PLAN.SCHOOL_KIND = LMIT.SCHOOL_KIND ";
        $query .= "                                                   AND PLAN.YEAR        = LMIT.YEAR ";
        $query .= "                                                   AND PLAN.SCHREGNO    = LMIT.SCHREGNO ";
        $query .= "                                                   AND PLAN.SLIP_NO     = LMIT.SLIP_NO ";
        $query .= "                                                   AND PLAN.PLAN_YEAR   = LMIT.PLAN_YEAR ";
        $query .= "                                                   AND PLAN.PLAN_MONTH  = LMIT.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ON LMIT.SCHOOLCD         = REDUC_C.SCHOOLCD ";
        $query .= "                                                 AND LMIT.SCHOOL_KIND      = REDUC_C.SCHOOL_KIND ";
        $query .= "                                                 AND LMIT.YEAR             = REDUC_C.YEAR ";
        $query .= "                                                 AND LMIT.SLIP_NO          = REDUC_C.SLIP_NO ";
        $query .= "                                                 AND PLAN.PLAN_MONTH       = REDUC_C.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D1 ON LMIT.SCHOOLCD         = REDUC_D1.SCHOOLCD ";
        $query .= "                                         AND LMIT.SCHOOL_KIND       = REDUC_D1.SCHOOL_KIND ";
        $query .= "                                         AND LMIT.YEAR              = REDUC_D1.YEAR ";
        $query .= "                                         AND REDUC_D1.REDUCTION_TARGET = '1' ";
        $query .= "                                         AND LMIT.SLIP_NO           = REDUC_D1.SLIP_NO ";
        $query .= "                                         AND PLAN.PLAN_MONTH        = REDUC_D1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D2 ON LMIT.SCHOOLCD         = REDUC_D2.SCHOOLCD ";
        $query .= "                                         AND LMIT.SCHOOL_KIND       = REDUC_D2.SCHOOL_KIND ";
        $query .= "                                         AND LMIT.YEAR              = REDUC_D2.YEAR ";
        $query .= "                                         AND REDUC_D2.REDUCTION_TARGET = '2' ";
        $query .= "                                         AND LMIT.SLIP_NO           = REDUC_D2.SLIP_NO ";
        $query .= "                                         AND PLAN.PLAN_MONTH        = REDUC_D2.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_1 ON LMIT.SCHOOLCD        = BURDEN_1.SCHOOLCD ";
        $query .= "                                                      AND LMIT.SCHOOL_KIND       = BURDEN_1.SCHOOL_KIND ";
        $query .= "                                                      AND LMIT.YEAR              = BURDEN_1.YEAR ";
        $query .= "                                                      AND BURDEN_1.REDUCTION_TARGET = '1' ";
        $query .= "                                                      AND LMIT.SLIP_NO           = BURDEN_1.SLIP_NO ";
        $query .= "                                                      AND PLAN.PLAN_MONTH        = BURDEN_1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_2 ON LMIT.SCHOOLCD        = BURDEN_2.SCHOOLCD ";
        $query .= "                                                      AND LMIT.SCHOOL_KIND       = BURDEN_2.SCHOOL_KIND ";
        $query .= "                                                      AND LMIT.YEAR              = BURDEN_2.YEAR ";
        $query .= "                                                      AND BURDEN_2.REDUCTION_TARGET = '2' ";
        $query .= "                                                      AND LMIT.SLIP_NO           = BURDEN_2.SLIP_NO ";
        $query .= "                                                      AND PLAN.PLAN_MONTH        = BURDEN_2.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_1 ON LMIT.SCHOOLCD          = SCHOOL_1.SCHOOLCD ";
        $query .= "                                                 AND LMIT.SCHOOL_KIND       = SCHOOL_1.SCHOOL_KIND ";
        $query .= "                                                 AND LMIT.YEAR              = SCHOOL_1.YEAR ";
        $query .= "                                                 AND SCHOOL_1.REDUCTION_TARGET = '1' ";
        $query .= "                                                 AND LMIT.SLIP_NO           = SCHOOL_1.SLIP_NO ";
        $query .= "                                                 AND PLAN.PLAN_MONTH        = SCHOOL_1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_2 ON LMIT.SCHOOLCD          = SCHOOL_2.SCHOOLCD ";
        $query .= "                                                 AND LMIT.SCHOOL_KIND       = SCHOOL_2.SCHOOL_KIND ";
        $query .= "                                                 AND LMIT.YEAR              = SCHOOL_2.YEAR ";
        $query .= "                                                 AND SCHOOL_2.REDUCTION_TARGET = '2' ";
        $query .= "                                                 AND LMIT.SLIP_NO           = SCHOOL_2.SLIP_NO ";
        $query .= "                                                 AND PLAN.PLAN_MONTH        = SCHOOL_2.PLAN_MONTH ";
        $query .= " WHERE ";
        $query .= "         PLAN.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND PLAN.SCHOOL_KIND     = '".$schKind."' ";
        $query .= "     AND PLAN.YEAR            = '".$year."' ";
        $query .= "     AND PLAN.SCHREGNO        = '".$schregNo."' ";
        $query .= "     AND PLAN.PLAN_YEAR       = '{$planYear}' ";
        $query .= "     AND PLAN.PLAN_MONTH      = '{$planMonth}' ";
        $query .= "     AND PLAN.PLAN_MONEY      <> 0 ";
        $query .= "     AND PLAN.PAID_MONEY      IS NULL ";
        $query .= "     AND SLIP.CANCEL_DATE     IS NULL ";
        $query .= "     AND SLIP.PAY_DIV         IN ('1', '2') ";
        $query .= "     AND (LMIT.PAID_LIMIT_DATE IS NULL OR LMIT.PAID_LIMIT_MONTH IN {$setMonthIn}) ";
        $query .= "     AND BANK.ACCOUNTNO       = '".sprintf("%07d", $accountNo)."' ";
        $query .= "     AND BANK.ACCOUNTNAME     like '".rtrim($accountName)."%' ";

        return $query;
    }

    //maxSEQ取得(COLLECT_SLIP_PLAN_PAID_M_DAT)
    function getMaxSeqMdat($schKind, $setYear, $schno, $slipNo, $lcd, $mcd, $planYear, $planMonth) {
        $query  = " SELECT ";
        $query .= "     max(SEQ) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND  = '{$schKind}' ";
        $query .= "     AND YEAR         = '{$setYear}' ";
        $query .= "     AND SLIP_NO      = '{$slipNo}' ";
        $query .= "     AND COLLECT_L_CD = '{$lcd}' ";
        $query .= "     AND COLLECT_M_CD = '{$mcd}' ";
        $query .= "     AND PLAN_YEAR    = '{$planYear}' ";
        $query .= "     AND PLAN_MONTH   = '{$planMonth}' ";

        return $query;
   }

    //maxSEQ取得(COLLECT_SLIP_PLAN_PAID_DAT)
    function getMaxSeqPaidDat($schKind, $setYear, $schno, $slipNo, $planYear, $planMonth) {
        $query  = " SELECT ";
        $query .= "     max(SEQ) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND  = '{$schKind}' ";
        $query .= "     AND YEAR         = '{$setYear}' ";
        $query .= "     AND SLIP_NO      = '{$slipNo}' ";
        $query .= "     AND PLAN_YEAR    = '{$planYear}' ";
        $query .= "     AND PLAN_MONTH   = '{$planMonth}' ";

        return $query;
   }

    //返金伝票取得(LEVY_REQUEST_OUTGO_DAT)
    function getLevyRequestOutGoDat($model, $schregNo, $schKind, $year, $accountNo, $accountName) {
        $query  = " SELECT ";
        $query .= "     OUTG.* ";
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
        $query .= " WHERE ";
        $query .= "         SCHO.SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHO.SCHOOL_KIND     = '".$schKind."' ";
        $query .= "     AND SCHO.YEAR            = '".$year."' ";
        $query .= "     AND SCHO.SCHREGNO        = '".$schregNo."' ";
        $query .= "     AND SCHO.LINE_NO         = 1 ";
        $query .= "     AND SCHO.OUTGO_L_CD      = '99' ";
        $query .= "     AND SCHO.OUTGO_M_CD      = '99' ";
        $query .= "     AND SCHO.OUTGO_S_CD      = '999' ";
        $query .= "     AND OUTG.HENKIN_FLG      = '1' ";
        $query .= "     AND value(OUTG.HENKIN_APPROVAL, '0') = '0' ";
        $query .= "     AND OUTG.OUTGO_CANCEL    IS NULL ";
        $query .= "     AND OUTG.HENKIN_DATE     IS NULL ";
        $query .= "     AND BANK.ACCOUNTNO       = '".sprintf("%07d", $accountNo)."' ";
        $query .= "     AND BANK.ACCOUNTNAME  like '".rtrim($accountName)."%' ";

        return $query;
   }

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }
}
?>
