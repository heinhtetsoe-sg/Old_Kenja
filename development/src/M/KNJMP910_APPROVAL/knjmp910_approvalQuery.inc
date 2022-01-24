<?php

require_once('for_php7.php');

class knjmp910_approvalQuery extends Query {

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
    
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
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

    //返金実行状況と決裁の状況を取得(比較時に利用)
    function getLevyData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "HENKIN_APPROVAL") {
            $query .= "     T1.HENKIN_APPROVAL ";
        } else if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } else {
            $query .= "     T1.OUTGO_CANCEL ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        return $query;
    }

    //支出項目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.LEVY_L_CD = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getOutgoLMcd}' ";
        return $query;
    }

    //支出金額
    function getRequestGk($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";

        return $query;
    }

    //受取人氏名
    function getOutgoStaff($model) {
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
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
    
        $outgolcd = substr($model->getOutgoLMcd, 0, 2);
        $outgomcd = substr($model->getOutgoLMcd, 2, 2);
    
        $data = array();
        $data["OUTGO_CHECK1"][TEXT]            = $model->field["OUTGO_CHECK1"];
        $data["OUTGO_CHECK1_DATE"][DATE]       = str_replace("/","-",$model->field["OUTGO_CHECK1_DATE"]);
        $data["OUTGO_CHECK1_STAFF"][TEXT]      = $model->field["OUTGO_CHECK1_STAFF"];
        $data["OUTGO_CHECK2"][TEXT]            = $model->field["OUTGO_CHECK2"];
        $data["OUTGO_CHECK3"][TEXT]            = $model->field["OUTGO_CHECK3"];
        
        $data["OUTGO_DATE"][DATE]              = str_replace("/","-",$model->field["OUTGO_DATE"]);
        $data["OUTGO_EXPENSE_FLG"][TEXT]       = $model->field["OUTGO_EXPENSE_FLG"];
        $data["OUTGO_CERTIFICATE_CNT"][NUMBER] = $model->field["OUTGO_CERTIFICATE_CNT"];

        if ($model->field["OUTGO_APPROVAL_VALUE"] == "") {
            $data["OUTGO_APPROVAL"][TEXT]  = $model->field["OUTGO_APPROVAL"];
        }
        if ($model->field["OUTGO_CANCEL_VALUE"] == "") {
            $data["OUTGO_CANCEL"][TEXT]    = $model->field["OUTGO_CANCEL"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->year}' ";
        $where .= " AND OUTGO_L_CD || OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $where .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
        $db->query($query);
        
        $db->commit();
        return;
    }

}
?>