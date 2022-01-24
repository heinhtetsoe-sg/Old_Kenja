<?php

require_once('for_php7.php');

class knjz081Query extends Query {

    //リストデータ取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     EDBOARD_SCHOOLCD ";

        return $query;
    }

    //教育委員会チェック
    function checkEdboard() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        $edboard = $db->getOne($query);
        Query::dbCheckIn($db);
        return $edboard;
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM EDBOARD_SCHOOL_MST ".
                 " WHERE EDBOARD_SCHOOLCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["EDBOARD_SCHOOLCD"][TEXT]     = $fields["EDBOARD_SCHOOLCD"];
        $data["EDBOARD_SCHOOLNAME"][TEXT]   = $fields["EDBOARD_SCHOOLNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "EDBOARD_SCHOOL_MST");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["EDBOARD_SCHOOLCD"][TEXT]     = $fields["EDBOARD_SCHOOLCD"];
        $data["EDBOARD_SCHOOLNAME"][TEXT]   = $fields["EDBOARD_SCHOOLNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $where = " WHERE EDBOARD_SCHOOLCD   = '" .$fields["EDBOARD_SCHOOLCD"]."'";

        $query = Query::updateSQL($data, "EDBOARD_SCHOOL_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM EDBOARD_SCHOOL_MST "
                ." WHERE EDBOARD_SCHOOLCD  = '" .$fields["EDBOARD_SCHOOLCD"] ."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
