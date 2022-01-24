<?php

require_once('for_php7.php');

class knjl013yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学科・コース取得
    function getDesirediv($model)
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $query .= "     VALUE(T3.INTERVIEW_ATTEND_FLG, '0') AS KIKOKU, ";
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->field["APPLICANTDIV"] == "2" && ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "3")) {
            $query .= " CASE WHEN SUBSTR(T1.EXAMNO,1,1) = '6' THEN '6' ELSE '0' END AS NAIBU, ";
        }
        $query .= "     T1.DESIREDIV || '-' || VALUE(T1.RECOM_KIND,'0') AS VALUE, ";
        $query .= "     T1.DESIREDIV || '-' || VALUE(T1.RECOM_KIND,'0') || ':' || L2.EXAMCOURSE_NAME || ' ' || VALUE(N1.NAME1,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTDESIRE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "         ON  T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "         ON  N1.NAMECD1 = 'L023' ";
        $query .= "         AND N1.NAMECD2 = T1.RECOM_KIND ";
        $query .= "     LEFT JOIN ENTEXAM_WISHDIV_MST L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV = T1.TESTDIV ";
        $query .= "         AND L1.DESIREDIV = T1.DESIREDIV ";
        $query .= "         AND L1.WISHNO = '1' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L2 ";
        $query .= "         ON  L2.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "         AND L2.TESTDIV = L1.TESTDIV ";
        $query .= "         AND L2.COURSECD = L1.COURSECD ";
        $query .= "         AND L2.MAJORCD = L1.MAJORCD ";
        $query .= "         AND L2.EXAMCOURSECD = L1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $query .= "     KIKOKU, ";
        $query .= "     VALUE ";
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->field["APPLICANTDIV"] == "2" && ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "3")) {
            $query .= " ,NAIBU ";
        }

        return $query;
    }

    //座席番号生成済みリスト取得
    function getReceptCnt($model)
    {
        $query  = "";
        $query .= " SELECT ";
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $query .= "     VALUE(T1.INTERVIEW_ATTEND_FLG, '0') AS KIKOKU, ";
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->field["APPLICANTDIV"] == "2" && ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "3")) {
            $query .= " CASE WHEN SUBSTR(T3.EXAMNO,1,1) = '6' THEN '6' ELSE '0' END AS NAIBU, ";
        }
        $query .= "     T3.DESIREDIV || '-' || VALUE(T3.RECOM_KIND,'0') || ':' || L2.EXAMCOURSE_NAME || ' ' || VALUE(N1.NAME1,'') AS LABEL, ";
        $query .= "     COUNT(T2.RECEPTNO) AS CNT_RECEPTNO, ";
        $query .= "     MIN(T2.RECEPTNO) AS S_RECEPTNO, ";
        $query .= "     MAX(T2.RECEPTNO) AS E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T2 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "         ON  T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO = T2.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTDESIRE_DAT T3 ";
        $query .= "         ON  T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T3.TESTDIV = T2.TESTDIV ";
        $query .= "         AND T3.EXAMNO = T2.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "         ON  N1.NAMECD1 = 'L023' ";
        $query .= "         AND N1.NAMECD2 = T3.RECOM_KIND ";
        $query .= "     LEFT JOIN ENTEXAM_WISHDIV_MST L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV = T3.TESTDIV ";
        $query .= "         AND L1.DESIREDIV = T3.DESIREDIV ";
        $query .= "         AND L1.WISHNO = '1' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L2 ";
        $query .= "         ON  L2.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "         AND L2.TESTDIV = L1.TESTDIV ";
        $query .= "         AND L2.COURSECD = L1.COURSECD ";
        $query .= "         AND L2.MAJORCD = L1.MAJORCD ";
        $query .= "         AND L2.EXAMCOURSECD = L1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T2.ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     T2.APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T2.TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $query .= " GROUP BY ";
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $query .= "     VALUE(T1.INTERVIEW_ATTEND_FLG, '0'), ";
        $query .= "     T3.DESIREDIV, ";
        $query .= "     VALUE(T3.RECOM_KIND,'0'), ";
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->field["APPLICANTDIV"] == "2" && ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "3")) {
            $query .= " CASE WHEN SUBSTR(T3.EXAMNO,1,1) = '6' THEN '6' ELSE '0' END, ";
        }
        $query .= "     L2.EXAMCOURSE_NAME, ";
        $query .= "     VALUE(N1.NAME1,'') ";
        $query .= " ORDER BY ";
        $query .= "     S_RECEPTNO ";

        return $query;
    }

    //受験番号取得
    function getReceptExamno($model, $receptno) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     T2.APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     T2.TESTDIV         = '{$model->field["TESTDIV"]}' AND ";
        $query .= "     T2.RECEPTNO        = '{$receptno}' ";
        return $query;
    }

    //受験番号を配列にセット（１件も無い場合、存在エラー）
    function existCheck($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "SELECT T1.EXAMNO ";
        $query .= "  FROM ENTEXAM_APPLICANTDESIRE_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "           ON  T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "           AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND T3.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->field["TESTDIV"]."' ";
        //中学(3:第二回一般)・高校(3:一般)を実行した場合、一般入試希望者(再受験者)のうち合格者を除く
        if ($model->field["TESTDIV"] == "3") {
            $query .= "   AND NOT EXISTS ( ";
            $query .= "       SELECT 'X' ";
            $query .= "         FROM ENTEXAM_RECEPT_DAT B1 ";
            $query .= "              INNER JOIN ENTEXAM_APPLICANTBASE_DAT B2 ";
            $query .= "                  ON  B2.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "                  AND B2.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "                  AND B2.EXAMNO       = B1.EXAMNO ";
            $query .= "                  AND VALUE(B2.SELECT_SUBCLASS_DIV,'0') <> '1' "; //1:特進チャレンジ受験者は対象外
            $query .= "              LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = B1.JUDGEDIV ";
            $query .= "        WHERE B1.ENTEXAMYEAR    = '{$model->ObjYear}' ";
            $query .= "          AND B1.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "          AND B1.TESTDIV        < '{$model->field["TESTDIV"]}' ";
            $query .= "          AND B1.EXAMNO         = T1.EXAMNO ";
            $query .= "          AND N1.NAMESPARE1     = '1' ";//合格
            $query .= "   ) ";
        }
        $query .= " ORDER BY T1.EXAMNO ";

        $examno = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $examno[] = $row["EXAMNO"];
        }

        Query::dbCheckIn($db);

        return $examno;
    }

    //重複データチェック(同じ受験番号が違う入試制度に在る場合、重複エラー）
    function existCheck2($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "SELECT RECEPTNO ";
        $query .= "      ,EXAMNO ";
        $query .= "      ,N1.NAME1 AS APPLICANTDIV  ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT T1  ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.APPLICANTDIV  ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "   AND EXAMNO IN ('".implode("','",$model->examno)."') ";
        $query .= "   AND (APPLICANTDIV != '".$model->field["APPLICANTDIV"]."')";
        $query .= " ORDER BY RECEPTNO ";

        $existdata = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        return $existdata;
    }

    //生成対象の受験番号を配列にセット
    function getExamno($db, $model, $i) {
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->field["APPLICANTDIV"] == "2" && ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "3")) {
            $query .= " CASE WHEN SUBSTR(T1.EXAMNO,1,1) = '6' THEN '6' ELSE '0' END AS NAIBU, ";
            list ($kikoku, $desirediv, $recomKind, $naibu) = preg_split("/-/", $model->selectData[$i]);
        } else {
            list ($kikoku, $desirediv, $recomKind) = preg_split("/-/", $model->selectData[$i]);
        }

        $query  = "";
        $query .= "SELECT T1.EXAMNO ";
        $query .= "  FROM ENTEXAM_APPLICANTDESIRE_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "           ON  T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "           AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND T3.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND T1.DESIREDIV      = '".$desirediv."' ";
        //帰国生対応(高校のみ)
        if ("0" < $kikoku) {
            $query .= "   AND VALUE(T3.INTERVIEW_ATTEND_FLG, '0')  = '1' ";
        } else {
            $query .= "   AND VALUE(T3.INTERVIEW_ATTEND_FLG, '0') != '1' ";
        }
        if ("0" < $recomKind) {
//echo "desirediv=" .$desirediv .", recomKind=" .$recomKind ."<BR>";
            $query .= "   AND T1.RECOM_KIND = '".$recomKind."' ";
        }
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->field["APPLICANTDIV"] == "2" && ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "3")) {
            if ("0" < $naibu) {
//echo "desirediv=" .$desirediv .", recomKind=" .$recomKind ."<BR>";
                $query .= "   AND SUBSTR(T1.EXAMNO,1,1)  = '6' ";
            } else {
                $query .= "   AND SUBSTR(T1.EXAMNO,1,1) != '6' ";
            }
        }
        //中学(3:第二回一般)・高校(3:一般)を実行した場合、一般入試希望者のうち合格者を除く
        if ($model->field["TESTDIV"] == "3") {
            $query .= "   AND NOT EXISTS ( ";
            $query .= "       SELECT 'X' ";
            $query .= "         FROM ENTEXAM_RECEPT_DAT B1 ";
            $query .= "              INNER JOIN ENTEXAM_APPLICANTBASE_DAT B2 ";
            $query .= "                  ON  B2.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "                  AND B2.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "                  AND B2.EXAMNO       = B1.EXAMNO ";
            $query .= "                  AND VALUE(B2.SELECT_SUBCLASS_DIV,'0') <> '1' "; //1:特進チャレンジ受験者は対象外
            $query .= "              LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = B1.JUDGEDIV ";
            $query .= "        WHERE B1.ENTEXAMYEAR    = '{$model->ObjYear}' ";
            $query .= "          AND B1.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "          AND B1.TESTDIV        < '{$model->field["TESTDIV"]}' ";
            $query .= "          AND B1.EXAMNO         = T1.EXAMNO ";
            $query .= "          AND N1.NAMESPARE1     = '1' ";//合格
            $query .= "   ) ";
        }
        $query .= " ORDER BY T1.EXAMNO ";

        $examno = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $examno[] = $row["EXAMNO"];
        }

        return $examno;
    }

    function ExecuteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除処理（一旦すべてを削除する）
        $query  = "DELETE FROM entexam_recept_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->field["APPLICANTDIV"]."'";
        $query .= "   AND testdiv      = '".$model->field["TESTDIV"]."'";
        $db->query($query);

        $query  = "DELETE FROM entexam_score_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->field["APPLICANTDIV"]."'";
        $query .= "   AND testdiv      = '".$model->field["TESTDIV"]."'";
        $db->query($query);

        //追加処理
        $max_receptno = $end_receptno = 0;
        for ($i = 0; $i < get_count($model->selectData); $i++) {
            $model->examno = array();
            $model->examno = knjl013yQuery::getExamno($db, $model, $i);
            if (0 < get_count($model->examno)) {
                $max_receptno = (0 < $i) ? $end_receptno : 0;
                foreach ($model->examno as $key => $val) {
                    //座席番号の値
                    if ($model->recept_div == "1") {
                        //受験番号をコピー
                        $receptno = $val;
                    } else {
                        //連番(受験番号順)
                        $receptno = $key + 1 + $max_receptno;
                        $receptno = sprintf("%05d",$receptno);
                    }
//echo "receptno=" .$receptno .", examno=" .$val ."<BR>";
                    $query  = "INSERT INTO entexam_recept_dat";
                    $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, examno,registercd, updated)";
                    $query .= " VALUES ('".$model->ObjYear."'";
                    $query .= "        ,'".$model->field["APPLICANTDIV"]."'";
                    $query .= "        ,'".$model->field["TESTDIV"]."'";
                    $query .= "        ,'1'";
                    $query .= "        ,'".sprintf("%05d",$receptno)."'";
                    $query .= "        ,'".$val."'";
                    $query .= "        ,'".STAFFCD."'";
                    $query .= "        ,SYSDATE()";
                    $query .= ")";
                    $db->query($query);
                    //最後の座席番号を保持
                    $end_receptno = (int) $receptno;
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
