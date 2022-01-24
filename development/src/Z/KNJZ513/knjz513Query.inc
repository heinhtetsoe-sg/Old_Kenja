<?php

require_once('for_php7.php');

class knjz513Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDICAL_CARE_NAME_MST ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD ";

        return $query;
    }

    //MEDICAL_CARE_NAME_MSTから１レコード取得
    function getMedicalCareNameMst($namecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDICAL_CARE_NAME_MST ";
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
        $data["NAMECD"][TEXT]       = $model->field["NAMECD"];
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "MEDICAL_CARE_NAME_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     NAMECD = '".$model->field["NAMECD"]."' ";

        $query = Query::updateSQL($data, "MEDICAL_CARE_NAME_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MEDICAL_CARE_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD = '".$model->field["NAMECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
