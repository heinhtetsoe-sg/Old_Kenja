<?php

require_once('for_php7.php');

class knjz160_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM FACILITY_MST ".
                 " WHERE FACCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //一覧
    function getList()
    {
        return "select * from FACILITY_MST order by FACCD";
    }
    
    //年度データの確認
    function IsExisting($faccd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from facility_ydat where faccd = '".$faccd."'"; 
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
        
        $data["FACCD"][TEXT]            = $fields["FACCD"];
        $data["FACILITYNAME"][TEXT]     = $fields["FACILITYNAME"];
        $data["FACILITYABBV"][TEXT]     = $fields["FACILITYABBV"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["CHR_CAPACITY"][NUMBER]   = $fields["CHR_CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "FACILITY_MST");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["FACILITYNAME"][TEXT]     = $fields["FACILITYNAME"];
        $data["FACILITYABBV"][TEXT]     = $fields["FACILITYABBV"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["CHR_CAPACITY"][NUMBER]   = $fields["CHR_CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE FACCD = '" .$fields["FACCD"]."'";
        $query = Query::updateSQL($data, "FACILITY_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM FACILITY_MST "
                ." WHERE FACCD  = '" .$fields["FACCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
