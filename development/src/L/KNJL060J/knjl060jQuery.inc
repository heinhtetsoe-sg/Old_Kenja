<?php

require_once('for_php7.php');

class knjl060jquery extends Query {

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

        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }

        //エラー無視
        $db->expectError();

        /*                  */
        /* 得点データの更新 */
        /*                  */
        /* 偏差値・順位 受験型に関わらず受験科目ごとに算出 */
        $std_score = "DECIMAL(ROUND(50 + 10 * (T1.score - T2.avg_score) / CASE WHEN T2.std_score = 0 THEN 1 ELSE T2.std_score END,2), 5, 2)";
        $query  = "      SELECT T1.APPLICANTDIV, T1.EXAM_TYPE, T1.RECEPTNO, T1.TESTSUBCLASSCD, ";
        $query .= "             ".$std_score." as std_score ";   //偏差値(小数第3位で四捨五入)
        $query .= "            ,RANK() OVER(PARTITION BY T1.TESTSUBCLASSCD ORDER BY ".$std_score." DESC) as rank ";
        $query .= "            ,'".STAFFCD."' as registercd ";
        $query .= "            ,SYSDATE() as updated ";
        $query .= "        FROM entexam_score_dat T1 ";
        $query .= "            ,(SELECT testsubclasscd ";
        $query .= "                    ,STDDEV(score)       AS std_score ";   //標準偏差
        $query .= "                    ,AVG(DECIMAL(score)) AS avg_score ";   //平均
        $query .= "                FROM entexam_score_dat ";
        $query .= "               WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                 AND testdiv      = '".$model->testdiv."' ";
        $query .= "             GROUP BY testsubclasscd) T2 ";
        $query .= "       WHERE T1.testsubclasscd = T2.testsubclasscd ";
        $query .= "         AND T1.entexamyear    = '".$model->examyear."' ";
        $query .= "         AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "       ORDER BY T1.testsubclasscd, 6 ";
        $result_list = $db->query($query);
        //エラー時後処理
        $result_list = $db->query($query);
        if (DB::isError($result_list)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result_list->getCode();
        }
        while ($row = $result_list->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data = array();
            $data["STD_SCORE"][NUMBER]  = $row["STD_SCORE"];
            $data["RANK"][NUMBER]       = $row["RANK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE entexamyear   = '".$model->examyear."' ";
            $where .= "   AND testdiv       = '".$model->testdiv."' ";
            $where .= "   AND applicantdiv  = '".$row["APPLICANTDIV"]."' ";
            $where .= "   AND exam_type     = '".$row["EXAM_TYPE"]."' ";
            $where .= "   AND receptno      = '".$row["RECEPTNO"]."' ";
            $where .= "   AND testsubclasscd = '".$row["TESTSUBCLASSCD"]."' ";
            $query = Query::updateSQL($data, "entexam_score_dat", $where);
            //エラー時後処理
            $result = $db->query($query);
            if (DB::isError($result)) {
                $db->popExpect();
                Query::dbCheckIn($db);
                return $result->getCode();
            }
        }

        /*                  */
        /* 受付データの更新 */
        /*                  */
        /* 判定偏差値・判定偏差値区分 入試区分ごとに算出 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, judge_deviation, judge_deviation_div, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT  TT0.EXAMNO ";
        $query .= "       ,CASE WHEN TT0.EXAM_TYPE = '2' AND TT1.JUDGE_DEVIATION < TT2.JUDGE_DEVIATION ";
        $query .= "             THEN TT2.JUDGE_DEVIATION ELSE TT1.JUDGE_DEVIATION END AS JUDGE_DEVIATION ";//判定偏差値
        $query .= "       ,CASE WHEN TT0.EXAM_TYPE = '2' AND TT1.JUDGE_DEVIATION < TT2.JUDGE_DEVIATION ";
        $query .= "             THEN TT2.JUDGE_DEVIATION_DIV ELSE TT1.JUDGE_DEVIATION_DIV END AS JUDGE_DEVIATION_DIV ";//判定偏差値区分
        $query .= "       ,'".STAFFCD."' ";
        $query .= "       ,SYSDATE() ";
        $query .= "  FROM  ENTEXAM_RECEPT_DAT TT0 ";
                           /* ４科平均偏差値 ４科型・２科型・３科型を含む */
        $query .= "        LEFT JOIN ( ";
        $query .= "            SELECT  APPLICANTDIV,EXAM_TYPE,RECEPTNO,ROUND(AVG(STD_SCORE),1) AS JUDGE_DEVIATION,'2' AS JUDGE_DEVIATION_DIV ";
        $query .= "              FROM  ENTEXAM_SCORE_DAT ";
        $query .= "             WHERE  ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "               AND  TESTDIV      = '".$model->testdiv."' ";
        $query .= "            GROUP BY APPLICANTDIV,EXAM_TYPE,RECEPTNO ";
        $query .= "        ) TT1 ON TT1.APPLICANTDIV   = TT0.APPLICANTDIV ";
        $query .= "             AND TT1.EXAM_TYPE      = TT0.EXAM_TYPE ";
        $query .= "             AND TT1.RECEPTNO       = TT0.RECEPTNO ";
                           /* ３科平均偏差値 ４科型のみ */
        $query .= "        LEFT JOIN ( ";
        $query .= "            SELECT  APPLICANTDIV,EXAM_TYPE,RECEPTNO,ROUND(AVG(STD_SCORE),1) AS JUDGE_DEVIATION,'1' AS JUDGE_DEVIATION_DIV ";
        $query .= "              FROM (SELECT  APPLICANTDIV,EXAM_TYPE,RECEPTNO,'9' AS TESTSUBCLASSCD,MAX(STD_SCORE) AS STD_SCORE ";
        $query .= "                      FROM  ENTEXAM_SCORE_DAT ";
        $query .= "                     WHERE  ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                       AND  TESTDIV      = '".$model->testdiv."' ";
        $query .= "                       AND  TESTSUBCLASSCD IN ('4','5') ";
        $query .= "                    GROUP BY APPLICANTDIV,EXAM_TYPE,RECEPTNO ";
        $query .= "                    UNION ALL ";
        $query .= "                    SELECT  APPLICANTDIV,EXAM_TYPE,RECEPTNO,TESTSUBCLASSCD,STD_SCORE ";
        $query .= "                      FROM  ENTEXAM_SCORE_DAT ";
        $query .= "                     WHERE  ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                       AND  TESTDIV      = '".$model->testdiv."' ";
        $query .= "                       AND  TESTSUBCLASSCD IN ('2','3') ) T1 ";
        $query .= "            GROUP BY APPLICANTDIV,EXAM_TYPE,RECEPTNO ";
        $query .= "        ) TT2 ON TT2.APPLICANTDIV   = TT0.APPLICANTDIV ";
        $query .= "             AND TT2.EXAM_TYPE      = TT0.EXAM_TYPE ";
        $query .= "             AND TT2.RECEPTNO       = TT0.RECEPTNO ";
        $query .= " WHERE  TT0.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "   AND  TT0.TESTDIV      = '".$model->testdiv."' ";
        $query .= "   AND  TT0.applicantdiv = entexam_recept_dat.applicantdiv ";
        $query .= "   AND  TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND  TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        /* 全科目受験フラグ・全科目合計・全科目平均・全科目順位 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, total_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT1.attend_all_flg ";     //全科目受験フラグ
        $query .= "      ,TT1.total4 ";             //全科目合計
        $query .= "      ,TT1.average4 ";           //全科目平均
        $query .= "      ,TT1.rank4 ";              //全科目順位（受験型ごと）
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT OUTER JOIN (";
                            /* ２科型 */ 
        $query .= "         SELECT t1.applicantdiv";
        $query .= "               ,t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN SUM(SMALLINT(t1.attend_flg)) >= 2 THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "               ,SUM(t1.score)                    AS total4";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average4";
        $query .= "               ,RANK() OVER(ORDER BY SUM(t1.score) DESC) as rank4";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.exam_type    = '1'";
        $query .= "         GROUP BY t1.applicantdiv, t1.receptno, t1.exam_type";
                            /* ４科型 */ 
        $query .= "         UNION ALL ";
        $query .= "         SELECT t1.applicantdiv";
        $query .= "               ,t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN SUM(SMALLINT(t1.attend_flg)) >= 4 THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "               ,SUM(t1.score)                    AS total4";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average4";
        $query .= "               ,RANK() OVER(ORDER BY SUM(t1.score) DESC) as rank4";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.exam_type    = '2'";
        $query .= "         GROUP BY t1.applicantdiv, t1.receptno, t1.exam_type";
                            /* ３科型 */ 
        $query .= "         UNION ALL ";
        $query .= "         SELECT t1.applicantdiv";
        $query .= "               ,t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN SUM(SMALLINT(t1.attend_flg)) >= 3 THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "               ,SUM(t1.score)                    AS total4";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average4";
        $query .= "               ,RANK() OVER(ORDER BY SUM(t1.score) DESC) as rank4";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.exam_type    = '3'";
        $query .= "         GROUP BY t1.applicantdiv, t1.receptno, t1.exam_type";
        $query .= "       ) TT1 ON TT0.applicantdiv   = TT1.applicantdiv ";
        $query .= "            AND TT0.receptno       = TT1.receptno ";
        $query .= "            AND TT0.exam_type      = TT1.exam_type ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.applicantdiv = entexam_recept_dat.applicantdiv ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        $db->popExpect();
        Query::dbCheckIn($db); 
        return DB_OK;
    } 
}
?>

