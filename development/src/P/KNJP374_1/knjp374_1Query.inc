<?php

require_once('for_php7.php');

class knjp374_1Query extends Query {
    /************/
    /** 左画面 **/
    /************/
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     TRANS_DIV.TRANSFER_DIV, ";
        $query .= "     TRANS_DIV.TRANSFER_NAME ";
        $query .= " FROM ";
        $query .= "     MONEY_TRANSFER_DIV_MST TRANS_DIV ";
        $query .= " WHERE ";
        $query .= "     TRANS_DIV.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND TRANS_DIV.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND TRANS_DIV.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     TRANSFER_DIV ";

        return $query;
    }

    /************/
    /** 右画面 **/
    /************/
    //１レコード取得
    function getOneRecord($model, $transferDiv) {

        $query  = " SELECT ";
        $query .= "     TRANS_DIV.TRANSFER_DIV, ";
        $query .= "     TRANS_DIV.TRANSFER_NAME ";
        $query .= " FROM ";
        $query .= "     MONEY_TRANSFER_DIV_MST TRANS_DIV ";
        $query .= " WHERE ";
        $query .= "     TRANS_DIV.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND TRANS_DIV.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND TRANS_DIV.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TRANS_DIV.TRANSFER_DIV = '{$transferDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     TRANSFER_DIV ";

        return $query;
    }

    /**************/
    /** 更新処理 **/
    /**************/
    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]          = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]       = $model->schoolKind;
        $data["YEAR"][TEXT]              = CTRL_YEAR;
        $data["TRANSFER_DIV"][TEXT]      = $model->field["TRANSFER_DIV"];
        $data["TRANSFER_NAME"][TEXT]     = $model->field["TRANSFER_NAME"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $query = Query::insertSQL($data, "MONEY_TRANSFER_DIV_MST");
        $db->query($query);

        $db->commit();

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["TRANSFER_NAME"][TEXT]    = $model->field["TRANSFER_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND     = '".$model->schoolKind."' ";
        $where .= "   AND YEAR            = '".CTRL_YEAR."' ";
        $where .= "   AND TRANSFER_DIV    = '".$model->field["TRANSFER_DIV"]."' ";

        $query = Query::updateSQL($data, "MONEY_TRANSFER_DIV_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM MONEY_TRANSFER_DIV_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND     = '".$model->schoolKind."' ";
        $query .= "     AND YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND TRANSFER_DIV    = '".$model->field["TRANSFER_DIV"]."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /****************/
    /** コピー処理 **/
    /****************/
    //今年度のデータが存在するかチェック
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM MONEY_TRANSFER_DIV_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "   AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "   AND YEAR        = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO MONEY_TRANSFER_DIV_MST ";
        $query .= "           ( SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             TRANSFER_DIV, ";
        $query .= "             TRANSFER_NAME, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         T1.SCHOOLCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         T1.TRANSFER_DIV, ";
        $query .= "         T1.TRANSFER_NAME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         MONEY_TRANSFER_DIV_MST T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "         AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "         AND T1.YEAR        = '".(CTRL_YEAR - 1)."'";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
