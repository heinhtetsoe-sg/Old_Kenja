<?php

require_once('for_php7.php');

class knjl324pQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //塾一覧取得
    function selectPriSchoolQuery($model) {
        $query  = " SELECT ";
        $query .= "     E1.REMARK1 || '-' || VALUE(E1.REMARK3, '0000000') AS VALUE, ";
        $query .= "     P1.PRISCHOOL_NAME || VALUE(P2.PRISCHOOL_NAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT E1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT E2 ON E2.ENTEXAMYEAR  = E1.ENTEXAMYEAR ";
        $query .= "                                     AND E2.APPLICANTDIV = E1.APPLICANTDIV ";
        $query .= "                                     AND E2.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "                                     AND E2.EXAM_TYPE    = '1' ";
        $query .= "                                     AND E2.EXAMNO       = E1.EXAMNO ";
        $query .= "     LEFT JOIN PRISCHOOL_MST P1 ON E1.REMARK1 = P1.PRISCHOOLCD ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST P2 ON E1.REMARK1 = P2.PRISCHOOLCD ";
        $query .= "                                     AND E1.REMARK3 = P2.PRISCHOOL_CLASS_CD ";
        $query .= " WHERE ";
        $query .= "         E1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND E1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND E1.SEQ          = '008' ";
        $query .= "     AND E1.REMARK1      IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     E1.REMARK1 || '-' || VALUE(E1.REMARK3, '0000000'), ";
        $query .= "     P1.PRISCHOOL_NAME || VALUE(P2.PRISCHOOL_NAME, '') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
