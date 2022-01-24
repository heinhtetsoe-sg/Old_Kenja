<?php

require_once('for_php7.php');

class knjd132pQuery extends Query {
    //所見取得
    function getHreportremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //INSERT -- HREPORTREMARK_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd132pQuery::getHreportremarkDat($model));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->semester;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "         YEAR     = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '{$model->semester}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
