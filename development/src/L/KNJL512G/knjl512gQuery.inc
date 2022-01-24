<?php

require_once('for_php7.php');

class knjl512gQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
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

    //リストtoリスト取得
    function getVEntexamBaseDat($model) {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO AS VALUE, ";
        $query .= "     CHAR(DATE(BASE.RECEPTDATE)) || '：' || BASE.EXAMNO || '：' || BASE.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      IN ('31','32') ";
        $query.= " ORDER BY ";
        $query.= "     BASE.RECEPTDATE DESC, ";
        $query.= "     BASE.EXAMNO     ASC   ";

        return $query;
    }

}
?>