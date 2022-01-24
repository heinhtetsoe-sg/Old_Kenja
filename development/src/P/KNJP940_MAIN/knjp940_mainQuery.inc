<?php

require_once('for_php7.php');

class knjp940_mainQuery extends Query {

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
        $query .= "     AND T1.SEISAN_NO = '{$model->getRequestNo}' ";
        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     T1.SEISAN_L_CD || T1.SEISAN_M_CD AS SEISAN_L_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_SEISAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEISAN_L_CD = '{$model->getSeisanLcd}' ";
        $query .= "     AND T1.SEISAN_L_CD || T1.SEISAN_M_CD = '{$model->getSeisanLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEISAN_L_CD, ";
        $query .= "     T1.SEISAN_M_CD, ";
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
        
    //精算項目
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
        $query .= " AND T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.LEVY_IN_OUT_DIV = '2' ";
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
        $query .= "     LEVY_REQUEST_SEISAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";

        return $query;
    }

    //削除
    function &getDeleteQuery($db, $model) {
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_SEISAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND SEISAN_L_CD = '{$model->getSeisanLcd}' ";
        $query .= " AND SEISAN_M_CD = '{$model->getSeisanMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);
        
        //支出伺いデータの精算伝票の番号をNULL更新
        $data = array();
        $data["SEISAN_NO"][TEXT]            = "";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $where  = " WHERE ";
        $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $where .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $where .= " AND YEAR = '{$model->year}' ";
        $where .= " AND OUTGO_L_CD || OUTGO_M_CD = '{$model->getSeisanLMcd}' ";
        $where .= " AND REQUEST_NO  = '{$model->getOutgoRequestNo}' ";
        
        $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
        $db->query($query);

        $db->commit();
        return;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_SEISAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND SEISAN_L_CD = '{$model->getSeisanLcd}' ";
        $query .= " AND SEISAN_M_CD = '{$model->getSeisanMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $db->query($query);

        $seisanlcd = substr($model->field["SEISAN_L_M_CD"], 0, 2);
        $seisanmcd = substr($model->field["SEISAN_L_M_CD"], 2, 2);

        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["SEISAN_L_CD"][TEXT]      = $seisanlcd;
        $data["SEISAN_M_CD"][TEXT]      = $seisanmcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["REQUEST_DATE"][DATE]     = str_replace("/","-",$model->field["REQUEST_DATE"]);
        $data["REQUEST_STAFF"][TEXT]    = $model->field["REQUEST_STAFF"];
        
        $data["SEISAN_TITLE"][TEXT]         = $model->field["SEISAN_TITLE"];
        $data["GENKIN_JURYOU_STAFF"][TEXT]  = $model->field["GENKIN_JURYOU_STAFF"];
        $data["SEISAN_NAIYOU"][TEXT]        = $model->field["SEISAN_NAIYOU"];
        $data["JURYOU_DATE"][DATE]          = str_replace("/","-",$model->field["JURYOU_DATE"]);
        $data["JURYOU_GK"][NUMBER]          = $model->field["JURYOU_GK"];
        $data["SIHARAI_DATE"][DATE]         = str_replace("/","-",$model->field["SIHARAI_DATE"]);
        $data["SIHARAI_GK"][NUMBER]         = $model->field["SIHARAI_GK"];
        $data["ZAN_GK"][NUMBER]             = $model->field["ZAN_GK"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["SUITOU_STAFF"][TEXT]         = $model->field["SUITOU_STAFF"];
        $data["INCOME_DATE"][DATE]          = str_replace("/","-",$model->field["INCOME_DATE"]);
        if ($model->field["INCOME_DATE"]) {
            $data["SEISAN_APPROVAL"][TEXT]           = "1";
        } else {
            $data["SEISAN_APPROVAL"][TEXT]           = "";
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_SEISAN_DAT");
        $db->query($query);

        //支出伺いデータの精算伝票を更新
        $data = array();
        $data["SEISAN_NO"][TEXT]            = $model->getRequestNo;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $where  = " WHERE ";
        $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $where .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $where .= " AND YEAR = '{$model->year}' ";
        $where .= " AND OUTGO_L_CD || OUTGO_M_CD = '{$model->getSeisanLMcd}' ";
        $where .= " AND REQUEST_NO  = '{$model->getOutgoRequestNo}' ";

        $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
        $db->query($query);

        $db->commit();
        return;
    }
    
}
?>