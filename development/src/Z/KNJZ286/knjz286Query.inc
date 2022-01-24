<?php

require_once('for_php7.php');

class knjz286Query extends Query {

    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        $query .= " ORDER BY  ";
        $query .= "     POSITIONCD ";

        return $query;
    }

    //１レコード取得
    function getRow($positioncd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$positioncd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //学校設定教務主任等コードチェック
    function getEdboardPositionDat($positioncd, $flg="") {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     EDBOARD_POSITION_DAT ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$positioncd."' ";
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
            $data["POSITIONCD"][TEXT]       = $fields["POSITIONCD"];
        }
        $data["POSITIONNAME"][TEXT]         = $fields["POSITIONNAME"];
        $data["BASE_POSITIONNAME"][TEXT]    = $fields["BASE_POSITIONNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "POSITION_MST");
            $db->query($query);
        } else {
            $where = " WHERE POSITIONCD = '".$fields["POSITIONCD"]."' ";
            $query = Query::updateSQL($data, "POSITION_MST", $where);
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
        $query .= "     POSITION_MST ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$fields["POSITIONCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
