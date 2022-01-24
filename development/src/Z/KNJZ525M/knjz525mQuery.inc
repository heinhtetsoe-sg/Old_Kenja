<?php

require_once('for_php7.php');

class knjz525mQuery extends Query
{

    //データ取得
    public function getChallengedStatussheetItemNameDat($year, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "count") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM CHALLENGED_STATUSSHEET_ITEM_NAME_DAT ";
        $query .= " WHERE YEAR = '".$year."' ";
        if (!$flg) {
            $query .= " ORDER BY INT(DATA_DIV) ";
        }
        return $query;
    }

    //更新
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //全項目削除
        knjz525mQuery::getDeleteQuery($db, $model);

        //ヘッダ更新
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]         = '0';
        $data["SHEET_PATTERN"][TEXT]    = $model->field["SHEET_PATTERN"];
        $data["STATUS_NAME"][TEXT]      = $model->field["STATUS_NAME"];
        if ($model->field["SHEET_PATTERN"] == "2") {
            $data["GROWUP_NAME"][TEXT]  = $model->field["GROWUP_NAME"];
        }
        $data["COMMENTS"][TEXT]         = $model->field["COMMENTS"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "CHALLENGED_STATUSSHEET_ITEM_NAME_DAT");
        $db->query($query);

        //更新
        if ($model->field["SETCNT"] > 0) {
            for ($i = 1; $i <= $model->field["SETCNT"]; $i++) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["DATA_DIV"][TEXT]         = $i;
                $data["DATA_DIV_NAME"][TEXT]    = $model->field["DATA_DIV_NAME".$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "CHALLENGED_STATUSSHEET_ITEM_NAME_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除(ヘッダ＆項目)
    public function &getDeleteQuery($db, $model)
    {
        $query  = " DELETE FROM ";
        $query .= "     CHALLENGED_STATUSSHEET_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        return;
    }

    //コピー
    public function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();

        //前年度のデータ取得
        $result = $db->query(knjz525mQuery::getChallengedStatussheetItemNameDat(CTRL_YEAR-1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["DATA_DIV"][TEXT]         = $row["DATA_DIV"];
            $data["SHEET_PATTERN"][TEXT]    = $row["SHEET_PATTERN"];
            $data["DATA_DIV_NAME"][TEXT]    = $row["DATA_DIV_NAME"];
            $data["STATUS_NAME"][TEXT]      = $row["STATUS_NAME"];
            $data["GROWUP_NAME"][TEXT]      = $row["GROWUP_NAME"];
            $data["COMMENTS"][TEXT]         = $row["COMMENTS"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "CHALLENGED_STATUSSHEET_ITEM_NAME_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }
}
