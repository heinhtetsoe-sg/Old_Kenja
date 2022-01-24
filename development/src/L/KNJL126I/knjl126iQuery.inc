<?php
class knjl126iquery extends Query
{
    public function getNameMst($model, $namecd1, $namecd2 = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->examYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試制度名の取得
    public function getApplicantDivName($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '{$model->examYear}' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= "   AND NAMECD2 = '{$model->field["APPLICANTDIV"]}' ";

        return $query;
    }

    //入試区分コンボ
    public function getEntexamTestDivMst($model, $testdev = '')
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= "   , TESTDIV_ABBV ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($testdev != '') {
            $query .= "     AND TESTDIV = '{$testdev}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //性別の取得
    public function getSex($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '{$model->examYear}' ";
        $query .= "   AND NAMECD1 = 'Z002' ";
        $query .= "   AND NAMECD2 = '{$model->field["SEX"]}' ";

        return $query;
    }

    public function selectCsvQuery($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.TESTDIV, "; // 入試区分コード
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.SEX, "; // 性別コード
        $query .= "     R1.ATTEND_ALL_FLG, "; // 出欠区分コード
        $query .= "     B1.JUDGE_KIND, "; // 内定区分コード
        $query .= "     B1.JUDGEMENT, "; // 合格区分コード
        $query .= "     B1.ENTDIV, "; // 入学辞退者フラグ
        $query .= "     B1.BIRTHDAY, ";
        $query .= "     B1.FS_CD, ";
        $query .= "     FM1.FINSCHOOL_NAME, ";
        $query .= "     ADDR1.ZIPCD, ";
        $query .= "     ADDR1.ADDRESS1, ";
        $query .= "     ADDR1.ADDRESS2, ";
        $query .= "     ADDR1.TELNO, ";
        $query .= "     ADDR1.GNAME, ";
        $query .= "     ADDR1.GKANA, ";
        $query .= "     ADDR1.GZIPCD, ";
        $query .= "     ADDR1.GADDRESS1, ";
        $query .= "     ADDR1.GADDRESS2, ";
        $query .= "     ADDR1.GTELNO, ";
        $query .= "     ADDR1.SEND_ZIPCD, ";
        $query .= "     ADDR1.SEND_ADDRESS1, ";
        $query .= "     ADDR1.SEND_ADDRESS2, ";
        $query .= "     ADDR1.SEND_TELNO, ";
        $query .= "     B_D1.REMARK1 AS SIBOU_RANK, "; // 高等部志望順位
        $query .= "     FM_H1.FINSCHOOL_NAME AS HEIGAN1, "; // 併願校名１
        $query .= "     FM_H2.FINSCHOOL_NAME AS HEIGAN2, "; // 併願校名２
        $query .= "     CONF1.TOTAL_ALL, "; // ９科評定
        $query .= "     CONF_D_SEQ001.REMARK1 AS HYOTEI_1_2_TOTAL, "; // １・２年次評定合計
        // 実技３科評定(A方式のみ)
        if ($model->field["TESTDIV"] == '1') {
            $query .= "     CONF1.TOTAL3 AS JITSUGI3_HYOTEI, ";
        } else {
            $query .= "     '' AS JITSUGI3_HYOTEI, ";
        }
        $query .= "     COALESCE(CONF1.ABSENCE_DAYS, 0) + COALESCE(CONF1.ABSENCE_DAYS2, 0) + COALESCE(CONF1.ABSENCE_DAYS3, 0) AS ABSENCE_DAYS_TOTAL, "; // ３年間欠席日数
        $query .= "     CONF1.ABSENCE_DAYS3, "; // ３年次欠席日数
        $query .= "     CONF_D_SEQ002.REMARK3 AS TYOSASYO_PLUS, "; // +調査書
        $query .= "     CASE ";
        $query .= "         WHEN CONF_D_SEQ002.REMARK4 > 0 THEN - CONF_D_SEQ002.REMARK4 ";
        $query .= "         ELSE CONF_D_SEQ002.REMARK4 ";
        $query .= "     END AS TYOSASYO_MINUS, "; // -調査書
        // 同窓(B方式のみ)
        if ($model->field["TESTDIV"] == '2') {
            $query .= "     CONF_D_SEQ002.REMARK7 AS DOUSOU, ";
        } else {
            $query .= "     '' AS DOUSOU, ";
        }
        $query .= "     CONF_D_SEQ002.REMARK8 AS EIKEN_KYU, "; // 英検取得級
        $query .= "     CONF_D_SEQ002.REMARK9 AS TYOUSEITEN_PLUS, "; // +調整点
        $query .= "     CASE ";
        $query .= "         WHEN CONF_D_SEQ002.REMARK10 > 0 THEN - CONF_D_SEQ002.REMARK10 ";
        $query .= "         ELSE  CONF_D_SEQ002.REMARK10 ";
        $query .= "     END AS TYOUSEITEN_MINUS, "; // -調整点
        $query .= "     NM_INA.NAME1 AS IN_A, "; // 面接評価１
        $query .= "     NM_INB.NAME1 AS IN_B, "; // 面接評価２
        $query .= "     NM_INC.NAME1 AS IN_C, "; // 面接評価３
        $query .= "     IN1.SCORE1, "; // 面接ポイント
        $query .= "     COALESCE(CONF_D_SEQ001.REMARK11, 0) + COALESCE(IN1.SCORE1, 0) AS TOTAL_PLUS, "; // +合計
        $query .= "     - COALESCE(CONF_D_SEQ001.REMARK12, 0) + COALESCE(IN1.SCORE1, 0) AS TOTAL_MINUS, "; // -合計
        $query .= "     S_SUB1.SCORE AS TESTSUBCLASSCD1, "; // 国語
        $query .= "     S_SUB2.SCORE AS TESTSUBCLASSCD2, "; // 数学
        $query .= "     S_SUB3.SCORE AS TESTSUBCLASSCD3, "; // 英語
        // 全体序列 A方式で高等部が第一志望の場合は30点加点した合計
        if ($model->field["TESTDIV"] == '1') {
            $query .= "     CASE ";
            $query .= "         WHEN B_D1.REMARK1 = '1' THEN COALESCE(R1.TOTAL1, 0) + 30 ";
            $query .= "         ELSE  R1.TOTAL1 ";
            $query .= "     END AS TOTAL1, ";
        } else {
            $query .= "     R1.TOTAL1, ";
        }
        $query .= "     R1.SEX_RANK1 "; // 性別順位
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B_D1 ";
        $query .= "          ON B_D1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND B_D1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B_D1.EXAMNO = R1.EXAMNO ";
        $query .= "         AND B_D1.SEQ = '005' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM1 ";
        $query .= "         ON FM1.FINSCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM_H1 ";
        $query .= "         ON FM_H1.FINSCHOOLCD = B_D1.REMARK2 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM_H2 ";
        $query .= "         ON FM_H2.FINSCHOOLCD = B_D1.REMARK3 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ";
        $query .= "          ON ADDR1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND ADDR1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND ADDR1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF1 ";
        $query .= "          ON CONF1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND CONF1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND CONF1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONF_D_SEQ001 ";
        $query .= "          ON CONF_D_SEQ001.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND CONF_D_SEQ001.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND CONF_D_SEQ001.EXAMNO = R1.EXAMNO ";
        $query .= "         AND CONF_D_SEQ001.SEQ = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONF_D_SEQ002 ";
        $query .= "          ON CONF_D_SEQ002.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND CONF_D_SEQ002.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND CONF_D_SEQ002.EXAMNO = R1.EXAMNO ";
        $query .= "         AND CONF_D_SEQ002.SEQ = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT IN1 ";
        $query .= "          ON IN1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND IN1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND IN1.TESTDIV = R1.TESTDIV ";
        $query .= "         AND IN1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST NM_INA ";
        $query .= "          ON NM_INA.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND NM_INA.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND NM_INA.SETTING_CD = 'LH27' ";
        $query .= "         AND NM_INA.SEQ = IN1.INTERVIEW_A ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST NM_INB ";
        $query .= "          ON NM_INB.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND NM_INB.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND NM_INB.SETTING_CD = 'LH27' ";
        $query .= "         AND NM_INB.SEQ = IN1.INTERVIEW_B ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST NM_INC ";
        $query .= "          ON NM_INC.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND NM_INC.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND NM_INC.SETTING_CD = 'LH27' ";
        $query .= "         AND NM_INC.SEQ = IN1.INTERVIEW_C ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S_SUB1 ";
        $query .= "          ON S_SUB1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND S_SUB1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND S_SUB1.TESTDIV = R1.APPLICANTDIV ";
        $query .= "         AND S_SUB1.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "         AND S_SUB1.RECEPTNO = R1.RECEPTNO ";
        $query .= "         AND S_SUB1.TESTSUBCLASSCD = '1' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S_SUB2 ";
        $query .= "          ON S_SUB2.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND S_SUB2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND S_SUB2.TESTDIV = R1.APPLICANTDIV ";
        $query .= "         AND S_SUB2.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "         AND S_SUB2.RECEPTNO = R1.RECEPTNO ";
        $query .= "         AND S_SUB2.TESTSUBCLASSCD = '2' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S_SUB3 ";
        $query .= "          ON S_SUB3.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND S_SUB3.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND S_SUB3.TESTDIV = R1.APPLICANTDIV ";
        $query .= "         AND S_SUB3.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "         AND S_SUB3.RECEPTNO = R1.RECEPTNO ";
        $query .= "         AND S_SUB3.TESTSUBCLASSCD = '3' ";
        $query .= " WHERE ";
        $query .= "         R1.ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND R1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        // 全員以外の場合
        if ($model->field["SEX"] != '3') {
            $query .= "     AND B1.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     R1.EXAMNO ";

        return $query;
    }
}
