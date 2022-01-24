<?php

require_once('for_php7.php');

class knjz515Query extends Query {

    //区分選択のリスト
    function getNameMst() {
        $query  = " SELECT ";
        $query  .= "  NAMECD2 AS VALUE, ";
        $query  .= "  NAMECD2 || ':' || NAME1 AS LABEL ";
        $query  .= " FROM ";
        $query  .= "  V_NAME_MST ";
        $query  .= " WHERE ";
        $query  .= "  NAMECD1 = 'E062' ";
        $query  .= "  AND YEAR = '".CTRL_YEAR."' ";
        $query  .= " ORDER BY ";
        $query  .= "  NAMECD2 ";

        return $query;
    }

    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     MDNM.*, ";
        $query .= "     VNM.NAME1 AS DIVNAME ";
        $query .= " FROM ";
        $query .= "     MEDICAL_DAILYWANTS_NAME_MST MDNM ";
        $query .= "     LEFT JOIN V_NAME_MST VNM ";
        $query .= "       ON  VNM.NAMECD1 = 'E062' ";
        $query .= "       AND MDNM.DIV = VNM.NAMECD2 ";
        $query .= "       AND VNM.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD ";

        return $query;
    }

    //MEDICAL_DAILYWANTS_NAME_MSTから１レコード取得
    function getMedicalDailywantsNameMst($namecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDICAL_DAILYWANTS_NAME_MST ";
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

        $query = Query::insertSQL($data, "MEDICAL_DAILYWANTS_NAME_MST");
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

        $query = Query::updateSQL($data, "MEDICAL_DAILYWANTS_NAME_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MEDICAL_DAILYWANTS_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD = '".$model->field["NAMECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
