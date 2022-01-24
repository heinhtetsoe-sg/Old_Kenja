<?php

require_once('for_php7.php');

class knjz055_2Query extends Query {
    //１レコード取得
    function getRow($cd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECODE = '" .$cd ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度データの確認
    function IsExisting($coursecode) {
        $db    = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COURSECODE_YDAT ";
        $query .= " WHERE ";
        $query .= "     COURSECODE = '".$coursecode."' ";

        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }
    //--- INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["COURSECODE"][TEXT]           = $fields["COURSECODE"];
        $data["COURSECODENAME"][TEXT]       = $fields["COURSECODENAME"];
        $data["COURSECODEABBV1"][TEXT]      = $fields["COURSECODEABBV1"];
        $data["COURSECODEABBV2"][TEXT]      = $fields["COURSECODEABBV2"];
        $data["COURSECODEABBV3"][TEXT]      = $fields["COURSECODEABBV3"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "COURSECODE_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["COURSECODE"][TEXT]           = $fields["COURSECODE"];
        $data["COURSECODENAME"][TEXT]       = $fields["COURSECODENAME"];
        $data["COURSECODEABBV1"][TEXT]      = $fields["COURSECODEABBV1"];
        $data["COURSECODEABBV2"][TEXT]      = $fields["COURSECODEABBV2"];
        $data["COURSECODEABBV3"][TEXT]      = $fields["COURSECODEABBV3"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where = " WHERE COURSECODE = '" .$fields["COURSECODE"]."'";

        $query = Query::updateSQL($data, "COURSECODE_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECODE = '" .$fields["COURSECODE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
