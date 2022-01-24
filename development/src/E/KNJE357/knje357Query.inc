<?php

require_once('for_php7.php');

class knje357Query extends Query {

    //コピー存在チェック
    function getCopyCheckPrg($year) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_PROGRAM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //一覧取得
    function getList($model) {

        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ADVERTISE_DIV, ";
        $query .= "     T1.ADVERTISE_DIV || '：' || N1.NAME1 AS ADVERTISE_NAME, ";
        $query .= "     T1.PROGRAM_CD, ";
        $query .= "     T1.PROGRAM_NAME, ";
        $query .= "     T1.PROGRAM_ABBV ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_PROGRAM_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E044' ";
        $query .= "          AND T1.ADVERTISE_DIV = N1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->leftYear}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ADVERTISE_DIV, ";
        $query .= "     T1.PROGRAM_CD ";
        return $query;
    }

    //１レコード取得
    function getPrgDat($model, $advertise_div, $program_cd) {

        $query  = " SELECT ";
        $query .= "     *  ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_PROGRAM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";
        $query .= "     AND ADVERTISE_DIV = '{$advertise_div}' ";
        if ($program_cd === "0") {
            $query .= "     AND PROGRAM_CD = '00' ";
        } else {
            $query .= "     AND PROGRAM_CD = '{$program_cd}' ";
        }

        return $query;
    }

    //募集
    function getAdveMst($model) {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";
        $query .= "     AND NAMECD1 = 'E044' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //COPY
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " INSERT INTO COLLEGE_EXAM_PROGRAM_DAT ";
        $query .= " SELECT ";
        $query .= "     '{$model->leftYear}', ";
        $query .= "     ADVERTISE_DIV, ";
        $query .= "     PROGRAM_CD, ";
        $query .= "     PROGRAM_NAME, ";
        $query .= "     PROGRAM_ABBV, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_PROGRAM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->leftYear - 1)."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = $model->leftYear;
        $data["ADVERTISE_DIV"][TEXT]    = $fields["ADVERTISE_DIV"];
        $data["PROGRAM_CD"][TEXT]       = $fields["PROGRAM_CD"];
        $data["PROGRAM_NAME"][TEXT]     = $fields["PROGRAM_NAME"];
        $data["PROGRAM_ABBV"][TEXT]     = $fields["PROGRAM_ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "COLLEGE_EXAM_PROGRAM_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $data["PROGRAM_NAME"][TEXT] = $fields["PROGRAM_NAME"];
        $data["PROGRAM_ABBV"][TEXT] = $fields["PROGRAM_ABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR       = '{$model->leftYear}' ";
        $where .= "     AND ADVERTISE_DIV  = '{$fields["ADVERTISE_DIV"]}' ";
        $where .= "     AND PROGRAM_CD  = '{$fields["PROGRAM_CD"]}' ";

        $query = Query::updateSQL($data, "COLLEGE_EXAM_PROGRAM_DAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM  ";
        $query .= "     COLLEGE_EXAM_PROGRAM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '{$model->leftYear}' ";
        $query .= "     AND ADVERTISE_DIV  = '{$fields["ADVERTISE_DIV"]}' ";
        $query .= "     AND PROGRAM_CD  = '{$fields["PROGRAM_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
