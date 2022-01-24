<?php

require_once('for_php7.php');

class knjl250cQuery extends Query {

    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtn = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtn;
    }

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
}
?>
