<?php

require_once('for_php7.php');

class knjz151_2Query extends Query {

    //リスト取得
    function getList($model)
    {
        $query  = "SELECT ";
        $query .= "     * ";
        $query .= "FROM ";
        $query .= "     ISSUECOMPANY_MST ";
        $query .= "ORDER BY ";
        $query .= "     ISSUECOMPANYCD";
        return $query;
    }
    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM ISSUECOMPANY_MST ".
                 " WHERE ISSUECOMPANYCD = '" .$cd ."'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //年度データの確認
    function IsExisting($issuecd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM ISSUECOMPANY_YDAT WHERE ISSUECOMPANYCD = '".$issuecd."'"; 
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
        
        $data["ISSUECOMPANYCD"][TEXT]       = $fields["ISSUECOMPANYCD"];
        $data["ISSUECOMPANYNAME"][TEXT]     = $fields["ISSUECOMPANYNAME"];
        $data["ISSUECOMPANYABBV"][TEXT]     = $fields["ISSUECOMPANYABBV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        
        $query = Query::insertSQL($data, "ISSUECOMPANY_MST");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["ISSUECOMPANYCD"][TEXT]       = $fields["ISSUECOMPANYCD"];        
        $data["ISSUECOMPANYNAME"][TEXT]     = $fields["ISSUECOMPANYNAME"];
        $data["ISSUECOMPANYABBV"][TEXT]     = $fields["ISSUECOMPANYABBV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where = "WHERE ISSUECOMPANYCD         = '" .$fields["ISSUECOMPANYCD"] ."'";
        $query = Query::updateSQL($data, "ISSUECOMPANY_MST", $where);

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

        $query = "DELETE FROM ISSUECOMPANY_MST "
                ." WHERE ISSUECOMPANYCD  = '" .$fields["ISSUECOMPANYCD"] ."'";
        $query;
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
?>
