<?php

require_once('for_php7.php');

class knjl343Query extends Query {

    //クラス未確定が１件でもあるか
    function getEntClassStd($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.ENTDIV = '1' ";
        $query .= "     AND T1.PROCEDUREDIV = '1' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_RECEPT_DAT E1 ";
        $query .= "             WHERE ";
        $query .= "                 E1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "                 AND E1.JUDGECLASS = '5' ";
        $query .= "                 AND E1.EXAMNO = T1.EXAMNO ";
        $query .= "     ) ";
        $query .= "     AND T1.ENTCLASS IS NULL ";
        if ($model->field["OUTPUT"] == "2"){
            $query .= "     AND T1.EXAMNO = '{$model->field["EXAMNO"]}' ";
        }

        return $query;
    }
}
?>
