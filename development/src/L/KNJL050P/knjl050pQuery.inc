<?php

require_once('for_php7.php');

class knjl050pQuery extends Query {
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    function getTestSubclasscd($model) {
        $subNo = ($model->applicantdiv == "2") ? "2" : "1";

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME{$subNo} AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME{$subNo} IS NOT NULL AND ";
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
        $query  = " WITH PERFECT_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.* ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PERFECT_MST S1 ";
        $query .= "         INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 TESTSUBCLASSCD, ";
        $query .= "                 min(COURSECD||MAJORCD||EXAMCOURSECD) as COURSE ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_PERFECT_MST ";
        $query .= "             WHERE ";
        $query .= "                 ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                 APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                 TESTDIV         = '{$model->testdiv}' ";
        $query .= "             GROUP BY ";
        $query .= "                 TESTSUBCLASSCD ";
        $query .= "         ) S2 ON S2.TESTSUBCLASSCD   = S1.TESTSUBCLASSCD ";
        $query .= "             AND S2.COURSE           = S1.COURSECD||S1.MAJORCD||S1.EXAMCOURSECD ";
        $query .= "     WHERE ";
        $query .= "         S1.ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "         S1.APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "         S1.TESTDIV         = '{$model->testdiv}' ";
        $query .= "     ) ";

        $query .= " , MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         T1.JUDGEDIV, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         L1.ATTEND_FLG, ";
        $query .= "         L1.SCORE, ";
        $query .= "         L1.STD_SCORE, ";
        $query .= "         VALUE(S4.PERFECT, 100) AS PERFECT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S2 ";
        $query .= "              ON S2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S2.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S2.EXAMNO           = T1.EXAMNO ";
        $query .= "             AND S2.SEQ              = '001' ";
        $query .= "         INNER JOIN PERFECT_T S4 ";
        $query .= "              ON S4.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
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
        $query .= "         AND T1.RECEPTNO BETWEEN '{$model->s_receptno}' AND '{$model->e_receptno}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();

        //保持
        $keep = array();

        //満点チェック
        $result = $db->query(knjl050pQuery::SelectQuery($model, "CHECK"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $keep[$row["RECEPTNO"]] = $row;

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

        //削除
        $query  = " DELETE FROM entexam_score_dat ";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."' ";
        $query .= "   AND applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "   AND testdiv        = '".$model->testdiv."' ";
        $query .= "   AND exam_type      = '1' ";
        $query .= "   AND testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "   AND receptno IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        //追加
        foreach ($model->score as $receptno => $score) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = 1;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
            if ($score === '*' || $keep[$receptno]["JUDGEDIV"] == "4") {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
//                $data["STD_SCORE"][NUMBER]  = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
//                $data["STD_SCORE"][NUMBER]  = !strlen($score) ? "" : $keep[$receptno]["STD_SCORE"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>