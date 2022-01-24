<?php
class knjl361kQuery extends Query {
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

    //スカラシップ
    function GetScalashipdiv($model){
        $query  = " SELECT DISTINCT ";
        $query .= "     SCALASHIPDIV, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     LEFT JOIN V_NAME_MST ON YEAR = ENTEXAMYEAR ";
        $query .= "     AND NAMECD1 = 'Z006' ";
        $query .= "     AND NAMECD2 = SCALASHIPDIV ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        if ($model->special_reason_div != '9') {
            $query .= " AND SPECIAL_REASON_DIV = '{$model->special_reason_div}' ";
        }
        $query .= "     AND TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND SCALASHIPDIV IS NOT NULL ";
        $query .= "     AND SCALASHIPDIV != '' ";
        $query .= " ORDER BY ";
        $query .= "     SCALASHIPDIV ";

        return $query;
    }

    //中高判別
    function GetJorH($model){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

}
?>
