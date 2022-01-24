<?php

require_once('for_php7.php');

class knjz261aQuery extends Query {

    //年度一覧取得
    function selectYearQuery() {

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //校務分掌年度一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.DUTYSHARECD AS VALUE, ";
        $query .= "     T1.DUTYSHARECD || '　' || T2.SHARENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_YDAT T1, ";
        $query .= "     DUTYSHARE_MST T2 ";
        $query .= " WHERE "; 
        $query .= "     T1.YEAR = '".$model->year."' AND "; 
        $query .= "     T1.DUTYSHARECD = T2.DUTYSHARECD "; 
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校務分掌一覧取得
    function selectDutyshareQuery($model) {
        $query  = " SELECT ";
        $query .= "     DUTYSHARECD AS VALUE, ";
        $query .= "     DUTYSHARECD || '　' || SHARENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     DUTYSHARE_YDAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR = '".$model->year."' AND ";
        $query .= "                     S1.DUTYSHARECD  = T1.DUTYSHARECD) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        //校務分掌コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = " DELETE FROM DUTYSHARE_YDAT WHERE YEAR = '".$model->year."' ";
        $db->query($query);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                //追加
                $data = array();
                $data["YEAR"][TEXT]         = $model->year;
                $data["DUTYSHARECD"][TEXT]  = $array[$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $db->query(Query::insertSQL($data, "DUTYSHARE_YDAT"));
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
