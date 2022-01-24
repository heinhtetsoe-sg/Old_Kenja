<?php

require_once('for_php7.php');

class knjj201_2Query extends Query {

    //リスト
    function getList()
    {
        $query  = " SELECT * FROM GUARDIAN_COMMITTEE_MST ";
        $query .= " ORDER BY DIV, EXECUTIVECD ";
        return $query;

    }

    //１レコード取得
    function getRow($div, $executivecd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM GUARDIAN_COMMITTEE_MST ".
                 " WHERE DIV = '".$div."' and EXECUTIVECD = '" .$executivecd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //区分
    function getDiv()
    {
        $db = Query::dbCheckOut();

        $query = "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1='J006' ORDER BY NAMECD2";
        $result    = $db->query($query);
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        return $opt;
    }

    //年度データの確認
    function IsExisting($div, $executivecd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM GUARDIAN_COMMITTEE_YDAT WHERE DIV = '".$div."' and EXECUTIVECD = '".$executivecd."'";
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

        $data["DIV"][TEXT]              = $fields["DIV"];
        $data["EXECUTIVECD"][TEXT]      = $fields["EXECUTIVECD"];
        $data["NAME"][TEXT]             = $fields["NAME"];
        $data["ABBV"][TEXT]             = $fields["ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "GUARDIAN_COMMITTEE_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["DIV"][TEXT]              = $fields["DIV"];
        $data["EXECUTIVECD"][TEXT]      = $fields["EXECUTIVECD"];
        $data["NAME"][TEXT]             = $fields["NAME"];
        $data["ABBV"][TEXT]             = $fields["ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE DIV = '" .$fields["DIV"]."' and EXECUTIVECD = '" .$fields["EXECUTIVECD"]."'";

        $query = Query::updateSQL($data, "GUARDIAN_COMMITTEE_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM GUARDIAN_COMMITTEE_MST "
                ." WHERE DIV  = '" .$fields["DIV"]."' and EXECUTIVECD  = '" .$fields["EXECUTIVECD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
