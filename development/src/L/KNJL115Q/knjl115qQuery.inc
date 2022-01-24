<?php

require_once('for_php7.php');

class knjl115qQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2    = '".$namecd2."' ";
        }
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ
    function getCsvData($model) {
        $query .= " WITH T_SCORE AS (  ";
        $query .= "     SELECT  ";
        $query .= "         R1.TESTDIV,  ";
        $query .= "         R1.EXAMNO,  ";
        $query .= "         R1.TOTAL4 AS TOTAL,  ";
        $query .= "         SUM(CASE WHEN S1.TESTSUBCLASSCD = '1' THEN S1.SCORE END) AS KOKUGO,  ";
        $query .= "         SUM(CASE WHEN S1.TESTSUBCLASSCD = '2' THEN S1.SCORE END) AS SUUGAKU,  ";
        $query .= "         SUM(CASE WHEN S1.TESTSUBCLASSCD = '3' THEN S1.SCORE END) AS RIKA,  ";
        $query .= "         SUM(CASE WHEN S1.TESTSUBCLASSCD = '5' THEN S1.SCORE END) AS EIGO,  ";
        $query .= "         SUM(CASE WHEN S1.TESTSUBCLASSCD = '6' THEN S1.SCORE END) AS SHOURON  ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1  ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT S1  ";
        $query .= "                  ON S1.ENTEXAMYEAR = R1.ENTEXAMYEAR  ";
        $query .= "                 AND S1.APPLICANTDIV = R1.APPLICANTDIV  ";
        $query .= "                 AND S1.TESTDIV = R1.TESTDIV  ";
        $query .= "                 AND S1.RECEPTNO = R1.RECEPTNO  ";
        $query .= "     WHERE  ";
        $query .= "             R1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "         AND R1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     GROUP BY  ";
        $query .= "         R1.TESTDIV,  ";
        $query .= "         R1.EXAMNO,  ";
        $query .= "         R1.TOTAL4  ";
        $query .= " )  ";
        $query .= " SELECT ";
        $query .= "     VBASE.EXAMNO, ";
        $query .= "     L063.NAME1 AS HR_NAME, ";
        $query .= "     S035.REMARK2 AS ATTEND_NO, ";
        $query .= "     L004.ABBV1 AS TESTDIV, ";
        $query .= "     '' AS HCLASS, ";
        $query .= "     '' AS HCLASSNO, ";
        $query .= "     VBASE.NAME, ";
        $query .= "     VBASE.NAME_KANA, ";
        $query .= "     Z002.NAME2 AS SEX, ";
        $query .= "     '' AS STUDENT_NO, ";
        $query .= "     FINSCHOOL.FINSCHOOL_NAME, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     SAT.SAT_NO, ";
        $query .= "     CASE ";
        $query .= "         WHEN S023.REMARK1 = '1' THEN '特別' ";
        $query .= "         WHEN S023.REMARK1 = '2' THEN '一般' ";
        $query .= "     END AS SKIBOU, ";
        $query .= "     S023.REMARK3 AS SRESULT, ";
        $query .= "     SCORE.EIGO, ";
        $query .= "     SCORE.SUUGAKU, ";
        $query .= "     SCORE.KOKUGO, ";
        $query .= "     SCORE.RIKA, ";
        $query .= "     SCORE.TOTAL, ";
        $query .= "     SCORE.SHOURON, ";
        $query .= "     L027_1.NAME1 AS S_INTERVIEW_VALUE, ";
        $query .= "     KIJUN.EIGO AS EIGO_KIJUN, ";
        $query .= "     KIJUN.SUUGAKU AS SUUGAKU_KIJUN, ";
        $query .= "     KIJUN.KOKUGO AS KOKUGO_KIJUN, ";
        $query .= "     KIJUN.TOTAL AS TOTAL_KIJUN, ";
        $query .= "     L027_2.NAME1 AS I_INTERVIEW_VALUE, ";
        $query .= "     CASE ";
        $query .= "         WHEN S035.REMARK3 = '1' THEN '音楽' ";
        $query .= "         WHEN S035.REMARK3 = '2' THEN '美術' ";
        $query .= "         WHEN S035.REMARK3 = '3' THEN '書道' ";
        $query .= "         WHEN S035.REMARK3 = '4' THEN 'どれでも' ";
        $query .= "     END AS ART1, ";
        $query .= "     CASE ";
        $query .= "         WHEN S035.REMARK4 = '1' THEN '音楽' ";
        $query .= "         WHEN S035.REMARK4 = '2' THEN '美術' ";
        $query .= "         WHEN S035.REMARK4 = '3' THEN '書道' ";
        $query .= "         WHEN S035.REMARK4 = '4' THEN 'どれでも' ";
        $query .= "     END AS ART2 ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT VBASE ";
        //入試合格者(VBASE.JUDGEMENTと紐づく名称ML013.NAMESPARE1=1のデータ)が対象
        $query .= "     INNER JOIN NAME_MST L013 ";
        $query .= "          ON L013.NAMECD2 = VBASE.JUDGEMENT ";
        $query .= "         AND L013.NAMECD1 = 'L013' ";
        $query .= "         AND L013.NAMESPARE1 = '1' ";
        $query .= "     LEFT JOIN NAME_MST Z002 ";
        $query .= "          ON Z002.NAMECD1 = 'Z002' ";
        $query .= "         AND VBASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L004 ";
        $query .= "          ON L004.NAMECD2 = VBASE.TESTDIV ";
        $query .= "         AND L004.NAMECD1 = 'L004' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S035 ";
        $query .= "          ON S035.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ";
        $query .= "         AND S035.APPLICANTDIV = VBASE.APPLICANTDIV ";
        $query .= "         AND S035.EXAMNO       = VBASE.EXAMNO ";
        $query .= "         AND S035.SEQ          = '035' ";
        $query .= "     LEFT JOIN NAME_MST L063 ";
        $query .= "          ON L063.NAMECD2 = S035.REMARK1 ";
        $query .= "         AND L063.NAMECD1 = 'L063' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINSCHOOL ";
        $query .= "          ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = VBASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO       = VBASE.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST H201 ";
        $query .= "          ON H201.NAMECD1 = 'H201' ";
        $query .= "         AND ADDR.RELATIONSHIP = H201.NAMECD2 ";
        $query .= "     LEFT JOIN SAT_APP_FORM_MST SAT ";
        $query .= "          ON SAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SAT.SAT_NO = VBASE.JIZEN_BANGOU ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S023 ";
        $query .= "          ON S023.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ";
        $query .= "         AND S023.APPLICANTDIV = VBASE.APPLICANTDIV ";
        $query .= "         AND S023.EXAMNO       = VBASE.EXAMNO ";
        $query .= "         AND S023.SEQ          = '023' ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV1 ";
        $query .= "          ON INTV1.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ";
        $query .= "         AND INTV1.APPLICANTDIV = VBASE.APPLICANTDIV  ";
        $query .= "         AND INTV1.TESTDIV      = VBASE.TESTDIV ";
        $query .= "         AND INTV1.EXAMNO       = VBASE.EXAMNO ";
        $query .= "         AND INTV1.TESTDIV      <> '5' ";
        $query .= "     LEFT JOIN V_NAME_MST L027_1 ";
        $query .= "          ON L027_1.YEAR = VBASE.ENTEXAMYEAR ";
        $query .= "         AND L027_1.NAMECD2 = INTV1.INTERVIEW_VALUE ";
        $query .= "         AND L027_1.NAMECD1 = 'L027' ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV2 ";
        $query .= "          ON INTV2.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ";
        $query .= "         AND INTV2.APPLICANTDIV = VBASE.APPLICANTDIV  ";
        $query .= "         AND INTV2.EXAMNO       = VBASE.EXAMNO ";
        $query .= "         AND INTV2.TESTDIV      = '5' ";
        $query .= "     LEFT JOIN V_NAME_MST L027_2 ";
        $query .= "          ON L027_2.YEAR = VBASE.ENTEXAMYEAR ";
        $query .= "         AND L027_2.NAMECD2 = INTV2.INTERVIEW_VALUE ";
        $query .= "         AND L027_2.NAMECD1 = 'L027' ";
        $query .= "     LEFT JOIN T_SCORE SCORE  ";
        $query .= "          ON SCORE.EXAMNO   = VBASE.EXAMNO  ";
        $query .= "         AND SCORE.TESTDIV = VBASE.TESTDIV  ";
        $query .= "         AND SCORE.TESTDIV <> '5' ";
        $query .= "     LEFT JOIN T_SCORE KIJUN  ";
        $query .= "          ON KIJUN.EXAMNO   = VBASE.EXAMNO  ";
        $query .= "         AND KIJUN.TESTDIV = '5' ";
        $query .= " WHERE ";
        $query .= "     VBASE.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND VBASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND value(VBASE.ENTDIV, '')       = '1' "; // 入学区分
        $query .= "     AND value(VBASE.PROCEDUREDIV, '') = '1' "; // 手続き区分
        $query .= " ORDER BY ";
        $query .= "     VBASE.EXAMNO ";

        return $query;
    }
}
?>
