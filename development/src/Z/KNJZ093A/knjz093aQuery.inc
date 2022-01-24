<?php

require_once('for_php7.php');

class knjz093aQuery extends Query {

    //出身学校年度一覧取得
    function selectYearQuery($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //出身学校年度一覧取得
    function selestFinschoolYdat($model) {

        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     CASE WHEN LENGTH(T2.NAME1) > 0 THEN T2.NAME1 ELSE '　' END AS NAME1 ";
        $query .= " FROM ";
        $query .= "     V_FINSCHOOL_MST T1 ";
        $query .= "     INNER JOIN FINSCHOOL_MST I1 ON T1.FINSCHOOLCD = I1.FINSCHOOLCD ";
        $query .= "     LEFT  JOIN NAME_MST T2 ON T2.NAMECD2 = I1.FINSCHOOL_TYPE ";
        $query .= "                           AND T2.NAMECD1 = 'L019' "; 
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.FINSCHOOLCD ";

        return $query;
    }

    //出身学校科目一覧取得
    function selectFinschoolMst($model) {

        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     CASE WHEN LENGTH(T2.NAME1) > 0 THEN T2.NAME1 ELSE '　' END AS NAME1 ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON  T2.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= "                          AND  T2.NAMECD1 = 'L019' "; 
        $query .= " WHERE  ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     FINSCHOOL_YDAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR = '".$model->year."' AND ";
        $query .= "                     S1.FINSCHOOLCD = T1.FINSCHOOLCD) ";
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOLCD ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        //出身学校コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     FINSCHOOL_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $db->query($query);

        if ($array[0] != "") {
            //出身学校年度データの更新
            for ($i = 0; $i < get_count($array); $i++){
                $data["YEAR"][TEXT]             = $model->year;
                $data["FINSCHOOLCD"][TEXT]      = $array[$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "FINSCHOOL_YDAT");
                $db->query($query);
            }
        } else {
            $model->year = "";
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
