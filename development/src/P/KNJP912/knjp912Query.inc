<?php

require_once('for_php7.php');

class knjp912Query extends Query {

    //支出伺の伝票取得
    function getOutgoRequestNo() {
    
        $query  = " SELECT ";
        $query .= "     CASE WHEN HENKIN_APPROVAL = '1' THEN '済　' || REQUEST_NO ELSE '未　' || REQUEST_NO END AS LABEL, ";
        $query .= "     VALUE(HENKIN_APPROVAL, '0') || '-' || REQUEST_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND HENKIN_FLG = '1' ";
        $query .= " AND OUTGO_APPROVAL = '1' ";
        $query .= " AND OUTGO_CANCEL IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     REQUEST_NO ";

        return $query;
    }

    //対象の返金用の支出伺を取得
    function getOutGoHenkinData($model) {
        if ($model->field["STATUS_REQUEST_NO"] !== 'all') {
            list($henkin_approval, $requestNo) = explode('-', $model->field["STATUS_REQUEST_NO"]);
        } else {
            $henkin_approval = "";
            $requestNo = "";
        }
        $query  = " SELECT ";
        $query .= "     L2.SCHREGNO, ";
        $query .= "     L1.COMMODITY_PRICE, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.OUTGO_L_CD, ";
        $query .= "     T1.OUTGO_M_CD, ";
        $query .= "     L1.OUTGO_S_CD, ";
        $query .= "     T1.REQUEST_NO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                AND L1.OUTGO_L_CD = T1.OUTGO_L_CD ";
        $query .= "                                                AND L1.OUTGO_M_CD = T1.OUTGO_M_CD ";
        $query .= "                                                AND L1.REQUEST_NO = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                                AND L2.OUTGO_L_CD = T1.OUTGO_L_CD ";
        $query .= "                                                AND L2.OUTGO_M_CD = T1.OUTGO_M_CD ";
        $query .= "                                                AND L2.REQUEST_NO = T1.REQUEST_NO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND L1.OUTGO_S_CD = L2.OUTGO_S_CD ";
        $query .= " AND T1.HENKIN_FLG = '1' ";
        $query .= " AND T1.HENKIN_APPROVAL IS NULL ";
        $query .= " AND T1.HENKIN_DATE IS NULL ";
        $query .= " AND T1.OUTGO_APPROVAL = '1' ";
        $query .= " AND T1.OUTGO_CANCEL IS NULL ";
        if ($requestNo) {
            $query .= " AND T1.REQUEST_NO = '{$requestNo}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     L2.SCHREGNO, ";
        $query .= "     L1.COMMODITY_PRICE, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.OUTGO_L_CD, ";
        $query .= "     T1.OUTGO_M_CD, ";
        $query .= "     L1.OUTGO_S_CD, ";
        $query .= "     T1.REQUEST_NO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_S_CD ";

        return $query;
    }

    //返金用COLLECT_MONEY_REPAY_*_DATの返金可能な金額を取得
    function getSchnoCollectMoneyData($row, $model) {
        //対象テーブルセット
        if ($row["COLLECT_S_CD"] == '00') {
            $dueTablename = "COLLECT_MONEY_DUE_M_DAT";
            $paidTablename = "COLLECT_MONEY_PAID_M_DAT";
            $repayTablename = "COLLECT_MONEY_REPAY_M_DAT";
        } else {
            $dueTablename = "COLLECT_MONEY_DUE_S_DAT";
            $paidTablename = "COLLECT_MONEY_PAID_S_DAT";
            $repayTablename = "COLLECT_MONEY_REPAY_S_DAT";
        }
        //入金している金額を取得
        $query  = " WITH PAID_TOTAL AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(L1.PAID_MONEY) AS PAID_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     $dueTablename T1 ";
        $query .= "     INNER JOIN $paidTablename L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                AND L1.SCHREGNO = T1.SCHREGNO ";        
        $query .= "                                AND L1.COLLECT_GRP_CD = T1.COLLECT_GRP_CD  ";
        $query .= "                                AND L1.COLLECT_L_CD = T1.COLLECT_L_CD  ";
        $query .= "                                AND L1.COLLECT_M_CD = T1.COLLECT_M_CD  ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "                                AND L1.COLLECT_S_CD = T1.COLLECT_S_CD  ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$row["YEAR"]}' ";
        $query .= "     AND T1.COLLECT_GRP_CD   = '{$row["COLLECT_GRP_CD"]}' ";
        $query .= "     AND T1.COLLECT_L_CD     = '{$row["COLLECT_L_CD"]}' ";
        $query .= "     AND T1.COLLECT_M_CD     = '{$row["COLLECT_M_CD"]}' ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "     AND T1.COLLECT_S_CD     = '{$row["COLLECT_S_CD"]}' ";
        }
        $query .= " AND T1.SCHREGNO = '{$row["SCHREGNO"]}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        //返金している金額を取得
        $query .= " ), REPAY_TOTAL AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(L1.REPAY_MONEY) AS REPAY_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     $dueTablename T1 ";
        $query .= "     INNER JOIN $repayTablename L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                  AND L1.SCHREGNO = T1.SCHREGNO ";        
        $query .= "                                  AND L1.COLLECT_GRP_CD = T1.COLLECT_GRP_CD  ";
        $query .= "                                  AND L1.COLLECT_L_CD = T1.COLLECT_L_CD  ";
        $query .= "                                  AND L1.COLLECT_M_CD = T1.COLLECT_M_CD  ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "                                AND L1.COLLECT_S_CD = T1.COLLECT_S_CD  ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$row["YEAR"]}' ";
        $query .= "     AND T1.COLLECT_GRP_CD   = '{$row["COLLECT_GRP_CD"]}' ";
        $query .= "     AND T1.COLLECT_L_CD     = '{$row["COLLECT_L_CD"]}' ";
        $query .= "     AND T1.COLLECT_M_CD     = '{$row["COLLECT_M_CD"]}' ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "     AND T1.COLLECT_S_CD     = '{$row["COLLECT_S_CD"]}' ";
        }
        $query .= " AND T1.SCHREGNO = '{$row["SCHREGNO"]}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        //返金可能額
        $query .= " ) ";
        $query .= " SELECT ";
        //$query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.PAID_MONEY_TOTAL, 0) - VALUE(L1.REPAY_MONEY_TOTAL, 0) AS HENKIN_KANOU_GK ";
        $query .= " FROM ";
        $query .= "     PAID_TOTAL T1";
        $query .= "     LEFT JOIN REPAY_TOTAL L1 ON L1.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }

    //COLLECT_MONEY_REPAY_*_DATのREPAY_SEQのMAXを取得
    function getMaxRepayseq($row, $model) {
        //対象テーブルセット
        if ($row["COLLECT_S_CD"] == '00') {
            $repayTablename = "COLLECT_MONEY_REPAY_M_DAT";
        } else {
            $repayTablename = "COLLECT_MONEY_REPAY_S_DAT";
        }
        $query  = " SELECT ";
        $query .= "     MAX(REPAY_SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     $repayTablename ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$row["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$row["SCHREGNO"]}' ";

        return $query;
    }

    //対象の生徒ごとに返金更新処理
    function updateQuery($db, $row, $model) {
        //MAXのSEQを取得
        $getMaxseq = $db->getOne(knjp912Query::getMaxRepayseq($row, $model));
        if ($getMaxseq != "") {
            $setSeq = $getMaxseq + 1;
        } else {
            $setSeq = "1";
        }
        //対象テーブルセット
        if ($row["COLLECT_S_CD"] == '00') {
            $repayTablename = "COLLECT_MONEY_REPAY_M_DAT";
        } else {
            $repayTablename = "COLLECT_MONEY_REPAY_S_DAT";
        }
        
        //返金データを作成
        $data = array();
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
        $data["REPAY_SEQ"][NUMBER]      = $setSeq;
        $data["COLLECT_GRP_CD"][TEXT]   = $row["COLLECT_GRP_CD"];
        $data["COLLECT_L_CD"][TEXT]     = $row["COLLECT_L_CD"];
        $data["COLLECT_M_CD"][TEXT]     = $row["COLLECT_M_CD"];
        if ($repayTablename == "COLLECT_MONEY_REPAY_S_DAT") {
            $data["COLLECT_S_CD"][TEXT]     = $row["COLLECT_S_CD"];
        }
        $data["REPAY_INPUT_FLG"][TEXT]  = "2"; //2:徴収金システムより入金をセット
        $data["REPAY_MONEY_DATE"][DATE] = $model->field["HENKIN_DATE"];
        $data["REPAY_MONEY"][NUMBER]    = $row["COMMODITY_PRICE"];
        $data["REPAY_MONEY_DIV"][TEXT]  = "99"; //名称マスタ「G209」99:その他
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, $repayTablename);
        $db->query($query);
        
        //対象の支出伺の返金手続きのチェックと日付を更新する
        $data = array();
        $data["HENKIN_APPROVAL"][TEXT]  = "1";
        $data["HENKIN_DATE"][DATE]      = $model->field["HENKIN_DATE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $where  = " WHERE ";
        $where .= "     YEAR = '".$row["YEAR"]."' ";
        $where .= " AND OUTGO_L_CD = '".$row["OUTGO_L_CD"]."' ";
        $where .= " AND OUTGO_M_CD = '".$row["OUTGO_M_CD"]."' ";
        $where .= " AND REQUEST_NO = '".$row["REQUEST_NO"]."' ";

        $query = Query::updateSQL($data, LEVY_REQUEST_OUTGO_DAT, $where);
        $db->query($query);
        
        return;
    }
}
?>