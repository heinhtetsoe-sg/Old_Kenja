<?php

require_once('for_php7.php');

class knjz521Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDICAL_DEPARTMENTS_MST ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD ";

        return $query;
    }

    //MEDICAL_DEPARTMENTS_MSTから１レコード取得
    function getMedicalDepartmentsMst($namecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDICAL_DEPARTMENTS_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD = '".$namecd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAMECD"][TEXT]         = $model->field["NAMECD"];
        $data["NAME"][TEXT]           = $model->field["NAME"];
        $data["ABBV"][TEXT]           = $model->field["ABBV"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "MEDICAL_DEPARTMENTS_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["ABBV"][TEXT]             = $model->field["ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     NAMECD = '".$model->field["NAMECD"]."' ";

        $query = Query::updateSQL($data, "MEDICAL_DEPARTMENTS_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MEDICAL_DEPARTMENTS_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD    = '".$model->field["NAMECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
