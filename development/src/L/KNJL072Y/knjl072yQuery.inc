<?php

require_once('for_php7.php');

class knjl072yQuery extends Query {

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
        $query .= "      ,T2.JUDGE_KIND ";
        $query .= "      ,N1.NAME1 AS JUDGEKIND_NAME ";
        $query .= "      ,N1.ABBV1 AS JUDGEKIND_ABBV ";
        $query .= "      ,T2.SPORTS_FLG ";
        $query .= "      ,T3.REMARK1 AS SPORTS_FLG2";
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
        if ($model->desirediv != "9") {
            $query .= "          AND W1.DESIREDIV      = '{$model->desirediv}' ";
        }
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "            ON N1.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "           AND N1.NAMECD1 = 'L025' ";
        $query .= "           AND N1.NAMECD2 = T2.JUDGE_KIND ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "            ON N2.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "           AND N2.NAMECD1 = 'L013' ";
        $query .= "           AND N2.NAMECD2 = T1.JUDGEDIV ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT C1 ";
        $query .= "               ON T1.ENTEXAMYEAR    = C1.ENTEXAMYEAR ";
        $query .= "              AND T1.EXAMNO         = C1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "            ON T3.ENTEXAMYEAR    = T2.ENTEXAMYEAR ";
        $query .= "           AND T3.EXAMNO         = T2.EXAMNO ";
        $query .= "           AND T3.SEQ = '005' ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T1.EXAM_TYPE      = '".$model->exam_type."' ";
        $query .= "   AND N2.NAMESPARE1     = '1' "; //合格者のみ表示する
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

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新前のデータを取得
        $cur_data = array();
        $result    = $db->query(knjl072yQuery::SelectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cur_data[$row["EXAMNO"]] = $row["JUDGE_KIND"];
        }

        foreach ($model->score as $examno => $val)
        {
            //更新前の値が変更されてない場合は何もしない。つまり、変更データのみ更新する。
            if ($cur_data[$examno] == $val["JUDGE_KIND"]) {
                continue;
            } else {
                //条件
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND";
                $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
                $where .= "       EXAMNO        = '".$examno."'";

                $tmp_data = array();
                $tmp_data["JUDGE_KIND"] = $val["JUDGE_KIND"];

                //基礎データ
                $data = array();
                $data["JUDGE_KIND"][TEXT]       = $tmp_data["JUDGE_KIND"];
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