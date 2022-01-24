<?php
class knjl321aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //試験回コンボ
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        if ($testdiv) {
            $query .= "     TESTDIV_NAME ";
        } else {
            $query .= "     TESTDIV AS VALUE, ";
            $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
            $query .= "     DEFAULT_FLG AS NAMESPARE2, ";
            $query .= "     TESTDIV, ";
            $query .= "     TESTDIV_NAME, ";
            $query .= "     TESTDIV_ABBV, ";
            $query .= "     PRETEST_FLG "; //プレテストフラグ
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //受験校種(J,H)を取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= "     AND NAMECD2 = '{$model->field["APPLICANTDIV"]}' ";

        return $query;
    }

    //CSV出力
    public function getCsvQuery($model)
    {
        $query  = "";
        //入試
        $query .= " WITH T_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPTNO, ";
        $query .= "         TESTSUBCLASSCD, ";
        $query .= "         SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT ";
        $query .= "     WHERE ";
        $query .= "             ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "         AND APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND TESTDIV        = '{$model->field["TESTDIV"]}' ";
        $query .= "         AND EXAM_TYPE      = '{$model->examType}' ";
        $query .= " ) ";
        //プレテスト
        $query .= " , PRE_TEST AS ( ";
        $query .= "     SELECT ";
        $query .= "         RCPT.EXAMNO, ";
        $query .= "         RCPT.TESTDIV, ";
        $query .= "         SUM(SCOP.SCORE) AS TOTAL ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "         LEFT JOIN ENTEXAM_TESTDIV_MST TSTM ";
        $query .= "              ON TSTM.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "             AND TSTM.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "             AND TSTM.TESTDIV        = RCPT.TESTDIV ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT SCOP ";
        $query .= "              ON SCOP.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "             AND SCOP.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "             AND SCOP.TESTDIV        = RCPT.TESTDIV ";
        $query .= "             AND SCOP.EXAM_TYPE      = RCPT.EXAM_TYPE ";
        $query .= "             AND SCOP.RECEPTNO       = RCPT.RECEPTNO ";
        $query .= "     WHERE ";
        $query .= "             RCPT.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "         AND RCPT.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND RCPT.EXAM_TYPE      = '{$model->examType}' ";
        $query .= "         AND TSTM.PRETEST_FLG    = '1' "; //プレテストフラグ
        $query .= "     GROUP BY ";
        $query .= "         RCPT.EXAMNO, ";
        $query .= "         RCPT.TESTDIV ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     RCPT.DIV_RANK1 AS COURSE_RANK, ";
        $query .= "     RCPT.TOTAL_RANK1 AS ALL_RANK, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     R006.REMARK1 AS SHDIV, ";
        $query .= "     L006.ABBV1 AS SHDIV_NAME, ";
        $query .= "     L058.ABBV1 AS COURSE_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.ABBV1 AS SEX_ABBV, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        foreach ($model->subClassArr as $subCd => $subName) {
            $query .= "     SCO{$subCd}.SCORE AS SCORE{$subCd}, ";
        }
        $query .= "     L055.NAMESPARE2 AS MINASHI, ";
        $query .= "     RCPT.TOTAL4, ";
        $query .= "     RCPT.TOTAL3, ";
        $query .= "     RCPT.TOTAL2, ";
        $query .= "     RCPT.TOTAL1, ";
        $query .= "     INTV.SCORE AS SCORE6, ";
        foreach ($model->preTestArr as $preCd => $preName) {
            $query .= "     PRE{$preCd}.TOTAL AS PRE_TOTAL{$preCd}, ";
        }
        $query .= "     HNR0.HONORDIV_ABBV AS HONORDIV0, ";
        $query .= "     HNR1.HONORDIV_ABBV AS HONORDIV1, ";
        $query .= "     HNR2.HONORDIV_ABBV AS HONORDIV2, ";
        $query .= "     HNR3.HONORDIV_ABBV AS HONORDIV3, ";
        $query .= "     NM_JIZEN_SEN.ABBV1 AS JIZEN_SENGAN, ";
        $query .= "     L013.ABBV1 AS ENT_COURSE, ";
        $query .= "     L013_8.ABBV1 AS SENGAN_ENT, ";
        $query .= "     B029.REMARK9 AS HONOR_REMARK, ";
        $query .= "     RCPT.EXAMNO, "; //志願者SEQ
        $query .= "     NM_DESIRE.ABBV1 AS DESIREDIV, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.EMERGENCYTELNO, ";
        $query .= "     BASE.FS_GRDYEAR, ";
        $query .= "     BASE.FS_M, ";
        $query .= "     L016.NAME1 AS FS_GRDDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";

        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO         = RCPT.EXAMNO ";

        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B029 ";
        $query .= "          ON B029.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND B029.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND B029.EXAMNO         = RCPT.EXAMNO ";
        $query .= "         AND B029.SEQ            = '029' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ";
        $query .= "          ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND R006.TESTDIV        = RCPT.TESTDIV ";
        $query .= "         AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ";
        $query .= "         AND R006.RECEPTNO       = RCPT.RECEPTNO ";
        $query .= "         AND R006.SEQ            = '006' ";
        foreach ($model->subClassArr as $subCd => $subName) {
            $query .= "     LEFT JOIN T_SCORE SCO{$subCd} ON SCO{$subCd}.RECEPTNO = RCPT.RECEPTNO AND SCO{$subCd}.TESTSUBCLASSCD = '{$subCd}' ";
        }
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ005 ";
        $query .= "          ON SEQ005.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ005.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ005.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ005.SEQ          = '005' ";
        $query .= "     LEFT JOIN NAME_MST L055 ";
        $query .= "          ON L055.NAMECD1    = 'L055' ";
        $query .= "         AND L055.NAMECD2    = SEQ005.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ";
        $query .= "          ON INTV.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND INTV.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND INTV.TESTDIV        = RCPT.TESTDIV ";
        $query .= "         AND INTV.EXAMNO         = RCPT.EXAMNO ";
        foreach ($model->preTestArr as $preCd => $preName) {
            $query .= "     LEFT JOIN PRE_TEST PRE{$preCd} ON PRE{$preCd}.EXAMNO = RCPT.EXAMNO AND PRE{$preCd}.TESTDIV = '{$preCd}' ";
        }
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON FINS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "          ON Z002.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND Z002.NAMECD1    = 'Z002' ";
        $query .= "         AND Z002.NAMECD2    = BASE.SEX ";
        $query .= "     LEFT JOIN V_NAME_MST L006 ";
        $query .= "          ON L006.YEAR       = R006.ENTEXAMYEAR ";
        $query .= "         AND L006.NAMECD1    = 'L006' ";
        $query .= "         AND L006.NAMECD2    = R006.REMARK1 ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "          ON L013.YEAR       = R006.ENTEXAMYEAR ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "         AND L013.NAMECD1    = 'LJ13' ";
        } else {
            $query .= "         AND L013.NAMECD1    = 'LH13' ";
        }
        $query .= "         AND L013.NAMECD2    = (CASE R006.REMARK1 WHEN '1' THEN R006.REMARK8 WHEN '2' THEN R006.REMARK9 ELSE NULL END) ";
        $query .= "     LEFT JOIN V_NAME_MST L013_8 ";
        $query .= "          ON L013_8.YEAR     = RCPT.ENTEXAMYEAR ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "         AND L013_8.NAMECD1    = 'LJ13' ";
        } else {
            $query .= "         AND L013_8.NAMECD1    = 'LH13' ";
        }
        $query .= "         AND L013_8.NAMECD2  = R006.REMARK8 ";
        $query .= "     LEFT JOIN V_NAME_MST L058 ";
        $query .= "          ON L058.YEAR       = R006.ENTEXAMYEAR ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "         AND L058.NAMECD1    = 'LJ58' ";
        } else {
            $query .= "         AND L058.NAMECD1    = 'LH58' ";
        }
        $query .= "         AND L058.NAMECD2    = R006.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_HONORDIV_MST HNR0 ";
        $query .= "          ON HNR0.ENTEXAMYEAR    = B029.ENTEXAMYEAR ";
        $query .= "         AND HNR0.APPLICANTDIV   = B029.APPLICANTDIV ";
        $query .= "         AND HNR0.HONORDIV       = B029.REMARK7 ";
        $query .= "     LEFT JOIN ENTEXAM_HONORDIV_MST HNR1 ";
        $query .= "          ON HNR1.ENTEXAMYEAR    = B029.ENTEXAMYEAR ";
        $query .= "         AND HNR1.APPLICANTDIV   = B029.APPLICANTDIV ";
        $query .= "         AND HNR1.HONORDIV       = B029.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_HONORDIV_MST HNR2 ";
        $query .= "          ON HNR2.ENTEXAMYEAR    = B029.ENTEXAMYEAR ";
        $query .= "         AND HNR2.APPLICANTDIV   = B029.APPLICANTDIV ";
        $query .= "         AND HNR2.HONORDIV       = B029.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_HONORDIV_MST HNR3 ";
        $query .= "          ON HNR3.ENTEXAMYEAR    = B029.ENTEXAMYEAR ";
        $query .= "         AND HNR3.APPLICANTDIV   = B029.APPLICANTDIV ";
        $query .= "         AND HNR3.HONORDIV       = B029.REMARK3 ";
        $query .= "     LEFT JOIN NAME_MST NM_DESIRE ";
        $query .= "          ON NM_DESIRE.NAMECD1    = 'L{$model->schoolKind}57' ";
        $query .= "         AND NM_DESIRE.NAMECD2    = B029.REMARK6 ";
        $query .= "     LEFT JOIN NAME_MST NM_JIZEN_SEN ";
        $query .= "          ON NM_JIZEN_SEN.NAMECD1 = 'L{$model->schoolKind}58' ";
        $query .= "         AND NM_JIZEN_SEN.NAMECD2 = B029.REMARK8 ";
        $query .= "     LEFT JOIN NAME_MST L016 ";
        $query .= "          ON L016.NAMECD1 = 'L016' ";
        $query .= "         AND L016.NAMECD2 = VALUE(BASE.FS_GRDDIV, '1') ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND RCPT.TESTDIV        = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '{$model->examType}' ";
        if (strlen($model->field["SHDIV"]) && $model->field["SHDIV"] != "ALL") {
            $query .= "     AND R006.REMARK1        = '{$model->field["SHDIV"]}' ";
        }
        if (strlen($model->field["EXAMCOURSE"]) && $model->field["EXAMCOURSE"] != "ALL") {
            $query .= "     AND R006.REMARK2        = '{$model->field["EXAMCOURSE"]}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["SORT"] == "1") {
            $query .= "     VALUE(RCPT.TOTAL1, -1) DESC, ";
        }
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    //CSV出力(他合格コース欄)
    public function getCsvPassCourseQuery($model)
    {
        $schoolKind = ($model->field["APPLICANTDIV"] == "1") ? "J" : "H";

        $query   = " SELECT ";
        $query  .= "    T1.ENTEXAMYEAR, ";
        $query  .= "    T1.APPLICANTDIV, ";
        $query  .= "    T1.EXAMNO, ";
        $query  .= "    T1.RECEPTNO, ";
        $query  .= "    T1.TESTDIV, ";
        $query  .= "    T5.TESTDIV_NAME, ";
        $query  .= "    T5.TESTDIV_ABBV, ";
        $query  .= "    T2.REMARK8 AS SENGAN_PASS_DIV, ";
        $query  .= "    T2.REMARK9 AS HEIGAN_PASS_DIV, ";
        $query  .= "    T3.ABBV1 AS SENGAN, ";
        $query  .= "    T4.ABBV1 AS HEIGAN ";
        $query  .= " FROM ";
        $query  .= "    ENTEXAM_RECEPT_DAT T1 ";
        $query  .= " INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT T2 ";
        $query  .= "     ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query  .= "    AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query  .= "    AND T1.TESTDIV = T2.TESTDIV ";
        $query  .= "    AND T1.EXAM_TYPE = T2.EXAM_TYPE ";
        $query  .= "    AND T1.RECEPTNO = T2.RECEPTNO ";
        $query  .= "    AND T2.SEQ = '006' ";
        $query  .= " LEFT JOIN V_NAME_MST T3 ";
        $query  .= "     ON T1.ENTEXAMYEAR = T3.YEAR ";
        $query  .= "    AND T3.NAMECD1 = 'L{$schoolKind}13' ";
        $query  .= "    AND T3.NAMECD2 = T2.REMARK8 ";
        $query  .= " LEFT JOIN V_NAME_MST T4 ";
        $query  .= "     ON T1.ENTEXAMYEAR = T4.YEAR ";
        $query  .= "    AND T4.NAMECD1 = 'L{$schoolKind}13' ";
        $query  .= "    AND T4.NAMECD2 = T2.REMARK9 ";
        $query  .= " LEFT JOIN ENTEXAM_TESTDIV_MST T5 ";
        $query  .= "     ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query  .= "    AND T5.APPLICANTDIV = T1.APPLICANTDIV ";
        $query  .= "    AND T5.TESTDIV = T1.TESTDIV ";
        $query  .= " WHERE ";
        $query  .= "    T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query  .= "    AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query  .= "    AND T1.EXAM_TYPE = '1' ";
        $query  .= " ORDER BY ";
        $query  .= "    T1.EXAMNO, ";
        $query  .= "    T1.TESTDIV ";

        return $query;
    }
}
