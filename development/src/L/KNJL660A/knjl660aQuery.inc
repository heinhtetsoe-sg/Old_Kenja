<?php

require_once('for_php7.php');

class knjl660aQuery extends Query
{
    //名称マスタ取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象人数チェック
    public function checkRecCnt($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE";
        $query .= "     ENTEXAMYEAR     = '".$model->examyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' ";

        return $query;
    }

    //得点データチェック
    public function checkScoCnt($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->examyear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' AND ";
        $query .= "     EXISTS( SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 V_NAME_MST N1 ";
        $query .= "             WHERE ";
        $query .= "                 N1.YEAR     = T1.ENTEXAMYEAR AND ";
        $query .= "                 N1.NAMECD1  = 'L009' AND ";
        $query .= "                 N1.NAMECD2  = T1.TESTSUBCLASSCD AND ";
        $query .= "                 CASE WHEN T1.APPLICANTDIV = '2' THEN (N1.NAME1 IS NOT NULL AND N1.NAMECD2 <> 'A') ";
        $query .= "                      WHEN T1.APPLICANTDIV = '1' THEN (N1.NAME2 IS NOT NULL AND (CASE WHEN T1.TESTDIV IN ('1','2') THEN VALUE(N1.NAMESPARE3,'') <> '1' ";
        $query .= "                                                                                      ELSE N1.NAMESPARE3 = '1' END)) END ";
        $query .= "             ) ";

        return $query;
    }

