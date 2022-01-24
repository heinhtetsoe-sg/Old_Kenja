<?php

require_once('for_php7.php');

class knjl302eQuery extends Query {
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

    function selectCsvQuery($model) {

        $query  = " SELECT ";
        $query .= "  ROW_NUMBER() OVER (ORDER BY H1.EXAMHALLCD) AS NO_, "; //行番号※CSV用
        $query .= "  T1.EXAMNO, ";
        $query .= "  H2.EXAMHALL_NAME, ";
        $query .= "  T1.NAME, ";
        $query .= "  T1.NAME_KANA, ";
        $query .= "  T1.BIRTHDAY, ";
        $query .= "  FM.FINSCHOOL_NAME, ";
        $query .= "  CASE WHEN T3.TOTAL5 IS NULL AND T3_001.REMARK10 IS NULL AND T3_002.REMARK10 IS NULL THEN NULL ";
        $query .= "       ELSE ROUND((VALUE(T3.TOTAL5, 0) + INT(VALUE(T3_001.REMARK10, '0')) + INT(VALUE(T3_002.REMARK10, '0'))) / 15.0 , 1) END AS CONF_AVG5, ";
        $query .= "  L045.NAME1 AS TDIVNAME, ";
        $query .= "  (CASE WHEN T1.SHDIV = '2' THEN '★' ELSE '' END) AS SHDIV, ";
        $query .= "  L058.NAME1 AS DDIVNAME, ";
        $query .= "  T2.REMARK10 AS QUALIFYDETAIL, ";
        $query .= "  CASE WHEN T2.REMARK2 = '1' THEN '実績' WHEN T2.REMARK2 = '2' THEN '推薦' ELSE '' END AS QUALIFY, ";
        $query .= "  CASE WHEN T2.REMARK3 = '1' THEN '―' WHEN T2.REMARK3 = '2' THEN '有り' WHEN T2.REMARK3 = '3' THEN '無し' ELSE '' END AS SKILL, ";
        $query .= "  CASE WHEN T2.REMARK4 = '1' THEN '―' WHEN T2.REMARK4 = '2' THEN '可' WHEN T2.REMARK4 = '3' THEN '否' ELSE '' END AS JUDGE, ";
        $query .= "  H1.EXAMHALLGROUPCD, ";
        if ($model->testscoreprint == "1") {
            $query .= "  COALESCE(L027_1.NAME1,'') AS INTERVIEW1, ";
            $query .= "  COALESCE(L027_2.NAME1,'') AS INTERVIEW2, ";
        } else {
            $query .= "  '' AS INTERVIEW1, ";
            $query .= "  '' AS INTERVIEW2, ";
        }
        $query .= "  T4.REMARK1 AS CIRCLECNT, ";
        $query .= "  (CASE WHEN T3.ABSENCE_DAYS3 >= 16 THEN CHAR(T3.ABSENCE_DAYS3) ELSE '―' END) AS ABSENCE_DAYS, ";
        $query .= "  T3.REMARK1 AS REMARK ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "   AND T2.SEQ = '034' ";
        $query .= "  LEFT JOIN ENTEXAM_HALL_GROUP_DAT H1 ";
        $query .= "    ON H1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND H1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND H1.TESTDIV = T1.TESTDIV ";
        $query .= "   AND H1.EXAMNO = T1.EXAMNO ";
        $query .= "   AND H1.EXAMHALL_TYPE = '1' ";
        $query .= "  LEFT JOIN ENTEXAM_HALL_YDAT H2 ";
        $query .= "    ON H2.ENTEXAMYEAR = H1.ENTEXAMYEAR ";
        $query .= "   AND H2.APPLICANTDIV = H1.APPLICANTDIV ";
        $query .= "   AND H2.TESTDIV = H1.TESTDIV ";
        $query .= "   AND H2.EXAM_TYPE = H1.EXAMHALL_TYPE ";
        $query .= "   AND H2.EXAMHALLCD = H1.EXAMHALLCD ";
        $query .= "  LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ";
        $query .= "    ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND M1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND M1.TESTDIV = T1.TESTDIV ";
        $query .= "   AND M1.EXAMNO = T1.EXAMNO ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ";
        $query .= "    ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_001 ";
        $query .= "    ON T3_001.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "   AND T3_001.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "   AND T3_001.EXAMNO = T3.EXAMNO ";
        $query .= "   AND T3_001.SEQ = '001' ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_002 ";
        $query .= "    ON T3_002.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "   AND T3_002.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "   AND T3_002.EXAMNO = T3.EXAMNO ";
        $query .= "   AND T3_002.SEQ = '002' ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4 ";
        $query .= "    ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T4.EXAMNO = T1.EXAMNO ";
        $query .= "   AND T4.SEQ = '003' ";
        $query .= "  LEFT JOIN NAME_MST L045 ";
        $query .= "    ON L045.NAMECD1 = 'L045' ";
        $query .= "   AND L045.NAMECD2 = T1.TESTDIV1 ";
        $query .= "  LEFT JOIN NAME_MST L058 ";
        $query .= "    ON L058.NAMECD1 = 'L058' ";
        $query .= "   AND L058.NAMECD2 = T1.DESIREDIV ";
        $query .= "  LEFT JOIN NAME_MST L027_1 ";
        $query .= "    ON L027_1.NAMECD1 = 'L027' ";
        $query .= "   AND L027_1.NAMECD2 = M1.INTERVIEW_A ";
        $query .= "  LEFT JOIN NAME_MST L027_2 ";
        $query .= "    ON L027_2.NAMECD1 = 'L027' ";
        $query .= "   AND L027_2.NAMECD2 = M1.INTERVIEW_B ";
        $query .= "  LEFT JOIN FINSCHOOL_MST FM ";
        $query .= "    ON FM.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '" . $model->examyear . "' ";
        $query .= "   AND T1.APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query .= "   AND T1.TESTDIV = '" . $model->testdiv . "' ";
        $query .= " ORDER BY ";
        $query .= "  H1.EXAMHALLCD, ";
        $query .= "  H1.EXAMHALLGROUPCD, ";
        $query .= "  INT(H1.EXAMHALLGROUP_ORDER), ";
        $query .= "  T1.EXAMNO ";

        return $query;
    }
}
?>
