<?php

require_once('for_php7.php');


class knjmp920_meisaiQuery extends Query {

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

    //支出伺の決済の状況を取得(比較時に利用)
    function getOutgoData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } else {
            $query .= "     T1.OUTGO_CANCEL ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.KOUNYU_NO = '{$model->getRequestNo}' ";
        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.KOUNYU_L_CD || T1.KOUNYU_M_CD || T1.KOUNYU_S_CD AS KOUNYU_L_M_S_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.KOUNYU_L_CD = '{$model->getKounyuLcd}' ";
        $query .= "     AND T1.KOUNYU_L_CD || T1.KOUNYU_M_CD = '{$model->getKounyuLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        //$query .= "     AND T1.KOUNYU_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $query .= "     AND T1.LINE_NO = ".$model->getLineNo." ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.KOUNYU_L_CD, ";
        $query .= "     T1.KOUNYU_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //購入科目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.LEVY_L_CD = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getKounyuLMcd}' ";

        return $query;
    }

    //購入細目
    function getLevySDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD || ':' || T1.LEVY_S_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= "     INNER JOIN LEVY_M_MST L1 ON L1.LEVY_L_CD = T1.LEVY_L_CD ";
        $query .= "                             AND L1.LEVY_M_CD = T1.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getKounyuLMcd}' ";

        return $query;
    }

    //明細データの有無確認
    function getMeisaiData($model, $kounyuscd, $flg="") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.KOUNYU_L_CD || T1.KOUNYU_M_CD = '{$model->getKounyuLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND T1.KOUNYU_S_CD = '".$kounyuscd."' ";
        if ($flg) {
            $query .= "     AND T1.LINE_NO <> ".$model->getLineNo." ";
        }
        return $query;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        
        $kounyulcd = substr($model->getKounyuLMcd, 0, 2);
        $kounyumcd = substr($model->getKounyuLMcd, 2, 2);
        $kounyuscd = substr($model->field["KOUNYU_L_M_S_CD"], 4, 2);
        
        $countMeisai = $db->getOne(knjmp920_meisaiQuery::getMeisaiData($model, $kounyuscd, ""));
        //明細データをセット
        $data = array();
        $data["LINE_NO"][NUMBER]                = $model->getLineNo;
        $data["COMMODITY_PRICE"][NUMBER]        = $model->field["COMMODITY_PRICE"];
        $data["COMMODITY_CNT"][NUMBER]          = $model->field["COMMODITY_CNT"];
        $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = $model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"];
        $data["TOTAL_TAX"][NUMBER]              = floor(($model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"]) * 0.08);
        $data["TOTAL_PRICE"][NUMBER]            = floor(($model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"]) * 1.08);
        $data["REMARK"][TEXT]                   = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";
        if ($countMeisai == 0) {
            $data["YEAR"][TEXT]             = $model->year;
            $data["KOUNYU_L_CD"][TEXT]       = $kounyulcd;
            $data["KOUNYU_M_CD"][TEXT]       = $kounyumcd;
            $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
            $data["KOUNYU_S_CD"][TEXT]       = $kounyuscd;
            $query = Query::insertSQL($data, "LEVY_REQUEST_KOUNYU_MEISAI_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= " AND KOUNYU_L_CD || KOUNYU_M_CD = '{$model->getKounyuLMcd}' ";
            $where .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
            $where .= " AND KOUNYU_S_CD = '".$kounyuscd."' ";
            //$where .= " AND LINE_NO = ".$model->getLineNo." ";
            $query = Query::updateSQL($data, "LEVY_REQUEST_KOUNYU_MEISAI_DAT", $where);
            $db->query($query);
        }
        
        $db->commit();
        return;
    }
    
    //UPDATE
    function &getDeleteQuery($db, $model) {
        $db->autoCommit(false);

        $kounyuscd = substr($model->field["KOUNYU_L_M_S_CD"], 4, 2);

        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND KOUNYU_L_CD = '{$model->getKounyuLcd}' ";
        $query .= " AND KOUNYU_M_CD = '{$model->getKounyuMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " AND KOUNYU_S_CD = '".$kounyuscd."' ";
        //$query .= " AND LINE_NO = ".$model->getLineNo." ";
        $db->query($query);

        $db->commit();
        return;
    }
}
?>

