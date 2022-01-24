<?php

require_once('for_php7.php');

class knjl053pQuery extends Query {
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
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
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
        $query .= "         T1.JUDGEDIV, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         L1.TOTAL, ";
        $query .= "         L1.VALUE1, ";
        $query .= "         L1.VALUE2, ";
        $query .= "         L1.VALUE3, ";
        $query .= "         L1.SCORE1, ";
        $query .= "         L1.SCORE2, ";
        $query .= "         L1.SCORE3 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_SAKUBUN_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L1.EXAMNO           = T1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' ";
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
        $query .= "     FETCH FIRST 500 ROWS ONLY ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "BAC_COUNT" && $flg != "NEXT_COUNT" ) {
            $query .= " ORDER BY RECEPTNO";
        }

        return $query;
    }

    //作文のマーク・点数を取得
    function getSukubun($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS SCORE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L048' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ENTEXAM_SAKUBUN_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAMNO IN ('".implode("','",array_keys($model->receptno))."')";
        $db->query($query);

        //作文のマーク・点数を取得
        $sukubun = array();
        $query = knjl053pQuery::getSukubun($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sukubun[$row["VALUE"]] = (int) $row["SCORE"];
        }
        $result->free();

        //追加
        foreach ($model->receptno as $receptno => $receptno) {
            $value1 = $model->value[$receptno]["VALUE1"];
            $value2 = $model->value[$receptno]["VALUE2"];
            $value3 = $model->value[$receptno]["VALUE3"];
            $score1 = $sukubun[$value1];
            $score2 = $sukubun[$value2];
            $score3 = $sukubun[$value3];

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAMNO"][TEXT]           = $receptno;
            $data["VALUE1"][TEXT]           = $value1;
            $data["VALUE2"][TEXT]           = $value2;
            $data["VALUE3"][TEXT]           = $value3;
            $data["SCORE1"][NUMBER]         = !strlen($score1) ? "" : $score1;
            $data["SCORE2"][NUMBER]         = !strlen($score2) ? "" : $score2;
            $data["SCORE3"][NUMBER]         = !strlen($score3) ? "" : $score3;
            $total = "";
            if (strlen($score1) && strlen($score2) && strlen($score3)) {
                $total = $score1 + $score2 + $score3;
            } else if (strlen($score1) && strlen($score2)) {
                $total = $score1 + $score2;
            } else if (strlen($score1) && strlen($score3)) {
                $total = $score1 + $score3;
            } else if (strlen($score2) && strlen($score3)) {
                $total = $score2 + $score3;
            } else if (strlen($score1)) {
                $total = $score1;
            } else if (strlen($score2)) {
                $total = $score2;
            } else if (strlen($score3)) {
                $total = $score3;
            }
            $data["TOTAL"][NUMBER]          = !strlen($total) ? "" : 40 + 5 * ($total - 3);
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_SAKUBUN_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>