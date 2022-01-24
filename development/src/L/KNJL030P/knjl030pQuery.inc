<?php

require_once('for_php7.php');

class knjl030pQuery extends Query {
    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model) {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '{$model->year}' ";

        return $query;
    }

    //名称マスタから取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 AS DEFAULT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得
    function selectQuery($model, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     T1.EXAMNO_FROM || ' ～ ' || T1.EXAMNO_TO AS EXAMNO_FROM_TO, ";
        $query .= "     T1.TESTDIV || ':' || N1.NAME1 AS TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.NAMECD1      = '{$namecd1}' ";
        $query .= "         AND N1.NAMECD2      = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECNO ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $recno) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND RECNO        =  ".$recno." ";

        return $query;
    }

    //受験番号チェック
    function checkExamno($year, $applicantdiv, $recno, $examno_from, $examno_to="") {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' ";
        if ($recno) {
            $query .= " AND RECNO       != ".$recno." ";
        }
        if ($examno_to) {
            $query .= " AND (   EXAMNO_FROM BETWEEN '".$examno_from."' AND '".$examno_to."' ";
            $query .= "      OR EXAMNO_TO BETWEEN '".$examno_from."' AND '".$examno_to."') ";
        } else {
            $query .= " AND '".$examno_from."' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        }

        return $query;
    }

    //MAX自動発番取得
    function getMaxRecno($year, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     MAX(RECNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $maxRecno = $db->getOne(knjl030pQuery::getMaxRecno($fields["ENTEXAMYEAR"], $fields["APPLICANTDIV"]));
        $setRecno = ($maxRecno > 0) ? $maxRecno + 1 : 1;

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["RECNO"][NUMBER]          = $setRecno;
        $data["EXAMNO_FROM"][TEXT]      = $fields["EXAMNO_FROM"];
        $data["EXAMNO_TO"][TEXT]        = $fields["EXAMNO_TO"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_NO_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        return $setRecno;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["EXAMNO_FROM"][TEXT]      = $fields["EXAMNO_FROM"];
        $data["EXAMNO_TO"][TEXT]        = $fields["EXAMNO_TO"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '".$fields["ENTEXAMYEAR"]."' ";
        $where .= "     AND APPLICANTDIV = '".$fields["APPLICANTDIV"]."' ";
        $where .= "     AND RECNO        =  ".$fields["RECNO"]." ";

        $query = Query::updateSQL($data, "ENTEXAM_NO_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$fields["ENTEXAMYEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$fields["APPLICANTDIV"]."' ";
        $query .= "     AND RECNO        =  ".$fields["RECNO"]." ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //年度にデータが存在するのかチェック
    function checkEntexamNoDat($year, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear, $applicantdiv) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     RECNO, ";
        $query .= "     EXAMNO_FROM, ";
        $query .= "     EXAMNO_TO, ";
        $query .= "     TESTDIV0, ";
        $query .= "     TESTDIV, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     REMARK_DIV, ";
        $query .= "     MEMO_KISO, ";
        $query .= "     MEMO_HIKKI, ";
        $query .= "     MEMO_MENSETU, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$thisYear."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";

        return $query;
    }
}
?>
