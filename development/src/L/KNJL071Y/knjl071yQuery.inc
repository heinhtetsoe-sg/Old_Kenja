<?php

require_once('for_php7.php');

class knjl071yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $isShdiv = false) {
        $query  = " WITH T_SHDIV(VALUE, LABEL, NAMESPARE2) AS ( ";
        $query .= "     VALUES('9', '9' || ':' || '全員', '') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        //専併区分コンボボックス
        if ($isShdiv) {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     VALUE,";
            $query .= "     LABEL, ";
            $query .= "     NAMESPARE2 ";
            $query .= " FROM ";
            $query .= "     T_SHDIV ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより取得
    function getNameMst2($namecd1, $year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //志望区分
    function getDesirediv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.DESIREDIV AS VALUE, ";
        $query .= "     T1.DESIREDIV || ':' || L1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "          ON L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND L1.COURSECD     = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= "         AND L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.WISHNO       = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        //成績
        $total = "";
        if ($model->applicantdiv == "1") {
            $total = "T1.TOTAL2"; //中学
        } else if ($model->applicantdiv == "2" && $model->testdiv == "1") {
            $total = ($model->rate_div == "2") ? "T1.TOTAL1" : "T1.TOTAL3"; //高校学特
        } else if ($model->applicantdiv == "2" && $model->testdiv == "2") {
            $total = "smallint(C1.AVERAGE_ALL)"; //高校推薦
        } else {
            $total = ($model->rate_div == "2") ? "T1.TOTAL2" : "T1.TOTAL4"; //高校一般
        }

        $query  = "SELECT T1.RECEPTNO ";
        $query .= "      ,T1.EXAMNO ";
        $query .= "      ,{$total} as TOTAL4 ";
        $query .= "      ,RANK() OVER(ORDER BY value(T1.ATTEND_ALL_FLG,'0') DESC, value({$total},-1) DESC) AS TOTAL_RANK4 ";
        $query .= "      ,T1.ATTEND_ALL_FLG ";
        $query .= "      ,T1.JUDGEDIV ";
        $query .= "      ,N1.NAME1 AS JUDGEDIV_NAME ";
        $query .= "      ,N1.ABBV1 AS JUDGEDIV_ABBV ";
        $query .= "      ,W1.DESIREDIV ";
        $query .= "      ,CASE WHEN T2.TESTDIV = T1.TESTDIV THEN T2.SLIDE_FLG END AS SLIDE_FLG ";
        $query .= "      ,T2.SHIFT_DESIRE_FLG ";
        $query .= "      ,T2.SELECT_SUBCLASS_DIV ";
        $query .= "      ,C1.AVERAGE_ALL ";
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
        $query .= "              AND W1.DESIREDIV      = '{$model->desirediv}' ";
        if (strlen($model->recom_kind)) {
            $recom_kind = ($model->applicantdiv == "2" && $model->testdiv == "2") ? "W1.RECOM_KIND" : "W1.SHDIV";
            if ($model->recom_kind != "9") $query .= "          AND {$recom_kind}     = '{$model->recom_kind}' ";
        }
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "            ON N1.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "           AND N1.NAMECD1 = 'L013' ";
        $query .= "           AND N1.NAMECD2 = T1.JUDGEDIV ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT C1 ";
        $query .= "               ON T1.ENTEXAMYEAR    = C1.ENTEXAMYEAR ";
        $query .= "              AND T1.EXAMNO         = C1.EXAMNO ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T1.EXAM_TYPE      = '".$model->exam_type."' ";
        //対象者ラジオボタン 1:外部生のみ 2:内部生のみ 3:全て (内部生の受験番号:6XXXX)
        if ($model->inout == "1") $query .= "   AND T1.EXAMNO NOT LIKE '6%' ";
        if ($model->inout == "2") $query .= "   AND T1.EXAMNO     LIKE '6%' ";
        //対象者(帰国生)ラジオボタン 1:帰国生除く 2:帰国生のみ
        if ($model->kikoku == "2") {
            $query .= "   AND VALUE(T2.INTERVIEW_ATTEND_FLG, '0')  = '1' ";
        } else {
            $query .= "   AND VALUE(T2.INTERVIEW_ATTEND_FLG, '0') != '1' ";
        }
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") $query .= "     value(T1.ATTEND_ALL_FLG,'0') DESC, value({$total},-1) DESC, ";
        $query .= "     T1.EXAMNO ";
        return $query;
    }

    //受験コースを取得
    function getCourseQuery($model, $desirediv, $wishno) {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     DESIREDIV       = '".$desirediv."' AND ";
        $query .= "     WISHNO          = '".$wishno."' ";

        return $query;
    }

    //特進チャレンジ者の1:学特入試での進学コース合格を取得
    function getJudgeCourse($model, $examno) {
        $query  = " WITH MAX_WISHDIV AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         DESIREDIV, ";
        $query .= "         MAX(WISHNO) AS WISHNO "; //進学コース
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST ";
        $query .= "     GROUP BY ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         DESIREDIV ";
        $query .= "     ) ";
        $query .= " , T_WISHDIV_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.ENTEXAMYEAR, ";
        $query .= "         W1.APPLICANTDIV, ";
        $query .= "         W1.TESTDIV, ";
        $query .= "         W1.DESIREDIV, ";
        $query .= "         W1.WISHNO, ";
        $query .= "         W1.COURSECD, ";
        $query .= "         W1.MAJORCD, ";
        $query .= "         W1.EXAMCOURSECD ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST W1 ";
        $query .= "         INNER JOIN MAX_WISHDIV W2 ";
        $query .= "             ON  W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
        $query .= "             AND W2.APPLICANTDIV = W1.APPLICANTDIV ";
        $query .= "             AND W2.TESTDIV = W1.TESTDIV ";
        $query .= "             AND W2.DESIREDIV = W1.DESIREDIV ";
        $query .= "             AND W2.WISHNO = W1.WISHNO ";
        $query .= "     ) ";
        $query .= "SELECT T2.RECEPTNO ";
        $query .= "      ,T2.EXAMNO ";
        $query .= "      ,T2.JUDGEDIV ";
        $query .= "      ,W1.COURSECD ";
        $query .= "      ,W1.MAJORCD ";
        $query .= "      ,W1.EXAMCOURSECD ";
        $query .= "  FROM ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "             ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.TESTDIV      = T1.TESTDIV "; //出願区分(1:学特入試)
        $query .= "             AND T2.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "       INNER JOIN T_WISHDIV_MST W1 ";
        $query .= "             ON  W1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND W1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND W1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND W1.DESIREDIV    = T1.DESIREDIV ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '{$model->ObjYear}'";
        $query .= "   AND T1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND T1.EXAMNO         = '{$examno}' ";
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //名称マスタの取得（判定コード）
        $opt_judge = array();
        $result = $db->query(knjl071yQuery::getNameMst2("L013", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_judge[$row["NAMECD2"]] = $row["NAMESPARE1"];   //合格扱い
        }

        //更新前のデータを取得
        $cur_data = array();
        $result    = $db->query(knjl071yQuery::SelectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cur_data[$row["EXAMNO"]] = $row["JUDGEDIV"];
            $cur_data["DESIREDIV"][$row["EXAMNO"]] = $row["DESIREDIV"];
            $cur_data["SLIDE_FLG"][$row["EXAMNO"]] = $row["SLIDE_FLG"];
            $cur_data["SHIFT_DESIRE_FLG"][$row["EXAMNO"]] = $row["SHIFT_DESIRE_FLG"];
            $cur_data["SELECT_SUBCLASS_DIV"][$row["EXAMNO"]] = $row["SELECT_SUBCLASS_DIV"];
            $cur_data["AVERAGE_ALL"][$row["EXAMNO"]] = $row["AVERAGE_ALL"];
        }

        foreach ($model->score as $examno => $val)
        {
            //更新前の値が変更されてない場合は何もしない。つまり、変更データのみ更新する。
            if ($cur_data[$examno] == $val["JUDGEDIV"]) {
                continue;
            } else {
                //条件
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND";
                $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
                $where .= "       TESTDIV       = '".$model->testdiv."' AND";
                $where .= "       EXAMNO        = '".$examno."'";
                //受付データ
                $data = array();
                $data["JUDGEDIV"][TEXT]     = $val["JUDGEDIV"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);

                //基礎データ
                $tmp_data = array();
                $tmp_data["SUC_COURSECD"] = "";
                $tmp_data["SUC_MAJORCD"] = "";
                $tmp_data["SUC_COURSECODE"] = "";
                $tmp_data["SPECIAL_MEASURES"] = "";
                $tmp_data["PROCEDUREDIV"] = "";
                $tmp_data["ENTDIV"] = "";

                $opt_course = array();
                if ($val["JUDGEDIV"] == "3" && $cur_data["SLIDE_FLG"][$examno] == "1") {
                    $result    = $db->query(knjl071yQuery::getCourseQuery($model, $cur_data["DESIREDIV"][$examno], "2"));
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $tmp_data["JUDGEMENT"]      = $val["JUDGEDIV"];
                        $tmp_data["SUC_COURSECD"]   = $row["COURSECD"];
                        $tmp_data["SUC_MAJORCD"]    = $row["MAJORCD"];
                        $tmp_data["SUC_COURSECODE"] = $row["EXAMCOURSECD"];
                    }
                } else if ($val["JUDGEDIV"] == "5" && $cur_data["SHIFT_DESIRE_FLG"][$examno] == "1" && $cur_data["SLIDE_FLG"][$examno] == "1" && ($cur_data["DESIREDIV"][$examno] == "1" || $cur_data["DESIREDIV"][$examno] == "3")) {
                    //◆「特別判定合格は、学特入試の進学に加え特進も可とする」に伴う修正
                    //合格コースについて、
                    //①画面の志望区分「1:普通科特進」「3:英語科特進」かつスライド希望「有」の場合、評定合計(下記条件)で特進コースまたは進学コースをセットする。
                    //--評定合計が特進は普通科・英語科とも43以上、進学は普通科35以上、英語科38以上・・・【入試要項に記載されている特別判定の条件】
                    //※つまり、第１志望または第２志望が合格コースとなる。
                    if (strlen($cur_data["AVERAGE_ALL"][$examno]) && (int) $cur_data["AVERAGE_ALL"][$examno] >= 35 && $cur_data["DESIREDIV"][$examno] == "1") $wishno = "2";
                    if (strlen($cur_data["AVERAGE_ALL"][$examno]) && (int) $cur_data["AVERAGE_ALL"][$examno] >= 38 && $cur_data["DESIREDIV"][$examno] == "3") $wishno = "2";
                    if (strlen($cur_data["AVERAGE_ALL"][$examno]) && (int) $cur_data["AVERAGE_ALL"][$examno] >= 43) $wishno = "1";
                    if (!strlen($wishno)) $wishno = "1"; //念のため
                    $result    = $db->query(knjl071yQuery::getCourseQuery($model, $cur_data["DESIREDIV"][$examno], $wishno));
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $tmp_data["JUDGEMENT"]      = $val["JUDGEDIV"];
                        $tmp_data["SUC_COURSECD"]   = $row["COURSECD"];
                        $tmp_data["SUC_MAJORCD"]    = $row["MAJORCD"];
                        $tmp_data["SUC_COURSECODE"] = $row["EXAMCOURSECD"];
                    }
                } else if ($val["JUDGEDIV"] == "5" && $cur_data["SHIFT_DESIRE_FLG"][$examno] == "1") {
                    //◆「特別判定合格は、学特入試の進学に加え特進も可とする」に伴う修正
                    //合格コースについて、
                    //②画面の志望区分「1:普通科特進」「3:英語科特進」の場合、特進コースをセットする。
                    //③画面の志望区分「2:普通科進学」「4:英語科進学」の場合、進学コースをセットする。
                    //※つまり、第１志望が合格コースとなる。
                    $result    = $db->query(knjl071yQuery::getCourseQuery($model, $cur_data["DESIREDIV"][$examno], "1"));
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $tmp_data["JUDGEMENT"]      = $val["JUDGEDIV"];
                        $tmp_data["SUC_COURSECD"]   = $row["COURSECD"];
                        $tmp_data["SUC_MAJORCD"]    = $row["MAJORCD"];
                        $tmp_data["SUC_COURSECODE"] = $row["EXAMCOURSECD"];
                    }
                } elseif ($opt_judge[$val["JUDGEDIV"]] == "1") {      //「1:合格」扱い
                    $result    = $db->query(knjl071yQuery::getCourseQuery($model, $cur_data["DESIREDIV"][$examno], "1"));
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $tmp_data["JUDGEMENT"]      = $val["JUDGEDIV"];
                        $tmp_data["SUC_COURSECD"]   = $row["COURSECD"];
                        $tmp_data["SUC_MAJORCD"]    = $row["MAJORCD"];
                        $tmp_data["SUC_COURSECODE"] = $row["EXAMCOURSECD"];
                    }
                //3:一般入試で特進チャレンジ者の場合、1:学特入試での進学コース合格に戻す。
                //理由：特進チャレンジ者は、1:学特入試で進学コース合格して、合格を保有したまま3:一般入試の特進コースを再受験しています。
                } else if ($model->applicantdiv == "2" && $model->testdiv == "3" && $cur_data["SELECT_SUBCLASS_DIV"][$examno] == "1") {
                    $result    = $db->query(knjl071yQuery::getJudgeCourse($model, $examno));
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $tmp_data["JUDGEMENT"]      = $row["JUDGEDIV"];
                        $tmp_data["SUC_COURSECD"]   = $row["COURSECD"];
                        $tmp_data["SUC_MAJORCD"]    = $row["MAJORCD"];
                        $tmp_data["SUC_COURSECODE"] = $row["EXAMCOURSECD"];
                    }
                } else {
                    $tmp_data["JUDGEMENT"] = $val["JUDGEDIV"];
                }
                //条件
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND";
                $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
                $where .= "       EXAMNO        = '".$examno."'";
                $data = array();
                $data["JUDGEMENT"][TEXT]        = $tmp_data["JUDGEMENT"];
                $data["SUC_COURSECD"][TEXT]     = $tmp_data["SUC_COURSECD"];
                $data["SUC_MAJORCD"][TEXT]      = $tmp_data["SUC_MAJORCD"];
                $data["SUC_COURSECODE"][TEXT]   = $tmp_data["SUC_COURSECODE"];
                $data["SPECIAL_MEASURES"][TEXT] = $tmp_data["SPECIAL_MEASURES"];
                $data["PROCEDUREDIV"][TEXT]     = $tmp_data["PROCEDUREDIV"];
                $data["ENTDIV"][TEXT]           = $tmp_data["ENTDIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>