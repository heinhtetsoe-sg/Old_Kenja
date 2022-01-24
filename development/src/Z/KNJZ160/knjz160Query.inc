<?php

require_once('for_php7.php');

class knjz160Query extends Query {

    //施設年度一覧取得
    function selectQuery($model)
    {
        $query =  "SELECT FACCD, FACILITYNAME FROM V_FACILITY_MST ";
        $query .= " WHERE YEAR = '".$model->year."'"; 
        $query .= " ORDER BY FACCD";
        return $query;
    }
    
    //施設年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM FACILITY_YDAT ORDER BY YEAR DESC";
    }
    
    //施設一覧取得
    function selectFacQuery($faccd_array,$model)
    {
        $query  = "SELECT m.FACCD, m.FACILITYNAME";
        $query .= "  FROM FACILITY_MST m";
        $query .= " WHERE NOT EXISTS (SELECT 'X' FROM FACILITY_YDAT y";
        $query .= "                    WHERE y.YEAR = '".$model->year."'";
        $query .= "                      AND y.FACCD = m.FACCD)";
        return $query;
    }

    function &getUpdateQuery(&$model)
    {        
        //施設コード配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE from facility_ydat where YEAR = '" .$model->year ."'";
        $db->query($query);
        
        if ($array[0] != ""){
            //成績項目別得点データの登録
            $query = "INSERT INTO facility_ydat (YEAR,FACCD,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                              //科目コード
                $query.= ",'" .STAFFCD ."'";
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
    
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
