<?php

require_once('for_php7.php');

class knjl250yQuery extends Query {

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
        //満点マスタ
        $query .= "WITH T_PERFECT AS ( ";
        $query .= "    SELECT TESTSUBCLASSCD ";
        $query .= "          ,PERFECT ";
        $query .= "          ,RATE ";
        $query .= "      FROM ENTEXAM_PERFECT_MST ";
        $query .= "     WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND COURSECD      = '0' ";
        $query .= "       AND MAJORCD       = '000' ";
        $query .= "       AND EXAMCOURSECD  = '0000' ";
        $query .= ") ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 AS TESTSUBCLASSCD, ";
        $query .= "     T1.NAME1 AS TESTSUBCLASSNAME, ";
        $query .= "     VALUE(P1.PERFECT, 100) AS PERFECT, ";
        $query .= "     VALUE(P1.RATE, 100) AS RATE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_PERFECT P1 ON P1.TESTSUBCLASSCD = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '{$model->ObjYear}' ";
        $query .= "     AND T1.NAMECD1     = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";
        return $query;
    }

    //グループ
    function getHallName($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMHALLCD AS VALUE, ";
        $query .= "     T1.EXAMHALLCD || ':' || T1.EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
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
    function SelectQuery($model) {
        $query  = "";
        //得点データ
        $query .= "WITH T_SCORE AS ( ";
        $query .= "    SELECT S1.RECEPTNO ";
        foreach ($model->subcdArray as $key => $sub) {
            $query .= "      ,min(CASE WHEN S1.TESTSUBCLASSCD = '{$sub["TESTSUBCLASSCD"]}' THEN S1.ATTEND_FLG END) AS ATTEND_FLG{$sub["TESTSUBCLASSCD"]} ";
            $query .= "      ,min(CASE WHEN S1.TESTSUBCLASSCD = '{$sub["TESTSUBCLASSCD"]}' THEN S1.SCORE END) AS SCORE{$sub["TESTSUBCLASSCD"]} ";
        }
        $query .= "      FROM ENTEXAM_SCORE_DAT S1 ";
        $query .= "     WHERE S1.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "       AND S1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "       AND S1.TESTDIV        = '{$model->testdiv}' ";
        $query .= "       AND S1.EXAM_TYPE      = '{$model->exam_type}' ";
        $query .= "    GROUP BY S1.RECEPTNO ";
        $query .= ") ";
        //メイン
        $query .= "SELECT T1.RECEPTNO ";
        $query .= "      ,T1.EXAMNO ";
        $query .= "      ,T2.NAME";
        foreach ($model->subcdArray as $key => $sub) {
            $query .= "  ,S1.ATTEND_FLG{$sub["TESTSUBCLASSCD"]} ";
            $query .= "  ,S1.SCORE{$sub["TESTSUBCLASSCD"]} ";
        }
        $query .= "      ,100 AS PERFECT "; //満点がない場合、100点とする。
        $query .= "      ,N2.NAME1 AS APPLICANTDIV_NAME ";
        $query .= "      ,N3.NAME1 AS TESTDIV_NAME ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "               ON T2.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "              AND T2.EXAMNO         = T1.EXAMNO ";
        $query .= "              AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "       INNER JOIN ENTEXAM_HALL_LIST_YDAT H1 ";
        $query .= "               ON H1.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "              AND H1.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "              AND H1.TESTDIV        = T1.TESTDIV ";
        $query .= "              AND H1.EXAM_TYPE      = T1.EXAM_TYPE ";
        $query .= "              AND H1.EXAMHALLCD     = '{$model->examhallcd}' ";
        $query .= "              AND H1.RECEPTNO       = T1.RECEPTNO ";

        $query .= "        LEFT JOIN T_SCORE S1 ON S1.RECEPTNO = T1.RECEPTNO ";

        $query .= "        LEFT JOIN NAME_MST N1 ";
        $query .= "               ON N1.NAMECD1 = 'Z002' ";
        $query .= "              AND N1.NAMECD2 = T2.SEX ";
        $query .= "        LEFT JOIN NAME_MST N2 ";
        $query .= "               ON N2.NAMECD1 = 'L003' ";
        $query .= "              AND N2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "        LEFT JOIN NAME_MST N3 ";
        $query .= "               ON N3.NAMECD1 = 'L004' ";
        $query .= "              AND N3.NAMECD2 = T1.TESTDIV ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV        = '{$model->testdiv}' ";
        $query .= "   AND T1.EXAM_TYPE      = '{$model->exam_type}' ";
        $query .= " ORDER BY T1.RECEPTNO";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();

        //満点チェック
        $result = $db->query(knjl250yQuery::SelectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($model->subcdArray as $key => $sub) {
                $checkScore = $model->score[$row["RECEPTNO"]]["SCORE".$sub["TESTSUBCLASSCD"]];
                if (strlen($checkScore) && $checkScore != "*" && !is_numeric($checkScore)) {
                    $model->setWarning("MSG901","得点欄は数値又は、＊(小文字)のみ入力可能です。\\n受験番号：".$row["EXAMNO"]);
                    Query::dbCheckIn($db);
                    return false;
                }
                if ($checkScore > (int)$sub["PERFECT"]) {
                    $model->setWarning("MSG901","満点：".(int)$sub["PERFECT"]."以下で入力してください\\n受験番号：".$row["EXAMNO"]);
                    Query::dbCheckIn($db);
                    return false;
                }
            }
        }

        $db->autoCommit(false);

        foreach ($model->score as $receptno => $scoreArray) {
            foreach ($model->subcdArray as $key => $sub) {
                //削除
                $query  = "DELETE FROM ENTEXAM_SCORE_DAT";
                $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}'";
                $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}'";
                $query .= "   AND TESTDIV        = '{$model->testdiv}'";
                $query .= "   AND EXAM_TYPE      = '{$model->exam_type}'";
                $query .= "   AND RECEPTNO       = '{$receptno}'";
                $query .= "   AND TESTSUBCLASSCD = '{$sub["TESTSUBCLASSCD"]}'";
                $db->query($query);
                //追加
                $score = $scoreArray["SCORE".$sub["TESTSUBCLASSCD"]];
                $rate = (int) $sub["RATE"];
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["TESTSUBCLASSCD"][TEXT]   = $sub["TESTSUBCLASSCD"];
                if ($score === '*') {
                    $data["ATTEND_FLG"][TEXT]   = "0";
                    $data["SCORE"][NUMBER]      = "";
                    $data["SCORE2"][NUMBER]     = "";
                } else {
                    $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                    $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
                    $data["SCORE2"][NUMBER]     = !strlen($score) ? "" : round($score * $rate / 100);//重み(%)
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>