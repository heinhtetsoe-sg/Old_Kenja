<?php

require_once('for_php7.php');

class knjd139tQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchkind($grade) {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$grade."' ";

        return $query;
    }

    //１レコード取得
    function getHreportRemarkRow($model) {
        $query  = " SELECT ";
        $query .= "     HREP.ATTENDREC_REMARK, ";
        $query .= "     HREP.SPECIALACTREMARK, ";
        $query .= "     HREP.REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT HREP ";
        $query .= " WHERE ";
        $query .= "         HREP.YEAR     = '".$model->exp_year."' ";
        $query .= "     AND HREP.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND HREP.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //存在チェック
    function getHreportRemarkCnt($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //HREPORTREMARK_DAT
        //存在チェック
        $check = $db->getCol(knjd139tQuery::getHreportRemarkCnt($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]              = $model->exp_year;
            $data["SEMESTER"][TEXT]          = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]          = $model->schregno;
            $data["SPECIALACTREMARK"][TEXT]  = $model->field["SPECIALACTREMARK"];
            $data["REMARK1"][TEXT]           = $model->field["REMARK1"];
            $data["ATTENDREC_REMARK"][TEXT]  = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["SPECIALACTREMARK"][TEXT]  = $model->field["SPECIALACTREMARK"];
            $data["REMARK1"][TEXT]           = $model->field["REMARK1"];
            $data["ATTENDREC_REMARK"][TEXT]  = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE ";
            $where .= "         YEAR     = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T4.NAME1 AS SEMESTERNAME, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T2.COMMITTEENAME AS COMMITTEE_SHOW, ";
        $query .= "     T1.CHARGENAME AS CHARGE_SHOW, ";
        $query .= "     T3.NAME1 AS EXECUTIVE_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN COMMITTEE_MST T2 ";
        $query .= "          ON T1.COMMITTEE_FLG    = T2.COMMITTEE_FLG ";
        $query .= "         AND T1.COMMITTEECD      = T2.COMMITTEECD ";
        $query .= "     LEFT JOIN NAME_MST T3 ";
        $query .= "          ON T3.NAMECD1      = 'J002' ";
        $query .= "         AND T1.EXECUTIVECD  = T3.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST T4 ";
        $query .= "          ON T4.NAMECD1      = 'J004' ";
        $query .= "         AND T1.SEMESTER     = T4.NAMECD2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR     = G1.YEAR ";
        $query .= "         AND T1.GRADE    = G1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }
}
?>
