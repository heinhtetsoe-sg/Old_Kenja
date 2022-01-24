<?php

require_once('for_php7.php');

class knjz281aQuery extends Query {

    //年度一覧取得
    function selectYearQuery() {

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     JOB_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //職名年度一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.JOBCD AS VALUE, ";
        $query .= "     T1.JOBCD || '　' || T2.JOBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOB_YDAT T1, ";
        $query .= "     JOB_MST T2 ";
        $query .= " WHERE "; 
        $query .= "     T1.YEAR = '".$model->year."' AND "; 
        $query .= "     T1.JOBCD = T2.JOBCD "; 
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職名一覧取得
    function selectJobQuery($model) {
        $query  = " SELECT ";
        $query .= "     JOBCD AS VALUE, ";
        $query .= "     JOBCD || '　' || JOBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOB_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     JOB_YDAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR = '".$model->year."' AND ";
        $query .= "                     S1.JOBCD  = T1.JOBCD) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        //職名コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = " DELETE FROM JOB_YDAT WHERE YEAR = '".$model->year."' ";
        $db->query($query);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                //追加
                $data = array();
                $data["YEAR"][TEXT]         = $model->year;
                $data["JOBCD"][TEXT]        = $array[$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $db->query(Query::insertSQL($data, "JOB_YDAT"));
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
