<?php

require_once('for_php7.php');

class knjl560fQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
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

    /***
    *** 受験科目
    *** 「1:中学」・・・得点入力（1:国100、2:算100、3:英50）
    *** （TOTAL4）3科200点換算　(国100＋算100＋理50)×2÷2.5＝満点200
    *** （TOTAL3）3科合計　国100＋算100＋英50＝満点250
    *** （TOTAL2）2科合計　国100＋算100＝満点200
    *** （TOTAL1）200合計　（TOTAL2）と（TOTAL4）でよい方
    ***
    *** 「2:高校」・・・得点入力（1:国100、2:社100、3:数100、4:理100、5:英100）
    *** （TOTAL4）5科合計　国100＋社100＋数100＋理100＋英100＝満点500
    *** （TOTAL3）3科合計　国100＋数100＋英100＝満点300
    ***/
    //算出
    function ExecuteQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);


        //ENTEXAM_RECEPT_DAT（見なし含める）

        //合計
        $query = knjl560fQuery::getTotalRecept($model);
        $db->query($query);

        //順位(合計で)
        $query = knjl560fQuery::getRankRecept($model);
        $db->query($query);

        //偏差値(合計で)
        //高校
        if ($model->applicantdiv == "2") {
            $query = knjl560fQuery::getDeviationRecept($model);
            $db->query($query);
        }


        //ENTEXAM_RECEPT_DETAIL_DAT（見なし含めない）

        //SEQ
        $seqList = ($model->applicantdiv == "1") ? array("011","012","013") : array("011","012");
        foreach ($seqList as $key => $seq) {
            //レコード作成
            $query = knjl560fQuery::getDelReceptDetail($model, $seq);
            $db->query($query);
            $query = knjl560fQuery::getInsReceptDetail($model, $seq);
            $db->query($query);

            //合計
            $query = knjl560fQuery::getTotalReceptDetail($model, $seq);
            $db->query($query);

            //順位(合計で)
            $query = knjl560fQuery::getRankReceptDetail($model, $seq);
            $db->query($query);

            //偏差値(合計で)
            //高校
            if ($model->applicantdiv == "2") {
                $query = knjl560fQuery::getDeviationReceptDetail($model, $seq);
                $db->query($query);
            }
        }


        $db->commit();
        Query::dbCheckIn($db);
        return DB_OK;
    } 

    //合計
    function getTotalRecept($model) {
        $minasiFlg = "1"; //1:見なし含める

        $query  = "UPDATE ENTEXAM_RECEPT_DAT UPD ";
        $query .= "   SET (ATTEND_ALL_FLG, ";
        $query .= "        TOTAL4, ";
        $query .= "        TOTAL3, ";
        $query .= "        TOTAL2, ";
        $query .= "        TOTAL1, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT ";
        $query .= "      CASE WHEN TT4.COUNT > 0 THEN '1' END AS ATTEND_ALL_FLG, ";
        //中学
        if ($model->applicantdiv == "1") {
            $query .= "      CASE WHEN TT4.COUNT = 3 ";
            $query .= "           THEN TT4.J_TOTAL3KA200 ";
            $query .= "      END AS TOTAL4, ";                  //3教科200点換算
            $query .= "      TT4.J_TOTAL3KA AS TOTAL3, ";       //3教科合計
            $query .= "      TT4.J_TOTAL2KA AS TOTAL2, ";       //2教科合計
            $query .= "      CASE WHEN VALUE(TT4.J_TOTAL2KA,-1) < VALUE(TT4.J_TOTAL3KA200,-1) ";
            $query .= "           THEN TT4.J_TOTAL3KA200 ";
            $query .= "           ELSE TT4.J_TOTAL2KA ";
            $query .= "      END AS TOTAL1, ";                  //2教科合計と3教科200点換算でよい方
        //高校
        } else {
            $query .= "      CASE WHEN TT4.COUNT = 5 ";
            $query .= "           THEN TT4.H_TOTAL5KA ";
            $query .= "      END AS TOTAL4, ";                  //5教科合計
            $query .= "      TT4.H_TOTAL3KA AS TOTAL3, ";       //3教科合計
            $query .= "      NULL AS TOTAL2, ";
            $query .= "      NULL AS TOTAL1, ";
        }
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT RECEPTNO, ";
        $query .= "                EXAM_TYPE, ";
        //中学
        if ($model->applicantdiv == "1") {
            $query .= "                SMALLINT(ROUND(SUM(SCORE) * 2 / 2.5)) AS J_TOTAL3KA200, ";
            $query .= "                SUM(SCORE) AS J_TOTAL3KA, ";
            $query .= "                SUM(CASE WHEN TESTSUBCLASSCD IN ('1','3') THEN SCORE END) AS J_TOTAL2KA, ";
        //高校
        } else {
            $query .= "                SUM(SCORE) AS H_TOTAL5KA, ";
            $query .= "                SUM(CASE WHEN TESTSUBCLASSCD IN ('1','3','5') THEN SCORE END) AS H_TOTAL3KA, ";
        }
        $query .= "                COUNT(SCORE) AS COUNT ";
        $query .= "           FROM ( ";
        $query .= "                 SELECT R1.RECEPTNO, ";
        $query .= "                        R1.EXAM_TYPE, ";
        $query .= "                        S1.TESTSUBCLASSCD, ";
        if ($minasiFlg == "1") {
            $query .= "                        CASE WHEN S1.TESTSUBCLASSCD = '5' AND VALUE(S1.SCORE,-1) < VALUE(SMALLINT(N1.NAMESPARE2),-1) ";
            $query .= "                             THEN SMALLINT(N1.NAMESPARE2) ";
            $query .= "                             ELSE S1.SCORE ";
            $query .= "                        END AS SCORE ";      //試験得点と見なし得点でよい方（見なし含める）
        } else {
            $query .= "                        S1.SCORE AS SCORE "; //試験得点
        }
        $query .= "                   FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "                        LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "                             ON S1.ENTEXAMYEAR   = R1.ENTEXAMYEAR ";
        $query .= "                            AND S1.APPLICANTDIV  = R1.APPLICANTDIV ";
        $query .= "                            AND S1.TESTDIV       = R1.TESTDIV ";
        $query .= "                            AND S1.EXAM_TYPE     = R1.EXAM_TYPE ";
        $query .= "                            AND S1.RECEPTNO      = R1.RECEPTNO ";
        $query .= "                        LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "                             ON BD005.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                            AND BD005.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                            AND BD005.EXAMNO = R1.EXAMNO ";
        $query .= "                            AND BD005.SEQ = '005' ";
        $query .= "                        LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L055' AND N1.NAMECD2 = BD005.REMARK1 ";
        $query .= "                  WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "                    AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                    AND R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "                ) SCORE_DAT ";
        $query .= "         GROUP BY ";
        $query .= "                RECEPTNO, ";
        $query .= "                EXAM_TYPE ";
        $query .= "       ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT4.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND TT0.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TT0.TESTDIV      = '{$model->testdiv}' "; 
        $query .= "   AND TT0.EXAM_TYPE    = UPD.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = UPD.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE UPD.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND UPD.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND UPD.TESTDIV      = '{$model->testdiv}' "; 

        return $query;
    }

    //順位
    function getRankRecept($model) {
        $query  = "UPDATE ENTEXAM_RECEPT_DAT UPD ";
        $query .= "   SET (TOTAL_RANK4, ";
        $query .= "        TOTAL_RANK3, ";
        $query .= "        TOTAL_RANK2, ";
        $query .= "        TOTAL_RANK1, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT TT4.TOTAL_RANK4, ";
        $query .= "       TT3.TOTAL_RANK3, ";
        $query .= "       TT2.TOTAL_RANK2, ";
        $query .= "       TT1.TOTAL_RANK1, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.TOTAL4 DESC) AS TOTAL_RANK4 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.TOTAL4 IS NOT NULL ";
        $query .= "       ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT4.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.TOTAL3 DESC) AS TOTAL_RANK3 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.TOTAL3 IS NOT NULL ";
        $query .= "       ) TT3 ON TT3.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT3.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.TOTAL2 DESC) AS TOTAL_RANK2 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.TOTAL2 IS NOT NULL ";
        $query .= "       ) TT2 ON TT2.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT2.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.TOTAL1 DESC) AS TOTAL_RANK1 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.TOTAL1 IS NOT NULL ";
        $query .= "       ) TT1 ON TT1.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT1.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND TT0.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TT0.TESTDIV      = '{$model->testdiv}' "; 
        $query .= "   AND TT0.EXAM_TYPE    = UPD.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = UPD.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE UPD.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND UPD.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND UPD.TESTDIV      = '{$model->testdiv}' "; 

        return $query;
    }

    //偏差値
    function getDeviationRecept($model) {
        $query  = "UPDATE ENTEXAM_RECEPT_DAT UPD ";
        $query .= "   SET (AVARAGE4, ";
        $query .= "        AVARAGE3, ";
        $query .= "        AVARAGE2, ";
        $query .= "        AVARAGE1, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT TT4.DEVIATION4, ";
        $query .= "       TT3.DEVIATION3, ";
        $query .= "       TT2.DEVIATION2, ";
        $query .= "       TT1.DEVIATION1, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,CASE WHEN 0 < R2.STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL4-R2.AVG)/R2.STDDEV+50)*10,0)/10,5,1) END AS DEVIATION4 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "                LEFT JOIN ( ";
        $query .= "                    SELECT ";
        $query .= "                        TESTDIV, ";
        $query .= "                        DECIMAL(ROUND(AVG(FLOAT(TOTAL4))*10,0)/10,5,1) AS AVG, ";
        $query .= "                        DECIMAL(ROUND(STDDEV(FLOAT(TOTAL4))*10,0)/10,5,1) AS STDDEV ";
        $query .= "                    FROM ";
        $query .= "                        ENTEXAM_RECEPT_DAT ";
        $query .= "                    WHERE ";
        $query .= "                        ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "                        AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                        AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "                    GROUP BY ";
        $query .= "                        TESTDIV ";
        $query .= "                ) R2 ON R2.TESTDIV = R1.TESTDIV ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.TOTAL4 IS NOT NULL ";
        $query .= "       ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT4.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,CASE WHEN 0 < R2.STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL3-R2.AVG)/R2.STDDEV+50)*10,0)/10,5,1) END AS DEVIATION3 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "                LEFT JOIN ( ";
        $query .= "                    SELECT ";
        $query .= "                        TESTDIV, ";
        $query .= "                        DECIMAL(ROUND(AVG(FLOAT(TOTAL3))*10,0)/10,5,1) AS AVG, ";
        $query .= "                        DECIMAL(ROUND(STDDEV(FLOAT(TOTAL3))*10,0)/10,5,1) AS STDDEV ";
        $query .= "                    FROM ";
        $query .= "                        ENTEXAM_RECEPT_DAT ";
        $query .= "                    WHERE ";
        $query .= "                        ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "                        AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                        AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "                    GROUP BY ";
        $query .= "                        TESTDIV ";
        $query .= "                ) R2 ON R2.TESTDIV = R1.TESTDIV ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.TOTAL3 IS NOT NULL ";
        $query .= "       ) TT3 ON TT3.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT3.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,CASE WHEN 0 < R2.STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL2-R2.AVG)/R2.STDDEV+50)*10,0)/10,5,1) END AS DEVIATION2 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "                LEFT JOIN ( ";
        $query .= "                    SELECT ";
        $query .= "                        TESTDIV, ";
        $query .= "                        DECIMAL(ROUND(AVG(FLOAT(TOTAL2))*10,0)/10,5,1) AS AVG, ";
        $query .= "                        DECIMAL(ROUND(STDDEV(FLOAT(TOTAL2))*10,0)/10,5,1) AS STDDEV ";
        $query .= "                    FROM ";
        $query .= "                        ENTEXAM_RECEPT_DAT ";
        $query .= "                    WHERE ";
        $query .= "                        ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "                        AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                        AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "                    GROUP BY ";
        $query .= "                        TESTDIV ";
        $query .= "                ) R2 ON R2.TESTDIV = R1.TESTDIV ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.TOTAL2 IS NOT NULL ";
        $query .= "       ) TT2 ON TT2.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT2.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,CASE WHEN 0 < R2.STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL1-R2.AVG)/R2.STDDEV+50)*10,0)/10,5,1) END AS DEVIATION1 ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "                LEFT JOIN ( ";
        $query .= "                    SELECT ";
        $query .= "                        TESTDIV, ";
        $query .= "                        DECIMAL(ROUND(AVG(FLOAT(TOTAL1))*10,0)/10,5,1) AS AVG, ";
        $query .= "                        DECIMAL(ROUND(STDDEV(FLOAT(TOTAL1))*10,0)/10,5,1) AS STDDEV ";
        $query .= "                    FROM ";
        $query .= "                        ENTEXAM_RECEPT_DAT ";
        $query .= "                    WHERE ";
        $query .= "                        ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "                        AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                        AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "                    GROUP BY ";
        $query .= "                        TESTDIV ";
        $query .= "                ) R2 ON R2.TESTDIV = R1.TESTDIV ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.TOTAL1 IS NOT NULL ";
        $query .= "       ) TT1 ON TT1.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT1.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND TT0.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TT0.TESTDIV      = '{$model->testdiv}' "; 
        $query .= "   AND TT0.EXAM_TYPE    = UPD.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = UPD.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE UPD.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND UPD.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND UPD.TESTDIV      = '{$model->testdiv}' "; 

        return $query;
    }

    //合計
    function getTotalReceptDetail($model, $seq) {
        $minasiFlg = ""; //1:見なし含める

        $query  = "UPDATE ENTEXAM_RECEPT_DETAIL_DAT UPD ";
        $query .= "   SET (REMARK1, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT ";
        //中学
        if ($model->applicantdiv == "1") {
            if ($seq == "011") {
                $query .= "      CASE WHEN TT4.COUNT = 3 ";
                $query .= "           THEN TT4.J_TOTAL3KA200 ";
                $query .= "      END AS REMARK1, ";                 //3教科200点換算
            } else if ($seq == "012") {
                $query .= "      TT4.J_TOTAL3KA AS REMARK1, ";      //3教科合計
            } else if ($seq == "013") {
                $query .= "      CASE WHEN VALUE(TT4.J_TOTAL2KA,-1) < VALUE(TT4.J_TOTAL3KA200,-1) ";
                $query .= "           THEN TT4.J_TOTAL3KA200 ";
                $query .= "           ELSE TT4.J_TOTAL2KA ";
                $query .= "      END AS REMARK1, ";                 //2教科合計と3教科200点換算でよい方
            } else {
                $query .= "      NULL AS REMARK1, "; //DBエラー回避用
            }
        //高校
        } else {
            if ($seq == "011") {
                $query .= "      CASE WHEN TT4.COUNT = 5 ";
                $query .= "           THEN TT4.H_TOTAL5KA ";
                $query .= "      END AS REMARK1, ";                 //5教科合計
            } else if ($seq == "012") {
                $query .= "      TT4.H_TOTAL3KA AS REMARK1, ";      //3教科合計
            } else {
                $query .= "      NULL AS REMARK1, "; //DBエラー回避用
            }
        }
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DETAIL_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT RECEPTNO, ";
        $query .= "                EXAM_TYPE, ";
        //中学
        if ($model->applicantdiv == "1") {
            $query .= "                SMALLINT(ROUND(SUM(SCORE) * 2 / 2.5)) AS J_TOTAL3KA200, ";
            $query .= "                SUM(SCORE) AS J_TOTAL3KA, ";
            $query .= "                SUM(CASE WHEN TESTSUBCLASSCD IN ('1','3') THEN SCORE END) AS J_TOTAL2KA, ";
        //高校
        } else {
            $query .= "                SUM(SCORE) AS H_TOTAL5KA, ";
            $query .= "                SUM(CASE WHEN TESTSUBCLASSCD IN ('1','3','5') THEN SCORE END) AS H_TOTAL3KA, ";
        }
        $query .= "                COUNT(SCORE) AS COUNT ";
        $query .= "           FROM ( ";
        $query .= "                 SELECT R1.RECEPTNO, ";
        $query .= "                        R1.EXAM_TYPE, ";
        $query .= "                        S1.TESTSUBCLASSCD, ";
        if ($minasiFlg == "1") {
            $query .= "                        CASE WHEN S1.TESTSUBCLASSCD = '5' AND VALUE(S1.SCORE,-1) < VALUE(SMALLINT(N1.NAMESPARE2),-1) ";
            $query .= "                             THEN SMALLINT(N1.NAMESPARE2) ";
            $query .= "                             ELSE S1.SCORE ";
            $query .= "                        END AS SCORE ";      //試験得点と見なし得点でよい方（見なし含める）
        } else {
            $query .= "                        S1.SCORE AS SCORE "; //試験得点
        }
        $query .= "                   FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "                        LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "                             ON S1.ENTEXAMYEAR   = R1.ENTEXAMYEAR ";
        $query .= "                            AND S1.APPLICANTDIV  = R1.APPLICANTDIV ";
        $query .= "                            AND S1.TESTDIV       = R1.TESTDIV ";
        $query .= "                            AND S1.EXAM_TYPE     = R1.EXAM_TYPE ";
        $query .= "                            AND S1.RECEPTNO      = R1.RECEPTNO ";
        $query .= "                        LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "                             ON BD005.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                            AND BD005.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                            AND BD005.EXAMNO = R1.EXAMNO ";
        $query .= "                            AND BD005.SEQ = '005' ";
        $query .= "                        LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L055' AND N1.NAMECD2 = BD005.REMARK1 ";
        $query .= "                  WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "                    AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                    AND R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "                ) SCORE_DAT ";
        $query .= "         GROUP BY ";
        $query .= "                RECEPTNO, ";
        $query .= "                EXAM_TYPE ";
        $query .= "       ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT4.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND TT0.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TT0.TESTDIV      = '{$model->testdiv}' ";
        $query .= "   AND TT0.EXAM_TYPE    = UPD.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = UPD.RECEPTNO ";
        $query .= "   AND TT0.SEQ          = '{$seq}' ";
        $query .= "       )";
        $query .= " WHERE UPD.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND UPD.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND UPD.TESTDIV      = '{$model->testdiv}' ";
        $query .= "   AND UPD.SEQ          = '{$seq}' ";

        return $query;
    }

    //順位
    function getRankReceptDetail($model, $seq) {
        $query  = "UPDATE ENTEXAM_RECEPT_DETAIL_DAT UPD ";
        $query .= "   SET (REMARK3, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT TT4.TOTAL_RANK AS REMARK3, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DETAIL_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY SMALLINT(R1.REMARK1) DESC) AS TOTAL_RANK ";
        $query .= "           FROM ENTEXAM_RECEPT_DETAIL_DAT R1 ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.SEQ          = '{$seq}' ";
        $query .= "            AND R1.REMARK1 IS NOT NULL ";
        $query .= "       ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT4.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND TT0.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TT0.TESTDIV      = '{$model->testdiv}' "; 
        $query .= "   AND TT0.EXAM_TYPE    = UPD.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = UPD.RECEPTNO ";
        $query .= "   AND TT0.SEQ          = '{$seq}' ";
        $query .= "       )";
        $query .= " WHERE UPD.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND UPD.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND UPD.TESTDIV      = '{$model->testdiv}' "; 
        $query .= "   AND UPD.SEQ          = '{$seq}' ";

        return $query;
    }

    //偏差値
    function getDeviationReceptDetail($model, $seq) {
        $query  = "UPDATE ENTEXAM_RECEPT_DETAIL_DAT UPD ";
        $query .= "   SET (REMARK2, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT TT4.DEVIATION AS REMARK2, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DETAIL_DAT TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT R1.RECEPTNO ";
        $query .= "               ,R1.EXAM_TYPE ";
        $query .= "               ,CASE WHEN 0 < R2.STDDEV THEN DECIMAL(ROUND((10*(SMALLINT(R1.REMARK1)-R2.AVG)/R2.STDDEV+50)*10,0)/10,5,1) END AS DEVIATION ";
        $query .= "           FROM ENTEXAM_RECEPT_DETAIL_DAT R1 ";
        $query .= "                LEFT JOIN ( ";
        $query .= "                    SELECT ";
        $query .= "                        TESTDIV, ";
        $query .= "                        DECIMAL(ROUND(AVG(FLOAT(SMALLINT(REMARK1)))*10,0)/10,5,1) AS AVG, ";
        $query .= "                        DECIMAL(ROUND(STDDEV(FLOAT(SMALLINT(REMARK1)))*10,0)/10,5,1) AS STDDEV ";
        $query .= "                    FROM ";
        $query .= "                        ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= "                    WHERE ";
        $query .= "                        ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "                        AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                        AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "                        AND SEQ          = '{$seq}' ";
        $query .= "                    GROUP BY ";
        $query .= "                        TESTDIV ";
        $query .= "                ) R2 ON R2.TESTDIV = R1.TESTDIV ";
        $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R1.APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "            AND R1.TESTDIV      = '{$model->testdiv}'";
        $query .= "            AND R1.SEQ          = '{$seq}' ";
        $query .= "            AND R1.REMARK1 IS NOT NULL ";
        $query .= "       ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= "            AND TT4.EXAM_TYPE = TT0.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND TT0.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TT0.TESTDIV      = '{$model->testdiv}' "; 
        $query .= "   AND TT0.EXAM_TYPE    = UPD.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = UPD.RECEPTNO ";
        $query .= "   AND TT0.SEQ          = '{$seq}' ";
        $query .= "       )";
        $query .= " WHERE UPD.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND UPD.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND UPD.TESTDIV      = '{$model->testdiv}' "; 
        $query .= "   AND UPD.SEQ          = '{$seq}' ";

        return $query;
    }

    //ENTEXAM_RECEPT_DETAIL_DAT(SEQ=011,012,013)レコード作成
    function getDelReceptDetail($model, $seq) {
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV      = '{$model->testdiv}' "; 
        $query .= "   AND SEQ          = '{$seq}' ";
        return $query;
    }
    function getInsReceptDetail($model, $seq) {
        $query  = " INSERT INTO ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= "     (ENTEXAMYEAR, ";
        $query .= "      APPLICANTDIV, ";
        $query .= "      TESTDIV, ";
        $query .= "      EXAM_TYPE, ";
        $query .= "      RECEPTNO, ";
        $query .= "      SEQ, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED) ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     RECEPTNO, ";
        $query .= "     '{$seq}' AS SEQ, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV      = '{$model->testdiv}' "; 
        return $query;
    }
}
?>
