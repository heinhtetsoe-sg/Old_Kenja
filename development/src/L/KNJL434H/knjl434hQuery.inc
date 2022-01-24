<?php

require_once('for_php7.php');

class knjl434hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //受験型取得
    public function getExamtypeMst($model, $examtype = "")
    {
        $query .= " SELECT ";
        $query .= "     EXAM_TYPE AS VALUE, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL,     ";
        $query .= "     REMARK1 AS TESTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($examtype) {
            $query .= " AND EXAM_TYPE = '{$examtype}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $namecd1, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     SETTING_CD   = '{$namecd1}' ";
        if ($seq) {
            $query .= " AND SEQ = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }
    
    //科目取得
    public function getTestsubclasscd($model, $testsubclasscd = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD || ':' || S1.NAME1 AS LABEL, ";
        $query .= "     T1.TESTSUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST S1 ";
        $query .= "          ON S1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L009' ";
        $query .= "         AND S1.SEQ          = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->field["EXAMTYPE"]}' ";
        if ($testsubclasscd) {
            $query .= "     AND T1.TESTSUBCLASSCD = '{$testsubclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CAST(VALUE AS INTEGER) ";

        return $query;
    }
}
