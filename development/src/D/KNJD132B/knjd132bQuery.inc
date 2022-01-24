<?php

require_once('for_php7.php');

class knjd132bQuery extends Query {

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' ";
        $query .= "     AND GRADE   = '".$model->grade."' ";

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
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部活動参照
    function getClub($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L2.CLUBNAME, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND L1.CLUBCD = T1.CLUBCD ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_MST L2 ON L2.CLUBCD = T1.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     L1.SEQ, ";
        $query .= "     L2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                                     AND L1.SEQ  = T1.SEQ ";
        $query .= " LEFT JOIN ";
        $query .= "     COMMITTEE_MST L2 ON  L2.COMMITTEE_FLG = T1.COMMITTEE_FLG ";
        $query .= "                      AND L2.COMMITTEECD   = T1.COMMITTEECD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }

    //１レコード取得
    function getHreportremarkDat($model, $semester) {
        $query .= " SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= "     , REMARK1 ";
        $query .= "     , ATTENDREC_REMARK ";
        $query .= "     , COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    function getHreportremarkDetailDat($model, $div, $code) {
        $query .= " SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= "     , REMARK1 ";
        $query .= "     , REMARK2 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND DIV = '{$div}' ";
        $query .= "     AND CODE = '{$code}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }


    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //特別活動の記録 存在チェック
        $check = $db->getCol(knjd132bQuery::getHreportremarkDetailDat($model,"01", "01"));
        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = "9";
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["DIV"][TEXT]                  = "01";
            $data["CODE"][TEXT]                 = "01";
            $data["REMARK1"][TEXT]              = $model->field["DETAIL_REMARK1"];
            $data["REMARK2"][TEXT]              = $model->field["DETAIL_REMARK2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data["REMARK1"][TEXT]              = $model->field["DETAIL_REMARK1"];
            $data["REMARK2"][TEXT]              = $model->field["DETAIL_REMARK2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";
            $where .= "     AND DIV = '01' ";
            $where .= "     AND CODE = '01' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }
        //特別活動の記録 存在チェック
        $check = $db->getCol(knjd132bQuery::getHreportremarkDetailDat($model,"01", "02"));
        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = "9";
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["DIV"][TEXT]                  = "01";
            $data["CODE"][TEXT]                 = "02";
            $data["REMARK1"][TEXT]              = $model->field["DETAIL_REMARK1_CHK"];
            $data["REMARK2"][TEXT]              = $model->field["DETAIL_REMARK2_CHK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data["REMARK1"][TEXT]              = $model->field["DETAIL_REMARK1_CHK"];
            $data["REMARK2"][TEXT]              = $model->field["DETAIL_REMARK2_CHK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";
            $where .= "     AND DIV = '01' ";
            $where .= "     AND CODE = '02' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        //道徳、特記すべき事項 存在チェック
        $check = $db->getCol(knjd132bQuery::getHreportremarkDat($model, "9"));
        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = "9";
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        //出欠の記録備考 存在チェック
        $check = $db->getCol(knjd132bQuery::getHreportremarkDat($model, $model->field["SEMESTER"]));
        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

}
?>