    //算出
    public function ExecuteQuery($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //エラー無視
        $db->expectError();

        // 全科目分受験フラグ、合計、平均
        $query  = " UPDATE ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " SET ";
        $query .= "    (ATTEND_ALL_FLG, ";
        $query .= "     TOTAL4, ";
        $query .= "     AVARAGE4, ";
        $query .= "     TOTAL1, ";
        $query .= "     AVARAGE1, ";
        $query .= "     TOTAL3, ";
        $query .= "     AVARAGE3, ";
        $query .= "     TOTAL2, ";
        $query .= "     AVARAGE2, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED) ";
        $query .= "     = ( SELECT ";
        $query .= "             CASE WHEN SUM(CASE WHEN T1.TARGET1 = '1' AND L1.SCORE2 IS NULL THEN 1 ELSE 0 END) > 0 THEN 0 ELSE 1 END AS ATTEND_ALL_FLG, ";
        $query .= "             SUM(CASE WHEN T1.TARGET1 = '1' THEN L1.SCORE2 END) AS TOTAL4, ";
        $query .= "             ROUND(AVG(DECIMAL(CASE WHEN T1.TARGET1 = '1' THEN L1.SCORE2 END)),2) AS AVERAGE4, ";
        $query .= "             SUM(CASE WHEN T1.TARGET2 = '1' THEN L1.SCORE2 END) AS TOTAL1, ";
        $query .= "             ROUND(AVG(DECIMAL(CASE WHEN T1.TARGET2 = '1' THEN L1.SCORE2 END)),2) AS AVERAGE1, ";
        $query .= "             SUM(CASE WHEN T1.TARGET1 = '1' THEN L1.SCORE END) AS TOTAL3, ";
        $query .= "             ROUND(AVG(DECIMAL(CASE WHEN T1.TARGET1 = '1' THEN L1.SCORE END)),2) AS AVERAGE3, ";
        $query .= "             SUM(CASE WHEN T1.TARGET2 = '1' THEN L1.SCORE END) AS TOTAL2, ";
        $query .= "             ROUND(AVG(DECIMAL(CASE WHEN T1.TARGET2 = '1' THEN L1.SCORE END)),2) AS AVERAGE2, ";
        $query .= "             '".STAFFCD."', ";
        $query .= "             SYSDATE() ";
        $query .= "         FROM ";
        $query .= "            (SELECT DISTINCT ";
        $query .= "                 T1.RECEPTNO, ";
        $query .= "                 T2.NAMECD2 AS TESTSUBCLASSCD, ";
        $query .= "                 CASE WHEN L1.PERFECT IS NOT NULL THEN '1' END AS TARGET1, ";
        $query .= "                 CASE WHEN L2.PERFECT IS NOT NULL THEN '1' END AS TARGET2 ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_RECEPT_DETAIL_DAT T1, ";
        $query .= "                 V_NAME_MST T2 ";
        $query .= "                 LEFT JOIN ENTEXAM_PERFECT_MST L1 ";
        $query .= "                      ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "                     AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "                     AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "                     AND L1.COURSECD || L1.MAJORCD = '0000' ";
        $query .= "                     AND L1.EXAMCOURSECD     = T1.REMARK1 ";
        $query .= "                     AND L1.TESTSUBCLASSCD   = T2.NAMECD2 ";
        $query .= "                 LEFT JOIN ENTEXAM_PERFECT_MST L2 ";
        $query .= "                      ON L2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "                     AND L2.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "                     AND L2.TESTDIV          = T1.TESTDIV ";
        $query .= "                     AND L2.COURSECD || L2.MAJORCD = '0000' ";
        $query .= "                     AND L2.EXAMCOURSECD     = T1.REMARK2 ";
        $query .= "                     AND L2.TESTSUBCLASSCD   = T2.NAMECD2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR  = '".$model->examyear."' AND ";
        $query .= "                 T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "                 T1.TESTDIV      = '".$model->testdiv."' AND ";
        $query .= "                 T1.EXAM_TYPE    = '1' AND ";
        $query .= "                 T1.SEQ          = '007' AND ";
        $query .= "                 T2.NAMECD1      = 'L009' AND ";
        $query .= "                 T1.ENTEXAMYEAR  = T2.YEAR ";
        if ($model->applicantdiv == '2') {
            $query .= "             AND T2.NAMECD2 <> 'A' ";
        }
        $query .= "             ) T1 ";
        $query .= "             LEFT JOIN ENTEXAM_SCORE_DAT L1 ";
        $query .= "                  ON L1.ENTEXAMYEAR      = '".$model->examyear."' ";
        $query .= "                 AND L1.APPLICANTDIV     = '".$model->applicantdiv."' ";
        $query .= "                 AND L1.TESTDIV          = '".$model->testdiv."' ";
        $query .= "                 AND L1.EXAM_TYPE        = '1' ";
        $query .= "                 AND T1.RECEPTNO         = L1.RECEPTNO ";
        $query .= "                 AND T1.TESTSUBCLASSCD   = L1.TESTSUBCLASSCD ";
        $query .= "         WHERE ";
        $query .= "             T1.RECEPTNO = ENTEXAM_RECEPT_DAT.RECEPTNO AND ";
        $query .= "            (T1.TARGET1 IS NOT NULL OR T1.TARGET2 IS NOT NULL) ";
        $query .= "         GROUP BY ";
        $query .= "             T1.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->examyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '1' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【合計での順位】--------------------
        // 席次(合計)
        $query  = " UPDATE ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " SET ";
        $query .= "    (TOTAL_RANK4, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED) ";
        $query .= "     = ( SELECT ";
        $query .= "             TT4.TOTAL_RANK4, ";
        $query .= "             '".STAFFCD."', ";
        $query .= "             SYSDATE() ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "             LEFT JOIN ( SELECT ";
        $query .= "                             T1.RECEPTNO, ";
        $query .= "                             RANK() OVER(PARTITION BY T1.TESTDIV ORDER BY T1.TOTAL4 DESC) AS TOTAL_RANK4 ";
        $query .= "                         FROM ";
        $query .= "                             ENTEXAM_RECEPT_DAT T1 ";
        $query .= "                         WHERE ";
        $query .= "                             T1.ENTEXAMYEAR  = '".$model->examyear."' AND ";
        $query .= "                             T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "                             T1.TESTDIV      = '".$model->testdiv."' AND ";
        $query .= "                             T1.EXAM_TYPE    = '1' AND ";
        $query .= "                             T1.TOTAL4 IS NOT NULL ";
        $query .= "                         ) TT4  ON TT0.RECEPTNO  = TT4.RECEPTNO ";
        $query .= "         WHERE ";
        $query .= "             TT0.ENTEXAMYEAR     = '".$model->examyear."' AND ";
        $query .= "             TT0.APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "             TT0.TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "             TT0.ATTEND_ALL_FLG  = '1' AND ";
        $query .= "             TT0.RECEPTNO        = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->examyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."'AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '1' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //正常
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    }
}
