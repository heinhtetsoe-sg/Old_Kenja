<?php
class knjl111iquery extends Query
{
    public function getNameMst($model, $namecd1, $namecd2 = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
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

    // 科目取得
    public function getTestSubclassList($model)
    {
        $query  = " SELECT ";
        $query .= "     SEQ ";
        $query .= "   , NAME1 ";
        $query .= " FROM ENTEXAM_SETTING_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND SETTING_CD = 'L009' ";
        $query .= "   AND NAME1 IS NOT NULL ";
        $query .= "   AND SEQ IN (1, 2, 3) ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";
        return $query;
    }

    //入試区分コンボ
    public function getEntexamTestDivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分名取得
    public function getEntexamTestDivAbbv($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV_ABBV ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        return $query;
    }

    public function selectCsvQuery($model, $testsubclassList)
    {
        $query  = " SELECT ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.TESTDIV, "; // 入試区分コード
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.SEX, "; // 性別コード
        $query .= "     R1.ATTEND_ALL_FLG, "; // 出欠区分コード
        $query .= "     B1.JUDGEMENT, "; // 合格区分コード
        $query .= "     B_D1.REMARK1 AS PROCEDUREDIV1, "; // 1次入学手続き済フラグ
        $query .= "     B1.PROCEDUREDIV AS PROCEDUREDIV2, "; // 2次入学手続き済フラグ
        $query .= "     B_D1.REMARK3 AS PROCEDUREDIV3, "; // 招集日参加フラグ
        $query .= "     B1.ENTDIV, "; // 入学辞退者フラグ
        $query .= "     B1.BIRTHDAY AS BIRTHDAY_1, ";
        $query .= "     B1.FS_AREA_DIV, "; // 出身校地区コード
        $query .= "     NM_AREACD.NAME1 AS FS_AREA_NAME,"; // 出身校地区名称
        $query .= "     B1.FS_CD, ";
        $query .= "     FM1.FINSCHOOL_NAME, ";
        $query .= "     VARCHAR_FORMAT(B1.FS_DAY, 'YYYYMM') AS FS_YM, "; // 卒業年月 西暦
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
        $query .= "     ADDR1.GTELNO2, ";
        foreach ($testsubclassList as $key => $val) {
            $query .= "     S{$key}.SCORE AS EXAM_SCORE{$key}, ";
        }
        $query .= "     IN1.INTERVIEW_A, "; // 面接評価結果
        for ($remarkNo = 1; $remarkNo <= 7; $remarkNo++) {
            $query .= "     DOCU1_{$remarkNo}.REMARK1 AS OTHER_REMARK_{$remarkNo}, "; // 資料評価結果
        }
        $query .= "     R1.TOTAL1, "; // 受験科目合計点
        $query .= "     R1.AVARAGE1, "; // 受験科目平均点
        $query .= "     R1.JUDGE_DEVIATION, "; // 受験科目偏差値
        $query .= "     R1.TOTAL_RANK1, "; // 全体序列
        $query .= "     R1.SEX_RANK1 "; // 性別序列
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "         ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B_D1 ";
        $query .= "         ON B_D1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND B_D1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B_D1.EXAMNO = R1.EXAMNO ";
        $query .= "         AND B_D1.SEQ = '022' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM1 ";
        $query .= "         ON FM1.FINSCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ";
        $query .= "         ON ADDR1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND ADDR1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND ADDR1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT IN1 ";
        $query .= "         ON IN1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND IN1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND IN1.TESTDIV = R1.TESTDIV ";
        $query .= "         AND IN1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST NM_AREACD ";
        $query .= "         ON NM_AREACD.NAMECD1 = 'Z003' ";
        $query .= "         AND NM_AREACD.NAMECD2 = FM1.DISTRICTCD ";
        foreach ($testsubclassList as $key => $val) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S{$key} ";
            $query .= "          ON S{$key}.ENTEXAMYEAR     = R1.ENTEXAMYEAR ";
            $query .= "         AND S{$key}.APPLICANTDIV    = R1.APPLICANTDIV ";
            $query .= "         AND S{$key}.TESTDIV         = R1.TESTDIV ";
            $query .= "         AND S{$key}.EXAM_TYPE       = R1.EXAM_TYPE ";
            $query .= "         AND S{$key}.RECEPTNO        = R1.RECEPTNO ";
            $query .= "         AND S{$key}.TESTSUBCLASSCD  = '{$key}' ";
        }
        for ($remarkNo = 1; $remarkNo <= 7; $remarkNo++) {
            $seq = sprintf("%03d", $remarkNo);
            $query .= "     LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT DOCU1_{$remarkNo} ";
            $query .= "          ON DOCU1_{$remarkNo}.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
            $query .= "         AND DOCU1_{$remarkNo}.APPLICANTDIV = R1.APPLICANTDIV ";
            $query .= "         AND DOCU1_{$remarkNo}.TESTDIV = R1.TESTDIV ";
            $query .= "         AND DOCU1_{$remarkNo}.EXAM_TYPE = R1.EXAM_TYPE ";
            $query .= "         AND DOCU1_{$remarkNo}.EXAMNO = R1.EXAMNO ";
            $query .= "         AND DOCU1_{$remarkNo}.SEQ = '{$seq}' ";
        }
        $query .= " WHERE ";
        $query .= "         R1.ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND R1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     R1.EXAMNO ";

        return $query;
    }
}
