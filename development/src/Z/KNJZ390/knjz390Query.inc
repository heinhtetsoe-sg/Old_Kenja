<?php

require_once('for_php7.php');

class knjz390Query extends Query {

    //課程年度一覧取得
    function selectQuery($model)
    {
        $query  = "SELECT t1.STATIONCD, t1.STATIONNAME ";
        $query .= " FROM STATION_MST t1,STATION_YDAT t2 ";
        $query .= " WHERE t2.year = '".$model->year."' "; 
        $query .= "       AND t1.STATIONCD = t2.STATIONCD "; 
        $query .= " ORDER BY t1.STATIONCD";
        return $query;
    }

    //課程年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT year FROM STATION_YDAT ORDER BY year DESC";
    }

    //課程一覧取得
    function selectStationQuery($stationcd_array,$model)
    {
        $query  = "SELECT STATIONCD, STATIONNAME ";
        $query .= " FROM STATION_MST m ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM STATION_YDAT y ";
        $query .= "                    WHERE y.year = '".$model->year."'";
        $query .= "                      AND y.STATIONCD = m.STATIONCD) ";
        $query .= " ORDER BY STATIONCD ";
        return $query;
    }
    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //課程コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM STATION_YDAT WHERE year = '" .$model->year ."'";
        $db->query($query);

        $query = "INSERT INTO STATION_YDAT(year,STATIONCD,registercd,updated)";
        $query.= "VALUES";
        if ($array[0] != ""){
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //課程コード
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
