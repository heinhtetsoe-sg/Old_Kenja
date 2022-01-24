<?php

require_once('for_php7.php');

class knjl320rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMECD2 = '{$model->fixApplicantDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    function getNameMstName1($year, $namecd1, $namecd2)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";
        return $query;
    }

    //名称マスタより取得
    function getKamouk()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L009' ";
        return $query;
    }

    function selectCsvQuery($model) {
        $query .= " SELECT ";
        $query .= "  row_number() over( ";
        $query .= " ORDER BY ";
        // ソート順が成績順の場合に色々変化。傾斜配点/加算点含むの選択によって4パターン。
        if ($model->field["OUTPUT"] == "1") {
            if ($model->field["INC_KASAN"] == "on") {
                if ($model->field["OUTKEISYA"] == "1") {
                    //傾斜配点する
                    $query .= "  VALUE(R1.TOTAL3, -1) DESC, ";
                } else {
                    //傾斜配点しない
                    $query .= "  VALUE(R1.TOTAL1, -1) DESC, ";
                }
            } else {
                if ($model->field["OUTKEISYA"] == "1") {
                    //傾斜配点する
                    $query .= "  VALUE(R1.TOTAL4, -1) DESC, ";
                } else {
                    //傾斜配点しない
                    $query .= "  VALUE(R1.TOTAL2, -1) DESC, ";
                }
            }
        }
        $query .= "  R1.RECEPTNO, ";
        $query .= "  V1.EXAMNO ";
        $query .= "  ) AS NO, ";
        $query .= "  R1.RECEPTNO, ";
        $query .= "  B1.NAME, ";
        $query .= "  B1.NAME_KANA, ";
        $query .= "  B1.BIRTHDAY, ";
        $query .= "  FM.FINSCHOOL_NAME, ";
        $query .= "  FM.FINSCHOOL_NAME_ABBV, ";
        $query .= "  PM.PRISCHOOL_NAME, ";
        $query .= "  '' AS PRISCHOOL_CLASS_NAME, ";
        if ($model->field["TESTDIV"] == "1") {
            $query .= "  CASE WHEN BD2_013.REMARK1 = '1' THEN '○' ELSE '' END AS SHDIV, ";
        } else {
            $query .= "  CASE WHEN BD2_013.REMARK2 = '1' THEN '○' ELSE '' END AS SHDIV, ";
        }
        if ($model->field["OUTKEISYA"] == "1") {
            //傾斜配点する
            $query .= "  S1_1.SCORE2 AS S1_1_SCORE, ";
            $query .= "  S1_2.SCORE2 AS S1_2_SCORE, ";
            $query .= "  S1_4.SCORE2 AS S1_4_SCORE, ";
            $query .= "  S1_5.SCORE2 AS S1_5_SCORE, ";
            $query .= "  S1_6.SCORE2 AS S1_6_SCORE, ";
            $query .= "  S1_7.SCORE2 AS S1_7_SCORE, ";
            $query .= "  R1.TOTAL4 AS TOTAL_A, ";
            $query .= "  R1.TOTAL3 AS TOTAL_B, ";
            $query .= "  R1.TOTAL_RANK4 AS TOTAL_RANK_A, ";
            $query .= "  R1.TOTAL_RANK3 AS TOTAL_RANK_B, ";
        } else {
            //傾斜配点しない
            $query .= "  S1_1.SCORE AS S1_1_SCORE, ";
            $query .= "  S1_2.SCORE AS S1_2_SCORE, ";
            $query .= "  S1_4.SCORE AS S1_4_SCORE, ";
            $query .= "  S1_5.SCORE AS S1_5_SCORE, ";
            $query .= "  S1_6.SCORE AS S1_6_SCORE, ";
            $query .= "  S1_7.SCORE AS S1_7_SCORE, ";
            $query .= "  R1.TOTAL2 AS TOTAL_A, ";
            $query .= "  R1.TOTAL1 AS TOTAL_B, ";
            $query .= "  R1.TOTAL_RANK2 AS TOTAL_RANK_A, ";
            $query .= "  R1.TOTAL_RANK1 AS TOTAL_RANK_B, ";
        }
        $query .= " RD1.REMARK1 AS KATEN1, ";
        $query .= " RD1.REMARK2 AS KATEN2, ";
        $query .= "  INT1.INTERVIEW_VALUE, ";
        $query .= "  R1.JUDGEDIV, ";
        $query .= "  L013.NAME1 AS JDIVNAME, ";
        $query .= "  Z002.ABBV1 AS SEX_NAME, ";
        $query .= "  VB1.INNER_PROMISE, ";                 // ★VB1のテーブルは無い
        $query .= "  VB1_R.RECEPTNO AS OTHER_RECEPTNO, ";  // ★VB1のテーブルは無い
        $query .= "  R1_R.JUDGEDIV AS OTHER_JUDGEDIV, ";
        $query .= "  L013R.NAME1 AS OTHER_JDIVNAME, ";
        $query .= "  B1.REMARK1, ";
        $query .= "  B1.REMARK2, ";
        $query .= "  VALUE(NML064.NAME1, '') AS NAIDAKU, ";
        $query .= "  VALUE(INTEGER(BD1_006.REMARK5),0) + VALUE(INTEGER(BD1_006.REMARK6),0) AS NOTICE ";
        $query .= " FROM ";
        $query .= "  V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V1 ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "    ON B1.ENTEXAMYEAR = V1.ENTEXAMYEAR ";
        $query .= "   AND B1.APPLICANTDIV = V1.APPLICANTDIV ";
        $query .= "   AND B1.EXAMNO = V1.EXAMNO ";
        $query .= "  LEFT JOIN FINSCHOOL_MST FM ";
        $query .= "    ON FM.FINSCHOOLCD = B1.FS_CD ";
        $query .= "  LEFT JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "    ON R1.ENTEXAMYEAR = V1.ENTEXAMYEAR ";
        $query .= "   AND R1.APPLICANTDIV = V1.APPLICANTDIV ";
        $query .= "   AND R1.TESTDIV = V1.TESTDIV ";
        $query .= "   AND R1.EXAMNO = V1.EXAMNO ";
        $query .= "  LEFT JOIN ENTEXAM_SCORE_DAT S1_1 ";
        $query .= "    ON S1_1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "   AND S1_1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "   AND S1_1.TESTDIV = R1.TESTDIV ";
        $query .= "   AND S1_1.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "   AND S1_1.RECEPTNO = R1.RECEPTNO ";
        $query .= "   AND S1_1.TESTSUBCLASSCD = '1' ";
        $query .= "  LEFT JOIN ENTEXAM_SCORE_DAT S1_2 ";
        $query .= "    ON S1_2.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "   AND S1_2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "   AND S1_2.TESTDIV = R1.TESTDIV ";
        $query .= "   AND S1_2.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "   AND S1_2.RECEPTNO = R1.RECEPTNO ";
        $query .= "   AND S1_2.TESTSUBCLASSCD = '2' ";
        $query .= "  LEFT JOIN ENTEXAM_SCORE_DAT S1_4 ";
        $query .= "    ON S1_4.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "   AND S1_4.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "   AND S1_4.TESTDIV = R1.TESTDIV ";
        $query .= "   AND S1_4.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "   AND S1_4.RECEPTNO = R1.RECEPTNO ";
        $query .= "   AND S1_4.TESTSUBCLASSCD = '4' ";
        $query .= "  LEFT JOIN ENTEXAM_SCORE_DAT S1_5 ";
        $query .= "    ON S1_5.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "   AND S1_5.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "   AND S1_5.TESTDIV = R1.TESTDIV ";
        $query .= "   AND S1_5.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "   AND S1_5.RECEPTNO = R1.RECEPTNO ";
        $query .= "   AND S1_5.TESTSUBCLASSCD = '5' ";
        $query .= "  LEFT JOIN ENTEXAM_SCORE_DAT S1_6 ";
        $query .= "    ON S1_6.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "   AND S1_6.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "   AND S1_6.TESTDIV = R1.TESTDIV ";
        $query .= "   AND S1_6.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "   AND S1_6.RECEPTNO = R1.RECEPTNO ";
        $query .= "   AND S1_6.TESTSUBCLASSCD = '6' ";
        $query .= "  LEFT JOIN ENTEXAM_SCORE_DAT S1_7 ";
        $query .= "    ON S1_7.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "   AND S1_7.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "   AND S1_7.TESTDIV = R1.TESTDIV ";
        $query .= "   AND S1_7.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "   AND S1_7.RECEPTNO = R1.RECEPTNO ";
        $query .= "   AND S1_7.TESTSUBCLASSCD = '7' ";
        $query .= "  LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD1 ";
        $query .= "    ON RD1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "   AND RD1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "   AND RD1.TESTDIV = R1.TESTDIV ";
        $query .= "   AND RD1.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "   AND RD1.RECEPTNO = R1.RECEPTNO ";
        $query .= "   AND RD1.SEQ = '008' ";
        $query .= "  LEFT JOIN ENTEXAM_INTERVIEW_DAT INT1 ";
        $query .= "    ON INT1.ENTEXAMYEAR = V1.ENTEXAMYEAR ";
        $query .= "   AND INT1.APPLICANTDIV = V1.APPLICANTDIV ";
        $query .= "   AND INT1.TESTDIV = V1.TESTDIV ";
        $query .= "   AND INT1.EXAMNO = V1.EXAMNO ";
        $query .= "  LEFT JOIN ENTEXAM_RECEPT_DAT R1_R ";
        $query .= "    ON R1_R.ENTEXAMYEAR = V1.ENTEXAMYEAR ";
        $query .= "   AND R1_R.APPLICANTDIV = V1.APPLICANTDIV ";
        //常に反対のTESTDIVで取得(2の時は1、1の時は2)
        if ($model->field["TESTDIV"] == "1") {
            $query .= "   AND R1_R.TESTDIV = '2' ";
        } else {
            $query .= "   AND R1_R.TESTDIV = '1' ";
        }
        $query .= "   AND R1_R.EXAMNO = V1.EXAMNO ";
        $query .= "  LEFT JOIN NAME_MST L013 ";
        $query .= "    ON L013.NAMECD1 = 'L013' ";
        $query .= "   AND L013.NAMECD2 = R1.JUDGEDIV ";
        $query .= "  LEFT JOIN NAME_MST L013R ";
        $query .= "    ON L013R.NAMECD1 = 'L013' ";
        $query .= "   AND L013R.NAMECD2 = R1_R.JUDGEDIV ";
        $query .= "  LEFT JOIN V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT VB1 ";
        $query .= "    ON VB1.ENTEXAMYEAR = V1.ENTEXAMYEAR ";
        $query .= "   AND VB1.APPLICANTDIV = V1.APPLICANTDIV ";
        $query .= "   AND VB1.EXAMNO = V1.EXAMNO ";
        $query .= "   AND VB1.TESTDIV = V1.TESTDIV ";
        $query .= "  LEFT JOIN V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT VB1_R ";
        $query .= "    ON VB1_R.ENTEXAMYEAR = V1.ENTEXAMYEAR ";
        $query .= "   AND VB1_R.APPLICANTDIV = V1.APPLICANTDIV ";
        $query .= "   AND VB1_R.EXAMNO = V1.EXAMNO ";
        //常に反対のTESTDIVで取得(2の時は1、1の時は2)
        if ($model->field["TESTDIV"] == "1") {
            $query .= "   AND VB1_R.TESTDIV = '2' ";
        } else {
            $query .= "   AND VB1_R.TESTDIV = '1' ";
        }
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_013 ON BD2_013.ENTEXAMYEAR = B1.ENTEXAMYEAR  ";
        $query .= "   AND BD2_013.APPLICANTDIV = B1.APPLICANTDIV  ";
        $query .= "   AND BD2_013.EXAMNO       = B1.EXAMNO  ";
        $query .= "   AND BD2_013.SEQ          = '013'  ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_014 ON BD2_014.ENTEXAMYEAR = B1.ENTEXAMYEAR  ";
        $query .= "   AND BD2_014.APPLICANTDIV = B1.APPLICANTDIV  ";
        $query .= "   AND BD2_014.EXAMNO       = B1.EXAMNO  ";
        $query .= "   AND BD2_014.SEQ          = '014'  ";
        if ($model->field["TESTDIV"] == "1") {
            $query .= "     LEFT JOIN NAME_MST NML064  ";
            $query .= "          ON NML064.NAMECD2 = BD2_014.REMARK1 ";
            $query .= "         AND NML064.NAMECD1 = 'L064'  ";
        } else {
            $query .= "     LEFT JOIN NAME_MST NML064  ";
            $query .= "          ON NML064.NAMECD2 = BD2_014.REMARK2 ";
            $query .= "         AND NML064.NAMECD1 = 'L064'  ";
        }
        $query .= "  LEFT JOIN NAME_MST Z002 ";
        $query .= "          ON Z002.NAMECD2 = B1.SEX ";
        $query .= "         AND Z002.NAMECD1 = 'Z002' ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD08 ON BD08.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "   AND BD08.APPLICANTDIV    = B1.APPLICANTDIV  ";
        $query .= "   AND BD08.EXAMNO          = B1.EXAMNO  ";
        $query .= "   AND BD08.SEQ             = '008'  ";
        $query .= "  LEFT JOIN PRISCHOOL_MST PM ";
        $query .= "    ON PM.PRISCHOOLCD = BD08.REMARK1 ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT BD1_006 ON BD1_006.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "   AND BD1_006.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "   AND BD1_006.EXAMNO       = B1.EXAMNO ";
        $query .= "   AND BD1_006.SEQ          = '006' ";
        $query .= " WHERE ";
        $query .= "  V1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "  AND V1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "  AND V1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " ORDER BY ";
        // ソート順が成績順の場合に色々変化。傾斜配点/加算点含むの選択によって4パターン。
        if ($model->field["OUTPUT"] == "2") {
            if ($model->field["INC_KASAN"] == "on") {
                if ($model->field["OUTKEISYA"] == "1") {
                    //傾斜配点する
                    $query .= "  VALUE(R1.TOTAL3, -1) DESC, ";
                } else {
                    //傾斜配点しない
                    $query .= "  VALUE(R1.TOTAL1, -1) DESC, ";
                }
            } else {
                if ($model->field["OUTKEISYA"] == "1") {
                    //傾斜配点する
                    $query .= "  VALUE(R1.TOTAL4, -1) DESC, ";
                } else {
                    //傾斜配点しない
                    $query .= "  VALUE(R1.TOTAL2, -1) DESC, ";
                }
            }
        }
        $query .= "  R1.RECEPTNO, ";
        $query .= "  V1.EXAMNO ";

        return $query;
    }
}
?>
