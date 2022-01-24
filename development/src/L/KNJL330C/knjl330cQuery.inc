<?php

require_once('for_php7.php');

class knjl330cQuery extends Query
{
    //学校コードを取得
    public function getNameMstZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称マスタより取得
    public function getNameMst($model, $year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAME1 AS CSV_LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L004") {
            $query .= "     AND NAMECD2 NOT IN ('5','6') ";
        }
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ出力データ取得
    public function selectCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO AS LOGIN_ID, ";
        $query .= "     T1.BIRTHDAY AS PASSWORD, ";
        $query .= "     T1.APPLICANTDIV || ";
        $query .= "     T1.TESTDIV || ";
        $query .= "     T1.SHDIV || ";
        $query .= "     T1.SEX || ";
        $query .= "     T2.JUDGEDIV || ";
        $query .= "     (CASE WHEN T3.PROCEDUREDIV IS NULL THEN '0' ELSE T3.PROCEDUREDIV END) AS STATUS, ";
        $query .= "     '' AS REMARK, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.SHDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "            ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "           AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "            ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND T3.APPLICANTDIV = '1' ";
        $query .= "           AND T3.TESTDIV      = '1' ";
        $query .= "           AND T3.EXAMNO       = T1.RECOM_EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     T1.SHDIV        = '".$model->field["SHDIV"]."' AND ";
        $query .= "     T2.JUDGEDIV IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
