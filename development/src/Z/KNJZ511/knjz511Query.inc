<?php

require_once('for_php7.php');

class knjz511Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_TRAINING_MST ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD ";

        return $query;
    }

    //CHALLENGED_TRAINING_MSTから１レコード取得
    function getChallengedTrainingMst($namecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_TRAINING_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD = '".$namecd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAMECD"][TEXT]               = $model->field["NAMECD"];
        $data["TRAINING_CONTENTS"][TEXT]    = $model->field["TRAINING_CONTENTS"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_TRAINING_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["TRAINING_CONTENTS"][TEXT]    = $model->field["TRAINING_CONTENTS"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     NAMECD = '".$model->field["NAMECD"]."' ";

        $query = Query::updateSQL($data, "CHALLENGED_TRAINING_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_TRAINING_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD = '".$model->field["NAMECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
