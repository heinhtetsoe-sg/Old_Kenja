<?php

require_once('for_php7.php');

class knjz517Query extends Query {

    //左フレームのリスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     WELFARE_USE_SERVICE_CENTER_MST ";
        if ($model->areacd != "ALL") {
            $query .= " WHERE ";
            if ($model->areacd == "NULL") {
                $query .= "     VALUE(AREACD,'') = '' ";
            } else {
                $query .= "     AREACD = '".$model->areacd."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     SERVICE_CENTERCD ";
        $query .= "    ,SERVICE_CENTERCD_EDABAN ";

        return $query;
    }

    //WELFARE_USE_SERVICE_CENTER_MSTから１レコード取得
    function getWelfare_useServiceCenterMst($service_centercd, $service_centercd_edaban) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     WELFARE_USE_SERVICE_CENTER_MST ";
        $query .= " WHERE ";
        $query .= "     SERVICE_CENTERCD = '".$service_centercd."' ";
        $query .= " AND SERVICE_CENTERCD_EDABAN = '".$service_centercd_edaban."' ";
        return $query;
    }

    //サービス一覧取得
    function getServiceName() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E039' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //Z010取得
    function getZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //圏域取得
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E040' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SERVICE_CENTERCD"][TEXT] = $model->field["SERVICE_CENTERCD"];
        if ($model->field["SERVICE_CENTERCD_EDABAN"]) {
            $data["SERVICE_CENTERCD_EDABAN"][TEXT] = $model->field["SERVICE_CENTERCD_EDABAN"];
        } else {
            $data["SERVICE_CENTERCD_EDABAN"][TEXT] = '00';
        }
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["ABBV"][TEXT]             = $model->field["ABBV"];
        $data["AREACD"][TEXT]           = $model->field["AREACD"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]            = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]            = $model->field["ADDR2"];
        $data["ADDR3"][TEXT]            = $model->field["ADDR3"];
        $data["COMMISSION_NAME"][TEXT]  = $model->field["COMMISSION_NAME"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["FAXNO"][TEXT]            = $model->field["FAXNO"];
        $data["CHALLENGED_SUPPORT_FLG"][TEXT] = $model->field["CHALLENGED_SUPPORT_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "WELFARE_USE_SERVICE_CENTER_MST");
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        //追加 -- WELFARE_USE_SERVICE_CENTER_MST
        $data = array();
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["ABBV"][TEXT]             = $model->field["ABBV"];
        $data["AREACD"][TEXT]           = $model->field["AREACD"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]            = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]            = $model->field["ADDR2"];
        $data["ADDR3"][TEXT]            = $model->field["ADDR3"];
        $data["COMMISSION_NAME"][TEXT]  = $model->field["COMMISSION_NAME"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["FAXNO"][TEXT]            = $model->field["FAXNO"];
        $data["CHALLENGED_SUPPORT_FLG"][TEXT] = $model->field["CHALLENGED_SUPPORT_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SERVICE_CENTERCD = '".$model->field["SERVICE_CENTERCD"]."' ";
        $where .= " AND SERVICE_CENTERCD_EDABAN = '".$model->field["SERVICE_CENTERCD_EDABAN"]."' ";

        $query = Query::updateSQL($data, "WELFARE_USE_SERVICE_CENTER_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        //削除 -- WELFARE_USE_SERVICE_CENTER_MST
        $query  = " DELETE FROM ";
        $query .= "     WELFARE_USE_SERVICE_CENTER_MST ";
        $query .= " WHERE ";
        $query .= "     SERVICE_CENTERCD = '".$model->field["SERVICE_CENTERCD"]."' ";
        $query .= " AND SERVICE_CENTERCD_EDABAN = '".$model->field["SERVICE_CENTERCD_EDABAN"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }
}
?>
