<?php

require_once('for_php7.php');

class knjl570aQuery extends Query {
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

    //志望区分コンボ
    function getHopeCourseCd($model, $cd="") {
        $query  = " SELECT ";
        if ($cd == "") {
            $query .= "   T1.HOPE_COURSECODE || ':' || T2.HOPE_NAME AS LABEL, ";  // 志望区分名称
            $query .= "   T1.HOPE_COURSECODE AS VALUE ";                          // 志望区分コード
        } else {
            $query .= "   T2.HOPE_NAME ";  // 志望区分名称
        }
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_YDAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_HOPE_COURSE_MST T2 ";
        $query .= "     ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
        if ($cd != "") {
            $query .= "   AND T1.HOPE_COURSECODE = '{$cd}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "  T1.HOPE_COURSECODE ";
        }

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
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ";
        $query .= "   ON BASEDTL01.ENTEXAMYEAR = EABD.ENTEXAMYEAR ";
        $query .= "  AND BASEDTL01.APPLICANTDIV = EABD.APPLICANTDIV ";
        $query .= "  AND BASEDTL01.EXAMNO = EABD.EXAMNO ";
        $query .= "  AND BASEDTL01.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     EABD.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND EABD.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($model->testdiv != $model->CONST_SELALL) {
            $query .= "     AND EABD.TESTDIV = {$model->testdiv} ";
        }
        if ($model->desirediv != $model->CONST_SELALL) {
            $query .= "     AND BASEDTL01.REMARK10 = {$model->desirediv} ";
        }
        $query .= " ORDER BY ESDWK1.TESTSUBCLASSCD ";
        return $query;
    }

    //一覧表示
    function getCsvQuery($model, $subclsnamelist) {
        $query  = " WITH CRPT_DETAIL_CHL AS ( ";
        $query .= "     SELECT ";
        $query .= "       CRPT.ENTEXAMYEAR, ";
        $query .= "       CRPT.APPLICANTDIV, ";
        $query .= "       CRPT.EXAMNO, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_01, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_02, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_03, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_04, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_05, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_06, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_07, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_08, ";
        $query .= "       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_09, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_01, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_02, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_03, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_04, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_05, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_06, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_07, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_08, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_09, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01') AND CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_01, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02') AND CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_02, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03') AND CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_03, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04') AND CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_04, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05') AND CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_05, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06') AND CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_06, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07') AND CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_07, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08') AND CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_08, ";
        $query .= "       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09') AND CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_09 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "       ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "      AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "      AND BASE.TESTDIV = RCPT.TESTDIV ";
        $query .= "      AND BASE.EXAMNO = RCPT.EXAMNO ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDAT ";
        $query .= "      ON BASEDAT.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "     AND BASEDAT.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "     AND BASEDAT.EXAMNO = RCPT.EXAMNO ";
        $query .= "     AND BASEDAT.SEQ = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CRPT ";
        $query .= "       ON CRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND CRPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "      AND CRPT.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_CONFRPT_BASE_MST ECBM ";
        $query .= "       ON ECBM.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND ECBM.TESTDIV = BASE.TESTDIV ";
        $query .= "      AND ECBM.HOPE_COURSECODE = BASEDAT.REMARK10 ";
        $query .= " WHERE ";
        $query .= "     RCPT.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ), RCPT_BASE AS ( ";
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
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     FM.FINSCHOOL_NAME_ABBV AS FINSCHOOL_ABBV, ";
        $query .= "     HOPEMST_SUC.HOPE_NAME, ";
        $query .= "     CRPT.TOTAL5, ";
        $query .= "     CRPT.TOTAL_ALL, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT01, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT02, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT03, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT04, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT05, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT06, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT07, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT08, ";
        $query .= "     CRPT.CONFIDENTIAL_RPT09, ";
        $query .= "     SCORE01.TESTSUBCLASSCD AS TESTSUBCLSCD1, ";
        $query .= "     SCORE01.SCORE AS SCORE_1, ";
        $query .= "     SCORE01.STD_SCORE AS SCORE_DEV1, ";
        $query .= "     SCORE02.TESTSUBCLASSCD AS TESTSUBCLSCD2, ";
        $query .= "     SCORE02.SCORE AS SCORE_2, ";
        $query .= "     SCORE02.STD_SCORE AS SCORE_DEV2, ";
        $query .= "     SCORE03.TESTSUBCLASSCD AS TESTSUBCLSCD3, ";
        $query .= "     SCORE03.SCORE AS SCORE_3, ";
        $query .= "     SCORE03.STD_SCORE AS SCORE_DEV3, ";
        $query .= "     RCPT.TOTAL4, ";
        $query .= "     RCPT.TOTAL_RANK4, ";
        $query .= "     CASE WHEN BASEDTL04.REMARK8 = '1' THEN '確約' ELSE '' END AS PROMISEPAPER, ";
        $query .= "     CASE WHEN ";
        $query .= "          (CASE WHEN CRPT_CHK.CRPT_CHKC_01 = 2 OR (CRPT_CHK.CRPT_CHKC_01 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT01, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHKC_02 = 2 OR (CRPT_CHK.CRPT_CHKC_02 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT02, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHKC_03 = 2 OR (CRPT_CHK.CRPT_CHKC_03 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT03, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHKC_04 = 2 OR (CRPT_CHK.CRPT_CHKC_04 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT04, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHKC_05 = 2 OR (CRPT_CHK.CRPT_CHKC_05 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT05, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHKC_06 = 2 OR (CRPT_CHK.CRPT_CHKC_06 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT06, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHKC_07 = 2 OR (CRPT_CHK.CRPT_CHKC_07 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT07, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHKC_08 = 2 OR (CRPT_CHK.CRPT_CHKC_08 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT08, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHKC_09 = 2 OR (CRPT_CHK.CRPT_CHKC_09 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT09, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ";
        $query .= "          ) > 0 THEN '＊' ";
        $query .= "          WHEN ";
        $query .= "          (CASE WHEN CRPT_CHK.CRPT_CHK5_01 = 1 THEN CRPT.CONFIDENTIAL_RPT01 ELSE 0 END  ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK5_02 = 1 THEN CRPT.CONFIDENTIAL_RPT02 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK5_03 = 1 THEN CRPT.CONFIDENTIAL_RPT03 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK5_04 = 1 THEN CRPT.CONFIDENTIAL_RPT04 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK5_05 = 1 THEN CRPT.CONFIDENTIAL_RPT05 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK5_06 = 1 THEN CRPT.CONFIDENTIAL_RPT06 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK5_07 = 1 THEN CRPT.CONFIDENTIAL_RPT07 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK5_08 = 1 THEN CRPT.CONFIDENTIAL_RPT08 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK5_09 = 1 THEN CRPT.CONFIDENTIAL_RPT09 ELSE 0 END ";
        $query .= "          ) < ECBM.SCORE5 THEN '＊' ";
        $query .= "          WHEN ";
        $query .= "          (CASE WHEN CRPT_CHK.CRPT_CHK9_01 = 1 THEN CRPT.CONFIDENTIAL_RPT01 ELSE 0 END  ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK9_02 = 1 THEN CRPT.CONFIDENTIAL_RPT02 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK9_03 = 1 THEN CRPT.CONFIDENTIAL_RPT03 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK9_04 = 1 THEN CRPT.CONFIDENTIAL_RPT04 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK9_05 = 1 THEN CRPT.CONFIDENTIAL_RPT05 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK9_06 = 1 THEN CRPT.CONFIDENTIAL_RPT06 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK9_07 = 1 THEN CRPT.CONFIDENTIAL_RPT07 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK9_08 = 1 THEN CRPT.CONFIDENTIAL_RPT08 ELSE 0 END ";
        $query .= "          + CASE WHEN CRPT_CHK.CRPT_CHK9_09 = 1 THEN CRPT.CONFIDENTIAL_RPT09 ELSE 0 END ";
        $query .= "          ) < ECBM.SCORE9 THEN '＊' ";
        $query .= "          ELSE '' ";
        $query .= "          END AS NOT_STANDARD, ";
        $query .= "     L013.NAME1 ";
        $query .= "     || CASE WHEN BASE.JUDGEMENT = '1' AND BASE.SUC_COURSECODE IS NOT NULL ";
        $query .= "                  AND BASEDTL01.REMARK10 <> BASE.SUC_COURSECODE ";
        $query .= "                  THEN '(' || HOPEMST_SUC.NOT_PASS_NAME || ')' ";
        $query .= "             ELSE '' ";
        $query .= "        END AS RESULT, ";
        $query .= "     FMH1.FINSCHOOL_NAME_ABBV AS HEIGAN_KOURITU1, ";
        $query .= "     FMH2.FINSCHOOL_NAME_ABBV AS HEIGAN_KOURITU2, ";
        $query .= "     FMH3.FINSCHOOL_NAME_ABBV AS HEIGAN_SIRITU1, ";
        $query .= "     FMH4.FINSCHOOL_NAME_ABBV AS HEIGAN_SIRITU2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "       ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "      AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "      AND BASE.TESTDIV = RCPT.TESTDIV ";
        $query .= "      AND BASE.EXAMNO = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL_MST ESM ";
        $query .= "       ON ESM.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND ESM.ENTEXAM_SCHOOLCD = BASE.FS_CD";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM ";
        $query .= "       ON FM.FINSCHOOLCD = ESM.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ";
        $query .= "       ON BASEDTL01.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND BASEDTL01.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "      AND BASEDTL01.EXAMNO = BASE.EXAMNO ";
        $query .= "      AND BASEDTL01.SEQ = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CRPT ";
        $query .= "       ON CRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND CRPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "      AND CRPT.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN RCPT_BASE SCORE01 ";
        $query .= "       ON SCORE01.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "      AND SCORE01.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "      AND SCORE01.TESTDIV = RCPT.TESTDIV ";
        $query .= "      AND SCORE01.EXAM_TYPE = RCPT.EXAM_TYPE ";
        $query .= "      AND SCORE01.RECEPTNO = RCPT.RECEPTNO ";
        $query .= "      AND SCORE01.TESTSUBCLASSCD = '".$subclsnamelist[0]."' ";
        $query .= "     LEFT JOIN RCPT_BASE SCORE02 ";
        $query .= "       ON SCORE02.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "      AND SCORE02.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "      AND SCORE02.TESTDIV = RCPT.TESTDIV ";
        $query .= "      AND SCORE02.EXAM_TYPE = RCPT.EXAM_TYPE ";
        $query .= "      AND SCORE02.RECEPTNO = RCPT.RECEPTNO ";
        $query .= "      AND SCORE02.TESTSUBCLASSCD = '".$subclsnamelist[1]."' ";
        $query .= "     LEFT JOIN RCPT_BASE SCORE03 ";
        $query .= "       ON SCORE03.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "      AND SCORE03.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "      AND SCORE03.TESTDIV = RCPT.TESTDIV ";
        $query .= "      AND SCORE03.EXAM_TYPE = RCPT.EXAM_TYPE ";
        $query .= "      AND SCORE03.RECEPTNO = RCPT.RECEPTNO ";
        $query .= "      AND SCORE03.TESTSUBCLASSCD = '".$subclsnamelist[2]."' ";
        $query .= "     LEFT JOIN NAME_MST L009_1 ";
        $query .= "       ON L009_1.NAMECD1 = 'L009' ";
        $query .= "      AND L009_1.NAMECD2 = SCORE01.TESTSUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST L009_2 ";
        $query .= "       ON L009_2.NAMECD1 = 'L009' ";
        $query .= "      AND L009_2.NAMECD2 = SCORE02.TESTSUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST L009_3 ";
        $query .= "       ON L009_3.NAMECD1 = 'L009' ";
        $query .= "      AND L009_3.NAMECD2 = SCORE03.TESTSUBCLASSCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL04 ";
        $query .= "       ON BASEDTL04.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND BASEDTL04.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "      AND BASEDTL04.EXAMNO = BASE.EXAMNO ";
        $query .= "      AND BASEDTL04.SEQ = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_CONFRPT_BASE_MST ECBM ";
        $query .= "       ON ECBM.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND ECBM.TESTDIV = BASE.TESTDIV ";
        $query .= "      AND ECBM.HOPE_COURSECODE = BASEDTL01.REMARK10 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL05 ";
        $query .= "       ON BASEDTL05.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND BASEDTL05.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "      AND BASEDTL05.EXAMNO = BASE.EXAMNO ";
        $query .= "      AND BASEDTL05.SEQ = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL_MST ESMB05_1 ";
        $query .= "       ON ESMB05_1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND ESMB05_1.ENTEXAM_SCHOOLCD = BASEDTL05.REMARK1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FMH1 ";
        $query .= "       ON FMH1.FINSCHOOLCD = ESMB05_1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL_MST ESMB05_2 ";
        $query .= "       ON ESMB05_2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND ESMB05_2.ENTEXAM_SCHOOLCD = BASEDTL05.REMARK2 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FMH2 ";
        $query .= "       ON FMH2.FINSCHOOLCD = ESMB05_2.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL_MST ESMB05_3 ";
        $query .= "       ON ESMB05_3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND ESMB05_3.ENTEXAM_SCHOOLCD = BASEDTL05.REMARK3 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FMH3 ";
        $query .= "       ON FMH3.FINSCHOOLCD = ESMB05_3.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL_MST ESMB05_4 ";
        $query .= "       ON ESMB05_4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "      AND ESMB05_4.ENTEXAM_SCHOOLCD = BASEDTL05.REMARK4 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FMH4 ";
        $query .= "       ON FMH4.FINSCHOOLCD = ESMB05_4.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_SUC ";
        $query .= "       ON HOPEMST_SUC.HOPE_COURSECODE = BASEDTL01.REMARK10 ";
        $query .= "     LEFT JOIN NAME_MST L013 ";
        $query .= "       ON L013.NAMECD1 = 'L013' ";
        $query .= "      AND L013.NAMECD2 = BASE.JUDGEMENT ";
        $query .= "     LEFT JOIN CRPT_DETAIL_CHL CRPT_CHK ";
        $query .= "       ON CRPT_CHK.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "      AND CRPT_CHK.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "      AND CRPT_CHK.EXAMNO = RCPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     RCPT.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($model->testdiv != $model->CONST_SELALL) {
            $query .= "     AND RCPT.TESTDIV = '{$model->testdiv}' ";
        }
        if ($model->desirediv != $model->CONST_SELALL) {
            $query .= "     AND BASEDTL01.REMARK10 = '{$model->desirediv}' ";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= " RCPT.TOTAL4 IS NULL ASC, ";
            $query .= " RCPT.TOTAL4 DESC, ";
        }
        $query .= " RCPT.RECEPTNO ";
        
        return $query;
    }
}
?>