<?php

require_once('for_php7.php');

class knjz400jQuery extends Query {

    //年度観点一覧取得
    function selectQuery($model)
    {
        $query  =  "SELECT t1.viewcd, t1.viewname ";
        $query .= "FROM jviewname_mst t1, jviewname_ydat t2 ";
        $query .= "WHERE t2.year = '".$model->year."' AND "; 
        $query .= "      t1.viewcd = t2.viewcd "; 
        $query .= "ORDER BY t1.viewcd";
        return $query;
    }
    //年度一覧取得
    function selectYearQuery()
    {
        $query  = "SELECT SMALLINT(YEAR) AS year FROM jviewname_ydat ";
        $query .= "GROUP BY year ";
        $query .= "ORDER BY year DESC ";
        return $query;
    }
    //観点一覧取得
    function selectSubclassQuery($subclasscd_array,$model)
    {
        $query  = "SELECT viewcd,viewname "; 
        $query .= " FROM jviewname_mst m ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                   FROM jviewname_ydat y ";
        $query .= "                   WHERE y.year = '".$model->year."'";
        $query .= "                    AND y.viewcd = m.viewcd) ";
        $query .= " ORDER BY viewcd ";
        return $query;
    }
    //UPDATE
    function &getUpdateQuery($model)
    {
        $all_light = false;
        //観点コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM jviewname_ydat WHERE year = '" .$model->year ."'";
        $db->query($query);

        //観点年度データの登録
        if ($array[0] !="") {
            $query = "INSERT INTO jviewname_ydat(year,viewcd,registercd,updated)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //観点コード
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付

            }
            $db->query($query);
        } else {
            $all_light = true;
        }
        $db->commit(); //トランザクションをコミットする。

        Query::dbCheckIn($db);
        return $all_light;
    }
}
?>
