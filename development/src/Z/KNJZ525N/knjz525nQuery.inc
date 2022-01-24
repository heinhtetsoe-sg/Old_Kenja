<?php

require_once('for_php7.php');
class knjz525nQuery extends Query {
    //データ取得
    function getChallengedAssessmentStatusGrowupDat($model, $year, $flg="") {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     INT(DATA_DIV) ";
        }

        return $query;
    }

    //更新
    function &getInsertQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //全項目削除
        knjz525nQuery::getDeleteQuery($db, $model);

        //ヘッダ更新
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]         = '0';
        $data["SHEET_PATTERN"][TEXT]    = $model->field["WINDOW_PTRN"];
        $data["STATUS_NAME"][TEXT]      = $model->field["STATUS_NAME"];
        if ($model->field["WINDOW_PTRN"] == "2") {
            $data["GROWUP_NAME"][TEXT]  =   $model->field["GROWUP_NAME"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT", $where);
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

                $query = Query::insertSQL($data, "CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除(ヘッダ＆項目)
    function &getDeleteQuery($db, $model) {

        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        return;
    }

    //コピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();

        //前年度のデータ取得
        $result = $db->query(knjz525nQuery::getChallengedAssessmentStatusGrowupDat($model, CTRL_YEAR-1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["DATA_DIV"][TEXT]         = $row["DATA_DIV"];
            $data["DATA_DIV_NAME"][TEXT]    = $row["DATA_DIV_NAME"];
            $data["STATUS_NAME"][TEXT]      = $row["STATUS_NAME"];
            if ($model->field["WINDOW_PTRN"] == "2") {
                $data["GROWUP_NAME"][TEXT]      = $row["GROWUP_NAME"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>
