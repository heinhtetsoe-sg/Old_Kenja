<?php

require_once('for_php7.php');
class knjp826query extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/
    //引落日
    function getDirectDebit($model, $directDebit='') {
        $query  = " SELECT ";
        if ($directDebit) {
            $query .= "     OUTPUT_FLG ";
        } else {
            $query .= "     DIRECT_DEBIT AS VALUE, ";
            $query .= "     substr(DIRECT_DEBIT, 1, 2) || '/' || substr(DIRECT_DEBIT, 3, 2) || ";
            $query .= "     case ";
            $query .= "         when OUTPUT_FLG = '1' then '（済）' ";
            $query .= "         else '　　　' ";
            $query .= "     end AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_ZENGIN_HEADER_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->field["REDISTER_DISP"] != '1') {
            $query .= "     AND VALUE(OUTPUT_FLG, '0') = '0' ";
        }
        if ($directDebit) {
            $query .= "     AND DIRECT_DEBIT = '{$directDebit}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     case ";
            $query .= "         when int(substr(DIRECT_DEBIT, 1, 2)) < 4 then int(substr(DIRECT_DEBIT, 1, 2)) + 12 ";
            $query .= "         else int(substr(DIRECT_DEBIT, 1, 2)) ";
            $query .= "     end DESC, ";
            $query .= "     substr(DIRECT_DEBIT, 3, 2) DESC ";
        }

        return $query;
    }

    //振込日
    function getPaidDate($model) {
        $query  = " SELECT ";
        $query .= "     PPAID.PLAN_PAID_MONEY_DATE || ':' || VALUE(PPAID.SGL_OUTPUT_FLG, '0') AS VALUE, ";
        $query .= "     SUBSTR(PPAID.PLAN_PAID_MONEY_DATE, 6, 2) || '/' || SUBSTR(PPAID.PLAN_PAID_MONEY_DATE, 9) || ";
        $query .= "     case ";
        $query .= "         when PPAID.SGL_OUTPUT_FLG = '1' then '（済）' ";
        $query .= "         else '　　　' ";
        $query .= "     end AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT PPAID ";
        $query .= " WHERE ";
        $query .= "     PPAID.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND PPAID.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["REDISTER_DISP"] != '1') {
            $query .= "     AND VALUE(PPAID.SGL_OUTPUT_FLG, '0') = '0' ";
        }
        $query .= "     AND PPAID.PLAN_PAID_MONEY_DIV   = '{$model->field["CSV_DIV"]}' ";
        $query .= "     AND PPAID.SCHOOLCD || PPAID.SCHOOL_KIND || PPAID.YEAR || PPAID.SLIP_NO NOT IN  ( ";
        $query .= "             SELECT ";
        $query .= "                 PLAN_M.SCHOOLCD || PLAN_M.SCHOOL_KIND || PLAN_M.YEAR || PLAN_M.SLIP_NO ";
        $query .= "             FROM ";
        $query .= "                 COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "                 INNER JOIN COLLECT_M_DETAIL_MST M_DETAIL ON PLAN_M.SCHOOLCD = M_DETAIL.SCHOOLCD ";
        $query .= "                       AND PLAN_M.SCHOOL_KIND = M_DETAIL.SCHOOL_KIND ";
        $query .= "                       AND PLAN_M.YEAR = M_DETAIL.YEAR ";
        $query .= "                       AND PLAN_M.COLLECT_L_CD = M_DETAIL.COLLECT_L_CD ";
        $query .= "                       AND PLAN_M.COLLECT_M_CD = M_DETAIL.COLLECT_M_CD ";
        $query .= "                       AND M_DETAIL.SEQ = '001' ";
        $query .= "                       AND M_DETAIL.REMARK1 = '1' ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     PPAID.PLAN_PAID_MONEY_DATE, ";
        $query .= "     PPAID.SGL_OUTPUT_FLG ";
        $query .= " ORDER BY ";
        $query .= "     PPAID.PLAN_PAID_MONEY_DATE DESC, ";
        $query .= "     VALUE(PPAID.SGL_OUTPUT_FLG, '0') ";

        return $query;
    }

    //対象月コンボ
    function getMonth($model) {
        $query  = "    WITH MAIN AS ( ";
        $query .= "      SELECT DISTINCT ";
        $query .= "        PLAN_MONTH ";
        $query .= "      FROM ";
        $query .= "        COLLECT_SLIP_PLAN_PAID_DAT PPAID ";
        $query .= "      WHERE ";
        $query .= "        PPAID.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "        AND PPAID.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["REDISTER_DISP"] != '1') {
            $query .= "        AND VALUE(PPAID.SGL_OUTPUT_FLG, '0') = '0' ";
        }
        $query .= "        AND PPAID.PLAN_PAID_MONEY_DATE   = '".$model->paidDate."' ";
        $query .= "        AND PPAID.PLAN_PAID_MONEY_DIV = '{$model->field["CSV_DIV"]}' ";
        $query .= "        AND PPAID.SCHOOLCD || PPAID.SCHOOL_KIND || PPAID.YEAR || PPAID.SLIP_NO NOT IN ( ";
        $query .= "          SELECT ";
        $query .= "            PLAN_M.SCHOOLCD || PLAN_M.SCHOOL_KIND || PLAN_M.YEAR || PLAN_M.SLIP_NO ";
        $query .= "          FROM ";
        $query .= "            COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "            INNER JOIN COLLECT_M_DETAIL_MST M_DETAIL ";
        $query .= "              ON PLAN_M.SCHOOLCD = M_DETAIL.SCHOOLCD ";
        $query .= "              AND PLAN_M.SCHOOL_KIND = M_DETAIL.SCHOOL_KIND ";
        $query .= "              AND PLAN_M.YEAR = M_DETAIL.YEAR ";
        $query .= "              AND PLAN_M.COLLECT_L_CD = M_DETAIL.COLLECT_L_CD ";
        $query .= "              AND PLAN_M.COLLECT_M_CD = M_DETAIL.COLLECT_M_CD ";
        $query .= "              AND M_DETAIL.SEQ = '001' ";
        $query .= "              AND M_DETAIL.REMARK1 = '1' ";
        $query .= "        ) ";
        $query .= "    ) ";
        $query .= "    SELECT ";
        $query .= "      PLAN_MONTH || '月' AS LABEL, ";
        $query .= "      PLAN_MONTH AS VALUE";
        $query .= "    FROM ";
        $query .= "      MAIN ";
        $query .= "    ORDER BY ";
        $query .= "      CASE ";
        $query .= "        WHEN PLAN_MONTH < '04' ";
        $query .= "        THEN 2 ";
        $query .= "        ELSE 1 ";
        $query .= "        END, ";
        $query .= "      PLAN_MONTH ";

        return $query;
    }

    //対象科目コンボ
    function getCollectLcd($model) {
        $query  = "    WITH MAIN AS ( ";
        $query .= "      SELECT DISTINCT ";
        $query .= "        PPAID_M.SCHOOLCD, ";
        $query .= "        PPAID_M.SCHOOL_KIND, ";
        $query .= "        PPAID_M.YEAR, ";
        $query .= "        PPAID_M.COLLECT_L_CD ";
        $query .= "      FROM ";
        $query .= "        COLLECT_SLIP_PLAN_PAID_DAT PPAID ";
        $query .= "      LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT PPAID_M ";
        $query .= "         ON PPAID_M.SCHOOLCD     = PPAID.SCHOOLCD ";
        $query .= "        AND PPAID_M.SCHOOL_KIND  = PPAID.SCHOOL_KIND ";
        $query .= "        AND PPAID_M.YEAR         = PPAID.YEAR ";
        $query .= "        AND PPAID_M.SLIP_NO      = PPAID.SLIP_NO ";
        $query .= "        AND PPAID_M.PLAN_YEAR    = PPAID.PLAN_YEAR ";
        $query .= "        AND PPAID_M.PLAN_MONTH   = PPAID.PLAN_MONTH ";
        $query .= "        AND PPAID_M.SEQ          = PPAID.SEQ ";
        $query .= "        AND PPAID_M.SCHREGNO     = PPAID.SCHREGNO ";
        $query .= "      WHERE ";
        $query .= "        PPAID.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "        AND PPAID.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["REDISTER_DISP"] != '1') {
            $query .= "        AND VALUE(PPAID.SGL_OUTPUT_FLG, '0') = '0' ";
        }
        $query .= "        AND PPAID.PLAN_PAID_MONEY_DATE   = '".$model->paidDate."' ";
        $query .= "        AND PPAID.PLAN_PAID_MONEY_DIV = '{$model->field["CSV_DIV"]}' ";
        $query .= "        AND PPAID.SCHOOLCD || PPAID.SCHOOL_KIND || PPAID.YEAR || PPAID.SLIP_NO NOT IN ( ";
        $query .= "          SELECT ";
        $query .= "            PLAN_M.SCHOOLCD || PLAN_M.SCHOOL_KIND || PLAN_M.YEAR || PLAN_M.SLIP_NO ";
        $query .= "          FROM ";
        $query .= "            COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "            INNER JOIN COLLECT_M_DETAIL_MST M_DETAIL ";
        $query .= "              ON PLAN_M.SCHOOLCD = M_DETAIL.SCHOOLCD ";
        $query .= "              AND PLAN_M.SCHOOL_KIND = M_DETAIL.SCHOOL_KIND ";
        $query .= "              AND PLAN_M.YEAR = M_DETAIL.YEAR ";
        $query .= "              AND PLAN_M.COLLECT_L_CD = M_DETAIL.COLLECT_L_CD ";
        $query .= "              AND PLAN_M.COLLECT_M_CD = M_DETAIL.COLLECT_M_CD ";
        $query .= "              AND M_DETAIL.SEQ = '001' ";
        $query .= "              AND M_DETAIL.REMARK1 = '1' ";
        $query .= "        ) ";
        $query .= "    ) ";
        $query .= "    SELECT ";
        $query .= "        T1.COLLECT_L_CD || ':' || LMST.COLLECT_L_NAME  || CASE T1.SCHOOL_KIND WHEN 'J' THEN '(中学)' ELSE '(高校)' END  AS LABEL, ";
        $query .= "        T1.COLLECT_L_CD || '-' || T1.SCHOOL_KIND AS VALUE ";
        $query .= "    FROM ";
        $query .= "      MAIN T1";
        $query .= "      LEFT JOIN COLLECT_L_MST LMST    ON LMST.SCHOOLCD     = T1.SCHOOLCD ";
        $query .= "                                     AND LMST.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "                                     AND LMST.YEAR         = T1.YEAR ";
        $query .= "                                     AND LMST.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "    ORDER BY ";
        $query .= "      T1.COLLECT_L_CD, ";
        $query .= "      T1.SCHOOL_KIND ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //ヘッダー情報取得
    function getCollectSchoolBankMst() {
        $query  = " SELECT ";
        $query .= "     T1.ACCOUNTNAME_KANA, ";
        $query .= "     T1.ACCOUNT_CD, ";
        $query .= "     T1.BANK_CD, ";
        $query .= "     T1.BANKNAME_KANA, ";
        $query .= "     T1.BRANCHCD, ";
        $query .= "     T1.BRANCHNAME_KANA, ";
        $query .= "     case ";
        $query .= "         when T1.DEPOSIT_TYPE = '1' then '普通' ";
        $query .= "         else '当座' ";
        $query .= "     end as DEPOSIT_TYPE, ";
        $query .= "     T1.ACCOUNTNO, ";
        $query .= "     T2.BANKNAME, ";
        $query .= "     T2.BRANCHNAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST T1 ";
        $query .= "     LEFT JOIN BANK_MST T2 ON T2.BANKCD   = T1.BANK_CD ";
        $query .= "                          AND T2.BRANCHCD = T1.BRANCHCD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND T1.SCHOOL_KIND = '99' ";   //共通
        $query .= "     AND T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND T1.FORMAT_DIV  = '1' ";    // 1:引落 2:返金
        $query .= "     AND T1.SEQ         = '001' ";  // 固定

        return $query;
    }

    //UPDATE
    function &getUpdateZenginQuery($db, $model) {
        $outPutFlg = $db->getOne(knjp826query::getDirectDebit($model->field["DIRECT_DEBIT"]));

        if ($outPutFlg != '1') {
            //COLLECT_ZENGIN_HEADER_RECORD_DAT
            $data = array();
            $data["OUTPUT_FLG"][TEXT]   = '1';
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE YEAR         = '".CTRL_YEAR."' ";
            $where .= "   AND DIRECT_DEBIT = '".$model->field["DIRECT_DEBIT"]."' ";

            $query = Query::updateSQL($data, "COLLECT_ZENGIN_HEADER_RECORD_DAT", $where);
            $db->query($query);

            //COLLECT_SGL_OUTPUT_CSV_DAT
            $query  = " DELETE ";
            $query .= " FROM ";
            $query .= "     COLLECT_SGL_OUTPUT_CSV_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND DIRECT_DEBIT = '".$model->field["DIRECT_DEBIT"]."' ";
            $db->query($query);

            $query = knjp826Query::selectQuery($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $customerHead = substr($row["CUSTOMER_CD_HEAD"], 0, 10);
                $customerCd = str_pad($customerHead.$row["CUSTOMER_SCHREG"], 20, 0, STR_PAD_RIGHT);

                $data = array();
                $data["YEAR"][TEXT]                 = CTRL_YEAR;
                $data["DIRECT_DEBIT"][TEXT]         = $model->field["DIRECT_DEBIT"];
                $data["SEQ"][NUMBER]                = $row['SEQ'];
                $data["ACCOUNTNAME"][TEXT]          = $row['ACCOUNTNAME'];
                $data["ACCOUNTNAME_KANJI"][TEXT]    = ''; //null
                $data["SCHREGNO"][TEXT]             = $customerCd;
                $data["PLAN_MONEY"][TEXT]           = $row['PLAN_MONEY'];
                $data["TOTAL_MONEY"][TEXT]          = $row['TOTAL_MONEY'];
                $data["BANKCD"][TEXT]               = $row['BANKCD'];
                $data["BANKNAME"][TEXT]             = $row['BANKNAME'];
                $data["BANKNAME_KANJI"][TEXT]       = ''; //null
                $data["BRANCHCD"][TEXT]             = $row['BRANCHCD'];
                $data["BRANCHNAME"][TEXT]           = $row['BRANCHNAME'];
                $data["BRANCHNAME_KANJI"][TEXT]     = ''; //null
                $data["DEPOSIT_DIV"][TEXT]          = $row['DEPOSIT_DIV'];
                $data["ACCOUNTNO"][TEXT]            = $row['ACCOUNTNO'];
                $data["RESULT_CD"][TEXT]            = $row['RESULT_CD'];
                $data["SUMMARY"][TEXT]              = ''; //null
                $data["TOROKUNO"][TEXT]             = ''; //null
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_SGL_OUTPUT_CSV_DAT");
                $db->query($query);
            }
        }
    }

    //振込日
    function getPaidPlan($model, $updDiv = '') {
        $sglOutputFlg = $updDiv == 'noUpd' ? "1" : "0";

        $query  = " SELECT ";
        $query .= "     PPAID.*, ";
        if ($model->field["COLLECT_L_CD"]) {
            $query .= "     PPAID_M.PLAN_PAID_MONEY  ";
            $query .= "     - VALUE(REDUC_CP.DECISION_MONEY, 0) ";
            $query .= "     - VALUE(REDUC_CP.ADD_DECISION_MONEY, 0) ";
            $query .= "     - VALUE(REDUC_P.DECISION_MONEY, 0) ";
            $query .= "     - VALUE(REDUC_SP.DECISION_MONEY, 0) ";
            $query .= "     - VALUE(REDUC_B.BURDEN_CHARGE, 0) AS PAIDM_MONEY, ";
        }
        $query .= "     VALUE(CUSTOMER.CUSTOMER_NUMBER, '0000000000') AS CUSTOMER_NUMBER_HEAD, ";
        $query .= "     CASE WHEN PPAID.SCHOOL_KIND = 'J' THEN MAPPING.SCHREGNO_J ";
        $query .= "          WHEN PPAID.SCHOOL_KIND = 'H' THEN MAPPING.SCHREGNO_H ";
        $query .= "          ELSE '' ";
        $query .= "     END AS CUSTOMER_SCHREG, ";
        $query .= "     RBANK.BANKCD, ";
        $query .= "     BANK.BANKNAME, ";
        $query .= "     BANK.BANKNAME_KANA, ";
        $query .= "     RBANK.BRANCHCD, ";
        $query .= "     BANK.BRANCHNAME, ";
        $query .= "     BANK.BRANCHNAME_KANA, ";
        $query .= "     CASE ";
        $query .= "         WHEN RBANK.DEPOSIT_ITEM = '1' THEN '普通' ";
        $query .= "         ELSE '当座' ";
        $query .= "     END AS DEPOSIT_TYPE, ";
        $query .= "     RBANK.ACCOUNTNO, ";
        $query .= "     RBANK.ACCOUNTNAME, ";
        $query .= "     RBANK.RELATIONSHIP, ";
        $query .= "     RBANK.PAID_INFO_CD ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT PPAID ";
        if ($model->field["COLLECT_L_CD"]) {
            $query .= "     LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT PPAID_M ";
            $query .= "         ON PPAID_M.SCHOOLCD            = PPAID.SCHOOLCD ";
            $query .= "        AND PPAID_M.SCHOOL_KIND         = PPAID.SCHOOL_KIND ";
            $query .= "        AND PPAID_M.YEAR                = PPAID.YEAR ";
            $query .= "        AND PPAID_M.SLIP_NO             = PPAID.SLIP_NO ";
            $query .= "        AND PPAID_M.PLAN_YEAR           = PPAID.PLAN_YEAR ";
            $query .= "        AND PPAID_M.PLAN_MONTH          = PPAID.PLAN_MONTH ";
            $query .= "        AND PPAID_M.SEQ                 = PPAID.SEQ ";
            $query .= "        AND PPAID_M.SCHREGNO            = PPAID.SCHREGNO ";
            $query .= "     LEFT JOIN COLLECT_M_MST CMST ";
            $query .= "         ON CMST.SCHOOLCD               = PPAID_M.SCHOOLCD ";
            $query .= "        AND CMST.SCHOOL_KIND            = PPAID_M.SCHOOL_KIND ";
            $query .= "        AND CMST.YEAR                   = PPAID_M.YEAR ";
            $query .= "        AND CMST.COLLECT_L_CD           = PPAID_M.COLLECT_L_CD ";
            $query .= "        AND CMST.COLLECT_M_CD           = PPAID_M.COLLECT_M_CD ";
            $query .= "     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_CP ";
            $query .= "         ON REDUC_CP.SCHOOLCD           = PPAID_M.SCHOOLCD ";
            $query .= "        AND REDUC_CP.SCHOOL_KIND        = PPAID_M.SCHOOL_KIND ";
            $query .= "        AND REDUC_CP.YEAR               = PPAID_M.YEAR ";
            $query .= "        AND REDUC_CP.SLIP_NO            = PPAID_M.SLIP_NO ";
            $query .= "        AND REDUC_CP.PLAN_YEAR          = PPAID_M.PLAN_YEAR ";
            $query .= "        AND REDUC_CP.PLAN_MONTH         = PPAID_M.PLAN_MONTH ";
            $query .= "        AND REDUC_CP.SCHREGNO           = PPAID_M.SCHREGNO ";
            $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDUC_P ";
            $query .= "         ON REDUC_P.SCHOOLCD            = PPAID_M.SCHOOLCD ";
            $query .= "        AND REDUC_P.SCHOOL_KIND         = PPAID_M.SCHOOL_KIND ";
            $query .= "        AND REDUC_P.YEAR                = PPAID_M.YEAR ";
            $query .= "        AND REDUC_P.REDUCTION_TARGET    = CMST.GAKUNOKIN_DIV ";
            $query .= "        AND REDUC_P.SLIP_NO             = PPAID_M.SLIP_NO ";
            $query .= "        AND REDUC_P.PLAN_YEAR           = PPAID_M.PLAN_YEAR ";
            $query .= "        AND REDUC_P.PLAN_MONTH          = PPAID_M.PLAN_MONTH ";
            $query .= "        AND REDUC_P.SCHREGNO            = PPAID_M.SCHREGNO ";
            $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT REDUC_SP ";
            $query .= "         ON REDUC_SP.SCHOOLCD           = PPAID_M.SCHOOLCD ";
            $query .= "        AND REDUC_SP.SCHOOL_KIND        = PPAID_M.SCHOOL_KIND ";
            $query .= "        AND REDUC_SP.YEAR               = PPAID_M.YEAR ";
            $query .= "        AND REDUC_SP.REDUCTION_TARGET   = CMST.GAKUNOKIN_DIV ";
            $query .= "        AND REDUC_SP.SLIP_NO            = PPAID_M.SLIP_NO ";
            $query .= "        AND REDUC_SP.PLAN_YEAR          = PPAID_M.PLAN_YEAR ";
            $query .= "        AND REDUC_SP.PLAN_MONTH         = PPAID_M.PLAN_MONTH ";
            $query .= "        AND REDUC_SP.SCHREGNO           = PPAID_M.SCHREGNO ";
            $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT REDUC_B ";
            $query .= "         ON REDUC_B.SCHOOLCD            = PPAID_M.SCHOOLCD ";
            $query .= "        AND REDUC_B.SCHOOL_KIND         = PPAID_M.SCHOOL_KIND ";
            $query .= "        AND REDUC_B.YEAR                = PPAID_M.YEAR ";
            $query .= "        AND REDUC_B.REDUCTION_TARGET   =  CMST.GAKUNOKIN_DIV ";
            $query .= "        AND REDUC_B.SLIP_NO             = PPAID_M.SLIP_NO ";
            $query .= "        AND REDUC_B.PLAN_YEAR           = PPAID_M.PLAN_YEAR ";
            $query .= "        AND REDUC_B.PLAN_MONTH          = PPAID_M.PLAN_MONTH ";
            $query .= "        AND REDUC_B.SCHREGNO            = PPAID_M.SCHREGNO ";
        }
        $query .="      LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT MAPPING ";
        $query .="        ON PPAID.SCHREGNO = MAPPING.SCHREGNO ";
        $query .= "     LEFT JOIN REGISTBANK_DAT RBANK ON PPAID.SCHOOLCD = RBANK.SCHOOLCD ";
        $query .= "          AND PPAID.SCHREGNO = RBANK.SCHREGNO ";
        $query .= "          AND RBANK.SEQ = '1' ";
        $query .= "     LEFT JOIN BANK_MST BANK ON RBANK.BANKCD = BANK.BANKCD ";
        $query .= "          AND RBANK.BRANCHCD = BANK.BRANCHCD ";
        $query .= "     LEFT JOIN COLLECT_SCHREG_CUSTOMER_DAT CUSTOMER ON PPAID.SCHREGNO = CUSTOMER.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     PPAID.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND PPAID.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(PPAID.SGL_OUTPUT_FLG, '0') = '{$sglOutputFlg}' ";
        $query .= "     AND PPAID.PLAN_PAID_MONEY_DATE  = '{$model->field["PAID_DATE"]}' ";
        $query .= "     AND PPAID.PLAN_PAID_MONEY_DIV   = '{$model->field["CSV_DIV"]}' ";
        if ($model->field["PLAN_MONTH"]) {
            $query .= "     AND PPAID.PLAN_MONTH   = '".$model->field["PLAN_MONTH"]."' ";
        }
        if ($model->field["COLLECT_L_CD"]) {
            list($lcd, $schoolKind) = explode("-", $model->field["COLLECT_L_CD"]);
            $query .= "     AND PPAID_M.COLLECT_L_CD   = '".$lcd."' ";
            $query .= "     AND PPAID_M.SCHOOL_KIND    = '".$schoolKind."' ";
        }
        $query .= "     AND PPAID.SCHOOLCD || PPAID.SCHOOL_KIND || PPAID.YEAR || PPAID.SLIP_NO NOT IN  ( ";
        $query .= "             SELECT ";
        $query .= "                 PLAN_M.SCHOOLCD || PLAN_M.SCHOOL_KIND || PLAN_M.YEAR || PLAN_M.SLIP_NO ";
        $query .= "             FROM ";
        $query .= "                 COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "                 INNER JOIN COLLECT_M_DETAIL_MST M_DETAIL ON PLAN_M.SCHOOLCD = M_DETAIL.SCHOOLCD ";
        $query .= "                       AND PLAN_M.SCHOOL_KIND = M_DETAIL.SCHOOL_KIND ";
        $query .= "                       AND PLAN_M.YEAR = M_DETAIL.YEAR ";
        $query .= "                       AND PLAN_M.COLLECT_L_CD = M_DETAIL.COLLECT_L_CD ";
        $query .= "                       AND PLAN_M.COLLECT_M_CD = M_DETAIL.COLLECT_M_CD ";
        $query .= "                       AND M_DETAIL.SEQ = '001' ";
        $query .= "                       AND M_DETAIL.REMARK1 = '1' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     PPAID.SCHOOLCD, ";
        $query .= "     PPAID.SCHOOL_KIND, ";
        $query .= "     PPAID.SLIP_NO, ";
        $query .= "     PPAID.PLAN_YEAR, ";
        $query .= "     PPAID.PLAN_MONTH, ";
        $query .= "     PPAID.SEQ ";

        return $query;
    }

    //UPDATE
    function &getUpdatePaidPlanQuery($db, &$model, $updDiv = '') {
        $query = knjp826query::getPaidPlan($model, $updDiv);
        $result = $db->query($query);
        $model->csvData = array();
        $moneyStr = ($this->field["COLLECT_L_CD"]) ? "PAIDM_MONEY" : "PLAN_PAID_MONEY" ; //※振込 で科目が指定された場合にのみ明細の金額を表示
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $customerHead = substr($row["CUSTOMER_NUMBER_HEAD"], 0, 10);
            $row["CUSTOMER_NUMBER"] = str_pad($customerHead.$row["CUSTOMER_SCHREG"], 20, 0, STR_PAD_RIGHT);

            if (is_array($model->csvData[$row["SCHREGNO"]])) {
                $model->csvData[$row["SCHREGNO"]][$moneyStr] += $row[$moneyStr];
            } else {
                $model->csvData[$row["SCHREGNO"]] = $row;
            }
            if ($updDiv != 'noUpd') {
                $data = array();
                $data["SGL_OUTPUT_FLG"][TEXT]   = '1';
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD = '{$row["SCHOOLCD"]}' ";
                $where .= "     AND SCHOOL_KIND = '{$row["SCHOOL_KIND"]}' ";
                $where .= "     AND YEAR = '{$row["YEAR"]}' ";
                $where .= "     AND SLIP_NO = '{$row["SLIP_NO"]}' ";
                $where .= "     AND PLAN_YEAR = '{$row["PLAN_YEAR"]}' ";
                $where .= "     AND PLAN_MONTH = '{$row["PLAN_MONTH"]}' ";
                $where .= "     AND SEQ = '{$row["SEQ"]}' ";

                $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_PAID_DAT", $where);
                $db->query($query);
            }
        }
        $result->free();
    }

    //登録データ取得
    function selectQuery($model) {
        $query  = " WITH REGD_DATA AS ( ";
        $query .= "    SELECT ";
        $query .= "      YEAR, ";
        $query .= "      MAX(SEMESTER) AS MAX_SEMESTER, ";
        $query .= "      SCHREGNO, ";
        $query .= "      GRADE ";
        $query .= "    FROM ";
        $query .= "      SCHREG_REGD_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "    GROUP BY ";
        $query .= "      YEAR, ";
        $query .= "      SCHREGNO, ";
        $query .= "      GRADE ";
        $query .= "  ) ";

        $query .= " SELECT ";
        $query .= "     ZD.SEQ, ";
        $query .= "     ZD.ACCOUNTNAME, ";
        $query .= "     VALUE(ZD.CUSTOMER_CD, '0000000000') AS CUSTOMER_CD_HEAD, ";
        $query .= "     CASE WHEN REGDG.SCHOOL_KIND = 'J' THEN MAPPING.SCHREGNO_J ";
        $query .= "          WHEN REGDG.SCHOOL_KIND = 'H' THEN MAPPING.SCHREGNO_H ";
        $query .= "          ELSE '' ";
        $query .= "     END AS CUSTOMER_SCHREG, ";
        $query .= "     ZD.PLAN_MONEY, ";
        $query .= "     ZD.PLAN_MONEY as TOTAL_MONEY, ";
        $query .= "     ZD.BANKCD, ";
        $query .= "     ZD.BANKNAME, ";
        $query .= "     ZD.BRANCHCD, ";
        $query .= "     ZD.BRANCHNAME, ";
        $query .= "     case ";
        $query .= "         when ZD.DEPOSIT_DIV = '1' then '普通' ";
        $query .= "         when ZD.DEPOSIT_DIV = '2' then '当座' ";
        $query .= "         else '' ";
        $query .= "     end as DEPOSIT_DIV, ";
        $query .= "     ZD.ACCOUNTNO, ";
        $query .= "     '振替済' as RESULT_CD ";
        $query .= " FROM ";
        $query .= "     COLLECT_ZENGIN_DATA_RECORD_DAT ZD ";
        $query .= "     LEFT JOIN COLLECT_SCHREG_CUSTOMER_DAT CD ON CD.CUSTOMER_NUMBER = ZD.CUSTOMER_CD ";
        $query .= "     LEFT JOIN REGD_DATA REGD ON REGD.SCHREGNO = CD.SCHREGNO ";
        $query .="      LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR  = REGD.YEAR ";
        $query .="                                      AND REGDG.GRADE = REGD.GRADE ";
        $query .="      LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT MAPPING ON MAPPING.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         ZD.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND ZD.DIRECT_DEBIT = '".$model->field["DIRECT_DEBIT"]."' ";
        $query .= "     AND ZD.RESULT_CD    = '0' "; //振替済のみ
        $query .= " ORDER BY ";
        $query .= "     ZD.SEQ ";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     SIGL.SEQ, ";
        $query .= "     SIGL.ACCOUNTNAME, ";
        $query .= "     SIGL.ACCOUNTNAME_KANJI, ";
        $query .= "     SIGL.SCHREGNO, ";
        $query .= "     SIGL.PLAN_MONEY, ";
        $query .= "     SIGL.TOTAL_MONEY, ";
        $query .= "     SIGL.BANKCD, ";
        $query .= "     SIGL.BANKNAME, ";
        $query .= "     SIGL.BANKNAME_KANJI, ";
        $query .= "     SIGL.BRANCHCD, ";
        $query .= "     SIGL.BRANCHNAME, ";
        $query .= "     SIGL.BRANCHNAME_KANJI, ";
        $query .= "     SIGL.DEPOSIT_DIV, ";
        $query .= "     SIGL.ACCOUNTNO, ";
        $query .= "     SIGL.RESULT_CD, ";
        $query .= "     SIGL.SUMMARY, ";
        $query .= "     SIGL.TOROKUNO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_OUTPUT_CSV_DAT SIGL ";
        $query .= " WHERE ";
        $query .= "         SIGL.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND SIGL.DIRECT_DEBIT = '".$model->field["DIRECT_DEBIT"]."' ";
        $query .= " ORDER BY ";
        $query .= "     SIGL.SEQ ";

        return $query;
    }

    // 合計情報取得
    function getTotalInfo($model) {
        $query  = " SELECT ";
        $query .= "     sum(int(PLAN_MONEY)) as YOTEI_TOTAL, ";
        $query .= "     sum(1) as YOTEI_COUNT, ";
        $query .= "     sum(case when RESULT_CD = '0' then int(PLAN_MONEY) else 0 end) as ZUMI_TOTAL, ";
        $query .= "     sum(case when RESULT_CD = '0' then 1 else 0 end) as ZUMI_COUNT, ";
        $query .= "     sum(case when RESULT_CD = '0' then 0 else int(PLAN_MONEY) end) as FUNOU_TOTAL, ";
        $query .= "     sum(case when RESULT_CD = '0' then 0 else 1 end) as FUNOU_COUNT ";
        $query .= " FROM ";
        $query .= "     COLLECT_ZENGIN_DATA_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND DIRECT_DEBIT = '".$model->field["DIRECT_DEBIT"]."' ";

        return $query;
    }

}
?>
