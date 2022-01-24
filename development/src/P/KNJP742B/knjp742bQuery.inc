<?php
class knjp742bquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJP742B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    /******************/
    /**  校種取得    **/
    /******************/
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
        $query .= "     YEAR = '".CTRL_YEAR."' ";
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

    /******************/
    /**  銀行取得    **/
    /******************/
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
        $query .= "     AND SBANK.YEAR        = '".CTRL_YEAR."' ";
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

    /******************/
    /**  引落し月    **/
    /******************/
    public function getPaidMonth($model)
    {
        $query  = " WITH MONTH(LABEL, VALUE, SORT) AS ( ";
        $sep = "";
        for ($i = 1; $i <= 12; $i++) {
            $monthLabel = sprintf("%2d月", $i);
            $monthVal   = sprintf("%02d", $i);
            $sortVal   = sprintf("%02d", ($i < 4) ? $i + 12 : $i);
            $query .= " {$sep} ";
            $query .= " VALUES( '".$monthLabel."', '".$monthVal."', '".$sortVal."') ";
            $sep = "UNION";
        }
        $query .= " ) ";
        $query .= "  ";
        $query .= " SELECT LABEL, VALUE FROM MONTH ";
        $query .= " ORDER BY SORT ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //学校銀行情報取得
    public function getCollectSchoolBankMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND FORMAT_DIV  = '".$model->field["INPUT"]."'";
        $query .= "     AND SEQ         = '{$model->seq}' ";
        $query .= "     AND BANK_CD     = '{$model->field["BANK_CD"]}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";

        return $query;
    }

    //SCHREG_BASE_MST
    public function checkBaseMstSql($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //顧客番号登録あるか
    public function checkCustomerDat($custNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHREG_CUSTOMER_DAT ";
        $query .= " WHERE ";
        $query .= "     CUSTOMER_NUMBER = '{$custNo}' ";

        return $query;
    }

    //顧客番号取得
    public function getCustomerDat()
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     CUSTOMER_NUMBER ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHREG_CUSTOMER_DAT ";

        return $query;
    }

    //校種取得取得
    public function getSchoolKindSch()
    {
        $query  = " SELECT ";
        $query .= "     CUST.CUSTOMER_NUMBER, ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHREG_CUSTOMER_DAT CUST ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = CUST.SCHREGNO ";
        $query .= "                                   AND REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";

        return $query;
    }

    //COLLECT_SLIP_PLAN_CSV_DATカウント取得
    public function getPlanCsvDat($model)
    {
        $query  = " SELECT ";
        $query .= "     CSVD.SCHOOL_KIND, ";
        $query .= "     BANK.ACCOUNTNO, ";
        $query .= "     BANK.ACCOUNTNAME, ";
        $query .= "     CUST.CUSTOMER_NUMBER, ";
        $query .= "     CSVD.PLAN_MONEY, ";
        $query .= "     CSVD.PLAN_MONTH, ";
        $query .= "     CSVD.PAID_FLG ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_CSV_DAT CSVD ";
        $query .= "     LEFT JOIN REGISTBANK_DAT BANK ON BANK.SCHOOLCD    = CSVD.SCHOOLCD ";
        $query .= "                                  AND BANK.SCHREGNO    = CSVD.SCHREGNO ";
        $query .= "                                  AND BANK.SEQ         = CSVD.PAY_DIV ";
        $query .= "     INNER JOIN COLLECT_SCHREG_CUSTOMER_DAT CUST ON CUST.SCHREGNO = CSVD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         CSVD.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND CSVD.YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND CSVD.PAY_DIV         IN ('1', '2') ";

        return $query;
    }

    //返金予定金額取得
    public function checkHenkinDat($model, $data)
    {
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
        $query .= "     AND right('0000000' || BANK.ACCOUNTNO, 7)       = '".sprintf("%07d", $data["ACCOUNTNO"])."' ";
        $query .= "     AND BANK.ACCOUNTNAME  like '".rtrim($data["ACCOUNTNAME"])."%' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $dataRecordSeq = 1;
        for ($i = 0; $i < count($data_arr); $i++) {

            //入金フォーマット
            if ($model->field["INPUT"] == "1") {
                //SIGELシステム使用
                if ($model->Properties["useSIGELsystem"] == "1") {
                    $dataRecordSeq = knjp742bQuery::getSigelTableDelIns($db, $data_arr[$i], $dataRecordSeq);
                }

                //2:データレコード以外は、処理しない。※1:ヘッダーレコード 8:トレーラレコード 9:エンドレコード
                if ($data_arr[$i]["RECORD_DIV"] != "2") {
                    continue;
                }

                //振替結果コードが"0"以外は、処理しない。
                if ($data_arr[$i]["RESULT_CD"] !== "0") {
                    continue;
                }

                $setPadeYear    = ($setPadeMonth < 4) ? CTRL_YEAR + 1: CTRL_YEAR;
                $setPadeMonth   = mb_substr($data_arr[$i]["PAID_DATE"], 0, 2);
                $setPadeDay     = mb_substr($data_arr[$i]["PAID_DATE"], 2, 2);
                $setPadeDate    = $setPadeYear."-".$setPadeMonth."-".$setPadeDay;

                if ($setPadeDate > CTRL_DATE) {
                    $setPadeDate    = ($setPadeYear - 1)."-".$setPadeMonth."-".$setPadeDay;
                    $setPadeYear--;
                }
                $setPadeYM      = $setPadeYear.$setPadeMonth;

                $setLimitMonth  = ($model->field["PAID_MONTH"]) ? $model->field["PAID_MONTH"] : mb_substr($data_arr[$i]["PAID_DATE"], 0, 2);
                $customerNumber = $data_arr[$i]["CUSTOMER_NUMBER"];
                $acountNo       = $data_arr[$i]["ACCOUNTNO"];
                $acountName     = $data_arr[$i]["ACCOUNTNAME"];
                $planMoney      = $data_arr[$i]["PLAN_MONEY"];
                $befSlipNo      = "";

                //学籍番号取得
                $query = knjp742bQuery::getSchregNo($customerNumber);
                $schregNo = $db->getOne($query);

                //校種取得
                $query = knjp742bQuery::getSchoolKind($schregNo);
                $schkind = $db->getOne($query);

                //伝票毎、月毎の金額を更新
                $query = knjp742bQuery::getCollectSlipPlanMDat($model, $schkind, CTRL_YEAR, $schregNo, $setPadeYear, $setPadeMonth, $setLimitMonth, $customerNumber, $acountNo, $acountName, "MONTH");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowSchoolCd            = $row["SCHOOLCD"];
                    $rowSchoolKind          = $row["SCHOOL_KIND"];
                    $rowYear                = $row["YEAR"];
                    $rowSchregNo            = $row["SCHREGNO"];
                    $rowSlipNo              = $row["SLIP_NO"];
                    $rowPlanYear            = $row["PLAN_YEAR"];
                    $rowPlanMonth           = $row["PLAN_MONTH"];
                    $rowPlanMoney           = $row["PLAN_MONEY"];
                    $rowReducedPlanMoney    = $row["REDUCED_PLAN_MONEY"];

                    $maxSeq = $db->getOne(knjp742bQuery::getMaxSeqPaidDat($rowSchoolKind, $rowYear, $rowSlipNo, $rowPlanYear, $rowPlanMonth));
                    $data = array();
                    $data["SCHOOLCD"][TEXT]             = $rowSchoolCd;
                    $data["SCHOOL_KIND"][TEXT]          = $rowSchoolKind;
                    $data["YEAR"][TEXT]                 = $rowYear;
                    $data["SLIP_NO"][TEXT]              = $rowSlipNo;
                    $data["PLAN_YEAR"][TEXT]            = $rowPlanYear;
                    $data["PLAN_MONTH"][TEXT]           = $rowPlanMonth;
                    $data["SEQ"][TEXT]                  = $maxSeq + 1;
                    $data["SCHREGNO"][TEXT]             = $rowSchregNo;
                    $data["PLAN_PAID_MONEY_DATE"][DATE] = $setPadeDate;
                    $data["PLAN_PAID_MONEY"][NUMBER]    = $rowReducedPlanMoney;
                    $data["PLAN_PAID_MONEY_DIV"][TEXT]  = "1"; // 1:引落
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_DAT");
                    $db->query($query);
                }

                //更新用データ取得
                $query = knjp742bQuery::getCollectSlipPlanMDat($model, $schkind, CTRL_YEAR, $schregNo, $setPadeYear, $setPadeMonth, $setLimitMonth, $customerNumber, $acountNo, $acountName, "MAIN");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowSchoolCd    = $row["SCHOOLCD"];
                    $rowSchoolKind  = $row["SCHOOL_KIND"];
                    $rowYear        = $row["YEAR"];
                    $rowSchregNo    = $row["SCHREGNO"];
                    $rowSlipNo      = $row["SLIP_NO"];
                    $rowCollectLcd  = $row["COLLECT_L_CD"];
                    $rowCollectMcd  = $row["COLLECT_M_CD"];
                    $rowPlanYear    = $row["PLAN_YEAR"];
                    $rowPlanMonth   = $row["PLAN_MONTH"];
                    $rowPlanMoney   = $row["PLAN_MONEY"];
                    $rowPayDiv      = $row["PAY_DIV"];

                    //COLLECT_SLIP_PLAN_M_DAT
                    $data = array();
                    $data["PAID_MONEY"][NUMBER]     = $rowPlanMoney;
                    $data["PAID_YEARMONTH"][TEXT]   = $setPadeYM;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $where  = " WHERE SCHOOLCD     = '{$rowSchoolCd}' ";
                    $where .= "   AND SCHOOL_KIND  = '{$rowSchoolKind}' ";
                    $where .= "   AND YEAR         = '{$rowYear}' ";
                    $where .= "   AND SCHREGNO     = '{$rowSchregNo}' ";
                    $where .= "   AND SLIP_NO      = '{$rowSlipNo}' ";
                    $where .= "   AND COLLECT_L_CD = '{$rowCollectLcd}' ";
                    $where .= "   AND COLLECT_M_CD = '{$rowCollectMcd}' ";
                    $where .= "   AND PLAN_YEAR    = '{$rowPlanYear}' ";
                    $where .= "   AND PLAN_MONTH   = '{$rowPlanMonth}' ";

                    $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                    $db->query($query);

                    //COLLECT_SLIP_PLAN_PAID_M_DAT
                    //maxSEQ取得
                    $maxSeq = $db->getOne(knjp742bQuery::getMaxSeqMdat($rowSchoolKind, $rowYear, $rowSlipNo, $rowCollectLcd, $rowCollectMcd, $rowPlanYear, $rowPlanMonth));
                    $data = array();
                    $data["SCHOOLCD"][TEXT]             = $rowSchoolCd;
                    $data["SCHOOL_KIND"][TEXT]          = $rowSchoolKind;
                    $data["YEAR"][TEXT]                 = $rowYear;
                    $data["SLIP_NO"][TEXT]              = $rowSlipNo;
                    $data["COLLECT_L_CD"][TEXT]         = $rowCollectLcd;
                    $data["COLLECT_M_CD"][TEXT]         = $rowCollectMcd;
                    $data["PLAN_YEAR"][TEXT]            = $rowPlanYear;
                    $data["PLAN_MONTH"][TEXT]           = $rowPlanMonth;
                    $data["SEQ"][TEXT]                  = $maxSeq + 1;
                    $data["SCHREGNO"][TEXT]             = $rowSchregNo;
                    $data["PLAN_PAID_MONEY_DATE"][DATE] = $setPadeDate;
                    $data["PLAN_PAID_MONEY"][NUMBER]    = $rowPlanMoney;
                    $data["PLAN_PAID_MONEY_DIV"][TEXT]  = "1"; // 1:引落
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_M_DAT");
                    $db->query($query);

                    //COLLECT_SLIP_PLAN_CSV_DAT更新（支払いフラグを立てる）
                    if ($befSlipNo != "" && $befSlipNo != $rowSlipNo) {
                        $data = array();
                        $data["PAID_MONEY"][NUMBER]     = $planMoney;
                        $data["PAID_YEARMONTH"][TEXT]   = $setPadeYM;
                        $data["PAID_FLG"][TEXT]         = '1';
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][NUMBER]        = "sysdate()";

                        $where  = " WHERE SCHOOLCD     = '{$rowSchoolCd}' ";
                        $where .= "   AND SCHOOL_KIND  = '{$rowSchoolKind}' ";
                        $where .= "   AND YEAR         = '{$rowYear}' ";
                        $where .= "   AND SCHREGNO     = '{$rowSchregNo}' ";
                        $where .= "   AND PLAN_YEAR    = '{$rowPlanYear}' ";
                        $where .= "   AND PLAN_MONTH   = '{$rowPlanMonth}' ";
                        $where .= "   AND PAY_DIV      = '{$rowPayDiv}' ";

                        $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_CSV_DAT", $where);
                        $db->query($query);
                    }

                    $befSlipNo = $rowSlipNo;
                }
                $cnt++;
            //返金フォーマット
            } else {

                //2:データレコード以外は、処理しない。※1:ヘッダーレコード 8:トレーラレコード 9:エンドレコード
                if ($data_arr[$i]["RECORD_DIV"] != "2") {
                    continue;
                }

                //振替結果コードが"0"以外は、処理しない。
                if ($data_arr[$i]["RESULT_CD"] !== "0") {
                    continue;
                }

                //教職員データは、処理しない。2018/12/13現在
                if ($data_arr[$i]["GRADE"] == "00") {
                    continue;
                }

                $setSchKind     = $model->schKindArr[CTRL_YEAR.$data_arr[$i]["GRADE"]];
                $setHenkinYear  = (mb_substr($data_arr[$i]["HENKIN_DATE"], 0, 2) <= 3) ? CTRL_YEAR + 1: CTRL_YEAR;
                $setHenkinDate  = $setHenkinYear."-".mb_substr($data_arr[$i]["HENKIN_DATE"], 0, 2)."-".mb_substr($data_arr[$i]["HENKIN_DATE"], 2, 2);

                //LEVY_REQUEST_OUTGO_DAT更新
                $query = knjp742bQuery::getLevyRequestOutGoDat($model, $schregNo, $setSchKind, CTRL_YEAR, $data_arr[$i]["ACCOUNTNO"], $data_arr[$i]["ACCOUNTNAME"]);
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
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }

    //学籍番号取得
    public function getSchregNo($custNo)
    {
        $query  = " SELECT ";
        $query .= "     CUST.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHREG_CUSTOMER_DAT CUST ";
        $query .= " WHERE ";
        $query .= "     CUST.CUSTOMER_NUMBER = '{$custNo}' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //COLLECT_SLIP_PLAN_M_DAT取得
    public function getCollectSlipPlanMDat($model, $schkind, $year, $schregNo, $pYear, $pMonth, $limitMonth, $customerNumber, $accountNo, $accountName, $div = "")
    {
        $query  = " WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         PLAN.*, ";
        $query .= "         SLIP.PAY_DIV, ";
        $query .= "         VALUE(CASE WHEN REDCP.OFFSET_FLG = '1' THEN REDCP.DECISION_MONEY ELSE 0 END, 0) AS COUNTRY_MONEY, ";
        $query .= "         VALUE(CASE WHEN REDCP.ADD_OFFSET_FLG = '1' THEN REDCP.ADD_DECISION_MONEY ELSE 0 END, 0) AS COUNTRY_ADDMONEY, ";
        $query .= "         VALUE(CASE WHEN REDP.OFFSET_FLG = '1' THEN REDP.DECISION_MONEY ELSE 0 END, 0) AS PREF_MONEY1, ";
        $query .= "         VALUE(CASE WHEN REDP.OFFSET_FLG = '1' THEN REDP.DECISION_MONEY ELSE 0 END, 0) AS PREF_MONEY2, ";
        $query .= "         VALUE(BURDEN.BURDEN_CHARGE, 0) AS BURDEN_CHARGE, ";
        $query .= "         VALUE(REDSP.PLAN_MONEY, 0) AS GENMEN_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT PLAN ";
        $query .= "     LEFT JOIN COLLECT_SLIP_DAT SLIP ";
        $query .= "          ON PLAN.SCHOOLCD           = SLIP.SCHOOLCD ";
        $query .= "         AND PLAN.SCHOOL_KIND        = SLIP.SCHOOL_KIND ";
        $query .= "         AND PLAN.YEAR               = SLIP.YEAR ";
        $query .= "         AND PLAN.SLIP_NO            = SLIP.SLIP_NO ";
        $query .= "     LEFT JOIN REGISTBANK_DAT BANK ";
        $query .= "          ON PLAN.SCHOOLCD           = BANK.SCHOOLCD ";
        $query .= "         AND PLAN.SCHREGNO           = BANK.SCHREGNO ";
        $query .= "         AND SLIP.PAY_DIV            = BANK.SEQ ";
        $query .= "     LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LMIT ";
        $query .= "          ON PLAN.SCHOOLCD           = LMIT.SCHOOLCD ";
        $query .= "         AND PLAN.SCHOOL_KIND        = LMIT.SCHOOL_KIND ";
        $query .= "         AND PLAN.YEAR               = LMIT.YEAR ";
        $query .= "         AND PLAN.SCHREGNO           = LMIT.SCHREGNO ";
        $query .= "         AND PLAN.SLIP_NO            = LMIT.SLIP_NO ";
        $query .= "         AND PLAN.PLAN_YEAR          = LMIT.PLAN_YEAR ";
        $query .= "         AND PLAN.PLAN_MONTH         = LMIT.PLAN_MONTH ";
        $query .= "     INNER JOIN COLLECT_SCHREG_CUSTOMER_DAT CUST ";
        $query .= "          ON CUST.SCHREGNO           = PLAN.SCHREGNO ";
        $query .= "         AND CUST.CUSTOMER_NUMBER    = '".$customerNumber."' ";
        $query .= "     LEFT JOIN COLLECT_M_MST CMMST ";
        $query .= "         ON CMMST.SCHOOLCD           = PLAN.SCHOOLCD ";
        $query .= "         AND CMMST.SCHOOL_KIND       = PLAN.SCHOOL_KIND ";
        $query .= "         AND CMMST.YEAR              = PLAN.YEAR ";
        $query .= "         AND CMMST.COLLECT_L_CD      = PLAN.COLLECT_L_CD ";
        $query .= "         AND CMMST.COLLECT_M_CD      = PLAN.COLLECT_M_CD ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDCP ";
        $query .= "          ON REDCP.SCHOOLCD          = PLAN.SCHOOLCD ";
        $query .= "         AND REDCP.SCHOOL_KIND       = PLAN.SCHOOL_KIND ";
        $query .= "         AND REDCP.YEAR              = PLAN.YEAR ";
        $query .= "         AND REDCP.SLIP_NO           = PLAN.SLIP_NO ";
        $query .= "         AND REDCP.PLAN_YEAR         = PLAN.PLAN_YEAR ";
        $query .= "         AND REDCP.PLAN_MONTH        = PLAN.PLAN_MONTH ";
        $query .= "         AND REDCP.SCHREGNO          = PLAN.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDP ";
        $query .= "          ON REDP.SCHOOLCD           = PLAN.SCHOOLCD ";
        $query .= "         AND REDP.SCHOOL_KIND        = PLAN.SCHOOL_KIND ";
        $query .= "         AND REDP.YEAR               = PLAN.YEAR ";
        $query .= "         AND REDP.REDUCTION_TARGET   = CMMST.GAKUNOKIN_DIV ";
        $query .= "         AND REDP.SLIP_NO            = PLAN.SLIP_NO ";
        $query .= "         AND REDP.PLAN_YEAR          = PLAN.PLAN_YEAR ";
        $query .= "         AND REDP.PLAN_MONTH         = PLAN.PLAN_MONTH ";
        $query .= "         AND REDP.SCHREGNO           = PLAN.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT REDSP ";
        $query .= "          ON REDSP.SCHOOLCD          = PLAN.SCHOOLCD ";
        $query .= "         AND REDSP.SCHOOL_KIND       = PLAN.SCHOOL_KIND ";
        $query .= "         AND REDSP.YEAR              = PLAN.YEAR ";
        $query .= "         AND REDSP.REDUCTION_TARGET  = CMMST.GAKUNOKIN_DIV ";
        $query .= "         AND REDSP.SLIP_NO           = PLAN.SLIP_NO ";
        $query .= "         AND REDSP.PLAN_YEAR         = PLAN.PLAN_YEAR ";
        $query .= "         AND REDSP.PLAN_MONTH        = PLAN.PLAN_MONTH ";
        $query .= "         AND REDSP.SCHREGNO          = PLAN.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN ";
        $query .= "          ON BURDEN.SCHOOLCD         = PLAN.SCHOOLCD ";
        $query .= "         AND BURDEN.SCHOOL_KIND      = PLAN.SCHOOL_KIND ";
        $query .= "         AND BURDEN.YEAR             = PLAN.YEAR ";
        $query .= "         AND BURDEN.REDUCTION_TARGET = CMMST.GAKUNOKIN_DIV ";
        $query .= "         AND BURDEN.SLIP_NO          = PLAN.SLIP_NO ";
        $query .= "         AND BURDEN.SCHREGNO         = PLAN.SCHREGNO ";
        $query .= "         AND BURDEN.PLAN_YEAR        = PLAN.PLAN_YEAR ";
        $query .= "         AND BURDEN.PLAN_MONTH       = PLAN.PLAN_MONTH ";
        $query .= "     WHERE ";
        $query .= "             PLAN.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND PLAN.SCHOOL_KIND     = '".$schkind."' ";
        $query .= "         AND PLAN.YEAR            = '".$year."' ";
        $query .= "         AND PLAN.SCHREGNO        = '".$schregNo."' ";
        $query .= "         AND PLAN.PLAN_MONEY      <> 0 ";
        $query .= "         AND PLAN.PAID_MONEY      IS NULL ";
        $query .= "         AND SLIP.CANCEL_DATE     IS NULL ";
        $query .= "         AND SLIP.PAY_DIV         IN ('1', '2') ";
        $query .= "         AND LMIT.PAID_LIMIT_MONTH = '{$limitMonth}' ";
        $query .= "         AND right('0000000' || BANK.ACCOUNTNO, 7) = '".sprintf("%07d", $accountNo)."' ";
        $query .= "         AND BANK.ACCOUNTNAME     like '".rtrim($accountName)."%' ";
        $query .= " ) ";
        $query .= " , MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         CASE WHEN T1.PLAN_MONEY - T1.COUNTRY_MONEY - T1.COUNTRY_ADDMONEY - T1.PREF_MONEY1 - T1.PREF_MONEY2 - T1.BURDEN_CHARGE - T1.GENMEN_MONEY > 0 ";
        $query .= "                 THEN T1.PLAN_MONEY - T1.COUNTRY_MONEY - T1.COUNTRY_ADDMONEY - T1.PREF_MONEY1 - T1.PREF_MONEY2 - T1.BURDEN_CHARGE - T1.GENMEN_MONEY ";
        $query .= "              ELSE ";
        $query .= "                 0 ";
        $query .= "         END AS REDUCED_PLAN_MONEY ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= " ) ";
        if ($div == "MAIN") {
            $query .= " SELECT ";
            $query .= "     T1.* ";
            $query .= " FROM ";
            $query .= "     MAIN T1 ";
            $query .= " ORDER BY ";
            $query .= "     T1.SLIP_NO, ";
            $query .= "     T1.COLLECT_L_CD, ";
            $query .= "     T1.COLLECT_M_CD, ";
            $query .= "     T1.PLAN_YEAR, ";
            $query .= "     T1.PLAN_MONTH ";
        } elseif ($div == "MONTH") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOLCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SLIP_NO, ";
            $query .= "     T1.PLAN_YEAR, ";
            $query .= "     T1.PLAN_MONTH ,";
            $query .= "     SUM(T1.PLAN_MONEY) AS PLAN_MONEY, ";
            $query .= "     SUM(T1.REDUCED_PLAN_MONEY) AS REDUCED_PLAN_MONEY ";
            $query .= " FROM ";
            $query .= "     MAIN T1 ";
            $query .= " GROUP BY ";
            $query .= "     T1.SCHOOLCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SLIP_NO, ";
            $query .= "     T1.PLAN_YEAR, ";
            $query .= "     T1.PLAN_MONTH ";
            $query .= " ORDER BY ";
            $query .= "     T1.SLIP_NO, ";
            $query .= "     T1.PLAN_YEAR, ";
            $query .= "     T1.PLAN_MONTH ";
        }

        return $query;
    }

    //maxSEQ取得(COLLECT_SLIP_PLAN_PAID_M_DAT)
    public function getMaxSeqMdat($schKind, $setYear, $slipNo, $lcd, $mcd, $planYear, $planMonth)
    {
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
    public function getMaxSeqPaidDat($schKind, $setYear, $slipNo, $planYear, $planMonth)
    {
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
    public function getLevyRequestOutGoDat($model, $schregNo, $schKind, $year, $accountNo, $accountName)
    {
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
        $query .= "     AND right('0000000' || BANK.ACCOUNTNO, 7) = '".sprintf("%07d", $accountNo)."' ";
        $query .= "     AND BANK.ACCOUNTNAME   like '".rtrim($accountName)."%' ";

        return $query;
    }

    //データの追加（SIGEL）
    public function getSigelTableDelIns($db, $data_arr, $dataRecordSeq)
    {
        //ヘッダー・レコード
        if ($data_arr["DATA_DIV"] == '1') {
            //COLLECT_ZENGIN_HEADER_RECORD_DAT
            $query = knjp742bQuery::delZenginDat("COLLECT_ZENGIN_HEADER_RECORD_DAT", $data_arr["DIRECT_DEBIT"]);
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["DATA_DIV"][TEXT]     = $data_arr["DATA_DIV"];
            $data["TYPE_CD"][TEXT]      = $data_arr["TYPE_CD"];
            $data["CD_DIV"][TEXT]       = $data_arr["CD_DIV"];
            $data["CLIENT_CD"][TEXT]    = $data_arr["CLIENT_CD"];
            $data["CLIENT_NAME"][TEXT]  = $data_arr["CLIENT_NAME"];
            $data["DIRECT_DEBIT"][TEXT] = $data_arr["DIRECT_DEBIT"];
            $data["T_BANKCD"][TEXT]     = $data_arr["T_BANKCD"];
            $data["T_BANKNAME"][TEXT]   = $data_arr["T_BANKNAME"];
            $data["T_BRANCHCD"][TEXT]   = $data_arr["T_BRANCHCD"];
            $data["T_BRANCHNAME"][TEXT] = $data_arr["T_BRANCHNAME"];
            $data["DEPOSIT_DIV"][TEXT]  = $data_arr["DEPOSIT_DIV"];
            $data["T_ACCOUNTNO"][TEXT]  = $data_arr["T_ACCOUNTNO"];
            $data["DUMMY"][TEXT]        = $data_arr["DUMMY"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = knjp742bQuery::insertSQL($data, "COLLECT_ZENGIN_HEADER_RECORD_DAT");//空白も追加するため自作関数
            $db->query($query);

        //データ・レコード
        } elseif ($data_arr["DATA_DIV"] == '2') {
            //delete
            if ($dataRecordSeq == '1') {
                $query = knjp742bQuery::delZenginDat("COLLECT_ZENGIN_DATA_RECORD_DAT", $data_arr["DIRECT_DEBIT"]);
                $db->query($query);
            }

            //insert
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["DIRECT_DEBIT"][TEXT]     = $data_arr["DIRECT_DEBIT"];
            $data["SEQ"][NUMBER]            = $dataRecordSeq;
            $data["DATA_DIV"][TEXT]         = $data_arr["DATA_DIV"];
            $data["BANKCD"][TEXT]           = $data_arr["BANKCD"];
            $data["BANKNAME"][TEXT]         = $data_arr["BANKNAME"];
            $data["BRANCHCD"][TEXT]         = $data_arr["BRANCHCD"];
            $data["BRANCHNAME"][TEXT]       = $data_arr["BRANCHNAME"];
            $data["DUMMY1"][TEXT]           = $data_arr["DUMMY1"];
            $data["DEPOSIT_DIV"][TEXT]      = $data_arr["DEPOSIT_ITEM"];
            $data["ACCOUNTNO"][TEXT]        = $data_arr["ACCOUNTNO"];
            $data["ACCOUNTNAME"][TEXT]      = $data_arr["ACCOUNTNAME"];
            $data["PLAN_MONEY"][TEXT]       = $data_arr["PLAN_MONEY"];
            $data["NEW_CD"][TEXT]           = $data_arr["SHINKI_CD"];
            $data["CUSTOMER_CD"][TEXT]      = $data_arr["CUSTOMER_NUMBER"];
            $data["RESULT_CD"][TEXT]        = $data_arr["RESULT_CD"];
            $data["DUMMY2"][TEXT]           = $data_arr["DUMMY2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = knjp742bQuery::insertSQL($data, "COLLECT_ZENGIN_DATA_RECORD_DAT");
            $db->query($query);

            $dataRecordSeq++;

        //トレーラ・レコード(COLLECT_ZENGIN_TRAILER_RECORD_DAT)
        } elseif ($data_arr["DATA_DIV"] == '8') {
            //delete
            $query = knjp742bQuery::delZenginDat("COLLECT_ZENGIN_TRAILER_RECORD_DAT", $data_arr["DIRECT_DEBIT"]);
            $db->query($query);

            //insert
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["DIRECT_DEBIT"][TEXT]         = $data_arr["DIRECT_DEBIT"];
            $data["DATA_DIV"][TEXT]             = $data_arr["DATA_DIV"];
            $data["TOTAL_CNT"][TEXT]            = $data_arr["TOTAL_CNT"];
            $data["TOTAL_MONEY"][TEXT]          = $data_arr["TOTAL_MONEY"];
            $data["TRANSFER_CNT"][TEXT]         = $data_arr["TRANSFER_CNT"];
            $data["TRANSFER_MONEY"][TEXT]       = $data_arr["TRANSFER_MONEY"];
            $data["NOT_TRANSFER_CNT"][TEXT]     = $data_arr["NOT_TRANSFER_CNT"];
            $data["NOT_TRANSFER_MONEY"][TEXT]   = $data_arr["NOT_TRANSFER_MONEY"];
            $data["DUMMY"][TEXT]                = $data_arr["DUMMY"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $query = knjp742bQuery::insertSQL($data, "COLLECT_ZENGIN_TRAILER_RECORD_DAT");
            $db->query($query);

        //エンド・レコード(COLLECT_ZENGIN_END_RECORD_DAT)
        } elseif ($data_arr["DATA_DIV"] == '9') {
            //delete
            $query = knjp742bQuery::delZenginDat("COLLECT_ZENGIN_END_RECORD_DAT", $data_arr["DIRECT_DEBIT"]);
            $db->query($query);

            //insert
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["DIRECT_DEBIT"][TEXT] = $data_arr["DIRECT_DEBIT"];
            $data["DATA_DIV"][TEXT]     = $data_arr["DATA_DIV"];
            $data["DUMMY"][TEXT]        = $data_arr["DUMMY"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = knjp742bQuery::insertSQL($data, "COLLECT_ZENGIN_END_RECORD_DAT");
            $db->query($query);
        }

        return $dataRecordSeq;
    }

    //ZENGIN関係削除
    public function delZenginDat($table, $directDebit)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND DIRECT_DEBIT = '{$directDebit}' ";

        return $query;
    }

    //insert文作成
    public function insertSQL($arg, $table)
    {
        $fields = array_keys($arg);
        $sql = "insert into $table(" .implode($fields, ",") .") ";

        $sql .= "values(";
        $sp = "";
        foreach ($fields as $f) {
            $key = key($arg[$f]);
            if ($arg[$f][$key] == '') {
                $sql .= $sp ."NULL";
            } else {
                switch ($key) {
                    case TEXT:
                        $sql .= $sp ."'" .Query::addquote($arg[$f][$key]) ."'";
                        break;
                    case NUMBER:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                    case FUNC:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                    case DATE:
                        $sql .= $sp ."'" .Query::date2sql($arg[$f][$key]) ."'";
                        break;
                    default:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                }
            }
            $sp = ",";
        }
        return $sql .= ")";
    }

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }
}
