<?php

require_once('for_php7.php');

class knjl322dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    function getNameMstSpare($model, $spare) {
        $query  = " SELECT DISTINCT ";
        if ($spare != "4") {
            $query .= "     NAMESPARE{$spare} ";
        } else {
            $query .= "     ABBV3 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->examyear."' ";
        $query .= "     AND NAMECD1 = 'L045' ";
        $query .= "     AND NAMECD2 = '".$model->testdiv."' ";

        return $query;
    }

    //マスタより取得
    function getFinSchool($model) {
        $query  = " SELECT ";
        $query .= "     BASE.FS_CD AS VALUE, ";
        $query .= "     FINS.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FINS.FINSCHOOL_NAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
