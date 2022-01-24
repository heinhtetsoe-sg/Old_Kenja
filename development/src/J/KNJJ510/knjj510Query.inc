<?php

require_once('for_php7.php');

class knjj510Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SPORTS_ITEM_MST ";
        $query .= " ORDER BY ";
        $query .= "     ITEMCD ";

        return $query;
    }

    //SPORTS_ITEM_MSTから１レコード取得
    function getSportsItemMst($itemcd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SPORTS_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$itemcd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data["ITEMCD"][TEXT]       = $model->field["ITEMCD"];
        $data["ITEMNAME"][TEXT]     = $model->field["ITEMNAME"];
        $data["ITEMABBV"][TEXT]     = $model->field["ITEMABBV"];
        $data["UNIT"][TEXT]         = $model->field["UNIT"];
        $data["SHOWORDER"][NUMBER]  = $model->field["SHOWORDER"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "SPORTS_ITEM_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ITEMNAME"][TEXT]     = $model->field["ITEMNAME"];
        $data["ITEMABBV"][TEXT]     = $model->field["ITEMABBV"];
        $data["UNIT"][TEXT]         = $model->field["UNIT"];
        $data["SHOWORDER"][NUMBER]  = $model->field["SHOWORDER"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ITEMCD = '".$model->field["ITEMCD"]."' ";

        $query = Query::updateSQL($data, "SPORTS_ITEM_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SPORTS_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$model->field["ITEMCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
