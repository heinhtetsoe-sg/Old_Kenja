<?php

require_once('for_php7.php');

class knjh210Query extends Query {

    //寮年度一覧取得
    function selectQuery($model)
    {
        $query =  "select m.DOMI_CD, m.DOMI_NAME from DOMITORY_YDAT d, DOMITORY_MST m ";
        $query .= "where d.DOMI_CD = m.DOMI_CD ";
        $query .= "and d.YEAR = '".$model->year."'"; 
        $query .= "order by m.DOMI_CD";
        return $query;
    }

    //年度一覧取得
    function selectYearQuery()
    {
        $query  = " select distinct YEAR from DOMITORY_YDAT ";
        $query .= " order by year desc";
        return $query;
    }

    //寮マスタデータ一覧取得
    function selectClassQuery($model)
    {
        $query  = "SELECT dm.DOMI_CD, dm.DOMI_NAME ";
        $query .= "FROM DOMITORY_MST dm ";
        $query .= "WHERE NOT EXISTS (SELECT 'X' ";                          /* NO001↓ */
        $query .= "                  FROM DOMITORY_YDAT dy";
        $query .= "                  WHERE dy.year = '".$model->year."'";
        $query .= "                  AND dy.DOMI_CD = dm.DOMI_CD)";         /* NO001↑ */
        $query .= " ORDER BY dm.DOMI_CD ";
        return $query;
        
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //寮コードを配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE from DOMITORY_YDAT where YEAR = '" .$model->year ."'";
        $db->query($query);
        
        if ($array[0] != ""){
            //寮年度データの登録
            $query = "INSERT INTO DOMITORY_YDAT(YEAR, DOMI_CD,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //寮コード
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
