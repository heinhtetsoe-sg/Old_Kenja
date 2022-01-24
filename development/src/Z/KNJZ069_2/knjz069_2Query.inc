<?php

require_once('for_php7.php');

class knjz069_2Query extends Query {

    //PYP_ELEMENT_MST データ取得
    function getPypElementMst()
    {
        $query  = " SELECT ";
        $query .= "     ELEMENT_DIV ";
        $query .= "     , ELEMENT_CD ";
        $query .= "     , ELEMENT_NAME ";
        $query .= " FROM ";
        $query .= "     PYP_ELEMENT_MST ";
        $query .= " ORDER BY ";
        $query .= "     ELEMENT_DIV ";
        $query .= "     , ELEMENT_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($element_div, $element_cd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     ELEMENT_DIV ";
        $query .= "     , ELEMENT_CD ";
        $query .= "     , ELEMENT_NAME ";
        $query .= " FROM ";
        $query .= "     PYP_ELEMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ELEMENT_DIV = '".$element_div."' ";
        $query .= "     AND ELEMENT_CD = '".$element_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     ELEMENT_DIV ";
        $query .= "     , ELEMENT_CD ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data = array();

        $data["ELEMENT_DIV"][TEXT]    = $fields["ELEMENT_DIV"];
        $data["ELEMENT_CD"][TEXT]     = $fields["ELEMENT_CD"];
        $data["ELEMENT_NAME"][TEXT]   = $fields["ELEMENT_NAME"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "PYP_ELEMENT_MST");

        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }
    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data = array();

        $data["ELEMENT_NAME"][TEXT]     = $fields["ELEMENT_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE  ELEMENT_DIV     = '".$fields["ELEMENT_DIV"]."' ";
        $where .= "   AND  ELEMENT_CD      = '".$fields["ELEMENT_CD"]."' ";

        $query = Query::updateSQL($data, "PYP_ELEMENT_MST", $where);

        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM PYP_ELEMENT_MST ";
        $query .= " WHERE  ELEMENT_DIV     = '".$fields["ELEMENT_DIV"]."' ";
        $query .= "   AND  ELEMENT_CD      = '".$fields["ELEMENT_CD"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }
}
?>
