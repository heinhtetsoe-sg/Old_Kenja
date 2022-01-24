<?php

require_once('for_php7.php');
class knjp905_mainQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
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

    //精算票の支払額、残額を取得
    function getSeisanGk($model, $flg="") {
        $query  = " SELECT ";
        if ($flg == "SIHARAI_GK") {
            $query .= "     SIHARAI_GK ";
        } else {
            $query .= "     ZAN_GK ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_SEISAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND SEISAN_L_CD || SEISAN_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= " AND REQUEST_NO  = '{$model->getSeisanRequestNo}' ";

        return $query;
    }

    //一覧取得
    function getRow($model) {

        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     L$i.OUTGO_L_CD || L$i.OUTGO_M_CD || L$i.OUTGO_S_CD AS OUTGO_L_M_S_CD$i, ";
            $query .= "     L$i.OUTGO_L_CD || L$i.OUTGO_M_CD || L$i.OUTGO_S_CD || ':' || S$i.LEVY_S_NAME AS LEVY_S_NAME$i, ";
            $query .= "     L$i.COMMODITY_PRICE AS COMMODITY_PRICE$i, ";
            $query .= "     L$i.COMMODITY_CNT AS COMMODITY_CNT$i, ";
            $query .= "     L$i.TOTAL_TAX AS TOTAL_TAX$i, ";
            $query .= "     L$i.TOTAL_PRICE AS TOTAL_PRICE$i, ";
            $query .= "     L$i.REMARK AS REMARK$i, ";
        }
        $query .= "     T1.INCOME_L_CD || T1.INCOME_M_CD AS INCOME_LM_CD, ";
        $query .= "     T1.OUTGO_L_CD || T1.OUTGO_M_CD AS OUTGO_L_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L$i ON L$i.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= "                                                AND L$i.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                                AND L$i.YEAR        = T1.YEAR ";
            $query .= "                                                AND L$i.REQUEST_NO  = T1.REQUEST_NO ";
            $query .= "                                                AND L$i.LINE_NO     = {$i} ";
            $query .= "                                                AND L$i.OUTGO_L_CD  = T1.OUTGO_L_CD ";
            $query .= "                                                AND L$i.OUTGO_M_CD  = T1.OUTGO_M_CD ";
            $query .= "                                                AND L$i.OUTGO_L_CD  = T1.OUTGO_L_CD ";
            $query .= "                                                AND L$i.OUTGO_M_CD  = T1.OUTGO_M_CD ";
            $query .= "     LEFT JOIN LEVY_S_MST S$i ON S$i.SCHOOLCD    = L$i.SCHOOLCD ";
            $query .= "                             AND S$i.SCHOOL_KIND = L$i.SCHOOL_KIND ";
            $query .= "                             AND S$i.YEAR        = L$i.YEAR ";
            $query .= "                             AND S$i.LEVY_L_CD   = L$i.OUTGO_L_CD ";
            $query .= "                             AND S$i.LEVY_M_CD   = L$i.OUTGO_M_CD ";
            $query .= "                             AND S$i.LEVY_S_CD   = L$i.OUTGO_S_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.OUTGO_L_CD, ";
        $query .= "     T1.OUTGO_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //getRowのSQLが長すぎてエラーになったのでgetRowから予実管理系を分離
    function getRow2($model, $lineno, $lmscd) {
        $query  = " SELECT ";
        $query .= "     BUDGET_M.BUDGET_L_CD || BUDGET_M.BUDGET_M_CD || BUDGET_M.BUDGET_S_CD AS BUDGET_L_M_S_CD, ";
        $query .= "     CASE WHEN BUDGET_M.BUDGET_S_CD = 'AAA' THEN '予備費' ";
        $query .= "     ELSE BUDGET_M.BUDGET_L_CD || BUDGET_M.BUDGET_M_CD || BUDGET_M.BUDGET_S_CD || ':' || S2.LEVY_S_NAME END AS BUDGET_S_NAME, ";
        $query .= "     BUDGET.BUDGET_MONEY AS BUDGET_MONEY ";
        $query .="  FROM ";
        $query .="      LEVY_BUDGET_MEISAI_DAT BUDGET_M ";
        $query .="      LEFT JOIN LEVY_BUDGET_DAT BUDGET ON BUDGET.SCHOOLCD      = BUDGET_M.SCHOOLCD ";
        $query .="                                      AND BUDGET.SCHOOL_KIND   = BUDGET_M.SCHOOL_KIND";
        $query .="                                      AND BUDGET.YEAR          = BUDGET_M.YEAR";
        $query .="                                      AND BUDGET.BUDGET_L_CD   = BUDGET_M.BUDGET_L_CD ";
        $query .="                                      AND BUDGET.BUDGET_M_CD   = BUDGET_M.BUDGET_M_CD ";
        $query .="                                      AND BUDGET.BUDGET_S_CD   = BUDGET_M.BUDGET_S_CD ";
        $query .= "     LEFT JOIN LEVY_S_MST S2 ON S2.SCHOOLCD   = BUDGET_M.SCHOOLCD ";
        $query .= "                             AND S2.SCHOOL_KIND = BUDGET_M.SCHOOL_KIND ";
        $query .= "                             AND S2.YEAR        = BUDGET_M.YEAR ";
        $query .= "                             AND S2.LEVY_L_CD   = BUDGET_M.BUDGET_L_CD ";
        $query .= "                             AND S2.LEVY_M_CD   = BUDGET_M.BUDGET_M_CD ";
        $query .= "                             AND S2.LEVY_S_CD   = BUDGET_M.BUDGET_S_CD ";
        $query .= " WHERE ";
        $query .= "         BUDGET_M.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND BUDGET_M.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND BUDGET_M.YEAR        = '{$model->year}' ";
        $query .= "     AND BUDGET_M.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND BUDGET_M.LINE_NO     = '{$lineno}' ";
        $query .= "     AND BUDGET_M.OUTGO_L_CD || BUDGET_M.OUTGO_M_CD || BUDGET_M.OUTGO_S_CD = '{$lmscd}' ";
        $query .= " ORDER BY ";
        $query .= "     BUDGET_M.YEAR, ";
        $query .= "     BUDGET_M.OUTGO_L_CD, ";
        $query .= "     BUDGET_M.OUTGO_M_CD, ";
        $query .= "     BUDGET_M.REQUEST_NO ";

        return $query;
    }

    //入金科目、決済の状況を取得(比較時に利用)
    function getLevyData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "INCOME_LM_CD") {
            $query .= "     T1.INCOME_L_CD || T1.INCOME_M_CD ";
        } else if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } else if ($flg == "CANCEL") {
            $query .= "     T1.OUTGO_CANCEL ";
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

    //収入の計算
    function getIncomeSumRequestGk($model, $incomeLMcd, $date = "") {
        $lcd = substr($incomeLMcd, 0, 2);
        $mcd = substr($incomeLMcd, 2, 2);

        $query  = " SELECT ";
        $query .= "     VALUE(SUM(REQUEST_GK), 0) AS SUM_REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND INCOME_L_CD = '{$lcd}' ";
        $query .= " AND INCOME_M_CD = '{$mcd}' ";
        $query .= " AND INCOME_APPROVAL = '1' ";
        $query .= " AND INCOME_CANCEL IS NULL ";
        if ($date) {
            $setDate = str_replace("/", "-" ,$date);
            $query .= " AND REQUEST_DATE <= '{$setDate}' ";
        }
        return $query;
    }

    //支出の計算高(入力中の伝票は直接支出額から計算するため、対象からは除く)
    function getOutGoSumRequestGk($model, $Row, $date = "") {
        $lcd = substr($Row["INCOME_LM_CD"], 0, 2);
        $mcd = substr($Row["INCOME_LM_CD"], 2, 2);

        $query  = " SELECT ";
        $query .= "     VALUE(SUM(REQUEST_GK), 0) AS SUM_REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND INCOME_L_CD = '{$lcd}' ";
        $query .= " AND INCOME_M_CD = '{$mcd}' ";
        if ($model->getRequestNo) {
            $query .= " AND REQUEST_NO <> '{$model->getRequestNo}' ";
        }
        $query .= " AND OUTGO_CANCEL IS NULL ";
        if ($date) {
            $setDate = str_replace("/", "-" ,$date);
            $query .= " AND REQUEST_DATE <= '{$setDate}' ";
        }
        $query .= "  ";
        return $query;
    }

    //伺い人
    function getRequestStaff($model, $staff) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     STAFFCD || ':' || STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staff}' ";
        return $query;
    }

    //収入項目
    function getIncomeLDiv($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.INCOME_L_CD || T1.INCOME_M_CD VALUE, ";
        $query .= "     T1.INCOME_L_CD || T1.INCOME_M_CD || ':' || L2.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                            AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                            AND L1.YEAR        = T1.YEAR ";
        $query .= "                            AND L1.LEVY_L_CD   = T1.INCOME_L_CD ";
        $query .= "     LEFT JOIN LEVY_M_MST L2 ON L2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                            AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                            AND L2.YEAR        = T1.YEAR ";
        $query .= "                            AND L2.LEVY_L_CD   = T1.INCOME_L_CD ";
        $query .= "                            AND L2.LEVY_M_CD   = T1.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND  = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR         = '{$model->year}' ";
        $query .= " AND INCOME_APPROVAL = '1' ";
        $query .= " AND INCOME_CANCEL IS NULL ";
        $query .= " AND L2.LEVY_IN_OUT_DIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //支出科目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND     = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR            = '{$model->year}' ";
        $query .= " AND T1.YEAR            = '{$model->year}' ";
        $query .= "   AND T1.LEVY_L_CD     = '".$model->selectedLCd."' ";
        $query .= " AND T1.ZATU_FLG IS NULL  ";
        $query .= " AND T1.YOBI_FLG IS NULL  ";
        $query .= " AND T1.KURIKOSI_FLG IS NULL  ";
        $query .= " AND T1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //業者コード
    function getTraderCd($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.TRADER_CD AS VALUE, ";
        $query .= "     T1.TRADER_CD || ':' || L1.TRADER_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TRADER_YDAT T1 ";
        $query .= "     LEFT JOIN TRADER_MST L1 ON L1.TRADER_CD = T1.TRADER_CD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //業者データ取得
    function getTraderData($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     L1.* ";
        $query .= " FROM ";
        $query .= "     TRADER_YDAT T1 ";
        $query .= "     LEFT JOIN TRADER_MST L1 ON L1.TRADER_CD = T1.TRADER_CD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.TRADER_CD = '{$model->field["TRADER_CD"]}' ";
        return $query;
    }

    //銀行コード
    function getBankCd() {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.BANKCD AS VALUE, ";
        $query .= "     T1.BANKCD || ':' || T1.BANKNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BANK_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //支店コード
    function getBranchCd($bankcd) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.BRANCHCD AS VALUE, ";
        $query .= "     T1.BRANCHCD || ':' || T1.BRANCHNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BANK_MST T1 ";
        $query .= " WHERE ";
        $query .= "     BANKCD = '{$bankcd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //支店コード
    function getNameMst($namecd) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //伝票番号を取得
    function getMaxRequestNo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND YEAR        = '{$model->year}' ";

        return $query;
    }

    //予算情報取得
    function getBudgetInfo($model) {
        $lcd = substr($model->field["OUTGO_L_M_CD"], 0, 2);
        $mcd = substr($model->field["OUTGO_L_M_CD"], 2, 4);

        $query  ="    WITH BUDGET_T AS ( ";
        $query .="      SELECT ";
        $query .="        SCHOOLCD, ";
        $query .="        SCHOOL_KIND, ";
        $query .="        YEAR, ";
        $query .="        BUDGET_L_CD, ";
        $query .="        BUDGET_M_CD, ";
        $query .="        SUM(BUDGET_MONEY) AS BUDGET_TOTAL ";
        $query .="      FROM ";
        $query .="        LEVY_BUDGET_DAT ";
        $query .="      GROUP BY ";
        $query .="        SCHOOLCD, ";
        $query .="        SCHOOL_KIND, ";
        $query .="        YEAR, ";
        $query .="        BUDGET_L_CD, ";
        $query .="        BUDGET_M_CD ";
        $query .="    ) , OUTGO_T AS ( ";
        $query .="      SELECT ";
        $query .="        OUTGO.SCHOOLCD, ";
        $query .="        OUTGO.SCHOOL_KIND, ";
        $query .="        OUTGO.YEAR, ";
        $query .="        OUTGO.OUTGO_L_CD, ";
        $query .="        OUTGO.OUTGO_M_CD, ";
        $query .="        SUM(SCH_PRICE * SCH_CNT) AS OUTGO_TOTAL ";
        $query .="      FROM ";
        $query .="        LEVY_REQUEST_OUTGO_MEISAI_DAT MEISAI ";
        $query .="        INNER JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ";
        $query .="          ON MEISAI.SCHOOLCD = OUTGO.SCHOOLCD ";
        $query .="          AND MEISAI.SCHOOL_KIND = OUTGO.SCHOOL_KIND ";
        $query .="          AND MEISAI.YEAR = OUTGO.YEAR ";
        $query .="          AND MEISAI.REQUEST_NO = OUTGO.REQUEST_NO ";
        $query .="          AND MEISAI.OUTGO_L_CD = OUTGO.OUTGO_L_CD ";
        $query .="          AND MEISAI.OUTGO_M_CD = OUTGO.OUTGO_M_CD ";
        $query .="      WHERE ";
        $query .="            MEISAI.SCHOOLCD           = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .="        AND MEISAI.SCHOOL_KIND        = '".$model->getSchoolKind."' ";
        $query .="        AND MEISAI.YEAR               = '{$model->year}' ";
        $query .="        AND MEISAI.OUTGO_L_CD         = '{$lcd}' ";
        $query .="        AND MEISAI.OUTGO_M_CD         = '{$mcd}' ";
        $query .="        AND EXISTS ( ";
        $query .="          SELECT ";
        $query .="            'X' ";
        $query .="          FROM ";
        $query .="            LEVY_BUDGET_MEISAI_DAT BUDGET_M ";
        $query .="          WHERE ";
        $query .="                MEISAI.SCHOOLCD       = BUDGET_M.SCHOOLCD ";
        $query .="            AND MEISAI.SCHOOL_KIND    = BUDGET_M.SCHOOL_KIND ";
        $query .="            AND MEISAI.YEAR           = BUDGET_M.YEAR ";
        $query .="            AND MEISAI.REQUEST_NO     = BUDGET_M.REQUEST_NO ";
        $query .="            AND MEISAI.OUTGO_L_CD     = BUDGET_M.OUTGO_L_CD ";
        $query .="            AND MEISAI.OUTGO_M_CD     = BUDGET_M.OUTGO_M_CD ";
        $query .="            AND MEISAI.OUTGO_S_CD     = BUDGET_M.OUTGO_S_CD ";
        $query .="        ) ";
        $query .="      GROUP BY ";
        $query .="        OUTGO.SCHOOLCD, ";
        $query .="        OUTGO.SCHOOL_KIND, ";
        $query .="        OUTGO.YEAR, ";
        $query .="        OUTGO.OUTGO_L_CD, ";
        $query .="        OUTGO.OUTGO_M_CD ";
        $query .="    ) ";
        $query .="    SELECT ";
        $query .="      BUDGET_TOTAL, ";
        $query .="      OUTGO_TOTAL, ";
        $query .="      BUDGET_TOTAL - OUTGO_TOTAL AS BUDGET_ZAN ";
        $query .="    FROM ";
        $query .="      BUDGET_T BT ";
        $query .="      INNER JOIN OUTGO_T OT ";
        $query .="        ON BT.SCHOOLCD = OT.SCHOOLCD ";
        $query .="        AND BT.SCHOOL_KIND = OT.SCHOOL_KIND ";
        $query .="        AND BT.YEAR = OT.YEAR ";
        $query .="        AND BT.BUDGET_L_CD = OT.OUTGO_L_CD ";
        $query .="        AND BT.BUDGET_M_CD = OT.OUTGO_M_CD ";
        $query .="    WHERE ";
        $query .="      BT.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .="      AND BT.SCHOOL_KIND = '".$model->getSchoolKind."' ";
        $query .="      AND BT.YEAR = '{$model->year}' ";
        $query .="      AND BT.BUDGET_L_CD = '{$lcd}' ";
        $query .="      AND BT.BUDGET_M_CD = '{$mcd}' ";

        return $query;
    }

    //削除、または作り直しの時
    function &getDeleteQuery($db, $model, $flg="") {
        $db->autoCommit(false);

        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= " AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
        $db->query($query);

        //SCHREG
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= " AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
        $db->query($query);

        //MAIN
        if ($flg) {
            $query  = " DELETE FROM ";
            $query .= "     LEVY_REQUEST_OUTGO_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
            $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
            $query .= " AND YEAR        = '{$model->year}' ";
            $query .= " AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
            $query .= " AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
            $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
            $db->query($query);
        }
        $db->commit();
        return;
    }

    //備考等取得
    function getRemark($model) {
        $query  = " SELECT ";
        $query .= "     REQUEST_TESUURYOU, ";
        $query .= "     TESUURYOU_SUMMARY, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= "     AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
        $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);

        //備考等を保持しておく
        $query = knjp905_mainQuery::getRemark($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $db->query($query);

        $outgolcd = substr($model->field["OUTGO_L_M_CD"], 0, 2);
        $outgomcd = substr($model->field["OUTGO_L_M_CD"], 2, 2);

        $incomeLcd = substr($model->field["INCOME_LM_CD"], 0, 2);
        $incomeMcd = substr($model->field["INCOME_LM_CD"], 2, 2);

        //小文字カナの大文字カナ、空白変換リスト
        $replace_of = array('ｧ','ｨ','ｩ','ｪ','ｫ','ｯ','ｬ','ｭ','ｮ','ヵ','ヶ','　');
        $replace_by = array('ｱ','ｲ','ｳ','ｴ','ｵ','ﾂ','ﾔ','ﾕ','ﾖ','ｶ','ｹ',' ');

        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["OUTGO_L_CD"][TEXT]       = $outgolcd;
        $data["OUTGO_M_CD"][TEXT]       = $outgomcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["REQUEST_DATE"][DATE]     = str_replace("/","-",$model->field["REQUEST_DATE"]);
        $data["REQUEST_REASON"][TEXT]   = $model->field["REQUEST_REASON"];
        if (!$model->field["REQUEST_STAFF"]) {
            $data["REQUEST_STAFF"][TEXT]    = STAFFCD;
        } else {
            $data["REQUEST_STAFF"][TEXT]    = $model->field["REQUEST_STAFF"];
        }
        $data["REQUEST_GK"][NUMBER]     = $model->reqestGk;
        $data["INCOME_L_CD"][TEXT]      = $incomeLcd;
        $data["INCOME_M_CD"][TEXT]      = $incomeMcd;
        if ($model->getKounyuRequestNo) {
            $data["KOUNYU_NO"][TEXT]       = $model->getKounyuRequestNo;
        }
        if ($model->getSekouRequestNo) {
            $data["SEKOU_NO"][TEXT]        = $model->getSekouRequestNo;
        }
        if ($model->getSeisanRequestNo) {
            $data["SEISAN_NO"][TEXT]       = $model->getSeisanRequestNo;
        }
        $data["TRADER_CD"][TEXT]        = $model->field["TRADER_CD"];
        $data["TRADER_NAME"][TEXT]      = $model->field["TRADER_NAME"];
        $data["BANKCD"][TEXT]           = $model->field["BANKCD"];
        $data["BRANCHCD"][TEXT]         = $model->field["BRANCHCD"];
        $data["BANK_DEPOSIT_ITEM"][TEXT]= $model->field["BANK_DEPOSIT_ITEM"];
        $data["BANK_ACCOUNTNO"][TEXT]   = $model->field["BANK_ACCOUNTNO"];
        $data["ACCOUNTNAME"][TEXT]      = $model->field["ACCOUNTNAME"];
        $data["ACCOUNTNAME_KANA"][TEXT] = str_replace($replace_of, $replace_by, mb_convert_kana($model->field["ACCOUNTNAME_KANA"], "akh"));
        $data["PAY_DIV"][TEXT]          = $model->field["PAY_DIV"];
        $data["REQUEST_TESUURYOU"][NUMBER]  = $row["REQUEST_TESUURYOU"];
        $data["TESUURYOU_SUMMARY"][TEXT]    = $row["TESUURYOU_SUMMARY"];
        $data["REMARK"][TEXT]               = $row["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_DAT");
        $db->query($query);

        $db->commit();
        return;
    }

    /************/
    /* 行コピー */
    /************/
    //max行番号を取得
    function getMaxLine($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     max(LINE_NO) ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= "     AND OUTGO_L_CD || OUTGO_M_CD = '{$model->getOutgoLMcd}' ";

        return $query;
    }

    //行コピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $maxLine = $db->getOne(knjp905_mainQuery::getMaxLine($model)) + 1;
        $insData = false;

        $outgolcd = substr($model->getOutgoLMcd, 0, 2);
        $outgomcd = substr($model->getOutgoLMcd, 2, 2);

        for ($i = 1; $i <= $model->koumoku; $i++) {
            //チェックのみ処理
            if ($model->field["COPY_CHECK".$i] == '1') {
                //LEVY_REQUEST_OUTGO_MEISAI_DAT
                $query  = " INSERT INTO LEVY_REQUEST_OUTGO_MEISAI_DAT ";
                $query .= "           ( SCHOOLCD, ";
                $query .= "             SCHOOL_KIND, ";
                $query .= "             YEAR, ";
                $query .= "             REQUEST_NO, ";
                $query .= "             LINE_NO, ";
                $query .= "             OUTGO_L_CD, ";
                $query .= "             OUTGO_M_CD, ";
                $query .= "             OUTGO_S_CD, ";
                $query .= "             COMMODITY_PRICE, ";
                $query .= "             COMMODITY_CNT, ";
                $query .= "             TOTAL_PRICE_ZEINUKI, ";
                $query .= "             TOTAL_TAX, ";
                $query .= "             TOTAL_PRICE, ";
                $query .= "             SCH_PRICE, ";
                $query .= "             SCH_CNT, ";
                $query .= "             HASUU, ";
                $query .= "             WARIHURI_DIV, ";
                $query .= "             TRADER_SEIKYU_NO, ";
                $query .= "             SEIKYU_MONTH, ";
                $query .= "             REMARK, ";
                $query .= "             REGISTERCD, ";
                $query .= "             UPDATED ) ";
                $query .= "   (SELECT ";
                $query .= "         SCHOOLCD, ";
                $query .= "         SCHOOL_KIND, ";
                $query .= "         YEAR, ";
                $query .= "         REQUEST_NO, ";
                $query .= "         {$maxLine}, ";
                $query .= "         OUTGO_L_CD, ";
                $query .= "         OUTGO_M_CD, ";
                $query .= "         OUTGO_S_CD, ";
                $query .= "         COMMODITY_PRICE, ";
                $query .= "         COMMODITY_CNT, ";
                $query .= "         TOTAL_PRICE_ZEINUKI, ";
                $query .= "         TOTAL_TAX, ";
                $query .= "         TOTAL_PRICE, ";
                $query .= "         SCH_PRICE, ";
                $query .= "         SCH_CNT, ";
                $query .= "         HASUU, ";
                $query .= "         WARIHURI_DIV, ";
                $query .= "         TRADER_SEIKYU_NO, ";
                $query .= "         SEIKYU_MONTH, ";
                $query .= "         REMARK, ";
                $query .= "         '".STAFFCD."', ";
                $query .= "         sysdate() ";
                $query .= "     FROM ";
                $query .= "         LEVY_REQUEST_OUTGO_MEISAI_DAT ";
                $query .= "     WHERE ";
                $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
                $query .= "     AND YEAR        = '{$model->year}' ";
                $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";
                $query .= "     AND LINE_NO     = {$i} ";
                $query .= "     AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
                $query .= "     AND OUTGO_L_CD || OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
                $query .= "     ) ";
                $db->query($query);

                //LEVY_REQUEST_OUTGO_SCHREG_DAT
                $query  = " INSERT INTO LEVY_REQUEST_OUTGO_SCHREG_DAT ";
                $query .= "           ( SCHOOLCD, ";
                $query .= "             SCHOOL_KIND, ";
                $query .= "             YEAR, ";
                $query .= "             REQUEST_NO, ";
                $query .= "             SCHREGNO, ";
                $query .= "             LINE_NO, ";
                $query .= "             OUTGO_L_CD, ";
                $query .= "             OUTGO_M_CD, ";
                $query .= "             OUTGO_S_CD, ";
                $query .= "             OUTGO_DATE, ";
                $query .= "             OUTGO_NO, ";
                $query .= "             OUTGO_MONEY, ";
                $query .= "             OUTGO_CERTIFICATE_CNT, ";
                $query .= "             REGISTERCD, ";
                $query .= "             UPDATED ) ";
                $query .= "   (SELECT ";
                $query .= "         SCHOOLCD, ";
                $query .= "         SCHOOL_KIND, ";
                $query .= "         YEAR, ";
                $query .= "         REQUEST_NO, ";
                $query .= "         SCHREGNO, ";
                $query .= "         {$maxLine}, ";
                $query .= "         OUTGO_L_CD, ";
                $query .= "         OUTGO_M_CD, ";
                $query .= "         OUTGO_S_CD, ";
                $query .= "         OUTGO_DATE, ";
                $query .= "         OUTGO_NO, ";
                $query .= "         OUTGO_MONEY, ";
                $query .= "         OUTGO_CERTIFICATE_CNT, ";
                $query .= "         '".STAFFCD."', ";
                $query .= "         sysdate() ";
                $query .= "     FROM ";
                $query .= "         LEVY_REQUEST_OUTGO_SCHREG_DAT ";
                $query .= "     WHERE ";
                $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
                $query .= "     AND YEAR        = '{$model->year}' ";
                $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";
                $query .= "     AND LINE_NO     = {$i} ";
                $query .= "     AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
                $query .= "     AND OUTGO_L_CD || OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
                $query .= "     ) ";
                $db->query($query);

                $maxLine++;
                $insData = true;
            }
        }

        //LEVY_REQUEST_OUTGO_DAT
        if ($insData) {
            $outGoCnt = $db->getOne(knjp905_mainQuery::getCntOutGoDat($model, $outgolcd, $outgomcd));

            //支出額を取得
            $reqesutGk = $db->getOne(knjp905_mainQuery::getReqesutGk($model, $outgolcd, $outgomcd));

            $incomeLcd = substr($model->field["INCOME_LM_CD"], 0, 2);
            $incomeMcd = substr($model->field["INCOME_LM_CD"], 2, 2);

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
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //LEVY_REQUEST_OUTGO_DATカウント
    function getCntOutGoDat($model, $lCd, $mCd) {
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

    //支出額総計を取得
    function getReqesutGk($model, $lCd, $mCd) {
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

    /****************/
    /* 手数料／備考 */
    /****************/
    //１レコード取得
    function getRowSub($model) {
        $query  = " SELECT ";
        $query .= "     REQUEST_TESUURYOU, ";
        $query .= "     TESUURYOU_SUMMARY, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '{$model->subSchoolCd}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->subSchoolKind}' ";
        $query .= "     AND YEAR        = '{$model->subYear}' ";
        $query .= "     AND OUTGO_L_CD  = '{$model->subOutgoLcd}' ";
        $query .= "     AND OUTGO_M_CD  = '{$model->subOutgoMcd}' ";
        $query .= "     AND REQUEST_NO  = '{$model->subRequestNo}' ";

        return $query;
    }

    //初期値取得
    function getLevyTaxMst($model, $fieldName) {
        $query  = " SELECT ";
        $query .= "     {$fieldName} ";
        $query .= " FROM ";
        $query .= "     LEVY_TAX_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$model->subYear}' ";
        $query .= "     AND TAX_CD     = '001' ";// 固定【001:振込手数料】
        $query .= "     AND '".CTRL_DATE."' between DATE_FROM and value(DATE_TO, '9999-12-31') ";
        $query .= " ORDER BY ";
        $query .= "     DATE_FROM desc ";

        return $query;
    }

    //UPDATE
    function &getUpdateQueryRemark($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["REQUEST_TESUURYOU"][NUMBER]  = $model->subField["REQUEST_TESUURYOU"];
        $data["TESUURYOU_SUMMARY"][TEXT]    = $model->subField["TESUURYOU_SUMMARY"];
        $data["REMARK"][TEXT]               = $model->subField["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD    = '{$model->subSchoolCd}' ";
        $where .= " AND SCHOOL_KIND = '{$model->subSchoolKind}' ";
        $where .= " AND YEAR        = '{$model->subYear}' ";
        $where .= " AND OUTGO_L_CD  = '{$model->subOutgoLcd}' ";
        $where .= " AND OUTGO_M_CD  = '{$model->subOutgoMcd}' ";
        $where .= " AND REQUEST_NO  = '{$model->subRequestNo}' ";

        $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>