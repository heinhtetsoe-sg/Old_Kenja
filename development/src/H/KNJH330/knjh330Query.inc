<?php

require_once('for_php7.php');

class knjh330Query extends Query {

    //模試種別
    function getMockDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H308' ";
        $query .= "     AND NAMESPARE2 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //リスト
    function getList()
    {
        $query  = " SELECT * FROM MOCK_MST ";
        $query .= " ORDER BY MOCKCD ";
        return $query;

    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM MOCK_MST ".
                 " WHERE MOCKCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["MOCKCD"][TEXT]         = $fields["MOCK_DIV"].$fields["MOCKCD"];
        $data["MOCKNAME1"][TEXT]      = $fields["MOCKNAME1"];
        $data["MOCKNAME2"][TEXT]      = $fields["MOCKNAME2"];
        $data["MOCKNAME3"][TEXT]      = $fields["MOCKNAME3"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]	  = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["MOCKCD"][TEXT]         = $fields["MOCK_DIV"].$fields["MOCKCD"];
        $data["MOCKNAME1"][TEXT]      = $fields["MOCKNAME1"];
        $data["MOCKNAME2"][TEXT]      = $fields["MOCKNAME2"];
        $data["MOCKNAME3"][TEXT]      = $fields["MOCKNAME3"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $where = " WHERE MOCKCD = '" .$fields["MOCK_DIV"].$fields["MOCKCD"]."'";

        $query = Query::updateSQL($data, "MOCK_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM MOCK_MST "
                ." WHERE MOCKCD  = '" .$fields["MOCK_DIV"].$fields["MOCKCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
