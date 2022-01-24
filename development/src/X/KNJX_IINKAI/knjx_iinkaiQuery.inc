<?php

require_once('for_php7.php');

class knjx_iinkaiQuery extends Query {

    //生徒情報
    function getSchInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

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

}
?>
