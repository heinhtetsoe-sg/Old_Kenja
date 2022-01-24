<?php

require_once('for_php7.php');

class knjz091aQuery extends Query {

    //年度取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM PRISCHOOL_YDAT GROUP BY YEAR ORDER BY YEAR desc";
    }

    //出身塾年度一覧取得
    function selectQuery($model)
    {
        $query  = "  SELECT";
        $query .= "      PRISCHOOLCD,";
        $query .= "      PRISCHOOL_NAME";
        $query .= "  FROM";
        $query .= "      V_PRISCHOOL_MST";
        $query .= "  WHERE";
        $query .= "      YEAR = '".$model->year."'"; 
        $query .= "  ORDER BY";
        $query .= "      PRISCHOOLCD";
        return $query;
    }
    
    
    //出身塾科目一覧取得
    function selectJuniorQuery($prischool_cd_array,$model)
    {

        $query  = "";
        $query .= "  SELECT";
        $query .= "      PRISCHOOLCD,";
        $query .= "      PRISCHOOL_NAME";
        $query .= "  FROM";
        $query .= "      PRISCHOOL_MST M";
        $query .= "  WHERE";
        $query .= "      NOT EXISTS (SELECT";
        $query .= "                      'X'";
        $query .= "                  FROM";
        $query .= "                      PRISCHOOL_YDAT Y";
        $query .= "                  WHERE";
        $query .= "                      Y.YEAR = '".$model->year."' AND";
        $query .= "                      Y.PRISCHOOLCD = M.PRISCHOOLCD";
        $query .= "                  )";
        $query .= "  ORDER BY";
        $query .= "      PRISCHOOLCD";
        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //出身塾コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE from PRISCHOOL_YDAT where YEAR = '" .$model->year ."'";
        $db->query($query);
        if ($array[0] != ""){
            //成績項目別得点データの登録
            $query = "INSERT INTO PRISCHOOL_YDAT(YEAR,PRISCHOOLCD,REGISTERCD,UPDATED)";
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
