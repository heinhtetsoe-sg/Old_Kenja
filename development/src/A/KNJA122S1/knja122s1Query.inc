<?php

require_once('for_php7.php');

class knja122s1Query extends Query {

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

    function getMaxSeq() {
        $query  = " SELECT ";
        $query .= "     MAX(VALUE(SEQ, 0)) AS MAXSEQ ";
        $query .= " FROM ";
        $query .= "     ATTEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    function getSignature() {
        $query  = " SELECT ";
        $query .= "     SIGNATURE ";
        $query .= " FROM ";
        $query .= "     ATTEST_USBKEY_DAT ";
        $query .= " WHERE ";
        $query .= "     RESULT = 172 ";

        return $query;
    }

    //更新処理
    function insUpDatQuery($db, $model, $signature, $exeResult) {

        $seq = $db->getOne(knja122s1Query::getMaxSeq());

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
