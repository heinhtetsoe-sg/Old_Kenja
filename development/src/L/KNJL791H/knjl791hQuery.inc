<?php
class knjl791hQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
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

    //入試区分取得
    public function getTestDiv($year, $applicantDiv, $testDiv = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($testDiv) {
            $query .= " AND TESTDIV = '{$testDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV出力
    public function SelectQueryCsv($model)
    {
        $query  = " SELECT ";
        $query .= "     BD009.REMARK1 AS EXAMNO, ";
        $query .= "     T1.BIRTHDAY AS PASSWORD, ";
        $query .= "     T1.JUDGEMENT, ";
        $query .= "     SET_L013.NAME1, ";
        $query .= "     SET_L013.NAMESPARE1, ";
        $query .= "     '' AS REMARK1, ";
        $query .= "     '' AS REMARK2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD009 ";
        $query .= "            ON BD009.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND BD009.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND BD009.EXAMNO       = T1.EXAMNO ";
        $query .= "           AND BD009.SEQ          = '009' ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST SET_L013 ";
        $query .= "            ON SET_L013.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND SET_L013.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND SET_L013.SETTING_CD   = 'L013' ";
        $query .= "           AND SET_L013.SEQ          = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '" . $model->examyear . "' ";
        $query .= "   AND T1.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "   AND T1.TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "   AND SET_L013.NAMESPARE1 = '1' ";
        }
        $query .= " ORDER BY  ";
        $query .= "     BD009.REMARK1, ";
        $query .= "     T1.EXAMNO ";
        return $query;
    }
}
