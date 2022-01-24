<?php

require_once('for_php7.php');

class knjz526Query extends Query {
    //データ取得
    function getChallengedSupportplanStatusDat($model, $year, $flg="") {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CHALLENGED_SUPPORTPLAN_STATUS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     DATA_DIV ";
        }

        return $query;
    }

    //更新
    function &getInsertQuery($model) {
        //削除
        knjz526Query::getDeleteQuery($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]         = 0;
        $data["STATUS_NAME"][TEXT]      = $model->field["STATUS_NAME"];
        $data["STATUS2_NAME"][TEXT]     = $model->field["STATUS2_NAME"];
        $data["STATUS3_NAME"][TEXT]     = $model->field["STATUS3_NAME"];
        $data["STATUS4_NAME"][TEXT]     = $model->field["STATUS4_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_SUPPORTPLAN_STATUS_DAT");
        $db->query($query);

        //更新
        if ($model->field["setcnt"] > 0) {
            for ($i = 1; $i <= $model->field["setcnt"]; $i++) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["DATA_DIV"][TEXT]         = $i;
                $data["DATA_DIV_NAME"][TEXT]    = $model->field["DATA_DIV_NAME".$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "CHALLENGED_SUPPORTPLAN_STATUS_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_SUPPORTPLAN_STATUS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //コピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();

        //前年度のデータ取得
        $result = $db->query(knjz526Query::getChallengedSupportplanStatusDat($model, CTRL_YEAR-1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["DATA_DIV"][TEXT]         = $row["DATA_DIV"];
            $data["DATA_DIV_NAME"][TEXT]    = $row["DATA_DIV_NAME"];
            $data["STATUS_NAME"][TEXT]      = $row["STATUS_NAME"];
            $data["STATUS2_NAME"][TEXT]     = $row["STATUS2_NAME"];
            $data["STATUS3_NAME"][TEXT]     = $row["STATUS3_NAME"];
            $data["STATUS4_NAME"][TEXT]     = $row["STATUS4_NAME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "CHALLENGED_SUPPORTPLAN_STATUS_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>
