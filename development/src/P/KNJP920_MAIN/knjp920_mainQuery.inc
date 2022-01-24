<?php

require_once('for_php7.php');

class knjp920_mainQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND CLOSE_FLG = '1' ";

        return $query;
    }

    //支出伺の決済の状況を取得(比較時に利用)
    function getOutgoData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } else if ($flg == "CANCEL") {
            $query .= "     T1.OUTGO_CANCEL ";
        } else {
            $query .= "     T1.REQUEST_NO ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.KOUNYU_NO = '{$model->getRequestNo}' ";
        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     L$i.KOUNYU_L_CD || L$i.KOUNYU_M_CD || L$i.KOUNYU_S_CD || ':' || S$i.LEVY_S_NAME AS LEVY_S_NAME$i, ";
            $query .= "     L$i.COMMODITY_CNT AS COMMODITY_CNT$i, ";
            $query .= "     L$i.COMMODITY_PRICE AS COMMODITY_PRICE$i, ";
            $query .= "     L$i.TOTAL_PRICE_ZEINUKI AS TOTAL_PRICE_ZEINUKI$i, ";
            $query .= "     L$i.TOTAL_TAX AS TOTAL_TAX$i, ";
            $query .= "     L$i.TOTAL_PRICE AS TOTAL_PRICE$i, ";
            $query .= "     L$i.REMARK AS REMARK$i, ";
        }
        $query .= "     T1.KOUNYU_L_CD || T1.KOUNYU_M_CD AS KOUNYU_L_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_DAT T1 ";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     LEFT JOIN LEVY_REQUEST_KOUNYU_MEISAI_DAT L$i ON L$i.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= "                                                 AND L$i.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                                 AND L$i.YEAR        = T1.YEAR ";
            $query .= "                                                 AND L$i.KOUNYU_L_CD = T1.KOUNYU_L_CD ";
            $query .= "                                                 AND L$i.KOUNYU_M_CD = T1.KOUNYU_M_CD ";
            $query .= "                                                 AND L$i.REQUEST_NO  = T1.REQUEST_NO ";
            $query .= "                                                 AND L$i.LINE_NO     = ".$i." ";
            //$query .= "                                                AND L$i.KOUNYU_S_CD = '".sprintf("%02d", $i)."' ";
            $query .= "     LEFT JOIN LEVY_S_MST S$i ON S$i.SCHOOLCD    = L$i.SCHOOLCD ";
            $query .= "                             AND S$i.SCHOOL_KIND = L$i.SCHOOL_KIND ";
            $query .= "                             AND S$i.YEAR        = L$i.YEAR ";
            $query .= "                             AND S$i.LEVY_L_CD   = L$i.KOUNYU_L_CD ";
            $query .= "                             AND S$i.LEVY_M_CD   = L$i.KOUNYU_M_CD ";
            $query .= "                             AND S$i.LEVY_S_CD   = L$i.KOUNYU_S_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.KOUNYU_L_CD = '{$model->getKounyuLcd}' ";
        $query .= "     AND T1.KOUNYU_L_CD || T1.KOUNYU_M_CD = '{$model->getKounyuLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.KOUNYU_L_CD, ";
        $query .= "     T1.KOUNYU_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
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
        
    //購入項目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " AND T1.ZATU_FLG IS NULL  ";
        $query .= " AND T1.YOBI_FLG IS NULL  ";
        $query .= " AND T1.KURIKOSI_FLG IS NULL  ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //見積書徴収業者マスタ
    function getTraderMst($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.TRADER_CD AS VALUE, ";
        $query .= "     T1.TRADER_CD || ':' || L1.TRADER_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TRADER_YDAT T1 ";
        $query .= "     LEFT JOIN TRADER_MST L1 ON L1.TRADER_CD = T1.TRADER_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
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
        $query .= "     LEVY_REQUEST_KOUNYU_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";

        return $query;
    }

    //削除、または作り直しの時
    function &getDeleteQuery($db, $model, $flg="") {
        $db->autoCommit(false);

        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND KOUNYU_L_CD = '{$model->getKounyuLcd}' ";
        $query .= " AND KOUNYU_M_CD = '{$model->getKounyuMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);

        //MAIN
        if ($flg) {
            $query  = " DELETE FROM ";
            $query .= "     LEVY_REQUEST_KOUNYU_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
            $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
            $query .= " AND YEAR = '{$model->year}' ";
            $query .= " AND KOUNYU_L_CD = '{$model->getKounyuLcd}' ";
            $query .= " AND KOUNYU_M_CD = '{$model->getKounyuMcd}' ";
            $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
            $db->query($query);
            
            //支出伺伝票の紐付けをNULLにする
            if ($model->getOutgoRequestNo) {
                $data = array();
                $data["KOUNYU_NO"][TEXT]  = "";
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][NUMBER]  = "sysdate()";
                $where  = " WHERE ";
                $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
                $where .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
                $where .= " AND YEAR = '{$model->year}' ";
                $where .= " AND OUTGO_L_CD = '{$model->getKounyuLcd}' ";
                $where .= " AND OUTGO_M_CD = '{$model->getKounyuMcd}' ";
                $where .= " AND REQUEST_NO = '{$model->getOutgoRequestNo}' ";
                
                $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
                $db->query($query);
            }
        }
        $db->commit();
        return;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND KOUNYU_L_CD = '{$model->getKounyuLcd}' ";
        $query .= " AND KOUNYU_M_CD = '{$model->getKounyuMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);

        $kounyulcd = substr($model->field["KOUNYU_L_M_CD"], 0, 2);
        $kounyumcd = substr($model->field["KOUNYU_L_M_CD"], 2, 2);
        
        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["KOUNYU_L_CD"][TEXT]       = $kounyulcd;
        $data["KOUNYU_M_CD"][TEXT]       = $kounyumcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["REQUEST_DATE"][DATE]     = str_replace("/","-",$model->field["REQUEST_DATE"]);
        //$data["REQUEST_REASON"][TEXT]   = $model->field["REQUEST_REASON"];
        $data["REQUEST_STAFF"][TEXT]    = $model->field["REQUEST_STAFF"];
        $data["REQUEST_GK"][NUMBER]     = $model->sumTotalPrice;

        $data["TRADER_CD1"][TEXT]           = $model->field["TRADER_CD1"];
        $data["TRADER_NAME1"][TEXT]         = $model->field["TRADER_NAME1"];
        $data["TRADER_KAKUTEI1"][TEXT]      = $model->field["TRADER_KAKUTEI1"];
        $data["TRADER_CD2"][TEXT]           = $model->field["TRADER_CD2"];
        $data["TRADER_NAME2"][TEXT]         = $model->field["TRADER_NAME2"];
        $data["TRADER_KAKUTEI2"][TEXT]      = $model->field["TRADER_KAKUTEI2"];
        $data["TRADER_CD3"][TEXT]           = $model->field["TRADER_CD3"];
        $data["TRADER_NAME3"][TEXT]         = $model->field["TRADER_NAME3"];
        $data["TRADER_KAKUTEI3"][TEXT]      = $model->field["TRADER_KAKUTEI3"];
        $data["TRADER_CD4"][TEXT]           = $model->field["TRADER_CD4"];
        $data["TRADER_NAME4"][TEXT]         = $model->field["TRADER_NAME4"];
        $data["TRADER_KAKUTEI4"][TEXT]      = $model->field["TRADER_KAKUTEI4"];

        $data["KOUNYU_MITUMORI_DATE"][DATE] = str_replace("/","-",$model->field["KOUNYU_MITUMORI_DATE"]);
        $data["KEIYAKU_HOUHOU"][TEXT]       = $model->field["KEIYAKU_HOUHOU"];
        $data["NOUNYU_LIMIT_DATE"][DATE]    = str_replace("/","-",$model->field["NOUNYU_LIMIT_DATE"]);
        $data["NOUNYU_PLACE"][TEXT]         = $model->field["NOUNYU_PLACE"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_KOUNYU_DAT");
        $db->query($query);

        $db->commit();
        return;
    }
    
}
?>