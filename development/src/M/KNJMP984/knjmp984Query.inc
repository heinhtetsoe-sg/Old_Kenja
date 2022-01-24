<?php

require_once('for_php7.php');

class knjmp984Query extends Query {

    //グループマスタ取得
    function selectQuery()
    {
        $query  = " SELECT ";
        $query .= "     * " ;
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_MST ";
        $query .= " ORDER BY ";
        $query .= "     LEVY_GROUP_CD ";

        return $query;
    }
    
    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     LEVY_GROUP_CD = '" .$cd ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    /**
     * INSERT
     */
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $data["LEVY_GROUP_CD"][TEXT]     = $fields["LEVY_GROUP_CD"];
        $data["LEVY_GROUP_NAME"][TEXT]   = $fields["LEVY_GROUP_NAME"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_GROUP_MST");
        
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["LEVY_GROUP_CD"][TEXT]     = $fields["LEVY_GROUP_CD"];
        $data["LEVY_GROUP_NAME"][TEXT]   = $fields["LEVY_GROUP_NAME"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";
        
        $where = "WHERE LEVY_GROUP_CD = '" .$fields["LEVY_GROUP_CD"] ."'";
        $query = Query::updateSQL($data, "LEVY_GROUP_MST", $where);
        
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    
    //設定している会計科目を取得
    function g_data_chk($cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     LEVY_GROUP_CD = '" .$cd ."' ";

        return $query;
    }
    
    /**
     * DELETE
     */
    function &getDeleteQuery($fields)
    {
        //設定している会計科目を取得
        $db = Query::dbCheckOut();
        $getCount = $db->getOne(knjmp984Query::g_data_chk($fields["LEVY_GROUP_CD"]));
        if ($getCount == 0) {
            $query  = " DELETE FROM ";
            $query .= "     LEVY_GROUP_MST ";
            $query .= " WHERE ";
            $query .= "     LEVY_GROUP_CD = '" .$fields["LEVY_GROUP_CD"] ."' ";

            $db->query($query);
            $result = true;
        } else {
            $result = false;
        } 
        
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
