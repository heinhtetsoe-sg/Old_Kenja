<?php

require_once('for_php7.php');

class knjl346Query extends Query {

    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";
        return $query;
    }

    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV, ";
        $query .= "     NAME, ";
        $query .= "     ABBV, ";
        $query .= "     TESTDAY ";
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
}
?>
