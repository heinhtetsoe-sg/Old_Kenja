<?php

require_once('for_php7.php');

class knjl060rQuery extends Query {

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
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより取得
    function getNameMstL009($model, $year, $nmsp1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        $query .= "     AND NAME{$model->applicantdiv} IS NOT NULL ";
        if (strlen($nmsp1)) {
            $query .= "     AND NAMESPARE1 = '".$nmsp1."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function ExecuteQueryH($model) {

        //DB接続
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
        //得点データチェック
        $query  = "SELECT COUNT(*) FROM entexam_score_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db);
            return false;
        }

        //1:推薦３科目、2:一般５科目・・・全科目分受験フラグ用
        $testsubArray3 = array();
        $testsubArray5 = array();
        $query = knjl060rQuery::getNameMstL009($model, $model->examyear, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE2"] == "1") $testsubArray3[] = $row["VALUE"];
            if ($row["NAMESPARE3"] == "1") $testsubArray5[] = $row["VALUE"];
        }
        $result->free();

        $cnt = ($model->testdiv == "1") ?get_count($testsubArray3) :get_count($testsubArray5);
        $testsub = ($model->testdiv == "1") ? "('" . implode("','", $testsubArray3) . "')" : "('" . implode("','", $testsubArray5) . "')";

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */

        /* 全科目分受験フラグ、合計点、平均点 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT1.attend_all_flg ";          //全科目受験フラグ
        $query .= "      ,CASE WHEN TT1.attend_flg = '1' THEN TT24.total4 END AS total4 ";                 //合計・・・推薦「満点390(300+90)点」、一般「満点590(500+90)点」
        $query .= "      ,CASE WHEN TT1.attend_flg = '1' THEN TT24.average4 END AS average4 ";               //平均
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
                          /* 全科目分受験フラグ */
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN COUNT(t1.score) >= {$cnt} THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "               ,CASE WHEN COUNT(t1.score) > 0 THEN '1' ELSE '0' END AS attend_flg";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.testsubclasscd IN {$testsub}";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno ";
        $query .= "            AND TT0.exam_type = TT1.exam_type ";
                          /* 合計点、平均点 */
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,SUM(t1.score)                    AS total4";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average4";
        $query .= "           FROM ";
        $query .= "            (SELECT t1.receptno";
        $query .= "                   ,t1.exam_type";
        $query .= "                   ,t1.score";
        $query .= "               FROM entexam_score_dat t1";
        $query .= "              where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "                AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                AND t1.testdiv      = '".$model->testdiv."'";
                                //合計には内申点の換算(内申点*90/135)も加算する
        $query .= "             UNION ALL";
        $query .= "             SELECT r1.receptno";
        $query .= "                   ,r1.exam_type";
        $query .= "                   ,c1.kasanten_all as score";
        $query .= "               FROM entexam_recept_dat r1";
        $query .= "                    inner join entexam_applicantconfrpt_dat c1";
        $query .= "                        on  c1.entexamyear  = r1.entexamyear";
        $query .= "                        and c1.applicantdiv = r1.applicantdiv";
        $query .= "                        and c1.examno       = r1.examno";
        $query .= "              where r1.entexamyear  = '".$model->examyear."' ";
        $query .= "                AND r1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                AND r1.testdiv      = '".$model->testdiv."'";
        $query .= "            ) t1 ";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT24 ON TT0.receptno  = TT24.receptno ";
        $query .= "             AND TT0.exam_type = TT24.exam_type ";
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
//echo "合計<BR>";
//echo $query;
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }


