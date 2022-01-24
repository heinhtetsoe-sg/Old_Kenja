<?php

require_once('for_php7.php');

class knjl060pQuery extends Query {

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

    //作文データチェック
    function checkSakubunCnt($model) {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_SAKUBUN_DAT T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'"; 
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        return $query;
    }

    //実績データチェック
    function checkJissekiCnt($model) {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_JISSEKI_DAT T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'"; 
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        return $query;
    }

    //算出・・・「2:高校」「1:県内推薦」以外
    function ExecuteQuery($model) {
        //参照データ（得点・面接）

        //DB接続
        $db = Query::dbCheckOut();

        //受験科目
        $testsubArray = array();
        $query = knjl060pQuery::getNameMst($model->examyear, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubArray[] = $row["VALUE"];
        }
        $result->free();

        //エラー無視
        $db->expectError();

        /***
        *** 受験科目
        *** 「1:中学」「1:県外前期」・・・満点500 1:国(150)、2:算(150)、3:理(100)、4:社(100)
        *** 「1:中学」「2:県内前期」・・・満点550 1:国(150)、2:算(150)、3:理(100)、4:社(100)、面接(50)
        *** 「1:中学」「3:県内後期」・・・満点350 1:国(150)、2:算(150)、面接(50)
        *** 「2:高校」「2:県内一般」・・・満点350 1:国(100)、2:数(100)、5:英(100)、面接(50)
        ***/

        /* 全科目分受験フラグ、合計(面接なし・あり) */ 
        //科目受験フラグ(attend_flg) 1:受験 0:欠席(*) NULL:得点未入力
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, ";
        $query .= "        attend_all_flg, ";
        $query .= "        total4, ";
        $query .= "        total2, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno, ";
        $query .= "      TT4.attend_all_flg                AS attend_all_flg, ";
        $query .= "      TT4.total                         AS total4, ";
        $query .= "      TT4.total + VALUE(L1.SCORE, 0)    AS total2, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        //得点
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,CASE WHEN min(t1.attend_flg) = '0' THEN '0' ELSE '1' END AS attend_all_flg ";
        $query .= "               ,SUM(t1.score) AS total ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "         GROUP BY t1.receptno ";
        $query .= "       ) TT4 ON TT4.receptno = TT0.receptno ";
        //面接
        $query .= "       LEFT JOIN ENTEXAM_INTERVIEW_DAT L1 ON L1.ENTEXAMYEAR = TT0.ENTEXAMYEAR ";
        $query .= "           AND L1.APPLICANTDIV = TT0.APPLICANTDIV ";
        $query .= "           AND L1.TESTDIV = TT0.TESTDIV ";
        $query .= "           AND L1.EXAMNO = TT0.EXAMNO ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        //--------------------【（得点・面接・合計）別　平均・標準偏差など】--------------------

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
        //面接(A)
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'A' AS TESTSUBCLASSCD, ";
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
        $query .= "     ENTEXAM_INTERVIEW_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
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
        $query .= "     T1.TESTDIV ";
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
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV ";
        //合計(C)(TOTAL2)
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'C' AS TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.TOTAL2 END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.TOTAL2 END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL2))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.TOTAL2) AS MAX_SCORE, ";
        $query .= "     MIN(T1.TOTAL2) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.TOTAL2) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.TOTAL2))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL2))*100000,0)/100000,8,5) AS CALC_AVG, ";
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

        //--------------------【得点・面接・合計　偏差値など】--------------------

        /* 偏差値(合計) */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, JUDGE_DEVIATION, LINK_JUDGE_DEVIATION, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.JUDGE_DEVIATION ";        //入試区分偏差値(TOTAL4)
        $query .= "      ,TT4.LINK_JUDGE_DEVIATION ";   //入試区分偏差値(TOTAL2)
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT  R1.receptno";
        $query .= "                ,R1.exam_type";
        $query .= "                ,CASE WHEN 0 < R2.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL4-R2.CALC_AVG)/R2.CALC_STDDEV+50)*10,0)/10,5,1) END AS JUDGE_DEVIATION ";
        $query .= "                ,CASE WHEN 0 < R3.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL2-R3.CALC_AVG)/R3.CALC_STDDEV+50)*10,0)/10,5,1) END AS LINK_JUDGE_DEVIATION ";
        $query .= "           FROM  entexam_recept_dat R1";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R2 ";
        $query .= "                      ON R2.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                     AND R2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                     AND R2.TESTDIV = R1.TESTDIV ";
        $query .= "                     AND R2.EXAM_TYPE = '1' ";
        $query .= "                     AND R2.TESTSUBCLASSCD = 'B' ";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R3 ";
        $query .= "                      ON R3.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                     AND R3.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                     AND R3.TESTDIV = R1.TESTDIV ";
        $query .= "                     AND R3.EXAM_TYPE = '1' ";
        $query .= "                     AND R3.TESTSUBCLASSCD = 'C' ";
        $query .= "          WHERE  R1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND  R1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND  R1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND  R1.total4 IS NOT NULL ";
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

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 偏差値(得点) */
        $query  = "UPDATE ENTEXAM_SCORE_DAT ";
        $query .= "   SET (STD_SCORE, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT ";
        $query .= "       TT4.DEVIATION AS STD_SCORE, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_SCORE_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT  R1.receptno";
        $query .= "                ,R1.exam_type";
        $query .= "                ,R1.TESTSUBCLASSCD";
        $query .= "                ,CASE WHEN 0 < R2.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.SCORE-R2.CALC_AVG)/R2.CALC_STDDEV+50)*10,0)/10,5,1) END AS DEVIATION ";
        $query .= "           FROM  ENTEXAM_SCORE_DAT R1";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R2 ";
        $query .= "                      ON R2.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                     AND R2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                     AND R2.TESTDIV = R1.TESTDIV ";
        $query .= "                     AND R2.EXAM_TYPE = '1' ";
        $query .= "                     AND R2.TESTSUBCLASSCD = R1.TESTSUBCLASSCD ";
        $query .= "          WHERE  R1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND  R1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND  R1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND  R1.SCORE IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
        $query .= "            AND TT0.exam_type = TT4.exam_type ";
        $query .= "            AND TT0.TESTSUBCLASSCD = TT4.TESTSUBCLASSCD ";
        $query .= " WHERE TT0.entexamyear   = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv  = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv       = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type     = ENTEXAM_SCORE_DAT.exam_type ";
        $query .= "   AND TT0.receptno      = ENTEXAM_SCORE_DAT.receptno ";
        $query .= "   AND TT0.TESTSUBCLASSCD= ENTEXAM_SCORE_DAT.TESTSUBCLASSCD ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 偏差値(面接) */
        $query  = "UPDATE ENTEXAM_INTERVIEW_DAT ";
        $query .= "   SET (STD_SCORE, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT ";
        $query .= "       TT4.DEVIATION AS STD_SCORE, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_INTERVIEW_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT  R1.EXAMNO";
        $query .= "                ,CASE WHEN 0 < R2.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.SCORE-R2.CALC_AVG)/R2.CALC_STDDEV+50)*10,0)/10,5,1) END AS DEVIATION ";
        $query .= "           FROM  ENTEXAM_INTERVIEW_DAT R1";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R2 ";
        $query .= "                      ON R2.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                     AND R2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                     AND R2.TESTDIV = R1.TESTDIV ";
        $query .= "                     AND R2.EXAM_TYPE = '1' ";
        $query .= "                     AND R2.TESTSUBCLASSCD = 'A' ";
        $query .= "          WHERE  R1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND  R1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND  R1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND  R1.SCORE IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.EXAMNO = TT4.EXAMNO ";
        $query .= " WHERE TT0.entexamyear   = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv  = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv       = '".$model->testdiv."' ";
        $query .= "   AND TT0.EXAMNO        = ENTEXAM_INTERVIEW_DAT.EXAMNO ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【合計偏差値での順位】--------------------

        /* 席次(合計) */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, ";
        $query .= "        total_rank4, ";
        $query .= "        total_rank2, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno, ";
        $query .= "       TT4.total_rank4, ";
        $query .= "       TT2.total_rank2, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.JUDGE_DEVIATION DESC) AS total_rank4 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.JUDGE_DEVIATION IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
        $query .= "            AND TT0.exam_type = TT4.exam_type ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.LINK_JUDGE_DEVIATION DESC) AS total_rank2 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.LINK_JUDGE_DEVIATION IS NOT NULL ";
        $query .= "       ) TT2 ON TT0.receptno  = TT2.receptno ";
        $query .= "            AND TT0.exam_type = TT2.exam_type ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

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

    //算出・・・「2:高校」「1:県内推薦」
    function ExecuteQuerySuisen($model) {
        //参照データ（調査書・作文・実績・面接）

        //DB接続
        $db = Query::dbCheckOut();

        //エラー無視
        $db->expectError();

        /* 調査書 */
        //３年間の成績の合計を算出
        $query  = "UPDATE ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= "   SET (TOTAL_ALL, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT ";
        $query .= "       TT4.SCORE AS TOTAL_ALL, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM V_ENTEXAM_APPLICANTBASE_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "           SELECT ";
        $query .= "               G3.EXAMNO, ";
        $query .= "               value(G3.CONFIDENTIAL_RPT01,0)+ ";
        $query .= "               value(G3.CONFIDENTIAL_RPT02,0)+ ";
        $query .= "               value(G3.CONFIDENTIAL_RPT03,0)+ ";
        $query .= "               value(G3.CONFIDENTIAL_RPT04,0)+ ";
        $query .= "               value(G3.CONFIDENTIAL_RPT05,0)+ ";
        $query .= "               value(G3.CONFIDENTIAL_RPT06,0)+ ";
        $query .= "               value(G3.CONFIDENTIAL_RPT07,0)+ ";
        $query .= "               value(G3.CONFIDENTIAL_RPT08,0)+ ";
        $query .= "               value(G3.CONFIDENTIAL_RPT09,0)+ ";
        $query .= "               value(smallint(G1.REMARK1),0)+ ";
        $query .= "               value(smallint(G1.REMARK2),0)+ ";
        $query .= "               value(smallint(G1.REMARK3),0)+ ";
        $query .= "               value(smallint(G1.REMARK4),0)+ ";
        $query .= "               value(smallint(G1.REMARK5),0)+ ";
        $query .= "               value(smallint(G1.REMARK6),0)+ ";
        $query .= "               value(smallint(G1.REMARK7),0)+ ";
        $query .= "               value(smallint(G1.REMARK8),0)+ ";
        $query .= "               value(smallint(G1.REMARK9),0)+ ";
        $query .= "               value(smallint(G2.REMARK1),0)+ ";
        $query .= "               value(smallint(G2.REMARK2),0)+ ";
        $query .= "               value(smallint(G2.REMARK3),0)+ ";
        $query .= "               value(smallint(G2.REMARK4),0)+ ";
        $query .= "               value(smallint(G2.REMARK5),0)+ ";
        $query .= "               value(smallint(G2.REMARK6),0)+ ";
        $query .= "               value(smallint(G2.REMARK7),0)+ ";
        $query .= "               value(smallint(G2.REMARK8),0)+ ";
        $query .= "               value(smallint(G2.REMARK9),0) AS SCORE ";
        $query .= "           FROM ";
        $query .= "               ENTEXAM_APPLICANTCONFRPT_DAT G3 ";
        $query .= "               LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT G1 ON G1.ENTEXAMYEAR = G3.ENTEXAMYEAR AND G1.APPLICANTDIV = G3.APPLICANTDIV AND G1.EXAMNO = G3.EXAMNO AND G1.SEQ = '001' ";
        $query .= "               LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT G2 ON G2.ENTEXAMYEAR = G3.ENTEXAMYEAR AND G2.APPLICANTDIV = G3.APPLICANTDIV AND G2.EXAMNO = G3.EXAMNO AND G2.SEQ = '002' ";
        $query .= "           WHERE ";
        $query .= "               G3.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "               AND G3.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "       ) TT4 ON TT0.EXAMNO = TT4.EXAMNO ";
        $query .= " WHERE TT0.entexamyear   = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv  = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv       = '".$model->testdiv."' ";
        $query .= "   AND TT0.EXAMNO        = ENTEXAM_APPLICANTCONFRPT_DAT.EXAMNO ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND EXAMNO IN ( "; 
        $query .= "        SELECT EXAMNO FROM V_ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "         WHERE entexamyear  = '".$model->examyear."'";
        $query .= "           AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "           AND testdiv      = '".$model->testdiv."'"; 
        $query .= "   ) "; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /***
        *** 受験科目
        *** 「2:高校」「1:県内推薦」・・・満点400 調査書(180)、作文(100)、実績(60)、面接(60)
        ***/

        /* 全科目分受験フラグ、合計(面接なし・あり) */ 
        //科目受験フラグ(attend_flg) 1:受験 0:欠席(*) NULL:得点未入力
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, ";
        $query .= "        attend_all_flg, ";
        $query .= "        total4, ";
        $query .= "        total2, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno, ";
        $query .= "      '1' AS attend_all_flg, ";
        $query .= "      VALUE(L4.TOTAL_ALL, 0) + VALUE(L2.TOTAL, 0) + VALUE(L3.SCORE, 0) AS total4, ";
        $query .= "      VALUE(L4.TOTAL_ALL, 0) + VALUE(L2.TOTAL, 0) + VALUE(L3.SCORE, 0) + VALUE(L1.SCORE, 0) AS total2, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        //調査書
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT L4 ON L4.ENTEXAMYEAR = TT0.ENTEXAMYEAR ";
        $query .= "           AND L4.APPLICANTDIV = TT0.APPLICANTDIV ";
        $query .= "           AND L4.EXAMNO = TT0.EXAMNO ";
        //作文
        $query .= "       LEFT JOIN ENTEXAM_SAKUBUN_DAT L2 ON L2.ENTEXAMYEAR = TT0.ENTEXAMYEAR ";
        $query .= "           AND L2.APPLICANTDIV = TT0.APPLICANTDIV ";
        $query .= "           AND L2.TESTDIV = TT0.TESTDIV ";
        $query .= "           AND L2.EXAMNO = TT0.EXAMNO ";
        //実績
        $query .= "       LEFT JOIN ENTEXAM_JISSEKI_DAT L3 ON L3.ENTEXAMYEAR = TT0.ENTEXAMYEAR ";
        $query .= "           AND L3.APPLICANTDIV = TT0.APPLICANTDIV ";
        $query .= "           AND L3.TESTDIV = TT0.TESTDIV ";
        $query .= "           AND L3.EXAMNO = TT0.EXAMNO ";
        //面接
        $query .= "       LEFT JOIN ENTEXAM_INTERVIEW_DAT L1 ON L1.ENTEXAMYEAR = TT0.ENTEXAMYEAR ";
        $query .= "           AND L1.APPLICANTDIV = TT0.APPLICANTDIV ";
        $query .= "           AND L1.TESTDIV = TT0.TESTDIV ";
        $query .= "           AND L1.EXAMNO = TT0.EXAMNO ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        //--------------------【（調査書・作文・実績・面接・合計）別　平均・標準偏差など】--------------------

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
        //面接(A)
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'A' AS TESTSUBCLASSCD, ";
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
        $query .= "     ENTEXAM_INTERVIEW_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
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
        $query .= "     T1.TESTDIV ";
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
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV ";
        //合計(C)(TOTAL2)
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'C' AS TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.TOTAL2 END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.TOTAL2 END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL2))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.TOTAL2) AS MAX_SCORE, ";
        $query .= "     MIN(T1.TOTAL2) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.TOTAL2) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.TOTAL2))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL2))*100000,0)/100000,8,5) AS CALC_AVG, ";
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
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV ";
        //調査書(D)
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T2.ENTEXAMYEAR, ";
        $query .= "     T2.APPLICANTDIV, ";
        $query .= "     T2.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'D' AS TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.TOTAL_ALL END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.TOTAL_ALL END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL_ALL))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.TOTAL_ALL) AS MAX_SCORE, ";
        $query .= "     MIN(T1.TOTAL_ALL) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.TOTAL_ALL) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.TOTAL_ALL))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL_ALL))*100000,0)/100000,8,5) AS CALC_AVG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T2 ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "          ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO = T2.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT T1 ";
        $query .= "          ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO = T2.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T2.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND T2.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T2.TESTDIV      = '".$model->testdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     T2.ENTEXAMYEAR, ";
        $query .= "     T2.APPLICANTDIV, ";
        $query .= "     T2.TESTDIV ";
        //作文(E)
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'E' AS TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.TOTAL END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.TOTAL END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.TOTAL) AS MAX_SCORE, ";
        $query .= "     MIN(T1.TOTAL) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.TOTAL) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.TOTAL))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL))*100000,0)/100000,8,5) AS CALC_AVG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SAKUBUN_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
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
        $query .= "     T1.TESTDIV ";
        //実績(F)
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'F' AS TESTSUBCLASSCD, ";
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
        $query .= "     ENTEXAM_JISSEKI_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
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
        $query .= "     T1.TESTDIV ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【面接・合計　偏差値など】--------------------

        /* 偏差値(合計) */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, JUDGE_DEVIATION, LINK_JUDGE_DEVIATION, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.JUDGE_DEVIATION ";        //入試区分偏差値(TOTAL4)
        $query .= "      ,TT4.LINK_JUDGE_DEVIATION ";   //入試区分偏差値(TOTAL2)
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT  R1.receptno";
        $query .= "                ,R1.exam_type";
        $query .= "                ,CASE WHEN 0 < R2.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL4-R2.CALC_AVG)/R2.CALC_STDDEV+50)*10,0)/10,5,1) END AS JUDGE_DEVIATION ";
        $query .= "                ,CASE WHEN 0 < R3.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL2-R3.CALC_AVG)/R3.CALC_STDDEV+50)*10,0)/10,5,1) END AS LINK_JUDGE_DEVIATION ";
        $query .= "           FROM  entexam_recept_dat R1";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R2 ";
        $query .= "                      ON R2.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                     AND R2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                     AND R2.TESTDIV = R1.TESTDIV ";
        $query .= "                     AND R2.EXAM_TYPE = '1' ";
        $query .= "                     AND R2.TESTSUBCLASSCD = 'B' ";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R3 ";
        $query .= "                      ON R3.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                     AND R3.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                     AND R3.TESTDIV = R1.TESTDIV ";
        $query .= "                     AND R3.EXAM_TYPE = '1' ";
        $query .= "                     AND R3.TESTSUBCLASSCD = 'C' ";
        $query .= "          WHERE  R1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND  R1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND  R1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND  R1.TOTAL4 IS NOT NULL ";
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

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 偏差値(面接) */
        $query  = "UPDATE ENTEXAM_INTERVIEW_DAT ";
        $query .= "   SET (STD_SCORE, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT ";
        $query .= "       TT4.DEVIATION AS STD_SCORE, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_INTERVIEW_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT  R1.EXAMNO";
        $query .= "                ,CASE WHEN 0 < R2.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.SCORE-R2.CALC_AVG)/R2.CALC_STDDEV+50)*10,0)/10,5,1) END AS DEVIATION ";
        $query .= "           FROM  ENTEXAM_INTERVIEW_DAT R1";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R2 ";
        $query .= "                      ON R2.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                     AND R2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                     AND R2.TESTDIV = R1.TESTDIV ";
        $query .= "                     AND R2.EXAM_TYPE = '1' ";
        $query .= "                     AND R2.TESTSUBCLASSCD = 'A' ";
        $query .= "          WHERE  R1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND  R1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND  R1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND  R1.SCORE IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.EXAMNO = TT4.EXAMNO ";
        $query .= " WHERE TT0.entexamyear   = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv  = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv       = '".$model->testdiv."' ";
        $query .= "   AND TT0.EXAMNO        = ENTEXAM_INTERVIEW_DAT.EXAMNO ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【合計偏差値での順位】--------------------

        /* 席次(合計) */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, ";
        $query .= "        total_rank4, ";
        $query .= "        total_rank2, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno, ";
        $query .= "       TT4.total_rank4, ";
        $query .= "       TT2.total_rank2, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.JUDGE_DEVIATION DESC) AS total_rank4 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.JUDGE_DEVIATION IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
        $query .= "            AND TT0.exam_type = TT4.exam_type ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.LINK_JUDGE_DEVIATION DESC) AS total_rank2 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.LINK_JUDGE_DEVIATION IS NOT NULL ";
        $query .= "       ) TT2 ON TT0.receptno  = TT2.receptno ";
        $query .= "            AND TT0.exam_type = TT2.exam_type ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

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

