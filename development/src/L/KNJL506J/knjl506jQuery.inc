<?php

require_once('for_php7.php');

class knjl506jQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
    public function getTestDiv($year, $applicantDiv, $testDiv = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($testDiv) {
            $query .= " AND TESTDIV = '{$testDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試方式取得
    public function getExamType($year, $applicantDiv, $examType = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMTYPE_NAME, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL, ";
        $query .= "     EXAM_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($examType) {
            $query .= " AND EXAM_TYPE = '{$examType}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //入試種別取得（CSV用）
    public function getTestDivArray($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTDIV_ABBV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     TEST_DATE, ";
        $query .= "     TESTDIV ";

        return $query;
    }

    //入試科目取得（成績一覧表用）
    public function getTestSubclassArray2($year, $applicantDiv, $testDiv, $examType)
    {
        $query   = " WITH GROUPING_EXAMTYPE AS ( ";
        $query  .= "     SELECT ";
        $query  .= "       EXAM_TYPE ";
        $query  .= "     FROM ";
        $query  .= "       ENTEXAM_RECEPT_DAT ";
        $query  .= "     WHERE ";
        $query  .= "       ENTEXAMYEAR = '{$year}' ";
        $query  .= "       AND APPLICANTDIV = '{$applicantDiv}' ";
        $query  .= "       AND TESTDIV = '{$testDiv}' ";
        if ($examType != "") {
            $query  .= "       AND EXAM_TYPE = '{$examType}' ";
        }
        $query  .= "     GROUP BY ";
        $query  .= "       EXAM_TYPE ";
        $query  .= " ) ";
        $query  .= " , GROUPING_MERGE_EXAMTYPE_SUBCLASS_MST AS ( ";
        $query  .= "     SELECT ";
        $query  .= "       SUBCLASSCD, ";
        $query  .= "       SUM(CASE WHEN JUDGE_SUMMARY = '1' THEN 1 ELSE 0 END) AS CNTFLG ";
        $query  .= "     FROM ";
        $query  .= "       ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query  .= "     WHERE ";
        $query  .= "       ENTEXAMYEAR = '{$year}' ";
        $query  .= "       AND APPLICANTDIV = '{$applicantDiv}' ";
        $query  .= "       AND EXAM_TYPE IN (SELECT EXAM_TYPE FROM GROUPING_EXAMTYPE) ";
        $query  .= "     GROUP BY ";
        $query  .= "       SUBCLASSCD ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "   T1.SUBCLASSCD, ";
        $query  .= "   T2_L009.NAME1, ";
        $query  .= "   CASE WHEN T1.CNTFLG > 0 THEN 1 ELSE 0 END AS GRPFLG ";
        $query  .= " FROM ";
        $query  .= "   GROUPING_MERGE_EXAMTYPE_SUBCLASS_MST T1 ";
        $query  .= "   LEFT JOIN NAME_MST T2_L009 ";
        $query  .= "     ON T2_L009.NAMECD1 = 'L009' ";
        $query  .= "    AND T2_L009.NAMECD2 = T1.SUBCLASSCD ";
        $query  .= " ORDER BY ";
        $query  .= "   T1.SUBCLASSCD ";

        return $query;
    }

    public function selectMainQuery1($db, $model, $year, $applicantDiv, $testDiv, $examType)
    {
        $query   = " WITH GROUPING_MERGE_EXAMTYPE_SUBCLASS_MST AS ( ";
        $query  .= "     SELECT ";
        $query  .= "       SUBCLASSCD, ";
        $query  .= "       SUM(CASE WHEN JUDGE_SUMMARY = '1' THEN 1 ELSE 0 END) AS CNTFLG ";
        $query  .= "     FROM ";
        $query  .= "       ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query  .= "     WHERE ";
        $query  .= "       ENTEXAMYEAR = '{$year}' ";
        $query  .= "       AND APPLICANTDIV = '{$applicantDiv}' ";
        $query  .= "     GROUP BY ";
        $query  .= "       SUBCLASSCD ";
        $query  .= " ), ORDER_SUB AS ( ";
        $query  .= " SELECT ";
        $query  .= "   T1.SUBCLASSCD, ";
        $query  .= "   T2_L009.NAME1, ";
        $query  .= "   CASE WHEN T1.CNTFLG > 0 THEN 1 ELSE 0 END AS GRPFLG ";
        $query  .= " FROM ";
        $query  .= "   GROUPING_MERGE_EXAMTYPE_SUBCLASS_MST T1 ";
        $query  .= "   LEFT JOIN NAME_MST T2_L009 ";
        $query  .= "     ON T2_L009.NAMECD1 = 'L009' ";
        $query  .= "    AND T2_L009.NAMECD2 = T1.SUBCLASSCD ";
        $query  .= " ) ";

        $query  .= " SELECT ";
        $query  .= "     T13.SUBCLASSCD, ";
        $query  .= "     ORDER_S.NAME1 AS SUBNAME, ";
        $query  .= "     T1.RECEPTNO, ";
        $query  .= "     T3_009.REMARK1 AS ORDERNO, ";
        $query  .= "     T2.NAME, ";
        $query  .= "     T12.SCORE ";
        $query  .= " FROM ";
        $query  .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query  .= "  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query  .= "    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query  .= "   AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query  .= "   AND T2.EXAMNO = T1.EXAMNO ";
        $query  .= "  LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T3_009 ";
        $query  .= "    ON T3_009.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query  .= "   AND T3_009.APPLICANTDIV = T1.APPLICANTDIV ";
        $query  .= "   AND T3_009.TESTDIV = T1.TESTDIV ";
        $query  .= "   AND T3_009.EXAM_TYPE = T1.EXAM_TYPE ";
        $query  .= "   AND T3_009.RECEPTNO = T1.RECEPTNO ";
        $query  .= "   AND T3_009.SEQ = '009' ";
        $query  .= "  LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST T13";
        $query  .= "    ON T13.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query  .= "   AND T13.APPLICANTDIV = T1.APPLICANTDIV ";
        $query  .= "   AND T13.EXAM_TYPE = T1.EXAM_TYPE ";
        $query  .= "  LEFT JOIN ENTEXAM_SCORE_DAT T12 ";
        $query  .= "    ON T12.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query  .= "   AND T12.APPLICANTDIV = T1.APPLICANTDIV ";
        $query  .= "   AND T12.TESTDIV = T1.TESTDIV ";
        $query  .= "   AND T12.EXAM_TYPE = T1.EXAM_TYPE ";
        $query  .= "   AND T12.RECEPTNO = T1.RECEPTNO ";
        $query  .= "   AND T12.ENTEXAMYEAR = T13.ENTEXAMYEAR ";
        $query  .= "   AND T12.APPLICANTDIV = T13.APPLICANTDIV ";
        $query  .= "   AND T12.EXAM_TYPE = T13.EXAM_TYPE ";
        $query  .= "   AND T12.TESTSUBCLASSCD = T13.SUBCLASSCD ";
        $query .= "   LEFT JOIN ORDER_SUB ORDER_S ";
        $query .= "    ON ORDER_S.SUBCLASSCD = T13.SUBCLASSCD ";
        $query  .= " WHERE ";
        $query  .= "  T1.ENTEXAMYEAR = '{$year}' ";
        $query  .= "  AND T1.APPLICANTDIV = '{$applicantDiv}' ";
        $query  .= "  AND T1.TESTDIV = '{$testDiv}' ";
        if ($examType != "") {
            $query  .= "  AND T1.EXAM_TYPE = '{$examType}' ";
        }
        $query  .= " ORDER BY ";
        $query  .= "   ORDER_S.GRPFLG DESC, ";
        $query  .= "   T13.SUBCLASSCD, ";
        if ($model->field["SORT1"] == "1") {
            $query  .= "     T3_009.REMARK1, ";
        }
        $query  .= "     T1.RECEPTNO ";

        return $query;
    }

    //CSV出力
    public function selectMainQuery2($db, $model, $year, $applicantDiv, $testDiv, $examType)
    {
        $query  = " WITH SRCHBASE_TBL AS ( ";
        //検索データを定義する。
        $query .= " SELECT DISTINCT ";
        $query .= "  T1.ENTEXAMYEAR, ";
        $query .= "  T1.APPLICANTDIV, ";
        $query .= "  T1.TESTDIV, ";
        $query .= "  T2.RECOM_EXAMNO, ";
        $query .= "  T1.JUDGEDIV ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T2.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "  T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "  AND T1.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ), PASTTEST_TBL AS ( ";
        //選択した試験種別よりも過去の試験を抽出
        $query .= " SELECT DISTINCT ";
        $query .= "  T1.ENTEXAMYEAR, ";
        $query .= "  T1.APPLICANTDIV, ";
        $query .= "  T1.RECOM_EXAMNO ";
        $query .= " FROM ";
        $query .= "     SRCHBASE_TBL T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TESTDIV IN ( ";
        $query .= "         SELECT ";
        $query .= "             T4.TESTDIV ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_TESTDIV_MST T3 ";
        $query .= "             LEFT JOIN ENTEXAM_TESTDIV_MST T4 ";
        $query .= "                  ON T4.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "                 AND T4.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "                 AND T4.TESTDIV <> T3.TESTDIV ";
        $query .= "                 AND T4.TEST_DATE || '-' || T4.TESTDIV < T3.TEST_DATE || '-' || T3.TESTDIV ";
        $query .= "                 AND T4.INTERVIEW_DIV = T3.INTERVIEW_DIV ";
        $query .= "                 AND T4.INTERVIEW_DIV = '1' ";
        $query .= "         WHERE ";
        $query .= "             T3.ENTEXAMYEAR = '{$year}' ";
        $query .= "             AND T3.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "             AND T3.TESTDIV = '{$testDiv}' ";
        $query .= "     ) ";
        $query .= "     AND VALUE(T1.JUDGEDIV,'') <> '4' ";
        $query .= " ) ";
        // 以下、表示用データ。
        $query .= " SELECT ";
        $query .= "  T1.RECEPTNO, ";
        $query .= "  T3_009.REMARK1 AS ORDERNO, ";
        $query .= "  T11_L013.ABBV1 AS PASS_UNPASS, ";
        $query .= "  CASE WHEN T4.RECOM_EXAMNO IS NOT NULL OR TM_TEKI.INTERVIEW_DIV IS NULL THEN '' ELSE '面' END AS INTERVIEW, ";
        $query .= "  CASE WHEN T1.JUDGEDIV = '4' THEN '欠' ELSE '' END AS ATTEND, ";
        $query .= "  T9.EXAMTYPE_NAME_ABBV AS EXAMTYPE, ";

        $datcnt = 1;
        $result = $db->query(knjl506jQuery::getTestDivArray($year, $applicantDiv));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query .= "  CASE WHEN T5_00".$datcnt.".JUDGEDIV = '1' THEN '合'  ";
            $query .= "       WHEN T5_00".$datcnt.".JUDGEDIV = '2' THEN '×' ";
            $query .= "       WHEN T5_00".$datcnt.".JUDGEDIV = '4' THEN '欠' ";
            $query .= "       WHEN T5_00".$datcnt.".RECOM_EXAMNO IS NOT NULL THEN '〇' ";
            $query .= "       ELSE '' END AS KEKKA".$datcnt.", ";
            $datcnt++;
        }

        if ($applicantDiv == "2") {
            $query .= "     T6_003.REMARK1 AS NAITEI, ";
        }

        $query .= "  CASE WHEN T6_004.REMARK1 = '1' THEN 'A' END AS CHK_A, ";
        $query .= "  CASE WHEN T6_004.REMARK2 = '1' THEN 'F' END AS CHK_F, ";
        $query .= "  CASE WHEN T6_004.REMARK3 = '1' THEN 'T' END AS CHK_T, ";
        $query .= "  CASE WHEN T6_004.REMARK4 = '1' THEN 'B' END AS CHK_B, ";
        $query .= "  CASE WHEN T6_004.REMARK5 = '1' THEN 'J' END AS CHK_J, ";

        if ($applicantDiv == "2") {
            $query .= " T6.TOTAL_ALL AS NAISINTEN, ";
            $query .= " T6.ABSENCE_DAYS3 AS KESSEKI, ";
        }

        $query .= "  T2.NAME, ";
        $query .= "  T2.NAME_KANA, ";

        $result = $db->query(knjl506jQuery::getTestSubclassArray2($year, $applicantDiv, $testDiv, $examType));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getSubClsCd = $row["SUBCLASSCD"];
            $subClsTblName = "T12_".$getSubClsCd;
            $query .= "  ". $subClsTblName.".SCORE AS SCORE".$getSubClsCd.", ";
        }

        if ($applicantDiv == "1") {
            //小計(TOTAL2)
            $query .= " T1.TOTAL2, ";
            //小計順位(TOTAL_RANK2)
            $query .= " T1.TOTAL_RANK2, ";
        }
        //合計(TOTAL4)
        $query .= " T1.TOTAL4, ";
        //合計順位(TOTAL_RANK4)
        $query .= " T1.TOTAL_RANK4, ";

        $query .= "  T6.REMARK1 AS REMARK, ";
        $query .= "  T7.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "  LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T3_009 ";
        $query .= "    ON T3_009.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T3_009.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T3_009.TESTDIV = T1.TESTDIV ";
        $query .= "   AND T3_009.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "   AND T3_009.RECEPTNO = T1.RECEPTNO ";
        $query .= "   AND T3_009.SEQ = '009' ";
        $query .= "  LEFT JOIN ENTEXAM_TESTDIV_MST TM_TEKI ";
        $query .= "    ON TM_TEKI.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND TM_TEKI.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND TM_TEKI.TESTDIV = T1.TESTDIV ";
        $query .= "  LEFT JOIN PASTTEST_TBL T4 ";
        $query .= "    ON T4.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "   AND T4.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "   AND T4.RECOM_EXAMNO = T2.RECOM_EXAMNO ";

        $srchdatcnt = 1;
        $result = $db->query(knjl506jQuery::getTestDivArray($year, $applicantDiv));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query .= "  LEFT JOIN SRCHBASE_TBL T5_00".$srchdatcnt." ";
            $query .= "    ON T5_00".$srchdatcnt.".ENTEXAMYEAR   = T2.ENTEXAMYEAR ";
            $query .= "   AND T5_00".$srchdatcnt.".APPLICANTDIV  = T2.APPLICANTDIV ";
            $query .= "   AND T5_00".$srchdatcnt.".TESTDIV       = '".$row["TESTDIV"]."' ";
            $query .= "   AND T5_00".$srchdatcnt.".RECOM_EXAMNO  = T2.RECOM_EXAMNO ";
            $srchdatcnt++;
        }

        $query .= "  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T6_004 ";
        $query .= "    ON T6_004.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "   AND T6_004.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "   AND T6_004.EXAMNO = T2.EXAMNO ";
        $query .= "   AND T6_004.SEQ = '004' ";
        $query .= "  LEFT JOIN FINSCHOOL_MST T7 ";
        $query .= "    ON T7.FINSCHOOLCD = T2.FS_CD ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T6 ";
        $query .= "    ON T6.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "   AND T6.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "   AND T6.EXAMNO = T2.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T6_003 ";
        $query .= "    ON T6_003.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "   AND T6_003.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "   AND T6_003.EXAMNO = T2.EXAMNO ";
        $query .= "   AND T6_003.SEQ = '003' ";
        $query .= "  LEFT JOIN ENTEXAM_EXAMTYPE_MST T9 ";
        $query .= "    ON T9.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T9.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T9.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "  LEFT JOIN NAME_MST T11_L013 ";
        $query .= "    ON T11_L013.NAMECD1 = 'L013' ";
        $query .= "   AND T11_L013.NAMECD2 = T1.JUDGEDIV ";

        $result = $db->query(knjl506jQuery::getTestSubclassArray2($year, $applicantDiv, $testDiv, $examType));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getSubClsCd = $row["SUBCLASSCD"];
            $subClsTblName = "T12_".$getSubClsCd;
            $query .= "  LEFT JOIN ENTEXAM_SCORE_DAT ".$subClsTblName." ";
            $query .= "    ON ".$subClsTblName.".ENTEXAMYEAR     = T1.ENTEXAMYEAR ";
            $query .= "   AND ".$subClsTblName.".APPLICANTDIV    = T1.APPLICANTDIV ";
            $query .= "   AND ".$subClsTblName.".TESTDIV         = T1.TESTDIV ";
            $query .= "   AND ".$subClsTblName.".EXAM_TYPE       = T1.EXAM_TYPE ";
            $query .= "   AND ".$subClsTblName.".RECEPTNO        = T1.RECEPTNO ";
            $query .= "   AND ".$subClsTblName.".TESTSUBCLASSCD  = '".$getSubClsCd."' ";
        }

        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND T1.TESTDIV = '{$testDiv}' ";
        if ($examType != "") {
            $query .= "     AND T1.EXAM_TYPE = '{$examType}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["SORT2"] == "1") {
            if ($model->field["SUBSORT"] == "1") {
                $query .= " VALUE(TOTAL2,0) DESC, ";
            } else {
                $query .= " VALUE(TOTAL4,0) DESC, ";
            }
        }
        $query .= " RECEPTNO ";

        return $query;
    }
}
