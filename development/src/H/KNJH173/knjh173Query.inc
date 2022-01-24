<?php

require_once('for_php7.php');

class knjh173Query extends Query {

    //１レコード取得
    function getRow($cd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HOUSE_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     HOUSE_GROUP_CD = '{$cd}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();
        
        $data["HOUSE_GROUP_CD"][TEXT]   = sprintf("%03d",$fields["HOUSE_GROUP_CD"]);
        $data["HOUSE_GROUP_NAME"][TEXT] = $fields["HOUSE_GROUP_NAME"];
        $data["REMARK1"][TEXT]            = $fields["REMARK1"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "HOUSE_GROUP_MST");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["HOUSE_GROUP_CD"][TEXT]     = sprintf("%03d",$fields["HOUSE_GROUP_CD"]);
        $data["HOUSE_GROUP_NAME"][TEXT]   = $fields["HOUSE_GROUP_NAME"];
        $data["REMARK1"][TEXT]              = $fields["REMARK1"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

          $where = " WHERE HOUSE_GROUP_CD = '{$fields["HOUSE_GROUP_CD"]}' ";
        $query = Query::updateSQL($data, "HOUSE_GROUP_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //--- DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HOUSE_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     HOUSE_GROUP_CD  = '{$fields["HOUSE_GROUP_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
