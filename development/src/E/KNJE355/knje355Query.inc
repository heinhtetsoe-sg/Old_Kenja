<?php

require_once('for_php7.php');

class knje355Query extends Query {

    //コピー存在チェック
    function getCopyCheckLmst($year) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_LDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //一覧取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     L_CD, ";
        $query .= "     L_NAME, ";
        $query .= "     L_ABBV ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_LDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";
        $query .= " ORDER BY ";
        $query .= "     L_CD ";
        return $query;
    }

    //１レコード取得
    function getLmst($model, $l_cd) {

        $query  = " SELECT ";
        $query .= "     *  ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_LDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";
        $query .= "     AND L_CD = '{$l_cd}' ";

        return $query;
    }

    //COPY
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " INSERT INTO COLLEGE_EXAM_LDAT ";
        $query .= " SELECT ";
        $query .= "     '{$model->leftYear}', ";
        $query .= "     L_CD, ";
        $query .= "     L_NAME, ";
        $query .= "     L_ABBV, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_LDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->leftYear - 1)."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->leftYear;
        $data["L_CD"][TEXT]         = $fields["L_CD"];
        $data["L_NAME"][TEXT]       = $fields["L_NAME"];
        $data["L_ABBV"][TEXT]       = $fields["L_ABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "COLLEGE_EXAM_LDAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $data["L_NAME"][TEXT]       = $fields["L_NAME"];
        $data["L_ABBV"][TEXT]       = $fields["L_ABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR       = '{$model->leftYear}' ";
        $where .= "     AND L_CD  = '{$fields["L_CD"]}' ";

        $query = Query::updateSQL($data, "COLLEGE_EXAM_LDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM  ";
        $query .= "     COLLEGE_EXAM_LDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '{$model->leftYear}' ";
        $query .= "     AND L_CD  = '{$fields["L_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
