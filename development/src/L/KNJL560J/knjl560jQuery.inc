<?php

require_once('for_php7.php');

class knjl560jQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試種別取得
    function getTestDiv($year, $applicantDiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象人数チェック
    function checkRecCnt($model) {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND T1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "   AND T1.TESTDIV      = '".$model->testdiv."'";
        return $query;
    }

    //得点データチェック
    function checkScoCnt($model) {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND T1.APPLICANTDIV = '".$model->applicantdiv."'"; 
        $query .= "   AND T1.TESTDIV      = '".$model->testdiv."'";
        return $query;
    }

    //算出
    function ExecuteQuery($model) {
        //参照データ（得点）

        //DB接続
        $db = Query::dbCheckOut();

        //受験科目
        $testsubArray = array();
        $query = knjl560jQuery::getNameMst($model->examyear, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubArray[] = $row["VALUE"];
        }
        $result->free();

        $cnt = get_count($testsubArray);

        //エラー無視
        $db->expectError();

        /* 全科目分受験フラグ、合計、平均 */ 
        $query  = "UPDATE ENTEXAM_RECEPT_DAT ";
        $query .= "   SET (EXAMNO, ";
        $query .= "        ATTEND_ALL_FLG, ";
        $query .= "        TOTAL4, ";
        $query .= "        AVARAGE4, ";
        $query .= "        TOTAL2, ";
        $query .= "        AVARAGE2, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT ";
        $query .= "      TT0.EXAMNO, ";
        $query .= "      TT4.ATTEND_ALL_FLG4, ";
        $query .= "      TT4.TOTAL4, ";
        $query .= "      TT4.AVG4, ";
        $query .= "      TT2.TOTAL2, ";
        $query .= "      TT2.AVG2, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT S1.RECEPTNO ";
        $query .= "               ,S1.EXAM_TYPE ";
        $query .= "               ,CASE WHEN COUNT(S1.SCORE) >= COUNT(E1.SUBCLASSCD) THEN '1' ELSE '0' END AS ATTEND_ALL_FLG4 ";
        $query .= "               ,SUM(S1.SCORE) AS TOTAL4 ";
        $query .= "               ,ROUND(AVG(DECIMAL(S1.SCORE)),1) AS AVG4 ";
        $query .= "           FROM ENTEXAM_SCORE_DAT S1 ";
        $query .= "                INNER JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST E1 ";// 入試方式マスタ
        $query .= "                     ON E1.ENTEXAMYEAR   = S1.ENTEXAMYEAR ";
        $query .= "                    AND E1.APPLICANTDIV  = S1.APPLICANTDIV ";
        $query .= "                    AND E1.EXAM_TYPE     = S1.EXAM_TYPE ";
        $query .= "                    AND E1.SUBCLASSCD    = S1.TESTSUBCLASSCD ";
        $query .= "          WHERE S1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "            AND S1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "            AND S1.TESTDIV      = '".$model->testdiv."'";
        $query .= "         GROUP BY ";
        $query .= "                S1.RECEPTNO ";
        $query .= "               ,S1.EXAM_TYPE";
        $query .= "       ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT4.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT S1.RECEPTNO ";
        $query .= "               ,S1.EXAM_TYPE ";
        $query .= "               ,CASE WHEN COUNT(S1.SCORE) >= COUNT(E1.SUBCLASSCD) THEN '1' ELSE '0' END AS ATTEND_ALL_FLG2 ";
        $query .= "               ,SUM(S1.SCORE) AS TOTAL2 ";
        $query .= "               ,ROUND(AVG(DECIMAL(S1.SCORE)),1) AS AVG2 ";
        $query .= "           FROM ENTEXAM_SCORE_DAT S1 ";
        $query .= "                INNER JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST E1 ";// 入試方式マスタ
        $query .= "                     ON E1.ENTEXAMYEAR   = S1.ENTEXAMYEAR ";
        $query .= "                    AND E1.APPLICANTDIV  = S1.APPLICANTDIV ";
        $query .= "                    AND E1.EXAM_TYPE     = S1.EXAM_TYPE ";
        $query .= "                    AND E1.SUBCLASSCD    = S1.TESTSUBCLASSCD ";
        $query .= "                    AND E1.JUDGE_SUMMARY = '1' ";
        $query .= "          WHERE S1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "            AND S1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "            AND S1.TESTDIV      = '".$model->testdiv."'";
        $query .= "         GROUP BY ";
        $query .= "                S1.RECEPTNO ";
        $query .= "               ,S1.EXAM_TYPE";
        $query .= "       ) TT2 ON TT2.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT2.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "   AND TT0.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.TESTDIV      = '".$model->testdiv."' ";
        $query .= "   AND TT0.EXAM_TYPE    = ENTEXAM_RECEPT_DAT.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "   AND TESTDIV      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        //--------------------【合計での順位】--------------------

        /* 席次(合計) */
        $query  = "UPDATE ENTEXAM_RECEPT_DAT ";
        $query .= "   SET (EXAMNO, ";
        $query .= "        TOTAL_RANK4, ";
        $query .= "        TOTAL_RANK2, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.EXAMNO, ";
        $query .= "       TT4.TOTAL_RANK4, ";
        $query .= "       TT2.TOTAL_RANK2, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.TOTAL4 DESC) AS TOTAL_RANK4 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          where R1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "            AND R1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "            AND R1.TESTDIV      = '".$model->testdiv."'";
        $query .= "            AND R1.TOTAL4 IS NOT NULL ";
        $query .= "       ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT4.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.TOTAL2 DESC) AS TOTAL_RANK2 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          where R1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "            AND R1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "            AND R1.TESTDIV      = '".$model->testdiv."'";
        $query .= "            AND R1.TOTAL2 IS NOT NULL ";
        $query .= "       ) TT2 ON TT2.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT2.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "   AND TT0.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.TESTDIV      = '".$model->testdiv."' ";
        $query .= "   AND TT0.EXAM_TYPE    = ENTEXAM_RECEPT_DAT.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "   AND TESTDIV      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【欠席】--------------------

        /* 欠席(一旦クリア) */
        $query  = " UPDATE ENTEXAM_RECEPT_DAT  ";
        $query .= "    SET JUDGEDIV = NULL  ";
        $query .= "  WHERE ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "    AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "    AND TESTDIV      = '".$model->testdiv."'  ";
        $query .= "    AND JUDGEDIV     = '4'  ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " UPDATE ENTEXAM_APPLICANTBASE_DAT  ";
        $query .= "    SET JUDGEMENT = NULL  ";
        $query .= "  WHERE ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "    AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "    AND JUDGEMENT    = '4'  ";
        $query .= "    AND EXAMNO IN ( ";
        $query .= "         SELECT TT0.EXAMNO  ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT TT0  ";
        $query .= "          WHERE TT0.ENTEXAMYEAR  = '".$model->examyear."'  ";
        $query .= "            AND TT0.APPLICANTDIV = '".$model->applicantdiv."'  ";
        $query .= "            AND TT0.TESTDIV      = '".$model->testdiv."'  ";
        $query .= "        ) ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 欠席 */
        //入試試験の科目が全て未受験の者は、「4:欠席」で更新
        $query  = " UPDATE ENTEXAM_RECEPT_DAT  ";
        $query .= "    SET JUDGEDIV = '4'  ";
        $query .= "  WHERE ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "    AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "    AND TESTDIV      = '".$model->testdiv."'  ";
        $query .= "    AND EXAMNO IN ( ";
        $query .= "         SELECT TT0.EXAMNO  ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT TT0  ";
        $query .= "          WHERE TT0.ENTEXAMYEAR  = '".$model->examyear."'  ";
        $query .= "            AND TT0.APPLICANTDIV = '".$model->applicantdiv."'  ";
        $query .= "            AND TT0.TESTDIV      = '".$model->testdiv."'  ";
        $query .= "            AND TT0.TOTAL4 IS NULL ";
        $query .= "        ) ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " UPDATE ENTEXAM_APPLICANTBASE_DAT  ";
        $query .= "    SET JUDGEMENT = '4'  ";
        $query .= "  WHERE ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "    AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "    AND JUDGEMENT IS NULL ";
        $query .= "    AND EXAMNO IN ( ";
        $query .= "         SELECT TT0.EXAMNO  ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT TT0  ";
        $query .= "          WHERE TT0.ENTEXAMYEAR  = '".$model->examyear."'  ";
        $query .= "            AND TT0.APPLICANTDIV = '".$model->applicantdiv."'  ";
        $query .= "            AND TT0.TESTDIV      = '".$model->testdiv."'  ";
        $query .= "            AND TT0.TOTAL4 IS NULL ";
        $query .= "        ) ";

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
?>

