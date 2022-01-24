<?php

require_once('for_php7.php');

class knjz336Query extends Query {
/************ 左画面 ***************/
    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     OTHER_SYSTEM_MST ";
        $query .= " ORDER BY ";
        $query .= "     SYSTEMID ";

        return $query;
    }

/************ 右画面 ***************/
    //１レコード取得
    function getRow($systemId) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     OTHER_SYSTEM_MST ";
        $query .= " WHERE ";
        $query .= "     SYSTEMID = '{$systemId}' ";

        return $query;
    }

/******** 追加更新処理  ********/
    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SYSTEMID"][TEXT]             = $fields["SYSTEMID"];
        $data["SYSTEM_NAME"][TEXT]          = $fields["SYSTEM_NAME"];
        $data["SYSTEM_NAME_ABBV"][TEXT]     = $fields["SYSTEM_NAME_ABBV"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "OTHER_SYSTEM_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SYSTEM_NAME"][TEXT]          = $fields["SYSTEM_NAME"];
        $data["SYSTEM_NAME_ABBV"][TEXT]     = $fields["SYSTEM_NAME_ABBV"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SYSTEMID = '".$fields["SYSTEMID"]."' ";

        $query = Query::updateSQL($data, "OTHER_SYSTEM_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     OTHER_SYSTEM_MST ";
        $query .= " WHERE ";
        $query .= "     SYSTEMID = '".$fields["SYSTEMID"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
