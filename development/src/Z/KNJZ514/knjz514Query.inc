<?php

require_once('for_php7.php');

class knjz514Query extends Query {

    //区分選択のリスト
    function getCNMst() {
        $query  = " SELECT ";
        $query .= "     CHALLENGECD AS VALUE, ";
        $query .= "     CHALLENGECD || ':' || NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_NAME_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     MPNM.*, ";
        $query .= "     CNM.NAME AS DIVNAME ";
        $query .= " FROM ";
        $query .= "     MEDICAL_PROSTHETICS_NAME_MST MPNM ";
        $query .= "     LEFT JOIN CHALLENGED_NAME_MST CNM ";
        $query .= "      ON MPNM.DIV = CNM.CHALLENGECD ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD ";

        return $query;
    }

    //MEDICAL_PROSTHETICS_NAME_MSTから１レコード取得
    function getMedicalProstheticsNameMst($namecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDICAL_PROSTHETICS_NAME_MST ";
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
        $data["DIV"][TEXT]          = $model->field["DIV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "MEDICAL_PROSTHETICS_NAME_MST");
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
        $data["DIV"][TEXT]          = $model->field["DIV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     NAMECD = '".$model->field["NAMECD"]."' ";

        $query = Query::updateSQL($data, "MEDICAL_PROSTHETICS_NAME_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MEDICAL_PROSTHETICS_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD = '".$model->field["NAMECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
