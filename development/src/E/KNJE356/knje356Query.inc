<?php

require_once('for_php7.php');

class knje356Query extends Query {

    //コピー存在チェック
    function getCopyCheckLmst($year) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_SDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //一覧取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.L_CD, ";
        $query .= "     T1.L_CD || '：' || LD.L_NAME AS L_NAME, ";
        $query .= "     T1.S_CD, ";
        $query .= "     T1.S_NAME, ";
        $query .= "     T1.S_ABBV ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_SDAT T1 ";
        $query .= "     LEFT JOIN COLLEGE_EXAM_LDAT LD ON T1.YEAR = LD.YEAR ";
        $query .= "          AND T1.L_CD = LD.L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->leftYear}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.L_CD, ";
        $query .= "     T1.S_CD ";
        return $query;
    }

    //１レコード取得
    function getSmst($model, $l_cd, $s_cd) {

        $query  = " SELECT ";
        $query .= "     *  ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_SDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";
        $query .= "     AND L_CD = '{$l_cd}' ";
        $query .= "     AND S_CD = '{$s_cd}' ";

        return $query;
    }

    //大分類
    function getLmst($model, $l_cd) {

        $query  = " SELECT ";
        $query .= "     L_CD AS VALUE, ";
        $query .= "     L_CD || ':' || L_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_LDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";

        return $query;
    }

    //COPY
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " INSERT INTO COLLEGE_EXAM_SDAT ";
        $query .= " SELECT ";
        $query .= "     '{$model->leftYear}', ";
        $query .= "     L_CD, ";
        $query .= "     S_CD, ";
        $query .= "     S_NAME, ";
        $query .= "     S_ABBV, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_SDAT ";
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
        $data["S_CD"][TEXT]         = $fields["S_CD"];
        $data["S_NAME"][TEXT]       = $fields["S_NAME"];
        $data["S_ABBV"][TEXT]       = $fields["S_ABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "COLLEGE_EXAM_SDAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $data["S_NAME"][TEXT]       = $fields["S_NAME"];
        $data["S_ABBV"][TEXT]       = $fields["S_ABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR       = '{$model->leftYear}' ";
        $where .= "     AND L_CD  = '{$fields["L_CD"]}' ";
        $where .= "     AND S_CD  = '{$fields["S_CD"]}' ";

        $query = Query::updateSQL($data, "COLLEGE_EXAM_SDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM  ";
        $query .= "     COLLEGE_EXAM_SDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '{$model->leftYear}' ";
        $query .= "     AND L_CD  = '{$fields["L_CD"]}' ";
        $query .= "     AND S_CD  = '{$fields["S_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
