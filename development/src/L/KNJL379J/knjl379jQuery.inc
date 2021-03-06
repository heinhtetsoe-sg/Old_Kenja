<?php

require_once('for_php7.php');

class knjl379jQuery extends Query {

    //名称マスタから取得
    function getSelectName($namecd1)
    {
        $query  = " SELECT NAMECD2, NAME1";
        $query .= " FROM NAME_MST";
        $query .= " WHERE NAMECD1 = '".$namecd1."'";
        $query .= " ORDER BY NAMECD2";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model)
    {

        $query  = " WITH   ";
        $query .= " RECEPTNO AS(SELECT  ";
        $query .= "                 T1.ENTEXAMYEAR, ";
        $query .= "                 T1.APPLICANTDIV, ";
        $query .= "                 T1.EXAMNO, ";
        $query .= "                 T1.NAME, ";
        $query .= "                 T1.SEX, ";
        $query .= "                 T2.TESTDIV, ";
        $query .= "                 T2.EXAM_TYPE, ";
        $query .= "                 T2.RECEPTNO, ";
        $query .= "                 T1.SPECIAL_MEASURES, ";
        $query .= "                 T3.EXAMINEE_DIV, ";
        $query .= "                 T2.JUDGEDIV, ";
        $query .= "                 T1.ENTDIV ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "                 ENTEXAM_RECEPT_DAT T2, ";
        $query .= "                 ENTEXAM_DESIRE_DAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "                 T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "                 T1.ENTEXAMYEAR = T3.ENTEXAMYEAR AND ";
        $query .= "                 T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "                 T1.APPLICANTDIV = T3.APPLICANTDIV AND ";
        $query .= "                 T2.EXAM_TYPE = T3.EXAM_TYPE AND ";
        $query .= "                 T2.TESTDIV = T3.TESTDIV AND ";
        $query .= "                 T1.EXAMNO = T2.EXAMNO AND ";
        $query .= "                 T1.EXAMNO = T3.EXAMNO ";
        $query .= " ), ";
        $query .= " SCORE1 AS(SELECT ";
        $query .= "                 T2.EXAMNO, ";
        $query .= "                 T1.TESTSUBCLASSCD, ";
        $query .= "                 T1.SCORE, ";
        $query .= "                 T1.STD_SCORE, ";
        $query .= "                 T2.EXAM_TYPE, ";
        $query .= "                 T2.EXAMINEE_DIV, ";
        $query .= "                 T2.JUDGEDIV ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_SCORE_DAT T1, ";
        $query .= "                 RECEPTNO T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "                 T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "                 T1.EXAM_TYPE = T2.EXAM_TYPE AND ";
        $query .= "                 T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "                 T1.RECEPTNO = T2.RECEPTNO AND ";
        $query .= "                 T1.TESTDIV = '1' ";
        $query .= " ), ";
        $query .= " SCORE2 AS(SELECT ";
        $query .= "                 T2.EXAMNO, ";
        $query .= "                 T1.TESTSUBCLASSCD, ";
        $query .= "                 T1.SCORE, ";
        $query .= "                 T1.STD_SCORE, ";
        $query .= "                 T2.EXAM_TYPE, ";
        $query .= "                 T2.EXAMINEE_DIV, ";
        $query .= "                 T2.JUDGEDIV ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_SCORE_DAT T1, ";
        $query .= "                 RECEPTNO T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "                 T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "                 T1.EXAM_TYPE = T2.EXAM_TYPE AND ";
        $query .= "                 T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "                 T1.RECEPTNO = T2.RECEPTNO AND ";
        $query .= "                 T1.TESTDIV = '2' ";
        $query .= " ), ";
        $query .= " SCORE3 AS(SELECT ";
        $query .= "                 T2.EXAMNO, ";
        $query .= "                 T1.TESTSUBCLASSCD, ";
        $query .= "                 T1.SCORE, ";
        $query .= "                 T1.STD_SCORE, ";
        $query .= "                 T2.EXAM_TYPE, ";
        $query .= "                 T2.EXAMINEE_DIV, ";
        $query .= "                 T2.JUDGEDIV ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_SCORE_DAT T1, ";
        $query .= "                 RECEPTNO T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "                 T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "                 T1.EXAM_TYPE = T2.EXAM_TYPE AND ";
        $query .= "                 T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "                 T1.RECEPTNO = T2.RECEPTNO AND ";
        $query .= "                 T1.TESTDIV = '3' ";
        $query .= " ), ";
        $query .= " SCORE4 AS(SELECT ";
        $query .= "                 T2.EXAMNO, ";
        $query .= "                 T1.TESTSUBCLASSCD, ";
        $query .= "                 T1.SCORE, ";
        $query .= "                 T1.STD_SCORE, ";
        $query .= "                 T2.EXAM_TYPE, ";
        $query .= "                 T2.EXAMINEE_DIV, ";
        $query .= "                 T2.JUDGEDIV ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_SCORE_DAT T1, ";
        $query .= "                 RECEPTNO T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "                 T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "                 T1.EXAM_TYPE = T2.EXAM_TYPE AND ";
        $query .= "                 T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "                 T1.RECEPTNO = T2.RECEPTNO AND ";
        $query .= "                 T1.TESTDIV = '4' ";
        $query .= " ), ";
        $query .= " SCORE6 AS(SELECT ";
        $query .= "                 T2.EXAMNO, ";
        $query .= "                 T1.TESTSUBCLASSCD, ";
        $query .= "                 T1.SCORE, ";
        $query .= "                 T1.STD_SCORE, ";
        $query .= "                 T2.EXAM_TYPE, ";
        $query .= "                 T2.EXAMINEE_DIV, ";
        $query .= "                 T2.JUDGEDIV ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_SCORE_DAT T1, ";
        $query .= "                 RECEPTNO T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "                 T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "                 T1.EXAM_TYPE = T2.EXAM_TYPE AND ";
        $query .= "                 T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "                 T1.RECEPTNO = T2.RECEPTNO AND ";
        $query .= "                 T1.TESTDIV = '6' ";
        $query .= " ), ";
        $query .= " SCORE5 AS(SELECT ";
        $query .= "                 T2.EXAMNO, ";
        $query .= "                 T1.TESTSUBCLASSCD, ";
        $query .= "                 T1.SCORE, ";
        $query .= "                 T1.STD_SCORE, ";
        $query .= "                 T2.EXAM_TYPE, ";
        $query .= "                 T2.EXAMINEE_DIV, ";
        $query .= "                 T2.JUDGEDIV ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_SCORE_DAT T1, ";
        $query .= "                 RECEPTNO T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "                 T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "                 T1.EXAM_TYPE = T2.EXAM_TYPE AND ";
        $query .= "                 T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "                 T1.RECEPTNO = T2.RECEPTNO AND ";
        $query .= "                 T1.TESTDIV = '5' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.SPECIAL_MEASURES, ";
        $query .= "     S11.SCORE AS SCORE11, ";
        $query .= "     S12.SCORE AS SCORE12, ";
        $query .= "     S13.SCORE AS SCORE13, ";
        $query .= "     S14.SCORE AS SCORE14, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S11.STD_SCORE,5,2),1),5,1) AS STD_SCORE11, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S12.STD_SCORE,5,2),1),5,1) AS STD_SCORE12, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S13.STD_SCORE,5,2),1),5,1) AS STD_SCORE13, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S14.STD_SCORE,5,2),1),5,1) AS STD_SCORE14, ";
        $query .= "     S11.EXAMINEE_DIV AS EXAMINEE_DIV1 , ";
        $query .= "     S11.JUDGEDIV AS JUDGEDIV1 , ";
        $query .= "     S11.EXAM_TYPE AS EXAM_TYPE1 , ";
        $query .= "     S21.SCORE AS SCORE21, ";
        $query .= "     S22.SCORE AS SCORE22, ";
        $query .= "     S23.SCORE AS SCORE23, ";
        $query .= "     S24.SCORE AS SCORE24, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S21.STD_SCORE,5,2),1),5,1) AS STD_SCORE21, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S22.STD_SCORE,5,2),1),5,1) AS STD_SCORE22, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S23.STD_SCORE,5,2),1),5,1) AS STD_SCORE23, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S24.STD_SCORE,5,2),1),5,1) AS STD_SCORE24, ";
        $query .= "     S21.EXAMINEE_DIV AS EXAMINEE_DIV2 , ";
        $query .= "     S21.JUDGEDIV AS JUDGEDIV2 , ";
        $query .= "     S21.EXAM_TYPE AS EXAM_TYPE2 , ";
        $query .= "     S31.SCORE AS SCORE31, ";
        $query .= "     S32.SCORE AS SCORE32, ";
        $query .= "     S33.SCORE AS SCORE33, ";
        $query .= "     S34.SCORE AS SCORE34, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S31.STD_SCORE,5,2),1),5,1) AS STD_SCORE31, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S32.STD_SCORE,5,2),1),5,1) AS STD_SCORE32, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S33.STD_SCORE,5,2),1),5,1) AS STD_SCORE33, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S34.STD_SCORE,5,2),1),5,1) AS STD_SCORE34, ";
        $query .= "     S31.EXAMINEE_DIV AS EXAMINEE_DIV3 , ";
        $query .= "     S31.JUDGEDIV AS JUDGEDIV3 , ";
        $query .= "     S31.EXAM_TYPE AS EXAM_TYPE3 , ";
        $query .= "     S41.SCORE AS SCORE41, ";
        $query .= "     S42.SCORE AS SCORE42, ";
        $query .= "     S43.SCORE AS SCORE43, ";
        $query .= "     S44.SCORE AS SCORE44, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S41.STD_SCORE,5,2),1),5,1) AS STD_SCORE41, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S42.STD_SCORE,5,2),1),5,1) AS STD_SCORE42, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S43.STD_SCORE,5,2),1),5,1) AS STD_SCORE43, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S44.STD_SCORE,5,2),1),5,1) AS STD_SCORE44, ";
        $query .= "     S41.EXAMINEE_DIV AS EXAMINEE_DIV4 , ";
        $query .= "     S41.JUDGEDIV AS JUDGEDIV4 , ";
        $query .= "     S41.EXAM_TYPE AS EXAM_TYPE4 , ";
        $query .= "     S61.SCORE AS SCORE61, ";
        $query .= "     S62.SCORE AS SCORE62, ";
        $query .= "     S63.SCORE AS SCORE63, ";
        $query .= "     S64.SCORE AS SCORE64, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S61.STD_SCORE,5,2),1),5,1) AS STD_SCORE61, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S62.STD_SCORE,5,2),1),5,1) AS STD_SCORE62, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S63.STD_SCORE,5,2),1),5,1) AS STD_SCORE63, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S64.STD_SCORE,5,2),1),5,1) AS STD_SCORE64, ";
        $query .= "     S61.EXAMINEE_DIV AS EXAMINEE_DIV6 , ";
        $query .= "     S61.JUDGEDIV AS JUDGEDIV6 , ";
        $query .= "     S61.EXAM_TYPE AS EXAM_TYPE6 , ";
        $query .= "     S51.SCORE AS SCORE51, ";
        $query .= "     S52.SCORE AS SCORE52, ";
        $query .= "     S53.SCORE AS SCORE53, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S51.STD_SCORE,5,2),1),5,1) AS STD_SCORE51, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S52.STD_SCORE,5,2),1),5,1) AS STD_SCORE52, ";
        $query .= "     DECIMAL(ROUND(DECIMAL(S53.STD_SCORE,5,2),1),5,1) AS STD_SCORE53, ";
        $query .= "     S51.EXAMINEE_DIV AS EXAMINEE_DIV5 , ";
        $query .= "     S51.JUDGEDIV AS JUDGEDIV5 , ";
        $query .= "     S51.EXAM_TYPE AS EXAM_TYPE5, ";
        $query .= "     T1.ENTDIV ";
        $query .= " FROM RECEPTNO T1 ";
        $query .= "         LEFT OUTER JOIN SCORE1 S11 ON T1.EXAMNO = S11.EXAMNO AND S11.TESTSUBCLASSCD = '2' ";
        $query .= "         LEFT OUTER JOIN SCORE1 S12 ON T1.EXAMNO = S12.EXAMNO AND S12.TESTSUBCLASSCD = '3' ";
        $query .= "         LEFT OUTER JOIN SCORE1 S13 ON T1.EXAMNO = S13.EXAMNO AND S13.TESTSUBCLASSCD = '5' ";
        $query .= "         LEFT OUTER JOIN SCORE1 S14 ON T1.EXAMNO = S14.EXAMNO AND S14.TESTSUBCLASSCD = '4' ";
        $query .= "         LEFT OUTER JOIN SCORE2 S21 ON T1.EXAMNO = S21.EXAMNO AND S21.TESTSUBCLASSCD = '2' ";
        $query .= "         LEFT OUTER JOIN SCORE2 S22 ON T1.EXAMNO = S22.EXAMNO AND S22.TESTSUBCLASSCD = '3' ";
        $query .= "         LEFT OUTER JOIN SCORE2 S23 ON T1.EXAMNO = S23.EXAMNO AND S23.TESTSUBCLASSCD = '5' ";
        $query .= "         LEFT OUTER JOIN SCORE2 S24 ON T1.EXAMNO = S24.EXAMNO AND S24.TESTSUBCLASSCD = '4' ";
        $query .= "         LEFT OUTER JOIN SCORE3 S31 ON T1.EXAMNO = S31.EXAMNO AND S31.TESTSUBCLASSCD = '2' ";
        $query .= "         LEFT OUTER JOIN SCORE3 S32 ON T1.EXAMNO = S32.EXAMNO AND S32.TESTSUBCLASSCD = '3' ";
        $query .= "         LEFT OUTER JOIN SCORE3 S33 ON T1.EXAMNO = S33.EXAMNO AND S33.TESTSUBCLASSCD = '5' ";
        $query .= "         LEFT OUTER JOIN SCORE3 S34 ON T1.EXAMNO = S34.EXAMNO AND S34.TESTSUBCLASSCD = '4' ";
        $query .= "         LEFT OUTER JOIN SCORE4 S41 ON T1.EXAMNO = S41.EXAMNO AND S41.TESTSUBCLASSCD = '2' ";
        $query .= "         LEFT OUTER JOIN SCORE4 S42 ON T1.EXAMNO = S42.EXAMNO AND S42.TESTSUBCLASSCD = '3' ";
        $query .= "         LEFT OUTER JOIN SCORE4 S43 ON T1.EXAMNO = S43.EXAMNO AND S43.TESTSUBCLASSCD = '5' ";
        $query .= "         LEFT OUTER JOIN SCORE4 S44 ON T1.EXAMNO = S44.EXAMNO AND S44.TESTSUBCLASSCD = '4' ";
        $query .= "         LEFT OUTER JOIN SCORE6 S61 ON T1.EXAMNO = S61.EXAMNO AND S61.TESTSUBCLASSCD = '2' ";
        $query .= "         LEFT OUTER JOIN SCORE6 S62 ON T1.EXAMNO = S62.EXAMNO AND S62.TESTSUBCLASSCD = '3' ";
        $query .= "         LEFT OUTER JOIN SCORE6 S63 ON T1.EXAMNO = S63.EXAMNO AND S63.TESTSUBCLASSCD = '5' ";
        $query .= "         LEFT OUTER JOIN SCORE6 S64 ON T1.EXAMNO = S64.EXAMNO AND S64.TESTSUBCLASSCD = '4' ";
        $query .= "         LEFT OUTER JOIN SCORE5 S51 ON T1.EXAMNO = S51.EXAMNO AND S51.TESTSUBCLASSCD = '2' ";
        $query .= "         LEFT OUTER JOIN SCORE5 S52 ON T1.EXAMNO = S52.EXAMNO AND S52.TESTSUBCLASSCD = '3' ";
        $query .= "         LEFT OUTER JOIN SCORE5 S53 ON T1.EXAMNO = S53.EXAMNO AND S53.TESTSUBCLASSCD = '1' ";
        $query .= " ORDER BY T1.EXAMNO ";

        return $query;
    }

}
?>
