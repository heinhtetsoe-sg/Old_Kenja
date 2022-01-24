<?php

require_once('for_php7.php');

class knjl352kQuery extends Query {
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

    //追加繰上合格グループNo
    function GetPassdiv($model){
        $query  = "SELECT DISTINCT JUDGEMENT_GROUP_NO AS GROUP_NO ";
        $query .= "FROM   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "WHERE  ENTEXAMYEAR='".$model->ObjYear."' AND ";
        if ($model->special_reason_div != '9') {
            $query .= "       SPECIAL_REASON_DIV = '{$model->special_reason_div}' AND ";
        }
        if ($model->testdiv != 99){
            $query .= "       TESTDIV='".$model->testdiv."' AND ";
        }
//NO002
//      $query .= "       JUDGEMENT='".$model->judge."' AND ";
        $query .= "       JUDGEMENT_GROUP_NO IS NOT NULL ";

        return $query;
    }

}
?>
