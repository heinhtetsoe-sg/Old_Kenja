<?php

require_once('for_php7.php');

class knjz524Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_CERTIF_NAME_MST ";
        $query .= " ORDER BY ";
        $query .= "     CERTIFNAME_CD ";

        return $query;
    }

    //CHALLENGED_CERTIF_NAME_MSTから１レコード取得
    function getChallengedCertifMst($namecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_CERTIF_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     CERTIFNAME_CD = '".$namecd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CERTIFNAME_CD"][TEXT]               = $model->field["CERTIFNAME_CD"];
        $data["CERTIFNAME"][TEXT]    = $model->field["CERTIFNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_CERTIF_NAME_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CERTIFNAME"][TEXT]    = $model->field["CERTIFNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     CERTIFNAME_CD = '".$model->field["CERTIFNAME_CD"]."' ";

        $query = Query::updateSQL($data, "CHALLENGED_CERTIF_NAME_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_CERTIF_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     CERTIFNAME_CD = '".$model->field["CERTIFNAME_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
