<?php

require_once('for_php7.php');

class knjz527Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHECK_NAME_MST ";
        $query .= " ORDER BY ";
        $query .= "     CHECK_CD ";

        return $query;
    }

    //CHECK_NAME_MSTから１レコード取得
    function getCheckNameMst($checkcd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHECK_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     CHECK_CD = '".$checkcd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CHECK_CD"][TEXT]       = $model->field["CHECK_CD"];
        $data["CHECK_NAME"][TEXT]         = $model->field["CHECK_NAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "CHECK_NAME_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CHECK_NAME"][TEXT]         = $model->field["CHECK_NAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     CHECK_CD = '".$model->field["CHECK_CD"]."' ";

        $query = Query::updateSQL($data, "CHECK_NAME_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHECK_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     CHECK_CD = '".$model->field["CHECK_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
