<?php

require_once('for_php7.php');

class knjl320yQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分取得
    function getDesireDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.DESIREDIV AS VALUE, ";
        $query .= "     T1.DESIREDIV || ':' || T2.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1, ";
        $query .= "     ENTEXAM_COURSE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "     T1.TESTDIV = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     T1.WISHNO = '1' AND ";
        $query .= "     T1.COURSECD = T2.COURSECD AND ";
        $query .= "     T1.MAJORCD = T2.MAJORCD AND ";
        $query .= "     T1.EXAMCOURSECD = T2.EXAMCOURSECD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
