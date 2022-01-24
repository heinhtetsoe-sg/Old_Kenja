<?php

require_once('for_php7.php');

class knjl505jQuery extends Query {

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
    function getTestDiv($year, $applicantDiv, $testDiv="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($testDiv) {
            $query .= " AND TESTDIV = '{$testDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試方式取得
    function getExamType($year, $applicantDiv, $examType="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMTYPE_NAME, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL, ";
        $query .= "     EXAM_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($examType) {
            $query .= " AND EXAM_TYPE = '{$examType}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
    function getTestDivArray($year, $applicantDiv) {
        $query  = " SELECT ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTDIV_ABBV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     TEST_DATE, ";
        $query .= "     TESTDIV ";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = "";
        $query .= " WITH SRCHBASE_TBL AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.ENTEXAMYEAR, ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.TESTDIV, ";
        $query .= "         T2.RECOM_EXAMNO, ";
        $query .= "         T1.JUDGEDIV ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ), PASTTEST_TBL AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.ENTEXAMYEAR, ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.RECOM_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         SRCHBASE_TBL T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.TESTDIV IN ( ";
        $query .= "             SELECT ";
        $query .= "                 T4.TESTDIV ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_TESTDIV_MST T3 ";
        $query .= "                 INNER JOIN ENTEXAM_TESTDIV_MST T4 ";
        $query .= "                      ON T4.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "                     AND T4.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "                     AND T4.TESTDIV <> T3.TESTDIV ";
        $query .= "                     AND T4.TEST_DATE || '-' || T4.TESTDIV < T3.TEST_DATE || '-' || T3.TESTDIV ";
        $query .= "                     AND T4.INTERVIEW_DIV = T3.INTERVIEW_DIV ";
        $query .= "                     AND T4.INTERVIEW_DIV = '1' ";// 1回面接
        $query .= "             WHERE ";
        $query .= "                 T3.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "                 AND T3.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "                 AND T3.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "         ) ";
        $query .= "         AND VALUE(T1.JUDGEDIV,'') <> '4' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T3_009.REMARK1 AS ORDERNO, ";
        $query .= "     CASE WHEN T4.RECOM_EXAMNO IS NOT NULL OR TM_TEKI.INTERVIEW_DIV IS NULL THEN '' ELSE '面' END AS INTERVIEW, ";
        $query .= "     CASE WHEN T1.JUDGEDIV = '4' THEN '欠' ELSE '' END AS ATTEND, ";
        $query .= "     T9.EXAMTYPE_NAME_ABBV AS EXAMTYPE, ";
        foreach ($model->testdivArray as $testdiv => $testdivAbbv) {
            $query .= "     CASE WHEN T5_{$testdiv}.JUDGEDIV = '1' THEN '合'  ";
            $query .= "          WHEN T5_{$testdiv}.JUDGEDIV = '2' THEN '×' ";
            $query .= "          WHEN T5_{$testdiv}.JUDGEDIV = '4' THEN '欠' ";
            $query .= "          WHEN T5_{$testdiv}.RECOM_EXAMNO IS NOT NULL THEN '〇' ";
            $query .= "          ELSE '' END AS KEKKA{$testdiv}, ";
        }
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     T6_003.REMARK1 AS NAITEI, ";
        }
        $query .= "     CASE WHEN T6_004.REMARK1 = '1' THEN 'A' END AS CHK_A, ";
        $query .= "     CASE WHEN T6_004.REMARK2 = '1' THEN 'F' END AS CHK_F, ";
        $query .= "     CASE WHEN T6_004.REMARK3 = '1' THEN 'T' END AS CHK_T, ";
        $query .= "     CASE WHEN T6_004.REMARK4 = '1' THEN 'B' END AS CHK_B, ";
        $query .= "     CASE WHEN T6_004.REMARK5 = '1' THEN 'J' END AS CHK_J, ";
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     T6.TOTAL_ALL AS NAISINTEN, ";
            $query .= "     T6.ABSENCE_DAYS3 AS KESSEKI, ";
        }
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T6.REMARK1 AS REMARK, ";
        $query .= "     T7.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T3_009 ";
        $query .= "          ON T3_009.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3_009.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T3_009.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T3_009.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "         AND T3_009.RECEPTNO = T1.RECEPTNO ";
        $query .= "         AND T3_009.SEQ = '009' ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST TM_TEKI ";
        $query .= "          ON TM_TEKI.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND TM_TEKI.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND TM_TEKI.TESTDIV = T1.TESTDIV ";
        $query .= "     LEFT JOIN PASTTEST_TBL T4 ";
        $query .= "          ON T4.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T4.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T4.RECOM_EXAMNO = T2.RECOM_EXAMNO ";
        foreach ($model->testdivArray as $testdiv => $testdivAbbv) {
            $query .= "     LEFT JOIN SRCHBASE_TBL T5_{$testdiv} ";
            $query .= "          ON T5_{$testdiv}.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
            $query .= "         AND T5_{$testdiv}.APPLICANTDIV = T2.APPLICANTDIV ";
            $query .= "         AND T5_{$testdiv}.TESTDIV = '{$testdiv}' ";
            $query .= "         AND T5_{$testdiv}.RECOM_EXAMNO = T2.RECOM_EXAMNO ";
        }
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T6 ";
        $query .= "          ON T6.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T6.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T6.EXAMNO = T2.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T6_003 ";
        $query .= "          ON T6_003.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T6_003.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T6_003.EXAMNO = T2.EXAMNO ";
        $query .= "         AND T6_003.SEQ = '003' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T6_004 ";
        $query .= "          ON T6_004.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T6_004.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T6_004.EXAMNO = T2.EXAMNO ";
        $query .= "         AND T6_004.SEQ = '004' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST T7 ";
        $query .= "          ON T7.FINSCHOOLCD = T2.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_EXAMTYPE_MST T9 ";
        $query .= "          ON T9.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T9.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T9.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["EXAM_TYPE"]) {
            $query .= "     AND T1.EXAM_TYPE = '{$model->field["EXAM_TYPE"]}' ";
        }
        if ($model->field["RECEPTNO_FROM"]) {
            $query .= "     AND T1.RECEPTNO >= '{$model->field["RECEPTNO_FROM"]}' ";
        }
        if ($model->field["RECEPTNO_TO"]) {
            $query .= "     AND T1.RECEPTNO <= '{$model->field["RECEPTNO_TO"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";  

        return $query;
    }
}
?>
