<?php

require_once('for_php7.php');
class knjl583aQuery extends Query {
    //名称マスタL013より取得
    function getL013NameMst($year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L013' ";
        $query .= "     AND NAMESPARE1 <> '' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else if ($namecd1 == "L008") {
            $query .= "     ABBV2 ";
        } else {
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
    function getNameMstIdName($model, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    function getTestSubjectId($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     ESDWK1.TESTSUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ESDWK1 ";
        $query .= " LEFT JOIN ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "   ON RCPT.ENTEXAMYEAR = ESDWK1.ENTEXAMYEAR ";
        $query .= "  AND RCPT.APPLICANTDIV = ESDWK1.APPLICANTDIV ";
        $query .= "  AND RCPT.TESTDIV = ESDWK1.TESTDIV ";
        $query .= "  AND RCPT.RECEPTNO = ESDWK1.RECEPTNO ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DAT EABD ";
        $query .= "   ON EABD.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "  AND EABD.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "  AND EABD.TESTDIV = RCPT.TESTDIV ";
        $query .= "  AND EABD.EXAMNO = RCPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     EABD.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND EABD.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ESDWK1.TESTSUBCLASSCD ";
        return $query;
    }

    //一覧表示
    function getCsvQuery($model, $subclsnamelist) {
        $query .= " WITH RCPT_BASE AS ( ";
        $query .= " SELECT ";
        $query .= "     ROW_NUMBER() OVER(partition by ESDWK1.RECEPTNO ORDER BY ESDWK1.ENTEXAMYEAR, ESDWK1.APPLICANTDIV, ESDWK1.TESTDIV, ESDWK1.EXAM_TYPE, ESDWK1.RECEPTNO, ESDWK1.TESTSUBCLASSCD) AS rn, ";
        $query .= "     RCPT.ENTEXAMYEAR, ";
        $query .= "     RCPT.APPLICANTDIV, ";
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     RCPT.EXAM_TYPE, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     RCPT.TOTAL1, ";
        $query .= "     RCPT.TOTAL_RANK4, ";
        $query .= "     RCPT.TOTAL4, ";
        $query .= "     ESDWK1.TESTSUBCLASSCD, ";
        $query .= "     ESDWK1.ATTEND_FLG, ";
        $query .= "     ESDWK1.SCORE, ";
        $query .= "     ESDWK1.STD_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT ESDWK1 ";
        $query .= "       ON ESDWK1.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "      AND ESDWK1.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "      AND ESDWK1.TESTDIV = RCPT.TESTDIV ";
        $query .= "      AND ESDWK1.EXAM_TYPE = RCPT.EXAM_TYPE ";
        $query .= "      AND ESDWK1.RECEPTNO = RCPT.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     RCPT.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.RECEPTNO, ";
        $query .= "   T2.NAME, ";
        $query .= "   T2.NAME_KANA, ";
        $query .= "   HOPEMST_SUC.HOPE_NAME, ";
        $query .= "   SCORE01.SCORE AS SCORE_1, ";
        $query .= "   DECIMAL(ROUND(SCORE01.STD_SCORE*10)/10, 4, 1) AS SCORE_DEV1, ";
        $query .= "   SCORE02.SCORE AS SCORE_2, ";
        $query .= "   DECIMAL(ROUND(SCORE02.STD_SCORE*10)/10, 4, 1) AS SCORE_DEV2, ";
        $query .= "   SCORE03.SCORE AS SCORE_3, ";
        $query .= "   DECIMAL(ROUND(SCORE03.STD_SCORE*10)/10, 4, 1) AS SCORE_DEV3, ";
        $query .= "   T1.TOTAL4, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT01, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT02, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT03, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT04, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT05, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT06, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT07, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT08, ";
        $query .= "   CRPT.CONFIDENTIAL_RPT09, ";
        $query .= "   CRPT.TOTAL5, ";
        $query .= "   CRPT.TOTAL_ALL, ";
        $query .= "   CASE WHEN T2.JUDGEMENT = '1' AND T2.SUC_COURSECODE IS NOT NULL THEN HOPEMST_REAL.PASS_NAME ";
        $query .= "        ELSE L013.NAME1 ";
        $query .= "        END AS GOUHI, ";
        $query .= "   CASE WHEN T2.JUDGEMENT = '1' AND T2.SUC_COURSECODE IS NOT NULL THEN ";
        $query .= "        CASE WHEN BASEDTL01.REMARK10 = T2.SUC_COURSECODE THEN L013.NAME1 ";
        $query .= "             ELSE HOPEMST_SUC.NOT_PASS_NAME ";
        $query .= "             END ";
        $query .= "        ELSE L013.NAME1 ";
        $query .= "        END AS GLABEL, ";
        $query .= "   NML025.NAME2 AS SHOUGAKU, ";
        $query .= "   CASE WHEN BASEDTL30.REMARK2 = '0' THEN '' ELSE NML066.NAME1 END AS TOKUTAIKANYUU, ";
        $query .= "   CASE WHEN BASEDTL04.REMARK8 = '1' THEN 'あり' ELSE '' END AS PROMISE, ";
        $query .= "   CASE WHEN BASEDTL22.REMARK1 = '1' THEN '〇' ELSE '' END AS LSUM_ADMFEE, ";
        $query .= "   CASE WHEN T2.PROCEDUREDIV = '1' THEN '〇' ELSE '' END AS ADMFEE, ";
        $query .= "   CASE WHEN T2.ENTDIV = '2' THEN '〇' ELSE '' END AS DECLINE ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "    AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "   LEFT JOIN RCPT_BASE SCORE01 ";
        $query .= "     ON SCORE01.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND SCORE01.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND SCORE01.TESTDIV = T1.TESTDIV ";
        $query .= "    AND SCORE01.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "    AND SCORE01.RECEPTNO = T1.RECEPTNO ";
        $query .= "    AND SCORE01.TESTSUBCLASSCD = '".$subclsnamelist[0]."' ";
        $query .= "   LEFT JOIN RCPT_BASE SCORE02 ";
        $query .= "     ON SCORE02.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND SCORE02.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND SCORE02.TESTDIV = T1.TESTDIV ";
        $query .= "    AND SCORE02.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "    AND SCORE02.RECEPTNO = T1.RECEPTNO ";
        $query .= "      AND SCORE02.TESTSUBCLASSCD = '".$subclsnamelist[1]."' ";
        $query .= "   LEFT JOIN RCPT_BASE SCORE03 ";
        $query .= "     ON SCORE03.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND SCORE03.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND SCORE03.TESTDIV = T1.TESTDIV ";
        $query .= "    AND SCORE03.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "    AND SCORE03.RECEPTNO = T1.RECEPTNO ";
        $query .= "      AND SCORE03.TESTSUBCLASSCD = '".$subclsnamelist[2]."' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ";
        $query .= "     ON BASEDTL01.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "    AND BASEDTL01.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "    AND BASEDTL01.EXAMNO = T2.EXAMNO ";
        $query .= "    AND BASEDTL01.SEQ = '001' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CRPT ";
        $query .= "     ON CRPT.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "    AND CRPT.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "    AND CRPT.EXAMNO = T2.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL30 ";
        $query .= "     ON BASEDTL30.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "    AND BASEDTL30.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "    AND BASEDTL30.EXAMNO = T2.EXAMNO ";
        $query .= "    AND BASEDTL30.SEQ = '030' ";
        $query .= "   LEFT JOIN NAME_MST NML025 ";
        $query .= "     ON NML025.NAMECD1 = 'L025' ";
        $query .= "    AND NML025.NAMECD2 = BASEDTL30.REMARK1 ";
        $query .= "   LEFT JOIN NAME_MST NML066 ";
        $query .= "     ON NML066.NAMECD1 = 'L066' ";
        $query .= "    AND NML066.NAMECD2 = BASEDTL30.REMARK2 ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL04 ";
        $query .= "     ON BASEDTL04.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "    AND BASEDTL04.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "    AND BASEDTL04.EXAMNO = T2.EXAMNO ";
        $query .= "    AND BASEDTL04.SEQ = '004' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL22 ";
        $query .= "     ON BASEDTL22.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "    AND BASEDTL22.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "    AND BASEDTL22.EXAMNO = T2.EXAMNO ";
        $query .= "    AND BASEDTL22.SEQ = '022' ";
        $query .= "    LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_SUC ";
        $query .= "      ON HOPEMST_SUC.HOPE_COURSECODE = BASEDTL01.REMARK10 ";
        $query .= "    LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_REAL ";
        $query .= "      ON HOPEMST_REAL.HOPE_COURSECODE = T2.SUC_COURSECODE ";
        $query .= "    LEFT JOIN NAME_MST L013 ";
        $query .= "      ON L013.NAMECD1 = 'L013' ";
        $query .= "     AND L013.NAMECD2 = T2.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";

        return $query;
    }
}
?>