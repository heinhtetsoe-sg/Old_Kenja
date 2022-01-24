<?php

require_once('for_php7.php');

class knjtx006Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM edboard_mst ".
                 " WHERE edboardcd = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //教育委員会コードの確認
    function IsExisting($edboardcd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM finschool_mst WHERE edboardcd = '".$edboardcd."'";
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if(is_array($row)){
            return true;
        }
        else{
            return false;
        }
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["EDBOARDCD"][TEXT]   = $fields["EDBOARDCD"];
        $data["EDBOARDNAME"][TEXT] = $fields["EDBOARDNAME"];
        $data["EDBOARDABBV"][TEXT] = $fields["EDBOARDABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data, "edboard_mst");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["EDBOARDCD"][TEXT]   = $fields["EDBOARDCD"];
        $data["EDBOARDNAME"][TEXT] = $fields["EDBOARDNAME"];
        $data["EDBOARDABBV"][TEXT] = $fields["EDBOARDABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";
        $where = " WHERE EDBOARDCD = '" .$fields["EDBOARDCD"]."'";

        $query = Query::updateSQL($data, "edboard_mst", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM edboard_mst "
                ." WHERE edboardcd  = '" .$fields["EDBOARDCD"] ."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
