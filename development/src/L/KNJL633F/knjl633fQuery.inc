<?php
class knjl633fQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        if (strlen($namecd2)) {
            $query .= "     NAME1 AS VALUE";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMECD1, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     ABBV1, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV出力
    public function selectDataQuery($model, $kyouka_array, $nyushikamoku_array)
    {
        $query  = "  WITH SCORE_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPT.ENTEXAMYEAR, ";
        $query .= "         RECEPT.APPLICANTDIV, ";
        $query .= "         RECEPT.TESTDIV, ";
        $query .= "         RECEPT.EXAMNO, ";
        $query .= "         RECEPT.EXAM_TYPE, ";
        $query .= "         SCORE.TESTSUBCLASSCD, ";
        $query .= "         SCORE.ATTEND_FLG, ";
        $query .= "         SCORE.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "         INNER JOIN ENTEXAM_SCORE_DAT SCORE ";
        $query .= "              ON SCORE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND SCORE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND SCORE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "             AND SCORE.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "             AND SCORE.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     DETAIL010.REMARK" . $model->field["TESTDIV"] . " AS TESTDIV, ";
        } else {
            $query .= "     BASE.TESTDIV, ";
        }
        $query .= "     BASE.SHDIV, "; //専併区分コード
        $query .= "     NML006.NAME1 AS SHDIV_NAME, "; //専併区分名称
        $query .= "     DETAIL001.REMARK10 AS EXAMCOURSE, "; //第一志望コード
        $query .= "     COURSE1.EXAMCOURSE_NAME, "; //第一志望名称
        $query .= "     DETAIL017.REMARK3 AS EXAMCOURSE2, "; //第二志望コード
        $query .= "     COURSE2.EXAMCOURSE_NAME AS EXAMCOURSE2_NAME, "; //第二志望名称
        $query .= "     DETAIL005.REMARK1 AS EIKEN_CD, "; //英語見なしコード
        $query .= "     NML055.NAME1 AS EIKEN_NAME, "; //英語見なし名称
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     DETAIL011.REMARK" . $model->field["TESTDIV"] . " AS TESTDIV1, "; //受験科目数コード
            $query .= "     NML005.NAME1 AS TESTDIV1_NAME, "; //受験科目数名称
            $query .= "     DETAIL012.REMARK" . $model->field["TESTDIV"] . " AS EXAMNO, ";
        } else {
            $query .= "     BASE.TESTDIV1, "; //受験科目数コード
            $query .= "     NML005.NAME1 AS TESTDIV1_NAME, "; //受験科目数名称
            $query .= "     BASE.EXAMNO AS EXAMNO, ";
        }
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.RELATIONSHIP, ";
        $query .= "     NMH201.NAME1 AS RELATIONSHIP_NAME, ";
        $query .= "     DETAIL005.REMARK5 AS TOKUBETU_JYOKEN, ";  //特別条件
        $query .= "     DETAIL005.REMARK2 AS SHOUGAKU_CD, ";      //奨学区分コード
        $query .= "     NML025.NAME1 AS SHOUGAKU_NAME, ";         //奨学区分名称
        $query .= "     DETAIL004.REMARK10 AS JIZENSODAN_TEXT, "; //事前相談文言
        $query .= "     DETAIL004.REMARK9 AS JIZENSODAN_CD, ";    //事前相談コード
        $query .= "     NML032.NAME1 AS JIZENSODAN_NAME, ";       //事前相談名称
        $query .= "     DETAIL005.REMARK3 AS TOKU_KATU, ";        //特別活動 中学
        $query .= "     DETAIL005.REMARK4 AS BIKOU, ";            //備考 中学
        //内申評定
        foreach ($kyouka_array as $key => $value) {
            $query .= "     CONFRPT.CONFIDENTIAL_RPT{$key}, ";
        }
        $query .= "     DETAIL020.REMARK1 AS JITURYOKU_3GOKEI, "; //実力テスト 3科合計
        $query .= "     DETAIL020.REMARK2 AS JITURYOKU_3HEIKIN, "; //実力テスト 3科平均
        $query .= "     DETAIL020.REMARK3 AS JITURYOKU_5GOKEI, "; //実力テスト 5科合計
        $query .= "     DETAIL020.REMARK4 AS JITURYOKU_5HEIKIN, "; //実力テスト 5科平均
        for ($kai = 1; $kai <= 2; $kai++) {
            $seq = ($kai == 1) ? "038" : "039";
            $query .= "     BASE_D_{$seq}.REMARK1 AS PRE_KOKUGO{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK2 AS PRE_SANSUU{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK3 AS PRE_EIGO{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK4 AS PRE_2KA{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK5 AS PRE_3KA{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK6 AS PRE_TOTAL{$kai}, ";
        }
        $query .= "     DETAIL004.REMARK1 AS CLUB_NAME, "; //クラブ名
        $query .= "     CONFRPT.REMARK1 AS TOKKI, ";       //特活・部活・特記事項
        $query .= "     CASE WHEN BASE.JUDGEMENT = '4' THEN '欠席' ELSE '' END AS KESSEKI, "; //欠席
        //入試情報 得点
        foreach ($nyushikamoku_array as $key => $value) {
            $query .= "     SCORE{$key}.SCORE AS NYUSHI_SCORE{$key}, ";
        }
        $query .= "     NML055.NAMESPARE2 AS EIGO_MINASHI, ";    //入試情報 英語見なし得点
        $query .= "     CASE WHEN VALUE(SCORE5.SCORE,-1) < VALUE(SMALLINT(NML055.NAMESPARE2),-1) ";
        $query .= "          THEN SMALLINT(NML055.NAMESPARE2) ";
        $query .= "          ELSE SCORE5.SCORE ";
        $query .= "     END AS EIGO_SCORE, ";                    //入試情報 英語高いほう
        $query .= "     RECEPT.TOTAL2, ";                        //入試情報 2教科合計 中学
        $query .= "     RECEPT.TOTAL3, ";                        //入試情報 合計 中学、3科合計 高校
        $query .= "     RECEPT.TOTAL4, ";                        //入試情報 3教科200点換算 中学、 5科合計 高校
        $query .= "     RECEPT.AVARAGE3 AS NYUSHI_3DEVIATION, "; //入試情報 3科偏差値
        $query .= "     RECEPT.AVARAGE4 AS NYUSHI_5DEVIATION, "; //入入試情報 5科偏差値
        $query .= "     BASE.JUDGEMENT, ";                       //合否情報 合否コード
        $query .= "     NML013.NAME1 AS JUDGEMENT_NAME, ";       //合否情報 合否名称
        $query .= "     BASE.SUC_COURSECD || BASE.SUC_MAJORCD || BASE.SUC_COURSECODE AS COURSEMAJOR, "; //合否情報 合格コースコード
        $query .= "     COURSE3.EXAMCOURSE_NAME AS COURSEMAJOR_NAME, "; //合否情報 合格コース名称
        $query .= "     BASE.PROCEDUREDIV, ";                           //手続情報 区分コード
        $query .= "     NML011.NAME1 AS PROCEDUREDIV_NAME, ";           //手続情報 区分名称
        $query .= "     BASE.ENTDIV, ";                                 //入学情報 区分コード
        $query .= "     NML012.NAME1 AS ENTDIV_NAME ";                  //入学情報 区分名称
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ";
        $query .= "          ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND CONFRPT.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND CONFRPT.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL001 ";
        $query .= "          ON DETAIL001.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL001.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL001.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL001.SEQ         = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL017 ";
        $query .= "          ON DETAIL017.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL017.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL017.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL017.SEQ         = '017' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL010 ";
        $query .= "          ON DETAIL010.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL010.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL010.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL010.SEQ         = '010' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL011 ";
        $query .= "          ON DETAIL011.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL011.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL011.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL011.SEQ         = '011' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL012 ";
        $query .= "          ON DETAIL012.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL012.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL012.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL012.SEQ         = '012' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL005 ";
        $query .= "          ON DETAIL005.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL005.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL005.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL005.SEQ         = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL004 ";
        $query .= "          ON DETAIL004.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL004.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL004.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL004.SEQ         = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL020 ";
        $query .= "          ON DETAIL020.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL020.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL020.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL020.SEQ         = '020' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "          ON RECEPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "         AND RECEPT.TESTDIV      = DETAIL010.REMARK" . $model->field["TESTDIV"] . " ";
        } else {
            $query .= "         AND RECEPT.TESTDIV      = BASE.TESTDIV ";
        }
        $query .= "         AND RECEPT.EXAMNO       = BASE.EXAMNO";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ";
        $query .= "          ON COURSE1.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND COURSE1.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND COURSE1.TESTDIV      = '1' ";
        $query .= "         AND COURSE1.COURSECD     = DETAIL001.REMARK8 ";
        $query .= "         AND COURSE1.MAJORCD      = DETAIL001.REMARK9 ";
        $query .= "         AND COURSE1.EXAMCOURSECD = DETAIL001.REMARK10 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ";
        $query .= "          ON COURSE2.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND COURSE2.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND COURSE2.TESTDIV      = '1' ";
        $query .= "         AND COURSE2.COURSECD     = DETAIL017.REMARK1 ";
        $query .= "         AND COURSE2.MAJORCD      = DETAIL017.REMARK2 ";
        $query .= "         AND COURSE2.EXAMCOURSECD = DETAIL017.REMARK3 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST COURSE3 ";
        $query .= "          ON COURSE3.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND COURSE3.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND COURSE3.TESTDIV      = '1' ";
        $query .= "         AND COURSE3.COURSECD     = BASE.SUC_COURSECD ";
        $query .= "         AND COURSE3.MAJORCD      = BASE.SUC_MAJORCD ";
        $query .= "         AND COURSE3.EXAMCOURSECD = BASE.SUC_COURSECODE ";
        //プレテスト得点(第1回)(第2回)
        for ($kai = 1; $kai <= 2; $kai++) {
            $seq = ($kai == 1) ? "038" : "039";
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_{$seq} ";
            $query .= "      ON BASE_D_{$seq}.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
            $query .= "     AND BASE_D_{$seq}.APPLICANTDIV = BASE.APPLICANTDIV ";
            $query .= "     AND BASE_D_{$seq}.EXAMNO = BASE.EXAMNO ";
            $query .= "     AND BASE_D_{$seq}.SEQ = '{$seq}' ";
        }
        //入試情報 得点
        foreach ($nyushikamoku_array as $key => $value) {
            $query .= "     LEFT JOIN SCORE_DAT SCORE{$key} ";
            $query .= "          ON SCORE{$key}.ENTEXAMYEAR    = BASE.ENTEXAMYEAR ";
            $query .= "         AND SCORE{$key}.APPLICANTDIV   = BASE.APPLICANTDIV ";
            if ($model->field["APPLICANTDIV"] == "1") {
                $query .= "         AND SCORE{$key}.TESTDIV        = DETAIL010.REMARK" . $model->field["TESTDIV"] . " ";
            } else {
                $query .= "         AND SCORE{$key}.TESTDIV        = BASE.TESTDIV ";
            }
            $query .= "         AND SCORE{$key}.EXAMNO         = BASE.EXAMNO ";
            $query .= "         AND SCORE{$key}.TESTSUBCLASSCD = '{$key}' ";
        }
        $query .= "     LEFT JOIN NAME_MST NML055 ON NML055.NAMECD1 = 'L055' AND NML055.NAMECD2 = DETAIL005.REMARK1 ";
        $query .= "     LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006' AND NML006.NAMECD2 = BASE.SHDIV ";
        $query .= "     LEFT JOIN NAME_MST NMH201 ON NMH201.NAMECD1 = 'H201' AND NMH201.NAMECD2 = ADDR.RELATIONSHIP ";
        $query .= "     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = DETAIL005.REMARK2 ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     LEFT JOIN NAME_MST NML005 ON NML005.NAMECD1 = 'L005' AND NML005.NAMECD2 =  DETAIL011.REMARK" . $model->field["TESTDIV"];
        } else {
            $query .= "     LEFT JOIN NAME_MST NML005 ON NML005.NAMECD1 = 'L005' AND NML005.NAMECD2 = BASE.TESTDIV1 ";
        }
        $query .= "     LEFT JOIN NAME_MST NML032 ON NML032.NAMECD1 = 'L032' AND NML032.NAMECD2 = DETAIL004.REMARK9 ";
        $query .= "     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = BASE.JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_MST NML011 ON NML011.NAMECD1 = 'L011' AND NML011.NAMECD2 = BASE.PROCEDUREDIV ";
        $query .= "     LEFT JOIN NAME_MST NML012 ON NML012.NAMECD1 = 'L012' AND NML012.NAMECD2 = BASE.ENTDIV ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '" . $model->ObjYear . "' ";
        $query .= "     AND BASE.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     AND DETAIL010.REMARK" . $model->field["TESTDIV"] . " = '" . $model->field["TESTDIV"] . "' ";
        } else {
            $query .= "     AND BASE.TESTDIV = '" . $model->field["TESTDIV"] . "' ";
        }
        if ($model->field["EXCEPT_KESSEKI"] == "1") {
            $query .= "     AND VALUE(BASE.JUDGEMENT,'') != '4' ";
        }
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }
}
?>
