<?php

require_once('for_php7.php');

class knjz260Query extends Query {

    //年度科目一覧取得
    function selectQuery($model)
    {
        $query  = "SELECT dutysharecd, sharename ";
        $query .= "  FROM v_dutyshare_mst ";
        $query .= " WHERE year = '".$model->year."'"; 
        $query .= " ORDER BY dutysharecd";
        return $query;
    }

    //年度一覧取得
    function selectYearQuery()
    {
        return "select distinct(YEAR) as year from dutyshare_ydat order by year desc";
    }
    //学校分掌部一覧取得
    function selectdutyshareQuery($dutysharecd_array,$model)
    {
        $query  = "SELECT dutysharecd, sharename "; 
        $query .= "  FROM dutyshare_mst m ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM dutyshare_ydat y ";
        $query .= "                    WHERE y.year = '".$model->year."'";
        $query .= "                      AND y.dutysharecd = m.dutysharecd) ";
        $query .= " ORDER BY DUTYSHARECD ";
        return $query;
    }
    //UPDATE
    function &getUpdateQuery($model)
    {
        $all_light = false;
        //校務分掌部コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM dutyshare_ydat WHERE year = '" .$model->year ."'";
        $db->query($query);

        //校務分掌年度データの登録
        if ($array[0] !="") {
            $query = "INSERT INTO dutyshare_ydat(year,dutysharecd,registercd,updated)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //校務分掌部コード
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $all_light = true;
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return $all_light;
    }
}
?>
