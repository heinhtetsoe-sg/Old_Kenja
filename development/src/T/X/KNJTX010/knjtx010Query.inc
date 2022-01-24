<?php

require_once('for_php7.php');

class knjtx010Query extends Query {

    //出身学校年度一覧取得
    function selectQuery($model)
    {
        $tableName = "FINSCHOOL_MST";
        $tableName2 = "V_FINSCHOOL_MST";
        $query  = " select T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, T2.NAME1 ";
        $query .= " from {$tableName2} T1 ";
        $query .= "     INNER JOIN {$tableName} I1 ON T1.FINSCHOOLCD = I1.FINSCHOOLCD ";
        $query .= "     LEFT  JOIN NAME_MST T2 ON T2.NAMECD2 = I1.FINSCHOOL_TYPE ";
        $query .= "                           AND T2.NAMECD1 = 'L019' "; 
        $query .= " where T1.YEAR = '".$model->year."'"; 
        $query .= " order by T1.FINSCHOOLCD";

        return $query;
    }
    
    //出身学校年度一覧取得
    function selectYearQuery($model)
    {
        $tableName2 = "FINSCHOOL_YDAT";
        return "SELECT DISTINCT YEAR FROM {$tableName2} GROUP BY YEAR ORDER BY YEAR desc";
    }
    
    //出身学校科目一覧取得
    function selectJuniorQuery($FINSCHOOL_cd_array, $model)
    {
        $tableName =  "FINSCHOOL_MST";
        $tableName2 = "FINSCHOOL_YDAT";
        $query  = "select FINSCHOOLCD, FINSCHOOL_NAME, T2.NAME1 ";
        $query .= "from {$tableName} m ";
        $query .= "     LEFT JOIN NAME_MST T2 ON  T2.NAMECD2 = m.FINSCHOOL_TYPE ";
        $query .= "                          AND  T2.NAMECD1 = 'L019' "; 
        $query .= "where not exists (select 'X' ";
        $query .= "                  from {$tableName2} y ";
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
        
        $tableName2 = "FINSCHOOL_YDAT";
        $query = "DELETE from {$tableName2} where YEAR = '" .$model->year ."'";
        $db->query($query);
        if ($array[0] != ""){
            //成績項目別得点データの登録
            $query = "INSERT INTO {$tableName2}(YEAR,FINSCHOOLCD,REGISTERCD,UPDATED)";
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
