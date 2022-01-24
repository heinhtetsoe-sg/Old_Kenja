<?php

require_once('for_php7.php');

class knjl570hquery extends Query {
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

    //名称マスタより取得
    function getNameMstAll($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //試験科目
    function getExamTypeTestSubclass($model) {
        $query  = " WITH RECEPT AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_RECEPT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "                     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "                     AND DISTINCT_ID  = '{$model->field["DISTINCT_ID"]}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUBC.SUBCLASSCD AS VALUE, ";
        $query .= "     L009.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_SUBCLASS_MST SUBC ";
        $query .= "     INNER JOIN RECEPT RCPT ON SUBC.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                           AND SUBC.APPLICANTDIV = RCPT.APPLICANTDIV  ";
        $query .= "                           AND SUBC.EXAM_TYPE    = RCPT.EXAM_TYPE  ";
        $query .= "     LEFT JOIN V_NAME_MST L009 ON L009.YEAR    = SUBC.ENTEXAMYEAR ";
        $query .= "                              AND L009.NAMECD1 = 'L009'  ";
        $query .= "                              AND L009.NAMECD2 = SUBC.SUBCLASSCD  ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ出力
    function selectDataQuery($model) {
        $query  = " SELECT ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     Z002.NAME1 AS SEX, ";
        $query .= "     BASE.NAME, ";
        for ($i = 1; $i <= 12; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "         CONF.CONFIDENTIAL_RPT{$num}, ";
            $query .= "         D001.REMARK{$i} AS DET001_{$i}, ";
        }
        $query .= "     CONF.TOTAL_ALL, ";
        foreach ($model->subClassArr as $cd => $name) {
            $query .= "         SCRE{$cd}.SCORE AS SCORE_{$cd}, ";
        }
        $query .= "     D007.REMARK10 AS FINSCHOOL_ADDR, ";
        $query .= "     D008.REMARK2 AS FINSCHOOL_NAME, ";
        $query .= "     D008.REMARK4 AS PRISCHOOL_NAME, ";
        $query .= "     D007.REMARK9 AS SH_FLG, ";
        $query .= "     '' AS XXX "; //特記事項はブランクで出力
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                             AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                             AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                                                AND CONF.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                                AND CONF.EXAMNO       = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT D001 ON D001.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                                                       AND D001.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                                       AND D001.EXAMNO       = RCPT.EXAMNO ";
        $query .= "                                                       AND D001.SEQ          = '001' ";
        foreach ($model->subClassArr as $cd => $name) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCRE{$cd} ON SCRE{$cd}.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
            $query .= "                                          AND SCRE{$cd}.APPLICANTDIV   = RCPT.APPLICANTDIV ";
            $query .= "                                          AND SCRE{$cd}.TESTDIV        = RCPT.TESTDIV ";
            $query .= "                                          AND SCRE{$cd}.EXAM_TYPE      = RCPT.EXAM_TYPE ";
            $query .= "                                          AND SCRE{$cd}.RECEPTNO       = RCPT.RECEPTNO ";
            $query .= "                                          AND SCRE{$cd}.TESTSUBCLASSCD = '{$cd}' ";
        }
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "                            AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D007 ";
        $query .= "          ON D007.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "         AND D007.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "         AND D007.TESTDIV      = RCPT.TESTDIV ";
        $query .= "         AND D007.EXAM_TYPE    = RCPT.EXAM_TYPE ";
        $query .= "         AND D007.RECEPTNO     = RCPT.RECEPTNO ";
        $query .= "         AND D007.SEQ          = '007' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D008 ";
        $query .= "          ON D008.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "         AND D008.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "         AND D008.TESTDIV      = RCPT.TESTDIV ";
        $query .= "         AND D008.EXAM_TYPE    = RCPT.EXAM_TYPE ";
        $query .= "         AND D008.RECEPTNO     = RCPT.RECEPTNO ";
        $query .= "         AND D008.SEQ          = '008' ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->year}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND RCPT.DISTINCT_ID    = '{$model->field["DISTINCT_ID"]}' ";
        $query .= " ORDER BY ";
        $query .= "     RCPT.EXAMNO ";

        return $query;
    }
}
?>
