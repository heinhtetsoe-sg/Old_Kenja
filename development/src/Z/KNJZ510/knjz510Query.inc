<?php

require_once('for_php7.php');

class knjz510Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_NAME_MST ";
        $query .= " ORDER BY ";
        $query .= "     CHALLENGECD ";

        return $query;
    }

    //CHALLENGED_NAME_MSTから１レコード取得
    function getChallengedNameMst($challengecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     CHALLENGECD = '".$challengecd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CHALLENGECD"][TEXT]  = $model->field["CHALLENGECD"];
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["ABBV"][TEXT]         = $model->field["ABBV"];
        $data["POPULAR_NAME"][TEXT] = $model->field["POPULAR_NAME"];
        $data["REMARK"][TEXT]       = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_NAME_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["ABBV"][TEXT]         = $model->field["ABBV"];
        $data["POPULAR_NAME"][TEXT] = $model->field["POPULAR_NAME"];
        $data["REMARK"][TEXT]       = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     CHALLENGECD = '".$model->field["CHALLENGECD"]."' ";

        $query = Query::updateSQL($data, "CHALLENGED_NAME_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     CHALLENGECD = '".$model->field["CHALLENGECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
