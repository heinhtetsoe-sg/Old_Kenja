<?php

require_once('for_php7.php');

class knjl348Query extends Query {

    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV, ";
        $query .= "     NAME, ";
        $query .= "     ABBV, ";
        $query .= "     TESTDAY, ";
        $query .= "     MONTH(TESTDAY) AS MONTH, ";
        $query .= "     DAY(TESTDAY) AS DAY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDAY, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    function getStudentSql($model) {
        $query  = " SELECT ";
        $query .= "     BASE_D.EXAMNO, ";
        $query .= "     BASE.RECOM_EXAMNO1, ";
        $query .= "     BASE.RECOM_EXAMNO2, ";
        $query .= "     BASE.RECOM_EXAMNO3, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.NAME2 AS SEX_NAME, ";
        $query .= "     BASE_D.REMARK1, ";
        $query .= "     BASE_D.REMARK2, ";
        $query .= "     BASE_D.REMARK3, ";
        $query .= "     BASE_D.REMARK4 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND BASE_D.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     BASE_D.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND BASE_D.SEQ = '005' ";
        $query .= "     AND BASE_D.REMARK2 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE_D.EXAMNO ";
        return $query;
    }

    function getReceptSql($model, $examno) {
        $query  = " SELECT ";
        $query .= "     RECEPTNO, ";
        $query .= "     TESTDIV, ";
        $query .= "     TOTAL4, ";
        $query .= "     JUDGEDIV, ";
        $query .= "     HONORDIV, ";
        $query .= "     JUDGECLASS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAM_TYPE = '2' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        return $query;
    }

    function getDesireSql($model, $examno) {
        $query  = " SELECT ";
        $query .= "     EXAMNO, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAMINEE_DIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        return $query;
    }

    function getBaseTestdivSql($model, $examno) {
        $query  = " SELECT ";
        $query .= "     EXAMNO, ";
        $query .= "     TESTDIV0, ";
        $query .= "     TESTDIV1, ";
        $query .= "     TESTDIV2, ";
        $query .= "     TESTDIV3, ";
        $query .= "     TESTDIV4, ";
        $query .= "     TESTDIV5, ";
        $query .= "     TESTDIV6 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        return $query;
    }

}
?>
