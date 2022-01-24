<?php

require_once('for_php7.php');

class knjl060gQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
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
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function ExecuteQuery($model) {

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
            $query .= "             W1.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT W1 ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ";
            $query .= "                   ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
            $query .= "                  AND W2.EXAMNO = W1.EXAMNO ";
            $query .= "                  AND W2.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "         WHERE ";
            $query .= "                 W1.ENTEXAMYEAR  = '".$model->examyear."' ";
            $query .= "             AND W1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND W1.TESTDIV      = '".$model->testdiv."' ";
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
            $query .= "             W1.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT W1 ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ";
            $query .= "                   ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
            $query .= "                  AND W2.EXAMNO = W1.EXAMNO ";
            $query .= "                  AND W2.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "         WHERE ";
            $query .= "                 W1.ENTEXAMYEAR  = '".$model->examyear."' ";
            $query .= "             AND W1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND W1.TESTDIV      = '".$model->testdiv."' ";
            $query .= "       )";
        }
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }

        //受験科目
        $testsubArray = array();
        $query = knjl060gQuery::getNameMst("L009", $model->examyear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubArray[] = $row["VALUE"];
        }
        $result->free();

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */

        /***
        *** 敬愛・柏原の受験科目
        *** 3科目(国、英、数)
        ***/

        /* 全科目分受験フラグ、合計点、平均点 */ 
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.attend_all_flg AS attend_all_flg ";
        $query .= "      ,TT4.total AS total4 ";
        $query .= "      ,TT4.average AS avarage4 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
                          //科目受験フラグ(attend_flg) 1:受験 0:欠席(*) NULL:得点未入力
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,CASE WHEN min(t1.attend_flg) = '0' THEN '0' ELSE '1' END AS attend_all_flg ";
        $query .= "               ,SUM(t1.score)                    AS total ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.testsubclasscd IN ('" . implode("','", $testsubArray) . "') ";
        $query .= "         GROUP BY t1.receptno ";
        $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
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
            $query .= "             W1.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT W1 ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ";
            $query .= "                   ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
            $query .= "                  AND W2.EXAMNO = W1.EXAMNO ";
            $query .= "                  AND W2.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "         WHERE ";
            $query .= "                 W1.ENTEXAMYEAR  = '".$model->examyear."' ";
            $query .= "             AND W1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "             AND W1.TESTDIV      = '".$model->testdiv."' ";
            $query .= "       )";
        }

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }


        /* 席次 */
        //入試区分順位、第１志望コース順位を算出
        //入試試験の科目が1つでも未受験があれば、順位対象外(NULLで更新)・・・これは保留
        //合計があれば、順位対象

        if (!strlen($model->special_reason_div)) {

        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, total_rank4, div_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.total_rank4 ";
        $query .= "      ,TT4.div_rank4 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.total4 DESC) AS total_rank4 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.dai1_coursecode ORDER BY t1.total4 DESC) AS div_rank4 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "                LEFT JOIN v_entexam_applicantbase_dat t2 ";
        $query .= "                     ON  t2.entexamyear  = t1.entexamyear ";
        $query .= "                     AND t2.applicantdiv = t1.applicantdiv";
        $query .= "                     AND t2.examno       = t1.examno";
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

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        }//special_reason_div

        //--------------------帳票【年度別　最高・最低・平均点】用--------------------

        if (!strlen($model->special_reason_div)) {

        //DELETE
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT ";
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

        //INSERT
        $query  = " INSERT INTO ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     SHDIV, ";
        $query .= "     COURSECODE, ";
        $query .= "     SEX, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     HIGHSCORE, ";
        $query .= "     LOWSCORE, ";
        $query .= "     AVG, ";
        $query .= "     TOTAL, ";
        $query .= "     COUNT, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T3.SHDIV, ";
        $query .= "     T3.DAI1_COURSECODE AS COURSECODE, ";
        $query .= "     T3.SEX, ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     MAX(T1.SCORE) AS HIGHSCORE, ";
        $query .= "     MIN(T1.SCORE) AS LOWSCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*10,0)/10,5,1) AS AVG, ";
        $query .= "     SUM(T1.SCORE) AS TOTAL, ";
        $query .= "     COUNT(T1.SCORE) AS COUNT, ";
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
        $query .= "     T3.SHDIV, ";
        $query .= "     T3.DAI1_COURSECODE, ";
        $query .= "     T3.SEX, ";
        $query .= "     T1.TESTSUBCLASSCD ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        }//special_reason_div

        //正常
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    } 
}
?>

