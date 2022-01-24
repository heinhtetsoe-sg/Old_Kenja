<?php

require_once('for_php7.php');

class knjz250_3Query extends Query {

    //全レコード取得
    function getAllRow()
    {
        $query  = " SELECT ";
        $query .= "   T1.CERTIF_DIV, ";
        $query .= "   T1.CERTIF_DIV_NAME, ";
        $query .= "   T2.CERTIF_KINDCD, ";
        $query .= "   T2.KINDNAME, ";
        $query .= "   COUNT(*) over (partition by T1.CERTIF_DIV) AS R_MAX ";
        $query .= " FROM ";
        $query .= "   CERTIF_KIND_DIV_MST T1 ";
        $query .= "   LEFT JOIN CERTIF_KIND_MST T2 ";
        $query .= "      ON T2.CERTIF_DIV = T1.CERTIF_DIV ";
        $query .= " GROUP BY ";
        $query .= "   T1.CERTIF_DIV, ";
        $query .= "   T1.CERTIF_DIV_NAME, ";
        $query .= "   T2.CERTIF_KINDCD, ";
        $query .= "   T2.KINDNAME ";
        $query .= " ORDER BY ";
        $query .= "   T1.CERTIF_DIV, ";
        $query .= "   T2.CERTIF_KINDCD ";

        return $query;
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM CERTIF_KIND_DIV_MST ".
                 " WHERE CERTIF_DIV = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //リストtoリストデータ取得
    //リスト右側
    function getSubclass($model) {
        $query  = " SELECT ";
        $query .= "   CERTIF_KINDCD || ' ' || KINDNAME AS LABEL, ";
        $query .= "   CERTIF_KINDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "   CERTIF_KIND_MST ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";

        return $query;
    }
    //リスト左側
    function getSubclass2($model, $certifdiv) {
        $query  = " SELECT ";
        $query .= "   CERTIF_KINDCD || ' ' || KINDNAME AS LABEL, ";
        $query .= "   CERTIF_KINDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "   CERTIF_KIND_MST ";
        $query .= " WHERE ";
        $query .= "   CERTIF_DIV = '".$certifdiv."' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CERTIF_DIV"][TEXT]        = $fields["CERTIF_DIV"];
        $data["CERTIF_DIV_NAME"][TEXT]   = $fields["CERTIF_DIV_NAME"];
        $data["REMARK1"][TEXT]           = "";
        $data["REMARK2"][TEXT]           = "";
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $query = Query::insertSQL($data, "CERTIF_KIND_DIV_MST");
        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
    //UPDATE
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CERTIF_DIV"][TEXT]        = $fields["CERTIF_DIV"];
        $data["CERTIF_DIV_NAME"][TEXT]   = $fields["CERTIF_DIV_NAME"];
        $data["REMARK1"][TEXT]           = "";
        $data["REMARK2"][TEXT]           = "";
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $where = " WHERE CERTIF_DIV    = '" .$fields["CERTIF_DIV"]."'";
        $query = Query::updateSQL($data, "CERTIF_KIND_DIV_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM CERTIF_KIND_DIV_MST WHERE CERTIF_DIV = '" .$fields["CERTIF_DIV"] ."'";
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &clearCertifDivQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CERTIF_DIV"][TEXT]        = "";
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $where = " WHERE CERTIF_DIV    = '" .$fields["CERTIF_DIV"]."'";
        $query = Query::updateSQL($data, "CERTIF_KIND_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &updateCertifDivQuery($fields, $kindcd)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CERTIF_DIV"][TEXT]        = $fields["CERTIF_DIV"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $where = " WHERE CERTIF_KINDCD = '".$kindcd."' ";
        $query = Query::updateSQL($data, "CERTIF_KIND_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
