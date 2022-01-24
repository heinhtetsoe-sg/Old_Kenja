<?php

require_once('for_php7.php');

class knjl100equery extends Query
{

    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd1 == "L065") {
            $query .= " AND NAMESPARE1 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getNameMstNamecd2($model, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    public function selectCsvQuery($model)
    {

        if ($model->field["TESTDIV"] == '1') {
            $nameSpare1 = "'1'";
        } elseif ($model->field["TESTDIV"] == '2') {
            $nameSpare1 = "'2', '3'";
        } elseif ($model->field["TESTDIV"] == '3') {
            $nameSpare1 = "'4'";
        }

        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.TESTDIV1, ";
        $query .= "     SEQ030.REMARK4 AS SEQ030_R4, ";
        $query .= "     SEQ030.REMARK5 AS SEQ030_R5 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ030 ON BASE.ENTEXAMYEAR = SEQ030.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = SEQ030.APPLICANTDIV ";
        $query .= "          AND BASE.EXAMNO = SEQ030.EXAMNO ";
        $query .= "          AND SEQ030.SEQ = '030' ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND BASE.TESTDIV IN ( ";
        $query .= "                          SELECT ";
        $query .= "                              NAMECD2 ";
        $query .= "                          FROM ";
        $query .= "                              V_NAME_MST ";
        $query .= "                          WHERE ";
        $query .= "                                  YEAR       = '{$model->ObjYear}' ";
        $query .= "                              AND NAMECD1    = 'L004' ";
        $query .= "                              AND value(NAMESPARE1, '0') IN ({$nameSpare1}) ";
        $query .= "                          ) ";
        $query .= " ORDER BY ";
        $query .= "     BASE.ENTEXAMYEAR, ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }
}
?>
