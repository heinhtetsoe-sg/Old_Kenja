<?php

require_once('for_php7.php');

class knjaopsQuery extends Query {

    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getSchoolmst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    function getNameMstCd2($namecd1, $namecd2) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    function getGdat() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    function getMaxSeq() {
        $query  = " SELECT ";
        $query .= "     MAX(VALUE(SEQ, 0)) AS MAXSEQ ";
        $query .= " FROM ";
        $query .= "     ATTEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //更新処理
    function insUpDatQuery($db, $model, $signature, $exeResult) {

        $seq = $db->getOne(knjaopsQuery::getMaxSeq());

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SEQ"][NUMBER]        = ((int)$seq + 1);
        $data["STAFFCD"][TEXT]      = STAFFCD;
        $data["CERT_NO"][NUMBER]    = 123456;
        $data["RANDOM"][TEXT]       = $model->randm;
        $data["SIGNATURE"][TEXT]    = $signature;
        $data["RESULT"][NUMBER]     = $exeResult;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ATTEST_DAT");

        return $query;
    }
}
?>
