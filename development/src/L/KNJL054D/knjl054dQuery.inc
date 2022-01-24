<?php

require_once('for_php7.php');

class knjl054dQuery extends Query {
    //名称マスタより取得
    function getNameMst($model, $namecd1) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストデータ取得
    function getList($model) {
        $query  = " WITH REPEAT_DATA AS ( ";
        $query .= "         SELECT ";
        $query .= "             EXAMNO ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_QUESTION_ANS_DAT ";
        $query .= "         WHERE ";
        $query .= "                 ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "             AND SUBCLASS_CD  = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "         GROUP BY ";
        $query .= "             EXAMNO ";
        $query .= "         HAVING ";
        $query .= "             COUNT(EXAMNO) <> 1 ";
        $query .= " ), NULL_DATA AS (  ";
        $query .= "         SELECT ";
        $query .= "             SEQ - 1 AS B_SEQ, ";
        $query .= "             SEQ, ";
        $query .= "             SEQ + 1 AS N_SEQ ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_QUESTION_ANS_DAT ";
        $query .= "         WHERE ";
        $query .= "                 ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "             AND SUBCLASS_CD  = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "             AND EXAMNO      IS NULL ";
        $query .= " ) ";
        //エラー番号１：欠席登録と矛盾する
        $query .= " SELECT DISTINCT ";
        $query .= "     '１' AS ERROR_NO, ";
        $query .= "     ANSD.SEQ, ";
        $query .= "     ANSD.EXAMNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_ANS_DAT ANSD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ANSD.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "                                             AND ANSD.EXAMNO      = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         ANSD.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND ANSD.SUBCLASS_CD  = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.JUDGEMENT    = '4' ";//欠席
        //エラー番号２:重複受験番号
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '２' AS ERROR_NO, ";
        $query .= "     ANSD.SEQ, ";
        $query .= "     ANSD.EXAMNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_ANS_DAT ANSD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ANSD.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "                                             AND ANSD.EXAMNO      = BASE.EXAMNO ";
        $query .= "     INNER JOIN REPEAT_DATA REPD ON REPD.EXAMNO = ANSD.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         ANSD.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND ANSD.SUBCLASS_CD  = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        //エラー番号３：受験番号記入漏れ
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '３' AS ERROR_NO, ";
        $query .= "     NULD.SEQ, ";
        $query .= "    (SELECT ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_QUESTION_ANS_DAT ";
        $query .= "     WHERE ";
        $query .= "             ENTEXAMYEAR = '".$model->year."' ";
        $query .= "         AND SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "         AND NULD.B_SEQ  = SEQ ";
        $query .= "     ) AS EXAMNO, ";
        $query .= "     (SELECT ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_QUESTION_ANS_DAT ";
        $query .= "     WHERE ";
        $query .= "             ENTEXAMYEAR = '".$model->year."' ";
        $query .= "         AND SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "         AND NULD.N_SEQ  = SEQ ";
        $query .= "     ) AS NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_ANS_DAT ANSD ";
        $query .= "     LEFT JOIN NULL_DATA NULD ON ANSD.SEQ = NULD.SEQ ";
        $query .= " WHERE ";
        $query .= "         ANSD.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND ANSD.SUBCLASS_CD  = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "     AND ANSD.EXAMNO      IS NULL ";
        //エラー番号４:欠席登録されていない欠番
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '４' AS ERROR_NO, ";
        $query .= "     ANSD.SEQ, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE  ";
        $query .= "     LEFT JOIN ENTEXAM_QUESTION_ANS_DAT ANSD ON BASE.ENTEXAMYEAR = ANSD.ENTEXAMYEAR ";
        $query .= "                                            AND ANSD.SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= "                                            AND BASE.EXAMNO      = ANSD.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      = '2' ";
        $query .= "     AND BASE.JUDGEMENT    <> '4' ";
        $query .= "     AND ANSD.EXAMNO       IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     ERROR_NO, ";
        $query .= "     EXAMNO ";

        return $query;
    }

/******** 更新処理  ********/
    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->setDataArr as $key => $seq) {
            if ($model->field["CORRECT_EXAMNO:".$key] == "") continue;

            $data = array();
            $data["EXAMNO"][TEXT]       = $model->field["CORRECT_EXAMNO:".$key];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "   AND SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
            $where .= "   AND SEQ         = ".$seq." ";

            $query = Query::updateSQL($data, "ENTEXAM_QUESTION_ANS_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>