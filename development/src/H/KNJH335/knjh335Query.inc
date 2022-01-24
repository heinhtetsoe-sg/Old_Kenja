<?php

require_once('for_php7.php');

class knjh335Query extends Query {

    //リスト取得
    function getList($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.*, ";
        if ($model->group_div == "1") {
            $query .= "    L1.STAFFNAME AS STF_AUTH_NAME ";
        } else {
            $query .= "    L1.GROUPNAME AS STF_AUTH_NAME ";
        }
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_MST T1 ";
        if ($model->group_div == "1") {
            $query .= "    LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.STF_AUTH_CD ";
        } else {
            $query .= "    LEFT JOIN USERGROUP_MST L1 ON '0000' || L1.GROUPCD = T1.STF_AUTH_CD ";
        }
        $query .= "WHERE ";
        $query .= "    GROUP_DIV = '".$model->group_div."' ";
        if ($model->group_div == "1") {
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
        $query .= "    T1.GROUPCD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $div = "")
    {
        if ($div == "CHECK") {
            $stf_auth_cd = $model->field["STF_AUTH_CD"];
            $groupcd     = $model->field["GROUPCD"];
        } else {
            $stf_auth_cd = $model->stf_auth_cd;
            $groupcd     = $model->groupcd;
        }

        $query  = "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_MST ";
        $query .= "WHERE ";
        $query .= "    GROUP_DIV = '".$model->group_div."' ";
        $query .= "    AND STF_AUTH_CD = '".$stf_auth_cd."' ";
        $query .= "    AND GROUPCD = '".$groupcd."' ";

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

        $data["GROUP_DIV"][TEXT]    = $fields["GROUP_DIV"];
        $data["STF_AUTH_CD"][TEXT]  = $fields["STF_AUTH_CD"];
        $data["GROUPCD"][TEXT]      = $fields["GROUPCD"];
        $data["GROUPNAME1"][TEXT]   = $fields["GROUPNAME1"];
        $data["GROUPNAME2"][TEXT]   = $fields["GROUPNAME2"];
        $data["GROUPNAME3"][TEXT]   = $fields["GROUPNAME3"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]	= "sysdate()";

        $query = Query::insertSQL($data, "MOCK_GROUP_MST");

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

        $data["GROUPNAME1"][TEXT]   = $fields["GROUPNAME1"];
        $data["GROUPNAME2"][TEXT]   = $fields["GROUPNAME2"];
        $data["GROUPNAME3"][TEXT]   = $fields["GROUPNAME3"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]	= "sysdate()";

        $where  = "WHERE ";
        $where .= "   GROUP_DIV = '".$fields["GROUP_DIV"]."' ";
        $where .= "   AND STF_AUTH_CD = '".$fields["STF_AUTH_CD"]."' ";
        $where .= "   AND GROUPCD = '".$fields["GROUPCD"]."' ";

        $query = Query::updateSQL($data, "MOCK_GROUP_MST", $where);

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //--- DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM ";
        $query .= "    MOCK_GROUP_MST ";
        $query .= "WHERE ";
        $query .= "   GROUP_DIV = '".$fields["GROUP_DIV"]."' ";
        $query .= "   AND STF_AUTH_CD = '".$fields["STF_AUTH_CD"]."' ";
        $query .= "   AND GROUPCD = '".$fields["GROUPCD"]."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);

        return $result;
    }

    //データ登録チェック
    function getGroupData($fields)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    GROUP_DIV = '".$fields["GROUP_DIV"]."' ";
        $query .= "    AND STF_AUTH_CD = '".$fields["STF_AUTH_CD"]."' ";
        $query .= "    AND GROUPCD = '".$fields["GROUPCD"]."' ";

        return $query;
    }
}
?>
