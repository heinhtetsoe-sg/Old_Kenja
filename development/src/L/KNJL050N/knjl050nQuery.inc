<?php

require_once('for_php7.php');

class knjl050nQuery extends Query {

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

    //志望区分
    function getExamcourse($model, $examcourse = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMCOURSE_NAME AS CSV_NAME, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        if ($examcourse) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD = '{$examcourse}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //受験科目
    function getTestSubclasscd($model, $nameCd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.NAME1 AS CSV_NAME, ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL ";
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
        $query .= "         SELECT ";
        $query .= "             TESTSUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_PERFECT_MST ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "             AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "             AND TESTDIV = '{$model->testdiv}' ";
        $query .= "             AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '{$model->examcourse}' ";
        $query .= "         ) ";
        $query .= "     AND T1.NAME1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg)
    {
        $query  = " WITH MAIN_T AS ( ";
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
        $query .= "     N4.NAME1 AS TESTSUBCLASSCD_NAME ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "           ON BASE.ENTEXAMYEAR   = RECE.ENTEXAMYEAR ";
        $query .= "          AND BASE.EXAMNO        = RECE.EXAMNO ";
        $query .= "          AND BASE.SHDIV         = '{$model->shdiv}' ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ";
        $query .= "           ON BASE_D.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
        $query .= "          AND BASE_D.EXAMNO      = RECE.EXAMNO ";
        $query .= "          AND BASE_D.SEQ         = '001' ";
        $query .= "          AND BASE_D.REMARK8 || '-' || BASE_D.REMARK9 || '-' || BASE_D.REMARK10 = '{$model->examcourse}' ";
        $query .= "     INNER JOIN ENTEXAM_PERFECT_MST PERFECT ";
        $query .= "            ON PERFECT.ENTEXAMYEAR    = RECE.ENTEXAMYEAR ";
        $query .= "           AND PERFECT.APPLICANTDIV   = RECE.APPLICANTDIV ";
        $query .= "           AND PERFECT.TESTDIV        = RECE.TESTDIV ";
        $query .= "           AND PERFECT.COURSECD       = BASE_D.REMARK8 ";
        $query .= "           AND PERFECT.MAJORCD        = BASE_D.REMARK9 ";
        $query .= "           AND PERFECT.EXAMCOURSECD   = BASE_D.REMARK10 ";
        $query .= "           AND PERFECT.TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ";
        $query .= "           ON SCORE.ENTEXAMYEAR      = RECE.ENTEXAMYEAR ";
        $query .= "          AND SCORE.APPLICANTDIV     = RECE.APPLICANTDIV ";
        $query .= "          AND SCORE.TESTDIV          = RECE.TESTDIV  ";
        $query .= "          AND SCORE.EXAM_TYPE        = RECE.EXAM_TYPE ";
        $query .= "          AND SCORE.RECEPTNO         = RECE.RECEPTNO ";
        $query .= "          AND SCORE.TESTSUBCLASSCD   = '".$model->testsubclasscd."' ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "            ON N1.NAMECD1 = 'Z002' ";
        $query .= "           AND N1.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN NAME_MST N2 ";
        $query .= "            ON N2.NAMECD1 = 'L003' ";
        $query .= "           AND N2.NAMECD2 = RECE.APPLICANTDIV ";
        $query .= "     LEFT JOIN NAME_MST N3 ";
        $query .= "            ON N3.NAMECD1 = 'L004' ";
        $query .= "           AND N3.NAMECD2 = RECE.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N4 ";
        $query .= "            ON N4.YEAR    = RECE.ENTEXAMYEAR ";
        $query .= "           AND N4.NAMECD1 = 'L009' ";
        $query .= "           AND N4.NAMECD2 = '".$model->testsubclasscd."' ";
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
        $query .= " FETCH FIRST 500 ROWS ONLY ";
        
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //満点チェック
        $result = $db->query(knjl050nQuery::SelectQuery($model, "CHECK"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $checkScore = $model->score[$row["RECEPTNO"]];
            if ($checkScore != "*" && !is_numeric($checkScore)) {
                $model->setWarning("MSG901","得点欄は数値又は、＊(小文字)のみ入力可能です。\\n受験番号：".$row["EXAMNO"]);
                Query::dbCheckIn($db);
                return false;
            }
            if ($checkScore > (int)$row["PERFECT"]) {
                $model->setWarning("MSG901","満点：".(int)$row["PERFECT"]."以下で入力してください\\n受験番号：".$row["EXAMNO"]);
                Query::dbCheckIn($db);
                return false;
            }
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

        foreach ($model->score as $receptno => $score) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
            if ($score === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
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