<?php

require_once('for_php7.php');

class knjl640iQuery extends Query
{
    //入試区分マスタより取得
    public function getTestDivMst($year, $applicantdiv, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR       = '{$year}' ";
        $query .= "     AND APPLICANTDIV  = '{$applicantdiv}' ";
        if ($testdiv != "") {
            $query .= "   AND TESTDIV = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特待生以外の入学申込金、施設設備費
    public function getDefaultCost($model)
    {
        $query  = "      SELECT ";
        $query .= "          REMARK3 AS ENTRY_FEE, ";
        $query .= "          REMARK6 AS EQUIPMENT_COST ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_TESTDIV_MST TDM ";
        $query .= "          LEFT JOIN ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDD001 ";
        $query .= "              ON TDD001.ENTEXAMYEAR    = TDM.ENTEXAMYEAR ";
        $query .= "              AND TDD001.APPLICANTDIV  = TDM.APPLICANTDIV ";
        $query .= "              AND TDD001.TESTDIV       = TDM.TESTDIV ";
        $query .= "              AND TDD001.SEQ           = '001' ";
        $query .= "      WHERE ";
        $query .= "          TDM.ENTEXAMYEAR         = '".$model->examYear."' ";
        $query .= "          AND TDM.APPLICANTDIV    = '".$model->applicantdiv."' ";
        $query .= "          AND TDM.TESTDIV         = '".$model->field["TESTDIV"]."' ";

        return $query;
    }

    public function getNyugakuTetsudukiSyuryousyaQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     ROWNUMBER() OVER() AS ROW_NUMBER, ";
        $query .= "     CASE WHEN BASE.TESTDIV0 = '1' THEN '普通科' WHEN BASE.TESTDIV0 = '2' THEN '工業科' ELSE '' END AS SUBJECT, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     Z002.NAME2 AS SEX, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FS.FINSCHOOL_NAME_ABBV, ";
        if ($model->field["OUTPUT_TYP1_SUB"] == "1") {
            // 1:施設設備費の場合
            $query .= "     DIV04.REMARK2 AS DEPOSIT, ";
            $query .= "     DIV04.REMARK2 AS TOTAL_MONEY, ";
            $query .= "     TO_CHAR(RECEPT_D020.REMARK4, 'MM') || '月' || TO_CHAR(RECEPT_D020.REMARK4, 'DD') || '日' AS DEPOSIT_DATE, ";
        } else {
            // 2:入学申込金の場合
            $query .= "     DIV04.REMARK1 AS DEPOSIT, ";
            $query .= "     DIV04.REMARK1 AS TOTAL_MONEY, ";
            $query .= "     TO_CHAR(RECEPT_D020.REMARK2, 'MM') || '月' || TO_CHAR(RECEPT_D020.REMARK2, 'DD') || '日' AS DEPOSIT_DATE, ";
        }
        $query .= "     DIV04.GENERAL_MARK AS REMARK, ";
        $query .= "     DIV04.GENERAL_CD AS SP_CD, "; //特待コード
        $query .= "     DIV05.GENERAL_CD AS SP_REASON_CD "; //特待理由コード
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ";
        $query .= "               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND L013.SETTING_CD   = 'L013' ";
        $query .= "           AND L013.SEQ          = RECEPT.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "           AND BASE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON ";
        $query .= "               Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= "           AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST AS FS ON ";
        $query .= "               FS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ";
        $query .= "               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST DIV04 ON ";
        $query .= "               DIV04.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ";
        $query .= "           AND DIV04.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ";
        $query .= "           AND DIV04.TESTDIV      = '0' "; // '0' 固定
        $query .= "           AND DIV04.GENERAL_DIV  = '04' ";
        $query .= "           AND DIV04.GENERAL_CD   = RECEPT_D015.REMARK4 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST DIV05 ON ";
        $query .= "               DIV05.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ";
        $query .= "           AND DIV05.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ";
        $query .= "           AND DIV05.TESTDIV      = '0' "; // '0' 固定
        $query .= "           AND DIV05.GENERAL_DIV  = '05' ";
        $query .= "           AND DIV05.GENERAL_CD   = RECEPT_D015.REMARK5 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ";
        $query .= "               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D020.SEQ          = '020' ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR  = '".$model->examYear."' ";
        $query .= " AND RECEPT.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND RECEPT.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " AND L013.NAMESPARE1     = '1' "; // 1:合格者
        $query .= " AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "; // 2:辞退者 以外
        // ALL:全て 以外の場合に条件に加える
        if ($model->field["DEPT"] != "ALL") {
            $query .= " AND BASE.TESTDIV0       = '".$model->field["DEPT"]."' ";
        }
        if ($model->field["OUTPUT_TYP1_SUB"] == "1") {
            // 1:施設設備費の場合
            $query .= " AND RECEPT_D020.REMARK3 = '1' ";
        } else {
            // 2:入学申込金の場合
            $query .= " AND RECEPT_D020.REMARK1 = '1' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["ORDER_BY"] == "2") {
            $query .= "     BASE.NAME_KANA, ";
        }
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    public function getNyugakuTetsudukiSyuryousyaSuQuery($model)
    {
        $query  = " WITH TEMP AS ( ";
        $query .= " SELECT ";
        if ($model->field["OUTPUT_TYP2_SUB"] == "1") {
            // 1:施設設備費の場合
            $query .= "     RECEPT_D020.REMARK4 AS DEPOSIT_DATE, ";
        } else {
            // 2:入学申込金の場合
            $query .= "     RECEPT_D020.REMARK2 AS DEPOSIT_DATE, ";
        }
        $query .= "     Z002.NAME2 AS SEX, ";
        $query .= "     CASE WHEN BASE.TESTDIV0 = '1' THEN 1 ELSE 0 END AS GENERAL_CNT, ";
        $query .= "     CASE WHEN BASE.TESTDIV0 = '2' THEN 1 ELSE 0 END AS TECHNICAL_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ";
        $query .= "               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND L013.SETTING_CD   = 'L013' ";
        $query .= "           AND L013.SEQ          = RECEPT.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "           AND BASE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON ";
        $query .= "               Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= "           AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ";
        $query .= "               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST DIV04 ON ";
        $query .= "               DIV04.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ";
        $query .= "           AND DIV04.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ";
        $query .= "           AND DIV04.TESTDIV      = '0' "; // '0' 固定
        $query .= "           AND DIV04.GENERAL_DIV  = '04' ";
        $query .= "           AND DIV04.GENERAL_CD   = RECEPT_D015.REMARK4 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ";
        $query .= "               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D020.SEQ          = '020' ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR  = '".$model->examYear."' ";
        $query .= " AND RECEPT.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND RECEPT.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " AND L013.NAMESPARE1     = '1' "; // 1:合格者
        $query .= " AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "; // 2:辞退者 以外
        // ALL:全て 以外の場合に条件に加える
        if ($model->field["DEPT"] != "ALL") {
            $query .= " AND BASE.TESTDIV0       = '".$model->field["DEPT"]."' ";
        }
        if ($model->field["OUTPUT_TYP2_SUB"] == "1") {
            // 1:施設設備費の場合
            $query .= " AND RECEPT_D020.REMARK3 = '1' ";
        } else {
            // 2:入学申込金の場合
            $query .= " AND RECEPT_D020.REMARK1 = '1' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     TO_CHAR(DEPOSIT_DATE, 'MM') || '月' || TO_CHAR(DEPOSIT_DATE, 'DD') || '日' AS DEPOSIT_DATE, ";
        $query .= "     CASE WHEN DAYOFWEEK(DEPOSIT_DATE) = '1' THEN '(日)' ";
        $query .= "          WHEN DAYOFWEEK(DEPOSIT_DATE) = '2' THEN '(月)' ";
        $query .= "          WHEN DAYOFWEEK(DEPOSIT_DATE) = '3' THEN '(火)' ";
        $query .= "          WHEN DAYOFWEEK(DEPOSIT_DATE) = '4' THEN '(水)' ";
        $query .= "          WHEN DAYOFWEEK(DEPOSIT_DATE) = '5' THEN '(木)' ";
        $query .= "          WHEN DAYOFWEEK(DEPOSIT_DATE) = '6' THEN '(金)' ";
        $query .= "          WHEN DAYOFWEEK(DEPOSIT_DATE) = '7' THEN '(土)' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS WEEKDAY, ";
        $query .= "     SEX, ";
        $query .= "     VALUE(SUM(GENERAL_CNT), 0) AS GENERAL_CNT, ";
        $query .= "     VALUE(SUM(TECHNICAL_CNT), 0) AS TECHNICAL_CNT, ";
        $query .= "     (VALUE(SUM(GENERAL_CNT), 0) + VALUE(SUM(TECHNICAL_CNT), 0)) AS SUMCNT ";
        $query .= " FROM ";
        $query .= "     TEMP ";
        $query .= " GROUP BY ";
        $query .= "     DEPOSIT_DATE, ";
        $query .= "     SEX ";
        $query .= " ORDER BY ";
        $query .= "     DEPOSIT_DATE, ";
        $query .= "     SEX DESC ";

        return $query;
    }

    public function getMinyukinsyaListQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     ROWNUMBER() OVER() AS ROW_NUMBER, ";
        $query .= "     CASE WHEN BASE.TESTDIV0 = '1' THEN '普通科' WHEN BASE.TESTDIV0 = '2' THEN '工業科' ELSE '' END AS SUBJECT, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     Z002.NAME2 AS SEX, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FS.FINSCHOOL_NAME_ABBV, ";
        if ($model->field["OUTPUT_TYP3_SUB"] == "1") {
            // 1:施設設備費の場合
            $query .= "     DIV04.REMARK2 AS DEPOSIT, ";
            $query .= "     DIV04.REMARK2 AS TOTAL_MONEY, ";
        } else {
            // 2:入学申込金の場合
            $query .= "     DIV04.REMARK1 AS DEPOSIT, ";
            $query .= "     DIV04.REMARK1 AS TOTAL_MONEY, ";
        }
        $query .= "     '' AS DEPOSIT_DATE, ";
        $query .= "     DIV04.GENERAL_MARK AS REMARK, ";
        $query .= "     DIV04.GENERAL_CD AS SP_CD, "; //特待コード
        $query .= "     DIV05.GENERAL_CD AS SP_REASON_CD "; //特待理由コード
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ";
        $query .= "               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND L013.SETTING_CD   = 'L013' ";
        $query .= "           AND L013.SEQ          = RECEPT.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "           AND BASE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON ";
        $query .= "               Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= "           AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST AS FS ON ";
        $query .= "               FS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ";
        $query .= "               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST DIV04 ON ";
        $query .= "               DIV04.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ";
        $query .= "           AND DIV04.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ";
        $query .= "           AND DIV04.TESTDIV      = '0' "; // '0' 固定
        $query .= "           AND DIV04.GENERAL_DIV  = '04' ";
        $query .= "           AND DIV04.GENERAL_CD   = RECEPT_D015.REMARK4 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST DIV05 ON ";
        $query .= "               DIV05.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ";
        $query .= "           AND DIV05.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ";
        $query .= "           AND DIV05.TESTDIV      = '0' "; // '0' 固定
        $query .= "           AND DIV05.GENERAL_DIV  = '05' ";
        $query .= "           AND DIV05.GENERAL_CD   = RECEPT_D015.REMARK5 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ";
        $query .= "               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D020.SEQ          = '020' ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR  = '".$model->examYear."' ";
        $query .= " AND RECEPT.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND RECEPT.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " AND L013.NAMESPARE1     = '1' "; // 1:合格者
        $query .= " AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "; // 2:辞退者 以外
        // ALL:全て 以外の場合に条件に加える
        if ($model->field["DEPT"] != "ALL") {
            $query .= " AND BASE.TESTDIV0       = '".$model->field["DEPT"]."' ";
        }
        if ($model->field["OUTPUT_TYP3_SUB"] == "1") {
            // 1:施設設備費の場合
            $query .= " AND (RECEPT_D020.REMARK3 <> '1' OR RECEPT_D020.REMARK3 IS NULL) ";
        } else {
            // 2:入学申込金の場合
            $query .= " AND (RECEPT_D020.REMARK1 <> '1' OR RECEPT_D020.REMARK1 IS NULL) ";
        }
        $query .= " ORDER BY ";
        if ($model->field["ORDER_BY"] == "2") {
            $query .= "     BASE.NAME_KANA, ";
        }
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    public function getNyuryouKibousyaListQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     ROWNUMBER() OVER() AS ROW_NUMBER, ";
        $query .= "     REGD_H.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE_D026.REMARK1 AS SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     Z002.NAME2 AS SEX, ";
        $query .= "     FS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     DIV05.GENERAL_NAME AS HONOR_REASON, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     VALUE(ADDR.ADDRESS1, '') || VALUE(ADDR.ADDRESS2, '') AS ADDRESS, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     L042.NAME1 AS DORMITORIES_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ";
        $query .= "               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D020.SEQ          = '020' ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ";
        $query .= "               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND L013.SETTING_CD   = 'L013' ";
        $query .= "           AND L013.SEQ          = RECEPT.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "           AND BASE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D026 ON ";
        $query .= "               BASE_D026.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND BASE_D026.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND BASE_D026.EXAMNO       = BASE.EXAMNO ";
        $query .= "           AND BASE_D026.SEQ          = '026' ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON ";
        $query .= "               REGD.YEAR     = BASE_D026.ENTEXAMYEAR ";
        $query .= "           AND REGD.SEMESTER = '1' ";
        $query .= "           AND REGD.SCHREGNO = BASE_D026.REMARK1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON ";
        $query .= "               REGD_H.YEAR     = REGD.YEAR ";
        $query .= "           AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND REGD_H.GRADE    = REGD.GRADE ";
        $query .= "           AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON ";
        $query .= "               Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= "           AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST AS FS ON ";
        $query .= "               FS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ";
        $query .= "               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST DIV05 ON ";
        $query .= "               DIV05.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ";
        $query .= "           AND DIV05.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ";
        $query .= "           AND DIV05.TESTDIV      = '0' "; // '0' 固定
        $query .= "           AND DIV05.GENERAL_DIV  = '05' ";
        $query .= "           AND DIV05.GENERAL_CD   = RECEPT_D015.REMARK5 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ";
        $query .= "               ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L042 ON ";
        $query .= "               L042.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND L042.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND L042.SETTING_CD   = 'L042' ";
        $query .= "           AND L042.SEQ          = BASE.SEX ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR  = '".$model->examYear."' ";
        $query .= " AND RECEPT.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND RECEPT.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        // ALL:全て 以外の場合に条件に加える
        if ($model->field["DEPT"] != "ALL") {
            $query .= " AND BASE.TESTDIV0       = '".$model->field["DEPT"]."' ";
        }
        $query .= " AND BASE.DORMITORY_FLG  = '1' "; // 1:入寮希望者
        $query .= " AND L013.NAMESPARE1     = '1' "; // 1:合格者
        $query .= " AND BASE.PROCEDUREDIV   = '1' "; // 1:手続き終了
        $query .= " AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "; // 2:辞退者 以外

        $query .= " ORDER BY ";
        if ($model->field["ORDER_BY"] == "2") {
            $query .= "     BASE.NAME_KANA, ";
        }
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    public function getNyuryouKibousyaLabelQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     FS.FINSCHOOL_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     L042.NAME1 AS DORMITORIES_NAME, ";
        $query .= "     DIV05.GENERAL_NAME AS HONOR_REASON ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ";
        $query .= "               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D020.SEQ          = '020' ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ";
        $query .= "               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND L013.SETTING_CD   = 'L013' ";
        $query .= "           AND L013.SEQ          = RECEPT.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "           AND BASE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D026 ON ";
        $query .= "               BASE_D026.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND BASE_D026.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND BASE_D026.EXAMNO       = BASE.EXAMNO ";
        $query .= "           AND BASE_D026.SEQ          = '026' ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON ";
        $query .= "               REGD.YEAR     = BASE_D026.ENTEXAMYEAR ";
        $query .= "           AND REGD.SEMESTER = '1' ";
        $query .= "           AND REGD.SCHREGNO = BASE_D026.REMARK1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON ";
        $query .= "               REGD_H.YEAR     = REGD.YEAR ";
        $query .= "           AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND REGD_H.GRADE    = REGD.GRADE ";
        $query .= "           AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON ";
        $query .= "               Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= "           AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST AS FS ON ";
        $query .= "               FS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ";
        $query .= "               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST DIV05 ON ";
        $query .= "               DIV05.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ";
        $query .= "           AND DIV05.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ";
        $query .= "           AND DIV05.TESTDIV      = '0' "; // '0' 固定
        $query .= "           AND DIV05.GENERAL_DIV  = '05' ";
        $query .= "           AND DIV05.GENERAL_CD   = RECEPT_D015.REMARK5 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ";
        $query .= "               ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L042 ON ";
        $query .= "               L042.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND L042.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND L042.SETTING_CD   = 'L042' ";
        $query .= "           AND L042.SEQ          = BASE.SEX ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR  = '".$model->examYear."' ";
        $query .= " AND RECEPT.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND RECEPT.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        // ALL:全て 以外の場合に条件に加える
        if ($model->field["DEPT"] != "ALL") {
            $query .= " AND BASE.TESTDIV0       = '".$model->field["DEPT"]."' ";
        }
        $query .= " AND BASE.DORMITORY_FLG  = '1' "; // 1:入寮希望者
        $query .= " AND L013.NAMESPARE1     = '1' "; // 1:合格者
        $query .= " AND BASE.PROCEDUREDIV   = '1' "; // 1:手続き終了
        $query .= " AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "; // 2:辞退者 以外

        $query .= " ORDER BY ";
        if ($model->field["ORDER_BY"] == "2") {
            $query .= "     BASE.NAME_KANA, ";
        }
        $query .= "     BASE.EXAMNO ";

        return $query;
    }
}
