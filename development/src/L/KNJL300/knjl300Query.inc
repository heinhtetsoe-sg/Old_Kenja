<?php

require_once('for_php7.php');

class knjl300Query extends Query {

    //入試区分取得
    function get_test_div($year)
    {
        $query  = " SELECT NAME AS NAME1,TESTDIV AS NAMECD2,SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$year."' AND ";
        $query .= " TESTDIV IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_HALL_DAT) ";
        $query .= " ORDER BY SHOWORDER, TESTDIV ";
        return $query;
    }
    //受験型名称取得
    function get_exam_div($namecd1, $namecd2)
    {
        $query  = " SELECT NAME1 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd1."' AND ";
        $query .= " NAMECD2 = '".$namecd2."' ";
        return $query;
    }

    //試験会場名称取得
    function get_hall_data($testdv,$exam_typ)
    {
        $query  = " SELECT EXAMHALL_NAME,S_RECEPTNO,E_RECEPTNO,EXAMHALLCD,EXAMHALLCD || '-' || S_RECEPTNO || '-' || E_RECEPTNO as VALUE ";
        $query .= " FROM ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= " TESTDIV = '".$testdv."' AND ";
        $query .= " EXAM_TYPE = '".$exam_typ."'";
        return $query;
    }
}
?>
