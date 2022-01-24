<?php

require_once('for_php7.php');

class knjl400yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    function getExamHall($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMHALLCD || '-' || VALUE(MIN(T2.RECEPTNO), '') || '-' || VALUE(MAX(T2.RECEPTNO), '') AS VALUE, ";
        $query .= "     T1.EXAMHALL_NAME || ' (' || VALUE(MIN(T2.RECEPTNO), '') || '～' || VALUE(MAX(T2.RECEPTNO), '') || ')' AS LABEL, ";
        $query .= "     MIN(T2.RECEPTNO) AS S_RECEPTNO, ";
        $query .= "     MAX(T2.RECEPTNO) AS E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_LIST_YDAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "         AND T2.EXAMHALLCD = T1.EXAMHALLCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     T1.EXAM_TYPE       = '1' ";	
        $query .= " GROUP BY ";
        $query .= "     T1.EXAMHALLCD, T1.EXAMHALL_NAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号取得
    function getExamNo($model, $receptno)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     EXAM_TYPE       = '1' AND ";	
        $query .= "     RECEPTNO        = '".$receptno."' ";	

        return $query;
    }
}
?>
