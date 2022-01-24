<?php

require_once('for_php7.php');

class knjz518Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHECK_CENTER_MST ";
        $query .= " ORDER BY ";
        $query .= "     CENTERCD ";

        return $query;
    }

    //CHECK_CENTER_MSTから１レコード取得
    function getInspectionCenterMst($centercd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHECK_CENTER_MST ";
        $query .= " WHERE ";
        $query .= "     CENTERCD = '".$centercd."' ";

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
        $data["CENTERCD"][TEXT]         = $model->field["CENTERCD"];
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["ABBV"][TEXT]             = $model->field["ABBV"];
        $data["AREACD"][TEXT]           = $model->field["AREACD"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]            = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]            = $model->field["ADDR2"];
        $data["ADDR3"][TEXT]            = $model->field["ADDR3"];
        $data["AREA_LOCAL"][TEXT]       = $model->field["AREA_LOCAL"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["FAXNO"][TEXT]            = $model->field["FAXNO"];
        $data["HOME_PAGE"][TEXT]        = $model->field["HOME_PAGE"];
        $data["EMAIL"][TEXT]            = $model->field["EMAIL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "CHECK_CENTER_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["ABBV"][TEXT]             = $model->field["ABBV"];
        $data["AREACD"][TEXT]           = $model->field["AREACD"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]            = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]            = $model->field["ADDR2"];
        $data["ADDR3"][TEXT]            = $model->field["ADDR3"];
        $data["AREA_LOCAL"][TEXT]       = $model->field["AREA_LOCAL"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["FAXNO"][TEXT]            = $model->field["FAXNO"];
        $data["HOME_PAGE"][TEXT]        = $model->field["HOME_PAGE"];
        $data["EMAIL"][TEXT]            = $model->field["EMAIL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     CENTERCD    = '".$model->field["CENTERCD"]."' ";

        $query = Query::updateSQL($data, "CHECK_CENTER_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHECK_CENTER_MST ";
        $query .= " WHERE ";
        $query .= "     CENTERCD    = '".$model->field["CENTERCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
