<?php

require_once('for_php7.php');

class knjl060mquery extends Query {

    //名称
    function GetName($namecd1, $ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
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
        //答案用紙得点データチェック
        $query  = "SELECT COUNT(*) FROM entexam_score_detail_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'"; 
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }

//        $db->autoCommit(false);

        //エラー無視
        $db->expectError();

        /*                  */
        /* 得点データの作成 */
        /*                  */

        //一旦、レコードを削除
        $query  = "DELETE FROM entexam_score_dat ";
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
        //答案用紙得点データから得点データを作成
        $query  = "INSERT INTO entexam_score_dat ";
        $query .= "SELECT '".$model->examyear."' as entexamyear ";
        $query .= "      ,'".$model->applicantdiv."' as applicantdiv ";
        $query .= "      ,'".$model->testdiv."' as testdiv ";
        $query .= "      ,'1' as exam_type "; //固定値
        $query .= "      ,t1.receptno ";
        $query .= "      ,t1.testsubclasscd ";
        $query .= "      ,CASE WHEN COUNT(t1.score) > 0 THEN '1' ELSE '0' END AS attend_flg ";
        $query .= "      ,SUM(t1.score) AS score "; //答案用紙の合計
        $query .= "      ,cast(null as decimal(5,2)) as std_score ";
        $query .= "      ,cast(null as smallint) as rank ";
        $query .= "      ,'".STAFFCD."' as registercd ";
        $query .= "      ,SYSDATE() as updated ";
        $query .= "  FROM entexam_score_detail_dat t1 ";
        $query .= " where t1.entexamyear  = '".$model->examyear."'  ";
        $query .= "   AND t1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND t1.testdiv      = '".$model->testdiv."' ";
        $query .= "GROUP BY t1.receptno,t1.testsubclasscd ";
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

        /* 受験科目ごとの偏差値・順位を求める */
        $std_score = "ROUND(50 + 10 * (T1.score - T2.avg_score) / CASE WHEN T2.std_score = 0 THEN 1 ELSE T2.std_score END,1)";
        $query  = "UPDATE entexam_score_dat  ";
        $query .= "   SET (std_score, rank, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT1.std_score ";
        $query .= "      ,TT1.rank ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_score_dat TT0 ";
        $query .= "       LEFT OUTER JOIN ( ";
        $query .= "      SELECT T1.receptno, T1.exam_type, T1.testsubclasscd ";
        $query .= "            ,".$std_score." as std_score ";   //偏差値(小数第2位で四捨五入)
        $query .= "            ,RANK() OVER(PARTITION BY T1.testsubclasscd ORDER BY T1.score DESC) as rank ";
        $query .= "            ,'".STAFFCD."' as registercd ";
        $query .= "            ,SYSDATE() as updated ";
        $query .= "        FROM entexam_score_dat T1 ";
        $query .= "            ,(SELECT testsubclasscd ";
        $query .= "                    ,ROUND(STDDEV(score),2)       AS std_score ";   //標準偏差(小数第3位で四捨五入)
        $query .= "                    ,ROUND(AVG(DECIMAL(score)),1) AS avg_score ";   //平均    (小数第2位で四捨五入)
        $query .= "                FROM entexam_score_dat ";
        $query .= "               WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                 AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "                 AND testdiv      = '".$model->testdiv."' ";
        $query .= "             GROUP BY testsubclasscd) T2 ";
        $query .= "       WHERE T1.testsubclasscd = T2.testsubclasscd ";
        $query .= "         AND T1.entexamyear    = '".$model->examyear."' ";
        $query .= "         AND T1.applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "         AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "         AND T1.score IS NOT NULL";
        $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno ";
        $query .= "            AND TT0.exam_type = TT1.exam_type ";
        $query .= "            AND TT0.testsubclasscd = TT1.testsubclasscd ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type      = entexam_score_dat.exam_type ";
        $query .= "   AND TT0.receptno       = entexam_score_dat.receptno ";
        $query .= "   AND TT0.testsubclasscd = entexam_score_dat.testsubclasscd ";
        $query .= "     ) ";
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
        /* 受付データの更新 */
        /*                  */

        /* 総点・偏差値合計・偏差値(理社６割)合計を求める */
        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, judge_deviation, link_judge_deviation, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT1.attend_all_flg ";         //全科目受験フラグ
        $query .= "      ,TT1.total4 ";                 //全科目合計
        $query .= "      ,TT1.average4 ";               //全科目平均
        $query .= "      ,TT1.judge_deviation ";        //偏差値合計(小数第2位で四捨五入)
        $query .= "      ,TT1.link_judge_deviation ";   //偏差値(理社６割)合計(小数第2位で四捨五入)
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
                            /* 全科目分の出欠フラグ、合計点、平均点 */ 
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN COUNT(t1.score) >= 4 THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "               ,SUM(t1.score)                    AS total4";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average4";
        $query .= "               ,ROUND(SUM(t1.std_score),1) AS judge_deviation ";
        $query .= "               ,ROUND(SUM(t1.std_score * value(L1.rate,100) / 100),1) AS link_judge_deviation ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "                LEFT JOIN entexam_perfect_mst L1 ON L1.entexamyear = t1.entexamyear ";
        $query .= "                                                AND L1.applicantdiv = t1.applicantdiv ";
        $query .= "                                                AND L1.testdiv = t1.testdiv ";
        $query .= "                                                AND L1.testsubclasscd = t1.testsubclasscd ";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
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
        /* 総点順位を求める */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, total_rank4, judge_deviation_rank, link_judge_deviation_rank, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT1.total_rank4 ";
        $query .= "      ,TT1.judge_deviation_rank ";
        $query .= "      ,TT1.link_judge_deviation_rank ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
                            /* ４科目全体席次 */ 
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(ORDER BY t1.total4 DESC) AS total_rank4";
        $query .= "               ,RANK() OVER(ORDER BY t1.judge_deviation DESC) AS judge_deviation_rank";
        $query .= "               ,RANK() OVER(ORDER BY t1.link_judge_deviation DESC) AS link_judge_deviation_rank";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.total4 IS NOT NULL";
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

        /*                    */
        /* 志願者データの作成 */
        /*                    */

        //一旦、レコードを削除
        $query  = "DELETE FROM entexam_desire_dat ";
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
        //受付データから志願者データを作成
        $query  = "INSERT INTO entexam_desire_dat ";
        $query .= "SELECT '".$model->examyear."' as entexamyear ";
        $query .= "      ,'".$model->applicantdiv."' as applicantdiv ";
        $query .= "      ,'".$model->testdiv."' as testdiv ";
        $query .= "      ,'1' as exam_type "; //固定値
        $query .= "      ,t1.examno  ";
        $query .= "      ,'1' as applicant_div  ";
        $query .= "      ,case when t1.total_rank4 is not null then '1' else '2' end as examinee_div  ";
        $query .= "      ,'".STAFFCD."' as registercd ";
        $query .= "      ,SYSDATE() as updated ";
        $query .= "  FROM entexam_recept_dat t1 ";
        $query .= " where t1.entexamyear  = '".$model->examyear."'  ";
        $query .= "   AND t1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND t1.testdiv      = '".$model->testdiv."' ";
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

