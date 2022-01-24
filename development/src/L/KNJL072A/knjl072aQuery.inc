<?php

require_once('for_php7.php');

class knjl072aQuery extends Query
{

    //名称マスタよりデータを取得
    public function getName($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3, ";
        $query .= "     ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($testdiv) {
            $query .= " AND TESTDIV = '".$testdiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //選択した受験コース、専併区分で、それ以下のランクを一覧画面に表示する
    public function getRank($model, $namecd1)
    {
        $query  = " WITH SHDIV_TBL AS ( ";
        $query .= "     SELECT ";
        $query .= "         '0' AS LINK, ";
        $query .= "         NAMECD2 AS SHDIV, ";
        $query .= "         NAME1 AS SHDIV_NAME ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND NAMECD1 = 'L006' ";
        $query .= " ), COURSE_TBL AS ( ";
        $query .= "     SELECT ";
        $query .= "         '0' AS LINK, ";
        $query .= "         NAMECD2 AS COURSE, ";
        $query .= "         NAME1 AS COURSE_NAME, ";
        $query .= "         NAMESPARE2 AS COURSE_RANK ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND NAMECD1 = '{$namecd1}' ";
        $query .= "         AND NAMESPARE2 IS NOT NULL ";
        $query .= " ), MAIN_TBL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.COURSE, ";
        $query .= "         T2.COURSE_RANK, ";
        $query .= "         T2.COURSE_NAME, ";
        $query .= "         T1.SHDIV, ";
        $query .= "         T1.SHDIV_NAME, ";
        $query .= "         RANK() OVER(ORDER BY T2.COURSE_RANK, T1.SHDIV DESC) AS ALL_RANK ";
        $query .= "     FROM ";
        $query .= "         SHDIV_TBL T1, ";
        $query .= "         COURSE_TBL T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.LINK = T2.LINK ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COURSE, ";
        $query .= "     COURSE_RANK, ";
        $query .= "     COURSE_NAME, ";
        $query .= "     SHDIV, ";
        $query .= "     SHDIV_NAME, ";
        $query .= "     ALL_RANK ";
        $query .= " FROM ";
        $query .= "     MAIN_TBL ";
        $query .= " WHERE ";
        //選択した受験コースのコースランク以下のコース(専願or併願)を取得しリストに追加
        //選択した専併区分が併願の時、全てのコース(専願)を取得しリストに追加
        $query .= "     SHDIV = '{$model->shdiv}' AND COURSE_RANK >= (SELECT COURSE_RANK FROM COURSE_TBL WHERE COURSE = '{$model->wish_course}') ";
        if ($model->shdiv == "2") {
            $query .= "     OR SHDIV = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SHDIV DESC, ";
        $query .= "     COURSE_RANK ";

        return $query;
    }

    //合格者取得(シミュレーション結果表示)
    public function selectQuerySuccessCnt($model, $pass_shdiv, $pass_course)
    {
        $query  = " SELECT ";
        if ($pass_shdiv == "1") {
            $query .= "     SUM(CASE WHEN VALUE(T1.SENGAN_JUDGEDIV, '0') NOT IN ('0','4') THEN 1 ELSE 0 END) AS SUCCESS_CNT, ";
            $query .= "     SUM(CASE WHEN VALUE(T1.SENGAN_JUDGEDIV, '0') = '0' THEN 1 ELSE 0 END) AS SUCCESS_CNT_NO, ";
            $query .= "     SUM(CASE WHEN VALUE(T1.SENGAN_JUDGEDIV, '0') = '4' THEN 1 ELSE 0 END) AS KESSEKI_CNT ";
        } elseif ($pass_shdiv == "2") {
            $query .= "     SUM(CASE WHEN VALUE(T1.HEIGAN_JUDGEDIV, '0') NOT IN ('0','4') THEN 1 ELSE 0 END) AS SUCCESS_CNT, ";
            $query .= "     SUM(CASE WHEN VALUE(T1.HEIGAN_JUDGEDIV, '0') = '0' THEN 1 ELSE 0 END) AS SUCCESS_CNT_NO, ";
            $query .= "     SUM(CASE WHEN VALUE(T1.HEIGAN_JUDGEDIV, '0') = '4' THEN 1 ELSE 0 END) AS KESSEKI_CNT ";
        } elseif ($pass_shdiv == "ALL") {
            $query .= "     SUM(CASE WHEN VALUE(T1.SENGAN_JUDGEDIV, '0') NOT IN ('0','4') OR VALUE(T1.HEIGAN_JUDGEDIV, '0') NOT IN ('0','4') THEN 1 ELSE 0 END) AS SUCCESS_CNT, ";
            $query .= "     SUM(CASE WHEN VALUE(T1.SENGAN_JUDGEDIV, '0') = '0' AND VALUE(T1.HEIGAN_JUDGEDIV, '0') = '0' THEN 1 ELSE 0 END) AS SUCCESS_CNT_NO, ";
            $query .= "     SUM(CASE WHEN VALUE(T1.SENGAN_JUDGEDIV, '0') = '4' OR VALUE(T1.HEIGAN_JUDGEDIV, '0') = '4' THEN 1 ELSE 0 END) AS KESSEKI_CNT ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_TMP T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR    = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE      = '{$model->examType}' ";
        $query .= "     AND T1.JUDGE_SHDIV    = '{$model->shdiv}' ";
        $query .= "     AND T1.JUDGE_COURSECD = '{$model->wish_course}' ";
        if ($pass_shdiv == "1") {
            $query .= "     AND T1.SENGAN_JUDGEDIV = '{$pass_course}' ";
        } elseif ($pass_shdiv == "2") {
            $query .= "     AND T1.HEIGAN_JUDGEDIV = '{$pass_course}' ";
        }

        return $query;
    }

    //-----------------------------------
    //シミュレーション処理実行
    //-----------------------------------

    //シミュレーションSQL
    public function simSql($model)
    {
        $query  = " SELECT ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     RCPT.JUDGEDIV AS KESSEKI, ";
        $query .= "     RCPT.TOTAL1 AS TOTAL, ";
        $query .= "     BD_L057.NAMECD2 AS JIZEN_HANTEI, ";
        $query .= "     VALUE(BD_L057.NAMESPARE2, '9') AS JIZEN_HANTEI_RANK, ";
        $query .= "     BD_L058.NAMECD2 AS JIZEN_SENGAN, ";
        $query .= "     VALUE(BD_L058.NAMESPARE2, '9') AS JIZEN_SENGAN_RANK, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ";
        $query .= "          ON RD006.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND RD006.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND RD006.TESTDIV       = RCPT.TESTDIV ";
        $query .= "         AND RD006.EXAM_TYPE     = RCPT.EXAM_TYPE ";
        $query .= "         AND RD006.RECEPTNO      = RCPT.RECEPTNO ";
        $query .= "         AND RD006.SEQ           = '006' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD029 ";
        $query .= "          ON BD029.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BD029.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND BD029.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         AND BD029.SEQ           = '029' ";
        $query .= "     LEFT JOIN NAME_MST L003 ";
        $query .= "          ON L003.NAMECD1        = 'L003' ";
        $query .= "         AND L003.NAMECD2        = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN NAME_MST BD_L057 ";
        $query .= "          ON BD_L057.NAMECD1     = 'L' || VALUE(L003.NAMESPARE3,'H') || '57' ";
        $query .= "         AND BD_L057.NAMECD2     = BD029.REMARK6 ";
        $query .= "     LEFT JOIN NAME_MST BD_L058 ";
        $query .= "          ON BD_L058.NAMECD1     = 'L' || VALUE(L003.NAMESPARE3,'H') || '58' ";
        $query .= "         AND BD_L058.NAMECD2     = BD029.REMARK8 ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->year}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '{$model->examType}' ";
        $query .= "     AND RD006.REMARK2       = '{$model->wish_course}' ";
        $query .= "     AND RD006.REMARK1       = '{$model->shdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    public function sim($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //仮判定データすべて削除
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $db->query($query);

        //仮判定データ追加
        $model->simList = array();
        $query = knjl072aQuery::simSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->simList[] = $row;
        }
        $result->free();

        foreach ($model->simList as $simKey => $simRow) {
            //初期化
            $sengan_judgediv = "";
            $heigan_judgediv = "";
            $sengan_judgediv_rank = "9";
            $heigan_judgediv_rank = "9";

            //手順1.得点無しの場合、欠席
            if ($simRow["TOTAL"] == "") {
                if ($model->shdiv == "1") {
                    //専願受験の場合、専願欠席
                    $sengan_judgediv = "4";
                } elseif ($model->shdiv == "2") {
                    //併願受験の場合、両方とも欠席
                    $sengan_judgediv = "4";
                    $heigan_judgediv = "4";
                }
            } else {
                //手順2～.上限点、下限点での判定
                foreach ($model->borderScoreList as $key => $row) {
                    if ($model->kagen_score[$key] <= $simRow["TOTAL"] && $simRow["TOTAL"] <= $model->jogen_score[$key]) {
                        if ($row["SHDIV"] == "1") {
                            $sengan_judgediv = $row["COURSE"];
                            $sengan_judgediv_rank = $row["COURSE_RANK"];
                        } elseif ($row["SHDIV"] == "2") {
                            $heigan_judgediv = $row["COURSE"];
                            $heigan_judgediv_rank = $row["COURSE_RANK"];
                        }
                    }
                }

                //合格がない場合、不合格
                if ($model->shdiv == "1" && $sengan_judgediv == "") {
                    $sengan_judgediv = "0";
                } elseif ($model->shdiv == "2" && $heigan_judgediv == "") {
                    $heigan_judgediv = "0";
                }

                //手順8.事前判定コースでの判定
                if ($model->shdiv == "1") {
                    if ($sengan_judgediv_rank > $simRow["JIZEN_HANTEI_RANK"]) {
                        $sengan_judgediv = $simRow["JIZEN_HANTEI"];
                        $sengan_judgediv_rank = $simRow["JIZEN_HANTEI_RANK"];
                    }
                } elseif ($model->shdiv == "2") {
                    if ($heigan_judgediv_rank > $simRow["JIZEN_HANTEI_RANK"]) {
                        $heigan_judgediv = $simRow["JIZEN_HANTEI"];
                        $heigan_judgediv_rank = $simRow["JIZEN_HANTEI_RANK"];
                    }
                }

                //手順9.事前専願コースでの判定（併願受験のみ）
                if ($model->shdiv == "2") {
                    if ($sengan_judgediv_rank > $simRow["JIZEN_SENGAN_RANK"]) {
                        $sengan_judgediv = $simRow["JIZEN_SENGAN"];
                        $sengan_judgediv_rank = $simRow["JIZEN_SENGAN_RANK"];
                    }
                }

                //手順10.専願合格コースが併願合格コース以下の場合、専願合格コースを消す（併願受験のみ）
                if ($model->shdiv == "2") {
                    if ($sengan_judgediv_rank >= $heigan_judgediv_rank) {
                        $sengan_judgediv = "";
                        $sengan_judgediv_rank = "";
                    }
                }
            }

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $model->examType;
            $data["RECEPTNO"][TEXT]         = $simRow["RECEPTNO"];
            $data["EXAMNO"][TEXT]           = $simRow["EXAMNO"];
            $data["JUDGE_SHDIV"][TEXT]      = $model->shdiv;
            $data["JUDGE_COURSECD"][TEXT]   = $model->wish_course;
            $data["SENGAN_JUDGEDIV"][TEXT]  = $sengan_judgediv;
            $data["HEIGAN_JUDGEDIV"][TEXT]  = $heigan_judgediv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $db->query(Query::insertSQL($data, "ENTEXAM_JUDGE_TMP"));
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //-----------------------------------
    //確定処理実行
    //-----------------------------------
    public function decision(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受付データ
        $query  = " UPDATE ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT T1 ";
        $query .= " SET ";
        $query .= "     (T1.REMARK8, T1.REMARK9, T1.UPDATED) =  ";
        $query .= " (SELECT ";
        $query .= "     TMP.SENGAN_JUDGEDIV, ";
        $query .= "     TMP.HEIGAN_JUDGEDIV, ";
        $query .= "     sysdate() ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_JUDGE_TMP TMP ";
        $query .= "  WHERE ";
        $query .= "     T1.ENTEXAMYEAR = TMP.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = TMP.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = TMP.TESTDIV ";
        $query .= "     AND T1.EXAM_TYPE = TMP.EXAM_TYPE ";
        $query .= "     AND T1.RECEPTNO = TMP.RECEPTNO ";
        $query .= " ) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->examType}' ";
        $query .= "     AND T1.SEQ = '006' ";
        $query .= "     AND EXISTS ";
        $query .= "         (SELECT ";
        $query .= "             'x' ";
        $query .= "          FROM ";
        $query .= "             ENTEXAM_JUDGE_TMP E_TMP ";
        $query .= "          WHERE ";
        $query .= "             T1.ENTEXAMYEAR = E_TMP.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = E_TMP.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV = E_TMP.TESTDIV ";
        $query .= "             AND T1.EXAM_TYPE = E_TMP.EXAM_TYPE ";
        $query .= "             AND T1.RECEPTNO = E_TMP.RECEPTNO ";
        $query .= "         ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
