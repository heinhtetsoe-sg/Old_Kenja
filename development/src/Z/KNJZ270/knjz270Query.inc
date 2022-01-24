<?php

require_once('for_php7.php');

class knjz270Query extends Query {

    //所属年度一覧取得
    function selectQuery($model)
    {
        $query  = "SELECT sectioncd, sectionname ";
        $query .= "  FROM v_section_mst ";
        $query .= " WHERE year = '".$model->year."'"; 
        $query .= " ORDER BY sectioncd ";
        return $query;
    }

    //所属年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT year FROM section_ydat ORDER BY year DESC";
    }

    //所属一覧取得
    function selectSectionQuery($sectioncd_array,$model)
    {
        $query  = "SELECT sectioncd, sectionname ";
        $query .= "  FROM SECTION_MST m ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM section_ydat y ";
        $query .= "                    WHERE y.year = '".$model->year."'";
        $query .= "                      AND y.sectioncd = m.sectioncd) ";
        $query .= " ORDER BY sectioncd ";
        return $query;
    }
    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //所属コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM section_ydat WHERE year = '" .$model->year ."'";
        $db->query($query);

        if ($array[0] != "") {
            $query = "INSERT INTO section_ydat(year,sectioncd,registercd,updated)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //所属コード
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
