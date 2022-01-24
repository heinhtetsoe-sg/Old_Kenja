<?php

require_once('for_php7.php');

class knjj180Query extends Query {

    //１レコード取得
    function getRow($branchcd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BRANCH_MST ";
        $query .= " WHERE ";
        $query .= "     BRANCHCD = '".$branchcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["BRANCHCD"][TEXT]    = $fields["BRANCHCD"];
        $data["BRANCHNAME"][TEXT]  = $fields["BRANCHNAME"];
        $data["ABBV"][TEXT]        = $fields["ABBV"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data, "BRANCH_MST");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["BRANCHCD"][TEXT]    = $fields["BRANCHCD"];
        $data["BRANCHNAME"][TEXT]  = $fields["BRANCHNAME"];
        $data["ABBV"][TEXT]        = $fields["ABBV"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";
        $where = " WHERE BRANCHCD = '" .$fields["BRANCHCD"]."'";

        $query = Query::updateSQL($data, "BRANCH_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM BRANCH_MST "
                ." WHERE BRANCHCD  = '" .$fields["BRANCHCD"] ."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
