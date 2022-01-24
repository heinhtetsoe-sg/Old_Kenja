<?php

require_once('for_php7.php');

class knjl537fQuery extends Query {

    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = " DELETE FROM ENTEXAM_DEVIATION_LEVEL_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->entexamyear."' ";
        $query .= "     AND DEV_CD  = '".$model->field["DEV_CD"]."' ";
        $db->query($query);

        //INSERT -- ENTEXAM_DEVIATION_LEVEL_MST
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->entexamyear;
        $data["DEV_CD"][TEXT]        = $model->field["DEV_CD"];
        $data["DEV_MARK"][TEXT]      = $model->field["DEV_MARK"];
        $data["DEV_LOW"][NUMBER]     = $model->field["DEV_LOW"];
        $data["DEV_HIGH"][NUMBER]    = $model->field["DEV_HIGH"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "SYSDATE()";

        $query = Query::insertSQL($data, "ENTEXAM_DEVIATION_LEVEL_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
