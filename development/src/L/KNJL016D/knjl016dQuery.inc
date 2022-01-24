<?php

require_once('for_php7.php');

class knjl016dQuery extends Query {
    //出身学校年度一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     MAIN.DECISION_CD AS VALUE, ";
        $query .= "     MAIN.DECISION_CD || ':' || MAIN.DECISION_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERNAL_DECISION_MST MAIN ";
        $query .= "     INNER JOIN ENTEXAM_INTERNAL_DECISION_YDAT YDAT ON MAIN.DECISION_CD = YDAT.DECISION_CD ";
        $query .= " WHERE "; 
        $query .= "     YDAT.ENTEXAMYEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     MAIN.DECISION_CD ";

        return $query;
    }

    //出身学校年度一覧取得
    function selectYearQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERNAL_DECISION_YDAT ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR desc";

        return $query;
    }

    //出身学校科目一覧取得
    function selectJuniorQuery($model) {
        $query  = " SELECT ";
        $query .= "     MAIN.DECISION_CD AS VALUE, ";
        $query .= "     MAIN.DECISION_CD || ':' || MAIN.DECISION_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERNAL_DECISION_MST MAIN ";
        $query .= " WHERE NOT EXISTS (SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ENTEXAM_INTERNAL_DECISION_YDAT YDAT ";
        $query .= "                   WHERE ";
        $query .= "                       YDAT.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "                   AND YDAT.DECISION_CD = MAIN.DECISION_CD) ";
        $query .= " ORDER BY ";
        $query .= "     MAIN.DECISION_CD ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        //内部判定コードを配列に格納
        $decCdArr = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "DELETE FROM ENTEXAM_INTERNAL_DECISION_YDAT WHERE ENTEXAMYEAR = '" .$model->year ."'";

        $db->query($query);
        if ($decCdArr[0] !="") {
            foreach ($decCdArr as $val) {
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["DECISION_CD"][TEXT]      = $val;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "ENTEXAM_INTERNAL_DECISION_YDAT");
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
