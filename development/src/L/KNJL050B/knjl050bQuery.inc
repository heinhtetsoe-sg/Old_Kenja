<?php

require_once('for_php7.php');

class knjl050bQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        //推薦入試は表示しない
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //受験科目
    function getTestSubclasscd($model, $nameCd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.NAME".$model->applicantdiv." AS CSV_NAME, ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME".$model->applicantdiv." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '{$model->ObjYear}' ";
        $query .= "     AND T1.NAMECD1     = 'L009' ";
        if ($nameCd2) {
            $query .= "     AND T1.NAMECD2     = '{$nameCd2}' ";
        }
        //満点マスタの設定が前提
        $query .= "     AND T1.NAMECD2 IN ( ";
        $query .= "         SELECT T5.TESTSUBCLASSCD ";
        $query .= "           FROM ENTEXAM_PERFECT_MST T5 ";
        $query .= "          WHERE T5.ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "            AND T5.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "            AND T5.TESTDIV       = '{$model->testdiv}' ";
        $query .= "          GROUP BY T5.TESTSUBCLASSCD ";
        $query .= "         ) ";
        $query .= "     AND T1.NAME".$model->applicantdiv." IS NOT NULL ";
        if ($model->testdiv == "1") {
            $query .= "     AND T1.NAMESPARE2 = '1' ";
        } else if ($model->testdiv == "2") {
            $query .= "     AND T1.NAMESPARE3 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //会場
    function getHallName($model, $hallCd = "")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMHALL_NAME AS CSV_NAME, ";
        $query .= "     T1.EXAMHALLCD AS VALUE, ";
        $query .= "     T1.EXAMHALL_NAME || ' (' || T1.S_RECEPTNO || '～' || T1.E_RECEPTNO || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";
        if ($hallCd) {
            $query .= "     AND T1.EXAMHALLCD     = '{$hallCd}' ";
        }

        $query .= " ORDER BY ";
        $query .= "     T1.EXAMHALLCD ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg)
    {
        $query  = " WITH WISH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     DESIREDIV, ";
        $query .= "     MIN(WISHNO) AS WISHNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV   = '".$model->testdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     DESIREDIV ";
        $query .= " ), MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     RECE.RECEPTNO, ";
        $query .= "     RECE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     N1.NAME2 AS SEX, ";
        $query .= "     SCORE.ATTEND_FLG, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     VALUE(PERFECT.PERFECT, 100) AS PERFECT, ";
        $query .= "     N2.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "     N3.NAME1 AS TESTDIV_NAME, ";
        $query .= "     N4.NAME".$model->applicantdiv." AS TESTSUBCLASSCD_NAME ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "           ON RECE.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "          AND RECE.EXAMNO        = BASE.EXAMNO ";
        if (strlen($model->special_reason_div)) {
            $query .= "      AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
        }
        $query .= "     INNER JOIN ENTEXAM_HALL_YDAT HALL ";
        $query .= "           ON RECE.ENTEXAMYEAR   = HALL.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV  = HALL.APPLICANTDIV ";
        $query .= "          AND RECE.TESTDIV       = HALL.TESTDIV ";
        $query .= "          AND RECE.EXAM_TYPE     = HALL.EXAM_TYPE ";
        $query .= "          AND HALL.EXAMHALLCD    = '".$model->examhallcd."' ";
        $query .= "          AND RECE.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= "     LEFT JOIN WISH_T ";
        $query .= "           ON BASE.DESIREDIV = WISH_T.DESIREDIV ";
        $query .= "     LEFT JOIN ENTEXAM_WISHDIV_MST WISH_M ";
        $query .= "           ON RECE.ENTEXAMYEAR   = WISH_M.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV  = WISH_M.APPLICANTDIV ";
        $query .= "          AND RECE.TESTDIV       = WISH_M.TESTDIV  ";
        $query .= "          AND WISH_T.DESIREDIV   = WISH_M.DESIREDIV  ";
        $query .= "          AND WISH_T.WISHNO      = WISH_M.WISHNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ";
        $query .= "           ON RECE.ENTEXAMYEAR     = SCORE.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV    = SCORE.APPLICANTDIV ";
        $query .= "          AND RECE.TESTDIV         = SCORE.TESTDIV  ";
        $query .= "          AND RECE.EXAM_TYPE       = SCORE.EXAM_TYPE ";
        $query .= "          AND RECE.RECEPTNO        = SCORE.RECEPTNO ";
        $query .= "          AND SCORE.TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        //満点マスタの設定が前提
        $query .= "        INNER JOIN ENTEXAM_PERFECT_MST PERFECT ";
        $query .= "               ON RECE.ENTEXAMYEAR       = PERFECT.ENTEXAMYEAR ";
        $query .= "              AND RECE.APPLICANTDIV      = PERFECT.APPLICANTDIV ";
        $query .= "              AND RECE.TESTDIV           = PERFECT.TESTDIV ";
        $query .= "              AND PERFECT.TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "        LEFT JOIN NAME_MST N1 ";
        $query .= "               ON N1.NAMECD1 = 'Z002' ";
        $query .= "              AND N1.NAMECD2 = BASE.SEX ";
        $query .= "        LEFT JOIN NAME_MST N2 ";
        $query .= "               ON N2.NAMECD1 = 'L003' ";
        $query .= "              AND N2.NAMECD2 = RECE.APPLICANTDIV ";
        $query .= "        LEFT JOIN NAME_MST N3 ";
        $query .= "               ON N3.NAMECD1 = 'L004' ";
        $query .= "              AND N3.NAMECD2 = RECE.TESTDIV ";
        $query .= "        LEFT JOIN V_NAME_MST N4 ";
        $query .= "               ON N4.YEAR    = RECE.ENTEXAMYEAR ";
        $query .= "              AND N4.NAMECD1 = 'L009' ";
        $query .= "              AND N4.NAMECD2 = '".$model->testsubclasscd."' ";
        $query .= " WHERE RECE.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND RECE.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND RECE.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND RECE.EXAM_TYPE      = '".$model->exam_type."' ";
        if ($flg == "") {
            if ($model->cmd == "back") {
                $query .= "   AND RECE.RECEPTNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY RECE.RECEPTNO DESC ";
            } else if ($model->cmd == "next") {
                $query .= "   AND RECE.RECEPTNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY RECE.RECEPTNO ";
            }
        } else if ($flg == "BAC_COUNT" || $flg == "NEXT_COUNT") {
            if ($flg == "BAC_COUNT") {
                $query .= "   AND RECE.RECEPTNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY RECE.RECEPTNO DESC ";
            } else if ($flg == "NEXT_COUNT") {
                $query .= "   AND RECE.RECEPTNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY RECE.RECEPTNO ";
            }
        } else if ($flg == "CHECK") {
                $query .= "   AND RECE.RECEPTNO >= '".$model->s_receptno."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        if ($flg == "BAC_COUNT" || $flg == "NEXT_COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     * ";
        }
        $query .= "  FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "BAC_COUNT" && $flg != "NEXT_COUNT" ) {
            $query .= " ORDER BY RECEPTNO";
        }
        $query .= " FETCH FIRST 70 ROWS ONLY ";
        
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //全部空白（得点または＊が1件も入力無）の時、削除する。
        $isDeleteALL = true;
        foreach ($model->score as $receptno => $score) {
            if (strlen($score)) $isDeleteALL = false;
        }

        //重み:初期値(%)
        $rate = 100;

        //全部空白以外の時、満点チェック
        if (!$isDeleteALL) {
            $result = $db->query(knjl050bQuery::SelectQuery($model, "CHECK"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //入力値チェック
                $checkScore = $model->score[$row["RECEPTNO"]];
                if ($checkScore != "*" && !is_numeric($checkScore)) {
                    $model->setWarning("MSG901","得点欄は数値又は、＊(小文字)のみ入力可能です。\\n受験番号：".$row["EXAMNO"]);
                    Query::dbCheckIn($db);
                    return false;
                }
                //満点チェック
                if ($model->score[$row["RECEPTNO"]] > (int)$row["PERFECT"]) {
                    $model->setWarning("MSG901","満点：".(int)$row["PERFECT"]."以下で入力してください\\n受験番号：".$row["EXAMNO"]);
                    Query::dbCheckIn($db);
                    return false;
                }
                $rate = (int) $row["RATE"];
            }
        }

        $db->autoCommit(false);

        //一度すべて削除
        $query  = "DELETE FROM entexam_score_dat";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv   = '".$model->applicantdiv."'";
        $query .= "   AND testdiv        = '".$model->testdiv."'";
        $query .= "   AND exam_type      = '".$model->exam_type."'";
        $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
        $query .= "   AND receptno IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        //全部空白以外の時、追加
        if (!$isDeleteALL) {
            foreach ($model->score as $receptno => $score) {
                //追加
                $query  = "INSERT INTO entexam_score_dat";
                $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, testsubclasscd, attend_flg, score, registercd, updated)";
                $query .= " VALUES ('".$model->ObjYear."'";
                $query .= "        ,'".$model->applicantdiv."'";
                $query .= "        ,'".$model->testdiv."'";
                $query .= "        ,'".$model->exam_type."'";
                $query .= "        ,'".$receptno."'";
                $query .= "        ,'".$model->testsubclasscd."'";
                if ($score === '*') {
                    $query .= "        , '0' ";
                } else {
                    $query .= "        ,'".(!strlen($score)  ? "" : "1" )."'";
                }
                $query .= "        , ".((!strlen($score) || $score === '*')  ? "NULL" : $score);
                $query .= "        ,'".STAFFCD."'";
                $query .= "        ,SYSDATE()";
                $query .= ")";
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>