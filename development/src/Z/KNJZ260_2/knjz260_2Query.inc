<?php

require_once('for_php7.php');

class knjz260_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM dutyshare_mst ".
                 " WHERE dutysharecd = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }
    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["DUTYSHARECD"][TEXT]      = $fields["DUTYSHARECD"];
        $data["SHARENAME"][TEXT]        = $fields["SHARENAME"];
        $data["BASE_SHARENAME"][TEXT]   = $fields["BASE_SHARENAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "dutyshare_mst");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["DUTYSHARECD"][TEXT]      = $fields["DUTYSHARECD"];
        $data["SHARENAME"][TEXT]        = $fields["SHARENAME"];
        $data["BASE_SHARENAME"][TEXT]   = $fields["BASE_SHARENAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE dutysharecd    = '" .$fields["DUTYSHARECD"]."'";
        $query = Query::updateSQL($data, "dutyshare_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    //DELETE
    function &getDeleteQuery($fields)
    {
        if (knjz260_2Query::year_data_chk($fields["DUTYSHARECD"])==0) {
            $db = Query::dbCheckOut();

            $query = "DELETE FROM dutyshare_mst "
                    ." WHERE dutysharecd  = '".$fields["DUTYSHARECD"] ."'";

            $db->query($query);
            Query::dbCheckIn($db);
            $result = true;
        } else {
            $result = false;
        }
        return $result;
    }
    function year_data_chk($cd)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT COUNT(*) AS cnt FROM dutyshare_ydat WHERE dutysharecd = '" .$cd ."'";
        $db->query($query);

        $row = $db->getone($query,null,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
}
?>
