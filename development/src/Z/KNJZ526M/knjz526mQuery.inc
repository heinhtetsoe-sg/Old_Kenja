<?php

require_once('for_php7.php');

class knjz526mQuery extends Query {

    //名称マスタ取得
    function getNameMst($nameCd1, $nameCd2) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "   , NAMECD2 AS VALUE ";
        $query .= "   , NAME1 ";
        $query .= "   , NAMESPARE1 ";
        $query .= "   , NAMESPARE3 ";
        $query .= "   , ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$nameCd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE(NAMECD2, '99') ";
        return $query;
    }

    //データ取得
    function getChallengedSupportplanKindNameDat($year, $kindNo = "") {
        $query .= " SELECT ";
        $query .= "     KIND_NO, ";
        $query .= "     KIND_SEQ, ";
        $query .= "     KIND_NAME, ";
        $query .= "     STATUS_NAME1, ";
        $query .= "     STATUS_NAME2, ";
        $query .= "     STATUS_NAME3, ";
        $query .= "     STATUS_NAME4 ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($kindNo) {
            $query .= "     AND KIND_NO = '{$kindNo}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     KIND_NO, ";
        $query .= "     KIND_SEQ ";
        return $query;
    }
    //更新
    function &getInsertQuery($model) {
        //削除
        knjz526mQuery::getDeleteQuery($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["KIND_NO"][TEXT]          = $model->kindNo;
        $data["KIND_SEQ"][TEXT]         = "000";
        $statusList = $model->pattern[$model->kindNo];
        for ($i=0; $i < get_count($statusList); $i++) { 
            $colnum = $statusList[$i];
            $data[$colnum["COLNUM_NAME"]][TEXT] = $model->field[$colnum["COLNUM_NAME"]];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT");
        $db->query($query);

        //更新
        if ($model->field["SETCNT"] > 0) {
            for ($i = 1; $i <= $model->field["SETCNT"]; $i++) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["KIND_NO"][TEXT]          = $model->kindNo;
                $data["KIND_SEQ"][TEXT]         = sprintf("%03d", $i);
                $data["KIND_NAME"][TEXT]        = $model->field["KIND_NAME".$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT");
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
        $query .= "     CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND KIND_NO = '".$model->kindNo."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //コピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();

        //前年度のデータ取得
        $query = knjz526mQuery::getChallengedSupportplanKindNameDat(CTRL_YEAR-1, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["KIND_NO"][TEXT]          = $row["KIND_NO"];
            $data["KIND_SEQ"][TEXT]         = $row["KIND_SEQ"];
            $data["KIND_NAME"][TEXT]        = $row["KIND_NAME"];
            $data["STATUS_NAME1"][TEXT]     = $row["STATUS_NAME1"];
            $data["STATUS_NAME2"][TEXT]     = $row["STATUS_NAME2"];
            $data["STATUS_NAME3"][TEXT]     = $row["STATUS_NAME3"];
            $data["STATUS_NAME4"][TEXT]     = $row["STATUS_NAME4"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>
