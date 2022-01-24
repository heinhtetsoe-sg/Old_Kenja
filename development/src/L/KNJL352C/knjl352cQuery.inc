<?php

require_once('for_php7.php');


class knjl352cQuery extends Query {

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
    function getTestDiv($namecd, $year, $model)
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
        $query .= "                     ENTEXAMYEAR = '".$year."' ) ";
        if (!$model->isGojou && !$model->isCollege) {
            $query .= " AND NAMECD2 NOT IN ('3','4') ";
        }
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
        if($model->field["TESTDIV"] == 'B'){
            $query .= "     T1.TESTDIV IN ('3','4') AND ";
        } else if($model->field["TESTDIV"] == 'X'){
            $query .= "     T1.TESTDIV IN ('1','2') AND ";
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
        //塾グループ
        if ($model->field["PRINT_TYPE"] == "3") {
            $query  = " SELECT DISTINCT ";
            $query .= "     T3.GRP_PRISCHOOLCD AS VALUE, ";
            $query .= "     T3.PRISCHOOL_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1, ";
            $query .= "     V_PRISCHOOL_MST T2 ";
            $query .= "     INNER JOIN V_PRISCHOOL_MST T3 ";
            $query .= "          ON T3.YEAR = T2.YEAR ";
            $query .= "         AND T3.PRISCHOOLCD = T2.GRP_PRISCHOOLCD ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.PRISCHOOLCD AS VALUE, ";
            $query .= "     T2.PRISCHOOL_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1, ";
            $query .= "     V_PRISCHOOL_MST T2 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = T2.YEAR AND ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        if($model->field["TESTDIV"] == 'B'){
            $query .= "     T1.TESTDIV IN ('3','4') AND ";
        } else if($model->field["TESTDIV"] == 'X'){
            $query .= "     T1.TESTDIV IN ('1','2') AND ";
        } else if($model->field["TESTDIV"] != '9'){
            $query .= "     T1.TESTDIV = '".$model->field["TESTDIV"]."' AND ";
        }
        $query .= "     T1.PRISCHOOLCD = T2.PRISCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校コード取得
    function getSchoolCode()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }
}
?>
