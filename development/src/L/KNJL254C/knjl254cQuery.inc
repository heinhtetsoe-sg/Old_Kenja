<?php

require_once('for_php7.php');

class knjl254cQuery extends Query {

    //入試制度取得
    function getApctDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L103' AND ";
        $query .= "     NAMECD2 = '1' ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //塾一覧
    function getPrischoolCd($model) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOLCD AS VALUE, ";
        $query .= "     PRISCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= "     AND PRISCHOOLCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             PS_CD ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "             AND PS_CD IS NOT NULL ";
        $query .= "             AND PRE_TESTDIV = '".$model->field["PRE_TESTDIV"]."' ";
        $query .= "         GROUP BY ";
        $query .= "             PS_CD ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
