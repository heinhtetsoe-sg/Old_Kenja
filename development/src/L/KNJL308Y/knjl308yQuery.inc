<?php
class knjl308yQuery extends Query {

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

    //試験会場取得
    function getExamHall($model)
    {
        if ($model->field["HALL_DIV"] == "1") {
            $query  = " SELECT ";
            $query .= "     EXAMHALLCD AS VALUE, ";
            $query .= "     EXAMHALL_NAME || ' (' || S_RECEPTNO || '～' || E_RECEPTNO || ')' AS LABEL, ";
            $query .= "     S_RECEPTNO, ";
            $query .= "     E_RECEPTNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_HALL_YDAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
            $query .= "     TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
            $query .= "     EXAM_TYPE       = '1' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.EXAMHALLCD || T1.DETAIL_NO AS VALUE, ";
            $query .= "     T2.EXAMHALL_NAME || '(' || T1.DETAIL_NO || ')' || ' (' || T1.S_RECEPTNO || '～' || T1.E_RECEPTNO || ')' AS LABEL, ";
            $query .= "     T1.S_RECEPTNO, ";
            $query .= "     T1.E_RECEPTNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_HALL_DETAIL_YDAT T1, ";
            $query .= "     ENTEXAM_HALL_YDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR AND ";
            $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
            $query .= "     T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
            $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
            $query .= "     T1.TESTDIV      = T2.TESTDIV AND ";
            $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' AND ";
            $query .= "     T1.EXAM_TYPE    = T2.EXAM_TYPE AND ";
            $query .= "     T1.EXAM_TYPE    = '1' AND ";
            $query .= "     T1.EXAMHALLCD   = T2.EXAMHALLCD ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //時間割パターン取得
    function getSchPtrn($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     PATTERN_NO AS VALUE,";
        $query .= "     PATTERN_NO || ':' || PATTERN_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     TESTDIV         = '".$model->field["TESTDIV"]."' ";
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
