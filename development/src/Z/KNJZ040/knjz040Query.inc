<?php

require_once('for_php7.php');

class knjz040Query extends Query {

    //課程年度一覧取得
    function selectQuery($model)
    {
        $query  = "SELECT coursecd, coursename ";
        $query .= " FROM v_course_mst ";
        $query .= " WHERE year = '".$model->year."'"; 
        $query .= " ORDER BY coursecd";
        return $query;
    }

    //課程年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT year FROM course_ydat ORDER BY year DESC";
    }

    //課程一覧取得
    function selectCourseQuery($coursecd_array,$model)
    {
        $query  = "SELECT coursecd, coursename ";
        $query .= " FROM course_mst m ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM course_ydat y ";
        $query .= "                    WHERE y.year = '".$model->year."'";
        $query .= "                      AND y.coursecd = m.coursecd) ";
        $query .= " ORDER BY coursecd ";
        return $query;
    }
    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //課程コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM course_ydat WHERE year = '" .$model->year ."'";
        $db->query($query);

        $query = "INSERT INTO course_ydat(year,coursecd,registercd,updated)";
        $query.= "VALUES";
        if ($array[0] != ""){
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$array[$i] ."'";                                 //課程コード
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
