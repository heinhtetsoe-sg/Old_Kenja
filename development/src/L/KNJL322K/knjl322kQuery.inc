<?php

require_once('for_php7.php');


class knjl322kQuery extends Query {
    //特別理由区分
    function getSpecialReasonDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1 = 'L017' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験区分
    function GetTestdiv($model){
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L003' ";

        return $query;
    }

    //試験科目
    function GetSubclass($model){
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L009' ";
        $query .= " ORDER BY NAMECD2";

        return $query;
    }

    //一覧リスト---1:会場
    function GetExamHallcd($model){
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMHALLCD AS CD, ";
        $query .= "     EXAMHALL_NAME AS NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        if ($model->testdiv != 99) 
            $query .= " WHERE TESTDIV = '".$model->testdiv."' ";
        $query .= " ORDER BY EXAMHALLCD ";

        return $query;
    }

    //一覧リスト---2:受験番号
    function GetExamno($model){
        $query  = "SELECT DISTINCT EXAMNO AS CD, NAME ";
        $query .= "FROM   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "WHERE  ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "       AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        if ($model->testdiv != 99) 
            $query .= "   AND TESTDIV = '".$model->testdiv."' ";
        $query .= "ORDER BY EXAMNO ";

        return $query;
    }

    //中高判別
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

}
?>
