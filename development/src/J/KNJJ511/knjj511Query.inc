<?php
class knjj511Query extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //種別の取得
    public function getSportsItemMst($model)
    {
        $query .= " SELECT ";
        $query .= "     ITEMCD AS VALUE, ";
        $query .= "     ITEMCD || ':' || ITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SPORTS_ITEM_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //左フレームのリスト
    public function getList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     SIM.ITEMNAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     Z002.NAME1 AS NAME1 ";
        $query .= " FROM ";
        $query .= "     SPORTS_ITEM_SCORE_BASE_MST T1 ";
        $query .= "     LEFT JOIN SPORTS_ITEM_MST SIM ";
        $query .= "            ON SIM.ITEMCD = T1.ITEMCD ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "            ON Z002.YEAR    = T1.YEAR ";
        $query .= "           AND Z002.NAMECD2 = T1.SEX ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     T1.SEX ";
        return $query;
    }

    //SPORTS_ITEM_SCORE_BASE_MSTから1レコード取得
    public function getSportsItemScoreBaseMst($model)
    {
        $query  = " SELECT DISTINCT ";
        foreach ($model->totalLevel as $lebel => $val) {
            $table = "L".$val;
            $low   = "RECORD_LOW_".$val;
            $high  = "RECORD_HIGH_".$val;
            $query .= "     ".$table.".RECORD_LOW  AS ".$low.", ";
            $query .= "     ".$table.".RECORD_HIGH AS ".$high.", ";
        }
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     SIM.UNIT, ";
        $query .= "     T1.SEX ";
        $query .= " FROM ";
        $query .= "     SPORTS_ITEM_SCORE_BASE_MST T1 ";
        $query .= "     LEFT JOIN SPORTS_ITEM_MST SIM ";
        $query .= "            ON SIM.ITEMCD = T1.ITEMCD ";
        foreach ($model->totalLevel as $lebel => $val) {
            $table = "L".$val;
            $query .= "     LEFT JOIN SPORTS_ITEM_SCORE_BASE_MST ".$table;
            $query .= "            ON ".$table.".YEAR         = T1.YEAR ";
            $query .= "           AND ".$table.".ITEMCD       = T1.ITEMCD ";
            $query .= "           AND ".$table.".SEX          = T1.SEX ";
            $query .= "           AND ".$table.".SCORE_LEVEL  = '".$val."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR    = '".$model->year."' ";
        $query .= " AND T1.ITEMCD  = '".$model->field["ITEMCD"]."' ";
        $query .= " AND T1.SEX     = '".$model->field["SEX"]."' ";
        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->totalLevel as $lebel => $val) {
            $low  = "RECORD_LOW_".$val;
            $high = "RECORD_HIGH_".$val;
            $data["YEAR"][TEXT]            = $model->year;
            $data["ITEMCD"][TEXT]          = $model->field["ITEMCD"];
            $data["SEX"][TEXT]             = $model->field["SEX"];
            $data["SCORE_LEVEL"][TEXT]     = $val;
            $data["RECORD_LOW"][NUMBER]    = $model->field[$low];
            $data["RECORD_HIGH"][NUMBER]   = $model->field[$high];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][NUMBER]       = "sysdate()";
            $query = Query::insertSQL($data, "SPORTS_ITEM_SCORE_BASE_MST");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->totalLevel as $lebel => $val) {
            $low  = "RECORD_LOW_".$val;
            $high = "RECORD_HIGH_".$val;
            $data["RECORD_LOW"][NUMBER]    = $model->field[$low];
            $data["RECORD_HIGH"][NUMBER]   = $model->field[$high];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][NUMBER]       = "sysdate()";
            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->year."' ";
            $where .= " AND ITEMCD      = '".$model->field["ITEMCD"]."' ";
            $where .= " AND SEX         = '".$model->field["SEX"]."' ";
            $where .= " AND SCORE_LEVEL = '".$val."' ";
    
            $query = Query::updateSQL($data, "SPORTS_ITEM_SCORE_BASE_MST", $where);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SPORTS_ITEM_SCORE_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " AND ITEMCD      = '".$model->field["ITEMCD"]."' ";
        $query .= " AND SEX         = '".$model->field["SEX"]."' ";
        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
    }
}
