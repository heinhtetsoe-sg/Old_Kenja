<?php

require_once('for_php7.php');

class knjz250Query extends Query {

    //証明書種類年度一覧取得
    function selectQuery($model)
    {
        $query  = "SELECT m.certif_kindcd, m.kindname ";
        $query .= "FROM certif_kind_ydat y, certif_kind_mst m ";
        $query .= "WHERE y.certif_kindcd = m.certif_kindcd ";
        $query .= "AND y.year = '".$model->year."'";
        $query .= "ORDER BY m.certif_kindcd";
        return $query;
    }

    //年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT year FROM certif_kind_ydat ORDER BY year DESC";
    }

    //証明書一覧取得
    function selectcertifQuery($model)
    {
        $query  = "SELECT certif_kindcd,kindname ";
        $query .= "FROM certif_kind_mst m "; 
        $query .= "WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                  FROM certif_kind_ydat y ";
        $query .= "                  WHERE y.year = '".$model->year."'";
        $query .= "                  AND y.certif_kindcd = m.certif_kindcd) ";
        $query .= "ORDER BY certif_kindcd ";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        //証明書種類コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM certif_kind_ydat WHERE year = '" .$model->year ."'";
        $db->query($query);

        //証明書種類年度データの登録
        if ($array[0] !="") {
            $query = "INSERT INTO certif_kind_ydat(year,certif_kindcd,registercd,updated)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //証明書種類コード
                $query.= ",'".STAFFCD."'";                                      //登録者コード
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
