<?php

require_once('for_php7.php');

class knjl060bQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($namecd1, $year, $namecd2 = "")
    {
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
        //中学は表示しない
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    public function executeQuery($model)
    {

        //DB接続
        $db = Query::dbCheckOut();

        //対象人数チェック
        $query  = "SELECT COUNT(*) FROM entexam_recept_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        if (strlen($model->special_reason_div)) {
            $query .= "   AND T1.RECEPTNO IN (";
            $query .= "         SELECT ";
            $query .= "             RECE.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT RECE ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "                   ON BASE.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
            $query .= "                  AND BASE.EXAMNO = RECE.EXAMNO ";
            $query .= "                  AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "         WHERE ";
            $query .= "             RECE.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "             AND RECE.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND RECE.TESTDIV = '".$model->testdiv."' ";
            $query .= "       )";
        }
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db);
            return false;
        }
        //得点データチェック
        $query  = "SELECT COUNT(*) FROM entexam_score_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        if (strlen($model->special_reason_div)) {
            $query .= "   AND T1.RECEPTNO IN (";
            $query .= "         SELECT ";
            $query .= "             RECE.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT RECE ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "                   ON BASE.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
            $query .= "                  AND BASE.EXAMNO = RECE.EXAMNO ";
            $query .= "                  AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "         WHERE ";
            $query .= "             RECE.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "             AND RECE.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND RECE.TESTDIV = '".$model->testdiv."' ";
            $query .= "       )";
        }
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db);
            return false;
        }

        //1:推薦３科目、2:一般５科目・・・全科目分受験フラグ用
        $testsubArray3 = array();
        $testsubArray5 = array();
        $query = knjl060bQuery::getNameMst("L009", $model->examyear, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE2"] == "1") {
                $testsubArray3[] = $row["VALUE"];
            }
            if ($row["NAMESPARE3"] == "1") {
                $testsubArray5[] = $row["VALUE"];
            }
        }
        $result->free();

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */

        /* 全科目分受験フラグ、合計点、平均点 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, total3, avarage3, total1, avarage1, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        //1:推薦入試 ３科
        //2:一般入試 ３科 OR ５科
        $query .= "      ,TT3.attend_all_flg3 AS attend_all_flg ";
        //３科目不足、５科目不足  ＮＵＬＬ
        //３科目あり、５科目不足  ３科目合計点
        //３科目あり、５科目あり  良い方の合計点
        $query .= "      ,CASE WHEN TT3.attend_all_flg3 != '1' THEN NULL ";
        $query .= "            WHEN TT3.attend_all_flg3  = '1' AND TT5.attend_all_flg5 != '1' THEN TT3.total3 ";
        $query .= "            WHEN TT3.average3 < TT5.average5 THEN TT5.total5 ELSE TT3.total3 ";
        $query .= "            END AS total4 ";
        //３科目不足、５科目不足  ＮＵＬＬ
        //３科目あり、５科目不足  ３科目平均点
        //３科目あり、５科目あり  良い方の平均点
        $query .= "      ,CASE WHEN TT3.attend_all_flg3 != '1' THEN NULL ";
        $query .= "            WHEN TT3.attend_all_flg3  = '1' AND TT5.attend_all_flg5 != '1' THEN TT3.average3 ";
        $query .= "            WHEN TT3.average3 < TT5.average5 THEN TT5.average5 ELSE TT3.average3 ";
        $query .= "            END AS avarage4 ";
        $query .= "      ,TT3.total3 AS total3 ";
        $query .= "      ,CASE WHEN TT3.attend_all_flg3 = '1' THEN TT3.average3 END AS avarage3 ";
        $query .= "      ,CASE WHEN TT2.testdiv1 = '2' THEN TT5.total5 END AS total1 ";
        $query .= "      ,CASE WHEN TT2.testdiv1 = '2' AND TT5.attend_all_flg5 = '1' THEN TT5.average5 END AS avarage1 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        //受験型 1:３科 2:５科
        $query .= "       LEFT JOIN entexam_applicantbase_dat TT2 ";
        $query .= "           ON  TT2.entexamyear  = TT0.entexamyear ";
        $query .= "           AND TT2.applicantdiv = TT0.applicantdiv";
        $query .= "           AND TT2.examno       = TT0.examno";
        //３科目 全科目分受験フラグ、合計点、平均点
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,CASE WHEN " .get_count($testsubArray3) . " <= COUNT(t1.score) THEN '1' ELSE '0' END AS attend_all_flg3 ";
        $query .= "               ,SUM(t1.score)                    AS total3 ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average3 ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.testsubclasscd IN ('" . implode("','", $testsubArray3) . "') ";
        $query .= "         GROUP BY t1.receptno ";
        $query .= "       ) TT3 ON TT0.receptno  = TT3.receptno ";
        //５科目 全科目分受験フラグ、合計点、平均点
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,CASE WHEN " .get_count($testsubArray5) . " <= COUNT(t1.score) THEN '1' ELSE '0' END AS attend_all_flg5 ";
        $query .= "               ,SUM(t1.score)                    AS total5 ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average5 ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.testsubclasscd IN ('" . implode("','", $testsubArray5) . "') ";
        $query .= "         GROUP BY t1.receptno ";
        $query .= "       ) TT5 ON TT0.receptno  = TT5.receptno ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'";
        if (strlen($model->special_reason_div)) {
            $query .= "   AND RECEPTNO IN (";
            $query .= "         SELECT ";
            $query .= "             RECE.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT RECE ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "                   ON BASE.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
            $query .= "                  AND BASE.EXAMNO = RECE.EXAMNO ";
            $query .= "                  AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "         WHERE ";
            $query .= "             RECE.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "             AND RECE.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND RECE.TESTDIV = '".$model->testdiv."' ";
            $query .= "       )";
        }

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            //echo "合計<BR>";
            //echo $query;
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }


        /* 席次 */
        //入試試験の科目が1つでも未受験があれば、順位対象外(NULLで更新)
        if (!strlen($model->special_reason_div)) {
            $query  = "UPDATE entexam_recept_dat ";
            $query .= "   SET (examno, total_rank4, total_rank3, total_rank1, registercd, updated) ";
            $query .= "     = ( ";
            $query .= "SELECT TT0.examno ";
            //total_rank4
            //1:推薦入試 ３科平均点の順位
            //2:一般入試 ３科OR５科の平均点の高い方の順位
            $query .= "      ,TT4.total_rank4 "; //母集団：入試区分全員
            $query .= "      ,TT3.total_rank3 "; //母集団：入試区分全員
            $query .= "      ,TT5.total_rank1 "; //母集団：受験型「2:５科」の者
            $query .= "      ,'".STAFFCD."' ";
            $query .= "      ,SYSDATE() ";
            $query .= "  FROM entexam_recept_dat TT0 ";
            //平均点の高い方
            $query .= "       LEFT JOIN ";
            $query .= "        (SELECT t1.receptno";
            $query .= "               ,t1.exam_type";
            $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.avarage4 DESC) AS total_rank4 ";
            $query .= "           FROM entexam_recept_dat t1";
            $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
            $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
