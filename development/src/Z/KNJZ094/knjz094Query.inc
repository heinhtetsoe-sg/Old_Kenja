<?php

require_once('for_php7.php');

class knjz094Query extends Query {
    //リスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_LOCATION_MST ";
        $query .= " ORDER BY ";
        $query .= "     DISTRICTCD ";

        return $query;
    }

    //１レコード取得
    function getRow($cd) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_LOCATION_MST ";
        $query .= " WHERE ";
        $query .= "     DISTRICTCD = '".$cd."' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["DISTRICTCD"][TEXT]         = $fields["DISTRICTCD"];
        $data["DISTRICT_NAME"][TEXT]      = $fields["DISTRICT_NAME"];
        $data["DISTRICT_NAME_ABBV"][TEXT] = $fields["DISTRICT_NAME_ABBV"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "FINSCHOOL_LOCATION_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["DISTRICTCD"][TEXT]         = $fields["DISTRICTCD"];
        $data["DISTRICT_NAME"][TEXT]      = $fields["DISTRICT_NAME"];
        $data["DISTRICT_NAME_ABBV"][TEXT] = $fields["DISTRICT_NAME_ABBV"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $where = " WHERE DISTRICTCD = '".$fields["DISTRICTCD"]."' ";

        $query = Query::updateSQL($data, "FINSCHOOL_LOCATION_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM FINSCHOOL_LOCATION_MST ";
        $query .= " WHERE ";
        $query .= "     DISTRICTCD = '" .$fields["DISTRICTCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
