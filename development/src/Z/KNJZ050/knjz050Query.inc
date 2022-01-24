<?php

require_once('for_php7.php');

class knjz050Query extends Query {

    //学科年度一覧取得
    function selectQuery($model)
    {
        $query =  "SELECT m.coursecd, m.majorcd, c.coursename, m.majorname ";
        $query .= "FROM v_major_mst m, course_mst c ";
        $query .= "WHERE m.year = '".$model->year."'";
        $query .= " AND m.coursecd = c.coursecd ";
        $query .= "ORDER BY m.coursecd,m.majorcd ";
        return $query;
    }

    //学科年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT year FROM major_ydat ORDER BY year DESC";
    }

    //学科一覧取得
    function selectJobQuery($majorcd_array,$model)
    {
        $query  = "SELECT m.coursecd, c.coursename, m.majorcd, m.majorname ";
        $query .= " FROM major_mst m, course_mst c";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                   FROM major_ydat y ";
        $query .= "                   WHERE y.year = '".$model->year."'";
        $query .= "                     AND y.coursecd = m.coursecd ";
        $query .= "                     AND y.majorcd = m.majorcd) ";
        $query .= " AND m.coursecd = c.coursecd ";
        $query .= " ORDER BY c.coursecd ";
        return $query;
    }
    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //学科コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM major_ydat WHERE year = '" .$model->year ."'";
        $db->query($query);

        $query = "INSERT INTO major_ydat(year,coursecd,majorcd,registercd,updated)";
        $query.= "VALUES";
        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .substr($array[$i],0,1)."'";                      //課程コード
                $query.= ",'" .substr($array[$i],1)."'";                        //学科コード
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
