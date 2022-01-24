<?php

require_once('for_php7.php');

class knjl580aQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="", $getName="") {
        $query  = " SELECT ";
        if ((!is_array($getName) && $getName == "") && $namecd2=="") {
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     T1.* ";
        } else {
            if (is_array($getName)) {
                foreach ($getName as $val) {
                    $query .= "     {$val} ";
                }
            } else {
                if ($getName != "") {
                    $query .= "     {$getName} ";
                }
            }
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        if ($namecd1 == 'L008') {
            $query .= "     AND NAMESPARE1 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //志望区分コンボ
    function getPassCourseCd($model, $passCourse = "") {
        $query  = " SELECT ";
        if ($passCourse) {
            $query .= "  T2.HOPE_NAME AS CSV_LABEL ";
        } else {
            $query .= "   T1.HOPE_COURSECODE || ':' || T2.HOPE_NAME AS LABEL, ";  // 志望区分コード
            $query .= "   T1.HOPE_COURSECODE AS VALUE ";                          // 志望区分名称
        }
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_YDAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_HOPE_COURSE_MST T2 ";
        $query .= "     ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
        if ($passCourse) {
            $query .= "     AND T1.HOPE_COURSECODE = '{$passCourse}' ";
        } else {
            $query .= " UNION ";
            //最低1行確実にデータが出れば良いので、自ユーザーをSTAFF_MSTに照会することで1RECORD作成
            $query .= " SELECT ";
            $query .= "  '不合格' AS LABEL, ";
            $query .= "  '".$model->CONST_FAILER."' AS VALUE ";
            $query .= " FROM ";
            $query .= "  STAFF_MST ";
            $query .= " WHERE ";
            $query .= "  STAFFCD = '".STAFFCD."' ";
            $query .= "  ";
            $query .= " ORDER BY ";
            $query .= "  VALUE ";
        }

        return $query;
    }

    //一覧表示
    function getCsvQuery($model, $subclscdlist) {

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
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     FM.FINSCHOOL_NAME_ABBV AS FINSCHOOL_ABBV, ";
        $query .= "     CRPT.TOTAL5, ";
        //5科目だが、5科目がどの番号の物かは別テーブル管理なので、別途取得したリストを利用して設定。
        foreach ($subclscdlist as $val) {
            $query .= "     CRPT.CONFIDENTIAL_RPT0{$val}, ";
        }
        $query .= "     T1.TOTAL4, ";
        $query .= "     T1.TOTAL_RANK4, ";
        $query .= "     CASE WHEN BASEDTL04.REMARK8 = '1' THEN 'あり' ELSE '' END AS PROMISEPAPER, ";
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
        $query .= "          (CASE WHEN CRPT_CHK.CRPT_CHK5_01 = 1 THEN CRPT.CONFIDENTIAL_RPT01 ELSE 0 END ";
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
        $query .= "          (CASE WHEN CRPT_CHK.CRPT_CHK9_01 = 1 THEN CRPT.CONFIDENTIAL_RPT01 ELSE 0 END ";
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
        $query .= "     HOPEMST_SUC.HOPE_NAME, ";
        $query .= "     L013.NAME1 ";
        $query .= "     || CASE WHEN T2.JUDGEMENT = '1' AND T2.SUC_COURSECODE IS NOT NULL ";
        $query .= "                  AND BASEDTL01.REMARK10 <> T2.SUC_COURSECODE ";
        $query .= "                  THEN '(' || HOPEMST_SUC.NOT_PASS_NAME || ')' ";
        $query .= "             ELSE '' ";
        $query .= "        END AS RESULT, ";
        $query .= "     NML025.NAME2 AS SHOUGAKU, ";
        $query .= "     NML066.NAME1 AS TOKUTAIKANYUU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.TESTDIV = T1.TESTDIV AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL_MST ESM ";
        $query .= "       ON ESM.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "      AND ESM.ENTEXAM_SCHOOLCD = T2.FS_CD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM ";
        $query .= "       ON FM.FINSCHOOLCD = ESM.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ";
        $query .= "       ON BASEDTL01.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "      AND BASEDTL01.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "      AND BASEDTL01.EXAMNO = T2.EXAMNO ";
        $query .= "      AND BASEDTL01.SEQ = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CRPT ";
        $query .= "       ON CRPT.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "      AND CRPT.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "      AND CRPT.EXAMNO = T2.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL30 ";
        $query .= "       ON BASEDTL30.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "      AND BASEDTL30.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "      AND BASEDTL30.EXAMNO = T2.EXAMNO ";
        $query .= "      AND BASEDTL30.SEQ = '030' ";
        $query .= "     LEFT JOIN NAME_MST NML025 ";
        $query .= "       ON NML025.NAMECD1 = 'L025' ";
        $query .= "      AND NML025.NAMECD2 = BASEDTL30.REMARK1 ";
        $query .= "     LEFT JOIN NAME_MST NML066 ";
        $query .= "       ON NML066.NAMECD1 = 'L066' ";
        $query .= "      AND NML066.NAMECD2 = BASEDTL30.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL04 ";
        $query .= "       ON BASEDTL04.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "      AND BASEDTL04.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "      AND BASEDTL04.EXAMNO = T2.EXAMNO ";
        $query .= "      AND BASEDTL04.SEQ = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL22 ";
        $query .= "       ON BASEDTL22.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "      AND BASEDTL22.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "      AND BASEDTL22.EXAMNO = T2.EXAMNO ";
        $query .= "      AND BASEDTL22.SEQ = '022' ";
        $query .= "     LEFT JOIN ENTEXAM_CONFRPT_BASE_MST ECBM ";
        $query .= "       ON ECBM.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "      AND ECBM.TESTDIV = T2.TESTDIV ";
        $query .= "      AND ECBM.HOPE_COURSECODE = BASEDTL01.REMARK10 ";
        $query .= "     LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_SUC ";
        $query .= "       ON HOPEMST_SUC.HOPE_COURSECODE = BASEDTL01.REMARK10 ";
        $query .= "     LEFT JOIN NAME_MST L013 ";
        $query .= "       ON L013.NAMECD1 = 'L013' ";
        $query .= "      AND L013.NAMECD2 = T2.JUDGEMENT ";
        $query .= "     LEFT JOIN CRPT_DETAIL_CHL CRPT_CHK ";
        $query .= "       ON CRPT_CHK.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND CRPT_CHK.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND CRPT_CHK.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($model->passCourse != $model->CONST_SELALL) {
            if ($model->passCourse != "" && $model->passCourse !== $model->CONST_FAILER) {
                $query .= "     AND T2.SUC_COURSECODE = '{$model->passCourse}' ";
            } else {
                $query .= "     AND T2.JUDGEMENT IN ('2', '4') ";
            }
        }
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= " T1.TOTAL4 IS NULL ASC, ";
            $query .= " T1.TOTAL4 DESC, ";
        }
        $query .= " T1.RECEPTNO ";

        return $query;
    }
}
?>