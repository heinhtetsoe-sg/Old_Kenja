<?php

require_once('for_php7.php');

class knjl060hquery extends Query {

    //名称
    function GetName($namecd1, $ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //科目
    function getSubclasscd($ObjYear, $applicantdiv)
    {
        $query = "SELECT TESTSUBCLASSCD FROM ENTEXAM_TESTSUBCLASSCD_DAT
                   WHERE ENTEXAMYEAR  = '".$ObjYear."'
                     AND APPLICANTDIV = '".$applicantdiv."'
                   ORDER BY TESTSUBCLASSCD";
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

        $subclasscd4 = array();

        //科目型の科目を取得
        $result = $db->query(knjl060hQuery::getSubclasscd($model->examyear, $model->applicantdiv));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd4[] = $row["TESTSUBCLASSCD"];
        }

        //エラー無視
        $db->expectError();
        /*                  */
        /* 受付データの更新 */
        /*                  */
        //高校一般のみ、加算点を含まない合計・平均・席次を算出。（３科合計・平均・席次）
        if ($model->applicantdiv == "2") {
            //３科合計・平均
            $query  = "UPDATE entexam_recept_dat ";
            $query .= "   SET (examno, total2, avarage2, registercd, updated) ";
            $query .= "     = ( ";
            $query .= "SELECT TT0.examno ";
            $query .= "      ,TT1.total2 ";             //全科目合計
            $query .= "      ,TT1.average2 ";           //全科目平均
            $query .= "      ,'".STAFFCD."' ";
            $query .= "      ,SYSDATE() ";
            $query .= "  FROM entexam_recept_dat TT0 ";
            $query .= "       LEFT OUTER JOIN ";
            $query .= "        (SELECT t1.receptno";
            $query .= "               ,t1.exam_type";
            $query .= "               ,CASE WHEN SUM(SMALLINT(t1.attend_flg)) >= ".get_count($subclasscd4)." THEN '1' ELSE '0' END AS attend_all_flg";
            $query .= "               ,SUM(t1.score)                    AS total2";
            $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average2";
            $query .= "           FROM (";
            $query .= "             SELECT W0.receptno";
            $query .= "                   ,W0.exam_type";
            $query .= "                   ,W0.attend_flg";
            $query .= "                   ,W0.score";
            $query .= "               FROM entexam_score_dat W0";
            $query .= "              where W0.entexamyear  = '".$model->examyear."' ";
            $query .= "                AND W0.applicantdiv = '".$model->applicantdiv."'";
            $query .= "                AND W0.testdiv      = '".$model->testdiv."'";
            $query .= "                and W0.testsubclasscd IN ('".implode("','",$subclasscd4)."')";
            $query .= "                 ) t1";
            $query .= "         GROUP BY t1.receptno, t1.exam_type";
            $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno ";
            $query .= "            AND TT0.exam_type = TT1.exam_type ";
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

            //３科席次
            $query  = "UPDATE entexam_recept_dat ";
            $query .= "   SET (examno, total_rank2, registercd, updated) ";
            $query .= "     = ( ";
            $query .= "         SELECT TT0.examno, TT4.total_rank2, '".STAFFCD."',SYSDATE() ";
            $query .= "           FROM entexam_applicantbase_dat TT9 ";
            $query .= "                INNER JOIN entexam_recept_dat TT0 ";
            $query .= "                        ON TT9.entexamyear  = TT0.entexamyear AND ";
            $query .= "                           TT9.examno       = TT0.examno  ";
            $query .= "                 LEFT OUTER JOIN  ";
            $query .= "                ( ";
            $query .= "                 SELECT T1.examno ";
            $query .= "                       ,CASE WHEN t1.total2 IS NOT NULL THEN COUNT(t2.total2)+1 END as total_rank2 ";
            $query .= "                   FROM (SELECT examno, total2 ";
            $query .= "                           FROM entexam_recept_dat ";
            $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
            $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "                            AND testdiv      = '".$model->testdiv."' ";
            $query .= "                        ) t1  ";
            $query .= "                        LEFT OUTER JOIN  ";
            $query .= "                        (SELECT examno, total2 ";
            $query .= "                           FROM entexam_recept_dat ";
            $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
            $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "                            AND testdiv      = '".$model->testdiv."' ";
            $query .= "                        ) t2 ON t1.total2 < t2.total2 ";
            $query .= "                  GROUP BY T1.examno, T1.total2 ";
            $query .= "                ) TT4 ON TT0.examno = TT4.examno  ";
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
        }//if


        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT1.attend_all_flg ";     //全科目受験フラグ
        $query .= "      ,TT1.total4 ";             //全科目合計
        $query .= "      ,TT1.average4 ";           //全科目平均
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT OUTER JOIN ";
                            /* 全科目分の出欠フラグ、合計点、平均点 */ 
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN SUM(SMALLINT(t1.attend_flg)) >= ".get_count($subclasscd4)." THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "               ,SUM(t1.score)                    AS total4";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average4";
        $query .= "           FROM (";
        $query .= "             SELECT W0.receptno";
        $query .= "                   ,W0.exam_type";
        $query .= "                   ,W0.attend_flg";
        $query .= "                   ,W0.score";
        $query .= "               FROM entexam_score_dat W0";
        $query .= "              where W0.entexamyear  = '".$model->examyear."' ";
        $query .= "                AND W0.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                AND W0.testdiv      = '".$model->testdiv."'";
        $query .= "                and W0.testsubclasscd IN ('".implode("','",$subclasscd4)."')";
            //高校一般のみ加算点あり
        if ($model->applicantdiv == "2") {
            $query .= "         UNION ALL";
            $query .= "         SELECT  W1.RECEPTNO,W1.EXAM_TYPE,'0' AS ATTEND_FLG,W2.KASANTEN_ALL AS SCORE";
            $query .= "           FROM  ENTEXAM_RECEPT_DAT W1,";
            $query .= "                 ENTEXAM_APPLICANTCONFRPT_DAT W2";
            $query .= "               ,(SELECT W0.receptno";
            $query .= "                       ,W0.exam_type";
            $query .= "                   FROM entexam_score_dat W0";
            $query .= "                  where W0.entexamyear  = '".$model->examyear."' ";
            $query .= "                    AND W0.applicantdiv = '".$model->applicantdiv."'";
            $query .= "                    AND W0.testdiv      = '".$model->testdiv."'";
            $query .= "                    AND W0.attend_flg   = '1'";
            $query .= "                    and W0.testsubclasscd IN ('".implode("','",$subclasscd4)."')";
            $query .= "                  GROUP BY W0.receptno, W0.exam_type) W3";
            $query .= "          WHERE  W1.ENTEXAMYEAR='".$model->examyear."'";
            $query .= "            AND  W1.APPLICANTDIV='".$model->applicantdiv."'";
            $query .= "            AND  W1.TESTDIV='".$model->testdiv."'";
            $query .= "            AND  W2.ENTEXAMYEAR=W1.ENTEXAMYEAR";
            $query .= "            AND  W2.EXAMNO=W1.EXAMNO";
            $query .= "            AND  W2.KASANTEN_ALL IS NOT NULL";
            $query .= "            AND  W3.receptno=W1.RECEPTNO";
            $query .= "            AND  W3.exam_type=W1.EXAM_TYPE";
        }
        $query .= "                 ) t1";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno ";
        $query .= "            AND TT0.exam_type = TT1.exam_type ";
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
        $query .= "   SET (examno, total_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "         SELECT TT0.examno, TT4.total_rank4, '".STAFFCD."',SYSDATE() ";
        $query .= "           FROM entexam_applicantbase_dat TT9 ";
        $query .= "                INNER JOIN entexam_recept_dat TT0 ";
        $query .= "                        ON TT9.entexamyear  = TT0.entexamyear AND ";
        $query .= "                           TT9.examno       = TT0.examno  ";
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
        $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND testdiv     = '".$model->testdiv."'";

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

