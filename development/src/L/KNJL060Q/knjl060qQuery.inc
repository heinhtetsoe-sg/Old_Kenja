<?php

require_once('for_php7.php');

class knjl060qQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1)
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
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        //入試区分「3:校長推薦、4：自己推薦入試、5:一般」のみ表示
        if ($namecd1 == "L004") {
            $query .= " AND NAMECD2 in ('3','4','5') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //対象人数チェック
    public function checkRecCnt($model)
    {
        $query  = "SELECT COUNT(*) FROM entexam_recept_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        return $query;
    }

    //得点データチェック
    public function checkScoCnt($model)
    {
        $query  = "SELECT COUNT(*) FROM entexam_score_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        return $query;
    }

    //高校
    public function executeQueryH($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //受験科目
        $testsubArray = array();
        $query = knjl060qQuery::getNameMst($model->examyear, "L009");
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
        *** 受験科目
        *** 入試区分「3:校長」：5科目(1:国、2:数、3:理、5:英、6:小論文)
        *** 入試区分「5:一般」：3科目(1:国、2:数、5:英)
        ***/

        /* 全科目分受験フラグ、合計点、平均点 */
        //科目受験フラグ(attend_flg) 1:受験 0:欠席(*) NULL:得点未入力
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, ";
        if ($model->testdiv === "3" || $model->testdiv === "4") {
            $query .= "        total2, avarage2, ";
        }
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.attend_all_flg AS attend_all_flg ";
        $query .= "      ,TT4.total AS total4 ";
        $query .= "      ,TT4.average AS avarage4 ";
        if ($model->testdiv === "3" || $model->testdiv === "4") {
            $query .= "      ,TT2.total AS total2 ";
            $query .= "      ,TT2.average AS avarage2 ";
        }
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,CASE WHEN min(t1.attend_flg) = '0' THEN '0' ELSE '1' END AS attend_all_flg ";
        $query .= "               ,SUM(t1.score)                    AS total ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.testsubclasscd != '6' ";
        $query .= "         GROUP BY t1.receptno ";
        $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
        if ($model->testdiv === "3" || $model->testdiv === "4") {
            $query .= "       LEFT JOIN ";
            $query .= "        (SELECT t1.receptno";
            $query .= "               ,CASE WHEN min(t1.attend_flg) = '0' THEN '0' ELSE '1' END AS attend_all_flg ";
            $query .= "               ,SUM(t1.score)                    AS total ";
            $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average ";
            $query .= "           FROM entexam_score_dat t1";
            $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
            $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
            $query .= "            AND t1.testsubclasscd != '6' ";
            $query .= "         GROUP BY t1.receptno ";
            $query .= "       ) TT2 ON TT0.receptno  = TT2.receptno ";
        }
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


        /* 席次 */
        //入試区分順位、コース順位を算出
        //科目が1つでも未受験があれば、順位対象外(NULLで更新)・・・これは保留
        //合計があれば、順位対象

        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, total_rank4, div_rank4, ";
        if ($model->testdiv === "3" || $model->testdiv === "4") {
            $query .= "        total_rank2, div_rank2, ";
        }
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.total_rank4 ";
        $query .= "      ,TT4.div_rank4 ";
        if ($model->testdiv === "3" || $model->testdiv === "4") {
            $query .= "      ,TT2.total_rank2 ";
            $query .= "      ,TT2.div_rank2 ";
        }
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
        if ($model->testdiv === "3" || $model->testdiv === "4") {
            $query .= "       LEFT JOIN ";
            $query .= "        (SELECT t1.receptno";
            $query .= "               ,t1.exam_type";
            $query .= "               ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.total2 DESC) AS total_rank2 ";
            $query .= "               ,RANK() OVER(PARTITION BY t2.dai1_coursecode ORDER BY t1.total2 DESC) AS div_rank2 ";
            $query .= "           FROM entexam_recept_dat t1";
            $query .= "                LEFT JOIN v_entexam_applicantbase_dat t2 ";
            $query .= "                     ON  t2.entexamyear  = t1.entexamyear ";
            $query .= "                     AND t2.applicantdiv = t1.applicantdiv";
            $query .= "                     AND t2.examno       = t1.examno";
            $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
            $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
            $query .= "            AND t1.total2 IS NOT NULL ";
            $query .= "       ) TT2 ON TT0.receptno  = TT2.receptno ";
            $query .= "            AND TT0.exam_type = TT2.exam_type ";
        }
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


        /* 偏差値 */
        //入試区分偏差値、コース偏差値を算出
        //合計があれば、偏差値対象

        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, JUDGE_DEVIATION, LINK_JUDGE_DEVIATION, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.JUDGE_DEVIATION ";        //入試区分偏差値
        $query .= "      ,TT4.LINK_JUDGE_DEVIATION ";   //コース偏差値
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT  R1.receptno";
        $query .= "                ,R1.exam_type";
        $query .= "                ,CASE WHEN 0 < R2.STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL4-R2.AVG)/R2.STDDEV+50)*10,0)/10,5,1) END AS JUDGE_DEVIATION ";
        $query .= "                ,CASE WHEN 0 < R3.STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL4-R3.AVG)/R3.STDDEV+50)*10,0)/10,5,1) END AS LINK_JUDGE_DEVIATION ";
        $query .= "           FROM  entexam_recept_dat R1";
        $query .= "                 LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                      ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "                     AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                     AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "                 LEFT JOIN ( ";
        $query .= "                     SELECT ";
        $query .= "                         TESTDIV, ";
        $query .= "                         DECIMAL(ROUND(AVG(FLOAT(TOTAL4))*10,0)/10,5,1) AS AVG, ";
        $query .= "                         DECIMAL(ROUND(STDDEV(FLOAT(TOTAL4))*10,0)/10,5,1) AS STDDEV ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_RECEPT_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         ENTEXAMYEAR      = '".$model->examyear."' ";
        $query .= "                         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "                         AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "                         AND EXAM_TYPE    = '1' ";
        $query .= "                     GROUP BY ";
        $query .= "                         TESTDIV ";
        $query .= "                 ) R2 ON R2.TESTDIV = R1.TESTDIV ";
        $query .= "                 LEFT JOIN ( ";
        $query .= "                     SELECT ";
        $query .= "                         R1.TESTDIV, ";
        $query .= "                         B1.DAI1_COURSECODE, ";
        $query .= "                         DECIMAL(ROUND(AVG(FLOAT(R1.TOTAL4))*10,0)/10,5,1) AS AVG, ";
        $query .= "                         DECIMAL(ROUND(STDDEV(FLOAT(R1.TOTAL4))*10,0)/10,5,1) AS STDDEV ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "                         LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                              ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "                             AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                             AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "                     WHERE ";
        $query .= "                         R1.ENTEXAMYEAR      = '".$model->examyear."' ";
        $query .= "                         AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "                         AND R1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "                         AND R1.EXAM_TYPE    = '1' ";
        $query .= "                     GROUP BY ";
        $query .= "                         R1.TESTDIV, ";
        $query .= "                         B1.DAI1_COURSECODE ";
        $query .= "                 ) R3 ON R3.TESTDIV = R1.TESTDIV AND R3.DAI1_COURSECODE = B1.DAI1_COURSECODE ";
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

        //正常
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    }

    //満点マスタより取得
    public function getPerfectMst($model)
    {
        $query  = " SELECT ";
        $query .= "     SUM(PERFECT) AS PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        return $query;
    }

    //中学・小学
    public function executeQueryJP($model)
    {

        //DB接続
        $db = Query::dbCheckOut();

        //受験科目
        $testsubArray = array();
        $query = knjl060qQuery::getNameMst($model->examyear, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubArray[] = $row["VALUE"];
        }
        $result->free();

        //満点取得
        $perfect = $db->getOne(knjl060qQuery::getPerfectMst($model));
        //DBエラー回避用
        //中学入試は国語100点、算数100点、理科50点、社会50点の300点満点
        //教科平均は(教科合計/3)で算出。3は(満点300/固定100)で算出。
        if (!strlen($perfect) && SCHOOLKIND == "J") {
            $perfect = 300;
        }
        //小学入試はペーパーテスト(50)、表現(25)、行動観察(25)の100点満点
        if (!strlen($perfect) && SCHOOLKIND == "P") {
            $perfect = 100;
        }

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */

        /***
        *** 受験科目
        *** 4科目　1:国(100)、2:算(100)、3:理(50)、4:社(50)
        ***/

        //--------------------【全科目分受験フラグ、合計点、平均点】--------------------

        /* 全科目分受験フラグ、合計点、平均点 */
        //科目受験フラグ(attend_flg) 1:受験 0:欠席(*) NULL:得点未入力
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, ";
        $query .= "        registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.attend_all_flg AS attend_all_flg ";
        $query .= "      ,TT4.total AS total4 ";
        $query .= "      ,TT4.average AS avarage4 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,CASE WHEN min(t1.attend_flg) = '0' THEN '0' ELSE '1' END AS attend_all_flg ";
        $query .= "               ,SUM(t1.score)                    AS total ";
        $query .= "               ,ROUND(SUM(FLOAT(t1.score))/({$perfect}/100),1)  AS average ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
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

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【席次】--------------------

        /* 席次 */
        //入試区分順位、男女別順位を算出
        //科目が1つでも未受験があれば、順位対象外(NULLで更新)・・・これは保留
        //合計があれば、順位対象

        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, total_rank4, div_rank4, ";
        $query .= "        registercd, updated) ";
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
        $query .= "               ,RANK() OVER(PARTITION BY t2.sex ORDER BY t1.total4 DESC) AS div_rank4 ";
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
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV ";
        //合計(G)(AVARAGE4)
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     'G' AS TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.AVARAGE4 END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.AVARAGE4 END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.AVARAGE4))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.AVARAGE4) AS MAX_SCORE, ";
        $query .= "     MIN(T1.AVARAGE4) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.AVARAGE4) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.AVARAGE4))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4))/({$perfect}/100)*100000,0)/100000,8,5) AS CALC_AVG, ";
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

        //正常
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    }
}
?>

