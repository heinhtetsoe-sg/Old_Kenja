<?php

require_once('for_php7.php');

class knjl022equery extends Query {

    function getNameMst($model, $namecd1, $nameSpare1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($nameSpare1) {
            $query .= "     AND NAMESPARE1 = '{$nameSpare1}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getNameMstNamecd2($model, $namecd1, $namecd2) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    function selectCsvQuery($model) {
        $query  = " SELECT ";
        $query .= "     BASE.ENTEXAMYEAR, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     L003.NAME1 AS APPLICANTDIV, ";
        $query .= "     L004.NAME1 AS TESTDIV, ";
        $query .= "     L045.NAME1 AS TESTDIV1, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     FSCHOOL.FINSCHOOL_NAME, ";
        $query .= "     SEQ034.REMARK1 AS SEQ034_R1, ";
        $query .= "     SEQ034.REMARK2 AS SEQ034_R2, ";
        $query .= "     SEQ034.REMARK3 AS SEQ034_R3, ";
        $query .= "     SEQ034.REMARK4 AS SEQ034_R4 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN NAME_MST L003 ON L003.NAMECD1 = 'L003' ";
        $query .= "          AND BASE.APPLICANTDIV = L003.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L004 ON L004.NAMECD1 = 'L004' ";
        $query .= "          AND BASE.TESTDIV = L004.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L045 ON L045.NAMECD1 = 'L045' ";
        $query .= "          AND BASE.TESTDIV1 = L045.NAMECD2 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ034 ON BASE.ENTEXAMYEAR = SEQ034.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = SEQ034.APPLICANTDIV ";
        $query .= "          AND BASE.EXAMNO = SEQ034.EXAMNO ";
        $query .= "          AND SEQ034.SEQ = '034' ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND BASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.ENTEXAMYEAR, ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

}
?>
