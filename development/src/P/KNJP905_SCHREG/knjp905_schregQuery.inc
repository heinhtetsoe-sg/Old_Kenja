<?php

class knjp905_schregQuery extends Query
{

    //本締めデータチェック
    public function getCloseFlgData($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND CLOSE_FLG   = '1' ";

        return $query;
    }

    //決済の状況を取得(比較時に利用)
    public function getLevyData($model, $flg)
    {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } elseif ($flg == "CANCEL") {
            $query .= "     T1.OUTGO_CANCEL ";
        } elseif ($flg == "COLLECT") {
            $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD || T1.COLLECT_S_CD || T1.COLLECT_GRP_CD AS COLLECT_L_M_S_GRP_CD ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        return $query;
    }

    //一覧取得
    public function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.OUTGO_L_CD || T1.OUTGO_M_CD || T1.OUTGO_S_CD AS OUTGO_L_M_S_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND T1.LINE_NO     = ".$model->getLineNo." ";
        $query .= "     AND T1.OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //返金、給付対象、取得
    public function getLevySmstInfo($model, $lmsCd)
    {
        $query  = " SELECT ";
        $query .= "     T1.REPAY_DIV, ";
        $query .= "     T1.BENEFIT ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD = '{$lmsCd}' ";

        return $query;
    }

    //支出科目
    public function getLevyMDiv($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getOutgoLMcd}' ";

