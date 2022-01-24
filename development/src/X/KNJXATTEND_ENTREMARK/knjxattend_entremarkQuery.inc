<?php

require_once('for_php7.php');

class knjxattend_entremarkQuery extends Query {
    //年度コンボ
    function getYear($schregno) {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //名前
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //出欠の記録
    function getRemark($schregno, $year) {
        $query .= " SELECT ";
        $query .= "     ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR = '{$year}' ";
        $query .= "   AND SCHREGNO = '{$schregno}' ";

        return $query;
    }
}
?>
