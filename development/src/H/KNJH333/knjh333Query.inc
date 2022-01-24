<?php

require_once('for_php7.php');

class knjh333Query extends Query {

    //参照年度取得
    function getReferenceYear()
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    YEAR || '年度' AS LABEL, ";
        $query .= "    YEAR AS VALUE ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    GROUP_DIV = '1' ";
        $query .= "    AND STF_AUTH_CD = '".STAFFCD."' ";
        $query .= "ORDER BY ";
        $query .= "    YEAR ";

        return $query;
    }

    //対象年度取得
    function getObjectYear()
    {
        $query  = "SELECT ";
        $query .= "    YEAR || '年度' AS LABEL, ";
        $query .= "    YEAR AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= "ORDER BY ";
        $query .= "    YEAR ";

        return $query;
    }

    //利用者名称取得
    function getStaffName()
    {
        $query  = "SELECT ";
        $query .= "    STAFFNAME ";
        $query .= "FROM ";
        $query .= "    STAFF_MST ";
        $query .= "WHERE ";
        $query .= "    STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    //グループ名称取得
    function GetGroupName($model)
    {
        $query .= "SELECT DISTINCT ";
        $query .= "    T1.GROUPCD, ";
        $query .= "    T1.GROUPCD || '　' || VALUE(L1.GROUPNAME1,'') AS GROUPNAME ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_DAT T1 ";
        $query .= "    LEFT JOIN MOCK_GROUP_MST L1 ON L1.GROUP_DIV = T1.GROUP_DIV ";
        $query .= "         AND L1.STF_AUTH_CD = T1.STF_AUTH_CD ";
        $query .= "         AND L1.GROUPCD = T1.GROUPCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->oyear."' ";
        $query .= "    AND T1.GROUP_DIV = '1' ";
        $query .= "    AND T1.STF_AUTH_CD = '".STAFFCD."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.GROUPCD ";

        return $query;
    }

    //模試グループデータ取得
    function getGroupData($model, $target, $gropucd)
    {
        if ($target == 0) {
            $table = "MOCK_MST";
            $cdField = "MOCKCD";
            $nmField = "MOCKNAME1";
            $where = "";
        } else {
            $table = "MOCK_TARGET_MST";
            $cdField = "TARGETCD";
            $nmField = "TARGETNAME1";
            $where = "AND L1.TARGET_DIV = '".$target."' AND L1.STF_AUTH_CD = '".STAFFCD."' ";
        }
        $query  = "SELECT ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.GROUP_DIV, ";
        $query .= "    T1.TARGET_DIV, ";
        $query .= "    T1.STF_AUTH_CD, ";
        $query .= "    T1.GROUPCD, ";
        $query .= "    T1.MOCK_TARGET_CD, ";
        $query .= "    L1.".$nmField." AS MOCKNAME ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_DAT T1 ";
        $query .= "    LEFT JOIN ".$table." L1 ON L1.".$cdField." = T1.MOCK_TARGET_CD ";
        $query .= "         ".$where." ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->oyear."' ";
        $query .= "    AND T1.GROUP_DIV = '1' ";
        $query .= "    AND T1.TARGET_DIV = '".$target."' ";
        $query .= "    AND T1.STF_AUTH_CD = '".STAFFCD."' ";
        $query .= "    AND T1.GROUPCD = '".$gropucd."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.GROUPCD ";

        return $query;
    }

    //グループ名称取得
    function getGropuName($gropucd)
    {
        $query  = "SELECT ";
        $query .= "    GROUPCD AS VALUE, ";
        $query .= "    GROUPCD || '　' || GROUPNAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_MST ";
        $query .= "WHERE ";
        $query .= "    GROUP_DIV = '1' ";
        $query .= "    AND STF_AUTH_CD = '".STAFFCD."' ";

        return $query;
    }

    //データ一覧取得
    function selectQuery($model, $target)
    {
        if ($target == 0) {
            $table = "MOCK_MST";
            $cdField = "MOCKCD";
            $nmField = "MOCKNAME1";
            $where = "";
        } else {
            $table = "MOCK_TARGET_MST";
            $cdField = "TARGETCD";
            $nmField = "TARGETNAME1";
            $where = "WHERE T1.TARGET_DIV = '".$target."' AND STF_AUTH_CD = '".STAFFCD."' ";
        }
        $query  = "SELECT ";
        $query .= "    T1.".$cdField." AS VALUE, ";
        $query .= "    L1.MOCK_TARGET_CD AS JOINCD, ";
        $query .= "    T1.".$nmField." AS LABEL ";
        $query .= "FROM ";
        $query .= "    ".$table." T1 ";
        $query .= "    LEFT JOIN (SELECT ";
        $query .= "                   MOCK_TARGET_CD ";
        $query .= "               FROM ";
        $query .= "                   MOCK_GROUP_DAT LT1 ";
        $query .= "               WHERE ";
        $query .= "                   LT1.YEAR = '".$model->oyear."' ";
        $query .= "                   AND LT1.GROUP_DIV = '1' ";
        $query .= "                   AND LT1.TARGET_DIV = '".$target."' ";
        $query .= "                   AND LT1.STF_AUTH_CD = '".STAFFCD."' ";
        $query .= "                   AND LT1.GROUPCD = '".$model->groupcd."' ";
        $query .= "              ) L1 ON L1.MOCK_TARGET_CD = T1.".$cdField." ";
        $query .= $where;
        $query .= "ORDER BY ";
        $query .= "    T1.".$cdField." ";

        return $query;
    }

    //コピー前処理
    function checkMockGroup($year, $groupcd = "")
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$year."' ";
        $query .= "    AND GROUP_DIV = '1' ";
        $query .= "    AND STF_AUTH_CD = '".STAFFCD."' ";
        if ($groupcd) {
            $query .= "    AND GROUPCD = '".$groupcd."' ";
        }

        return $query;
    }

    //コピー処理
    function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "INSERT INTO MOCK_GROUP_DAT ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->oyear."', ";
        $query .= "    GROUP_DIV, ";
        $query .= "    TARGET_DIV, ";
        $query .= "    STF_AUTH_CD, ";
        $query .= "    GROUPCD, ";
        $query .= "    MOCK_TARGET_CD, ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    SYSDATE() ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->ryear."' ";
        $query .= "    AND GROUP_DIV = '1' ";
        $query .= "    AND STF_AUTH_CD = '".STAFFCD."' ";
        $query .= ") ";

        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);

        return;
    }

    //模試グループ新規追加
    function &getMockGInstQuery(&$db, $select, $model, $target)
    {
        //模試グループ新規
        $data = array();
        $selectdata = explode(",", $select);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $db->query(knjh333Query::makeInsMocGQuery($model, $target, $selectdata[$i]));
        }
        return;
    }

    //データ削除SQL作成
    function makeDelMocGQuery($model, $target)
    {
        $query  = "DELETE FROM ";
        $query .= "    MOCK_GROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->oyear."' ";
        $query .= "    AND GROUP_DIV = '1' ";
        $query .= "    AND TARGET_DIV = '".$target."' ";
        $query .= "    AND STF_AUTH_CD = '".STAFFCD."' ";
        $query .= "    AND GROUPCD = '".$model->groupcd."' ";

        return $query;
    }

    //データ新規SQL作成
    function makeInsMocGQuery($model, $target, $selectdata)
    {
        $data["YEAR"][TEXT]             = $model->oyear;
        $data["GROUP_DIV"][TEXT]        = "1";
        $data["TARGET_DIV"][TEXT]       = $target;
        $data["STF_AUTH_CD"][TEXT]      = STAFFCD;
        $data["GROUPCD"][TEXT]          = $model->groupcd;
        $data["MOCK_TARGET_CD"][TEXT]   = $selectdata;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "MOCK_GROUP_DAT");

        return $query;
    }
}
?>
