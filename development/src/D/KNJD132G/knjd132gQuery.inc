<?php

require_once('for_php7.php');

class knjd132gQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年名取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //所見取得
    function getHreportremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     H2.TOTALSTUDYTIME, ";
        $query .= "     H1.COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT H1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT H2 ON H2.YEAR     = H1.YEAR ";
        $query .= "                                   AND H2.SEMESTER = '9' ";
        $query .= "                                   AND H2.SCHREGNO = H1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         H1.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND H1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND H1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT -- HREPORTREMARK_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $checkT = $db->getOne(knjd132gQuery::getHreportremarkDatCnt($model, $model->semester9));
        $checkC = $db->getOne(knjd132gQuery::getHreportremarkDatCnt($model, $model->semester));

        // HREPORTREMARK_DAT(TOTALSTUDYTIME)
        $data = array();
        $data["TOTALSTUDYTIME"][TEXT]   = $model->field["TOTALSTUDYTIME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($checkT == 0) {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->semester9;
            $data["SCHREGNO"][TEXT]     = $model->schregno;

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "         YEAR     = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '{$model->semester9}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }
        // HREPORTREMARK_DAT(COMMUNICATION)
        $data = array();
        $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($checkC == 0) {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["SCHREGNO"][TEXT]     = $model->schregno;

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "         YEAR     = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '{$model->semester}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //カウント取得--HREPORTREMARK_DAT
    function getHreportremarkDatCnt($model, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
}
?>
