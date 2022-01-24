<?php

require_once('for_php7.php');

class knjl309yQuery extends Query {

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
