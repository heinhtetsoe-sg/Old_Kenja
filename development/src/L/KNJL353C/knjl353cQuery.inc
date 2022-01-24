<?php

require_once('for_php7.php');


class knjl353cQuery extends Query {

    //入試制度取得
    function getApctDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN( SELECT DISTINCT ";
        $query .= "                     APPLICANTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_RECEPT_DAT ";
        $query .= "                 WHERE ";	
        $query .= "                     ENTEXAMYEAR = '".$year."') ";	
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    function getTestDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN( SELECT DISTINCT ";
        $query .= "                     TESTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_RECEPT_DAT ";
        $query .= "                 WHERE ";	
        $query .= "                     ENTEXAMYEAR = '".$year."' ) AND ";
        $query .= "     NAMECD2 NOT IN ('3','4') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名取得
    function getFinchoolName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.FS_CD AS VALUE, ";
        $query .= "     T2.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "     V_FINSCHOOL_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = T2.YEAR AND ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        if($model->field["TESTDIV"] == '8'){
            $query .= "     T1.TESTDIV IN ('3','4') AND ";
        } else if($model->field["TESTDIV"] != '9'){
            $query .= "     T1.TESTDIV = '".$model->field["TESTDIV"]."' AND ";
        }
        $query .= "     T1.FS_CD = T2.FINSCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //塾名取得
    function getPrischoolName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PRISCHOOLCD AS VALUE, ";
        $query .= "     T2.PRISCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "     V_PRISCHOOL_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = T2.YEAR AND ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        if($model->field["TESTDIV"] == '8'){
            $query .= "     T1.TESTDIV IN ('3','4') AND ";
        } else if($model->field["TESTDIV"] != '9'){
            $query .= "     T1.TESTDIV = '".$model->field["TESTDIV"]."' AND ";
        }
        $query .= "     T1.PRISCHOOLCD = T2.PRISCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
