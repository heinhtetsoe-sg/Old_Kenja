<?php

require_once('for_php7.php');

class knjz450Query extends Query {
    //リスト取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.QUALIFIED_CD, ";
        $query .= "     T1.CONDITION_DIV, ";
        $query .= "     L1.NAME1 AS CONDITION_NAME, ";
        $query .= "     T1.QUALIFIED_NAME, ";
        $query .= "     T1.QUALIFIED_ABBV, ";
        $query .= "     T1.SAKI_FLG, ";
        $query .= "     T1.PROMOTER, ";
        $query .= "     T1.MANAGEMENT_FLG ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.CONDITION_DIV ";
        $query .= "                 AND L1.NAMECD1 = 'H311' ";
        $query .= " ORDER BY ";
        $query .= "     INT(QUALIFIED_CD) ";

        return $query;
    }

    //設定区分
    function getConditionDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H311' ";

        return $query;
    }

    //資格マスタ(1レコード取得)
    function getQualifiedMst($qualified_cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '{$qualified_cd}' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["QUALIFIED_CD"][TEXT]   = $fields["QUALIFIED_CD"];
        $data["QUALIFIED_NAME"][TEXT] = $fields["QUALIFIED_NAME"];
        $data["QUALIFIED_ABBV"][TEXT] = $fields["QUALIFIED_ABBV"];
        $data["CONDITION_DIV"][TEXT]  = $fields["CONDITION_DIV"];
        $data["SAKI_FLG"][TEXT]       = $fields["SAKI_FLG"];
        $data["PROMOTER"][TEXT]       = $fields["PROMOTER"];
        $data["MANAGEMENT_FLG"][TEXT] = ($fields["MANAGEMENT_FLG"] == "1") ? "1": "";
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "sysdate()";

        $query = Query::insertSQL($data, "QUALIFIED_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["QUALIFIED_CD"][TEXT]    = $fields["QUALIFIED_CD"];
        $data["QUALIFIED_NAME"][TEXT]  = $fields["QUALIFIED_NAME"];
        $data["QUALIFIED_ABBV"][TEXT]  = $fields["QUALIFIED_ABBV"];
        $data["CONDITION_DIV"][TEXT]   = $fields["CONDITION_DIV"];
        $data["SAKI_FLG"][TEXT]        = $fields["SAKI_FLG"];
        $data["PROMOTER"][TEXT]        = $fields["PROMOTER"];
        $data["MANAGEMENT_FLG"][TEXT]  = ($fields["MANAGEMENT_FLG"] == "1") ? "1": "";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $where = "WHERE QUALIFIED_CD   = '{$fields["QUALIFIED_CD"]}'";
        $query = Query::updateSQL($data, "QUALIFIED_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM QUALIFIED_MST "
                ." WHERE QUALIFIED_CD  = '{$fields["QUALIFIED_CD"]}'";
        $query;
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
?>
