<?php

require_once('for_php7.php');

class knjz131Query extends Query {

    //年度
    function getYear() {
        $query  = " WITH THIS_YEAR(VALUE, LABEL) AS ( ";
        $query .= " VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        return $query;
    }

    //左データ取得
    function getAttendDiCdDat($model) {
        $query .= " SELECT ";
        $query .= "     DI_CD, ";
        $query .= "     DI_NAME1 ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     INT(DI_CD) ";
        return $query;
    }

    //１レコード取得
    function getRightDataRow($model, $diCd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}'";
        $query .= "     AND DI_CD = '{$diCd}'";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //読替取得
    function getYomikae($model) {
        $query .= " SELECT ";
        $query .= "     DI_CD AS VALUE, ";
        $query .= "     DI_CD || ':' || DI_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND DI_CD IN('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "     INT(DI_CD) ";
        return $query;
    }

    //賢者用DI
    function getDi($model) {
        $query .= " SELECT ";
        $query .= "     DI_CD AS VALUE, ";
        $query .= "     DI_CD || ':' || DI_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND INT(DI_CD) <= 50 ";
        $query .= " ORDER BY ";
        $query .= "     INT(DI_CD) ";
        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]             = $model->year;
        $data["DI_CD"][TEXT]            = $fields["DI_CD"];
        $data["DI_NAME1"][TEXT]         = $fields["DI_NAME1"];
        $data["DI_NAME2"][TEXT]         = $fields["DI_NAME2"];
        $data["ATSUB_REPL_DI_CD"][TEXT] = $fields["ATSUB_REPL_DI_CD"];
        $data["DI_MARK"][TEXT]          = $fields["DI_MARK"];
        $data["MULTIPLY"][TEXT]         = $fields["MULTIPLY"];
        $data["RESTRICT_FLG"][TEXT]     = $fields["RESTRICT_FLG"];
        $data["ONEDAY_DI_CD"][TEXT]     = $fields["ONEDAY_DI_CD"];
        $data["ORDER"][TEXT]            = $fields["ORDER"];
        $data["PETITION_ORDER"][TEXT]   = $fields["PETITION_ORDER"];
        $data["REP_DI_CD"][TEXT]        = $fields["REP_DI_CD"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_DI_CD_DAT");
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["DI_NAME1"][TEXT]         = $fields["DI_NAME1"];
        $data["DI_NAME2"][TEXT]         = $fields["DI_NAME2"];
        $data["ATSUB_REPL_DI_CD"][TEXT] = $fields["ATSUB_REPL_DI_CD"];
        $data["DI_MARK"][TEXT]          = $fields["DI_MARK"];
        $data["MULTIPLY"][TEXT]         = $fields["MULTIPLY"];
        $data["RESTRICT_FLG"][TEXT]     = $fields["RESTRICT_FLG"];
        $data["ONEDAY_DI_CD"][TEXT]     = $fields["ONEDAY_DI_CD"];
        $data["ORDER"][TEXT]            = $fields["ORDER"];
        $data["PETITION_ORDER"][TEXT]   = $fields["PETITION_ORDER"];
        $data["REP_DI_CD"][TEXT]        = $fields["REP_DI_CD"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->year}' ";
        $where .= "     AND DI_CD = '{$fields["DI_CD"]}' ";

        $query = Query::updateSQL($data, "ATTEND_DI_CD_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND DI_CD = '{$fields["DI_CD"]}'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

/**** 前年度コピー処理 ****/
    //今年度のデータが存在するかチェック
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ATTEND_DI_CD_DAT ";
        $query .= "    (YEAR, ";
        $query .= "     DI_CD, ";
        $query .= "     DI_NAME1, ";
        $query .= "     DI_NAME2, ";
        $query .= "     ATSUB_REPL_DI_CD, ";
        $query .= "     DI_MARK, ";
        $query .= "     MULTIPLY, ";
        $query .= "     RESTRICT_FLG, ";
        $query .= "     ONEDAY_DI_CD, ";
        $query .= "     ORDER, ";
        $query .= "     PETITION_ORDER, ";
        $query .= "     REP_DI_CD, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED) ";
        $query .= "   (SELECT ";
        $query .= "     '{$model->year}', ";
        $query .= "     DI_CD, ";
        $query .= "     DI_NAME1, ";
        $query .= "     DI_NAME2, ";
        $query .= "     ATSUB_REPL_DI_CD, ";
        $query .= "     DI_MARK, ";
        $query .= "     MULTIPLY, ";
        $query .= "     RESTRICT_FLG, ";
        $query .= "     ONEDAY_DI_CD, ";
        $query .= "     ORDER, ";
        $query .= "     PETITION_ORDER, ";
        $query .= "     REP_DI_CD, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DI_CD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
