<?php

require_once('for_php7.php');

class knje463Query extends Query {
    function getChallengedSupportBaseInfoYmst($model, $cd="") {
        $query  = " SELECT ";
        $query .= "     YDAT.* ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_GOAL_YMST YDAT ";
        $query .= " WHERE ";
        $query .= "         YDAT.YEAR     = '".CTRL_YEAR."' ";
        if ($cd != "") {
            $query .= "     AND YDAT.SPRT_SEQ = '".$cd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YDAT.SPRT_SEQ ";

        return $query;
    }

    function getCntChallengedSupportBaseInfoYmst($year, $seq="") {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   CHALLENGED_GOAL_YMST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";
        if ($seq != "") {
            $query .= "   AND SPRT_SEQ = '".$seq."' ";
        }
        return $query;
    }

    //更新
    function &getInsertQuery($model) {
        //削除

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knje463Query::getDeleteQuery($model, $db);

        for ($cnt = 1;$cnt <= $model->compcnt;$cnt++) {
            //更新
            //CHALLENGED_GOAL_YMST
            $gkcgycnt = $db->getOne(knje463Query::getCntChallengedSupportBaseInfoYmst(CTRL_YEAR, $cnt));
            $data = array();
            if ($gkcgycnt > 0) {
                $data["GOAL_TITLE"][TEXT] = $model->field["BASETITLENAME_".$cnt];
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";
                $where  = " WHERE ";
                $where .= "   YEAR = '".CTRL_YEAR."' ";
                $where .= "   AND SPRT_SEQ = '".sprintf("%02d", $cnt)."' ";
                $query = Query::updateSQL($data, "CHALLENGED_GOAL_YMST", $where);
            } else {
                $data["YEAR"][TEXT]       = CTRL_YEAR;
                $data["SPRT_SEQ"][TEXT]   = sprintf("%02d", $cnt);
                $data["GOAL_TITLE"][TEXT] = $model->field["BASETITLENAME_".$cnt];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "CHALLENGED_GOAL_YMST");
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model, $db) {

        // CHALLENGED_GOAL_YMST
        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_GOAL_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";

        $db->query($query);

        return;
    }

    //データの存在チェック（コピー用）
    function chkCopyData($year, $flg, $table) {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //コピー
    function getCopyQuery($Row, $table) {
        $data = array();
        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["SPRT_SEQ"][TEXT]   = $Row["SPRT_SEQ"];
        $data["GOAL_TITLE"][TEXT] = $Row["GOAL_TITLE"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $query = Query::insertSQL($data, $table);

        return $query;
    }
}
?>
