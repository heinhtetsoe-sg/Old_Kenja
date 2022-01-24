<?php

require_once('for_php7.php');

class knjm710lQuery extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_L_MST ";
        $query .= " ORDER BY ";
        $query .= "     COLLECT_L_CD ";

        return $query;
    }


    //１レコード取得
    function getRow($collect_l_cd)
    {
       $db = Query::dbCheckOut();

       $query  = " SELECT ";
       $query .= "     * ";
       $query .= " FROM ";
       $query .= "     COLLECT_L_MST ";
       $query .= " WHERE ";
       $query .= "     COLLECT_L_CD = '".$collect_l_cd."'";

       $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
       Query::dbCheckIn($db);
       return $Row;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["COLLECT_L_CD"][TEXT]      = sprintf("%02d",$fields["COLLECT_L_CD"]);
        $data["COLLECT_L_NAME"][TEXT]    = $fields["COLLECT_L_NAME"];
        $data["COLLECT_L_ABBV"][TEXT]    = $fields["COLLECT_L_ABBV"];
        $data["LEVY_FLG"][TEXT]          = $fields["LEVY_FLG"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_L_MST");
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["COLLECT_L_NAME"][TEXT]    = $fields["COLLECT_L_NAME"];
        $data["COLLECT_L_ABBV"][TEXT]    = $fields["COLLECT_L_ABBV"];
        $data["LEVY_FLG"][TEXT]          = $fields["LEVY_FLG"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE COLLECT_L_CD  = '".sprintf("%02d",$fields["COLLECT_L_CD"])."'";

        $query = Query::updateSQL($data, "COLLECT_L_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM COLLECT_L_MST ";
        $query .= " WHERE COLLECT_L_CD  = '".sprintf("%02d",$fields["COLLECT_L_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
