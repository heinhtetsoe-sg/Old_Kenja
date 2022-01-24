<?php

require_once('for_php7.php');

class knjz270_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM section_mst ".
                 " WHERE SECTIONCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度データと職員年度データの存在確認
    function IsExisting($sectioncd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM section_ydat y, staff_mst m ".
                 " WHERE (y.sectioncd = '".$sectioncd."') OR (m.sectioncd = '".$sectioncd."')";
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    //年度データと職員年度データの区別
    function IsPart($sectioncd)
    {
        $db     = Query::dbCheckOut();
        $query  = "SELECT * FROM section_ydat WHERE sectioncd = '".$sectioncd."'";
        $row    = $db->getRow($query);
        Query::dbCheckIn($db);

        if ($row != "") return 1;
        else            return 2;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["SECTIONCD"][TEXT]                = $fields["SECTIONCD"];
        $data["SECTIONNAME"][TEXT]              = $fields["SECTIONNAME"];
        $data["SECTIONABBV"][TEXT]              = $fields["SECTIONABBV"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        $query = Query::insertSQL($data, "section_mst");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["SECTIONCD"][TEXT]                = $fields["SECTIONCD"];
        $data["SECTIONNAME"][TEXT]              = $fields["SECTIONNAME"];
        $data["SECTIONABBV"][TEXT]              = $fields["SECTIONABBV"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

          $where = " WHERE sectioncd = '" .$fields["SECTIONCD"]."'";
        $query = Query::updateSQL($data, "section_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM section_mst "
                ." WHERE sectioncd  = '" .$fields["SECTIONCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
