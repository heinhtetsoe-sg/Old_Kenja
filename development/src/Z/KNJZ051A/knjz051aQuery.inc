<?php

require_once('for_php7.php');

class knjz051aQuery extends Query {

    //年度一覧取得
    function selectYearQuery() {

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     MAJOR_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //学科年度一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || '　' || T2.COURSENAME || '　' || T1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST T1, ";
        $query .= "     V_COURSE_MST T2 ";
        $query .= " WHERE "; 
        $query .= "     T1.YEAR     = T2.YEAR AND "; 
        $query .= "     T1.YEAR     = '".$model->year."' AND "; 
        $query .= "     T1.COURSECD = T2.COURSECD "; 
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学科一覧取得
    function selectMajorQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || '　' || T2.COURSENAME || '　' || T1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST T1, ";
        $query .= "     V_COURSE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".$model->year."' AND "; 
        $query .= "     T1.COURSECD = T2.COURSECD AND "; 
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     MAJOR_YDAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR     = '".$model->year."' AND ";
        $query .= "                     S1.COURSECD = T1.COURSECD AND ";
        $query .= "                     S1.MAJORCD  = T1.MAJORCD) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        //課程コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = " DELETE FROM MAJOR_YDAT WHERE YEAR = '".$model->year."' ";
        $db->query($query);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                list($coursecd, $majorcd) = preg_split("/-/", $array[$i]);
                //追加
                $data = array();
                $data["YEAR"][TEXT]         = $model->year;
                $data["COURSECD"][TEXT]     = $coursecd;
                $data["MAJORCD"][TEXT]      = $majorcd;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $db->query(Query::insertSQL($data, "MAJOR_YDAT"));
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
