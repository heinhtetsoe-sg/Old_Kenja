<?php

require_once('for_php7.php');

class knjl516hquery extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試判別取得
    function getDistinctId($model) {
        $query  = " SELECT ";
        $query .= "     DISTINCT_ID AS VALUE, ";
        $query .= "     DISTINCT_ID || ':' || DISTINCT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DISTINCTION_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //データ出力
    function selectDataQuery($model) {
        $query  = " SELECT ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     RCPT.ENTEXAMYEAR, ";
        $query .= "     'D' || substr(D007.REMARK2, 1, 2) AS PASS_STATUS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D007 ON D007.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                                             AND D007.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                             AND D007.TESTDIV      = RCPT.TESTDIV ";
        $query .= "                                             AND D007.EXAM_TYPE    = RCPT.EXAM_TYPE ";
        $query .= "                                             AND D007.RECEPTNO     = RCPT.RECEPTNO ";
        $query .= "                                             AND D007.SEQ          = '007' ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->year}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND RCPT.DISTINCT_ID    = '{$model->field["DISTINCT_ID"]}' ";
        $query .= "     AND RCPT.JUDGEDIV       = '1' "; // 合格
        $query .= " ORDER BY ";
        $query .= "     RCPT.EXAMNO ";

        return $query;
    }
}
?>
