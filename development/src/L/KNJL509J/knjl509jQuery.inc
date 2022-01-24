<?php

require_once('for_php7.php');

class knjl509jQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
    function getTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストtoリスト取得
    function getReceptDatData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     RCPT.RECEPTNO AS VALUE, ";
        $query .= "     RCPT.RECEPTNO || '：' || BASE.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                              AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                              AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' AND L013.NAMECD2 = RCPT.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["TESTDIV"] != "") {
            $query .= "     AND RCPT.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        }
        $query .= "     AND L013.NAMESPARE1   = '1' "; // 合格者
        // 手続修了者
        if ($model->field["TARGET_DIV"] == "2") {
            $query .= "     AND RCPT.PROCEDUREDIV1  = '1' ";
            $query .= "     AND RCPT.ADJOURNMENTDIV = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }
}
?>