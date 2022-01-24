<?php
class knjl051yQuery extends Query {

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
        $query .= "      ,T3.INTERVIEW_REMARK ";
        $query .= "      ,T3.INTERVIEW_VALUE ";
        $query .= "      ,T3.COMPOSITION_VALUE ";
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
        $query .= "        LEFT JOIN ENTEXAM_INTERVIEW_DAT T3 ";
        $query .= "               ON T1.ENTEXAMYEAR    = T3.ENTEXAMYEAR ";
        $query .= "              AND T1.APPLICANTDIV   = T3.APPLICANTDIV ";
        $query .= "              AND T1.TESTDIV        = T3.TESTDIV  ";
        $query .= "              AND T1.EXAMNO          = T3.EXAMNO ";
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
        $db->autoCommit(false);

        $query  = "DELETE FROM ENTEXAM_INTERVIEW_DAT";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."'";
        $query .= "   AND TESTDIV        = '".$model->testdiv."'";
        $query .= "   AND EXAMNO IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        foreach ($model->score as $examno => $val)
        {
            //追加
            $query  = "INSERT INTO ENTEXAM_INTERVIEW_DAT ";
            $query .= " (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAMNO, INTERVIEW_REMARK, INTERVIEW_VALUE, COMPOSITION_VALUE, REGISTERCD, UPDATED)";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$model->applicantdiv."'";
            $query .= "        ,'".$model->testdiv."'";
            $query .= "        ,'".$examno."'";
            $query .= "        ,'".$val["INTERVIEW_REMARK"]."'";
            $query .= "        ,'".$val["INTERVIEW_VALUE"]."'";
            $query .= "        ,'".$val["COMPOSITION_VALUE"]."'";
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