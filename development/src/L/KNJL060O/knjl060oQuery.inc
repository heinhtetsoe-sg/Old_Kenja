<?php

require_once('for_php7.php');

class knjl060oquery extends Query {

    //名称
    function GetName($namecd1, $ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    function ExecuteQuery($model)
    {
        $db = Query::dbCheckOut();
        
        //対象人数チェック
        $query  = "SELECT COUNT(*) FROM entexam_recept_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'"; 

        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }

//        $db->autoCommit(false);

        $subclasscd2 = $subclasscd4 = array();

        //科目型の科目を取得
        $result = $db->query(knjl060oQuery::GetName("L009", $model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMESPARE1"] == "1") {
                $subclasscd2[] = $row["NAMECD2"];
            }
            $subclasscd4[] = $row["NAMECD2"];
        }

        //加点算出対象入試か？ 1:加点算出対象入試 2:加点ポイント対象入試 NULL:ブランク
        $query = " SELECT W1.KATENDIV FROM ENTEXAM_TESTDIV_MST W1 WHERE W1.ENTEXAMYEAR = '".$model->examyear."' AND W1.TESTDIV = '".$model->testdiv."' ";
        $katendiv = $db->getOne($query);

        //エラー無視
        $db->expectError();
        /*                  */
        /* 受付データの更新 */
        /*                  */
        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total2, avarage2, total4, KATEN, avarage4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT1.attend_all_flg ";     //全科目受験フラグ
        $query .= "      ,TT1.total2 ";             //２科目合計
        $query .= "      ,TT1.average2 ";           //２科目平均
        //加点算出対象入試の場合、合計に加点を含める。
        if ($katendiv == "1") {
            $query .= "      ,TT1.total4 + VALUE(TT2.KATEN, 0) ";
            $query .= "      ,TT2.KATEN ";
        } else {
            $query .= "      ,TT1.total4 ";
            $query .= "      ,cast(NULL as smallint) AS KATEN ";
        }
        $query .= "      ,TT1.average4 ";           //４科目平均
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT OUTER JOIN ";
        $query .= "       (SELECT T1.receptno ";
        $query .= "              ,T1.exam_type ";
        $query .= "              ,CASE WHEN T2.attend_flg IS NULL THEN T1.attend_flg ELSE T2.attend_flg END AS attend_all_flg ";
        $query .= "              ,T1.total2 ";
        $query .= "              ,T1.average2 ";
        $query .= "              ,T2.total4 ";
        $query .= "              ,T2.average4 ";
        $query .= "         FROM  ";
                            /* 2科目型の人の出欠フラグ、合計点、平均点 */
        $query .= "        (SELECT receptno ";
        $query .= "              ,exam_type ";
        $query .= "              ,CASE WHEN SUM(SMALLINT(attend_flg)) = 2 THEN '1' ELSE '0' END AS attend_flg ";
        $query .= "              ,SUM(score)                   AS total2 ";
        $query .= "              ,ROUND(AVG(DECIMAL(score)),1) AS average2 ";
        $query .= "          FROM entexam_score_dat ";
        $query .= "         WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "           AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "           AND testdiv      = '".$model->testdiv."' ";
        $query .= "           AND exam_type    = '1' ";
        $query .= "           AND testsubclasscd IN ('".implode("','",$subclasscd2)."') ";
        $query .= "        GROUP BY receptno, exam_type ";
        $query .= "        UNION ";
                            /* 4科目型の人の2科目分の出欠フラグ、合計点、平均点 */
        $query .= "        select receptno ";
        $query .= "              ,exam_type ";
        $query .= "              ,CASE WHEN SUM(SMALLINT(attend_flg)) = 4 THEN '1' ELSE '0' END AS attend_flg ";
        $query .= "              ,SUM(score)                    AS total2 ";
        $query .= "              ,ROUND(AVG(DECIMAL(score)),1)   AS average2 ";
        $query .= "          FROM entexam_score_dat ";
        $query .= "         where entexamyear  = '".$model->examyear."'  ";
        $query .= "           AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "           AND testdiv      = '".$model->testdiv."' ";
        $query .= "           AND exam_type    = '2' ";
        $query .= "           and testsubclasscd IN ('".implode("','",$subclasscd2)."') ";
        $query .= "        GROUP BY receptno, exam_type) T1  ";
        $query .= "        LEFT OUTER JOIN ";
                            /* 4科目型の人の４科目分の出欠フラグ、合計点、平均点 */ 
        $query .= "       (SELECT receptno ";
        $query .= "              ,exam_type ";
        $query .= "              ,CASE WHEN SUM(SMALLINT(attend_flg)) = 4 THEN '1' ELSE '0' END AS attend_flg ";
        $query .= "              ,SUM(score)                    AS total4 ";
        $query .= "              ,ROUND(AVG(DECIMAL(score)),1)  AS average4 ";
        $query .= "          FROM entexam_score_dat ";
        $query .= "         where entexamyear  = '".$model->examyear."'  ";
        $query .= "           AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "           AND testdiv      = '".$model->testdiv."' ";
        $query .= "           AND exam_type    = '2' ";
        $query .= "           and testsubclasscd IN ('".implode("','",$subclasscd4)."') ";
        $query .= "        GROUP BY receptno, exam_type) T2 ";
        $query .= "         ON T1.receptno       = T2.receptno ";
        $query .= "        AND T1.exam_type      = T2.exam_type "; 
        $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno ";
        $query .= "            AND TT0.exam_type = TT1.exam_type ";
        /***/
        //加点算出対象入試の場合、合計に加点を含める。
        //◆加点条件
        //  １月中に実施した入試（KATENDIV=2）の受験回数により加点する。
        //      ・２回受験　→　５点
        //      ・３回受験　→　10点
        if ($katendiv == "1") {
            $query .= "     LEFT JOIN ( ";
            $query .= "         SELECT ";
            $query .= "             T1.EXAM_TYPE, ";
            $query .= "             T1.RECEPTNO, ";
            $query .= "             T1.EXAMNO, ";
            $query .= "             CASE WHEN VALUE(L0.CNT, 0) + VALUE(L1.CNT, 0) + VALUE(L2.CNT, 0) + VALUE(L3.CNT, 0) = 3 THEN 10 ";
            $query .= "                  WHEN VALUE(L0.CNT, 0) + VALUE(L1.CNT, 0) + VALUE(L2.CNT, 0) + VALUE(L3.CNT, 0) = 2 THEN 5 ";
            $query .= "                  ELSE NULL ";
            $query .= "             END AS KATEN ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT T1 ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.EXAMNO = T1.EXAMNO ";
            $query .= "             LEFT JOIN ( ";
            $query .= "                 SELECT ";
            $query .= "                     T1.EXAMNO, ";
            $query .= "                     COUNT(*) AS CNT ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_RECEPT_DAT T1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                     AND T1.TESTDIV IN (SELECT W1.TESTDIV FROM ENTEXAM_TESTDIV_MST W1 WHERE W1.ENTEXAMYEAR = '".$model->examyear."' AND W1.KATENDIV = '2') ";
            $query .= "                 GROUP BY ";
            $query .= "                     T1.EXAMNO ";
            $query .= "             ) L0 ON L0.EXAMNO = T1.EXAMNO ";
            $query .= "             LEFT JOIN ( ";
            $query .= "                 SELECT ";
            $query .= "                     T1.EXAMNO, ";
            $query .= "                     COUNT(*) AS CNT ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_RECEPT_DAT T1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                     AND T1.TESTDIV IN (SELECT W1.TESTDIV FROM ENTEXAM_TESTDIV_MST W1 WHERE W1.ENTEXAMYEAR = '".$model->examyear."' AND W1.KATENDIV = '2') ";
            $query .= "                 GROUP BY ";
            $query .= "                     T1.EXAMNO ";
            $query .= "             ) L1 ON L1.EXAMNO = T2.RECOM_EXAMNO1 ";
            $query .= "             LEFT JOIN ( ";
            $query .= "                 SELECT ";
            $query .= "                     T1.EXAMNO, ";
            $query .= "                     COUNT(*) AS CNT ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_RECEPT_DAT T1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                     AND T1.TESTDIV IN (SELECT W1.TESTDIV FROM ENTEXAM_TESTDIV_MST W1 WHERE W1.ENTEXAMYEAR = '".$model->examyear."' AND W1.KATENDIV = '2') ";
            $query .= "                 GROUP BY ";
            $query .= "                     T1.EXAMNO ";
            $query .= "             ) L2 ON L2.EXAMNO = T2.RECOM_EXAMNO2 ";
            $query .= "             LEFT JOIN ( ";
            $query .= "                 SELECT ";
            $query .= "                     T1.EXAMNO, ";
            $query .= "                     COUNT(*) AS CNT ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_RECEPT_DAT T1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                     AND T1.TESTDIV IN (SELECT W1.TESTDIV FROM ENTEXAM_TESTDIV_MST W1 WHERE W1.ENTEXAMYEAR = '".$model->examyear."' AND W1.KATENDIV = '2') ";
            $query .= "                 GROUP BY ";
            $query .= "                     T1.EXAMNO ";
            $query .= "             ) L3 ON L3.EXAMNO = T2.RECOM_EXAMNO3 ";
            $query .= "         WHERE ";
            $query .= "             T1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "             AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND T1.TESTDIV = '".$model->testdiv."' ";
            $query .= "     ) TT2 ON TT0.RECEPTNO  = TT2.RECEPTNO ";
            $query .= "          AND TT0.EXAM_TYPE = TT2.EXAM_TYPE ";
        }
        /***/
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


        /* 席次を求める */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, total_rank2, div_rank2, total_rank4, div_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "         SELECT TT0.examno, TT2.total_rank2, TT3.div_rank2, TT4.total_rank4, TT5.div_rank4, '".STAFFCD."',SYSDATE() ";
        $query .= "           FROM entexam_applicantbase_dat TT9 ";
        $query .= "                INNER JOIN entexam_recept_dat TT0 ";
        $query .= "                        ON TT9.entexamyear  = TT0.entexamyear AND ";
        $query .= "                           TT9.examno       = TT0.examno  ";
        $query .= "                LEFT OUTER JOIN ";
        $query .= "                ( ";
                                    /* ２科目全体席次（受験型にかかわらず4科目型の人の2科目分も含む） */ 
        $query .= "                 SELECT T1.examno ";
        $query .= "                       ,CASE WHEN t1.total2 IS NOT NULL THEN COUNT(t2.total2)+1 END as total_rank2 ";
        $query .= "                   FROM (SELECT examno, total2 ";
        $query .= "                           FROM entexam_recept_dat ";
        $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) t1  ";
        $query .= "                        LEFT OUTER JOIN ";
        $query .= "                        (SELECT examno, total2 ";
        $query .= "                           FROM entexam_recept_dat ";
        $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) t2 ON t1.total2 < t2.total2 ";
        $query .= "                  GROUP BY T1.examno, T1.total2 ";
        $query .= "                ) TT2 ON TT0.examno = TT2.examno ";
        $query .= "                 LEFT OUTER JOIN ";
        $query .= "                ( ";
                                    /* ２科目志望区分席次 */
        $query .= "                 SELECT T1.examno ";
        $query .= "                       ,T1.desirediv ";
        $query .= "                       ,CASE WHEN t1.total2 IS NOT NULL THEN COUNT(t2.total2)+1 END AS div_rank2 ";
        $query .= "                   FROM (SELECT T1.examno, T2.desirediv, T1.total2 ";
        $query .= "                           FROM entexam_recept_dat        T1 ";
        $query .= "                               ,entexam_applicantbase_dat T2 ";
        $query .= "                          WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "                            AND T1.examno       = T2.examno ";
        $query .= "                            AND T1.entexamyear  = '".$model->examyear."'";
        $query .= "                            AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND T1.testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) t1  ";
        $query .= "                        LEFT OUTER JOIN ";
        $query .= "                        (SELECT T1.examno, T2.desirediv, T1.total2 ";
        $query .= "                           FROM entexam_recept_dat        T1 ";
        $query .= "                               ,entexam_applicantbase_dat T2 ";
        $query .= "                          WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "                            AND T1.examno       = T2.examno ";
        $query .= "                            AND T1.entexamyear  = '".$model->examyear."'";
        $query .= "                            AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND T1.testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) t2 ON T1.desirediv = T2.desirediv AND ";
        $query .= "                                T1.total2    < T2.total2 ";
        $query .= "                  GROUP BY T1.examno, ";
        $query .= "                           T1.desirediv, ";
        $query .= "                           T1.total2";
        $query .= "                ) TT3 ON TT0.examno = TT3.examno AND TT9.desirediv = TT3.desirediv ";
        $query .= "                 LEFT OUTER JOIN  ";
        $query .= "                ( ";
                                    /* ４科目全体席次（2科目型の人の得点は除く） */ 
        $query .= "                 SELECT T1.examno ";
        $query .= "                       ,CASE WHEN t1.total4 IS NOT NULL THEN COUNT(t2.total4)+1 END as total_rank4 ";
        $query .= "                   FROM (SELECT examno, total4 ";
        $query .= "                           FROM entexam_recept_dat ";
        $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) t1  ";
        $query .= "                        LEFT OUTER JOIN  ";
        $query .= "                        (SELECT examno, total4 ";
        $query .= "                           FROM entexam_recept_dat ";
        $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) t2 ON t1.total4 < t2.total4 ";
        $query .= "                  GROUP BY T1.examno, T1.total4 ";
        $query .= "                ) TT4 ON TT0.examno = TT4.examno  ";
        $query .= "                 LEFT OUTER JOIN ";
        $query .= "                ( ";
                                    /* ４科目志望区分席次（2科目型の人の得点は除く） */ 
        $query .= "                 SELECT T1.examno ";
        $query .= "                       ,T1.desirediv ";
        $query .= "                       ,CASE WHEN t1.total4 IS NOT NULL THEN COUNT(t2.total4)+1 END AS div_rank4 ";
        $query .= "                   FROM (SELECT T1.examno, T2.desirediv, T1.total4 ";
        $query .= "                           FROM entexam_recept_dat        T1 ";
        $query .= "                               ,entexam_applicantbase_dat T2 ";
        $query .= "                          WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "                            AND T1.examno       = T2.examno ";
        $query .= "                            AND T1.entexamyear  = '".$model->examyear."'";
        $query .= "                            AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND T1.testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) t1  ";
        $query .= "                        LEFT OUTER JOIN ";
        $query .= "                        (SELECT T1.examno, T2.desirediv, T1.total4 ";
        $query .= "                           FROM entexam_recept_dat        T1 ";
        $query .= "                               ,entexam_applicantbase_dat T2 ";
        $query .= "                          WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "                            AND T1.examno       = T2.examno ";
        $query .= "                            AND T1.entexamyear  = '".$model->examyear."'";
        $query .= "                            AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND T1.testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) t2 ON T1.desirediv = T2.desirediv AND  ";
        $query .= "                                T1.total4    < T2.total4 ";
        $query .= "                  GROUP BY T1.examno, ";
        $query .= "                           T1.desirediv, ";
        $query .= "                           T1.total4";
        $query .= "                ) TT5 ON TT0.examno = TT5.examno AND TT9.desirediv = TT5.desirediv ";
        $query .= "          WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "            AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "            AND TT0.examno       = entexam_recept_dat.examno ";
        $query .= "       ) ";
        $query .= " WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND testdiv      = '".$model->testdiv."' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }


        /*                  */
        /* 得点データの更新 */
        /*                  */
        /* 偏差値 受験型に関わらず受験科目ごとに算出 */
        $query  = "UPDATE entexam_score_dat  ";
        $query .= "   SET std_score =  ";
        $query .= "     (SELECT ROUND(50 + 10 * (T1.score - T2.avg_score) / CASE WHEN T2.std_score = 0 THEN 1 ELSE T2.std_score END,1) ";   //偏差値(小数第2位で四捨五入)
        $query .= "        FROM entexam_score_dat T1 ";
        $query .= "            ,(SELECT testsubclasscd ";
        $query .= "                    ,ROUND(STDDEV(score),1)       AS std_score ";   //標準偏差
        $query .= "                    ,ROUND(AVG(DECIMAL(score)),1) AS avg_score ";   //平均
        $query .= "                FROM entexam_score_dat ";
        $query .= "               WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                 AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "                 AND testdiv      = '".$model->testdiv."' ";
        $query .= "             GROUP BY testsubclasscd) T2 ";
        $query .= "       WHERE T1.testsubclasscd = T2.testsubclasscd ";
        $query .= "         AND T1.entexamyear    = '".$model->examyear."' ";
        $query .= "         AND T1.applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "         AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "         AND T1.exam_type      = entexam_score_dat.exam_type ";
        $query .= "         AND T1.receptno       = entexam_score_dat.receptno ";
        $query .= "         AND T1.testsubclasscd = entexam_score_dat.testsubclasscd ";
        $query .= "     ), ";
        $query .= "      registercd = '".STAFFCD."', ";
        $query .= "      updated    = SYSDATE() ";
        $query .= " WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND testdiv      = '".$model->testdiv."' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 受験科目ごとの順位 */
        $query  = "UPDATE entexam_score_dat ";
        $query .= "   SET rank = (";
        $query .= "                 SELECT CASE WHEN T1.score IS NOT NULL THEN COUNT(T2.score)+1 END AS rank ";
        $query .= "                   FROM (SELECT * ";
        $query .= "                           FROM entexam_score_dat ";
        $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) T1   ";
        $query .= "                          LEFT OUTER JOIN   ";
        $query .= "                        (SELECT * ";
        $query .= "                           FROM entexam_score_dat ";
        $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) T2 ON T1.testsubclasscd = T2.testsubclasscd   ";
        $query .= "                            AND T1.score          < T2.score  ";
        $query .= "                  WHERE T1.exam_type      = entexam_score_dat.exam_type ";
        $query .= "                    AND T1.receptno       = entexam_score_dat.receptno ";
        $query .= "                    AND T1.testsubclasscd = entexam_score_dat.testsubclasscd ";
        $query .= "                  GROUP BY T1.receptno, T1.score, t1.testsubclasscd ";
        $query .= "              ), ";
        $query .= "       registercd = '".STAFFCD."', ";
        $query .= "       updated    = SYSDATE() ";
        $query .= " WHERE entexamyear = '".$model->examyear."'";
        $query .= "   AND testdiv     = '".$model->testdiv."'";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " DELETE FROM ENTEXAM_SCORE_SPARE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV = '".$model->testdiv."' ";
        $db->query($query);

        $query  = " INSERT INTO ENTEXAM_SCORE_SPARE_DAT ";
        $query .= " (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO, TESTSUBCLASSCD, SEQ, SCORE1) ";
        $query .= " WITH ALL_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     'A' AS TESTSUBCLASSCD, ";
        $query .= "     '001' AS SEQ, ";
        $query .= "     SUM(VALUE(T1.SCORE, 0)) AS SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->testdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     '001' AS SEQ, ";
        $query .= "     T1.SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->testdiv."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.SEQ, ";
        //加点算出対象入試の場合、合計に加点を含める。
        if ($katendiv == "1") {
            $query .= "     T1.SCORE + VALUE(T2.KATEN, 0) ";
        } else {
            $query .= "     T1.SCORE ";
        }
        $query .= " FROM ";
        $query .= "     ALL_T T1 ";
        if ($katendiv == "1") {
            $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ";
            $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
            $query .= "         AND T2.EXAM_TYPE = T1.EXAM_TYPE ";
            $query .= "         AND T2.RECEPTNO = T1.RECEPTNO ";
        }
        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }
        $db->popExpect();
//        $db->commit();
        Query::dbCheckIn($db); 
        return DB_OK;
    } 
}
?>

