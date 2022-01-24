<?php

require_once('for_php7.php');

class knjz140Query extends Query {

    //リスト表示
    function getList($model)
    {
        $query = "SELECT * FROM MESSAGE_MST ORDER BY MSG_CD";
        return $query;
    }
    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT SUBSTR(MSG_CD,4,3) as MSG_CD, ";
        $query .= "       MSG_LEVEL,MSG_CONTENT, ";
        $query .= "       MSG_DETAIL,HOWTO,REGISTERCD,UPDATED ";
        $query .= "FROM MESSAGE_MST ";
        $query .= "WHERE MSG_CD = '" .$cd ."'";
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
        
        $data["MSG_CD"][TEXT]       = "MSG".$fields["MSG_CD"];
        $data["MSG_LEVEL"][TEXT]    = $fields["MSG_LEVEL"];
        $data["MSG_CONTENT"][TEXT]  = $fields["MSG_CONTENT"];
        $data["MSG_DETAIL"][TEXT]   = $fields["MSG_DETAIL"];
        $data["HOWTO"][TEXT]        = $fields["HOWTO"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "MESSAGE_MST");
        
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
        
        $data["MSG_CD"][TEXT]       = "MSG".$fields["MSG_CD"];
        $data["MSG_LEVEL"][TEXT]    = $fields["MSG_LEVEL"];
        $data["MSG_CONTENT"][TEXT]  = $fields["MSG_CONTENT"];
        $data["MSG_DETAIL"][TEXT]   = $fields["MSG_DETAIL"];
        $data["HOWTO"][TEXT]        = $fields["HOWTO"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        
        $where = " WHERE MSG_CD = 'MSG" .$fields["MSG_CD"]."'";
        
        $query = Query::updateSQL($data, "MESSAGE_MST", $where);
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

        $query = "DELETE FROM MESSAGE_MST "
                ." WHERE MSG_CD  = 'MSG".$fields["MSG_CD"] ."'";
        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
