<?php

require_once('for_php7.php');

class knjz285_2Query extends Query {

    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        $query .= " ORDER BY  ";
        $query .= "     POSITIONCD ";

        return $query;
    }

    //１レコード取得
    function getRow($positioncd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$positioncd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //年度データの確認
    function IsExisting($positioncd) {
        $db    = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_YDAT ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$positioncd."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //UPDATE 
    function &getUpdateQuery($fields, $div) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($div == "add") {
            $data["POSITIONCD"][TEXT]       = $fields["POSITIONCD"];
        }
        $data["POSITIONNAME"][TEXT]         = $fields["POSITIONNAME"];
        $data["BASE_POSITIONNAME"][TEXT]    = $fields["BASE_POSITIONNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "POSITION_MST");
        } else {
            $where = " WHERE POSITIONCD = '".$fields["POSITIONCD"]."' ";
            $query = Query::updateSQL($data, "POSITION_MST", $where);
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$fields["POSITIONCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
