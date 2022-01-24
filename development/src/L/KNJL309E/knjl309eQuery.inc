<?php

require_once('for_php7.php');

class knjl309eQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $nspare = "") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($nspare != "") {
            $query .= " AND NAMESPARE1 = '".$nspare."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得2
    function getNameMstNamecd2($model, $namecd1, $namecd2) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    //名称マスタ取得3
    function getNameMstNamecd3($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ取得
    function selectCsvQuery($model) {
        //JavaとSQLは合わせている。一部、CSVで必要となる処理を入れている(NO_の算出、会場名の取得等)ので注意。
        //5教科合計3年分を取得
        $query   = " WITH SUM_SUBCLSSCORE AS ( ";
        $query  .= " SELECT ";
        $query  .= "  T1.ENTEXAMYEAR, ";
        $query  .= "  T1.APPLICANTDIV, ";
        $query  .= "  T1.EXAMNO, ";
        $query  .= "  VALUE(INT(S001.REMARK10), 0) AS RPT01, ";
        $query  .= "  VALUE(INT(S002.REMARK10), 0) AS RPT02, ";
        $query  .= "  VALUE(INT(S003.TOTAL5), 0) AS RPT03 ";
        $query  .= " FROM ";
        $query  .= " ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query  .= " LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT S001 ";
        $query  .= "   ON S001.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query  .= "  AND S001.APPLICANTDIV = T1.APPLICANTDIV ";
        $query  .= "  AND S001.EXAMNO = T1.EXAMNO ";
        $query  .= "  AND S001.SEQ = '001' ";
        $query  .= " LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT S002 ";
        $query  .= "   ON S002.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query  .= "  AND S002.APPLICANTDIV = T1.APPLICANTDIV ";
        $query  .= "  AND S002.EXAMNO = T1.EXAMNO ";
        $query  .= "  AND S002.SEQ = '002' ";
        $query  .= " LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT S003 ";
        $query  .= "   ON S003.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query  .= "  AND S003.APPLICANTDIV = T1.APPLICANTDIV ";
        $query  .= "  AND S003.EXAMNO = T1.EXAMNO ";
        //5教科の平均を算出
        $query  .= " ), AVERAGE_TOTAL5 AS ( ";
        $query  .= " SELECT ";
        $query  .= "   S_DAT.ENTEXAMYEAR, ";
        $query  .= "   S_DAT.APPLICANTDIV, ";
        $query  .= "   S_DAT.EXAMNO, ";
        $query  .= "   ( (S_DAT.RPT01 + S_DAT.RPT02 + S_DAT.RPT03) / (3.0 * 5.0) ) AS AVERAGE5";
        $query  .= " FROM ";
        $query  .= "   SUM_SUBCLSSCORE S_DAT ";
        $query  .= " ) ";
        //データ取得メイン
        $query .= " SELECT ";
        $query .= "   H2.EXAMHALL_NAME, ";
        if ($model->perhallcdprint == "1") {
            $query .= "     ROW_NUMBER() OVER (PARTITION BY H1.EXAMHALLCD ORDER BY H1.EXAMHALLCD,H1.EXAMHALLGROUPCD,INT(H1.EXAMHALLGROUP_ORDER) ASC,T1.EXAMNO ASC) AS NO_, ";
        } else {
            $query .= "     ROW_NUMBER() OVER (ORDER BY H1.EXAMHALLCD,H1.EXAMHALLGROUPCD,INT(H1.EXAMHALLGROUP_ORDER) ASC,T1.EXAMNO ASC) AS NO_, ";
        }
        $query .= "   T1.EXAMNO, ";
        //SEQ002.REMARK1はデバッグ用。同一人物の特定するために利用。
        //$query .= "   SEQ002.REMARK1, ";
        $query .= "   T1.NAME, ";
        $query .= "   T1.NAME_KANA, ";
        $query .= "   T1.BIRTHDAY, ";
        $query .= "   FM1.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME, ";
        $query .= "   ROUND(ATOTAL.AVERAGE5, 1) AS DEVI, ";
        $query .= "   CASE WHEN BD005.REMARK6 = '1' THEN '有' ELSE '' END AS REUNION, ";
        $query .= "   L058.NAME1 AS COURSE, ";
        $query .= "   CASE WHEN VALUE(L004_2.NAMESPARE3, '') = '2' THEN L061.ABBV1 END AS HEIGAN_ABBV, ";
        $query .= "   L045.NAME1 AS TDIVNAME, ";
        $query .= "   H1.EXAMHALLGROUPCD, ";
        if ($model->testscoreprint == "1") {
            $query .= "     L027_1.NAME1 AS INTERVIEW1, ";
            $query .= "     L027_2.NAME1 AS INTERVIEW2, ";
        } else {
            $query .= "     '' AS INTERVIEW1, ";
            $query .= "     '' AS INTERVIEW2, ";
        }
        $query .= "   CD003.REMARK1 AS CIRCLECNT, ";
        $query .= "   (CASE WHEN N3.ABSENCE_DAYS3 >= 16 THEN CHAR(N3.ABSENCE_DAYS3) ELSE '―' END) AS ABSENCE_DAYS, ";
        $query .= "   S1_1.SCORE AS SCORE1, ";
        $query .= "   S1_2.SCORE AS SCORE2, ";
        $query .= "   S1_3.SCORE AS SCORE3, ";
        $query .= "   S2.TOTAL4, ";
        $query .= "   S2.TOTAL_RANK4, ";
        $query .= "   N3.REMARK1 AS REMARK ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_HALL_GROUP_DAT H1 ";
        $query .= "      ON H1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND H1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND H1.TESTDIV = T1.TESTDIV ";
        $query .= "     AND H1.EXAMNO = T1.EXAMNO ";
        $query .= "     AND H1.EXAMHALL_TYPE = '1' ";
        $query .= "    LEFT JOIN ENTEXAM_HALL_YDAT H2 ";
        $query .= "      ON H2.ENTEXAMYEAR = H1.ENTEXAMYEAR ";
        $query .= "     AND H2.APPLICANTDIV = H1.APPLICANTDIV ";
        $query .= "     AND H2.TESTDIV = H1.TESTDIV ";
        $query .= "     AND H2.EXAM_TYPE = H1.EXAMHALL_TYPE ";
        $query .= "     AND H2.EXAMHALLCD = H1.EXAMHALLCD ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DAT S2 ";
        $query .= "     ON S2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND S2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND S2.TESTDIV = T1.TESTDIV ";
        $query .= "    AND S2.EXAMNO = T1.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_DAT S1_1 ";
        $query .= "     ON S1_1.ENTEXAMYEAR = S2.ENTEXAMYEAR ";
        $query .= "    AND S1_1.APPLICANTDIV = S2.APPLICANTDIV ";
        $query .= "    AND S1_1.TESTDIV = S2.TESTDIV ";
        $query .= "    AND S1_1.EXAM_TYPE = S2.EXAM_TYPE ";
        $query .= "    AND S1_1.RECEPTNO = S2.RECEPTNO ";
        $query .= "    AND S1_1.TESTSUBCLASSCD = '1' ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_DAT S1_2 ";
        $query .= "     ON S1_2.ENTEXAMYEAR = S2.ENTEXAMYEAR ";
        $query .= "    AND S1_2.APPLICANTDIV = S2.APPLICANTDIV ";
        $query .= "    AND S1_2.TESTDIV = S2.TESTDIV ";
        $query .= "    AND S1_2.EXAM_TYPE = S2.EXAM_TYPE ";
        $query .= "    AND S1_2.RECEPTNO = S2.RECEPTNO ";
        $query .= "    AND S1_2.TESTSUBCLASSCD = '2' ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_DAT S1_3 ";
        $query .= "     ON S1_3.ENTEXAMYEAR = S2.ENTEXAMYEAR ";
        $query .= "    AND S1_3.APPLICANTDIV = S2.APPLICANTDIV ";
        $query .= "    AND S1_3.TESTDIV = S2.TESTDIV ";
        $query .= "    AND S1_3.EXAM_TYPE = S2.EXAM_TYPE ";
        $query .= "    AND S1_3.RECEPTNO = S2.RECEPTNO ";
        $query .= "    AND S1_3.TESTSUBCLASSCD = '3' ";
        $query .= "   LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ";
        $query .= "     ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND M1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND M1.TESTDIV = T1.TESTDIV ";
        $query .= "    AND M1.EXAMNO = T1.EXAMNO ";
        $query .= "   LEFT JOIN FINSCHOOL_MST FM1 ";
        $query .= "     ON T1.FS_CD = FM1.FINSCHOOLCD ";
        $query .= "   LEFT JOIN NAME_MST L045 ";
        $query .= "     ON L045.NAMECD1 = 'L045' ";
        $query .= "    AND L045.NAMECD2 = T1.TESTDIV1 ";
        $query .= "   LEFT JOIN NAME_MST L027_1 ";
        $query .= "     ON L027_1.NAMECD1 = 'L027' ";
        $query .= "    AND L027_1.NAMECD2 = M1.INTERVIEW_A ";
        $query .= "   LEFT JOIN NAME_MST L027_2 ";
        $query .= "     ON L027_2.NAMECD1 = 'L027' ";
        $query .= "    AND L027_2.NAMECD2 = M1.INTERVIEW_B ";
        $query .= "   LEFT JOIN NAME_MST L058 ";
        $query .= "     ON L058.NAMECD1 = 'L058' ";
        $query .= "    AND L058.NAMECD2 = T1.DESIREDIV ";
        $query .= "   INNER JOIN V_NAME_MST L004 ON L004.YEAR       = T1.ENTEXAMYEAR ";
        $query .= "                              AND L004.NAMECD1    = 'L004' ";
        $query .= "                              AND L004.NAMECD2    = T1.TESTDIV ";
        $query .= "                              AND L004.NAMESPARE1 = '".$model->testdiv."' ";
        $query .= "   LEFT JOIN V_NAME_MST L004_2 ";
        $query .= "     ON L004_2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND L004_2.NAMECD1 = 'L004' ";
        $query .= "    AND L004_2.NAMECD2 = T1.TESTDIV ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD33 ";
        $query .= "     ON BD33.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND BD33.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND BD33.EXAMNO = T1.EXAMNO ";
        $query .= "    AND BD33.SEQ = '033' ";
        $query .= "   LEFT JOIN V_NAME_MST L061 ";
        $query .= "     ON L061.YEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND L061.NAMECD1 = 'L061' ";
        $query .= "    AND L061.NAMECD2 = BD33.REMARK3 ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "     ON BD005.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "    AND BD005.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND BD005.EXAMNO       = T1.EXAMNO ";
        $query .= "    AND BD005.SEQ          = '005' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT N3 ";
        $query .= "     ON N3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "    AND N3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND N3.EXAMNO       = T1.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ002 ";
        $query .= "     ON SEQ002.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "    AND SEQ002.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND SEQ002.EXAMNO       = T1.EXAMNO ";
        $query .= "    AND SEQ002.SEQ          = '002' ";
        $query .= "   LEFT JOIN AVERAGE_TOTAL5 ATOTAL ";
        $query .= "     ON ATOTAL.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND ATOTAL.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND ATOTAL.EXAMNO = T1.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD003 ";
        $query .= "     ON CD003.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "    AND CD003.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND CD003.EXAMNO       = T1.EXAMNO ";
        $query .= "    AND CD003.SEQ          = '003' ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '".$model->examyear."'   AND ";
        $query .= "   T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     H1.EXAMHALLCD, ";
        $query .= "     H1.EXAMHALLGROUPCD, ";
        $query .= "     INT(H1.EXAMHALLGROUP_ORDER), ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
