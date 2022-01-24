<?php

require_once('for_php7.php');

class knjmp900_mainQuery extends Query {

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

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     L$i.COMMODITY_NAME AS COMMODITY_NAME$i, ";
            $query .= "     L$i.COMMODITY_CNT AS COMMODITY_CNT$i, ";
            $query .= "     L$i.COMMODITY_PRICE AS COMMODITY_PRICE$i, ";
            $query .= "     L$i.TOTAL_PRICE AS TOTAL_PRICE$i, ";
            $query .= "     L$i.REMARK AS REMARK$i, ";
        }
        $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD AS COLLECT_L_M_S_CD, ";
        $query .= "     T1.INCOME_L_CD || T1.INCOME_M_CD AS INCOME_L_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     LEFT JOIN LEVY_REQUEST_INCOME_MEISAI_DAT L$i ON L$i.YEAR = T1.YEAR ";
            $query .= "                                                AND L$i.INCOME_L_CD = T1.INCOME_L_CD ";
            $query .= "                                                AND L$i.INCOME_M_CD = T1.INCOME_M_CD ";
            $query .= "                                                AND L$i.REQUEST_NO = T1.REQUEST_NO ";
            $query .= "                                                AND L$i.INCOME_S_CD = '".sprintf("%02d", $i)."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     T1.INCOME_M_CD, ";
        $query .= "     T1.REQUEST_NO ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //入金科目、決済の状況を取得(比較時に利用)
    function getLevyData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "COLLECTCD") {
            $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD AS COLLECT_L_M_S_CD ";
        } else if ($flg == "APPROVAL") {
            $query .= "     T1.INCOME_APPROVAL ";
        } else {
            $query .= "     T1.INCOME_CANCEL ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
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
    
    //入金科目
    function getCollectLDiv($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     L1.COLLECT_L_CD || L1.COLLECT_M_CD AS VALUE, ";
        $query .= "     L1.COLLECT_L_CD || L1.COLLECT_M_CD || ':' || L1.COLLECT_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_L_MST T1 ";
        $query .= "     LEFT JOIN COLLECT_M_MST L1 ON L1.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST L2 ON L2.YEAR = L1.YEAR ";
        $query .= "                               AND L2.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "                               AND L2.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '{$model->year}' ";
        $query .= " AND T1.LEVY_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //収入項目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.LEVY_IN_OUT_DIV = '1' ";
        $query .= " AND T1.ZATU_FLG IS NULL  ";
        $query .= " AND T1.YOBI_FLG IS NULL  ";
        $query .= " AND T1.KURIKOSI_FLG IS NULL  ";
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
        $query .= "     LEVY_REQUEST_INCOME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";

        return $query;
    }

    //削除、または作り直しの時
    function &getDeleteQuery($db, $model, $flg="") {
        $db->autoCommit(false);
        
        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= " AND INCOME_M_CD = '{$model->getIncomeMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);
        
        //SCHREG
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= " AND INCOME_M_CD = '{$model->getIncomeMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);
        
        //MAIN
        if ($flg) {
            $query  = " DELETE FROM ";
            $query .= "     LEVY_REQUEST_INCOME_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= " AND INCOME_L_CD = '{$model->getIncomeLcd}' ";
            $query .= " AND INCOME_M_CD = '{$model->getIncomeMcd}' ";
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
        $query .= "     LEVY_REQUEST_INCOME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= " AND INCOME_M_CD = '{$model->getIncomeMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);

        $incomelcd = substr($model->field["INCOME_L_M_CD"], 0, 2);
        $incomemcd = substr($model->field["INCOME_L_M_CD"], 2, 2);
        
        $collectlcd = substr($model->field["COLLECT_L_M_S_CD"], 0, 2);
        $collectmcd = substr($model->field["COLLECT_L_M_S_CD"], 2, 2);
        $collectscd = '00';

        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["INCOME_L_CD"][TEXT]      = $incomelcd;
        $data["INCOME_M_CD"][TEXT]      = $incomemcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["REQUEST_DATE"][DATE]     = str_replace("/","-",$model->field["REQUEST_DATE"]);
        $data["REQUEST_REASON"][TEXT]   = $model->field["REQUEST_REASON"];
        $data["REQUEST_STAFF"][TEXT]    = $model->field["REQUEST_STAFF"];
        $data["REQUEST_GK"][NUMBER]     = $model->field["REQUEST_GK"];
        $data["COLLECT_L_CD"][TEXT]     = $collectlcd;
        $data["COLLECT_M_CD"][TEXT]     = $collectmcd;
        $data["COLLECT_S_CD"][TEXT]     = $collectscd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_DAT");
        $db->query($query);

        $db->commit();
        return;
    }
    
}
?>