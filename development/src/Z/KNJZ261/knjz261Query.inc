<?php

require_once('for_php7.php');

class knjz261Query extends Query {

    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST ";
        $query .= " ORDER BY  ";
        $query .= "     DUTYSHARECD ";

        return $query;
    }

    //１レコード取得
    function getRow($dutysharecd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST ";
        $query .= " WHERE ";
        $query .= "     DUTYSHARECD = '".$dutysharecd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //学校設定校務分掌部コードチェック
    function getEdboardDutyshareDat($dutysharecd, $flg="") {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     EDBOARD_DUTYSHARE_DAT ";
        $query .= " WHERE ";
        $query .= "     DUTYSHARECD = '".$dutysharecd."' ";
        if ($flg) {
            $query .= "     AND EDBOARD_FLG = '2' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //UPDATE 
    function &getUpdateQuery($fields, $div) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($div == "add") {
            $data["DUTYSHARECD"][TEXT]      = $fields["DUTYSHARECD"];
        }
        $data["SHARENAME"][TEXT]        = $fields["SHARENAME"];
        $data["BASE_SHARENAME"][TEXT]   = $fields["BASE_SHARENAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "DUTYSHARE_MST");
            $db->query($query);
        } else {
            $where = " WHERE DUTYSHARECD  = '".$fields["DUTYSHARECD"]."' ";
            $query = Query::updateSQL($data, "DUTYSHARE_MST", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST ";
        $query .= " WHERE ";
        $query .= "     DUTYSHARECD = '".$fields["DUTYSHARECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