        /* 席次 */
        //入試試験の科目が1つでも未受験があれば、順位対象外(NULLで更新)
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, total_rank4, div_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        //入試区分ごと
        $query .= "      ,TT4.total_rank4 ";
        //入試区分＋第１志望コース(ＧＡ、ＧⅠ、ＧⅡ、ＳＧ)ごと
        $query .= "      ,TT4.div_rank4 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        //席次SQL
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv               ORDER BY t1.total4 DESC) AS total_rank4 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv, t3.coursecd, t3.majorcd, t3.examcoursecd ORDER BY t1.total4 DESC) AS div_rank4 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "                inner join entexam_applicantbase_dat t2 ";
        $query .= "                     ON  t2.entexamyear  = t1.entexamyear ";
        $query .= "                     AND t2.applicantdiv = t1.applicantdiv";
        $query .= "                     AND t2.testdiv      = t1.testdiv";
        $query .= "                     AND t2.examno       = t1.examno";
        $query .= "                left join entexam_wishdiv_mst t3 ";
        $query .= "                     ON  t3.entexamyear  = t2.entexamyear ";
        $query .= "                     AND t3.applicantdiv = t2.applicantdiv";
        $query .= "                     AND t3.testdiv      = t2.testdiv";
        $query .= "                     AND t3.desirediv    = t2.desirediv";
        $query .= "                     AND t3.wishno       = '1'"; //第１志望
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.attend_all_flg = '1'"; //1:全科目受験
        $query .= "            AND t1.total4 IS NOT NULL";
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
//echo "席次<BR>";
//echo $query;
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
        $query .= "            AND TT0.total4 IS NULL ";
        $query .= "        ) ";

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
        $query .= "            AND TT0.total4 IS NULL ";
        $query .= "        ) ";

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

    function ExecuteQueryJ($model) {

        //DB接続
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
        //得点データチェック
        $query  = "SELECT COUNT(*) FROM entexam_score_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db);
            return false;
        }

        //科目
        $subclassArray = array();
        $query = knjl060rQuery::getNameMstL009($model, $model->examyear, $model->testdiv);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclassArray[] = $row["VALUE"];
        }
        $result->free();

        $cnt = get_count($subclassArray);
        $testsub = "('" . implode("','", $subclassArray) . "')";

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */

        $setBaseKaten = $model->testdiv == '1' ? 15 : 10;
        $setFullKaten = $model->testdiv == '1' ? 0 : 10;
        /* 全科目分受験フラグ、合計点、平均点 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total1, avarage1, total2, avarage2, total3, avarage3, total4, avarage4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT RECEPT.examno ";
        $query .= "      ,ATTEND_T.attend_all_flg ";          //全科目受験フラグ
        $query .= "      ,CASE WHEN ATTEND_T.attend_flg = '1' THEN TOTAL_T.total1 END AS total1 ";
        $query .= "      ,CASE WHEN ATTEND_T.attend_flg = '1' THEN TOTAL_T.average1 END AS average1 ";
        $query .= "      ,CASE WHEN ATTEND_T.attend_flg = '1' THEN TOTAL_T.total2 END AS total2 ";
        $query .= "      ,CASE WHEN ATTEND_T.attend_flg = '1' THEN TOTAL_T.average2 END AS average2 ";
        $query .= "      ,CASE WHEN ATTEND_T.attend_flg = '1' THEN TOTAL_T.total3 END AS total3 ";
        $query .= "      ,CASE WHEN ATTEND_T.attend_flg = '1' THEN TOTAL_T.average3 END AS average3 ";
        $query .= "      ,CASE WHEN ATTEND_T.attend_flg = '1' THEN TOTAL_T.total4 END AS total4 ";
        $query .= "      ,CASE WHEN ATTEND_T.attend_flg = '1' THEN TOTAL_T.average4 END AS average4 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        /* 全科目分受験フラグ */
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN COUNT(t1.score) >= {$cnt} THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "               ,CASE WHEN COUNT(t1.score) > 0 THEN '1' ELSE '0' END AS attend_flg";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.testsubclasscd IN {$testsub}";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) ATTEND_T ON RECEPT.receptno  = ATTEND_T.receptno ";
        $query .= "            AND RECEPT.exam_type = ATTEND_T.exam_type ";
        /* 合計点、平均点 */
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,SUM(t1.score) ";
        $query .= "                    + ";
        $query .= "                    CASE WHEN MAX(BUN012.REMARK{$model->testdiv}) IS NOT NULL AND MAX(BUN013.REMARK{$model->testdiv}) = '1' ";
        $query .= "                         THEN {$setBaseKaten} ";
        $query .= "                         ELSE 0 ";
        $query .= "                    END ";
        if ($model->testdiv == '2') {
            $query .= "                + ";
            $query .= "                CASE WHEN MAX(BUN012.REMARK1) IS NOT NULL AND MAX(BUN013.REMARK1) IS NOT NULL AND MAX(VALUE(RECEPT2.ATTEND_ALL_FLG, '0')) = '1' ";
            $query .= "                     THEN {$setFullKaten} ";
            $query .= "                     ELSE 0 ";
            $query .= "                END ";
        }
        $query .= "               AS total1 ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score ";
        $query .= "                                     + ";
        $query .= "                                  CASE WHEN BUN012.REMARK{$model->testdiv} IS NOT NULL AND BUN013.REMARK{$model->testdiv} = '1' ";
        $query .= "                                       THEN {$setBaseKaten} ";
        $query .= "                                       ELSE 0 ";
        $query .= "                                  END ";
        if ($model->testdiv == '2') {
            $query .= "                                 + ";
            $query .= "                              CASE WHEN BUN012.REMARK1 IS NOT NULL AND BUN013.REMARK1 IS NOT NULL AND VALUE(RECEPT2.ATTEND_ALL_FLG, '0') = '1' ";
            $query .= "                                   THEN {$setFullKaten} ";
            $query .= "                                   ELSE 0 ";
            $query .= "                              END ";
        }
        $query .= "                )),1)  AS average1 ";
        $query .= "               ,SUM(t1.score)                    AS total2 ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average2 ";

        $query .= "               ,SUM(t1.score2) ";
        $query .= "                    + ";
        $query .= "                    CASE WHEN MAX(BUN012.REMARK{$model->testdiv}) IS NOT NULL AND MAX(BUN013.REMARK{$model->testdiv}) = '1' ";
        $query .= "                         THEN {$setBaseKaten} ";
        $query .= "                         ELSE 0 ";
        $query .= "                    END ";
        if ($model->testdiv == '2') {
            $query .= "                + ";
            $query .= "                CASE WHEN MAX(BUN012.REMARK1) IS NOT NULL AND MAX(BUN013.REMARK1) IS NOT NULL AND MAX(VALUE(RECEPT2.ATTEND_ALL_FLG, '0')) = '1' ";
            $query .= "                     THEN {$setFullKaten} ";
            $query .= "                     ELSE 0 ";
            $query .= "                END ";
        }
        $query .= "               AS total3 ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score2 ";
        $query .= "                                     + ";
        $query .= "                                  CASE WHEN BUN012.REMARK{$model->testdiv} IS NOT NULL AND BUN013.REMARK{$model->testdiv} = '1' ";
        $query .= "                                       THEN {$setBaseKaten} ";
        $query .= "                                       ELSE 0 ";
        $query .= "                                  END ";
        if ($model->testdiv == '2') {
            $query .= "                                 + ";
            $query .= "                              CASE WHEN BUN012.REMARK1 IS NOT NULL AND BUN013.REMARK1 IS NOT NULL AND VALUE(RECEPT2.ATTEND_ALL_FLG, '0') = '1' ";
            $query .= "                                   THEN {$setFullKaten} ";
            $query .= "                                   ELSE 0 ";
            $query .= "                              END ";
        }
        $query .= "                )),1)  AS average3 ";
        $query .= "               ,SUM(t1.score2)                    AS total4 ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score2)),1)  AS average4 ";
        $query .= "           FROM ";
        $query .= "            (SELECT t1.receptno";
        $query .= "                   ,t1.exam_type";
        $query .= "                   ,t1.score";
        $query .= "                   ,t1.score2";
        $query .= "               FROM entexam_score_dat t1";
        $query .= "              where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "                AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            ) t1 ";
        $query .= "           LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                AND RECEPT.APPLICANTDIV  = '".$model->applicantdiv."' ";
        $query .= "                AND RECEPT.TESTDIV       = '".$model->testdiv."' ";
        $query .= "                AND RECEPT.EXAM_TYPE     = t1.EXAM_TYPE ";
        $query .= "                AND RECEPT.RECEPTNO      = t1.RECEPTNO ";
        $query .= "           LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN012 ON RECEPT.ENTEXAMYEAR  = BUN012.ENTEXAMYEAR ";
        $query .= "                AND RECEPT.APPLICANTDIV  = BUN012.APPLICANTDIV ";
        $query .= "                AND RECEPT.EXAMNO  = BUN012.EXAMNO ";
        $query .= "                AND BUN012.SEQ  = '012' ";
        $query .= "           LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN013 ON RECEPT.ENTEXAMYEAR  = BUN013.ENTEXAMYEAR ";
        $query .= "                AND RECEPT.APPLICANTDIV  = BUN013.APPLICANTDIV ";
        $query .= "                AND RECEPT.EXAMNO  = BUN013.EXAMNO ";
        $query .= "                AND BUN013.SEQ  = '013' ";
        if ($model->testdiv == '2') {
            $query .= "           LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN010 ON RECEPT.ENTEXAMYEAR  = BUN010.ENTEXAMYEAR ";
            $query .= "                AND RECEPT.APPLICANTDIV  = BUN010.APPLICANTDIV ";
            $query .= "                AND RECEPT.EXAMNO  = BUN010.EXAMNO ";
            $query .= "                AND BUN010.SEQ  = '010' ";
            $query .= "           LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT2 ON RECEPT2.ENTEXAMYEAR  = '".$model->examyear."' ";
            $query .= "                AND RECEPT2.APPLICANTDIV  = '".$model->applicantdiv."' ";
            $query .= "                AND RECEPT2.TESTDIV       = BUN010.REMARK1 ";
            $query .= "                AND RECEPT2.EXAM_TYPE     = '1' ";
            $query .= "                AND RECEPT2.RECEPTNO      = BUN012.REMARK1 ";
        }
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TOTAL_T ON RECEPT.receptno  = TOTAL_T.receptno ";
        $query .= "             AND RECEPT.exam_type = TOTAL_T.exam_type ";
        $query .= " WHERE RECEPT.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND RECEPT.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND RECEPT.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND RECEPT.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND RECEPT.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'";

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
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, total_rank1, div_rank1, total_rank2, div_rank2, total_rank3, div_rank3, total_rank4, div_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT RECEPT.examno ";
        $query .= "      ,RANK_T.total_rank1 ";
        $query .= "      ,RANK_T.div_rank1 ";
        $query .= "      ,RANK_T.total_rank2 ";
        $query .= "      ,RANK_T.div_rank2 ";
        $query .= "      ,RANK_T.total_rank3 ";
        $query .= "      ,RANK_T.div_rank3 ";
        $query .= "      ,RANK_T.total_rank4 ";
        $query .= "      ,RANK_T.div_rank4 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat RECEPT ";
        //席次SQL
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv               ORDER BY t1.total1 DESC) AS total_rank1 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv, t3.coursecd, t3.majorcd, t3.examcoursecd ORDER BY t1.total1 DESC) AS div_rank1 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv               ORDER BY t1.total2 DESC) AS total_rank2 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv, t3.coursecd, t3.majorcd, t3.examcoursecd ORDER BY t1.total2 DESC) AS div_rank2 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv               ORDER BY t1.total3 DESC) AS total_rank3 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv, t3.coursecd, t3.majorcd, t3.examcoursecd ORDER BY t1.total3 DESC) AS div_rank3 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv               ORDER BY t1.total4 DESC) AS total_rank4 ";
        $query .= "               ,RANK() OVER(PARTITION BY t2.testdiv, t3.coursecd, t3.majorcd, t3.examcoursecd ORDER BY t1.total4 DESC) AS div_rank4 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "                inner join entexam_applicantbase_dat t2 ";
        $query .= "                     ON  t2.entexamyear  = t1.entexamyear ";
        $query .= "                     AND t2.applicantdiv = t1.applicantdiv";
        $query .= "                     AND t2.examno       = t1.examno";
        $query .= "                left join entexam_wishdiv_mst t3 ";
        $query .= "                     ON  t3.entexamyear  = t2.entexamyear ";
        $query .= "                     AND t3.applicantdiv = t2.applicantdiv";
        $query .= "                     AND t3.testdiv      = t2.testdiv";
        $query .= "                     AND t3.desirediv    = t2.desirediv";
        $query .= "                     AND t3.wishno       = '1'"; //第１志望
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.attend_all_flg = '1'"; //1:全科目受験
        $query .= "            AND t1.total4 IS NOT NULL";
        $query .= "       ) RANK_T ON RECEPT.receptno  = RANK_T.receptno ";
        $query .= "            AND RECEPT.exam_type = RANK_T.exam_type ";
        $query .= " WHERE RECEPT.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND RECEPT.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND RECEPT.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND RECEPT.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND RECEPT.receptno     = entexam_recept_dat.receptno ";
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

        $query  = " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     CASE WHEN MAX(BUN012.REMARK{$model->testdiv}) IS NOT NULL AND MAX(BUN013.REMARK{$model->testdiv}) = '1' ";
        $query .= "          THEN {$setBaseKaten} ";
        $query .= "          ELSE 0 ";
        if ($model->testdiv == '2') {
            $query .= "     END ";
            $query .= "     + ";
            $query .= "     CASE WHEN MAX(BUN012.REMARK1) IS NOT NULL AND MAX(BUN013.REMARK1) IS NOT NULL AND MAX(VALUE(RECEPT2.ATTEND_ALL_FLG, '0')) = '1' ";
            $query .= "          THEN {$setFullKaten} ";
            $query .= "          ELSE 0 ";
        }
        $query .= "     END AS TOTAL  ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         T1.RECEPTNO , ";
        $query .= "         T1.EXAM_TYPE , ";
        $query .= "         T1.SCORE , ";
        $query .= "         T1.SCORE2 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= "     ) T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "          AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "          AND RECEPT.TESTDIV = '{$model->testdiv}' ";
        $query .= "          AND RECEPT.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "          AND RECEPT.RECEPTNO = T1.RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN012 ON RECEPT.ENTEXAMYEAR = BUN012.ENTEXAMYEAR ";
        $query .= "          AND RECEPT.APPLICANTDIV = BUN012.APPLICANTDIV ";
        $query .= "          AND RECEPT.EXAMNO = BUN012.EXAMNO ";
        $query .= "          AND BUN012.SEQ = '012' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN013 ON RECEPT.ENTEXAMYEAR = BUN013.ENTEXAMYEAR ";
        $query .= "          AND RECEPT.APPLICANTDIV = BUN013.APPLICANTDIV ";
        $query .= "          AND RECEPT.EXAMNO = BUN013.EXAMNO ";
        $query .= "          AND BUN013.SEQ = '013' ";
        if ($model->testdiv == '2') {
            $query .= "           LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN010 ON RECEPT.ENTEXAMYEAR  = BUN010.ENTEXAMYEAR ";
            $query .= "                AND RECEPT.APPLICANTDIV  = BUN010.APPLICANTDIV ";
            $query .= "                AND RECEPT.EXAMNO  = BUN010.EXAMNO ";
            $query .= "                AND BUN010.SEQ  = '010' ";
            $query .= "           LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT2 ON RECEPT2.ENTEXAMYEAR  = '".$model->examyear."' ";
            $query .= "                AND RECEPT2.APPLICANTDIV  = '".$model->applicantdiv."' ";
            $query .= "                AND RECEPT2.TESTDIV       = BUN010.REMARK1 ";
            $query .= "                AND RECEPT2.EXAM_TYPE     = '1' ";
            $query .= "                AND RECEPT2.RECEPTNO      = BUN012.REMARK1 ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAM_TYPE ";

        $result = $db->query($query);
        while ($row008 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->examyear}' ";
            $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND TESTDIV = '{$model->testdiv}' ";
            $query .= "     AND EXAM_TYPE = '1' ";
            $query .= "     AND RECEPTNO = '{$row008["RECEPTNO"]}' ";
            $query .= "     AND SEQ = '008' ";
            $checkCnt = $db->getOne($query);

            $data = array();
            if ($checkCnt == 0) {
                $data["ENTEXAMYEAR"][TEXT]              = $model->examyear;
                $data["APPLICANTDIV"][TEXT]             = $model->applicantdiv;
                $data["TESTDIV"][TEXT]                  = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]                = '1';
                $data["RECEPTNO"][TEXT]                 = $row008["RECEPTNO"];
                $data["SEQ"][TEXT]                      = '008';
                $data["REMARK".$model->testdiv][TEXT]   = $row008["TOTAL"];
                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][FUNC]                  = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                $db->query($query);
            } else {
                $data["REMARK".$model->testdiv][TEXT]   = $row008["TOTAL"];
                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][FUNC]                  = "sysdate()";
                //条件
                $where  = " WHERE ";
                $where .= "         ENTEXAMYEAR  = '{$model->examyear}' ";
                $where .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $where .= "     AND TESTDIV      = '{$model->testdiv}' ";
                $where .= "     AND EXAM_TYPE    = '1' ";
                $where .= "     AND RECEPTNO     = '{$row008["RECEPTNO"]}' ";
                $where .= "     AND SEQ          = '008' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
                $db->query($query);
            }
        }
        $result->free();

        /* 欠席 */
        //入試試験の科目が全て未受験の者は、3:未受験(欠席)で更新
        $query  = " UPDATE entexam_recept_dat  ";
        $query .= "    SET judgediv = '3'  ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."'  ";
        $query .= "    AND examno IN ( ";
        $query .= "         SELECT RECEPT.examno  ";
        $query .= "           FROM entexam_recept_dat RECEPT  ";
        $query .= "          WHERE RECEPT.entexamyear  = '".$model->examyear."'  ";
        $query .= "            AND RECEPT.applicantdiv = '".$model->applicantdiv."'  ";
        $query .= "            AND RECEPT.testdiv      = '".$model->testdiv."'  ";
        $query .= "            AND RECEPT.total4 IS NULL ";
        $query .= "        ) ";

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
        $query .= "         SELECT RECEPT.examno  ";
        $query .= "           FROM entexam_recept_dat RECEPT  ";
        $query .= "          WHERE RECEPT.entexamyear  = '".$model->examyear."'  ";
        $query .= "            AND RECEPT.applicantdiv = '".$model->applicantdiv."'  ";
        $query .= "            AND RECEPT.testdiv      = '".$model->testdiv."'  ";
        $query .= "            AND RECEPT.total4 IS NULL ";
        $query .= "        ) ";

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

