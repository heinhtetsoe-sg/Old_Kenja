<?php

require_once('for_php7.php');

class knje462Query extends Query {
    //項目名称取得(名称M取得)
    function getCompositionTypeSmplList($ncd1, $cutcode=false, $ncd2="") {
        $query  = " SELECT ";
        if ($cutcode) {
            $query .= "     NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        } else {
        $query  = " SELECT ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 || '-' || VALUE(NAMESPARE1, '') || '-' || VALUE(NAMESPARE3, '') AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$ncd1."' ";
        if ($ncd2 != "") {
            $query .= "     AND NAMECD2 = '".$ncd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE(NAMECD2, '99') ";

        return $query;
    }

    function getChallengedSupportBaseInfoYmst($model, $cd="") {
        $query  = " SELECT ";
        $query .= "     YDAT.* ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_SUPPORT_BASE_INFO_YMST YDAT ";
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
        $query .= "   CHALLENGED_SUPPORT_BASE_INFO_YMST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";
        if ($seq != "") {
            $query .= "   AND SPRT_SEQ = '".$seq."' ";
        }
        return $query;
    }

    //更新
    function &getInsertQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        knje462Query::getDeleteQuery($model, $db);

        for ($cnt = 1;$cnt <= $model->compcnt;$cnt++) {
            //更新
            //CHALLENGED_SUPPORT_BASE_INFO_YMST
            $gkcgycnt = $db->getOne(knje462Query::getCntChallengedSupportBaseInfoYmst(CTRL_YEAR, $cnt));
            $data = array();
            if ($gkcgycnt > 0) {
                $data["BASE_TITLE"][TEXT] = $model->field["BASETITLENAME_".$cnt];
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";
                $where  = " WHERE ";
                $where .= "   YEAR = '".CTRL_YEAR."' ";
                $where .= "   AND SPRT_SEQ = '".sprintf("%02d", $cnt)."' ";
                $query = Query::updateSQL($data, "CHALLENGED_SUPPORT_BASE_INFO_YMST", $where);
            } else {
                $data["YEAR"][TEXT]       = CTRL_YEAR;
                $data["SPRT_SEQ"][TEXT]   = sprintf("%02d", $cnt);
                $data["BASE_TITLE"][TEXT] = $model->field["BASETITLENAME_".$cnt];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "CHALLENGED_SUPPORT_BASE_INFO_YMST");
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model, $db) {

        // CHALLENGED_SUPPORT_BASE_INFO_YMST
        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_SUPPORT_BASE_INFO_YMST ";
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
        $data["BASE_TITLE"][TEXT] = $Row["BASE_TITLE"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $query = Query::insertSQL($data, $table);

        return $query;
    }
}
?>
