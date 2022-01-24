<?php

require_once('for_php7.php');

class knjx_hyosyo_selectQuery extends Query {
    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //賞データ取得
    function getHyosyo($model, $detail_div, $namecd1) {
        $query  = "";
        if ($model->send_prgid == 'KNJE020') {
            $query .= " WITH T_YEAR AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         R1.YEAR ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'A023' AND T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3, ";
            $query .= "         SCHREG_REGD_DAT R1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
            $query .= "         T1.SEMESTER = '".$model->exp_semester."' AND ";
            $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "         T1.SCHREGNO = R1.SCHREGNO AND ";
            $query .= "         R1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";
            $query .= " ) ";
        }
        $query .= " SELECT ";
        $query .= "     DETAIL_SDATE, ";
        $query .= "     DETAILCD, ";
        $query .= "     L1.NAME1 AS DETAILCDNAME, ";
        $query .= "     CONTENT AS CONTENT, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = '".$namecd1."' AND L1.NAMECD2 = T1.DETAILCD ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     DETAIL_DIV   = '".$detail_div."' AND ";
        if ($model->send_prgid == 'KNJE020') {
            $query .= "     FISCALYEAR(DETAIL_SDATE) IN (SELECT YEAR FROM T_YEAR) ";
        } else {
            $query .= "     FISCALYEAR(DETAIL_SDATE) = '".$model->exp_year."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     DETAIL_SDATE ";

        return $query;
    }
}
?>
