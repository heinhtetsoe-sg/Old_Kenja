<?php

require_once('for_php7.php');

class knjl312mQuery extends Query {

    //入試制度取得
    function getApplicantdiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //試験科目
    function getSubclassDetail($model) {
        $query  = " SELECT ";
        $query .= "     T1.NAME1 || '-' || L1.TESTPAPERCD AS LABEL, ";
        $query .= "     T1.NAMECD2 || '-' ||L1.TESTPAPERCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_PERFECT_DETAIL_MST L1 ON T1.NAMECD2 = L1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.NAMECD1 = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場データ
    function getHallDat() {
        $query  = " SELECT ";
        $query .= "     EXAMHALL_NAME || ' ' || S_RECEPTNO || '～' || E_RECEPTNO AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO, ";
        $query .= "     EXAMHALL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
