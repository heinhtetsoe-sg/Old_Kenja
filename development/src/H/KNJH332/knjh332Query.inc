<?php

require_once('for_php7.php');

class knjh332Query extends Query {

    //職員名表示
    function getStaff()
    {
        $query  = " SELECT * FROM V_STAFF_MST ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " 	  AND STAFFCD = '".STAFFCD."' ";
        return $query;

    }

    //リストの最初表らわした時(デフォルト)の権限設定グループ
    function getFirst_GroupKey() 
    {
        $db = Query::dbCheckOut();

        $query  = knjh332Query::getGroup();
        $query .= " fetch first 1 rows only";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
        
    }

    //権限設定グループ
    function getGroup()
    {
        $query  = " SELECT T1.GROUPCD,T2.GROUPNAME FROM USERGROUP_DAT T1,USERGROUP_MST T2";
        $query .= " WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " 	  AND T1.STAFFCD = '".STAFFCD."' ";
        $query .= " 	  AND T1.GROUPCD = T2.GROUPCD ";
        return $query;

    }

    //リスト    
    function getList($groupcd)
    {
        $query  = " SELECT * FROM MOCK_TARGET_MST ";
        $query .= " WHERE TARGET_DIV = '2' ";
        $query .= " 	  AND STF_AUTH_CD = '".$groupcd."' ";
        $query .= " ORDER BY TARGETCD ";
        return $query;

    }

    //１レコード取得
    function getRow($targetcd,$groupcd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * FROM MOCK_TARGET_MST ";
        $query .= " WHERE TARGETCD = '" .$targetcd ."'";
        $query .= "       AND STF_AUTH_CD = '".$groupcd."'";
        $query .= "       AND TARGET_DIV = '2'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["TARGET_DIV"][TEXT]       = "2";
        $data["STF_AUTH_CD"][TEXT]      = $model->groupcd;
        $data["TARGETCD"][TEXT]         = $model->field["TARGETCD"];
        $data["TARGETNAME1"][TEXT]      = $model->field["TARGETNAME1"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_TARGET_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["TARGET_DIV"][TEXT]       = "2";
        $data["STF_AUTH_CD"][TEXT]      = $model->groupcd;
        $data["TARGETCD"][TEXT]         = $model->field["TARGETCD"];
        $data["TARGETNAME1"][TEXT]      = $model->field["TARGETNAME1"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE TARGETCD = '" .$model->field["TARGETCD"]."'";
        $where .= "       AND TARGET_DIV = '2'";
        $where .= "       AND STF_AUTH_CD = '".$model->groupcd."'";

        $query = Query::updateSQL($data, "MOCK_TARGET_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM MOCK_TARGET_MST ";
        $query .= " WHERE TARGETCD = '" .$model->field["TARGETCD"]."'";
        $query .= " AND TARGET_DIV = '2'";
        $query .= " AND STF_AUTH_CD = '".$model->groupcd."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
