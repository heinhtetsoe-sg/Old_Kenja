<?php

require_once('for_php7.php');

class knjl325aQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験種別コンボ
    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "     AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getPassCourse($model) {
        $query  = " SELECT ";
        $query .= "   NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "   NAMECD2 AS VALUE ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ObjYear."' ";
        if ($model->field["APPLICANTDIV"] == 2) { //高校
            $query .= " AND NAMECD1 = 'LH13' ";
        } else if ($model->field["APPLICANTDIV"] == 1) { //中学
            $query .= " AND NAMECD1 = 'LJ13' ";
        }
        $query .= " AND NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";
        
        return $query;
    }
}
?>
