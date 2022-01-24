<?php

require_once('for_php7.php');

class knjz290Query extends Query {

    //出身学校年度一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT STAFFCD, STAFFNAME ";
        $query .= " FROM V_STAFF_MST ";
        $query .= " WHERE YEAR = '".$model->year."'"; 
        $query .= " ORDER by STAFFCD";
        return $query;
    }
    
    //出身学校年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM STAFF_YDAT GROUP BY YEAR ORDER BY YEAR DESC";
    }
    
    //職員一覧取得
    function selectJuniorQuery($model)
    {
        $query  = "SELECT STAFFCD,STAFFNAME ";
        $query .= "FROM STAFF_MST m ";
        $query .= "WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                  FROM STAFF_YDAT y ";
        $query .= "                  WHERE y.YEAR = '".$model->year."'";
        $query .= "                  AND y.STAFFCD = m.STAFFCD) ";
        $query .= "ORDER BY STAFFCD";
        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //出身学校コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE FROM STAFF_YDAT WHERE YEAR = '" .$model->year ."'";
        $db->query($query);
        if ($array[0] != ""){
            //成績項目別得点データの登録
            $query = "INSERT INTO STAFF_YDAT(YEAR,STAFFCD,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //職員コード
                $query.= ",'".STAFFCD."'";
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>