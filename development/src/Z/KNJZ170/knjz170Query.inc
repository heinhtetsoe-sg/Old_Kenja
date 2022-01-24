<?php

require_once('for_php7.php');

class knjz170Query extends Query {

    //選択科目年度一覧取得
    function selectQuery($model)
    {
        $query  =  "select GROUPCD, GROUPNAME ";
        $query .= "  from v_electclass_mst ";
        $query .= " where YEAR = '". $model->year ."'"; 
        $query .= " order by GROUPCD";

        return $query;
    }
    
    //選択科目年度一覧取得
    function selectYearQuery()
    {
        return "select distinct YEAR from electclass_ydat order by year desc";
    }
    
    //選択科目一覧取得
    function selectElectQuery($groupcd_array,$model)
    {
        $query  = "select GROUPCD, GROUPNAME ";
        $query .= " from electclass_mst m ";
        $query .= " where not exists (select 'X' ";
        $query .= "                   from electclass_ydat y ";
        $query .= "                   where y.YEAR = '".$model->year."'";
        $query .= "                    and y.GROUPCD = m.GROUPCD) ";
        $query .= " order by GROUPCD ";
        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //群コード配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE from electclass_ydat where YEAR = '" .$model->year ."'";
        $db->query($query);

        //選択科目年度データの登録
        if ($array[0] != ""){
              $query = "INSERT INTO electclass_ydat(YEAR,GROUPCD,REGISTERCD,UPDATED)";
              $query.= "VALUES";
              for ($i = 0; $i < get_count($array); $i++){
                  $sp = ",";
                  if ($i == get_count($array)-1){
                      $sp = "";
                  }
                  $query.= "('" .$model->year ."'";                               //年度
                  $query.= ",'" .sprintf("%04d",$array[$i]) ."'";                 //選択科目コード
                  $query.= ",'" .STAFFCD."'";                                     //職員コード
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
