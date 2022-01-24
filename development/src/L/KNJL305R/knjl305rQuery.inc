<?php

require_once('for_php7.php');

class knjl305rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    function getNameMstVal($year, $field, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    function getCsvQuery($year, $model, $i)
    {
        $query .= " WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         BASE.EXAMNO, ";
        $query .= "         BASE.NAME_KANA, ";
        $query .= "         BASE.SEX, ";
        $query .= "         NMZ002.ABBV1 AS SEX_NAME, ";
        $query .= "         BASE.FS_CD, ";
        $query .= "         T7.FINSCHOOL_NAME, ";
        $query .= "         T3C.EXAMCOURSE_MARK AS EXAM_COURSEMARK1, ";
        $query .= "         T4C.EXAMCOURSE_MARK AS EXAM_COURSEMARK2, ";
        $query .= "         T5C.EXAMCOURSE_MARK AS EXAM_COURSEMARK3, ";
        $query .= "         T6C.EXAMCOURSE_MARK AS EXAM_COURSEMARK4, ";
        $query .= "         CASE WHEN RECEPT.TOTAL4 IS NOT NULL THEN '○' ELSE '×' END AS JUDGEMENT_NAME ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "             AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "             AND RECEPT.TESTDIV = BASE.TESTDIV ";
        $query .= "             AND RECEPT.EXAM_TYPE = '1' ";
        $query .= "             AND RECEPT.EXAMNO = BASE.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "             AND T3.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "             AND T3.TESTDIV = BASE.TESTDIV ";
        $query .= "             AND T3.DESIREDIV = BASE.DESIREDIV ";
        $query .= "             AND T3.WISHNO = '1' ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST T3C ON T3C.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "             AND T3C.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "             AND T3C.TESTDIV = T3.TESTDIV ";
        $query .= "             AND T3C.COURSECD = T3.COURSECD ";
        $query .= "             AND T3C.MAJORCD = T3.MAJORCD ";
        $query .= "             AND T3C.EXAMCOURSECD = T3.EXAMCOURSECD ";
        $query .= "         LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "             AND T4.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "             AND T4.TESTDIV = BASE.TESTDIV ";
        $query .= "             AND T4.DESIREDIV = BASE.DESIREDIV ";
        $query .= "             AND T4.WISHNO = '2' ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST T4C ON T4C.ENTEXAMYEAR = T4.ENTEXAMYEAR ";
        $query .= "             AND T4C.APPLICANTDIV = T4.APPLICANTDIV ";
        $query .= "             AND T4C.TESTDIV = T4.TESTDIV ";
        $query .= "             AND T4C.COURSECD = T4.COURSECD ";
        $query .= "             AND T4C.MAJORCD = T4.MAJORCD ";
        $query .= "             AND T4C.EXAMCOURSECD = T4.EXAMCOURSECD ";
        $query .= "         LEFT JOIN ENTEXAM_WISHDIV_MST T5 ON T5.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "             AND T5.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "             AND T5.TESTDIV = BASE.TESTDIV ";
        $query .= "             AND T5.DESIREDIV = BASE.DESIREDIV ";
        $query .= "             AND T5.WISHNO = '3' ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST T5C ON T5C.ENTEXAMYEAR = T5.ENTEXAMYEAR ";
        $query .= "             AND T5C.APPLICANTDIV = T5.APPLICANTDIV ";
        $query .= "             AND T5C.TESTDIV = T5.TESTDIV ";
        $query .= "             AND T5C.COURSECD = T5.COURSECD ";
        $query .= "             AND T5C.MAJORCD = T5.MAJORCD ";
        $query .= "             AND T5C.EXAMCOURSECD = T5.EXAMCOURSECD ";
        $query .= "         LEFT JOIN ENTEXAM_WISHDIV_MST T6 ON T6.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "             AND T6.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "             AND T6.TESTDIV = BASE.TESTDIV ";
        $query .= "             AND T6.DESIREDIV = BASE.DESIREDIV ";
        $query .= "             AND T6.WISHNO = '4' ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST T6C ON T6C.ENTEXAMYEAR = T6.ENTEXAMYEAR ";
        $query .= "             AND T6C.APPLICANTDIV = T6.APPLICANTDIV ";
        $query .= "             AND T6C.TESTDIV = T6.TESTDIV ";
        $query .= "             AND T6C.COURSECD = T6.COURSECD ";
        $query .= "             AND T6C.MAJORCD = T6.MAJORCD ";
        $query .= "             AND T6C.EXAMCOURSECD = T6.EXAMCOURSECD ";
        $query .= "         LEFT JOIN FINSCHOOL_MST T7 ON T7.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "         LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ";
        $query .= "             AND NMZ002.NAMECD2 = BASE.SEX ";
        $query .= "     WHERE ";
        $query .= "         BASE.ENTEXAMYEAR = '".$year."' ";
        $query .= "         AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "         AND BASE.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= " ), APPLICANT_BEFORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         BEF.BEFORE_PAGE, ";
        $query .= "         BEF.BEFORE_SEQ, ";
        $query .= "         BEF.NAME_KANA, ";
        $query .= "         BEF.SEX, ";
        $query .= "         NMZ002.NAME2 AS SEX_NAME, ";
        $query .= "         T3C.EXAMCOURSE_MARK AS BEF_COURSEMARK, ";
        $query .= "         NML004.ABBV3 AS SUB, ";
        $query .= "         BEF.NANKAN_FLG , ";
        $query .= "         BEF.FS_CD, ";
        $query .= "         T6.FINSCHOOL_NAME, ";
        $query .= "         BEF.RECOM_FLG, ";
        $query .= "         NML032.NAME2 AS RECOM_FLG_NAME, ";
        $query .= "         BEF.RECOM_REMARK, ";
        $query .= "         BEF.REMARK, ";
        $query .= "         BEF.SENBATU1_SCHOOL, ";
        $query .= "         T4C.REMARK1 AS SENBATU1 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANT_BEFORE_DAT BEF ";
        $query .= "         LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ";
        $query .= "             AND NMZ002.NAMECD2 = BEF.SEX ";
        $query .= "         LEFT JOIN NAME_MST NML032 ON NML032.NAMECD1 = 'L032' ";
        $query .= "             AND NML032.NAMECD2 = BEF.RECOM_FLG ";
        $query .= "         LEFT JOIN V_FINSCHOOL_MST T6 ON T6.YEAR = BEF.ENTEXAMYEAR ";
        $query .= "            AND T6.FINSCHOOLCD = BEF.FS_CD ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST T3C ON T3C.ENTEXAMYEAR = BEF.ENTEXAMYEAR ";
        $query .= "             AND T3C.APPLICANTDIV = BEF.APPLICANTDIV ";
        $query .= "             AND T3C.TESTDIV = BEF.TESTDIV ";
        $query .= "             AND T3C.COURSECD = BEF.BEFORE_COURSECD ";
        $query .= "             AND T3C.MAJORCD = BEF.BEFORE_MAJORCD ";
        $query .= "             AND T3C.EXAMCOURSECD = BEF.BEFORE_EXAMCOURSECD ";
        $query .= "         LEFT JOIN (SELECT T4.* ";
        $query .= "                 FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ";
        $query .= "                 INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "                  BASE.ENTEXAMYEAR = T4.ENTEXAMYEAR ";
        $query .= "                  AND BASE.APPLICANTDIV = T4.APPLICANTDIV ";
        $query .= "                  AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "                  AND BASE.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "                  AND BASE.EXAMNO = T4.EXAMNO ";
        $query .= "                 WHERE T4.ENTEXAMYEAR = '".$year."' AND T4.SEQ = '002' ";             
        $query .= "              )  T4 ON T4.REMARK1 = BEF.BEFORE_PAGE AND T4.REMARK2 = BEF.BEFORE_SEQ ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T4C ON T4C.ENTEXAMYEAR = T4.ENTEXAMYEAR ";
        $query .= "             AND T4C.APPLICANTDIV = T4.APPLICANTDIV ";
        $query .= "             AND T4C.EXAMNO = T4.EXAMNO ";
        $query .= "             AND T4C.SEQ = '003' ";
        $query .= "         LEFT JOIN NAME_MST NML004 ON NML004.NAMECD1 = 'L004' ";
        $query .= "             AND NML004.NAMECD2 = BEF.TESTDIV ";
        $query .= "     WHERE ";
        $query .= "         BEF.ENTEXAMYEAR = '".$year."' ";
        $query .= "         AND BEF.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "         AND BEF.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= " ), COMMON AS ( ";
/*
        $query .= "     SELECT T4.EXAMNO, T4.REMARK1, T4.REMARK2 ";
        $query .= "     FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "      BASE.ENTEXAMYEAR = T4.ENTEXAMYEAR ";
        $query .= "      AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "      AND BASE.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "      AND BASE.EXAMNO = T4.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         T4.ENTEXAMYEAR = '".$year."' ";
        $query .= "         AND T4.SEQ = '002' ";
 */
        $query .= "     SELECT DISTINCT BASE.EXAMNO, BEF.BEFORE_PAGE AS REMARK1, BEF.BEFORE_SEQ AS REMARK2 ";
        $query .= "     FROM ( ";
        $query .= "       SELECT NAME_KANA, SEX, FS_CD FROM BASE ";
        $query .= "       INTERSECT ";
        $query .= "       SELECT NAME_KANA, SEX, FS_CD FROM APPLICANT_BEFORE ";
        $query .= "     ) T1 ";
        $query .= "     INNER JOIN BASE ON BASE.NAME_KANA = T1.NAME_KANA ";
        $query .= "         AND BASE.SEX = T1.SEX ";
        $query .= "         AND BASE.FS_CD = T1.FS_CD ";
        $query .= "     INNER JOIN APPLICANT_BEFORE BEF ON BEF.NAME_KANA = T1.NAME_KANA ";
        $query .= "         AND BEF.SEX = T1.SEX ";
        $query .= "         AND BEF.FS_CD = T1.FS_CD ";
        $query .= " ) ";
        if ($i == 0) {
            $query .= " SELECT ";
            $query .= "     T1.EXAMNO AS BASE_EXAMNO, ";
            $query .= "     T1.NAME_KANA AS BASE_NAME_KANA, ";
            $query .= "     T1.SEX AS BASE_SEX, ";
            $query .= "     T1.SEX_NAME AS BASE_SEX_NAME, ";
            $query .= "     T1.FS_CD AS BASE_FS_CD, ";
            $query .= "     T1.FINSCHOOL_NAME AS BASE_FINSCHOOL_NAME, ";
            $query .= "     T1.EXAM_COURSEMARK1 AS BASE_EXAM_COURSEMARK1, ";
            $query .= "     T1.EXAM_COURSEMARK2 AS BASE_EXAM_COURSEMARK2, ";
            $query .= "     T1.EXAM_COURSEMARK3 AS BASE_EXAM_COURSEMARK3, ";
            $query .= "     T1.EXAM_COURSEMARK4 AS BASE_EXAM_COURSEMARK4, ";
            $query .= "     T1.JUDGEMENT_NAME AS BASE_JUDGEMENT_NAME, ";
            $query .= "     T2.BEFORE_PAGE, ";
            $query .= "     T2.BEFORE_SEQ, ";
            $query .= "     T2.NAME_KANA AS BEF_NAME_KANA, ";
            $query .= "     T2.SEX AS BEF_SEX, ";
            $query .= "     T2.SEX_NAME AS BEF_SEX_NAME, ";
            $query .= "     T2.BEF_COURSEMARK, ";
            $query .= "     T2.SUB AS BEF_SUB, ";
            $query .= "     T2.NANKAN_FLG AS BEF_NANKAN_FLG, ";
            $query .= "     T2.FS_CD AS BEF_FS_CD, ";
            $query .= "     T2.FINSCHOOL_NAME AS BEF_FINSCHOOL_NAME, ";
            $query .= "     T2.RECOM_FLG AS BEF_RECOM_FLG, ";
            $query .= "     T2.RECOM_FLG_NAME AS BEF_RECOM_FLG_NAME, ";
            $query .= "     T2.RECOM_REMARK AS BEF_RECOM_REMARK, ";
            $query .= "     T2.REMARK AS BEF_REMARK, ";
            $query .= "     T2.SENBATU1_SCHOOL AS BEF_SENBATU1_SCHOOL, ";
            $query .= "     T2.SENBATU1 AS BEF_SENBATU1 ";
            $query .= " FROM COMMON T0 ";
            $query .= " INNER JOIN BASE T1 ON T1.EXAMNO = T0.EXAMNO ";
            $query .= " INNER JOIN APPLICANT_BEFORE T2 ON T2.BEFORE_PAGE = T0.REMARK1 AND T2.BEFORE_SEQ = T0.REMARK2 ";
        } else if ($i == 1) {
            $query .= " SELECT ";
            $query .= "     T1.EXAMNO AS BASE_EXAMNO, ";
            $query .= "     T1.NAME_KANA AS BASE_NAME_KANA, ";
            $query .= "     T1.SEX AS BASE_SEX, ";
            $query .= "     T1.SEX_NAME AS BASE_SEX_NAME, ";
            $query .= "     T1.FS_CD AS BASE_FS_CD, ";
            $query .= "     T1.FINSCHOOL_NAME AS BASE_FINSCHOOL_NAME, ";
            $query .= "     T1.EXAM_COURSEMARK1 AS BASE_EXAM_COURSEMARK1, ";
            $query .= "     T1.EXAM_COURSEMARK2 AS BASE_EXAM_COURSEMARK2, ";
            $query .= "     T1.EXAM_COURSEMARK3 AS BASE_EXAM_COURSEMARK3, ";
            $query .= "     T1.EXAM_COURSEMARK4 AS BASE_EXAM_COURSEMARK4, ";
            $query .= "     T1.JUDGEMENT_NAME AS BASE_JUDGEMENT_NAME, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEFORE_PAGE, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEFORE_SEQ, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_NAME_KANA, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_SEX, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_SEX_NAME, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_COURSEMARK, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_SUB, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_NANKAN_FLG, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_FS_CD, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_FINSCHOOL_NAME, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_RECOM_FLG, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_RECOM_FLG_NAME, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_RECOM_REMARK, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_REMARK, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_SENBATU1_SCHOOL, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BEF_SENBATU1 ";
            $query .= " FROM BASE T1 ";
            $query .= " WHERE NOT EXISTS (SELECT 'X' FROM COMMON T0 WHERE T1.EXAMNO = T0.EXAMNO) ";
        } else if ($i == 2) {
            $query .= " SELECT ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_EXAMNO, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_NAME_KANA, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_SEX, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_SEX_NAME, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_FS_CD, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_FINSCHOOL_NAME, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_EXAM_COURSEMARK1, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_EXAM_COURSEMARK2, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_EXAM_COURSEMARK3, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_EXAM_COURSEMARK4, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS BASE_JUDGEMENT_NAME, ";
            $query .= "     T2.BEFORE_PAGE, ";
            $query .= "     T2.BEFORE_SEQ, ";
            $query .= "     T2.NAME_KANA AS BEF_NAME_KANA, ";
            $query .= "     T2.SEX AS BEF_SEX, ";
            $query .= "     T2.SEX_NAME AS BEF_SEX_NAME, ";
            $query .= "     T2.BEF_COURSEMARK, ";
            $query .= "     T2.SUB AS BEF_SUB, ";
            $query .= "     T2.NANKAN_FLG AS BEF_NANKAN_FLG, ";
            $query .= "     T2.FS_CD AS BEF_FS_CD, ";
            $query .= "     T2.FINSCHOOL_NAME AS BEF_FINSCHOOL_NAME, ";
            $query .= "     T2.RECOM_FLG AS BEF_RECOM_FLG, ";
            $query .= "     T2.RECOM_FLG_NAME AS BEF_RECOM_FLG_NAME, ";
            $query .= "     T2.RECOM_REMARK AS BEF_RECOM_REMARK, ";
            $query .= "     T2.REMARK AS BEF_REMARK, ";
            $query .= "     T2.SENBATU1_SCHOOL AS BEF_SENBATU1_SCHOOL, ";
            $query .= "     T2.SENBATU1 AS BEF_SENBATU1 ";
            $query .= " FROM APPLICANT_BEFORE T2 ";
            $query .= " WHERE NOT EXISTS (SELECT 'X' FROM COMMON T0 WHERE T2.BEFORE_PAGE = T0.REMARK1 AND T2.BEFORE_SEQ = T0.REMARK2) ";
        }
        $query .= " ORDER BY ";
        if ("2" == $model->field["OUTPUT"]) {
            $query .= "     BEFORE_PAGE, ";
            $query .= "     BEFORE_SEQ, ";
            $query .= "     BASE_EXAMNO ";
        } else { // if ("1" == $model->field["OUTPUT"]) {
            $query .= "     BASE_EXAMNO, ";
            $query .= "     BEFORE_PAGE, ";
            $query .= "     BEFORE_SEQ ";
        }

        return $query;
    }

}
?>
