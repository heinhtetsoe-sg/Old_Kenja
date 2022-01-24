<?php

require_once('for_php7.php');

class knje384query extends Query {

    //存在チェック -- TOEFL_MST
    function getToeflMst($year, $cntFlag = false) {
        $query  = " SELECT ";
        if ($cntFlag) {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     BASE_SCORE ";
            $query .= "     , RANGE_F ";
            $query .= "     , RANGE_T ";
        }
        $query .= " FROM ";
        $query .= "     TOEFL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //存在チェック -- TOEFL_MST
    function getCopyQuery() {
        $query  = " INSERT INTO TOEFL_MST ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' ";
        $query .= "     , BASE_SCORE ";
        $query .= "     , RANGE_F ";
        $query .= "     , RANGE_T ";
        $query .= "     , REGISTERCD ";
        $query .= "     , UPDATED TOEFL_MST ";
        $query .= " FROM ";
        $query .= "     TOEFL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";

        return $query;
    }

    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();

            //DELETE
            $query  = " DELETE FROM TOEFL_MST ";
            $query .= "     WHERE YEAR = '".CTRL_YEAR."' ";
            $db->query($query);

            //INSERT
            $data = array();
            $data["YEAR"][TEXT]            = CTRL_YEAR;
            $data["BASE_SCORE"][TEXT]      = $model->field["BASE_SCORE"];
            $data["RANGE_F"][TEXT]         = $model->field["RANGE_F"];
            $data["RANGE_T"][TEXT]         = $model->field["RANGE_T"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][NUMBER]       = "SYSDATE()";

            $query = Query::insertSQL($data, "TOEFL_MST");
            $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
