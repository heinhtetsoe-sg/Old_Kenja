<?php

require_once('for_php7.php');

class knjl060uQuery extends Query {

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

    //対象人数チェック
    function checkRecCnt($model) {
        $query  = "SELECT COUNT(*) FROM entexam_recept_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        return $query;
    }

    //得点データチェック
    function checkScoCnt($model) {
        $query  = "SELECT COUNT(*) FROM entexam_score_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'"; 
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        return $query;
    }

    //算出
    function ExecuteQuery($model) {
        //参照データ（得点）

        //DB接続
        $db = Query::dbCheckOut();

        //受験科目
        $testsubArray = array();
        $query = knjl060uQuery::getNameMst($model->examyear, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubArray[] = $row["VALUE"];
        }
        $result->free();

        $cnt = get_count($testsubArray);

        //エラー無視
        $db->expectError();

        /***
        *** 受験科目
        *** 「1:中学」・・・満点320 1:国(100)、2:算(100)、3:社(60)、4:理(60)
        ***/

        /* 全科目分受験フラグ、合計、平均 */ 
        //科目受験フラグ(attend_flg) 1:受験 0:欠席(*) NULL:得点未入力
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, ";
        $query .= "        attend_all_flg, ";
        $query .= "        total4, ";
        $query .= "        avarage4, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno, ";
        $query .= "      TT4.attend_all_flg     AS attend_all_flg, ";
        $query .= "      CASE WHEN B1.SLIDE_FLG = '1' THEN 0 ELSE TT4.total END AS total4, ";
        $query .= "      TT4.avg                AS avarage4, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,CASE WHEN COUNT(t1.score) >= {$cnt} THEN '1' ELSE '0' END AS attend_all_flg";
//      $query .= "               ,CASE WHEN min(t1.attend_flg) = '0' THEN '0' ELSE '1' END AS attend_all_flg ";
        $query .= "               ,SUM(t1.score) AS total ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS avg ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "         GROUP BY t1.receptno ";
        $query .= "       ) TT4 ON TT4.receptno = TT0.receptno ";
        $query .= "       LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT B1 ON B1.entexamyear = TT0.entexamyear AND B1.applicantdiv = TT0.applicantdiv AND B1.examno = TT0.examno ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 
        $query .= "   AND VALUE(judgediv,'0') != '5' "; //受験不可

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        //--------------------【合計での順位】--------------------

        /* 席次(合計) */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, ";
        $query .= "        total_rank4, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno, ";
        $query .= "       TT4.total_rank4, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.total4 DESC) AS total_rank4 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.total4 IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
        $query .= "            AND TT0.exam_type = TT4.exam_type ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 
        $query .= "   AND VALUE(judgediv,'0') != '5' "; //受験不可

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【（得点・合計）別　平均・標準偏差など】--------------------

        //DELETE
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_AVARAGE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " INSERT INTO ENTEXAM_JUDGE_AVARAGE_DAT ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     AVARAGE_MEN, ";
        $query .= "     AVARAGE_WOMEN, ";
        $query .= "     AVARAGE_TOTAL, ";
        $query .= "     MAX_SCORE, ";
        $query .= "     MIN_SCORE, ";
        $query .= "     COUNT, ";
        $query .= "     CALC_STDDEV, ";
        $query .= "     CALC_AVG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        //得点
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.SCORE END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.SCORE END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.SCORE) AS MAX_SCORE, ";
        $query .= "     MIN(T1.SCORE) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.SCORE) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.SCORE))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*100000,0)/100000,8,5) AS CALC_AVG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "         AND T2.RECEPTNO = T1.RECEPTNO ";
        $query .= "         AND VALUE(T2.JUDGEDIV,'0') != '5' "; //受験不可
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "          ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO = T2.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->testdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTSUBCLASSCD ";
        //合計(B)(TOTAL4)
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'B' AS TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.TOTAL4 END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.TOTAL4 END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.TOTAL4) AS MAX_SCORE, ";
        $query .= "     MIN(T1.TOTAL4) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.TOTAL4) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.TOTAL4))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4))*100000,0)/100000,8,5) AS CALC_AVG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "          ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND VALUE(T1.JUDGEDIV,'0') != '5' "; //受験不可
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【欠席】--------------------

        /* 欠席(一旦クリア) */
        $query  = " UPDATE entexam_recept_dat  ";
        $query .= "    SET judgediv = NULL  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND judgediv     = '4'  ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " UPDATE entexam_applicantbase_dat  ";
        $query .= "    SET judgement = NULL  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND judgement    = '4'  ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 欠席 */
        //入試試験の科目が全て未受験の者は、「4:欠席」で更新
        $query  = " UPDATE entexam_recept_dat  ";
        $query .= "    SET judgediv = '4'  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND examno IN ( ";
        $query .= "         SELECT TT0.examno  ";
        $query .= "           FROM entexam_recept_dat TT0  ";
        $query .= "          WHERE TT0.entexamyear  = '".$model->examyear."'  ";
        $query .= "            AND TT0.applicantdiv = '".$model->applicantdiv."'  ";
        $query .= "            AND TT0.testdiv      = '".$model->testdiv."'  ";
        $query .= "            AND TT0.total4 IS NULL ";
        $query .= "        ) ";
        $query .= "    AND VALUE(judgediv,'0') != '5' "; //受験不可

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " UPDATE entexam_applicantbase_dat  ";
        $query .= "    SET judgement = '4'  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND examno IN ( ";
        $query .= "         SELECT TT0.examno  ";
        $query .= "           FROM entexam_recept_dat TT0  ";
        $query .= "          WHERE TT0.entexamyear  = '".$model->examyear."'  ";
        $query .= "            AND TT0.applicantdiv = '".$model->applicantdiv."'  ";
        $query .= "            AND TT0.testdiv      = '".$model->testdiv."'  ";
        $query .= "            AND TT0.total4 IS NULL ";
        $query .= "        ) ";
        $query .= "    AND VALUE(judgement,'0') != '5' "; //受験不可

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

