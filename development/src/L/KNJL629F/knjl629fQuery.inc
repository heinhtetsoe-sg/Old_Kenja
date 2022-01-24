<?php

require_once('for_php7.php');

class knjl629fQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //対象人数チェック
    function checkRecCnt($model) {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND T1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "   AND T1.TESTDIV      = '".$model->testdiv."'";
        return $query;
    }

    //得点データチェック
    function checkScoCnt($model) {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND T1.APPLICANTDIV = '".$model->applicantdiv."'"; 
        $query .= "   AND T1.TESTDIV      = '".$model->testdiv."'";
        return $query;
    }
}
?>
