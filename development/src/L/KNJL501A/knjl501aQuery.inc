<?php

require_once('for_php7.php');

class knjl501aQuery extends Query {

    //課程年度一覧取得
    function selectQuery($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "   HOPE_COURSECODE, ";
        $query .= "   HOPE_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST ";
        $query .= " ORDER BY ";
        $query .= "   HOPE_COURSECODE ";

        return $query;
    }

    //課程年度一覧取得
    function selectYearQuery()
    {
        return " select distinct ENTEXAMYEAR AS year FROM ENTEXAM_HOPE_COURSE_YDAT ORDER BY year DESC ";
    }

    //課程一覧取得
    function selectCourseQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   m.HOPE_COURSECODE, ";
        $query .= "   m.HOPE_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST m ";
        $query .= " WHERE ";
        $query .= "   EXISTS (SELECT 'X' ";
        $query .= "               FROM ENTEXAM_HOPE_COURSE_YDAT y ";
        $query .= "               WHERE y.ENTEXAMYEAR = '".$model->year."'";
        $query .= "                     AND y.HOPE_COURSECODE = m.HOPE_COURSECODE) ";
        $query .= " ORDER BY ";
        $query .= "   m.HOPE_COURSECODE ";

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

        $query = "DELETE FROM ENTEXAM_HOPE_COURSE_YDAT WHERE ENTEXAMYEAR = '" .$model->year ."'";
        $db->query($query);

        if ($array[0] != ""){
            for ($i = 0; $i < get_count($array); $i++){
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["HOPE_COURSECODE"][TEXT]  = $array[$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "ENTEXAM_HOPE_COURSE_YDAT");
                $db->query($query);
            }
        } else {
            $model->year = "";
        }
        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。
        Query::dbCheckIn($db);
        return;
    }
}
?>
