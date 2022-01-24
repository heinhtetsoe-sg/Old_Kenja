<?php

require_once('for_php7.php');

class knjl050qQuery extends Query {
    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        if ($namecd1 == "L004") {
            $query .= "     AND NAMECD2 IN ('3','4','5') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    function getTestSubclasscd($model, $field) {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || ".$field." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     ".$field." IS NOT NULL AND ";
        //満点マスタの設定が前提
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     TESTSUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PERFECT_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                     TESTDIV         = '{$model->testdiv}' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg) {

        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         L1.ATTEND_FLG, ";
        $query .= "         L1.SCORE, ";
        $query .= "         L1.SCORE2, ";
        $query .= "         VALUE(S4.PERFECT, 100) AS PERFECT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        $query .= "         INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 ENTEXAMYEAR, ";
        $query .= "                 APPLICANTDIV, ";
        $query .= "                 TESTDIV, ";
        $query .= "                 TESTSUBCLASSCD, ";
        $query .= "                 MAX(PERFECT) AS PERFECT ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_PERFECT_MST ";
        $query .= "             GROUP BY ";
        $query .= "                 ENTEXAMYEAR, ";
        $query .= "                 APPLICANTDIV, ";
        $query .= "                 TESTDIV, ";
        $query .= "                 TESTSUBCLASSCD ";
        $query .= "         ) S4 ON S4.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S4.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S4.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND S4.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L1.EXAM_TYPE        = T1.EXAM_TYPE ";
        $query .= "             AND L1.RECEPTNO         = T1.RECEPTNO ";
        $query .= "             AND L1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "         AND VALUE(T1.JUDGEDIV,'') <> '4' ";
        //一般入試選択で「基準テストを含める・含めない・のみ」
        if (SCHOOLKIND == "H" && $model->testdiv == "5") {
            if ($model->field["KIJUN_TEST_DIV"] == "1") {
                //全て(一般入試、基準テスト)
            } else if ($model->field["KIJUN_TEST_DIV"] == "2") {
                //一般入試のみ
                $query .= "     AND S1.TESTDIV  = '5' ";
            } else if ($model->field["KIJUN_TEST_DIV"] == "3") {
                //基準テストのみ
                $query .= "     AND S1.TESTDIV != '5' ";
            }
        }
        if ($flg == "") {
            if ($model->cmd == "back") {
                $query .= "   AND T1.RECEPTNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO DESC ";
            } else if ($model->cmd == "next") {
                $query .= "   AND T1.RECEPTNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        } else if ($flg == "BAC_COUNT" || $flg == "NEXT_COUNT") {
            if ($flg == "BAC_COUNT") {
                $query .= "   AND T1.RECEPTNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO DESC ";
            } else if ($flg == "NEXT_COUNT") {
                $query .= "   AND T1.RECEPTNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        } else if ($flg == "CHECK") {
                $query .= "   AND T1.RECEPTNO >= '".$model->s_receptno."' ";
        }
        $query .= "     FETCH FIRST 5000 ROWS ONLY ";
        $query .= " ) ";

        $query .= " , T_RECNO AS ( ";
        $query .= "    SELECT ";
        $query .= "        T1.ENTEXAMYEAR, ";
        $query .= "        T1.APPLICANTDIV, ";
        $query .= "        T2.EXAMNO , ";
        $query .= "        MAX(RECNO) AS RECNO ";
        $query .= "    FROM ";
        $query .= "        ENTEXAM_NO_DAT T1 ";
        $query .= "        INNER JOIN MAIN_T T2 ON T2.EXAMNO BETWEEN T1.EXAMNO_FROM AND T1.EXAMNO_TO ";
        $query .= "    WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "    GROUP BY ";
        $query .= "        T1.ENTEXAMYEAR, ";
        $query .= "        T1.APPLICANTDIV, ";
        $query .= "        T2.EXAMNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.ATTEND_FLG, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.SCORE2, ";
        $query .= "     VALUE(L3.PERFECT, T1.PERFECT) AS PERFECT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= "     LEFT JOIN T_RECNO L1 ON L1.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_NO_DAT L2 ON L2.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "         AND L2.RECNO = L1.RECNO ";
        $query .= "     LEFT JOIN ENTEXAM_PERFECT_MST L3 ON L3.ENTEXAMYEAR = L2.ENTEXAMYEAR ";
        $query .= "         AND L3.APPLICANTDIV = L2.APPLICANTDIV ";
        $query .= "         AND L3.TESTDIV = L2.TESTDIV ";
        $query .= "         AND L3.COURSECD = L2.COURSECD ";
        $query .= "         AND L3.MAJORCD = L2.MAJORCD ";
        $query .= "         AND L3.EXAMCOURSECD = L2.EXAMCOURSECD ";
        $query .= "         AND L3.TESTSUBCLASSCD = '{$model->testsubclasscd}' ";

        if ($flg != "BAC_COUNT" && $flg != "NEXT_COUNT" ) {
            $query .= " ORDER BY T1.RECEPTNO";
        }

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();

        //満点チェック
        $result = $db->query(knjl050qQuery::SelectQuery($model, "CHECK"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $checkScore = $model->score[$row["RECEPTNO"]];
            if ($checkScore != "*" && !is_numeric($checkScore) && strlen($checkScore)) {
                $model->setWarning("MSG901","得点欄は数値又は、＊(小文字)のみ入力可能です。\\n受験番号：".$row["RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
            if ($checkScore > (int)$row["PERFECT"] && strlen($checkScore)) {
                $model->setWarning("MSG901","満点：".(int)$row["PERFECT"]."以下で入力してください\\n受験番号：".$row["RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
        }

        $db->autoCommit(false);

        //データ取得
        $query  = " SELECT * FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAM_TYPE      = '1' ";
        $query .= "   AND TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "   AND RECEPTNO       = '".$model->hid_receptno2."' ";
        $scoreRow = array();
        $scoreRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //削除
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAM_TYPE      = '1' ";
        $query .= "   AND TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "   AND RECEPTNO       = '".$model->hid_receptno2."' ";
        $db->query($query);

        //追加
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]        = 1;
        $data["RECEPTNO"][TEXT]         = $model->hid_receptno2;
        $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
        if ($model->field["NYURYOKU"] == "1") {
            if ($model->score[$model->hid_receptno2] === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
                $data["SCORE2"][NUMBER]     = $scoreRow["SCORE2"];
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($model->score[$model->hid_receptno2]) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($model->score[$model->hid_receptno2]) ? "" : $model->score[$model->hid_receptno2];
                $data["SCORE2"][NUMBER]     = $scoreRow["SCORE2"];
            }
        } else {
            if ($model->score2[$model->hid_receptno2] === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = $scoreRow["SCORE"];
                $data["SCORE2"][NUMBER]     = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($model->score2[$model->hid_receptno2]) ? "" : "1";
                $data["SCORE"][NUMBER]      = $scoreRow["SCORE"];
                $data["SCORE2"][NUMBER]     = !strlen($model->score2[$model->hid_receptno2]) ? "" : $model->score2[$model->hid_receptno2];
            }
        }
/***
        if ($model->score[$model->hid_receptno2] === '*') {
            $data["ATTEND_FLG"][TEXT]   = "0";
            $data["SCORE"][NUMBER]      = "";
            $data["SCORE2"][NUMBER]     = "";
        } else {
            $data["ATTEND_FLG"][TEXT]   = !strlen($model->score[$model->hid_receptno2]) ? "" : "1";
            $data["SCORE"][NUMBER]      = !strlen($model->score[$model->hid_receptno2]) ? "" : $model->score[$model->hid_receptno2];
            $model->score2[$model->hid_receptno2] = ($model->score2[$model->hid_receptno2] === '*') ? "":$model->score2[$model->hid_receptno2];
            $data["SCORE2"][NUMBER]     = !strlen($model->score2[$model->hid_receptno2]) ? "" : $model->score2[$model->hid_receptno2];
        }
***/
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>