//      $query .= "            AND t1.attend_all_flg = '1'"; //1:３科目受験
            $query .= "            AND t1.avarage4 IS NOT NULL ";
            $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
            $query .= "            AND TT0.exam_type = TT4.exam_type ";
            //３科目
            $query .= "       LEFT JOIN ";
            $query .= "        (SELECT t1.receptno";
            $query .= "               ,t1.exam_type";
            $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.avarage3 DESC) AS total_rank3 ";
            $query .= "           FROM entexam_recept_dat t1";
            $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
            $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
            $query .= "            AND t1.avarage3 IS NOT NULL ";
            $query .= "       ) TT3 ON TT0.receptno  = TT3.receptno ";
            $query .= "            AND TT0.exam_type = TT3.exam_type ";
            //５科目
            $query .= "       LEFT JOIN ";
            $query .= "        (SELECT t1.receptno";
            $query .= "               ,t1.exam_type";
            $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv  ORDER BY t1.avarage1 DESC) AS total_rank1 ";
            $query .= "           FROM entexam_recept_dat t1";
            $query .= "                inner join entexam_applicantbase_dat t2 ";
            $query .= "                     ON  t2.entexamyear  = t1.entexamyear ";
            $query .= "                     AND t2.applicantdiv = t1.applicantdiv";
            $query .= "                     AND t2.examno       = t1.examno";
            $query .= "                     AND t2.testdiv1     = '2' "; //受験型 1:３科 2:５科
            $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
            $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
            $query .= "            AND t1.avarage1 IS NOT NULL ";
            $query .= "       ) TT5 ON TT0.receptno  = TT5.receptno ";
            $query .= "            AND TT0.exam_type = TT5.exam_type ";
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
                //echo "席次<BR>";
                //echo $query;
                $db->popExpect();
                Query::dbCheckIn($db);
                return $result->getCode();
            }
        }//special_reason_div

        /* 欠席(一旦クリア) */
        $query  = " UPDATE entexam_recept_dat  ";
        $query .= "    SET judgediv = NULL  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND judgediv     = '3' ";
        if (strlen($model->special_reason_div)) {
            $query .= "   AND RECEPTNO IN (";
            $query .= "         SELECT ";
            $query .= "             RECE.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT RECE ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "                   ON BASE.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
            $query .= "                  AND BASE.EXAMNO = RECE.EXAMNO ";
            $query .= "                  AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "         WHERE ";
            $query .= "             RECE.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "             AND RECE.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND RECE.TESTDIV = '".$model->testdiv."' ";
            $query .= "       )";
        }

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 欠席 */
        //入試試験の科目が全て未受験の者は、3:未受験(欠席)で更新
        $query  = " UPDATE entexam_recept_dat  ";
        $query .= "    SET judgediv = '3'  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND examno IN ( ";
        $query .= "         SELECT TT0.examno  ";
        $query .= "           FROM entexam_recept_dat TT0  ";
        $query .= "          WHERE TT0.entexamyear  = '".$model->examyear."'  ";
        $query .= "            AND TT0.applicantdiv = '".$model->applicantdiv."'  ";
        $query .= "            AND TT0.testdiv      = '".$model->testdiv."'  ";
        $query .= "            AND TT0.total3 IS NULL ";
        $query .= "            AND TT0.total1 IS NULL ";
        $query .= "        ) ";
        if (strlen($model->special_reason_div)) {
            $query .= "   AND RECEPTNO IN (";
            $query .= "         SELECT ";
            $query .= "             RECE.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT RECE ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "                   ON BASE.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
            $query .= "                  AND BASE.EXAMNO = RECE.EXAMNO ";
            $query .= "                  AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "         WHERE ";
            $query .= "             RECE.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "             AND RECE.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND RECE.TESTDIV = '".$model->testdiv."' ";
            $query .= "       )";
        }

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 欠席(一旦クリア) */
        $query  = " UPDATE entexam_applicantbase_dat  ";
        $query .= "    SET judgement = NULL  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND judgement    = '3' ";
        if (strlen($model->special_reason_div)) {
            $query .= "    AND SPECIAL_REASON_DIV IS NOT NULL ";
        }

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " UPDATE entexam_applicantbase_dat  ";
        $query .= "    SET judgement = '3'  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND examno IN ( ";
        $query .= "         SELECT TT0.examno  ";
        $query .= "           FROM entexam_recept_dat TT0  ";
        $query .= "          WHERE TT0.entexamyear  = '".$model->examyear."'  ";
        $query .= "            AND TT0.applicantdiv = '".$model->applicantdiv."'  ";
        $query .= "            AND TT0.testdiv      = '".$model->testdiv."'  ";
        $query .= "            AND TT0.total3 IS NULL ";
        $query .= "            AND TT0.total1 IS NULL ";
        $query .= "        ) ";
        if (strlen($model->special_reason_div)) {
            $query .= "    AND SPECIAL_REASON_DIV IS NOT NULL ";
        }

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

