<?php

require_once('for_php7.php');

class knjz095Query extends Query {

    //協力校年度一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT T1.PARTNER_SCHOOLCD, T1.PARTNER_SCHOOL_NAME, T2.NAME1 ";
        $query .= " from PARTNER_SCHOOL_MST T1 ";
        $query .= "     INNER JOIN PARTNER_SCHOOL_YDAT T2 ON T2.PARTNER_SCHOOLCD = T1.PARTNER_SCHOOLCD ";
        $query .= "                                      AND T2.YEAR = '".$model->year."'"; 
        $query .= "     LEFT  JOIN NAME_MST T2 ON T2.NAMECD2 = T1.PARTNER_SCHOOL_TYPE ";
        $query .= "                           AND T2.NAMECD1 = 'L019' "; 
        $query .= " ORDER BY T1.PARTNER_SCHOOLCD";

        return $query;
    }

    //協力校年度一覧取得
    function selectYearQuery($model)
    {
        $tableName2 = "PARTNER_SCHOOL_YDAT";
        return "SELECT DISTINCT YEAR FROM {$tableName2} GROUP BY YEAR ORDER BY YEAR desc";
    }

    //協力校科目一覧取得
    function selectJuniorQuery($PARTNER_SCHOOL_cd_array, $model)
    {
        $tableName =  "PARTNER_SCHOOL_MST";
        $tableName2 = "PARTNER_SCHOOL_YDAT";
        $query  = "select PARTNER_SCHOOLCD, PARTNER_SCHOOL_NAME, T2.NAME1 ";
        $query .= "from {$tableName} m ";
        $query .= "     LEFT JOIN NAME_MST T2 ON  T2.NAMECD2 = m.PARTNER_SCHOOL_TYPE ";
        $query .= "                          AND  T2.NAMECD1 = 'L019' "; 
        $query .= "where not exists (select 'X' ";
        $query .= "                  from {$tableName2} y ";
        $query .= "                  where y.YEAR = '".$model->year."'";
        $query .= "                  and y.PARTNER_SCHOOLCD = m.PARTNER_SCHOOLCD) ";
        $query .= "ORDER BY PARTNER_SCHOOLCD";

        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //協力校コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $tableName2 = "PARTNER_SCHOOL_YDAT";
        $query = "DELETE from {$tableName2} where YEAR = '" .$model->year ."'";
        $db->query($query);
        if ($array[0] != ""){
            //成績項目別得点データの登録
                $query = "INSERT INTO {$tableName2}(YEAR,PARTNER_SCHOOLCD,REGISTERCD,UPDATED)";
                $query.= "VALUES";
                $sp = "";
                for ($i = 0; $i < get_count($array); $i++){
                    if ($i > 0 && $i % 5000 == 0) {
                        $db->query($query);

                        $query = "INSERT INTO {$tableName2}(YEAR,PARTNER_SCHOOL_    CD,REGISTERCD,UPDATED)";
                        $query.= "VALUES";
                        $sp = "";
                    }
                    $query.= $sp;
                    $query.= "('" .$model->year ."'";                               //年度
                    $query.= ",'" .$array[$i] ."'";                                 //科目コード
                    $query.= ",'".STAFFCD."'";
                    $query.= ",'" .$updated ."')";                                  //更新日付
                    $sp = ",";
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
