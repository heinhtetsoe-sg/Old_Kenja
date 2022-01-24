<?php

require_once('for_php7.php');

class knjl013rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == 'L003') {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";

        $db->query($query);

        if ($model->field["APPLICANTDIV"] == '1') {
            $query  = " INSERT INTO ENTEXAM_RECEPT_DAT ( ";
            $query .= "     ENTEXAMYEAR, ";
            $query .= "     APPLICANTDIV, ";
            $query .= "     TESTDIV, ";
            $query .= "     EXAM_TYPE, ";
            $query .= "     EXAMNO, ";
            $query .= "     RECEPTNO, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     ENTEXAMYEAR, ";
            $query .= "     APPLICANTDIV, ";
            $query .= "     TESTDIV, ";
            $query .= "     '1' AS EXAM_TYPE, ";
            $query .= "     EXAMNO, ";
            $query .= "     EXAMNO, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->year}' ";
            $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
            $query .= "     AND VALUE(JUDGEMENT, '0') != '4' ";
        } else {
            $query  = " INSERT INTO ENTEXAM_RECEPT_DAT ( ";
            $query .= "     ENTEXAMYEAR, ";
            $query .= "     APPLICANTDIV, ";
            $query .= "     TESTDIV, ";
            $query .= "     EXAM_TYPE, ";
            $query .= "     EXAMNO, ";
            $query .= "     RECEPTNO, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     APP.ENTEXAMYEAR, ";
            $query .= "     APP.APPLICANTDIV, ";
            $query .= "     '{$model->field["TESTDIV"]}' AS TESTDIV, ";
            $query .= "     '1' AS EXAM_TYPE, ";
            $query .= "     APP.EXAMNO, ";
            $query .= "     BUN012.REMARK{$model->field["TESTDIV"]}, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT APP ";
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN012 ON APP.ENTEXAMYEAR = BUN012.ENTEXAMYEAR ";
            $query .= "           AND APP.APPLICANTDIV = BUN012.APPLICANTDIV ";
            $query .= "           AND APP.EXAMNO = BUN012.EXAMNO ";
            $query .= "           AND BUN012.SEQ = '012' ";
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN010 ON APP.ENTEXAMYEAR = BUN010.ENTEXAMYEAR ";
            $query .= "           AND APP.APPLICANTDIV = BUN010.APPLICANTDIV ";
            $query .= "           AND APP.EXAMNO = BUN010.EXAMNO ";
            $query .= "           AND BUN010.SEQ = '010' ";
            $query .= "           AND BUN010.REMARK{$model->field["TESTDIV"]} = '{$model->field["TESTDIV"]}' ";
            $query .= " WHERE ";
            $query .= "     APP.ENTEXAMYEAR = '{$model->year}' ";
            $query .= "     AND APP.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND VALUE(APP.JUDGEMENT, '0') != '4' ";
        }

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
