<?php

require_once('for_php7.php');

class knjz090Query extends Query {

    //出身学校年度一覧取得
    function selectQuery($model)
    {
        $query  = " select FINSCHOOLCD, FINSCHOOL_NAME ";
        $query .= " from V_FINSCHOOL_MST ";
        $query .= " where YEAR = '".$model->year."'"; 
        $query .= " order by FINSCHOOLCD";
        return $query;
    }
    
    //出身学校年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM FINSCHOOL_YDAT GROUP BY YEAR ORDER BY YEAR desc";
    }
    
    //出身学校科目一覧取得
    function selectJuniorQuery($FINSCHOOL_cd_array,$model)
    {
        $query  = "select FINSCHOOLCD, FINSCHOOL_NAME ";
        $query .= "from FINSCHOOL_MST m ";
        $query .= "where not exists (select 'X' ";
        $query .= "                  from FINSCHOOL_YDAT y ";
        $query .= "                  where y.YEAR = '".$model->year."'";
        $query .= "                  and y.finschoolcd = m.finschoolcd) ";
        $query .= "ORDER BY FINSCHOOLCD";
        
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
        
        $query = "DELETE from FINSCHOOL_YDAT where YEAR = '" .$model->year ."'";
        $db->query($query);
        if ($array[0] != ""){
            //成績項目別得点データの登録
            $query = "INSERT INTO FINSCHOOL_YDAT(YEAR,FINSCHOOLCD,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //科目コード
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
