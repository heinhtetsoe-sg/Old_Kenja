<?php

require_once('for_php7.php');

class knjz390_2Query extends Query {

    //リスト    
    function getList()
    {
        $query  = " SELECT * FROM STATION_MST ";
        $query .= " ORDER BY STATIONCD ";
        return $query;

    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM STATION_MST ".
                 " WHERE STATIONCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度データの確認
    function IsExisting($stationcd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM STATION_YDAT WHERE STATIONCD = '".$stationcd."'"; 
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["STATIONCD"][TEXT]         = $fields["STATIONCD"];
        $data["STATIONNAME"][TEXT]       = $fields["STATIONNAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "STATION_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["STATIONCD"][TEXT]         = $fields["STATIONCD"];
        $data["STATIONNAME"][TEXT]       = $fields["STATIONNAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE STATIONCD = '" .$fields["STATIONCD"]."'";

        $query = Query::updateSQL($data, "STATION_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM STATION_MST "
                ." WHERE STATIONCD  = '" .$fields["STATIONCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
