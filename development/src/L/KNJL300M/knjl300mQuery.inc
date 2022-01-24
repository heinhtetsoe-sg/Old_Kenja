<?php

require_once('for_php7.php');

class knjl300mQuery extends Query {

    //入試制度取得
    function getApplicantDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN (SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_HALL_DAT)";
        $query .= " ORDER BY  ";
        $query .= "     NAMESPARE2, ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験会場名称取得
    function getHallData($applicantdiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     EXAMHALL_NAME, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO, ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     EXAMHALLCD || '-' || S_RECEPTNO || '-' || E_RECEPTNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     APPLICANTDIV = '".$applicantdiv."' AND ";
        $query .= "     TESTDIV = '".$testdiv."' AND ";
        $query .= "     EXAM_TYPE = '1'";
        $query .= " ORDER BY  ";
        $query .= "     EXAMHALLCD ";

        return $query;
    }
}
?>
