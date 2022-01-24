<?php

require_once('for_php7.php');

class knjl055dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //対象データ存在チェック
    function checkQueAnsCnt($model) {
        $query  = " SELECT COUNT(*) FROM ENTEXAM_QUESTION_ANS_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "   AND T1.SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";

        return $query;
    }

    //設問フラグが立っている問題番号を取得
    function getQuestionOrder($model) {
        $query  = " SELECT ";
        $query .= "     PO_Y.QUESTION_ORDER ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_POINT_YMST PO_Y ";
        $query .= " WHERE ";
        $query .= "         PO_Y.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND PO_Y.SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "     AND PO_Y.QUEST_FLAG  = '1' ";
        $query .= " ORDER BY ";
        $query .= "     INT(QUESTION_ORDER) ";

        return $query;
    }

    //算出
    function ExecuteQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //設問フラグが立っている問題番号を取得
        $qesOrderArray = array();
        $query = knjl055dQuery::getQuestionOrder($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $qesOrderArray[] = $row["QUESTION_ORDER"];
        }
        $result->free();

        //DELETE(ENTEXAM_QUESTION_SCORE_DAT)
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND SUBCLASS_CD  = '".$model->field["SUBCLASS_CD"]."' ";
        $db->query($query);

        //INSERT(ENTEXAM_QUESTION_SCORE_DAT)
        foreach ($qesOrderArray as $key => $order) {
            $query  = " INSERT INTO ";
            $query .= "     ENTEXAM_QUESTION_SCORE_DAT ";
            $query .= " ( ";
            $query .= "     ENTEXAMYEAR, ";
            $query .= "     SUBCLASS_CD, ";
            $query .= "     EXAMNO, ";
            $query .= "     LARGE_QUESTION, ";
            $query .= "     QUESTION, ";
            $query .= "     QUESTION_ORDER, ";
            $query .= "     PATTERN_CD, ";
            for ($num = 1; $num <= 10; $num++) {
                $query .= "     SELECT{$num}, ";
            }
            $query .= "     DOUBLE_MARK_FLG, ";
            $query .= "     NO_MARK_FLG, ";
            $query .= "     POINT, ";
            $query .= "     POINT_SYMBOL, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     PO_Y.ENTEXAMYEAR, ";
            $query .= "     PO_Y.SUBCLASS_CD, ";
            $query .= "     AN_D.EXAMNO, ";
            $query .= "     PO_Y.LARGE_QUESTION, ";
            $query .= "     PO_Y.QUESTION, ";
            $query .= "     PO_Y.QUESTION_ORDER, ";
            $query .= "     PO_Y.PATTERN_CD, ";
            for ($num = 1; $num <= 10; $num++) {
                $query .= "     CASE ";
                $query .= "         WHEN PO_Y.PATTERN_CD = '1' AND (SV_M.ANSWER1 = '{$num}' OR WV_M.ANSWER1 = '{$num}' OR WV_M.ANSWER2 = '{$num}') THEN '1' ";
                $query .= "         WHEN PO_Y.PATTERN_CD = '2' AND (SV_M.ANSWER1 = '{$num}' OR WV_M.ANSWER1 = '{$num}' OR WV_M.ANSWER2 = '{$num}') THEN '1' ";
                $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND (SV_M.ANSWER1 = '{$num}' OR WV_M.ANSWER1 = '{$num}' OR WV_M.ANSWER2 = '{$num}') THEN '1' ";
                $query .= "         ELSE '' ";
                $query .= "     END AS SELECT{$num}, ";
            }
            $query .= "     CASE ";
            $query .= "         WHEN AN_D.ANS{$order} != '0' AND SV_M.ANSWER1 IS NULL AND WV_M.ANSWER1 IS NULL THEN '1' ";
            $query .= "         ELSE ''  ";
            $query .= "     END AS DOUBLE_MARK_FLG, ";
            $query .= "     CASE WHEN AN_D.ANS{$order} = '0' THEN '1' ELSE '' END AS NO_MARK_FLG, ";
            $query .= "     CASE ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '1' AND SV_M.ANSWER1 = PO_Y.ANSWER1 THEN INT(PO_Y.POINT1) ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '2' AND (WV_M.ANSWER1 = PO_Y.ANSWER1 AND WV_M.ANSWER2 = PO_Y.ANSWER2) THEN INT(PO_Y.POINT1) ";
            //パターンコード３はマーク２つ選択する
            //マーク選択１つで、部分正解
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND WV_M.ANSWER1 IS NULL AND SV_M.ANSWER1 = PO_Y.ANSWER1 THEN INT(PO_Y.POINT1) ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND WV_M.ANSWER1 IS NULL AND SV_M.ANSWER1 = PO_Y.ANSWER2 THEN INT(PO_Y.POINT2) ";
            //マーク選択２つで、完全正解
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND SV_M.ANSWER1 IS NULL AND (WV_M.ANSWER1 = PO_Y.ANSWER1 AND WV_M.ANSWER2 = PO_Y.ANSWER2) THEN INT(PO_Y.POINT1) + INT(PO_Y.POINT2) ";
            //マーク選択２つで、部分正解
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND SV_M.ANSWER1 IS NULL AND ((WV_M.ANSWER1 = PO_Y.ANSWER1 AND WV_M.ANSWER2 != PO_Y.ANSWER2) OR ";
            $query .= "                                                                  (WV_M.ANSWER2 = PO_Y.ANSWER1 AND WV_M.ANSWER1 != PO_Y.ANSWER2)) THEN INT(PO_Y.POINT1) ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND SV_M.ANSWER1 IS NULL AND ((WV_M.ANSWER1 != PO_Y.ANSWER1 AND WV_M.ANSWER2 = PO_Y.ANSWER2) OR ";
            $query .= "                                                                  (WV_M.ANSWER2 != PO_Y.ANSWER1 AND WV_M.ANSWER1 = PO_Y.ANSWER2)) THEN INT(PO_Y.POINT2) ";
            $query .= "         ELSE 0 ";
            $query .= "     END AS POINT, ";
            $query .= "     CASE ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '1' AND SV_M.ANSWER1 = PO_Y.ANSWER1 THEN '〇' ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '2' AND (WV_M.ANSWER1 = PO_Y.ANSWER1 AND WV_M.ANSWER2 = PO_Y.ANSWER2) THEN '〇' ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND WV_M.ANSWER1 IS NULL AND SV_M.ANSWER1 = PO_Y.ANSWER1 THEN '△' ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND WV_M.ANSWER1 IS NULL AND SV_M.ANSWER1 = PO_Y.ANSWER2 THEN '△' ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND SV_M.ANSWER1 IS NULL AND (WV_M.ANSWER1 = PO_Y.ANSWER1 AND WV_M.ANSWER2 = PO_Y.ANSWER2) THEN '〇' ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND SV_M.ANSWER1 IS NULL AND ((WV_M.ANSWER1 = PO_Y.ANSWER1 AND WV_M.ANSWER2 != PO_Y.ANSWER2) OR ";
            $query .= "                                                                  (WV_M.ANSWER2 = PO_Y.ANSWER1 AND WV_M.ANSWER1 != PO_Y.ANSWER2)) THEN '△' ";
            $query .= "         WHEN PO_Y.PATTERN_CD = '3' AND SV_M.ANSWER1 IS NULL AND ((WV_M.ANSWER1 != PO_Y.ANSWER1 AND WV_M.ANSWER2 = PO_Y.ANSWER2) OR ";
            $query .= "                                                                  (WV_M.ANSWER2 != PO_Y.ANSWER1 AND WV_M.ANSWER1 = PO_Y.ANSWER2)) THEN '△' ";
            $query .= "         WHEN AN_D.ANS{$order} = '0' THEN '？' ";
            $query .= "         ELSE '×' ";
            $query .= "     END AS POINT_SYMBOL, ";
            $query .= "     '". STAFFCD ."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_QUESTION_POINT_YMST PO_Y ";
            $query .= "     LEFT JOIN ENTEXAM_QUESTION_ANS_DAT AN_D ON PO_Y.ENTEXAMYEAR = AN_D.ENTEXAMYEAR ";
            $query .= "                                            AND PO_Y.SUBCLASS_CD = AN_D.SUBCLASS_CD ";
            $query .= "     LEFT JOIN ENTEXAM_QUESTION_ANS_SVALUE_MST SV_M ON AN_D.ANS{$order} = SV_M.VALUE ";
            $query .= "     LEFT JOIN ENTEXAM_QUESTION_ANS_WVALUE_MST WV_M ON AN_D.ANS{$order} = WV_M.VALUE ";
            $query .= " WHERE ";
            $query .= "         PO_Y.ENTEXAMYEAR = '".$model->examyear."'  ";
            $query .= "     AND PO_Y.SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
            $query .= "     AND PO_Y.QUESTION_ORDER = '{$order}' ";
            $query .= " ORDER BY ";
            $query .= "     AN_D.EXAMNO ";

            $db->query($query);
        }

        //DELETE(ENTEXAM_SCORE_DAT)
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR    = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV        = '2' ";//一般のみ
        $query .= "     AND EXAM_TYPE      = '1' ";//固定
        $query .= "     AND TESTSUBCLASSCD = '".$model->field["SUBCLASS_CD"]."' ";
        $db->query($query);

        //INSERT(ENTEXAM_SCORE_DAT)
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     RECEPTNO, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     ATTEND_FLG, ";
        $query .= "     SCORE, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$model->examyear."', ";
        $query .= "     '".$model->applicantdiv."', ";
        $query .= "     '2', ";//一般のみ
        $query .= "     '1', ";//固定
        $query .= "     SC_D.EXAMNO, ";
        $query .= "     '".$model->field["SUBCLASS_CD"]."', ";
        $query .= "     '1', ";
        $query .= "     sum(SC_D.POINT), ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_SCORE_DAT SC_D ";
        $query .= " WHERE ";
        $query .= "         SC_D.ENTEXAMYEAR = '".$model->examyear."'  ";
        $query .= "     AND SC_D.SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= " GROUP BY ";
        $query .= "     SC_D.EXAMNO ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    } 
}
?>

