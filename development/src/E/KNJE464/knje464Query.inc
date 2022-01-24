<?php

require_once('for_php7.php');

class knje464Query extends Query {

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_SUPPORT_FACILITY_YMST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."'";
        $query .= " ORDER BY ";
        $query .= "     SPRT_FACILITY_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $facility_cd) {
       $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_SUPPORT_FACILITY_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."'";
        $query .= "     AND SPRT_FACILITY_CD = '".$facility_cd."'";

       $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
       Query::dbCheckIn($db);
       return $Row;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]               = CTRL_YEAR;
        $data["SPRT_FACILITY_CD"][TEXT]   = $fields["SPRT_FACILITY_CD"];
        $data["SPRT_FACILITY_NAME"][TEXT] = $fields["SPRT_FACILITY_NAME"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_SUPPORT_FACILITY_YMST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->facility_cd != $fields["SPRT_FACILITY_CD"]) {
            $data["SPRT_FACILITY_CD"][TEXT]   = $fields["SPRT_FACILITY_CD"];
        }
        $data["SPRT_FACILITY_NAME"][TEXT] = $fields["SPRT_FACILITY_NAME"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR                 = '".CTRL_YEAR."' ";
        $where .= "     AND SPRT_FACILITY_CD = '".($model->facility_cd)."' ";

        $query = Query::updateSQL($data, "CHALLENGED_SUPPORT_FACILITY_YMST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM CHALLENGED_SUPPORT_FACILITY_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '".CTRL_YEAR."' ";
        $query .= "     AND SPRT_FACILITY_CD = '".($fields["SPRT_FACILITY_CD"])."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック(CHALLENGED_SUPPORT_FACILITY_YMST)
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM CHALLENGED_SUPPORT_FACILITY_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '".CTRL_YEAR."' ";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO CHALLENGED_SUPPORT_FACILITY_YMST ";
        $query .= "           ( YEAR, ";
        $query .= "             SPRT_FACILITY_CD, ";
        $query .= "             SPRT_FACILITY_NAME, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SPRT_FACILITY_CD, ";
        $query .= "         SPRT_FACILITY_NAME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         CHALLENGED_SUPPORT_FACILITY_YMST ";
        $query .= "     WHERE ";
        $query  = "         YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
