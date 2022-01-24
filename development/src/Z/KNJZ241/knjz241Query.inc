<?php

require_once('for_php7.php');

class knjz241Query extends Query {

    //リスト    
    function getList()
    {
        $query  = " SELECT * FROM HEALTH_QUESTION_MST ";
        $query .= " ORDER BY QUESTIONCD ";
        return $query;

    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM HEALTH_QUESTION_MST ".
                 " WHERE QUESTIONCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["QUESTIONCD"][TEXT]       = $fields["QUESTIONCD"];
        $data["CONTENTS"][TEXT]         = $fields["CONTENTS"];
        $data["SORT"][TEXT]             = $fields["SORT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "HEALTH_QUESTION_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["QUESTIONCD"][TEXT]       = $fields["QUESTIONCD"];
        $data["CONTENTS"][TEXT]         = $fields["CONTENTS"];
        $data["SORT"][TEXT]             = $fields["SORT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE QUESTIONCD = '" .$fields["QUESTIONCD"]."'";

        $query = Query::updateSQL($data, "HEALTH_QUESTION_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM HEALTH_QUESTION_MST "
                ." WHERE QUESTIONCD  = '" .$fields["QUESTIONCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
