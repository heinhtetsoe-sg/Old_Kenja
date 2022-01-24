<?php

require_once('for_php7.php');

class knjl344kQuery extends Query {
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

    //追加繰上合格グループNo
    function GetPassdiv($model){
        $query  = "SELECT DISTINCT JUDGEMENT_GROUP_NO AS GROUP_NO ";
        $query .= "FROM   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "WHERE  ENTEXAMYEAR='".$model->ObjYear."' AND ";
        $query .= "       SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "       TESTDIV='".$model->testdiv."' AND ";
        $query .= "       JUDGEMENT='".$model->judge."' AND ";
        $query .= "       JUDGEMENT_GROUP_NO IS NOT NULL ";

        return $query;
    }

    //スカラシップ---NO005
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
        $query .= "     AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "     AND TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND SCALASHIPDIV IS NOT NULL ";
        $query .= "     AND SCALASHIPDIV != '' ";
        $query .= " ORDER BY ";
        $query .= "     SCALASHIPDIV ";

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
