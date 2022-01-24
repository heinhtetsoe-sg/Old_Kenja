<?php

require_once('for_php7.php');

class knjz512Query extends Query {

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDICAL_CENTER_MST ";
        $query .= " ORDER BY ";
        $query .= "     CENTERCD ";

        return $query;
    }

    //MEDICAL_CENTER_MSTから１レコード取得
    function getMedicalCenterMst($centercd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDICAL_CENTER_MST ";
        $query .= " WHERE ";
        $query .= "     CENTERCD    = '".$centercd."' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CENTERCD"][TEXT]                 = $model->field["CENTERCD"];
        $data["NAME"][TEXT]                     = $model->field["NAME"];
        $data["ABBV"][TEXT]                     = $model->field["ABBV"];
        $data["ZIPCD"][TEXT]                    = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]                    = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]                    = $model->field["ADDR2"];
        $data["TELNO"][TEXT]                    = $model->field["TELNO"];
        $data["FAXNO"][TEXT]                    = $model->field["FAXNO"];
        $data["MEDICAL_DIRECTOR_NAME"][TEXT]    = $model->field["MEDICAL_DIRECTOR_NAME"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        $query = Query::insertSQL($data, "MEDICAL_CENTER_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAME"][TEXT]                     = $model->field["NAME"];
        $data["ABBV"][TEXT]                     = $model->field["ABBV"];
        $data["ZIPCD"][TEXT]                    = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]                    = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]                    = $model->field["ADDR2"];
        $data["TELNO"][TEXT]                    = $model->field["TELNO"];
        $data["FAXNO"][TEXT]                    = $model->field["FAXNO"];
        $data["MEDICAL_DIRECTOR_NAME"][TEXT]    = $model->field["MEDICAL_DIRECTOR_NAME"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        $where  = " WHERE ";
        $where .= "     CENTERCD    = '".$model->field["CENTERCD"]."' ";

        $query = Query::updateSQL($data, "MEDICAL_CENTER_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MEDICAL_CENTER_MST ";
        $query .= " WHERE ";
        $query .= "     CENTERCD    = '".$model->field["CENTERCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
