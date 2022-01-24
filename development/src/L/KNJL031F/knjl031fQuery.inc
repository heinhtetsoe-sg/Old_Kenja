<?php

require_once('for_php7.php');

class knjl031fQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
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
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入試回数取得
    function getTestdiv0($year, $testdiv) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.NAMECD1  = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分取得
    function getExamcourse($year, $applicantdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     TESTDIV      = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ出力データ取得
    function selectCsvQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     L1.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     CASE WHEN T1.APPLICANTDIV = '1' THEN L3.NAME1 ELSE L2.NAME1 END AS TESTDIV_NAME, ";
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     L4.NAME1 AS TESTDIV0_NAME, ";
            $query .= "     L5.EXAMCOURSE_NAME, ";
        }
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     NML005.NAME1 AS EXAM_TYPE_NAME, ";
        }
        $query .= "     T1.RECEPTNO, ";
        $query .= "     S1.NAME, ";
        $query .= "     S1.NAME_KANA ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = S1.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO       = S1.EXAMNO ";
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT S2 ";
            $query .= "          ON T1.ENTEXAMYEAR  = S2.ENTEXAMYEAR ";
            $query .= "         AND T1.APPLICANTDIV = S2.APPLICANTDIV ";
            $query .= "         AND T1.TESTDIV      = S2.TESTDIV ";
            $query .= "         AND T1.EXAM_TYPE    = S2.EXAM_TYPE ";
            $query .= "         AND T1.RECEPTNO     = S2.RECEPTNO ";
            $query .= "         AND S2.SEQ          = '003' ";
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S3 ";
            $query .= "          ON T1.ENTEXAMYEAR  = S3.ENTEXAMYEAR ";
            $query .= "         AND T1.EXAMNO       = S3.EXAMNO ";
            $query .= "         AND S3.SEQ          = '001' ";
        }
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1      = 'L003' ";
        $query .= "         AND L1.NAMECD2      = T1.APPLICANTDIV ";
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1      = 'L004' ";
        $query .= "         AND L2.NAMECD2      = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST L3 ";
        $query .= "          ON L3.NAMECD1      = 'L024' ";
        $query .= "         AND L3.NAMECD2      = T1.TESTDIV ";
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     LEFT JOIN NAME_MST L4 ";
            $query .= "          ON L4.NAMECD1      = 'L034' ";
            $query .= "         AND L4.NAMECD2      = S2.REMARK1 ";
            $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L5 ";
            $query .= "          ON T1.ENTEXAMYEAR  = L5.ENTEXAMYEAR ";
            $query .= "         AND T1.APPLICANTDIV = L5.APPLICANTDIV ";
            $query .= "         AND L5.TESTDIV      = '1' ";
            $query .= "         AND S3.REMARK8      = L5.COURSECD ";
            $query .= "         AND S3.REMARK9      = L5.MAJORCD ";
            $query .= "         AND S3.REMARK10     = L5.EXAMCOURSECD ";
        }
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD011 ";
            $query .= "          ON BD011.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
            $query .= "         AND BD011.EXAMNO        = T1.EXAMNO ";
            $query .= "         AND BD011.SEQ           = '011' ";
            $query .= "     LEFT JOIN NAME_MST NML005 ";
            $query .= "          ON NML005.NAMECD1      = 'L005' ";
            $query .= "         AND NML005.NAMECD2      = BD011.REMARK{$model->field["TESTDIV"]} ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND VALUE(T1.JUDGEDIV,'') <> '4' ";
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= " AND S2.REMARK1      = '".$model->field["TESTDIV0"]."' ";
            if ($model->field["EXAMCOURSE"] != "ALL") {
                $query .= " AND S3.REMARK8 || S3.REMARK9 || S3.REMARK10 = '".$model->field["EXAMCOURSE"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";

        return $query;
    }
}
?>
