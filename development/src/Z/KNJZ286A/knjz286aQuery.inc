<?php

require_once('for_php7.php');

class knjz286aQuery extends Query {

    //年度一覧取得
    function selectYearQuery() {

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     POSITION_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //教務主任等年度一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.POSITIONCD AS VALUE, ";
        $query .= "     T1.POSITIONCD || '　' || T2.POSITIONNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     POSITION_YDAT T1, ";
        $query .= "     POSITION_MST T2 ";
        $query .= " WHERE "; 
        $query .= "     T1.YEAR = '".$model->year."' AND "; 
        $query .= "     T1.POSITIONCD = T2.POSITIONCD "; 
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教務主任等一覧取得
    function selectPositionQuery($model) {
        $query  = " SELECT ";
        $query .= "     POSITIONCD AS VALUE, ";
        $query .= "     POSITIONCD || '　' || POSITIONNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     POSITION_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     POSITION_YDAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR = '".$model->year."' AND ";
        $query .= "                     S1.POSITIONCD  = T1.POSITIONCD) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        //教務主任等コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = " DELETE FROM POSITION_YDAT WHERE YEAR = '".$model->year."' ";
        $db->query($query);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                //追加
                $data = array();
                $data["YEAR"][TEXT]         = $model->year;
                $data["POSITIONCD"][TEXT]   = $array[$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $db->query(Query::insertSQL($data, "POSITION_YDAT"));
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
