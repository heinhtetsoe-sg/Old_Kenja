<?php

require_once('for_php7.php');

class knjd132lQuery extends Query {
    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' AND ";
        $query .= "     GRADE   = '".$model->grade."' ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getHreportremarkDat($model, $semester) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->exp_year}' AND ";
        $query .= "     SEMESTER    = '{$semester}' AND ";
        $query .= "     SCHREGNO    = '{$model->schregno}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd132lQuery::getHreportremarkDat($model, $model->field["SEMESTER"]));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '{$model->exp_year}' AND ";
            $where .= "     SEMESTER    = '{$model->field["SEMESTER"]}' AND ";
            $where .= "     SCHREGNO    = '{$model->schregno}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
