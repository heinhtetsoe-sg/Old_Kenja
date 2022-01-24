<?php

require_once('for_php7.php');

class knjz015_ikkatsuQuery extends Query {
    //メニュー校種取得
    function getSchKind() {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //右リスト（プログラムID）
    function selectRightList1() {
        $query  = " SELECT ";
        $query .= "     PROGRAMID AS LABEL, ";
        $query .= "     PROGRAMID AS VALUE ";
        $query .= " FROM ";
        $query .= "     MENU_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID LIKE 'KNJ%' ";
        $query .= " GROUP BY ";
        $query .= "     PROGRAMID ";
        $query .= " ORDER BY ";
        $query .= "     PROGRAMID ";

        return $query;
    }

    //右リスト（対応校種）
    function selectRightList2() {
        $query  = " SELECT ";
        $query .= "     V1.NAME1 AS VALUE, ";
        $query .= "     V1.ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST V1 ";
        $query .= " WHERE ";
        $query .= "         V1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND V1.NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     V1.NAME1 ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        //配列に格納
        $prgID   = explode (",", $model->selectdata);
        $selKind = explode (",", $model->selectdata2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($prgID); $i++){
            $query  = " DELETE FROM ADMIN_CONTROL_PRG_SCHOOLKIND_MST ";
            $query .= "  WHERE SCHOOL_KIND        = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "    AND PROGRAMID          = '".$prgID[$i]."' ";
            $db->query($query);
        }

        for ($prgCnt = 0; $prgCnt < get_count($prgID); $prgCnt++) {
            for ($kindCnt = 0; $kindCnt < get_count($selKind); $kindCnt++) {
                $data["SCHOOL_KIND"][TEXT]          = $model->field["SCHOOL_KIND"];
                $data["PROGRAMID"][TEXT]            = $prgID[$prgCnt];
                $data["SELECT_SCHOOL_KIND"][TEXT]   = $selKind[$kindCnt];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $query = Query::insertSQL($data, "ADMIN_CONTROL_PRG_SCHOOLKIND_MST");
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。

        $db->autoCommit(true);
        Query::dbCheckIn($db);
        return;
    }
}
?>
