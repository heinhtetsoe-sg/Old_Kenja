<?php

require_once('for_php7.php');

class knjl312jQuery extends Query {

    //入試区分取得
    function get_test_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2,NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //受験科目取得
    function get_subclas_div($namecd,$model)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$model->ObjYear."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT TESTSUBCLASSCD FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";	
        $query .= " AND TESTDIV = '" .$model->field["TESTDV"]. "' ";	
        $query .= " )";	
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //試験会場取得
    function get_hall_data($testdv,$exam_typ,$subclass)
    {
        $query  = " SELECT EXAMHALL_NAME,EXAM_TYPE,EXAMHALLCD as VALUE ";
        $query .= " FROM ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= " TESTDIV = '".$testdv."' ";
        $query .= " ORDER BY EXAMHALLCD ";

        return $query;
    }
}
?>
