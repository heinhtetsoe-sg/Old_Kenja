<?php

require_once('for_php7.php');

class knjz391Query extends Query {
    //年度一覧取得
    function getYear() {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS (VALUES('".CTRL_YEAR."', '".CTRL_YEAR."')) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_BUS_YMST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //リスト取得
    function getChildcareBusYmst($model, $course_cd="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_BUS_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($course_cd) {
            $query .= " AND COURSE_CD   = '".$course_cd."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     COURSE_CD ";
        }

        return $query;
    }

    //更新処理
    function &getInsertQuery($model) {
        //削除
        knjz391Query::getDeleteQuery($model);

        //更新
        $db = Query::dbCheckOut();
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["COURSE_CD"][TEXT]        = $model->field["COURSE_CD"];
        $data["SCHEDULE_CD"][TEXT]      = $model->field["SCHEDULE_CD"];
        $data["BUS_NAME"][TEXT]         = $model->field["BUS_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "CHILDCARE_BUS_YMST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHILDCARE_BUS_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     COURSE_CD   = '".$model->field["COURSE_CD"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //前年度データ取得
    function getPreYearData($model, $flg) {
        //前年度
        $pre_year = $model->year - 1;

        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CHILDCARE_BUS_YMST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$pre_year."' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     CHILDCARE_BUS_YMST S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR         = '".$model->year."' AND ";
        $query .= "                     S1.COURSE_CD    = T1.COURSE_CD ";
        $query .= "                 ) ";

        return $query;
    }

    //コピー
    function &copyInsertQuery($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["COURSE_CD"][TEXT]        = $Row["COURSE_CD"];
        $data["SCHEDULE_CD"][TEXT]      = $Row["SCHEDULE_CD"];
        $data["BUS_NAME"][TEXT]         = $Row["BUS_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "CHILDCARE_BUS_YMST");
        return $query;
    }
}
?>
