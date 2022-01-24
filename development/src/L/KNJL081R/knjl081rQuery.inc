<?php

require_once('for_php7.php');

class knjl081rquery extends Query {

    function getNameMst($model, $namecd1, $namecd2='') {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
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
        $query .= "         NAMECD1 = '{$namecd1}' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    function selectCsvQuery($model) {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     case ";
        $query .= "         when RECEPT.JUDGEDIV = '1' then value(BUN013.REMARK{$model->field["TESTDIV"]}, '') || '1' ";
        $query .= "         when RECEPT.JUDGEDIV = '5' then value(BUN013.REMARK{$model->field["TESTDIV"]}, '') || '2' ";
        $query .= "         when RECEPT.JUDGEDIV = '6' then value(BUN013.REMARK{$model->field["TESTDIV"]}, '') || '3' ";
        $query .= "         when RECEPT.JUDGEDIV = '7' then value(BUN013.REMARK{$model->field["TESTDIV"]}, '') || '4' ";
        $query .= "         when RECEPT.JUDGEDIV = '2' then '81' "; //�s���i
        $query .= "         when RECEPT.JUDGEDIV = '3' then '91' "; //����
        $query .= "         else '' ";
        $query .= "     end AS GOUHI_HANTEI_CD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN013 ON BUN013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "                                                          AND BUN013.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "                                                          AND BUN013.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "                                                          AND BUN013.SEQ          = '013' ";
        $query .= " WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND RECEPT.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND RECEPT.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

}
?>
