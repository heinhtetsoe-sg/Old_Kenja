<?php

require_once('for_php7.php');

class knjz520Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     TEAM_MEMBER_MST ";
        $query .= " ORDER BY ";
        $query .= "     MEMBERCD ";

        return $query;
    }

    //TEAM_MEMBER_MSTから１レコード取得
    function getTeamMemberMst($membercd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     TEAM_MEMBER_MST ";
        $query .= " WHERE ";
        $query .= "     MEMBERCD = '".$membercd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["MEMBERCD"][TEXT]         = $model->field["MEMBERCD"];
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["ABBV"][TEXT]             = $model->field["ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "TEAM_MEMBER_MST");
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
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     MEMBERCD = '".$model->field["MEMBERCD"]."' ";

        $query = Query::updateSQL($data, "TEAM_MEMBER_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     TEAM_MEMBER_MST ";
        $query .= " WHERE ";
        $query .= "     MEMBERCD    = '".$model->field["MEMBERCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
