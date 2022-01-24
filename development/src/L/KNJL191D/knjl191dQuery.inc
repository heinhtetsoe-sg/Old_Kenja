<?php

require_once('for_php7.php');

class knjl191dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2){
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験種別取得
    function getTestDivList($year) {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //MAX_TESTDIV取得
    function getMaxTestDiv($year) {
        $query  = " SELECT ";
        $query .= "     max(TESTDIV) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR ";

        return $query;
    }

    //List to List 右側生徒
    function getPassList($model) {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO AS VALUE, ";
        $query .= "     BASE.EXAMNO || '　' || BASE.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND BASE.DESIREDIV    = '".$model->desirediv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //List to List 右側学校
    function getPassSchoolList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     FIN.FINSCHOOLCD AS VALUE, ";
        $query .= "     FIN.FINSCHOOLCD || '　' || FIN.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        if ($model->desirediv == "1"){
            $query .= "     INNER JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ";
        } else {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DT003 ";
            $query .= "             ON DT003.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
            $query .= "            AND DT003.APPLICANTDIV = BASE.APPLICANTDIV ";
            $query .= "            AND DT003.EXAMNO       = BASE.EXAMNO ";
            $query .= "            AND DT003.SEQ          = '033' ";
            $query .= "     INNER JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = DT003.REMARK2 ";
        }

        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND BASE.DESIREDIV    = '".$model->desirediv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
