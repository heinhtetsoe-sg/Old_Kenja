<?php

require_once('for_php7.php');

class knjh190Query extends Query {

    //１レコード取得
    function getRow($cd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AREA_MST ";
        $query .= " WHERE ";
        $query .= "     AREA_CD = '{$cd}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $data["AREA_CD"][TEXT]       = $fields["AREA_CD"];
        $data["AREA_NAME"][TEXT]     = $fields["AREA_NAME"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $query = Query::insertSQL($data, "AREA_MST");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["AREA_CD"][TEXT]    = $fields["AREA_CD"];
        $data["AREA_NAME"][TEXT]  = $fields["AREA_NAME"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

          $where = " WHERE AREA_CD = '{$fields["AREA_CD"]}' ";
        $query = Query::updateSQL($data, "AREA_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     AREA_MST ";
        $query .= " WHERE ";
        $query .= "     AREA_CD  = '{$fields["AREA_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
