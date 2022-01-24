<?php

require_once('for_php7.php');

class knjd137gQuery extends Query {
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

    //所見取得
    function getHreportremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //INSERT -- HREPORTREMARK_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd137gQuery::getHreportremarkDat($model));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '".$model->field["SEMESTER"]." ' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
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
        $query .= "     LEFT JOIN SCHREG_CLUB_HDETAIL_DAT L1 ";
        $query .= "              ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND L1.CLUBCD   = T1.CLUBCD ";
        $query .= "     LEFT JOIN CLUB_MST L2 ";
        $query .= "              ON L2.CLUBCD   = T1.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     L2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_COMMITTEE_HDETAIL_DAT L1 ";
        $query .= "              ON L1.YEAR = T1.YEAR ";
        $query .= "             AND L1.SEQ  = T1.SEQ ";
        $query .= "     LEFT JOIN COMMITTEE_MST L2 ";
        $query .= "              ON L2.COMMITTEE_FLG    = T1.COMMITTEE_FLG ";
        $query .= "             AND L2.COMMITTEECD      = T1.COMMITTEECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }
}
?>
