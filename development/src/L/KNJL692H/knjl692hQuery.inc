<?php
class knjl692hQuery extends Query
{
    //名称マスタ取得
    public function getEntexamTestDivMst($year, $applicantDiv, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= "   , TESTDIV_NAME ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$year}' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($testdiv != "") {
            $query .= "   AND TESTDIV = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //類別マスタ取得
    public function getEntexamClassifyMst($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     CLASSIFY_CD || ':' || VALUE(CLASSIFY_NAME, '') AS LABEL,  ";
        $query .= "     CLASSIFY_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CLASSIFY_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' ";
        $query .= " AND APPLICANTDIV = '".$applicantDiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    public function getExaminee($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.RECEPTNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     Z002.NAME1 AS SEX, ";
        $query .= "     T3_031.REMARK10 AS REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "      AND T2.EXAM_TYPE = '1' ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "       ON Z002.YEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND Z002.NAMECD1 = 'Z002' ";
        $query .= "      AND Z002.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3_031 ";
        $query .= "       ON T3_031.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T3_031.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T3_031.EXAMNO = T1.EXAMNO ";
        $query .= "      AND T3_031.SEQ = '031' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '2' "; // 2:高校 固定
        $query .= "     AND T1.TESTDIV  = '{$model->field["TESTDIV"]}' ";
        if ($model->field["TESTDIV1"] != "00") {
            $query .= "     AND T1.TESTDIV1 = '{$model->field["TESTDIV1"]}' ";
        }
        //受験番号(開始)
        if ($model->field["RECEPTNO_START"] != "") {
            $query .= " AND T2.RECEPTNO >= '{$model->field["RECEPTNO_START"]}' ";
        }
        //受験番号(終了)
        if ($model->field["RECEPTNO_END"] != "") {
            $query .= " AND T2.RECEPTNO <= '{$model->field["RECEPTNO_END"]}' ";
        }
        if ($model->field["FILTER1"] == "2") {
            $query .= " AND T1.JUDGEMENT = '1' ";
        } elseif ($model->field["FILTER1"] == "3") {
            $query .= " AND T1.JUDGEMENT = '1' ";
            $query .= " AND T1.TESTDIV1  = '01' "; //推薦
        } elseif ($model->field["FILTER1"] == "4") {
            $query .= " AND T1.JUDGEMENT = '1' ";
            $query .= " AND T1.ENT_DIV = '2' ";
        } elseif ($model->field["FILTER1"] == "5") {
            $query .= " AND T1.JUDGEMENT = '1' ";
            $query .= " AND T1.ENT_DIV = '1' ";
            $query .= " AND T1.PROCEDUREDIV = '1' ";
        }
        if ($model->field["SEX"] != "3") {
            $query .= " AND T1.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["ORDER"] == "2") {
            $query .= " T1.NAME ";
        } else {
            $query .= " T2.RECEPTNO ";
        }

        return $query;
    }

    public function getCsvData($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     ROWNUMBER() OVER() AS ROW_NUMBER, ";
        $query .= "     CASE WHEN BASE.ENTDIV = '1' THEN '〇' ELSE '' END AS ENTDIV, "; // 入学
        $query .= "     CASE WHEN BASE.PROCEDUREDIV = '1' THEN '〇' ELSE '' END AS PROCEDUREDIV, "; // 手続き
        $query .= "     CASE WHEN BASE.JUDGEMENT = '1' THEN '〇' ELSE '' END AS JUDGEMENT, "; // 合格
        $query .= "     CLASSIFY.MARK, "; // 類別
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     Z002.NAME1 AS SEX, ";  // 性別
        $query .= "     VARCHAR_FORMAT(BASE.BIRTHDAY, 'yyyy/MM/dd') AS BIRTHDAY, "; // 生年月日
        $query .= "     L001.NAME1 AS FINSCHOOL_DIST_NAME, ";  // 出身
        $query .= "     FS.FINSCHOOL_NAME, "; // 中学
        $query .= "     CONF.CONFIDENTIAL_RPT01, ";
        $query .= "     CONF.CONFIDENTIAL_RPT02, ";
        $query .= "     CONF.CONFIDENTIAL_RPT03, ";
        $query .= "     CONF.CONFIDENTIAL_RPT04, ";
        $query .= "     CONF.CONFIDENTIAL_RPT05, ";
        $query .= "     CONF.TOTAL3, "; // 3教科
        $query .= "     CONF.TOTAL5, "; // 5教科
        $query .= "     CONF.TOTAL_ALL, "; // 9教科
        $query .= "     SCORE_KOKUGO.SCORE AS SCORE_KOKUGO, "; // 国語
        $query .= "     SCORE_SUGAKU.SCORE AS SCORE_SUGAKU, "; // 数学
        $query .= "     SCORE_EIGO.SCORE AS SCORE_EIGO, "; // 英語
        $query .= "     RECEPT.TOTAL4 AS TOTAL, "; // 合計
        $query .= "     RECEPT.TOTAL_RANK4 AS TOTAL_RANK, "; // 順位
        $query .= "     L027.NAME1 AS INTERVIEW_A, "; // 面接
        $query .= "     REGEXP_REPLACE(VALUE(BD031.REMARK8, '') || '　' || VALUE(BD031.REMARK9, '') || '　' || VALUE(BD031.REMARK10, ''), '(^　+)|(　+$)', '') AS REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "            ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L012 ";
        $query .= "            ON L012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND L012.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND L012.SETTING_CD   = 'L012' ";
        $query .= "           AND L012.SEQ          = BASE.ENTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_CLASSIFY_MST CLASSIFY ";
        $query .= "            ON CLASSIFY.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND CLASSIFY.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND CLASSIFY.CLASSIFY_CD  = BASE.TESTDIV1 ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "            ON Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= "           AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FS ";
        $query .= "            ON FS.FINSCHOOLCD  = BASE.FS_CD ";
        $query .= "     LEFT JOIN NAME_MST L001 ";
        $query .= "            ON L001.NAMECD1 = 'L001' ";
        $query .= "           AND L001.NAMECD2 = FS.FINSCHOOL_DISTCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ";
        $query .= "            ON CONF.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND CONF.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE_KOKUGO ";
        $query .= "            ON SCORE_KOKUGO.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND SCORE_KOKUGO.APPLICANTDIV   = RECEPT.APPLICANTDIV ";
        $query .= "           AND SCORE_KOKUGO.TESTDIV        = RECEPT.TESTDIV ";
        $query .= "           AND SCORE_KOKUGO.EXAM_TYPE      = RECEPT.EXAM_TYPE ";
        $query .= "           AND SCORE_KOKUGO.RECEPTNO       = RECEPT.RECEPTNO ";
        $query .= "           AND SCORE_KOKUGO.TESTSUBCLASSCD = '1' "; // 国語
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE_SUGAKU ";
        $query .= "            ON SCORE_SUGAKU.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND SCORE_SUGAKU.APPLICANTDIV   = RECEPT.APPLICANTDIV ";
        $query .= "           AND SCORE_SUGAKU.TESTDIV        = RECEPT.TESTDIV ";
        $query .= "           AND SCORE_SUGAKU.EXAM_TYPE      = RECEPT.EXAM_TYPE ";
        $query .= "           AND SCORE_SUGAKU.RECEPTNO       = RECEPT.RECEPTNO ";
        $query .= "           AND SCORE_SUGAKU.TESTSUBCLASSCD = '2' "; // 数学
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE_EIGO ";
        $query .= "            ON SCORE_EIGO.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND SCORE_EIGO.APPLICANTDIV   = RECEPT.APPLICANTDIV ";
        $query .= "           AND SCORE_EIGO.TESTDIV        = RECEPT.TESTDIV ";
        $query .= "           AND SCORE_EIGO.EXAM_TYPE      = RECEPT.EXAM_TYPE ";
        $query .= "           AND SCORE_EIGO.RECEPTNO       = RECEPT.RECEPTNO ";
        $query .= "           AND SCORE_EIGO.TESTSUBCLASSCD = '3' "; // 英語
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ";
        $query .= "            ON INTERVIEW.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND INTERVIEW.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND INTERVIEW.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND INTERVIEW.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L027 ";
        $query .= "            ON L027.ENTEXAMYEAR  = INTERVIEW.ENTEXAMYEAR ";
        $query .= "           AND L027.APPLICANTDIV = INTERVIEW.APPLICANTDIV ";
        $query .= "           AND L027.SETTING_CD   = 'L027' ";
        $query .= "           AND L027.SEQ          = INTERVIEW.INTERVIEW_A ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ";
        $query .= "            ON BD031.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND BD031.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND BD031.EXAMNO       = BASE.EXAMNO ";
        $query .= "           AND BD031.SEQ          = '031' ";
        $query .= " WHERE  ";
        $query .= "     RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= " AND RECEPT.APPLICANTDIV = '2' "; // 2:高校 固定
        $query .= " AND RECEPT.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        if ($model->field["TESTDIV1"] != "00") {
            $query .= " AND BASE.TESTDIV1       = '{$model->field["TESTDIV1"]}' ";
        }
        //受験番号(開始)
        if ($model->field["RECEPTNO_START"] != "") {
            $query .= " AND RECEPT.RECEPTNO >= '{$model->field["RECEPTNO_START"]}' ";
        }
        //受験番号(終了)
        if ($model->field["RECEPTNO_END"] != "") {
            $query .= " AND RECEPT.RECEPTNO <= '{$model->field["RECEPTNO_END"]}' ";
        }
        if ($filter2 == "2") {
            $query .= " AND BASE.JUDGEMENT = '1' ";
        } elseif ($filter2 == "3") {
            $query .= " AND BASE.JUDGEMENT = '1' ";
            $query .= " AND BASE.TESTDIV1  = '01' "; //推薦
        } elseif ($filter2 == "4") {
            $query .= " AND BASE.JUDGEMENT = '1' ";
            $query .= " AND BASE.ENTDIV = '2' ";
        } elseif ($filter2 == "5") {
            $query .= " AND BASE.JUDGEMENT = '1' ";
            $query .= " AND BASE.ENTDIV = '1' ";
            $query .= " AND BASE.PROCEDUREDIV = '1' ";
        }
        if ($model->field["SEX"] != "3") {
            $query .= " AND T1.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["ORDER"] == "2") {
            $query .= " BASE.NAME ";
        } else {
            $query .= " RECEPT.RECEPTNO ";
        }

        return $query;
    }

    public function getPassExaminee($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.RECEPTNO, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "      AND T2.EXAM_TYPE = '1' ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '2' "; // 2:高校 固定
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["TESTDIV1"] != "00") {
            $query .= "     AND T1.TESTDIV1 = '{$model->field["TESTDIV1"]}' ";
        }
        $query .= "     AND T1.JUDGEMENT = '1' "; // 1:合格
        if ($model->field["RECEPTNO_START"] != "") {
            $query .= " AND T2.RECEPTNO >= '{$model->field["RECEPTNO_START"]}' ";
        }
        if ($model->field["RECEPTNO_END"] != "") {
            $query .= " AND T2.RECEPTNO <= '{$model->field["RECEPTNO_END"]}' ";
        }
        if ($model->field["SEX"] != "3") {
            $query .= " AND T1.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["ORDER"] == "2") {
            $query .= " T1.NAME ";
        } else {
            $query .= " T2.RECEPTNO ";
        }

        return $query;
    }
}
