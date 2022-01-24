<?php
class knjl068iquery extends Query
{
    public function getNameMst($model, $namecd1, $namecd2 = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->examYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試制度名の取得
    public function getApplicantDivName($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '{$model->examYear}' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= "   AND NAMECD2 = '{$model->field["APPLICANTDIV"]}' ";

        return $query;
    }

    //入試区分コンボ
    public function getEntexamTestDivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分名の取得
    public function getEntexamTestDivName($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV_ABBV ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";

        return $query;
    }

    public function selectCsvQuery($model)
    {
        $setExamHead = $model->field["APPLICANTDIV"] == "1" ? "111400" : "211700";
        $query  = " SELECT ";
        $query .= "     {$setExamHead} || T1.EXAMNO AS EXAMNO, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     VARCHAR_FORMAT(T1.BIRTHDAY, 'YYMMDD') AS BIRTHDAY_1, ";
        $query .= "     T1.FS_CD AS FS_CD_1, ";
        $query .= "     VARCHAR_FORMAT(T1.FS_DAY, 'YYMM') AS FS_YM_1, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.GNAME, ";
        $query .= "     REPLACE(T2.GZIPCD, '-', '') AS GZIPCD_1, ";
        $query .= "     T2.GADDRESS1 || T2.GADDRESS2 AS GADDRESS_1, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     '1' AS GOKAKU_FLAG, ";
        $query .= "     '0' AS REMARK1, ";
        $query .= "     REPLACE(T2.ZIPCD, '-', '') AS ZIPCD_1, ";
        $query .= "     T2.ADDRESS1 || T2.ADDRESS2 AS ADDRESS_1, ";
        $query .= "     T2.TELNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "         ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND VALUE(T1.JUDGEMENT, '') <> '5' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
