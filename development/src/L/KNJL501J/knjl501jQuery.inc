<?php

require_once('for_php7.php');

class knjl501jQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $namecd1, $nameCd2="") {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //リスト取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     TEST.ENTEXAMYEAR, ";
        $query .= "     TEST.APPLICANTDIV, ";
        $query .= "     TEST.TESTDIV, ";
        $query .= "     TEST.TESTDIV_NAME, ";
        $query .= "     TEST.TESTDIV_ABBV, ";
        $query .= "     CASE WHEN TEST.INTERVIEW_DIV = '1' THEN '1回面接' ";
        $query .= "          WHEN TEST.INTERVIEW_DIV = '2' THEN '全員面接' ";
        $query .= "                                        ELSE '' ";
        $query .= "     END AS INTERVIEW_DIV, ";
        $query .= "     TEST.CAPACITY, ";
        $query .= "     TEST.TEST_DATE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST TEST ";
        $query .= " WHERE ";
        $query .= "         TEST.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND TEST.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     TEST.TESTDIV ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $testdiv) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$testdiv."' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        $data["INTERVIEW_DIV"][TEXT]    = $fields["INTERVIEW_DIV"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["TEST_DATE"][TEXT]        = str_replace('/', '-', $fields["TEST_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        $data["INTERVIEW_DIV"][TEXT]    = $fields["INTERVIEW_DIV"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["TEST_DATE"][TEXT]        = str_replace('/', '-', $fields["TEST_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND TESTDIV           = '".$fields["TESTDIV"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_TESTDIV_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND TESTDIV         = '".$fields["TESTDIV"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
