<?php

require_once('for_php7.php');

class knjd155rQuery extends Query {

    //１レコード取得
    function getTrainRow($model, $schregno, $semester="")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_REMARK_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        if($semester) {
            $query .= "     AND SEMESTER = '".$semester."' ";
            $query .= "     AND TESTKINDCD = '00' ";
            $query .= "     AND TESTITEMCD = '00' ";
            $query .= "     AND SCORE_DIV  = '00' ";
            $query .= "     AND REMARK_DIV = '6' ";
        } else {
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND TESTKINDCD = '00' ";
            $query .= "     AND TESTITEMCD = '00' ";
            $query .= "     AND SCORE_DIV  = '00' ";
            $query .= "     AND REMARK_DIV = '5' ";
        }
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        /**********/
        /* 通  年 */
        /**********/
        //存在チェック
        $check = $db->getCol(knjd155rQuery::getTrainRow($model, $model->schregno));

        $data = array();
        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = "9";
            $data["TESTKINDCD"][TEXT]       = "00";
            $data["TESTITEMCD"][TEXT]       = "00";
            $data["SCORE_DIV"][TEXT]        = "00";
            $data["REMARK_DIV"][TEXT]       = "5";
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HEXAM_RECORD_REMARK_SDIV_DAT"));
        } else {
            $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER   = '9' ";
            $where .= "     AND TESTKINDCD = '00' ";
            $where .= "     AND TESTITEMCD = '00' ";
            $where .= "     AND SCORE_DIV  = '00' ";
            $where .= "     AND REMARK_DIV = '5' ";
            $where .= "     AND SCHREGNO   = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HEXAM_RECORD_REMARK_SDIV_DAT", $where));
        }

        /**********/
        /* 年  度 */
        /**********/
        //存在チェック
        $check = $db->getCol(knjd155rQuery::getTrainRow($model, $model->schregno, $model->field["SEMESTER"]));

        $data = array();
        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]       = "00";
            $data["TESTITEMCD"][TEXT]       = "00";
            $data["SCORE_DIV"][TEXT]        = "00";
            $data["REMARK_DIV"][TEXT]       = "6";
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["REMARK2"][TEXT]          = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HEXAM_RECORD_REMARK_SDIV_DAT"));
        } else {
            $data["REMARK2"][TEXT]          = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND TESTKINDCD = '00' ";
            $where .= "     AND TESTITEMCD = '00' ";
            $where .= "     AND SCORE_DIV  = '00' ";
            $where .= "     AND REMARK_DIV = '6' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HEXAM_RECORD_REMARK_SDIV_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
