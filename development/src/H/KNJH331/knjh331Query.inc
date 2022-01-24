<?php

require_once('for_php7.php');

class knjh331Query extends Query {

    //模試種別
    function getMockDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H308' ";
        $query .= "     AND NAMESPARE3 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //リスト取得
    function getList($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.*, ";
        if ($model->target_div == "1") {
            $query .= "    L1.STAFFNAME AS STF_AUTH_NAME ";
        } else {
            $query .= "    L1.GROUPNAME AS STF_AUTH_NAME ";
        }
        $query .= "FROM ";
        $query .= "    MOCK_TARGET_MST T1 ";
        if ($model->target_div == "1") {
            $query .= "    LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.STF_AUTH_CD ";
        } else {
            $query .= "    LEFT JOIN USERGROUP_MST L1 ON '0000' || L1.GROUPCD = T1.STF_AUTH_CD ";
        }
        $query .= "WHERE ";
        $query .= "    TARGET_DIV = '".$model->target_div."' ";
        if ($model->target_div == "1") {
            $query .= "    AND STF_AUTH_CD = '".STAFFCD."' ";
        } else {
            $query .= "    AND STF_AUTH_CD IN (SELECT ";
            $query .= "                            '0000' || GROUPCD ";
            $query .= "                        FROM ";
            $query .= "                            USERGROUP_DAT ";
            $query .= "                        WHERE ";
            $query .= "                            YEAR = '".CTRL_YEAR."' ";
            $query .= "                            AND STAFFCD = '".STAFFCD."') ";
        }
        $query .= "ORDER BY ";
        $query .= "    T1.TARGETCD ";

        return $query;

    }

    //１レコード取得
    function getRow($model, $div = "")
    {
        if ($div == "CHECK") {
            $stf_auth_cd = $model->field["STF_AUTH_CD"];
            $targetcd    = $model->field["MOCK_DIV"].$model->field["TARGETCD"];
        } else {
            $stf_auth_cd = $model->stf_auth_cd;
            $targetcd    = $model->mockdiv.$model->targetcd;
        }

        $query  = "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    MOCK_TARGET_MST ";
        $query .= "WHERE ";
        $query .= "    TARGET_DIV = '".$model->target_div."' ";
        $query .= "    AND STF_AUTH_CD = '".$stf_auth_cd."' ";
        $query .= "    AND TARGETCD = '".$targetcd."' ";

        return $query;
    }

    //ユーザーコード(STAFF)取得
    function getStaff($stf_auth_cd)
    {
        $query  = "SELECT ";
        $query .= "    STAFFCD AS VALUE, ";
        $query .= "    STAFFNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    STAFF_MST ";
        $query .= "WHERE ";
        $query .= "    STAFFCD = '".STAFFCD."' ";
        $query .= "ORDER BY ";
        $query .= "    STAFFCD ";

        return $query;
    }

    //ユーザーコード(USER)取得
    function getUser($stf_auth_cd)
    {
        $query  = "SELECT ";
        $query .= "    '0000' || T1.GROUPCD AS VALUE, ";
        $query .= "    L1.GROUPNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    USERGROUP_DAT T1 ";
        $query .= "    LEFT JOIN USERGROUP_MST L1 ON L1.GROUPCD = T1.GROUPCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.STAFFCD = '".STAFFCD."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.GROUPCD ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["TARGET_DIV"][TEXT]       = $fields["TARGET_DIV"];
        $data["STF_AUTH_CD"][TEXT]      = $fields["STF_AUTH_CD"];
        $data["TARGETCD"][TEXT]         = $fields["MOCK_DIV"].$fields["TARGETCD"];
        $data["TARGETNAME1"][TEXT]      = $fields["TARGETNAME1"];
        $data["TARGETNAME2"][TEXT]      = $fields["TARGETNAME2"];
        $data["TARGETNAME3"][TEXT]      = $fields["TARGETNAME3"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_TARGET_MST");

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //--- UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["TARGETNAME1"][TEXT]      = $fields["TARGETNAME1"];
        $data["TARGETNAME2"][TEXT]      = $fields["TARGETNAME2"];
        $data["TARGETNAME3"][TEXT]      = $fields["TARGETNAME3"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE TARGET_DIV = '" .$fields["TARGET_DIV"]."'";
        $where .= "       AND STF_AUTH_CD = '".$fields["STF_AUTH_CD"]."'";
        $where .= "       AND TARGETCD = '".$fields["MOCK_DIV"].$fields["TARGETCD"]."'";

        $query = Query::updateSQL($data, "MOCK_TARGET_MST", $where);

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM ";
        $query .= "    MOCK_TARGET_MST ";
        $query .= "WHERE ";
        $query .= "   TARGET_DIV = '".$fields["TARGET_DIV"]."' ";
        $query .= "   AND STF_AUTH_CD = '".$fields["STF_AUTH_CD"]."' ";
        $query .= "   AND TARGETCD = '".$fields["MOCK_DIV"].$fields["TARGETCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //データ登録チェック
    function getGroupData($fields)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    MOCK_TARGET_DAT ";
        $query .= "WHERE ";
        $query .= "    TARGET_DIV = '".$fields["TARGET_DIV"]."' ";
        $query .= "    AND STF_AUTH_CD = '".$fields["STF_AUTH_CD"]."' ";
        $query .= "    AND TARGETCD = '".$fields["MOCK_DIV"].$fields["TARGETCD"]."' ";

        return $query;
    }
}
?>
