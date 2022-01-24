<?php

require_once('for_php7.php');

class knjh170Query extends Query {

    //１レコード取得
    function getRow($cd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GO_HOME_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     GO_HOME_GROUP_NO = '{$cd}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();
        
        $data["GO_HOME_GROUP_NO"][TEXT]   = sprintf("%02d",$fields["GO_HOME_GROUP_NO"]);
        $data["GO_HOME_GROUP_NAME"][TEXT] = $fields["GO_HOME_GROUP_NAME"];
        $data["REMARK1"][TEXT]            = $fields["REMARK1"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "GO_HOME_GROUP_MST");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["GO_HOME_GROUP_NO"][TEXT]     = sprintf("%02d",$fields["GO_HOME_GROUP_NO"]);
        $data["GO_HOME_GROUP_NAME"][TEXT]   = $fields["GO_HOME_GROUP_NAME"];
        $data["MEETING_PLACE"][TEXT]        = $fields["MEETING_PLACE"];
        $data["REMARK1"][TEXT]              = $fields["REMARK1"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

          $where = " WHERE GO_HOME_GROUP_NO = '{$fields["GO_HOME_GROUP_NO"]}' ";
        $query = Query::updateSQL($data, "GO_HOME_GROUP_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //--- DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     GO_HOME_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     GO_HOME_GROUP_NO  = '{$fields["GO_HOME_GROUP_NO"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
