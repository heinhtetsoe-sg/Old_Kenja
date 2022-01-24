<?php

require_once('for_php7.php');

class knjz400j_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM jviewname_mst ".
                 " WHERE VIEWCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["VIEWCD"][TEXT]           = $fields["VIEWCD"];
        $data["VIEWNAME"][TEXT]         = $fields["VIEWNAME"];
        $data["SHOWORDER"][NUMBER]      = $fields["SHOWORDER"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "jviewname_mst");

        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }
    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["VIEWCD"][TEXT]           = $fields["VIEWCD"];
        $data["VIEWNAME"][TEXT]         = $fields["VIEWNAME"];
        $data["SHOWORDER"][NUMBER]      = $fields["SHOWORDER"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE viewcd         = '" .$fields["VIEWCD"]."'";

        $query = Query::updateSQL($data, "jviewname_mst", $where);

        $db->query($query);
        Query::dbCheckIn($db);

        return ;
    }
    //DELETE
    function &getDeleteQuery($fields)
    {
        if (knjz400j_2Query::year_data_chk($fields["VIEWCD"])==0) {
            $db = Query::dbCheckOut();

            $query = "DELETE FROM jviewname_mst "
                    ." WHERE viewcd  = '". $fields["VIEWCD"] ."'";

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
        $query = "SELECT COUNT(*) AS cnt FROM jviewname_ydat WHERE viewcd = '" .$cd ."'";
        $db->query($query);

        $row = $db->getone($query,null,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
}
?>
