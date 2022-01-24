<?php

require_once('for_php7.php');

class knjl060aQuery extends Query
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //試験回取得
    public function getTestDiv($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象人数チェック
    public function checkRecCnt($model)
    {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV      = '{$model->testdiv}' ";
        return $query;
    }

    //得点データチェック
    public function checkScoCnt($model)
    {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "   AND SCORE IS NOT NULL ";
        return $query;
    }

    //算出
    public function ExecuteQuery($model)
    {
        //参照データ（得点）

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受験科目
        $testsubArray = array();
        $query = knjl060aQuery::getNameMst($model->examyear, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubArray[] = $row["VALUE"];
        }
        $result->free();

        $cnt = get_count($testsubArray);

        //エラー無視
        $db->expectError();

        /***
        *** 受験科目
        *** 「1:中学」・・・得点入力（国100、算100、理50、社50、英50）
        *** （１）４科合計　国100＋算100＋理50＋社50＝300点満点
        *** （２）３科合計　国100＋算100＋理or社or英50×2＝300点満点
        *** （３）２科合計　（国100＋算100）×1.5＝300点満点
        *** （４）判定合計　上の（１）～（３）のうち最も高い点数
        ***
        *** 「2:高校」・・・得点入力（国100、算100、理100、社100、英100）
        *** （１）５科合計　大阪1次　国100＋数100＋理100＋社100＋英100＝500点満点
        *** （２）３科合計　和歌山1次　国100＋数100＋英100＝300点満点
        ***/

        /* 全科目分受験フラグ、合計 */
        //科目受験フラグ(attend_flg) 1:受験 0:欠席(*) NULL:得点未入力
        $query  = "UPDATE ENTEXAM_RECEPT_DAT ";
        $query .= "   SET (EXAMNO, ";
        $query .= "        ATTEND_ALL_FLG, ";
        if ($model->applicantdiv == "1") {
            $query .= "        TOTAL4, ";
            $query .= "        TOTAL3, ";
            $query .= "        TOTAL2, ";
            $query .= "        TOTAL1, ";
        } else {
            $query .= "        TOTAL1, ";
        }
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= " SELECT ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     S1.ATTEND_ALL_FLG, ";
        if ($model->applicantdiv == "1") {
            $query .= "     S1.TOTAL4, ";
            $query .= "     S1.TOTAL3, ";
            $query .= "     S1.TOTAL2, ";
            $query .= "     S1.TOTAL1, ";
        } else {
            $query .= "     S1.TOTAL AS TOTAL1, ";
        }
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             RECEPTNO, ";
        $query .= "             SUBCNT, ";
        $query .= "             ATTEND_ALL_FLG, ";
        $query .= "             TOTAL4, ";
        $query .= "             TOTAL3, ";
        $query .= "             TOTAL2, ";
        $query .= "             CASE WHEN TOTAL4 IS NOT NULL AND TOTAL3 IS NOT NULL AND TOTAL2 IS NOT NULL ";
        $query .= "                  THEN CASE WHEN TOTAL4 >= TOTAL3 AND TOTAL4 >= TOTAL2 THEN TOTAL4 ";
        $query .= "                            WHEN TOTAL3 >= TOTAL4 AND TOTAL3 >= TOTAL2 THEN TOTAL3 ";
        $query .= "                            WHEN TOTAL2 >= TOTAL3 AND TOTAL2 >= TOTAL4 THEN TOTAL2 ";
        $query .= "                       END ";
        $query .= "                  WHEN TOTAL3 IS NOT NULL AND TOTAL2 IS NOT NULL ";
        $query .= "                  THEN CASE WHEN TOTAL3 >= TOTAL2 THEN TOTAL3 ";
        $query .= "                            WHEN TOTAL2 >= TOTAL3 THEN TOTAL2 ";
        $query .= "                       END ";
        $query .= "                  WHEN TOTAL2 IS NOT NULL ";
        $query .= "                  THEN TOTAL2 ";
        $query .= "                  WHEN TOTAL1 IS NOT NULL ";
        $query .= "                  THEN TOTAL1 ";
        $query .= "             END AS TOTAL1, ";
        $query .= "             TOTAL ";
        $query .= "         FROM ";
        $query .= "             ( ";
        $query .= "             SELECT ";
        $query .= "                 RECEPTNO, ";
        $query .= "                 SUBCNT, ";
        $query .= "                 ATTEND_ALL_FLG, ";
        $query .= "                 CASE WHEN SUBCNT >= 4 THEN TOTAL4 END AS TOTAL4, ";
        $query .= "                 CASE WHEN SUBCNT >= 3 AND TOTAL3_3 >= TOTAL3_4 AND TOTAL3_3 >= TOTAL3_5 THEN TOTAL3_3 ";
        $query .= "                      WHEN SUBCNT >= 3 AND TOTAL3_4 >= TOTAL3_3 AND TOTAL3_4 >= TOTAL3_5 THEN TOTAL3_4 ";
        $query .= "                      WHEN SUBCNT >= 3 AND TOTAL3_5 >= TOTAL3_3 AND TOTAL3_5 >= TOTAL3_4 THEN TOTAL3_5 ";
        $query .= "                      END AS TOTAL3, ";
        $query .= "                 CASE WHEN SUBCNT >= 2 THEN TOTAL2 END AS TOTAL2, ";
        $query .= "                 CASE WHEN SUBCNT = 1 THEN TOTAL1 END AS TOTAL1, ";
        $query .= "                 TOTAL ";
        $query .= "             FROM ";
        $query .= "                 ( ";
        $query .= "                 SELECT ";
        $query .= "                     R006.RECEPTNO, ";
        $query .= "                     COUNT(EMSB.TESTSUBCLASSCD) AS SUBCNT, ";
        $query .= "                     CASE WHEN COUNT(EMSB.TESTSUBCLASSCD) = COUNT(S1.SCORE) THEN '1' ELSE '0' END AS ATTEND_ALL_FLG, ";
        $query .= "                     SUM(CASE WHEN EMSB.TESTSUBCLASSCD IN ('1','2','3','4') THEN S1.SCORE END) AS TOTAL4, ";
        $query .= "                     SUM(CASE WHEN EMSB.TESTSUBCLASSCD IN ('1','2') THEN S1.SCORE WHEN EMSB.TESTSUBCLASSCD = '3' THEN S1.SCORE*2 END) AS TOTAL3_3, ";
        $query .= "                     SUM(CASE WHEN EMSB.TESTSUBCLASSCD IN ('1','2') THEN S1.SCORE WHEN EMSB.TESTSUBCLASSCD = '4' THEN S1.SCORE*2 END) AS TOTAL3_4, ";
        $query .= "                     SUM(CASE WHEN EMSB.TESTSUBCLASSCD IN ('1','2') ";
        $query .= "                              THEN S1.SCORE ";
        $query .= "                              WHEN EMSB.TESTSUBCLASSCD = '5' AND S1.SCORE IS NOT NULL ";
        $query .= "                              THEN CASE WHEN VALUE(S1.SCORE, 0) >= VALUE(L055.NAMESPARE2, 0) ";
        $query .= "                                        THEN S1.SCORE * 2 ";
        $query .= "                                        ELSE L055.NAMESPARE2 * 2 ";
        $query .= "                                   END ";
        $query .= "                         END ";
        $query .= "                     ) AS TOTAL3_5, ";
        $query .= "                     SMALLINT(ROUND(SUM(CASE WHEN EMSB.TESTSUBCLASSCD IN ('1','2') THEN S1.SCORE*1.5 END),0)) AS TOTAL2, ";
        $query .= "                     SUM(CASE WHEN EMSB.TESTSUBCLASSCD = '5' AND S1.SCORE IS NOT NULL ";
        $query .= "                              THEN CASE WHEN VALUE(S1.SCORE, 0) >= VALUE(L055.NAMESPARE2, 0) ";
        $query .= "                                        THEN S1.SCORE * 2 ";
        $query .= "                                        ELSE L055.NAMESPARE2 * 2 ";
        $query .= "                                   END ";
        $query .= "                              ELSE S1.SCORE ";
        $query .= "                         END ";
        $query .= "                     ) AS TOTAL1, ";
        $query .= "                     SUM(CASE WHEN EMSB.TESTSUBCLASSCD = '5' AND S1.SCORE IS NOT NULL ";
        $query .= "                              THEN CASE WHEN VALUE(S1.SCORE, 0) >= VALUE(L055.NAMESPARE2, 0) ";
        $query .= "                                        THEN S1.SCORE ";
        $query .= "                                        ELSE L055.NAMESPARE2 ";
        $query .= "                                   END ";
        $query .= "                              ELSE S1.SCORE ";
        $query .= "                         END ";
        $query .= "                     ) AS TOTAL ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_RECEPT_DETAIL_DAT R006 ";
        $query .= "                     LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST EMSB ";
        $query .= "                          ON EMSB.ENTEXAMYEAR    = R006.ENTEXAMYEAR ";
        $query .= "                         AND EMSB.APPLICANTDIV   = R006.APPLICANTDIV ";
        $query .= "                         AND EMSB.EXAM_TYPE      = R006.REMARK3 ";
        $query .= "                         AND EMSB.TESTSUBCLASSCD != '6' "; //面接(6)は除く
        $query .= "                     LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "                          ON S1.ENTEXAMYEAR      = R006.ENTEXAMYEAR ";
        $query .= "                         AND S1.APPLICANTDIV     = R006.APPLICANTDIV ";
        $query .= "                         AND S1.TESTDIV          = R006.TESTDIV ";
        $query .= "                         AND S1.EXAM_TYPE        = R006.EXAM_TYPE ";
        $query .= "                         AND S1.RECEPTNO         = R006.RECEPTNO ";
        $query .= "                         AND S1.TESTSUBCLASSCD   = EMSB.TESTSUBCLASSCD ";
        $query .= "                     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "                          ON RECEPT.ENTEXAMYEAR      = R006.ENTEXAMYEAR ";
        $query .= "                         AND RECEPT.APPLICANTDIV     = R006.APPLICANTDIV ";
        $query .= "                         AND RECEPT.TESTDIV          = R006.TESTDIV ";
        $query .= "                         AND RECEPT.EXAM_TYPE        = R006.EXAM_TYPE ";
        $query .= "                         AND RECEPT.RECEPTNO         = R006.RECEPTNO ";
        $query .= "                     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ005 ";
        $query .= "                          ON SEQ005.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "                         AND SEQ005.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "                         AND SEQ005.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "                         AND SEQ005.SEQ          = '005' ";
        $query .= "                     LEFT JOIN NAME_MST L055 ";
        $query .= "                          ON L055.NAMECD1    = 'L055' ";
        $query .= "                         AND L055.NAMECD2    = SEQ005.REMARK1 ";
        $query .= "                 WHERE ";
        $query .= "                         R006.ENTEXAMYEAR    = '{$model->examyear}' ";
        $query .= "                     AND R006.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "                     AND R006.TESTDIV        = '{$model->testdiv}' ";
        $query .= "                     AND R006.SEQ            = '006' ";
        $query .= "                 GROUP BY ";
        $query .= "                     R006.RECEPTNO ";
        $query .= "                 ) ";
        $query .= "             ) ";
        $query .= "     ) S1 ON S1.RECEPTNO = R1.RECEPTNO ";
        $query .= " WHERE R1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "   AND R1.EXAM_TYPE    = ENTEXAM_RECEPT_DAT.EXAM_TYPE ";
        $query .= "   AND R1.RECEPTNO     = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->examyear}'";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}'";
        $query .= "   AND TESTDIV      = '{$model->testdiv}'";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->rollback();
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【合計での順位】--------------------

        /* 席次(合計) */
        $query  = "UPDATE ENTEXAM_RECEPT_DAT ";
        $query .= "   SET (EXAMNO, ";
        $query .= "        TOTAL_RANK1, ";
        $query .= "        DIV_RANK1, ";
        $query .= "        DIV_RANK2, ";
        $query .= "        DIV_RANK3, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT R1.EXAMNO, ";
        $query .= "       S1.TOTAL_RANK1, ";
        $query .= "       S1.DIV_RANK1, ";
        $query .= "       S1.DIV_RANK2, ";
        $query .= "       S1.DIV_RANK3, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT R2.RECEPTNO ";
        $query .= "               ,R2.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(PARTITION BY R2.TESTDIV ORDER BY R2.TOTAL1 DESC) AS TOTAL_RANK1 "; //全体順位・・・参考資料「高校特待生判定用資料.pdf」
        $query .= "               ,RANK() OVER(PARTITION BY R2.TESTDIV, R006.REMARK1, R006.REMARK2 ORDER BY R2.TOTAL1 DESC) AS DIV_RANK1 "; //専併別コース別順位・・・参考資料「高校特待生判定用資料.pdf」
        $query .= "               ,RANK() OVER(PARTITION BY R2.TESTDIV, R006.REMARK1 ORDER BY R2.TOTAL1 DESC) AS DIV_RANK2 "; //専併別順位・・・とりあえず作成しておく
        $query .= "               ,RANK() OVER(PARTITION BY R2.TESTDIV, R006.REMARK2 ORDER BY R2.TOTAL1 DESC) AS DIV_RANK3 "; //コース別順位・・・とりあえず作成しておく
        $query .= "           FROM ENTEXAM_RECEPT_DAT R2 ";
        $query .= "                LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ";
        $query .= "                     ON R006.ENTEXAMYEAR  = R2.ENTEXAMYEAR ";
        $query .= "                    AND R006.APPLICANTDIV = R2.APPLICANTDIV ";
        $query .= "                    AND R006.TESTDIV      = R2.TESTDIV ";
        $query .= "                    AND R006.EXAM_TYPE    = R2.EXAM_TYPE ";
        $query .= "                    AND R006.RECEPTNO     = R2.RECEPTNO ";
        $query .= "                    AND R006.SEQ          = '006' ";
        $query .= "          WHERE R2.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "            AND R2.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "            AND R2.TESTDIV      = '{$model->testdiv}' ";
        $query .= "            AND R2.TOTAL1 IS NOT NULL ";
        $query .= "       ) S1 ON R1.RECEPTNO  = S1.RECEPTNO ";
        $query .= " WHERE R1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "   AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND R1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "   AND R1.EXAM_TYPE    = ENTEXAM_RECEPT_DAT.EXAM_TYPE ";
        $query .= "   AND R1.RECEPTNO     = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "   AND TESTDIV      = '".$model->testdiv."'";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->rollback();
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【欠席】--------------------

        /* 欠席(一旦クリア) */
        $query  = " UPDATE ENTEXAM_RECEPT_DAT  ";
        $query .= "    SET JUDGEDIV = NULL  ";
        $query .= "  WHERE ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "    AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "    AND TESTDIV      = '".$model->testdiv."'  ";
        $query .= "    AND JUDGEDIV     = '4'  ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->rollback();
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 欠席 */
        //入試試験の科目が全て未受験の者は、「4:欠席」で更新
        $query  = " UPDATE ENTEXAM_RECEPT_DAT  ";
        $query .= "    SET JUDGEDIV = '4'  ";
        $query .= "  WHERE ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "    AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "    AND TESTDIV      = '".$model->testdiv."'  ";
        $query .= "    AND EXAMNO IN ( ";
        $query .= "         SELECT TT0.EXAMNO  ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT TT0  ";
        $query .= "          WHERE TT0.ENTEXAMYEAR  = '".$model->examyear."'  ";
        $query .= "            AND TT0.APPLICANTDIV = '".$model->applicantdiv."'  ";
        $query .= "            AND TT0.TESTDIV      = '".$model->testdiv."'  ";
        $query .= "            AND TT0.TOTAL1 IS NULL ";
        $query .= "        ) ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->rollback();
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //統計情報更新 DELETE INSERT
        $query  = "   DELETE ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_TOKEI_INOUT_HIGH_LOW_HISTORY_DAT ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->rollback();
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " INSERT INTO ENTEXAM_TOKEI_INOUT_HIGH_LOW_HISTORY_DAT ";
        $query .= "   WITH NMST_A023 AS ( ";
        $query .= "       SELECT ";
        $query .= "           NAMESPARE1 AS J_SCHOOLCD ";
        $query .= "       FROM ";
        $query .= "           NAME_MST ";
        $query .= "       WHERE ";
        $query .= "           NAMECD1 = 'A023' ";
        $query .= "           AND NAME1 = 'J' ";
        $query .= "   ), RECEPT_DATA AS ( ";
        $query .= "       SELECT ";
        $query .= "           RECEPT.ENTEXAMYEAR, ";
        $query .= "           RECEPT.APPLICANTDIV, ";
        $query .= "           RECEPT.TESTDIV, ";
        $query .= "           RECEPT.EXAM_TYPE, ";
        $query .= "           RECEPT.RECEPTNO, ";
        $query .= "           RECEPT.EXAMNO, ";
        $query .= "           RD006.REMARK1 AS SHDIV, ";
        $query .= "           RD006.REMARK2 AS DESIREDIV, ";
        $query .= "           RD006.REMARK3 AS EXAM_TYPE2, ";
        $query .= "           CASE ";
        $query .= "               WHEN RECEPT.APPLICANTDIV = '2' AND BASE.FS_CD = (SELECT J_SCHOOLCD FROM NMST_A023) ";
        $query .= "               THEN '1' ";
        $query .= "               ELSE '2' ";
        $query .= "           END AS INOUT_DIV "; //1：本校生, 2:外部生
        $query .= "       FROM ";
        $query .= "           ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "           INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ";
        $query .= "               ON RD006.ENTEXAMYEAR   = RECEPT.ENTEXAMYEAR ";
        $query .= "               AND RD006.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "               AND RD006.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "               AND RD006.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "               AND RD006.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "               AND RD006.SEQ          = '006' ";
        $query .= "           INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "               ON BASE.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "               AND BASE.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "               AND BASE.EXAMNO        = RECEPT.EXAMNO ";
        $query .= "       WHERE ";
        $query .= "           RECEPT.ENTEXAMYEAR         = '{$model->examyear}' ";
        $query .= "           AND RECEPT.APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "           AND RECEPT.TESTDIV         = '{$model->testdiv}' ";
        $query .= "           AND RECEPT.EXAM_TYPE       = '1' ";
        $query .= "           AND VALUE (RECEPT.JUDGEDIV, '') <> '4' "; //欠席除く
        $query .= "           AND RD006.REMARK1 IS NOT NULL ";
        $query .= "           AND RD006.REMARK2 IS NOT NULL ";
        $query .= "           AND RD006.REMARK3 IS NOT NULL ";
        $query .= "  ), TOUKEI_DATA AS ( ";
        $query .= "       SELECT ";
        $query .= "           VALUE (RD.INOUT_DIV, '9') AS INOUT_DIV, ";
        $query .= "           VALUE (RD.SHDIV, '9') AS SHDIV, ";
        $query .= "           VALUE (RD.DESIREDIV, '9') AS DESIREDIV, ";
        $query .= "           VALUE (SD.TESTSUBCLASSCD, '9') AS TESTSUBCLASSCD, ";
        $query .= "           MAX(SD.SCORE) AS HIGHSCORE, ";
        $query .= "           MIN(SD.SCORE) AS LOWSCORE, ";
        $query .= "           INT(AVG(SD.SCORE * 1.0) + 0.5) AS AVG, ";
        $query .= "           SUM(SD.SCORE) AS TOTAL, ";
        $query .= "           COUNT(DISTINCT SD.RECEPTNO) AS COUNT ";
        $query .= "       FROM ";
        $query .= "           ENTEXAM_SCORE_DAT SD ";
        $query .= "           INNER JOIN RECEPT_DATA RD ";
        $query .= "               ON RD.ENTEXAMYEAR         = SD.ENTEXAMYEAR ";
        $query .= "               AND RD.APPLICANTDIV       = SD.APPLICANTDIV ";
        $query .= "               AND RD.TESTDIV            = SD.TESTDIV ";
        $query .= "               AND RD.EXAM_TYPE          = SD.EXAM_TYPE ";
        $query .= "               AND RD.RECEPTNO           = SD.RECEPTNO ";
        $query .= "           INNER JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST EMSB ";
        $query .= "               ON EMSB.ENTEXAMYEAR       = SD.ENTEXAMYEAR ";
        $query .= "               AND EMSB.APPLICANTDIV     = SD.APPLICANTDIV ";
        $query .= "               AND EMSB.EXAM_TYPE        = RD.EXAM_TYPE2 ";
        $query .= "               AND EMSB.TESTSUBCLASSCD   = SD.TESTSUBCLASSCD ";
        $query .= "       WHERE ";
        $query .= "           EMSB.TESTSUBCLASSCD <> '6' "; //面接除く
        $query .= "       GROUP BY ";
        $query .= "           GROUPING SETS ( ";
        $query .= "               (RD.INOUT_DIV, RD.SHDIV, RD.DESIREDIV, SD.TESTSUBCLASSCD), ";
        $query .= "               (RD.INOUT_DIV, RD.SHDIV, RD.DESIREDIV), ";
        $query .= "               (SD.TESTSUBCLASSCD), ";
        $query .= "               () ";
        $query .= "           ) ";
        $query .= "  ) ";
        $query .= "   SELECT ";
        $query .= "       '{$model->examyear}' AS ENTEXAMYEAR, ";
        $query .= "       '{$model->applicantdiv}' AS APPLICANTDIV, ";
        $query .= "       '{$model->testdiv}' AS TESTDIV, ";
        $query .= "       INOUT_DIV, ";
        $query .= "       SHDIV, ";
        $query .= "       DESIREDIV, ";
        $query .= "       TESTSUBCLASSCD, ";
        $query .= "       HIGHSCORE, ";
        $query .= "       LOWSCORE, ";
        $query .= "       AVG, ";
        $query .= "       TOTAL, ";
        $query .= "       COUNT, ";
        $query .= "       '".STAFFCD."' AS STAFFCD, ";
        $query .= "       sysdate() AS REGISTERCD ";
        $query .= "   FROM ";
        $query .= "       TOUKEI_DATA T1 ";
        
        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->rollback();
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //正常
        $db->commit();
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    }
}
