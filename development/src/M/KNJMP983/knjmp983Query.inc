<?php

require_once('for_php7.php');

class knjmp983Query extends Query {

    //年度一覧取得
    function selectYearQuery() {

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     TRADER_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //業者年度一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.TRADER_CD AS VALUE, ";
        $query .= "     T1.TRADER_CD || '　' || T2.TRADER_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TRADER_YDAT T1, ";
        $query .= "     TRADER_MST T2 ";
        $query .= " WHERE "; 
        $query .= "     T1.YEAR = '".$model->year."' AND "; 
        $query .= "     T1.TRADER_CD = T2.TRADER_CD "; 
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //業者一覧取得
    function selectTraderQuery($model) {
        $query  = " SELECT ";
        $query .= "     TRADER_CD AS VALUE, ";
        $query .= "     TRADER_CD || '　' || TRADER_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TRADER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     TRADER_YDAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR = '".$model->year."' AND ";
        $query .= "                     S1.TRADER_CD  = T1.TRADER_CD) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        //業者コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = " DELETE FROM TRADER_YDAT WHERE YEAR = '".$model->year."' ";
        $db->query($query);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                //追加
                $data = array();
                $data["YEAR"][TEXT]         = $model->year;
                $data["TRADER_CD"][TEXT]    = $array[$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $db->query(Query::insertSQL($data, "TRADER_YDAT"));
            }
        } else {
            $model->year = "";
        }

        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。
        Query::dbCheckIn($db);
        return;
    }
}
?>
