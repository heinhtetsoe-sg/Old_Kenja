<?php

require_once('for_php7.php');

class knjz526nQuery extends Query {

    //データ取得
    function selectQuery() {
        $query  = " SELECT ";
        $query .= "     SELF_DIV ";
        $query .= "     , SELF_TITLE ";
        $query .= "     , SELF_ITEM ";
        $query .= "     , SELF_CONTENT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    function checkSelfDiv($selfDiv) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SELF_DIV = '".$selfDiv."' ";

        return $query;
    }

    function getRow($selfDiv, $selfSeq = "") {

        $query  = " SELECT ";
        $query .= "     SELF_DIV ";
        $query .= "     , SELF_TITLE ";
        $query .= "     , SELF_CONTENT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SELF_DIV = '".$selfDiv."' ";
        if ($selfSeq) {
            $query .= "     AND SELF_SEQ = '".$selfSeq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SELF_SEQ ";

       return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 1; $i <= $model->field["ITEM_CNT"]; $i++) {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SELF_DIV"][TEXT]         = $model->field["SELF_DIV"];
            $data["SELF_SEQ"][TEXT]         = $i;
            $data["SELF_TITLE"][TEXT]       = $model->field["SELF_TITLE"];
            $data["SELF_ITEM"][TEXT]        = "(".$i.")";
            $data["SELF_CONTENT"][TEXT]     = $model->field["SELF_CONTENT_".$i];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SELFRELIANCE_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SELF_DIV = '".$model->field["SELF_DIV"]."' ";

        $db->query($query);

        for ($i = 1; $i <= $model->field["ITEM_CNT"]; $i++) {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SELF_DIV"][TEXT]         = $model->field["SELF_DIV"];
            $data["SELF_SEQ"][TEXT]         = $i;
            $data["SELF_TITLE"][TEXT]       = $model->field["SELF_TITLE"];
            $data["SELF_ITEM"][TEXT]        = "(".$i.")";
            $data["SELF_CONTENT"][TEXT]     = $model->field["SELF_CONTENT_".$i];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SELFRELIANCE_MST");
            $db->query($query);
        }


        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SELF_DIV = '".$model->field["SELF_DIV"]."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック(HREPORT_GUIDANCE_SELFRELIANCE_MST)
    function CheckData($year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' ";
        $query .= "     , SELF_DIV ";
        $query .= "     , SELF_SEQ ";
        $query .= "     , SELF_TITLE ";
        $query .= "     , SELF_ITEM ";
        $query .= "     , SELF_CONTENT ";
        $query .= "     , '".STAFFCD."' ";
        $query .= "     , sysdate() ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
