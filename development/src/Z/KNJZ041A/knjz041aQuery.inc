<?php

require_once('for_php7.php');

class knjz041aQuery extends Query {

    //課程年度一覧取得
    function selectYearQuery() {

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     COURSE_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //課程年度一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     COURSENAME ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MST ";
        $query .= " WHERE "; 
        $query .= "     YEAR = '".$model->year."' "; 
        $query .= " ORDER BY ";
        $query .= "     COURSECD ";

        return $query;
    }


    //課程一覧取得
    function selectCourseQuery($coursecd_array, $model) {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     COURSENAME ";
        $query .= " FROM ";
        $query .= "     COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     COURSE_YDAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T2.YEAR = '".$model->year."' AND ";
        $query .= "                     T2.COURSECD = T1.COURSECD) ";
        $query .= " ORDER BY ";
        $query .= "     COURSECD ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        //課程コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM COURSE_YDAT WHERE YEAR = '".$model->year."' ";
        $db->query($query);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                //追加
                $data = array();
                $data["YEAR"][TEXT]         = $model->year;
                $data["COURSECD"][TEXT]     = $array[$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $db->query(Query::insertSQL($data, "COURSE_YDAT"));
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
