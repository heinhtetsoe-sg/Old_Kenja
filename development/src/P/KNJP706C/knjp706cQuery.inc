<?php

require_once('for_php7.php');

class knjp706cQuery extends Query {

    function getFresh($model) {
        $query  = "(SELECT ";
        $query .= "     M1.*, ";
        $query .= "     ML1.BASE_REMARK4 AS MUSYOU_KAISU ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT M1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST ML1 ON M1.SCHREGNO = ML1.SCHREGNO ";
        $query .= "          AND ML1.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     ENTERYEAR = '{$model->year}') ";

        return $query;
    }

    //生徒の名前の取得
    function getStudentName($model)
    {
        $tableName = " V_SCHREG_BASE_MST ";
        if ($model->search_div == "1") {
            $tableName = knjp706cQuery::getFresh($model);
        }
        
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     MUSYOU_KAISU ";
        $query .= " FROM ";
        $query .= "     {$tableName} T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //履修回数
    function getRisyuuCnt($model)
    {
        $query  = " WITH STD_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR < '".$model->year."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STD_YEAR ";

        return $query;
    }

    //名称取得
    function getName($year, $cd, $cd2)
    {
        $query  = " SELECT NAME1 ";
        $query .= "   FROM V_NAME_MST ";
        $query .= "  WHERE YEAR = '".$year."' ";
        $query .= "    AND NAMECD1 = '".$cd."' ";
        $query .= "    AND NAMECD2 = '".$cd2."' ";
        return $query;
    }

    //リストの取得
    function getList($model)
    {
        $query  = " WITH S_PAY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD, ";
        $query .= "     MAX(T1.PAID_MONEY_DATE) AS PAID_MONEY_DATE, ";
        $query .= "     MAX(T1.PAID_MONEY_DIV) AS PAID_MONEY_DIV, ";
        $query .= "     SUM(T1.PAID_MONEY) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD ";
        $query .= " ), S_REPAY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD, ";
        $query .= "     MAX(T1.REPAY_MONEY_DATE) AS REPAY_MONEY_DATE, ";
        $query .= "     MAX(T1.REPAY_MONEY_DIV) AS REPAY_MONEY_DIV, ";
        $query .= "     SUM(T1.REPAY_MONEY) AS REPAY_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_REPAY_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD, ";
        $query .= "     DUE_M.PAY_DIV, ";
        $query .= "     replace(CAST(DUE_M.PAY_DATE AS CHAR(10)), '-', '/') AS PAY_DATE, ";
        $query .= "     T1.COLLECT_CNT, ";
        $query .= "     L1.COLLECT_S_NAME, ";
        $query .= "     L1.COLLECT_S_MONEY, ";
        $query .= "     T1.MONEY_DUE, ";
        $query .= "     S_PAY.PAID_MONEY, ";
        $query .= "     S_PAY.PAID_MONEY_DATE, ";
        $query .= "     S_PAY.PAID_MONEY_DIV, ";
        $query .= "     SN1.NAME1 AS PAID_MONEY_DIV_NAME, ";
        $query .= "     S_REPAY.REPAY_MONEY, ";
        $query .= "     S_REPAY.REPAY_MONEY_DIV, ";
        $query .= "     SN2.NAME1 AS REPAY_MONEY_DIV_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_S_DAT T1 ";
        $query .= "     LEFT JOIN COLLECT_MONEY_DUE_M_DAT DUE_M ON T1.YEAR = DUE_M.YEAR ";
        $query .= "          AND T1.COLLECT_GRP_CD = DUE_M.COLLECT_GRP_CD ";
        $query .= "          AND T1.SCHREGNO = DUE_M.SCHREGNO ";
        $query .= "          AND T1.COLLECT_L_CD = DUE_M.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD = DUE_M.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= "          AND T1.COLLECT_S_CD = L1.COLLECT_S_CD ";
        $query .= "     LEFT JOIN S_PAY ON T1.SCHREGNO = S_PAY.SCHREGNO ";
        $query .= "          AND T1.COLLECT_GRP_CD = S_PAY.COLLECT_GRP_CD ";
        $query .= "          AND T1.COLLECT_L_CD = S_PAY.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD = S_PAY.COLLECT_M_CD ";
        $query .= "          AND T1.COLLECT_S_CD = S_PAY.COLLECT_S_CD ";
        $query .= "     LEFT JOIN NAME_MST SN1 ON SN1.NAMECD1 = 'G205' ";
        $query .= "          AND SN1.NAMECD2 = S_PAY.PAID_MONEY_DIV ";
        $query .= "     LEFT JOIN S_REPAY ON T1.SCHREGNO = S_REPAY.SCHREGNO ";
        $query .= "          AND T1.COLLECT_GRP_CD = S_REPAY.COLLECT_GRP_CD ";
        $query .= "          AND T1.COLLECT_L_CD = S_REPAY.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD = S_REPAY.COLLECT_M_CD ";
        $query .= "          AND T1.COLLECT_S_CD = S_REPAY.COLLECT_S_CD ";
        $query .= "     LEFT JOIN NAME_MST SN2 ON SN2.NAMECD1 = 'G209' ";
        $query .= "          AND SN2.NAMECD2 = S_REPAY.REPAY_MONEY_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD ";

        return $query;
    }

    //１レコード取得
    function getPaidData($model)
    {
        $query  = " WITH S_PAY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD, ";
        $query .= "     MAX(T1.PAID_MONEY_DATE) AS PAID_MONEY_DATE, ";
        $query .= "     MAX(T1.PAID_MONEY_DIV) AS PAID_MONEY_DIV, ";
        $query .= "     SUM(T1.PAID_MONEY) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.COLLECT_GRP_CD = '{$model->collect_grp_cd}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD ";
        $query .= " ), S_REPAY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD, ";
        $query .= "     MAX(T1.REPAY_MONEY_DATE) AS REPAY_MONEY_DATE, ";
        $query .= "     MAX(T1.REPAY_MONEY_DIV) AS REPAY_MONEY_DIV, ";
        $query .= "     SUM(T1.REPAY_MONEY) AS REPAY_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_REPAY_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.COLLECT_GRP_CD = '{$model->collect_grp_cd}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD, ";
        $query .= "     T1.MONEY_DUE, ";
        $query .= "     M_DUE.PAY_DIV, ";
        $query .= "     M_DUE.PAY_DATE, ";
        $query .= "     T1.COLLECT_CNT, ";
        $query .= "     L1.COLLECT_S_MONEY AS TANKA, ";
        $query .= "     S_PAY.PAID_MONEY_DATE, ";
        $query .= "     S_PAY.PAID_MONEY_DIV, ";
        $query .= "     S_PAY.PAID_MONEY, ";
        $query .= "     S_REPAY.REPAY_MONEY_DATE, ";
        $query .= "     S_REPAY.REPAY_MONEY_DIV, ";
        $query .= "     S_REPAY.REPAY_MONEY  ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_S_DAT T1 ";
        $query .= "     LEFT JOIN S_PAY ON T1.SCHREGNO = S_PAY.SCHREGNO ";
        $query .= "          AND T1.COLLECT_GRP_CD = S_PAY.COLLECT_GRP_CD ";
        $query .= "          AND T1.COLLECT_L_CD = S_PAY.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD = S_PAY.COLLECT_M_CD ";
        $query .= "          AND T1.COLLECT_S_CD = S_PAY.COLLECT_S_CD ";
        $query .= "     LEFT JOIN S_REPAY ON T1.SCHREGNO = S_REPAY.SCHREGNO ";
        $query .= "          AND T1.COLLECT_GRP_CD = S_REPAY.COLLECT_GRP_CD ";
        $query .= "          AND T1.COLLECT_L_CD = S_REPAY.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD = S_REPAY.COLLECT_M_CD ";
        $query .= "          AND T1.COLLECT_S_CD = S_REPAY.COLLECT_S_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= "          AND T1.COLLECT_S_CD = L1.COLLECT_S_CD ";
        $query .= "     LEFT JOIN COLLECT_MONEY_DUE_M_DAT M_DUE ON T1.YEAR = M_DUE.YEAR ";
        $query .= "          AND T1.COLLECT_GRP_CD = M_DUE.COLLECT_GRP_CD ";
        $query .= "          AND T1.SCHREGNO = M_DUE.SCHREGNO ";
        $query .= "          AND T1.COLLECT_L_CD = M_DUE.COLLECT_L_CD ";
        $query .= "          AND T1.COLLECT_M_CD = M_DUE.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO         = '".$model->schregno."' ";
        $query .= "     AND T1.COLLECT_GRP_CD   = '{$model->collect_grp_cd}' ";
        $query .= "     AND T1.COLLECT_L_CD     = '".$model->exp_lcd."' ";
        $query .= "     AND T1.COLLECT_M_CD     = '".$model->exp_mcd."' ";
        $query .= "     AND T1.COLLECT_S_CD     = '".$model->exp_scd."' ";

        return $query;
    }

    //入金詳細
    function getPaySeq($model)
    {
        $query  = " SELECT ";
        $query .= "     PAID_SEQ AS VALUE, ";
        $query .= "     replace(CAST(PAID_MONEY_DATE AS CHAR(10)), '-', '/') || '：' || rtrim(CAST(PAID_MONEY AS CHAR(10))) AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND COLLECT_GRP_CD   = '{$model->collect_grp_cd}' ";
        $query .= "     AND COLLECT_L_CD     = '".$model->exp_lcd."' ";
        $query .= "     AND COLLECT_M_CD     = '".$model->exp_mcd."' ";
        $query .= "     AND COLLECT_S_CD     = '".$model->exp_scd."' ";
        $query .= " ORDER BY ";
        $query .= "     PAID_MONEY_DATE, ";
        $query .= "     PAID_SEQ ";

        return $query;
    }

    //入金
    function getSPaid($model)
    {
        $tableName = " V_SCHREG_BASE_MST ";
        if ($model->search_div == "1") {
            $tableName = knjp706cQuery::getFresh($model);
        }
        $seq = $model->field["PAID_SEQ"] ? $model->field["PAID_SEQ"] : "0";
        $query  = " WITH PAY_TOTAL AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(PAID_MONEY) AS PAID_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND COLLECT_GRP_CD   = '{$model->collect_grp_cd}' ";
        $query .= "     AND COLLECT_L_CD     = '".$model->exp_lcd."' ";
        $query .= "     AND COLLECT_M_CD     = '".$model->exp_mcd."' ";
        $query .= "     AND COLLECT_S_CD     = '".$model->exp_scd."' ";
        $query .= "     AND PAID_SEQ != ".$seq." ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.PAID_MONEY_DATE, ";
        $query .= "     T1.PAID_MONEY, ";
        $query .= "     T1.PAID_MONEY_DIV, ";
        $query .= "     VALUE(PAY_TOTAL.PAID_MONEY_TOTAL, 0) AS PAID_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     {$tableName} BASE ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_S_DAT T1 ON T1.YEAR = '".$model->year."' ";
        $query .= "          AND BASE.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND T1.PAID_SEQ = ".$seq." ";
        $query .= "     LEFT JOIN PAY_TOTAL ON BASE.SCHREGNO = PAY_TOTAL.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //返金詳細
    function getRepaySeq($model)
    {
        $query  = " SELECT ";
        $query .= "     REPAY_SEQ AS VALUE, ";
        $query .= "     replace(CAST(REPAY_MONEY_DATE AS CHAR(10)), '-', '/') || ':' || rtrim(CAST(REPAY_MONEY AS CHAR(10))) AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND COLLECT_GRP_CD   = '{$model->collect_grp_cd}' ";
        $query .= "     AND COLLECT_L_CD     = '".$model->exp_lcd."' ";
        $query .= "     AND COLLECT_M_CD     = '".$model->exp_mcd."' ";
        $query .= "     AND COLLECT_S_CD     = '".$model->exp_scd."' ";
        $query .= " ORDER BY ";
        $query .= "     REPAY_MONEY_DATE, ";
        $query .= "     REPAY_SEQ ";

        return $query;
    }

    //入金
    function getSRepay($model)
    {
        $tableName = " V_SCHREG_BASE_MST ";
        if ($model->search_div == "1") {
            $tableName = knjp706cQuery::getFresh($model);
        }
        $seq = $model->field["REPAY_SEQ"] ? $model->field["REPAY_SEQ"] : "0";
        $query  = " WITH REPAY_TOTAL AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(REPAY_MONEY) AS REPAY_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND COLLECT_GRP_CD   = '{$model->collect_grp_cd}' ";
        $query .= "     AND COLLECT_L_CD     = '".$model->exp_lcd."' ";
        $query .= "     AND COLLECT_M_CD     = '".$model->exp_mcd."' ";
        $query .= "     AND COLLECT_S_CD     = '".$model->exp_scd."' ";
        $query .= "     AND REPAY_SEQ != ".$seq." ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.REPAY_MONEY_DATE, ";
        $query .= "     T1.REPAY_MONEY, ";
        $query .= "     T1.REPAY_MONEY_DIV, ";
        $query .= "     VALUE(REPAY_TOTAL.REPAY_MONEY_TOTAL, 0) AS REPAY_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     {$tableName} BASE ";
        $query .= "     LEFT JOIN COLLECT_MONEY_REPAY_S_DAT T1 ON T1.YEAR = '".$model->year."' ";
        $query .= "          AND BASE.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND T1.REPAY_SEQ = ".$seq." ";
        $query .= "     LEFT JOIN REPAY_TOTAL ON BASE.SCHREGNO = REPAY_TOTAL.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //名称マスタより取得
    function getNamecd($year, $cd)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //予定追加
    function &insertDue($db, &$model)
    {
        $data = array();
        $data["MONEY_DUE"][NUMBER]      = $model->field["MONEY_DUE"];
        $data["COLLECT_CNT"][NUMBER]    = $model->field["COLLECT_CNT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->year."' ";
        $where .= "     AND SCHREGNO = '{$model->schregno}' ";
        $where .= "     AND COLLECT_GRP_CD = '{$model->collect_grp_cd}' ";
        $where .= "     AND COLLECT_L_CD = '{$model->exp_lcd}' ";
        $where .= "     AND COLLECT_M_CD = '{$model->exp_mcd}' ";
        $where .= "     AND COLLECT_S_CD = '{$model->exp_scd}' ";

        $model->pay_div = $model->field["PAY_DIV"];

        $query = Query::updateSQL($data, "COLLECT_MONEY_DUE_S_DAT", $where);
        return $query;
    }

    //予定削除
    function &deleteDue($db, &$model)
    {
        $query  = " DELETE FROM  COLLECT_MONEY_DUE_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND COLLECT_GRP_CD = '{$model->collect_grp_cd}' ";
        $query .= "     AND COLLECT_L_CD = '{$model->exp_lcd}' ";
        $query .= "     AND COLLECT_M_CD = '{$model->exp_mcd}' ";
        $query .= "     AND COLLECT_S_CD = '{$model->exp_scd}' ";

        return $query;
    }

    function getMaxPaidSeq($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(PAID_SEQ) ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //入金追加
    function &insertPaid($db, &$model)
    {
        $data = array();
        if ($model->field["PAID_SEQ"]) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->year."' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";
            $where .= "     AND PAID_SEQ = {$model->field["PAID_SEQ"]} ";
            $where .= "     AND COLLECT_GRP_CD = '{$model->collect_grp_cd}' ";
            $where .= "     AND COLLECT_L_CD = '{$model->exp_lcd}' ";
            $where .= "     AND COLLECT_M_CD = '{$model->exp_mcd}' ";
            $where .= "     AND COLLECT_S_CD = '{$model->exp_scd}' ";
        } else {
            $query = knjp706cQuery::getMaxPaidSeq($model);
            $setSeq = $db->getOne($query);
            $setSeq++;
            $data["YEAR"][TEXT]             = $model->year;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["PAID_SEQ"][NUMBER]       = $setSeq;
            $data["COLLECT_GRP_CD"][TEXT]   = $model->collect_grp_cd;
            $data["COLLECT_L_CD"][TEXT]     = $model->exp_lcd;
            $data["COLLECT_M_CD"][TEXT]     = $model->exp_mcd;
            $data["COLLECT_S_CD"][TEXT]     = $model->exp_scd;
        }

        $data["PAID_INPUT_FLG"][TEXT]   = "2";
        $data["PAID_MONEY_DATE"][DATE]  = $model->field["PAID_MONEY_DATE"];
        $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
        $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($model->field["PAID_SEQ"]) {
            $query = Query::updateSQL($data, "COLLECT_MONEY_PAID_S_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "COLLECT_MONEY_PAID_S_DAT", $where);
            $model->field["PAID_SEQ"] = $setSeq;
        }

        return $query;
    }

    //入金削除
    function &deletePaid($db, &$model)
    {
        $query  = " DELETE FROM  COLLECT_MONEY_PAID_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND PAID_SEQ = {$model->field["PAID_SEQ"]} ";
        $query .= "     AND COLLECT_GRP_CD = '{$model->collect_grp_cd}' ";
        $query .= "     AND COLLECT_L_CD = '{$model->exp_lcd}' ";
        $query .= "     AND COLLECT_M_CD = '{$model->exp_mcd}' ";
        $query .= "     AND COLLECT_S_CD = '{$model->exp_scd}' ";

        return $query;
    }

    function getMaxRepaySeq($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(REPAY_SEQ) ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //返金追加
    function &insertRepay($db, &$model)
    {
        $data = array();
        if ($model->field["REPAY_SEQ"]) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->year."' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";
            $where .= "     AND REPAY_SEQ = {$model->field["REPAY_SEQ"]} ";
            $where .= "     AND COLLECT_GRP_CD = '{$model->collect_grp_cd}' ";
            $where .= "     AND COLLECT_L_CD = '{$model->exp_lcd}' ";
            $where .= "     AND COLLECT_M_CD = '{$model->exp_mcd}' ";
            $where .= "     AND COLLECT_S_CD = '{$model->exp_scd}' ";
        } else {
            $query = knjp706cQuery::getMaxRepaySeq($model);
            $setSeq = $db->getOne($query);
            $setSeq++;
            $data["YEAR"][TEXT]             = $model->year;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["REPAY_SEQ"][NUMBER]      = $setSeq;
            $data["COLLECT_GRP_CD"][TEXT]   = $model->collect_grp_cd;
            $data["COLLECT_L_CD"][TEXT]     = $model->exp_lcd;
            $data["COLLECT_M_CD"][TEXT]     = $model->exp_mcd;
            $data["COLLECT_S_CD"][TEXT]     = $model->exp_scd;
        }

        $data["REPAY_INPUT_FLG"][TEXT]   = "2";
        $data["REPAY_MONEY_DATE"][DATE]  = $model->field["REPAY_MONEY_DATE"];
        $data["REPAY_MONEY"][NUMBER]     = $model->field["REPAY_MONEY"];
        $data["REPAY_MONEY_DIV"][TEXT]   = $model->field["REPAY_MONEY_DIV"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        if ($model->field["REPAY_SEQ"]) {
            $query = Query::updateSQL($data, "COLLECT_MONEY_REPAY_S_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "COLLECT_MONEY_REPAY_S_DAT", $where);
            $model->field["REPAY_SEQ"] = $setSeq;
        }

        return $query;
    }

    //返金削除
    function &deleteRepay($db, &$model)
    {
        $query  = " DELETE FROM  COLLECT_MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND REPAY_SEQ = {$model->field["REPAY_SEQ"]} ";
        $query .= "     AND COLLECT_GRP_CD = '{$model->collect_grp_cd}' ";
        $query .= "     AND COLLECT_L_CD = '{$model->exp_lcd}' ";
        $query .= "     AND COLLECT_M_CD = '{$model->exp_mcd}' ";
        $query .= "     AND COLLECT_S_CD = '{$model->exp_scd}' ";

        return $query;
    }

###################################################

    //更新
    function &getUpdateQuery($model)
    {
        $data = array();
        
        if (!strlen($model->inst_cd) && !strlen($model->paid_flg)) {
            $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
            $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
            $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
        }
        $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
        $data["REPAY_DATE"][TEXT]       = str_replace("/", "-", $model->field["REPAY_DATE"]);
        $data["REPAY_DIV"][TEXT]        = $model->field["REPAY_DIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE YEAR           = '".$model->year."'";
        $where .= "   AND SCHREGNO       = '".$model->schregno."'";
        $where .= "   AND EXPENSE_L_CD   = '".$model->exp_lcd."'";
        $where .= "   AND EXPENSE_M_CD   = '".$model->exp_mcd."'";
        $where .= "   AND EXPENSE_S_CD   = '".$model->exp_scd."'";

        $query = Query::updateSQL($data, "MONEY_PAID_S_DAT", $where);
        # echo $query;
        return $query;
    }

    //削除  データ(入金データ、返金データ)無しの場合、レコード削除
    function getDeleteQuery($model)
    {
        $query  = " DELETE FROM  MONEY_PAID_S_DAT ";
        $query .= "  WHERE YEAR             = '".$model->year."' ";
        $query .= "    AND SCHREGNO         = '".$model->schregno."' ";
        $query .= "    AND EXPENSE_L_CD     = '".$model->exp_lcd."'";
        $query .= "    AND EXPENSE_M_CD     = '".$model->exp_mcd."'";
        $query .= "    AND EXPENSE_S_CD     = '".$model->exp_scd."'";
        $query .= "    AND PAID_MONEY_DATE  IS NULL";
        $query .= "    AND PAID_MONEY       IS NULL";
        $query .= "    AND PAID_MONEY_DIV   IS NULL";
        $query .= "    AND REPAY_DATE       IS NULL";
        $query .= "    AND REPAY_MONEY      IS NULL";
        $query .= "    AND REPAY_DIV        IS NULL";

        return $query;
    }

}
?>
