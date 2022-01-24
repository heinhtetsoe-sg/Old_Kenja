<?php

require_once('for_php7.php');

class knjz392Query extends Query {

    //年度
    function selectYearQuery() {
        return "SELECT DISTINCT year FROM CHILDCARE_FARE_YDAT ORDER BY year DESC";
    }

    //年度一覧取得
    function selectQuery($model) {
        $query  = "SELECT t1.FARE_CD, t1.FARE ";
        $query .= " FROM CHILDCARE_FARE_MST t1,CHILDCARE_FARE_YDAT t2 ";
        $query .= " WHERE t2.year = '".$model->year."' "; 
        $query .= "       AND t1.FARE_CD = t2.FARE_CD "; 
        $query .= " ORDER BY t1.FARE_CD";
        return $query;
    }

    //一覧取得
    function selectFareQuery($fare_cd_array,$model) {
        $query  = "SELECT FARE_CD, FARE ";
        $query .= " FROM CHILDCARE_FARE_MST m ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM CHILDCARE_FARE_YDAT y ";
        $query .= "                    WHERE y.year = '".$model->year."'";
        $query .= "                      AND y.FARE_CD = m.FARE_CD) ";
        $query .= " ORDER BY FARE_CD ";
        return $query;
    }
    //UPDATE
    function &getUpdateQuery(&$model) {
        //コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM CHILDCARE_FARE_YDAT WHERE year = '" .$model->year ."'";
        $db->query($query);

        $query = "INSERT INTO CHILDCARE_FARE_YDAT(year,FARE_CD,registercd,updated)";
        $query.= "VALUES";
        if ($array[0] != ""){
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //コード
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
