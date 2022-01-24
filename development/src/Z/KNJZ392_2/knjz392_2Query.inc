<?php

require_once('for_php7.php');

class knjz392_2Query extends Query {

    //リスト    
    function getList()
    {
        $query  = " SELECT * FROM CHILDCARE_FARE_MST ";
        $query .= " ORDER BY FARE_CD ";
        return $query;

    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM CHILDCARE_FARE_MST ".
                 " WHERE FARE_CD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度データの確認
    function IsExisting($fare_cd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM CHILDCARE_FARE_YDAT WHERE FARE_CD = '".$fare_cd."'"; 
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

        $data["FARE_CD"][TEXT]          = $fields["FARE_CD"];
        $data["FARE"][NUMBER]           = $fields["FARE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "CHILDCARE_FARE_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["FARE_CD"][TEXT]          = $fields["FARE_CD"];
        $data["FARE"][NUMBER]           = $fields["FARE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE FARE_CD = '" .$fields["FARE_CD"]."'";

        $query = Query::updateSQL($data, "CHILDCARE_FARE_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM CHILDCARE_FARE_MST "
                ." WHERE FARE_CD  = '" .$fields["FARE_CD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