        return $query;
    }

    //支出細目
    public function getLevySDiv($model, $maxFlg="")
    {
        $query  = "";
        $query .= " SELECT ";
        if ($maxFlg != "") {
            $query .= "     max(LEVY_S_CD) ";
        } else {
            $query .= "     T1.LEVY_S_NAME AS NAME, ";
            $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD || ':' || T1.LEVY_S_NAME AS LABEL, ";
            $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= "     INNER JOIN LEVY_M_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= "                             AND L1.LEVY_M_CD   = T1.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getOutgoLMcd}' ";
        if ($maxFlg == "") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //予算細目取得
    public function getBudgetSDiv($model)
    {
        $query  = " WITH YOBIHI (LEVY_L_M_CD, LEVY_S_CD, LEVY_S_NAME) AS ( ";
        $query .= "     VALUES('{$model->getOutgoLMcd}', 'AAA', '予備費') ";
        $query .= " ) ";
        $query .="    SELECT ";
        $query .="      LEVY_S.LEVY_L_CD || LEVY_S.LEVY_M_CD || LEVY_S.LEVY_S_CD || ':' || LEVY_S.LEVY_S_NAME AS LABEL, ";
        $query .="      LEVY_S.LEVY_L_CD || LEVY_S.LEVY_M_CD || LEVY_S.LEVY_S_CD AS VALUE ";
        $query .="    FROM ";
        $query .="      LEVY_S_MST LEVY_S ";
        $query .="    WHERE ";
        $query .="          LEVY_S.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .="      AND LEVY_S.SCHOOL_KIND = '".$model->getSchoolKind."' ";
        $query .="      AND LEVY_S.YEAR = '{$model->year}' ";
        $query .="      AND LEVY_S.LEVY_L_CD || LEVY_S.LEVY_M_CD = '{$model->getOutgoLMcd}' ";
        $query .="      AND EXISTS ( ";
        $query .="        SELECT ";
        $query .="          'X' ";
        $query .="        FROM ";
        $query .="          LEVY_BUDGET_DAT BUDGET ";
        $query .="        WHERE ";
        $query .="          BUDGET.SCHOOLCD = LEVY_S.SCHOOLCD ";
        $query .="          AND BUDGET.SCHOOL_KIND = LEVY_S.SCHOOL_KIND ";
        $query .="          AND BUDGET.YEAR = LEVY_S.YEAR ";
        $query .="          AND BUDGET.BUDGET_L_CD = LEVY_S.LEVY_L_CD ";
        $query .="          AND BUDGET.BUDGET_M_CD = LEVY_S.LEVY_M_CD ";
        $query .="          AND BUDGET.BUDGET_S_CD = LEVY_S.LEVY_S_CD ";
        $query .="      ) ";
        $query  .= "  UNION ";
        $query  .= "  SELECT ";
        $query  .= "    YOBIHI.LEVY_L_M_CD || YOBIHI.LEVY_S_CD || ':' || YOBIHI.LEVY_S_NAME AS LABEL, ";
        $query  .= "    YOBIHI.LEVY_L_M_CD || YOBIHI.LEVY_S_CD AS VALUE ";
        $query  .= "  FROM YOBIHI ";

        return $query;
    }

    //予算情報取得
    public function getBudgetInfo($model)
    {
        $query  ="    WITH OUTGO_T AS ( ";
        $query .="      SELECT ";
        $query .="         SCHOOLCD, ";
        $query .="         SCHOOL_KIND, ";
        $query .="         YEAR, ";
        $query .="         OUTGO_L_CD, ";
        $query .="         OUTGO_M_CD, ";
        $query .="         OUTGO_S_CD, ";
        $query .="         SUM(SCH_PRICE) AS OUTGO_MONEY ";
        $query .="      FROM ";
        $query .="        LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .="      GROUP BY ";
        $query .="        SCHOOLCD, ";
        $query .="        SCHOOL_KIND, ";
        $query .="        YEAR, ";
        $query .="        OUTGO_L_CD, ";
        $query .="        OUTGO_M_CD, ";
        $query .="        OUTGO_S_CD ";
        $query .="    ) ";
        $query .="    SELECT ";
        $query .="      VALUE(BUDGET.BUDGET_MONEY,0) AS BUDGET_TOTAL, ";
        $query .="      VALUE(BUDGET.BUDGET_MONEY,0) - VALUE(OUTGO_T.OUTGO_MONEY,0) AS BUDGET_ZAN ";
        $query .="    FROM ";
        $query .="      LEVY_BUDGET_DAT BUDGET ";
        $query .="      INNER JOIN OUTGO_T ";
        $query .="         ON BUDGET.SCHOOLCD = OUTGO_T.SCHOOLCD ";
        $query .="        AND BUDGET.SCHOOL_KIND = OUTGO_T.SCHOOL_KIND ";
        $query .="        AND BUDGET.YEAR = OUTGO_T.YEAR ";
        $query .="        AND BUDGET.BUDGET_L_CD = OUTGO_T.OUTGO_L_CD ";
        $query .="        AND BUDGET.BUDGET_M_CD = OUTGO_T.OUTGO_M_CD ";
        $query .="        AND BUDGET.BUDGET_S_CD = OUTGO_T.OUTGO_S_CD ";
        $query .="    WHERE ";
        $query .="          BUDGET.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .="      AND BUDGET.SCHOOL_KIND = '".$model->getSchoolKind."' ";
        $query .="      AND BUDGET.YEAR = '{$model->year}' ";
        $query .="      AND BUDGET.BUDGET_L_CD || BUDGET.BUDGET_M_CD || BUDGET.BUDGET_S_CD = '{$model->field["BUDGET_L_M_S_CD"]}' ";

        return $query;
    }

    //予算明細情報取得（支出に対応する予算CDを取得する）
    public function getBudgetMeisaiInfo($model)
    {
        $query  ="    SELECT ";
        $query .="      BUDGET_L_CD || BUDGET_M_CD || BUDGET_S_CD AS DEFAULT_S_CD ";
        $query .="    FROM ";
        $query .="      LEVY_BUDGET_MEISAI_DAT ";
        $query .="    WHERE ";
        $query .="      SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .="      AND SCHOOL_KIND = '".$model->getSchoolKind."' ";
        $query .="      AND YEAR        = '{$model->year}' ";
        $query .="      AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .="      AND LINE_NO     = '{$model->getLineNo}' ";
        $query .="      AND OUTGO_L_CD || OUTGO_M_CD || OUTGO_S_CD  = '{$model->field["OUTGO_L_M_S_CD"]}' ";

        return $query;
    }

    //予算明細存在チェック
    public function getExistsBudgetMeisai($model)
    {
        $query  ="    SELECT ";
        $query .="      COUNT(*) ";
        $query .="    FROM ";
        $query .="      LEVY_BUDGET_MEISAI_DAT ";
        $query .="    WHERE ";
        $query .="      SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .="      AND SCHOOL_KIND = '".$model->getSchoolKind."' ";
        $query .="      AND YEAR        = '{$model->year}' ";
        $query .="      AND BUDGET_L_CD || BUDGET_M_CD || BUDGET_S_CD = '{$model->field["BUDGET_L_M_S_CD"]}' ";
        $query .="      AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .="      AND LINE_NO     = '{$model->getLineNo}' ";
        $query .="      AND OUTGO_L_CD || OUTGO_M_CD || OUTGO_S_CD    = '{$model->field["OUTGO_L_M_S_CD"]}' ";

        return $query;
    }

    //請求月を取得
    public function getSeikyuMonth()
    {
        $query  = " SELECT ";
        for ($month = 1; $month < 13; $month++) {
            if ($month != 1) {
                $query .= " UNION ";
                $query .= " SELECT ";
            }
            $query .= "     $month AS ORDER_MONTH, ";
            $query .= "     '{$month}' AS VALUE, ";
            $query .= "     '{$month}' || '月分' AS LABEL ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     ORDER_MONTH ";

        return $query;
    }

    //生徒取得
    public function getSchno($model, $flg="")
    {
        //収入額 (生徒ごと)
        $query  = " WITH SCHREG_SELECT_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_OUTGO_SCHREG_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "         AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "         AND T1.YEAR        = '{$model->year}' ";
        $query .= "         AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "         AND T1.LINE_NO     = ".$model->getLineNo." ";
        $query .= "         AND T1.OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= "         AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= " ), INCOME_DAT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         COLLECT_L_CD, ";
        $query .= "         COLLECT_M_CD ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_INCOME_DAT ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "         AND SCHOOL_KIND = '{$model->getSchoolKind}'";
        $query .= "         AND YEAR        = '{$model->year}' ";
        $query .= "         AND INCOME_L_CD || INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= " ), PLAN_M_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SLIP_M.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_M_DAT SLIP_M ";
        $query .= "         LEFT JOIN COLLECT_SLIP_DAT SLIP_D ON SLIP_D.SCHOOLCD    = SLIP_M.SCHOOLCD ";
        $query .= "                                          AND SLIP_D.SCHOOL_KIND = SLIP_M.SCHOOL_KIND ";
        $query .= "                                          AND SLIP_D.YEAR        = SLIP_M.YEAR ";
        $query .= "                                          AND SLIP_D.SLIP_NO     = SLIP_M.SLIP_NO ";
        $query .= "         INNER JOIN INCOME_DAT INCM_D ON INCM_D.COLLECT_L_CD = SLIP_M.COLLECT_L_CD ";
        $query .= "                                     AND INCM_D.COLLECT_M_CD = SLIP_M.COLLECT_M_CD ";
        $query .= "     WHERE ";
        $query .= "             SLIP_M.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "         AND SLIP_M.SCHOOL_KIND  = '".$model->getSchoolKind."'";
        $query .= "         AND SLIP_M.YEAR         = '{$model->year}' ";
        $query .= "         AND SLIP_D.CANCEL_DATE  is null ";
        $query .= "     GROUP BY ";
        $query .= "         SLIP_M.SCHREGNO ";
        $query .= " ) ";
        //メイン
        $query .= " SELECT  ";
        $query .= "     SCH_RH.HR_NAME || T1.ATTENDNO || '番' || '　' || T2.NAME AS LABEL1,  ";
        $query .= "     T1.SCHREGNO || '　' || T2.NAME AS LABEL2,  ";
        $query .= "     T1.SCHREGNO AS VALUE,  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "     SCH_RH.HR_NAME || T1.ATTENDNO || '番' || '　' || T2.NAME AS LABEL  ";
        } else {
            $query .= "     T1.SCHREGNO || '　' || T2.NAME AS LABEL  ";
        }
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON T1.YEAR = SCH_RH.YEAR ";
        $query .= "                                      AND T1.SEMESTER = SCH_RH.SEMESTER ";
        $query .= "                                      AND T1.GRADE = SCH_RH.GRADE ";
        $query .= "                                      AND T1.HR_CLASS = SCH_RH.HR_CLASS ";
        $query .= "    ,SCHREG_BASE_MST T2 ";
        //選択した伝票番号のLINE_NOの生徒データのみを取得
        if ($flg) {
            $query .= "     INNER JOIN SCHREG_SELECT_DATA L2 ON L2.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "     INNER JOIN PLAN_M_DAT P1 ON P1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$model->year}'  ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO  ";
        //選択した伝票番号のLINE_NOの生徒データを除く(右側)
        if ($flg == "") {
            $query .= " AND T2.SCHREGNO NOT IN (SELECT ";
            $query .= "                             SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_SELECT_DATA ";
            $query .= "                         ) ";
        }
        $query .= " ORDER BY  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "   T1.GRADE || T1.HR_CLASS || T1.ATTENDNO ";
        } else {
            $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(T1.SCHREGNO, 5)  ";
        }

        return $query;
    }

    //異動者チェック用関数
    public function checkTransferStd($model, $schregno)
    {
        $query   = "  SELECT ";
        $query  .= "    COUNT(*) ";
        $query  .= "  FROM ";
        $query  .= "    SCHREG_BASE_MST ";
        $query  .= "  WHERE ";
        $query  .= "  SCHREGNO = '".$schregno."' ";
        $query  .= "  AND GRD_DATE < '".$model->getRequestDate."' ";
        $query  .= "  AND GRD_DIV IN ('2','3','6') ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //消費税取得
    public function getTaxMst($model)
    {
        $query   = " SELECT ";
        $query  .= "    TAX_VALUE ";
        $query  .= " FROM ";
        $query  .= "    LEVY_TAX_MST ";
        $query  .= " WHERE ";
        $query  .= "        YEAR   = '{$model->year}' ";
        $query  .= "    AND TAX_CD = '002' "; //固定
        $query  .= "    AND '{$model->getRequestDate}' between DATE_FROM and value(DATE_TO, '9999-12-31') ";
        $query  .= " ORDER BY ";
        $query  .= "    DATE_FROM desc ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($db, $model)
    {
        $db->autoCommit(false);

        //対象生徒
        $schregSelectData = array();
        $schregSelectData = explode(",", $model->selectStudent);

        $outgolcd   = substr($model->getOutgoLMcd, 0, 2);
        $outgomcd   = substr($model->getOutgoLMcd, 2, 2);
        $outgoscd   = substr($model->field["OUTGO_L_M_S_CD"], 4, 3);

        //消費税取得
        $query = knjp905_schregQuery::getTaxMst($model);
        $taxVal = $db->getOne($query);
        $taxVal1 = "0.".sprintf("%02d", $taxVal);
        $taxVal2 = "1.".sprintf("%02d", $taxVal);

        $tax   = ($model->field["IN_TAX"] == "1") ? 0: $taxVal1;
        $inTax = ($model->field["IN_TAX"] == "1") ? 1: $taxVal2;
        $totalPriceNoTax = $model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"];
        $totalPriceInTax = floor($totalPriceNoTax * $inTax);

        // 明細データをセット(Delete Insert)
        knjp905_schregQuery::getUpdateOutGoMeisaiDat($db, $model, get_count($schregSelectData), $outgolcd, $outgomcd, $outgoscd, $tax, $inTax, $totalPriceNoTax, $totalPriceInTax);

        if ($model->Properties["LevyBudget"] == "1") {
            //予算明細データをセット (Delete Insert) ※チェックボックスonならDELETEのみ
            $budgetlcd = substr($model->field["BUDGET_L_M_S_CD"], 0, 2);
            $budgetmcd = substr($model->field["BUDGET_L_M_S_CD"], 2, 2);
            $budgetscd = substr($model->field["BUDGET_L_M_S_CD"], 4, 3);

            knjp905_schregQuery::getUpdateBudgetMeisaiDat($db, $model, $budgetlcd, $budgetmcd, $budgetscd, $outgolcd, $outgomcd, $outgoscd);
        }

        // 支出生徒割り振りデータをセット(Delete Insert)
        knjp905_schregQuery::getDeleteOutGoSchregDat($db, $model);
        foreach ($schregSelectData as $schregNo) {
            knjp905_schregQuery::getUpdateOutGoSchregDat($db, $model, $schregNo, get_count($schregSelectData), $outgolcd, $outgomcd, $outgoscd, $totalPriceInTax);
        }

        // LEVY_S_MST
        knjp905_schregQuery::getUpdateLevyRequestOutGoDat($db, $model, $outgolcd, $outgomcd, $outgoscd);

        // LEVY_REQUEST_OUTGO_DAT
        knjp905_schregQuery::getUpdateOutGoDat($db, $model, $outgolcd, $outgomcd);

        $db->commit();
        return;
    }

    //更新 --LEVY_BUDGET_MEISAI_DAT--
    public function &getUpdateBudgetMeisaiDat($db, $model, $budgetlcd, $budgetmcd, $budgetscd, $outgolcd, $outgomcd, $outgoscd)
    {
        $query  ="    DELETE FROM ";
        $query .="      LEVY_BUDGET_MEISAI_DAT ";
        $query .="    WHERE ";
        $query .="          SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .="      AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .="      AND YEAR        = '{$model->year}' ";
        $query .="      AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .="      AND LINE_NO     = {$model->getLineNo} ";
        $query .="      AND BUDGET_L_CD = '{$budgetlcd}' ";
        $query .="      AND BUDGET_M_CD = '{$budgetmcd}' ";
//        $query .="      AND BUDGET_S_CD = '{$budgetscd}' ";
        $query .="      AND OUTGO_L_CD  = '{$outgolcd}' ";
        $query .="      AND OUTGO_M_CD  = '{$outgomcd}' ";
        $query .="      AND OUTGO_S_CD  = '{$outgoscd}' ";
        $db->query($query);

        if ($model->field["NOT_MINUS_FLG"] != "1") {
            $data = array();
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
            $data["YEAR"][TEXT]             = $model->year;
            $data["BUDGET_L_CD"][TEXT]      = $budgetlcd;
            $data["BUDGET_M_CD"][TEXT]      = $budgetmcd;
            $data["BUDGET_S_CD"][TEXT]      = $budgetscd;
            ;
            $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
            $data["LINE_NO"][NUMBER]        = $model->getLineNo;
            $data["OUTGO_L_CD"][TEXT]       = $outgolcd;
            $data["OUTGO_M_CD"][TEXT]       = $outgomcd;
            $data["OUTGO_S_CD"][TEXT]       = $outgoscd;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $query = Query::insertSQL($data, "LEVY_BUDGET_MEISAI_DAT");

            $db->query($query);
        }

        return;
    }

    //更新 --LEVY_REQUEST_OUTGO_MEISAI_DAT--
    public function &getUpdateOutGoMeisaiDat($db, $model, $schCnt, $outgolcd, $outgomcd, $outgoscd, $tax, $inTax, $totalPriceNoTax, $totalPriceInTax)
    {
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= " AND LINE_NO     =  {$model->getLineNo} ";
        $query .= " AND OUTGO_L_CD  = '{$outgolcd}' ";
        $query .= " AND OUTGO_M_CD  = '{$outgomcd}' ";
        $db->query($query);

        $data = array();
        $data["SCHOOLCD"][TEXT]                 = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]              = $model->getSchoolKind;
        $data["YEAR"][TEXT]                     = $model->year;
        $data["REQUEST_NO"][TEXT]               = $model->getRequestNo;
        $data["LINE_NO"][NUMBER]                = $model->getLineNo;
        $data["OUTGO_L_CD"][TEXT]               = $outgolcd;
        $data["OUTGO_M_CD"][TEXT]               = $outgomcd;
        $data["OUTGO_S_CD"][TEXT]               = $outgoscd;
        ;
        $data["COMMODITY_PRICE"][NUMBER]        = $model->field["COMMODITY_PRICE"];
        $data["COMMODITY_CNT"][NUMBER]          = $model->field["COMMODITY_CNT"]; //支出総額
        $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = $totalPriceNoTax;//合計額（税抜き）
        //生徒
        if ($model->field["WARIHURI_DIV"] == "1") {
            if (0 < $schCnt) {
                $data["SCH_CNT"][NUMBER]                = $schCnt;
                $data["TOTAL_TAX"][NUMBER]              = floor($totalPriceNoTax * $tax);
                $data["TOTAL_PRICE"][NUMBER]            = $totalPriceInTax;
                $data["SCH_PRICE"][NUMBER]              = ceil($totalPriceInTax / $schCnt);
                $data["HASUU"][NUMBER]                  = ceil($totalPriceInTax / $schCnt) * $schCnt - $totalPriceInTax;
            } else {
                $data["SCH_CNT"][NUMBER]                = 0;
                $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = 0;
                $data["TOTAL_TAX"][NUMBER]              = 0;
                $data["TOTAL_PRICE"][NUMBER]            = 0;
                $data["SCH_PRICE"][NUMBER]              = 0;
                $data["HASUU"][NUMBER]                  = 0;
            }
        } else {
            $data["TOTAL_TAX"][NUMBER]              = floor($totalPriceNoTax * $tax);
            $data["TOTAL_PRICE"][NUMBER]            = $totalPriceInTax;
            $data["TRADER_SEIKYU_NO"][TEXT]         = $model->field["TRADER_SEIKYU_NO"];
            $data["SEIKYU_MONTH"][TEXT]             = $model->field["SEIKYU_MONTH"];
        }
        $data["WARIHURI_DIV"][TEXT]         = $model->field["WARIHURI_DIV"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_MEISAI_DAT");
        $db->query($query);

        return;
    }

    //削除 --LEVY_REQUEST_OUTGO_MEISAI_DAT--
    public function &getDeleteOutGoSchregDat($db, $model)
    {
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->getSchoolKind}'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND T1.LINE_NO     =  {$model->getLineNo} ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $db->query($query);

        return;
    }

    // 更新 --LEVY_REQUEST_OUTGO_SCHREG_DAT--
    public function &getUpdateOutGoSchregDat($db, $model, $schregNo, $schCnt, $outgolcd, $outgomcd, $outgoscd, $totalPriceInTax)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["LINE_NO"][NUMBER]        = $model->getLineNo;
        $data["OUTGO_L_CD"][TEXT]       = $outgolcd;
        $data["OUTGO_M_CD"][TEXT]       = $outgomcd;
        $data["OUTGO_S_CD"][TEXT]       = $outgoscd;
        $data["OUTGO_MONEY"][NUMBER]    = ceil($totalPriceInTax / $schCnt);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_SCHREG_DAT");
        $db->query($query);

        return;
    }

    //更新 --LEVY_REQUEST_OUTGO_DAT--
    public function &getUpdateOutGoDat($db, $model, $outgolcd, $outgomcd)
    {
        $outGoCnt = $db->getOne(knjp905_schregQuery::getCntOutGoDat($model, $outgolcd, $outgomcd));

        //支出額を取得
        $reqesutGk = $db->getOne(knjp905_schregQuery::getReqesutGk($model, $outgolcd, $outgomcd));

        $incomeLcd = substr($model->getIncomeLMcd, 0, 2);
        $incomeMcd = substr($model->getIncomeLMcd, 2, 2);

        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["OUTGO_L_CD"][TEXT]       = $outgolcd;
        $data["OUTGO_M_CD"][TEXT]       = $outgomcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["REQUEST_GK"][NUMBER]     = $reqesutGk;
        $data["INCOME_L_CD"][TEXT]      = $incomeLcd;
        $data["INCOME_M_CD"][TEXT]      = $incomeMcd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($outGoCnt == 0) {
            $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "       SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "   AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
            $where .= "   AND YEAR        = '{$model->year}' ";
            $where .= "   AND OUTGO_L_CD  = '{$outgolcd}' ";
            $where .= "   AND OUTGO_M_CD  = '{$outgomcd}' ";
            $where .= "   AND REQUEST_NO  = '{$model->getRequestNo}' ";

            $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
            $db->query($query);
        }
        return;
    }

    // 更新 --LEVY_S_MST--
    public function &getUpdateLevyRequestOutGoDat($db, $model, $outgolcd, $outgomcd, $outgoscd)
    {
        $data = array();
        $data["REPAY_DIV"][TEXT]         = $model->field["REPAY_DIV"];
        if ($model->Properties["useBenefit"] == "1") {
            $data["BENEFIT"][TEXT]           = $model->field["BENEFIT"];
        }
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        if ($model->maxScd == $outgoscd) {
            list($lmsCd, $sName) = explode(":", $model->field["OUTGO_L_M_S_CD"]);
            $data["SCHOOLCD"][TEXT]          = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]       = $model->getSchoolKind;
            $data["YEAR"][TEXT]              = $model->year;
            $data["LEVY_L_CD"][TEXT]         = $outgolcd;
            $data["LEVY_M_CD"][TEXT]         = $outgomcd;
            $data["LEVY_S_CD"][TEXT]         = $outgoscd;
            $data["LEVY_S_NAME"][TEXT]       = $sName;

            $query = Query::insertSQL($data, "LEVY_S_MST");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "       SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "   AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
            $where .= "   AND YEAR        = '{$model->year}' ";
            $where .= "   AND LEVY_L_CD   = '{$outgolcd}' ";
            $where .= "   AND LEVY_M_CD   = '{$outgomcd}' ";
            $where .= "   AND LEVY_S_CD   = '{$outgoscd}' ";

            $query = Query::updateSQL($data, "LEVY_S_MST", $where);
            $db->query($query);
        }

        return;
    }

    //LEVY_REQUEST_OUTGO_DATカウント
    public function getCntOutGoDat($model, $lCd, $mCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND OUTGO_L_CD  = '{$lCd}' ";
        $query .= "     AND OUTGO_M_CD  = '{$mCd}' ";
        $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";

        return $query;
    }

    //LEVY_REQUEST_OUTGO_MEISAI_DATカウント
    public function getCntOutGoMeisaiDat($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND LINE_NO     = '{$model->getLineNo}' ";
        $query .= "     AND OUTGO_L_CD || OUTGO_M_CD || OUTGO_S_CD = '{$model->field["OUTGO_L_M_S_CD"]}' ";

        return $query;
    }

    //支出額総計を取得
    public function getReqesutGk($model, $lCd, $mCd)
    {
        $query  = " SELECT ";
        $query .= "     sum(TOTAL_PRICE) ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND OUTGO_L_CD  = '{$lCd}' ";
        $query .= "     AND OUTGO_M_CD  = '{$mCd}' ";

        return $query;
    }

    //DELETE
    public function &getDeleteQuery($db, $model)
    {
        $db->autoCommit(false);

        $outgolcd = substr($model->getOutgoLMcd, 0, 2);
        $outgomcd = substr($model->getOutgoLMcd, 2, 2);

        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= " AND LINE_NO     =  {$model->getLineNo} ";
        $db->query($query);

        //SCHREG
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= " AND LINE_NO     =  {$model->getLineNo} ";
        $query .= " AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
        $db->query($query);

        //LEVY_REQUEST_OUTGO_DAT
        knjp905_schregQuery::getUpdateOutGoDat($db, $model, $outgolcd, $outgomcd);

        $db->commit();
        return;
    }
}
?>

