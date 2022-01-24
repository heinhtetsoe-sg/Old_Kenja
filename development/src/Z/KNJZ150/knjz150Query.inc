<?php

require_once('for_php7.php');

class knjz150Query extends Query {

    //対象年度年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM TEXTBOOK_YDAT ORDER BY YEAR DESC";
    }

    //教科書年度一覧取得
    function selectTextBookYearQuery($model)
    {
        $query  = "SELECT TEXTBOOKCD, TEXTBOOKNAME ";
        $query .= "FROM V_TEXTBOOK_MST ";
        $query .= "WHERE YEAR = '".$model->year."' "; 
        $query .= "order by TEXTBOOKCD";
        return $query;
    }

    //教科書一覧取得
    function selectTextBookQuery($textbookcd_array,$model)
    {
        $query  = " SELECT TEXTBOOKCD, TEXTBOOKNAME ";
        $query .= " FROM TEXTBOOK_MST m ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                   FROM TEXTBOOK_YDAT y ";
        $query .= "                   WHERE y.YEAR = '".$model->year."'";
        $query .= "                   AND y.TEXTBOOKCD = m.TEXTBOOKCD) ";
        $query .= " ORDER BY TEXTBOOKCD ";
        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {
        //教科書コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM TEXTBOOK_YDAT WHERE YEAR = '" .$model->year ."'";
        $db->query($query);

        $query = "INSERT INTO TEXTBOOK_YDAT(YEAR,TEXTBOOKCD,REGISTERCD,UPDATED)";
        $query.= "VALUES";
        if ($array[0] != ""){
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //教科書コード
                $query.= ",'" .STAFFCD ."'";
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
