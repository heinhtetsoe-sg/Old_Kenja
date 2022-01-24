<?php

require_once('for_php7.php');

class knjz151Query extends Query {

    //発行者年度一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT t1.ISSUECOMPANYCD, t1.ISSUECOMPANYNAME ";
        $query .= " FROM ISSUECOMPANY_MST t1,ISSUECOMPANY_YDAT t2 ";
        $query .= " WHERE t2.YEAR = '".$model->year."'";
        $query .= "       AND t2.ISSUECOMPANYCD = t1.ISSUECOMPANYCD";
        $query .= " ORDER by ISSUECOMPANYCD";
        return $query;
    }
    
    //発行者年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM ISSUECOMPANY_YDAT GROUP BY YEAR ORDER BY YEAR DESC";
    }
    
    //職員一覧取得
    function selectJuniorQuery($model)
    {
        $query  = "SELECT ISSUECOMPANYCD,ISSUECOMPANYNAME ";
        $query .= "FROM ISSUECOMPANY_MST m ";
        $query .= "WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                  FROM ISSUECOMPANY_YDAT y ";
        $query .= "                  WHERE y.YEAR = '".$model->year."'";
        $query .= "                  AND y.ISSUECOMPANYCD = m.ISSUECOMPANYCD) ";
        $query .= "ORDER BY ISSUECOMPANYCD";
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
        
        $query = "DELETE FROM ISSUECOMPANY_YDAT WHERE YEAR = '" .$model->year ."'";
        $db->query($query);
        if ($array[0] != ""){
            //成績項目別得点データの登録
            $query = "INSERT INTO ISSUECOMPANY_YDAT(YEAR,ISSUECOMPANYCD,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //発行者コード
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
