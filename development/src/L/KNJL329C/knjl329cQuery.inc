<?php

require_once('for_php7.php');

class knjl329cQuery extends Query {
    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1, $namecd2 = "") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAME1 AS CSV_LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L004") {
            if ($model->isCollege) {
                $query .= "     AND NAMECD2 NOT IN ('5') ";
            } else {
                $query .= "     AND NAMECD2 NOT IN ('5','6') ";
            }
        }
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ出力データ取得
    function selectCsvQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO AS LOGIN_ID, ";
        $query .= "     T1.BIRTHDAY AS PASSWORD, ";
        if ($model->isCollege) {
            $query .= "     T1.APPLICANTDIV || ";
            $query .= "     (CASE WHEN T1.TESTDIV = '7' THEN CASE WHEN T1.SHDIV IN ('7','A') OR T1.SHDIV IN ('8','B') AND T2.JUDGEDIV = '8' THEN '4' ELSE '1' END ";
            $query .= "           WHEN T1.TESTDIV = '1' THEN CASE WHEN T1.SHDIV IN ('7','A') OR T1.SHDIV IN ('8','B') AND T2.JUDGEDIV = '8' THEN '4' ELSE '1' END ";
            $query .= "           WHEN T1.TESTDIV = '2' THEN CASE WHEN T1.SHDIV IN ('7','A') OR T1.SHDIV IN ('8','B') AND T2.JUDGEDIV = '8' THEN '7' ELSE '2' END ";
            $query .= "           WHEN T1.TESTDIV = '3' THEN CASE WHEN T1.SHDIV IN ('3') OR T1.SHDIV IN ('5') AND T2.JUDGEDIV = 'A' THEN '3' ELSE '8' END ";
            $query .= "           WHEN T1.TESTDIV = '4' THEN CASE WHEN T1.SHDIV IN ('3') OR T1.SHDIV IN ('5') AND T2.JUDGEDIV = 'A' THEN '3' ELSE '8' END ";
            $query .= "           ELSE T1.TESTDIV END) || ";
            $query .= "     (CASE WHEN T1.TESTDIV IN ('4','7') THEN '3' WHEN T1.SHDIV IN ('3','4','5','6','7','8') THEN '1' WHEN T1.SHDIV IN ('9','A','B') THEN '2' ELSE T1.SHDIV END) || ";
            $query .= "     T1.SEX || ";
            $query .= "     (CASE WHEN T2.JUDGEDIV IN ('8','9','A','B') THEN '1' ELSE T2.JUDGEDIV END) AS STATUS, ";
        } else {
            $query .= "     T1.APPLICANTDIV || ";
            $query .= "     T1.TESTDIV || ";
            $query .= "     T1.SHDIV || ";
            $query .= "     T1.SEX || ";
            $query .= "     T2.JUDGEDIV AS STATUS, ";
        }
        $query .= "     '' AS REMARK, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.SHDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "     ENTEXAM_RECEPT_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.EXAMNO       = T2.EXAMNO AND ";
        if ($model->isCollege) {
            $query .= "     T1.TESTDIV NOT IN ('5') AND ";
        } else {
            $query .= "     T1.TESTDIV NOT IN ('5','6') AND ";
        }
        $query .= "     T2.JUDGEDIV IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
