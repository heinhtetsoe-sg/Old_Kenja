<?php

require_once('for_php7.php');

class knjz523Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_CARD_NAME_MST ";
        $query .= " ORDER BY ";
        $query .= "     CARDNAME_CD ";

        return $query;
    }

    //CHALLENGED_CARD_NAME_MSTから１レコード取得
    function getChallengedCardNameMst($namecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_CARD_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     CARDNAME_CD = '".$namecd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CARDNAME_CD"][TEXT]          = $model->field["CARDNAME_CD"];
        $data["CARDNAME"][TEXT]             = $model->field["CARDNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_CARD_NAME_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CARDNAME"][TEXT]             = $model->field["CARDNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     CARDNAME_CD = '".$model->field["CARDNAME_CD"]."' ";

        $query = Query::updateSQL($data, "CHALLENGED_CARD_NAME_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_CARD_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     CARDNAME_CD = '".$model->field["CARDNAME_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
