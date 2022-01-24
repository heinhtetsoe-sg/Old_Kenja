<?php
class knjl050yQuery extends Query {

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
/***
        //高校学特入試のみ「9:加点」を表示する
        if ($model->applicantdiv == "2" && $model->testdiv == "1") {
        } else {
            $query .= " AND T1.NAMECD2     NOT IN ('9') ";
        }
        $query .= "     AND T1.NAMESPARE1  = '1' "; //得点入力科目
***/
        //満点マスタの設定が前提
        $query .= "     AND T1.NAMECD2 IN ( ";
        $query .= "         SELECT T5.TESTSUBCLASSCD ";
        $query .= "           FROM ENTEXAM_PERFECT_MST T5 ";
        $query .= "          WHERE T5.ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "            AND T5.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "            AND T5.TESTDIV       = '{$model->testdiv}' ";
        $query .= "          GROUP BY T5.TESTSUBCLASSCD ";
        $query .= "         ) ";
        $query .= "     AND T1.NAME".$model->applicantdiv." IS NOT NULL "; //中学・高校
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //会場
    function getHallName($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.EXAMHALLCD || T2.DETAIL_NO AS VALUE, ";
        $query .= "     T2.EXAMHALLCD || T2.DETAIL_NO || ':' || T1.EXAMHALL_NAME || '(' || T2.DETAIL_NO || ')' || ' (' || S1.EXAMNO || '～' || E1.EXAMNO || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_HALL_DETAIL_YDAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND T2.EXAMHALLCD   = T1.EXAMHALLCD ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT S1 ";
        $query .= "            ON S1.ENTEXAMYEAR    = T2.ENTEXAMYEAR ";
        $query .= "           AND S1.APPLICANTDIV   = T2.APPLICANTDIV ";
        $query .= "           AND S1.TESTDIV        = T2.TESTDIV ";
        $query .= "           AND S1.EXAM_TYPE      = T2.EXAM_TYPE ";
        $query .= "           AND S1.RECEPTNO       = T2.S_RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT E1 ";
        $query .= "            ON E1.ENTEXAMYEAR    = T2.ENTEXAMYEAR ";
        $query .= "           AND E1.APPLICANTDIV   = T2.APPLICANTDIV ";
        $query .= "           AND E1.TESTDIV        = T2.TESTDIV ";
        $query .= "           AND E1.EXAM_TYPE      = T2.EXAM_TYPE ";
        $query .= "           AND E1.RECEPTNO       = T2.E_RECEPTNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";

        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T1.RECEPTNO ";
        $query .= "      ,T1.EXAMNO ";
//        $query .= "      ,T2.NAME";
//        $query .= "      ,T2.NAME_KANA";
//        $query .= "      ,T2.SEX AS SEXCD ";
//        $query .= "      ,N1.NAME2 AS SEX ";
        $query .= "      ,T3.SCORE ";  //通常科目用
        $query .= "      ,T3.SCORE2 "; //重み得点用・・・入力なし。SCORE×重み(%)で更新する
        $query .= "      ,T3.SCORE3 "; //計算科目用・・・中学の「2:算数」の時、入力する
        $query .= "      ,VALUE(T5.PERFECT, 100) AS PERFECT "; //満点がない場合、満点を100点とする。
        //重みがない場合、「002:英語科」の「5:英語」「6:英語リスニング」は、重みを150%とする。
        $query .= "      ,case when W1.MAJORCD = '002' AND T5.TESTSUBCLASSCD IN('5','6') ";
        $query .= "            then VALUE(T5.RATE, 150) ";
        $query .= "            else VALUE(T5.RATE, 100) end AS RATE ";
        $query .= "      ,N2.NAME1 AS APPLICANTDIV_NAME ";
        $query .= "      ,N3.NAME1 AS TESTDIV_NAME ";
        $query .= "      ,N4.NAME".$model->applicantdiv." AS TESTSUBCLASSCD_NAME ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "               ON T1.ENTEXAMYEAR    = T2.ENTEXAMYEAR ";
        $query .= "              AND T1.EXAMNO         = T2.EXAMNO ";
        $query .= "              AND T1.APPLICANTDIV   = T2.APPLICANTDIV ";
        $query .= "       INNER JOIN V_ENTEXAM_APPLICANTDESIRE_DAT W1 ";
        $query .= "               ON T1.ENTEXAMYEAR    = W1.ENTEXAMYEAR ";
        $query .= "              AND T1.APPLICANTDIV   = W1.APPLICANTDIV ";
        $query .= "              AND T1.TESTDIV        = W1.TESTDIV ";
        $query .= "              AND T1.EXAMNO         = W1.EXAMNO ";
        $query .= "              AND W1.WISHNO         = '1' ";
        $query .= "       INNER JOIN ENTEXAM_HALL_DETAIL_YDAT W2 ";
        $query .= "               ON T1.ENTEXAMYEAR    = W2.ENTEXAMYEAR ";
        $query .= "              AND T1.APPLICANTDIV   = W2.APPLICANTDIV ";
        $query .= "              AND T1.TESTDIV        = W2.TESTDIV ";
        $query .= "              AND T1.EXAM_TYPE      = W2.EXAM_TYPE ";
        $query .= "              AND W2.EXAMHALLCD || W2.DETAIL_NO = '".$model->examhallcd."' ";
        $query .= "              AND T1.RECEPTNO BETWEEN W2.S_RECEPTNO AND W2.E_RECEPTNO ";
        $query .= "        LEFT JOIN ENTEXAM_SCORE_DAT T3 ";
        $query .= "               ON T1.ENTEXAMYEAR    = T3.ENTEXAMYEAR ";
        $query .= "              AND T1.APPLICANTDIV   = T3.APPLICANTDIV ";
        $query .= "              AND T1.TESTDIV        = T3.TESTDIV  ";
        $query .= "              AND T1.EXAM_TYPE      = T3.EXAM_TYPE ";
        $query .= "              AND T1.RECEPTNO       = T3.RECEPTNO ";
        $query .= "              AND T3.TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        //満点マスタの設定が前提
        $query .= "        INNER JOIN ENTEXAM_PERFECT_MST T5 ";
        $query .= "               ON W1.ENTEXAMYEAR    = T5.ENTEXAMYEAR ";
        $query .= "              AND W1.APPLICANTDIV   = T5.APPLICANTDIV ";
        $query .= "              AND W1.TESTDIV        = T5.TESTDIV ";
        $query .= "              AND W1.COURSECD       = T5.COURSECD ";
        $query .= "              AND W1.MAJORCD        = T5.MAJORCD ";
        $query .= "              AND W1.EXAMCOURSECD   = T5.EXAMCOURSECD ";
        $query .= "              AND T5.TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "        LEFT JOIN NAME_MST N1 ";
        $query .= "               ON N1.NAMECD1 = 'Z002' ";
        $query .= "              AND N1.NAMECD2 = T2.SEX ";
        $query .= "        LEFT JOIN NAME_MST N2 ";
        $query .= "               ON N2.NAMECD1 = 'L003' ";
        $query .= "              AND N2.NAMECD2 = T1.APPLICANTDIV ";
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query .= "        LEFT JOIN NAME_MST N3 ";
        $query .= "               ON N3.NAMECD1 = '{$namecd1}' ";
        $query .= "              AND N3.NAMECD2 = T1.TESTDIV ";
        $query .= "        LEFT JOIN V_NAME_MST N4 ";
        $query .= "               ON N4.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "              AND N4.NAMECD1 = 'L009' ";
        $query .= "              AND N4.NAMECD2 = '".$model->testsubclasscd."' ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T1.EXAM_TYPE      = '".$model->exam_type."' ";
        $query .= " ORDER BY T1.RECEPTNO";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //重み:初期値(%)
        $rate = 100;

        //満点チェック
        $result = $db->query(knjl050yQuery::SelectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //満点チェック
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
            $query .= "        ,'".(!strlen($score)  ? "0"    : "1" )."'";
            $query .= "        , ".(!strlen($score)  ? "NULL" : $score);
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