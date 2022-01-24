<?php

require_once('for_php7.php');

class knjl091oquery extends Query {

    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     SHOWORDER, ";
        $query .= "     NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$ObjYear}' ";
        $query .= "     AND SHOWDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     VALUE ";
        return $query;
    }

    //実行
    function selectCvsQuery($model)
    {
        $query .= " SELECT ";
        $query .= "   L1.EXAMNO AS LOGINID, ";
        $query .= "   REPLACE(CAST(L1.BIRTHDAY AS VARCHAR(10)), '-', '') AS PASSWD, ";
        $query .= "   CASE '".$model->field["TESTDIV"]."'  ";
        $query .= "       WHEN '0' THEN "; // 第1回英数特科コース入試 
        $query .= "           CASE WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS = '1' AND L1.SPECIAL_MEASURES = '3' THEN '14' "; // アップＴ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.HONORDIV = '1'                THEN '11' "; // 特待Ｔ合格T合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS = '2'              THEN '12' "; // Ｔ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS IN ('1', '4', '6') AND VALUE(L1.SPECIAL_MEASURES, '') <> '3' THEN '13' "; // Ｓ合格
        $query .= "                WHEN DD1.EXAMINEE_DIV = '2' AND DD1.APPLICANT_DIV = '1'     THEN '99' ";  // 欠席
        $query .= "                WHEN T1.JUDGEDIV = '2'                                      THEN '9' ";  // 不合格
        $query .= "           END ";
        $query .= "       WHEN '1' THEN "; // 特待生選抜入試
        $query .= "           CASE WHEN T1.JUDGEDIV = '1' AND T1.HONORDIV = '1'                THEN '21' "; // 正規合格（特待生）
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS = '2'              THEN '22' "; // Ｔ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS IN ('1', '4', '6') AND VALUE(L1.SPECIAL_MEASURES, '') <> '3' THEN '23' "; // Ｓ合格
        $query .= "                WHEN DD1.EXAMINEE_DIV = '2' AND DD1.APPLICANT_DIV = '1'     THEN '99' ";  // 欠席
        $query .= "                WHEN T1.JUDGEDIV = '2'                                      THEN '9' ";  // 不合格
        $query .= "           END ";
        $query .= "       WHEN '2' THEN "; // 第2回英数特科コース入試
        $query .= "           CASE WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS = '1' AND L1.SPECIAL_MEASURES = '3' THEN '14' "; // アップＴ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.HONORDIV = '1'                THEN '11' "; // 特待Ｔ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS ='2'               THEN '12' "; // Ｔ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS IN ('1', '4', '6') AND VALUE(L1.SPECIAL_MEASURES, '') <> '3' THEN '13' "; // Ｓ合格
        $query .= "                WHEN DD1.EXAMINEE_DIV = '2' AND DD1.APPLICANT_DIV = '1'     THEN '99' ";  // 欠席
        $query .= "                WHEN T1.JUDGEDIV = '2'                                      THEN '9' ";  // 不合格
        $query .= "           END ";
        $query .= "       WHEN '3' THEN "; // 第3回英数特科コース入試
        $query .= "           CASE WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS = '3'              THEN '34' "; // アップＴ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.HONORDIV = '1'                THEN '31' "; // 特待Ｔ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS = '2'              THEN '32' "; // Ｔ合格
        $query .= "                WHEN T1.JUDGEDIV = '1' AND T1.JUDGECLASS IN ('1', '4', '6') AND VALUE(L1.SPECIAL_MEASURES, '') <> '3' THEN '33' "; // Ｓ合格
        $query .= "                WHEN DD1.EXAMINEE_DIV = '2' AND DD1.APPLICANT_DIV = '1'     THEN '99' ";  // 欠席
        $query .= "                WHEN T1.JUDGEDIV = '2'                                      THEN '9' ";  // 不合格
        $query .= "           END ";
        $query .= "   END AS STATUS, ";
        $query .= "   '' ";
        if ($model->field["TESTDIV"] == "3") {
            $query .= "   || '☆☆入試得点状況☆☆' ";
            $query .= "   || '第４回目の入試得点状況はＷＥＢで公開しません。' ";
        } else {
            $query .= "   || 'あなたの総合得点は' || CASE WHEN SA.SCORE1 IS NOT NULL THEN RTRIM(CAST(SA.SCORE1 AS CHAR(4))) ELSE '' END || '点です。' ";
            $query .= "   || L009_1.NAME1 || 'は' || CASE WHEN S1.SCORE1 IS NOT NULL THEN RTRIM(CAST(S1.SCORE1 AS CHAR(4))) ELSE '' END || '点、' ";
            $query .= "   || L009_2.NAME1 || 'は' || CASE WHEN S2.SCORE1 IS NOT NULL THEN RTRIM(CAST(S2.SCORE1 AS CHAR(4))) ELSE '' END || '点、' ";
            $query .= "   || L009_3.NAME1 || 'は' || CASE WHEN S3.SCORE1 IS NOT NULL THEN RTRIM(CAST(S3.SCORE1 AS CHAR(4))) ELSE '' END || '点、' ";
            $query .= "   || L009_4.NAME1 || 'は' || CASE WHEN S4.SCORE1 IS NOT NULL THEN RTRIM(CAST(S4.SCORE1 AS CHAR(4))) ELSE '' END || '点です。' ";
            $query .= "   || '今回の特待合格ラインは' || CASE WHEN JA1.MIN_SCORE IS NOT NULL THEN RTRIM(CAST(JA1.MIN_SCORE AS CHAR(4))) ELSE '' END || '点、' ";
            $query .= "   || 'Ｔ合格ラインは' || CASE WHEN JA2.MIN_SCORE IS NOT NULL THEN RTRIM(CAST(JA2.MIN_SCORE AS CHAR(4))) ELSE '' END || '点、' ";
            $query .= "   || 'Ｓ合格ラインは' || CASE WHEN JA3.MIN_SCORE IS NOT NULL THEN RTRIM(CAST(JA3.MIN_SCORE AS CHAR(4))) ELSE '' END || '点です。' ";
            $query .= "   || '合格最低点等の情報はホームページをご覧ください。' ";
        }
        $query .= "   AS REMARK, ";
        $query .= "   L1.NAME AS REMARK2 ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT L1 ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DAT T1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "        AND T1.EXAMNO = L1.EXAMNO ";
        $query .= "        AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "   LEFT JOIN ENTEXAM_DESIRE_DAT DD1 ON DD1.ENTEXAMYEAR = L1.ENTEXAMYEAR";
        $query .= "        AND DD1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "        AND DD1.EXAMNO = L1.EXAMNO ";
        $query .= "        AND DD1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_SPARE_DAT SA ON T1.ENTEXAMYEAR = SA.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = SA.APPLICANTDIV ";
        $query .= "        AND T1.TESTDIV = SA.TESTDIV ";
        $query .= "        AND T1.RECEPTNO = SA.RECEPTNO ";
        $query .= "        AND SA.TESTSUBCLASSCD = 'A' ";
        $query .= "        AND SA.SEQ = '001' ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_SPARE_DAT S1 ON T1.ENTEXAMYEAR = S1.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = S1.APPLICANTDIV ";
        $query .= "        AND T1.TESTDIV = S1.TESTDIV ";
        $query .= "        AND T1.RECEPTNO = S1.RECEPTNO ";
        $query .= "        AND S1.TESTSUBCLASSCD = '1' ";
        $query .= "        AND S1.SEQ = '001' ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_SPARE_DAT S2 ON T1.ENTEXAMYEAR = S2.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = S2.APPLICANTDIV ";
        $query .= "        AND T1.TESTDIV = S2.TESTDIV ";
        $query .= "        AND T1.RECEPTNO = S2.RECEPTNO ";
        $query .= "        AND S2.TESTSUBCLASSCD = '2' ";
        $query .= "        AND S2.SEQ = '001' ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_SPARE_DAT S3 ON T1.ENTEXAMYEAR = S3.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = S3.APPLICANTDIV ";
        $query .= "        AND T1.TESTDIV = S3.TESTDIV ";
        $query .= "        AND T1.RECEPTNO = S3.RECEPTNO ";
        $query .= "        AND S3.TESTSUBCLASSCD = '3' ";
        $query .= "        AND S3.SEQ = '001' ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_SPARE_DAT S4 ON T1.ENTEXAMYEAR = S4.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = S4.APPLICANTDIV ";
        $query .= "        AND T1.TESTDIV = S4.TESTDIV ";
        $query .= "        AND T1.RECEPTNO = S4.RECEPTNO ";
        $query .= "        AND S4.TESTSUBCLASSCD = '4' ";
        $query .= "        AND S4.SEQ = '001' ";
        $query .= "   LEFT JOIN V_NAME_MST L009_1 ON T1.ENTEXAMYEAR = L009_1.YEAR AND L009_1.NAMECD1 = 'L009' AND L009_1.NAMECD2 = '1' ";
        $query .= "   LEFT JOIN V_NAME_MST L009_2 ON T1.ENTEXAMYEAR = L009_2.YEAR AND L009_2.NAMECD1 = 'L009' AND L009_2.NAMECD2 = '2' ";
        $query .= "   LEFT JOIN V_NAME_MST L009_3 ON T1.ENTEXAMYEAR = L009_3.YEAR AND L009_3.NAMECD1 = 'L009' AND L009_3.NAMECD2 = '3' ";
        $query .= "   LEFT JOIN V_NAME_MST L009_4 ON T1.ENTEXAMYEAR = L009_4.YEAR AND L009_4.NAMECD1 = 'L009' AND L009_4.NAMECD2 = '4' ";
        $query .= "   LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT JA1 ON T1.ENTEXAMYEAR = JA1.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = JA1.APPLICANTDIV ";
        $query .= "        AND T1.TESTDIV = JA1.TESTDIV ";
        $query .= "        AND T1.EXAM_TYPE = JA1.EXAM_TYPE ";
        $query .= "        AND JA1.TESTSUBCLASSCD = 'L' ";
        $query .= "   LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT JA2 ON T1.ENTEXAMYEAR = JA2.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = JA2.APPLICANTDIV ";
        $query .= "        AND T1.TESTDIV = JA2.TESTDIV ";
        $query .= "        AND T1.EXAM_TYPE = JA2.EXAM_TYPE ";
        $query .= "        AND JA2.TESTSUBCLASSCD = 'M' ";
        $query .= "   LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT JA3 ON T1.ENTEXAMYEAR = JA3.ENTEXAMYEAR ";
        $query .= "        AND T1.APPLICANTDIV = JA3.APPLICANTDIV ";
        $query .= "        AND T1.TESTDIV = JA3.TESTDIV ";
        $query .= "        AND T1.EXAM_TYPE = JA3.EXAM_TYPE ";
        $query .= "        AND JA3.TESTSUBCLASSCD = 'N' ";
        $query .= " WHERE ";
        $query .= "   L1.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "   AND L1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND (T1.RECEPTNO IS NOT NULL OR DD1.EXAMNO IS NOT NULL) ";
        $query .= " ORDER BY ";
        $query .= "   L1.EXAMNO ";
        return $query;
    }

}
?>
