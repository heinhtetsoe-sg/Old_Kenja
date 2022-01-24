<?php

require_once('for_php7.php');

class knjl670hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    public function getEntexamTestDivMst($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //類別マスタ取得
    public function getEntexamClassifyMst($model, $classifyCd = "")
    {
        $query  = " SELECT ";
        $query .= "     CLASSIFY_CD || ':' || VALUE(CLASSIFY_NAME, '') AS LABEL,  ";
        $query .= "     CLASSIFY_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CLASSIFY_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= " AND APPLICANTDIV = '{$model->applicantDiv}' "; // 2:高校 固定
        if ($classifyCd != "") {
            $query .= " AND CLASSIFY_CD = '{$classifyCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getJudgeExamee($model)
    {
        $query .= " WITH APBASE AS ( ";
        $query .= " SELECT ";
        $query .= "   ENTEXAMYEAR, ";
        $query .= "   APPLICANTDIV, ";
        $query .= "   TESTDIV, ";
        $query .= "   EXAMNO, ";
        $query .= "   TESTDIV1 ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantDiv}' ";
        $query .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        if (strcmp($model->field["TESTDIV1"], "00") != 0) {
            $query .= "  AND TESTDIV1 = '{$model->field["TESTDIV1"]}' ";
        }
        $query .= "   AND (JUDGEMENT IS NULL OR JUDGEMENT <> '3') ";  //欠席以外
        $query .= " ), MERGE_SCORE AS ( ";
        $query .= " SELECT ";
        $query .= "   ENTEXAMYEAR, ";
        $query .= "   APPLICANTDIV, ";
        $query .= "   TESTDIV, ";
        $query .= "   EXAM_TYPE, ";
        $query .= "   RECEPTNO, ";
        $query .= "   SUM(CASE WHEN (ATTEND_FLG <> '1' OR ATTEND_FLG IS NULL) THEN 0 ELSE 1 END) AS ABSENT_CNT ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_SCORE_DAT ";
        $query .= " GROUP BY ";
        $query .= "   ENTEXAMYEAR, ";
        $query .= "   APPLICANTDIV, ";
        $query .= "   TESTDIV, ";
        $query .= "   EXAM_TYPE, ";
        $query .= "   RECEPTNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.*, ";
        if ($model->cmd == "fix") {
            if (!$model->isTestSubj) {
                //小論文のみの入試日程の場合は合格点なし。面接の出欠、評価内容で確定
                $query .= "  CASE WHEN (VALUE(L027.NAMESPARE1, '') <> '1' AND VALUE(T3.ATTEND_FLG, '') <> '1') THEN '1' ELSE '2' END AS JUDGEMENT, ";
            } elseif ($model->field["PASS_POINT"] != "") {
                //H/Fの判定
                $query .= "  CASE WHEN T4.MARK IN ('H', 'F') ";
                $query .= "       THEN (CASE WHEN (T2.TOTAL4 IS NOT NULL AND VALUE(L027.NAMESPARE1, '') <> '1' AND VALUE(T3.ATTEND_FLG, '') <> '1') ";
                $query .= "                  THEN CASE WHEN {$model->field["PASS_POINT"]} <= T2.TOTAL4 ";
                $query .= "                            THEN '1' ";
                $query .= "                            ELSE '2' ";
                $query .= "                       END ";
                $query .= "             END) ";
                //H/F以外の判定
                $query .= "       ELSE (CASE WHEN (T4.COMMITMENT_FLG = '1' AND VALUE(L027.NAMESPARE1, '') <> '1' AND VALUE(T3.ATTEND_FLG, '') <> '1') ";
                $query .= "                  THEN '1' ";
                $query .= "                  ELSE (CASE WHEN (T2.TOTAL4 IS NOT NULL AND VALUE(L027.NAMESPARE1, '') <> '1' AND VALUE(T3.ATTEND_FLG, '') <> '1') ";
                $query .= "                             THEN (CASE WHEN {$model->field["PASS_POINT"]} <= T2.TOTAL4 ";
                $query .= "                                        THEN '1' ";
                $query .= "                                        ELSE '2' ";
                $query .= "                                   END) ";
                $query .= "                             ELSE '2' ";
                $query .= "                        END) ";
                $query .= "             END) ";
                $query .= "  END AS JUDGEMENT, ";
            }
            $query .= "CASE WHEN T5.ABSENT_CNT > 0 THEN '3' ELSE '' END AS ABSENT ";
        } else {
            $query .= "   '' AS JUDGEMENT, ";
            $query .= "   '' AS ABSENT ";
        }
        $query .= " FROM ";
        $query .= "   APBASE T1 ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "    AND T2.EXAM_TYPE = '1' ";
        $query .= "    AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "  LEFT JOIN ENTEXAM_INTERVIEW_DAT T3 ";
        $query .= "   ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "  AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "  AND T3.TESTDIV = T2.TESTDIV ";
        $query .= "  AND T3.EXAMNO = T2.EXAMNO ";
        $query .= "  LEFT JOIN ENTEXAM_CLASSIFY_MST T4 ";
        $query .= "   ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "  AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "  AND T4.CLASSIFY_CD = T1.TESTDIV1 ";
        $query .= "    LEFT JOIN MERGE_SCORE T5 ";
        $query .= "      ON T5.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "     AND T5.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "     AND T5.TESTDIV = T2.TESTDIV ";
        $query .= "     AND T5.EXAM_TYPE = T2.EXAM_TYPE ";
        $query .= "     AND T5.RECEPTNO = T2.RECEPTNO ";
        $query .= "   LEFT JOIN ENTEXAM_SETTING_MST L027 ";
        $query .= "      ON L027.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "     AND L027.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "     AND L027.SETTING_CD = 'L027' ";
        $query .= "     AND L027.SEQ = T3.INTERVIEW_A ";
        if ($model->isTestSubj) {
            $query .= " WHERE ";
            $query .= "   T2.TOTAL4 IS NOT NULL ";
        }
        return $query;
    }

    public function getUpdateQuery($model, $datas, $clrFlg = false)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $sucCnt = 0;

        foreach ($datas as $idx => $row) {
            $data = array();
            //クリア時は、合格後の項目についてもクリアする。登録時は合否とコース関連だけ。
            if ($clrFlg) {
                $data["JUDGEMENT"][TEXT] = "";
                $data["PROCEDUREDIV"][TEXT] = "";
                $data["ENTDIV"][TEXT] = "";
                $data["SUC_COURSECD"][TEXT] = "";
                $data["SUC_MAJORCD"][TEXT] = "";
                $data["SUC_COURSECODE"][TEXT] = "";
                $data["PROCEDUREDATE"][TEXT] = "";
            } else {
                $data["JUDGEMENT"][TEXT] = $row["ABSENT"] != "" ? $row["ABSENT"] : $row["JUDGEMENT"];
                $data["SUC_COURSECD"][TEXT] = "1";
                $data["SUC_MAJORCD"][TEXT] = "100";
                $data["SUC_COURSECODE"][TEXT] = "1001";
                if ($row["JUDGEMENT"] === "1") {
                    $sucCnt++;
                }
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $where  = " WHERE ";
            $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV = '{$model->applicantDiv}' ";
            $where .= "   AND EXAMNO = '{$row["EXAMNO"]}' ";
            if ($clrFlg) {
                $where .= "   AND JUDGEMENT <> '3' ";  //欠席はそのまま。
            }

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            if ($clrFlg) {
                //クリア時はRECEPT_DATの情報もクリアする。
                $data3 = array();
                $data3["PROCEDUREDIV1"][TEXT]  = "";
                $data3["PROCEDUREDATE1"][TEXT] = "";
                $data3["REGISTERCD"][TEXT]     = STAFFCD;
                $data3["UPDATED"][FUNC]        = "sysdate()";
                
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->applicantDiv}' ";
                $where .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
                $where .= "   AND EXAM_TYPE = '1' ";
                $where .= "   AND EXAMNO = '{$row["EXAMNO"]}' ";
                $query = Query::updateSQL($data3, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);
            }
        }

        //更新時だけ、実行履歴を登録
        if (!$clrFlg) {
            $data2 = array();
            $data2["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
            $data2["APPLICANTDIV"][TEXT] = $model->applicantDiv;
            $data2["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
            $data2["CLASSIFY_CD"][TEXT]  = $model->field["TESTDIV1"];
            $data2["EXECDATE"][FUNC]     = "sysdate()";
            $data2["BORDER_SCORE"][NUMBER] = $model->field["PASS_POINT"];
            $data2["SUCCESS_CNT"][NUMBER]  = $sucCnt;
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data2, "ENTEXAM_CLASSIFY_PASSINGMARK_MST");
            $db->query($query);
        } else {
            //クリア時はRECEPT_DATの情報もクリアする。
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    public function getRireki($model)
    {
        $query .= " SELECT ";
        $query .= "   T1.TESTDIV, ";
        $query .= "   T3.TESTDIV_NAME, ";
        $query .= "   T1.CLASSIFY_CD, ";
        $query .= "   CASE WHEN T1.CLASSIFY_CD = '00' THEN '全て' ELSE T2.CLASSIFY_NAME END AS CLASSIFY_NAME, ";
        $query .= "   T1.EXECDATE, ";
        $query .= "   T1.BORDER_SCORE, ";
        $query .= "   T1.SUCCESS_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CLASSIFY_PASSINGMARK_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_CLASSIFY_MST T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.CLASSIFY_CD = T1.CLASSIFY_CD ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST T3 ";
        $query .= "       ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T3.TESTDIV = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantDiv}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "   T1.EXECDATE DESC, ";
        $query .= "   T1.TESTDIV ";

        return $query;
    }
}
