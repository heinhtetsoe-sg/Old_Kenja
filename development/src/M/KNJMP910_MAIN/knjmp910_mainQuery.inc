<?php

require_once('for_php7.php');

class knjmp910_mainQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND CLOSE_FLG = '1' ";

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
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND SEISAN_L_CD || SEISAN_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getSeisanRequestNo}' ";

        return $query;
    }

    //返金用入金科目
    function getCollectSDiv($model) {
        $query  = "";
        //入金項目(細目なし)
        $query .= " SELECT DISTINCT ";
        $query .= "     L3.COLLECT_L_CD || L3.COLLECT_M_CD || '00' || L3.COLLECT_GRP_CD AS VALUE, ";
        $query .= "     L3.COLLECT_GRP_CD || ':' ||L3.COLLECT_L_CD || L3.COLLECT_M_CD || '00' || '　' || L1.COLLECT_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_L_MST T1  ";
        $query .= " INNER JOIN COLLECT_M_MST L1 ON L1.COLLECT_L_CD = T1.COLLECT_L_CD  ";
        //実際に入金のあるデータ
        $query .= " INNER JOIN COLLECT_MONEY_DUE_M_DAT L2 ON L2.YEAR = L1.YEAR  ";
        $query .= "                                      AND L2.COLLECT_L_CD = L1.COLLECT_L_CD  ";
        $query .= "                                      AND L2.COLLECT_M_CD = L1.COLLECT_M_CD  ";
        $query .= " INNER JOIN COLLECT_MONEY_PAID_M_DAT L3 ON L3.YEAR = L2.YEAR  ";
        $query .= "                                       AND L3.COLLECT_GRP_CD = L2.COLLECT_GRP_CD  ";
        $query .= "                                       AND L3.COLLECT_L_CD = L2.COLLECT_L_CD  ";
        $query .= "                                       AND L3.COLLECT_M_CD = L2.COLLECT_M_CD  ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '{$model->year}'  ";
        $query .= " AND L1.COLLECT_S_EXIST_FLG = '2'  ";
        $query .= " AND T1.LEVY_FLG = '1' ";
        $query .= " UNION ";
        //入金細目
        $query .= " SELECT DISTINCT ";
        $query .= "     L4.COLLECT_L_CD || L4.COLLECT_M_CD || L4.COLLECT_S_CD || L4.COLLECT_GRP_CD AS VALUE, ";
        $query .= "     L4.COLLECT_GRP_CD || ':' || L4.COLLECT_L_CD || L4.COLLECT_M_CD || L4.COLLECT_S_CD || '　' || L2.COLLECT_S_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_L_MST T1  ";
        $query .= " INNER JOIN COLLECT_M_MST L1 ON L1.COLLECT_L_CD = T1.COLLECT_L_CD  ";
        $query .= " INNER JOIN COLLECT_S_MST L2 ON L2.YEAR = L1.YEAR  ";
        $query .= "                            AND L2.COLLECT_L_CD = L1.COLLECT_L_CD  ";
        $query .= "                            AND L2.COLLECT_M_CD = L1.COLLECT_M_CD ";
        //実際に入金のあるデータ
        $query .= " INNER JOIN COLLECT_MONEY_DUE_S_DAT L3 ON L3.YEAR = L2.YEAR  ";
        $query .= "                                      AND L3.COLLECT_L_CD = L2.COLLECT_L_CD  ";
        $query .= "                                      AND L3.COLLECT_M_CD = L2.COLLECT_M_CD  ";
        $query .= "                                      AND L3.COLLECT_S_CD = L2.COLLECT_S_CD  ";
        $query .= " INNER JOIN COLLECT_MONEY_PAID_S_DAT L4 ON L4.YEAR = L3.YEAR  ";
        $query .= "                                       AND L4.COLLECT_GRP_CD = L3.COLLECT_GRP_CD  ";
        $query .= "                                       AND L4.COLLECT_L_CD = L3.COLLECT_L_CD  ";
        $query .= "                                       AND L4.COLLECT_M_CD = L3.COLLECT_M_CD  ";
        $query .= "                                       AND L4.COLLECT_S_CD = L3.COLLECT_S_CD  ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '{$model->year}'  ";
        $query .= " AND L1.COLLECT_S_EXIST_FLG = '1'  ";
        $query .= " AND T1.LEVY_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD || T1.COLLECT_S_CD || T1.COLLECT_GRP_CD AS COLLECT_L_M_S_GRP_CD, ";//返金用
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     L$i.OUTGO_L_CD || L$i.OUTGO_M_CD || L$i.OUTGO_S_CD || ':' || S$i.LEVY_S_NAME AS LEVY_S_NAME$i, ";
            $query .= "     L$i.COMMODITY_CNT AS COMMODITY_CNT$i, ";
            $query .= "     L$i.COMMODITY_PRICE AS COMMODITY_PRICE$i, ";
            $query .= "     L$i.TOTAL_PRICE_ZEINUKI AS TOTAL_PRICE_ZEINUKI$i, ";
            $query .= "     L$i.TOTAL_TAX AS TOTAL_TAX$i, ";
            $query .= "     L$i.TOTAL_PRICE AS TOTAL_PRICE$i, ";
            $query .= "     L$i.REMARK AS REMARK$i, ";
        }
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     T1.OUTGO_L_CD || T1.OUTGO_M_CD AS OUTGO_L_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L$i ON L$i.YEAR = T1.YEAR ";
            $query .= "                                                AND L$i.OUTGO_L_CD = T1.OUTGO_L_CD ";
            $query .= "                                                AND L$i.OUTGO_M_CD = T1.OUTGO_M_CD ";
            $query .= "                                                AND L$i.REQUEST_NO = T1.REQUEST_NO ";
            $query .= "                                                AND L$i.LINE_NO = ".$i." ";
            //$query .= "                                                AND L$i.OUTGO_S_CD = '".sprintf("%02d", $i)."' ";
            $query .= "     LEFT JOIN LEVY_S_MST S$i ON S$i.YEAR = L$i.YEAR ";
            $query .= "                             AND S$i.LEVY_L_CD = L$i.OUTGO_L_CD ";
            $query .= "                             AND S$i.LEVY_M_CD = L$i.OUTGO_M_CD ";
            $query .= "                             AND S$i.LEVY_S_CD = L$i.OUTGO_S_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD = '{$model->getOutgoLcd}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.OUTGO_L_CD, ";
        $query .= "     T1.OUTGO_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //入金科目、決済の状況を取得(比較時に利用)
    function getLevyData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "INCOME_L_CD") {
            $query .= "     T1.INCOME_L_CD ";
        } else if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } else if ($flg == "CANCEL") {
            $query .= "     T1.OUTGO_CANCEL ";
        } else if ($flg == "COLLECT") {
            $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD || T1.COLLECT_S_CD || T1.COLLECT_GRP_CD AS COLLECT_L_M_S_GRP_CD ";
        } else if ($flg == "HENKIN_FLG") {
            $query .= "     T1.HENKIN_FLG ";
        } else {
            $query .= "     T1.HENKIN_APPROVAL ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD = '{$model->getOutgoLcd}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        return $query;
    }

    //収入の計算
    function getIncomeSumRequestGk($model, $incomeLcd, $date = "") {
        $query  = " SELECT ";
        $query .= "     VALUE(SUM(REQUEST_GK), 0) AS SUM_REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND INCOME_L_CD =  '{$incomeLcd}' ";
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
        $query  = " SELECT ";
        $query .= "     VALUE(SUM(REQUEST_GK), 0) AS SUM_REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND INCOME_L_CD =  '{$Row["INCOME_L_CD"]}' ";
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
    function getRequestStaff($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFCD || ':' || STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //収入項目
    function getIncomeLDiv($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.INCOME_L_CD VALUE, ";
        $query .= "     T1.INCOME_L_CD || ':' || L1.LEVY_L_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST L1 ON L1.YEAR = L1.YEAR ";
        $query .= "                            AND L1.LEVY_L_CD = T1.INCOME_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND INCOME_APPROVAL = '1' ";
        $query .= " AND INCOME_CANCEL IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //支出科目
    function getLevyMDiv() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " AND T1.ZATU_FLG IS NULL  ";
        $query .= " AND T1.YOBI_FLG IS NULL  ";
        $query .= " AND T1.KURIKOSI_FLG IS NULL  ";
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
        $query .= "     YEAR = '{$model->year}' ";

        return $query;
    }

    //削除、または作り直しの時
    function &getDeleteQuery($db, $model, $flg="") {
        $db->autoCommit(false);
        
        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND OUTGO_L_CD = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD = '{$model->getOutgoMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);
        
        //SCHREG
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND OUTGO_L_CD = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD = '{$model->getOutgoMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);
        
        //MAIN
        if ($flg) {
            $query  = " DELETE FROM ";
            $query .= "     LEVY_REQUEST_OUTGO_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= " AND OUTGO_L_CD = '{$model->getOutgoLcd}' ";
            $query .= " AND OUTGO_M_CD = '{$model->getOutgoMcd}' ";
            $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
            $db->query($query);
        }
        $db->commit();
        return;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND OUTGO_L_CD = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD = '{$model->getOutgoMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);

        $outgolcd = substr($model->field["OUTGO_L_M_CD"], 0, 2);
        $outgomcd = substr($model->field["OUTGO_L_M_CD"], 2, 2);
        
        //小文字カナの大文字カナ、空白変換リスト
        $replace_of = array('ｧ','ｨ','ｩ','ｪ','ｫ','ｯ','ｬ','ｭ','ｮ','ヵ','ヶ','　');
        $replace_by = array('ｱ','ｲ','ｳ','ｴ','ｵ','ﾂ','ﾔ','ﾕ','ﾖ','ｶ','ｹ',' ');
        
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["OUTGO_L_CD"][TEXT]       = $outgolcd;
        $data["OUTGO_M_CD"][TEXT]       = $outgomcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["REQUEST_DATE"][DATE]     = str_replace("/","-",$model->field["REQUEST_DATE"]);
        $data["REQUEST_REASON"][TEXT]   = $model->field["REQUEST_REASON"];
        $data["REQUEST_STAFF"][TEXT]    = $model->field["REQUEST_STAFF"];
        $data["REQUEST_GK"][NUMBER]     = $model->field["REQUEST_GK"];
        $data["REQUEST_TESUURYOU"][NUMBER] = $model->field["REQUEST_TESUURYOU"];
        $data["INCOME_L_CD"][TEXT]      = $model->field["INCOME_L_CD"];
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
        //返金用
        if ($model->field["HENKIN_FLG"]) {
            $collectlcd   = substr($model->field["COLLECT_L_M_S_GRP_CD"], 0, 2);
            $collectmcd   = substr($model->field["COLLECT_L_M_S_GRP_CD"], 2, 2);
            $collectscd   = substr($model->field["COLLECT_L_M_S_GRP_CD"], 4, 2);
            $collectgrpcd = substr($model->field["COLLECT_L_M_S_GRP_CD"], 6, 4);
            $data["HENKIN_FLG"][TEXT]    = $model->field["HENKIN_FLG"];
            $data["COLLECT_GRP_CD"][TEXT]= $collectgrpcd;
            $data["COLLECT_L_CD"][TEXT]  = $collectlcd;
            $data["COLLECT_M_CD"][TEXT]  = $collectmcd;
            $data["COLLECT_S_CD"][TEXT]  = $collectscd;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_DAT");
        $db->query($query);

        $db->commit();
        return;
    }
    
}
?>