<?php

require_once('for_php7.php');

class knjl050rQuery extends Query {

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
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //受験科目
    function getTestSubclasscd($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME".$model->applicantdiv." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '{$model->ObjYear}' ";
        $query .= "     AND T1.NAMECD1     = 'L009' ";
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
        if ($model->applicantdiv == "2") {
            $query .= "     AND T1.NAMESPARE1 = '{$model->testdiv}' ";
        } else {
            if ($model->testdiv == "1") {
                $query .= "     AND T1.NAMESPARE2 = '1' ";
            } else {
                $query .= "     AND T1.NAMESPARE3 = '1' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //会場
    function getHallName($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMHALLCD AS VALUE, ";
        $query .= "     T1.EXAMHALL_NAME || ' (' || T1.S_RECEPTNO || '～' || T1.E_RECEPTNO || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";

        $query .= " ORDER BY ";
        $query .= "     T1.EXAMHALLCD ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
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
        $query .= " ) ";
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
        $query .= "          AND RECE.APPLICANTDIV  = BASE.APPLICANTDIV ";
        $query .= "          AND RECE.EXAMNO        = BASE.EXAMNO ";
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
        $query .= "              AND WISH_M.COURSECD        = PERFECT.COURSECD ";
        $query .= "              AND WISH_M.MAJORCD         = PERFECT.MAJORCD ";
        $query .= "              AND WISH_M.EXAMCOURSECD    = PERFECT.EXAMCOURSECD ";
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
        $query .= " ORDER BY RECE.RECEPTNO";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //重み:初期値(%)
        $rate = 100;

        //満点チェック
        $result = $db->query(knjl050rQuery::SelectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //満点チェック
            $checkScore = $model->score[$row["RECEPTNO"]];
            if ($checkScore != "*" && !is_numeric($checkScore)) {
                $model->setWarning("MSG901","得点欄は数値又は、＊(小文字)のみ入力可能です。\\n受験番号：".$row["EXAMNO"]);
                Query::dbCheckIn($db);
                return false;
            }
            if ($model->score[$row["RECEPTNO"]] > (int)$row["PERFECT"]) {
                $model->setWarning("MSG901","満点：".(int)$row["PERFECT"]."以下で入力してください\\n受験番号：".$row["EXAMNO"]);
                Query::dbCheckIn($db);
                return false;
            }
            //算数と計算の整合性チェック・・・中学の「2:算数」のみ
            if ($model->applicantdiv == "1" && $model->testsubclasscd == "2") {
                $score1 = $model->score[$row["RECEPTNO"]];
                $score3 = $model->score3[$row["RECEPTNO"]];
                //算数なし・計算ありの場合エラー
                if (!strlen($score1) && strlen($score3)) {
                    $model->setWarning("MSG901","【算数】の得点が未入力で【計算】だけ入力されています。\\n受験番号：".$row["EXAMNO"]);
                    Query::dbCheckIn($db);
                    return false;
                }
                //算数＜計算の場合エラー
                if (strlen($score1) && strlen($score3) && $score1 < $score3) {
                    $model->setWarning("MSG901","【計算】の得点が【算数】の得点を超えています。\\n受験番号：".$row["EXAMNO"]);
                    Query::dbCheckIn($db);
                    return false;
                }
            }
            $rate = (int) $row["RATE"];
        }

        $db->autoCommit(false);

        $query  = "DELETE FROM entexam_score_dat";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv   = '".$model->applicantdiv."'";
        $query .= "   AND testdiv        = '".$model->testdiv."'";
        $query .= "   AND exam_type      = '".$model->exam_type."'";
        $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
        $query .= "   AND receptno IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        foreach ($model->score as $receptno => $score)
        {
            //計算
            $score3 = ($model->applicantdiv == "1" && $model->testsubclasscd == "2") ? $model->score3[$receptno] : "";
            //重み
            $score2 = (strlen($score)) ? round($score * $rate / 100) : "";
            //追加
            $query  = "INSERT INTO entexam_score_dat";
            $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, testsubclasscd, attend_flg, score, score2, score3, registercd, updated)";
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
            $query .= "        , ".(!strlen($score2) ? "NULL" : $score2);
            $query .= "        , ".(!strlen($score3) ? "NULL" : $score3);
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>