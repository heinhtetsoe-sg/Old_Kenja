<?php

require_once('for_php7.php');

class knjz170_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        if ($cd != "")
        {
            $db    = Query::dbCheckOut();
            $query = "SELECT * FROM electclass_mst WHERE GROUPCD = '" .sprintf("%04d",$cd)."'";
            $row   = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            return $row;
        }
        return;
    }
    
    //年度データの確認
    function IsExisting($groupcd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from electclass_ydat where groupcd = '".sprintf("%04d",$groupcd)."'"; 
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
        
        $data["GROUPCD"][TEXT]       = sprintf("%04d",$fields["GROUPCD"]);
        $data["GROUPNAME"][TEXT]     = $fields["GROUPNAME"];
        $data["GROUPABBV"][TEXT]     = $fields["GROUPABBV"];
        $data["REMARK"][TEXT]        = $fields["REMARK"];
        $data["SHOWORDER"][NUMBER]   = $fields["SHOWORDER"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;        
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $query = Query::insertSQL($data, "electclass_mst");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["GROUPCD"][TEXT]       = sprintf("%04d",$fields["GROUPCD"]);
        $data["GROUPNAME"][TEXT]     = $fields["GROUPNAME"];
        $data["GROUPABBV"][TEXT]     = $fields["GROUPABBV"];
        $data["REMARK"][TEXT]        = $fields["REMARK"];
        $data["SHOWORDER"][NUMBER]   = $fields["SHOWORDER"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $where = " WHERE GROUPCD = '" .sprintf("%04d",$fields["GROUPCD"])."'";
        $query = Query::updateSQL($data, "electclass_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM electclass_mst "
                ." WHERE GROUPCD  = '" .sprintf("%04d",$fields["GROUPCD"])."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
