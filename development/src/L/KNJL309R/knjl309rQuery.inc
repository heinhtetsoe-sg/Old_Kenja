<?php

require_once('for_php7.php');

class knjl309rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
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
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //試験会場設定データより取得
    function getEntexamHallDat($year, $applicantdiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     EXAMHALL_NAME, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO, ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     EXAMHALLCD || '-' || S_RECEPTNO || '-' || E_RECEPTNO as VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= " AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= " AND TESTDIV = '".$testdiv."' ";
        $query .= " AND EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>