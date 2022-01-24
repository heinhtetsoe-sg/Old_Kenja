<?php

class knjl351kQuery extends Query {
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

    //一覧リスト---出身学校
    function GetFinschool($model){
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD AS OUTCD, ";
        $query .= "     FINSCHOOL_NAME AS OUTNAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " ORDER BY FINSCHOOLCD";

        return $query;
    }

    //一覧リスト---塾
    function GetPrischool($model){
        $query  = " SELECT ";
        $query .= "     PRISCHOOLCD AS OUTCD, ";
        $query .= "     PRISCHOOL_NAME AS OUTNAME ";
        $query .= " FROM ";
        $query .= "     V_PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= " ORDER BY PRISCHOOLCD";

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
