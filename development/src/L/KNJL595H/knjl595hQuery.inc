<?php

require_once('for_php7.php');
class knjl595hQuery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

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
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試判別取得
    public function getDistinctId($model)
    {
        $query  = " SELECT ";
        $query .= "     DISTINCT_ID AS VALUE, ";
        $query .= "     DISTINCT_ID || ':' || DISTINCT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DISTINCTION_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->field["YEAR"]}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //入学者のCSV
    public function selectCvsStudentQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.ENTEXAMYEAR, ";
        $query .= "     ".CTRL_SEMESTER." AS SEMESTER, ";
        $query .= "     '' AS SCHREGNO, ";
        $query .= "     '' AS ENT_DATE, ";
        $query .= "     '1' AS ENT_DIV, ";
        $query .= "     BASE.REMARK1 AS GRADE, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     '0' AS INOUTCD, ";
        $query .= "     '' AS COURSECD, ";
        $query .= "     '' AS MAJORCD, ";
        $query .= "     '' AS COURSECODE, ";
        $query .= "     RCPT.EXAM_TYPE, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     CAST(REPLACE(CAST(BASE.BIRTHDAY AS CHAR(10)), '-', '/') AS CHAR(10)) AS BIRTHDAY, ";
        $query .= "     BASE.SEX, "; //コードで出力する
        $query .= "     FSRP.FS_CD AS FS_CD, ";
        $query .= "     CASE WHEN VALUE(BASE.FS_GRDYEAR, '') != '' ";
        $query .= "          THEN FS_GRDYEAR || '/3/31' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS FINSCHOOLGRADDATE, ";
        $query .= "     PSRP.PS_CD AS PRISCHOOLCD, ";
        $query .= "     BASE.DORMITORY_FLG, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     '' AS FAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     ADDR.EMERGENCYCALL, ";
        $query .= "     ADDR.GTELNO AS EMERGENCYTELNO, ";
        $query .= "     '' AS SCALASHIPDIV, ";
        $query .= "     BASE.ENTEXAMYEAR AS KATEI_ENTEXAMYEAR, ";
        $query .= "     '' AS TIKUCD, ";
        $query .= "     BASE.EXAMNO AS EXAMNO, ";
        $query .= "     RCPT.TESTDIV AS TESTDIV, ";
        $query .= "     RCPT.EXAM_TYPE AS EXAM_TYPE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "                                             AND ADDR.EXAMNO      = BASE.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT RCPT ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                       AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                       AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                       AND RCPT.DISTINCT_ID  = '{$model->field["DISTINCT_ID"]}' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D008 ON D008.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                                             AND D008.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                             AND D008.TESTDIV      = RCPT.TESTDIV ";
        $query .= "                                             AND D008.EXAM_TYPE    = RCPT.EXAM_TYPE ";
        $query .= "                                             AND D008.RECEPTNO     = RCPT.RECEPTNO ";
        $query .= "                                             AND D008.SEQ          = '008' ";
        $query .= "     LEFT JOIN ENTEXAM_MIRAI_FS_REP_DAT FSRP ON FSRP.MIRAI_FS_CD = D008.REMARK1 "; // 変換テーブル（出身学校）
        $query .= "     LEFT JOIN ENTEXAM_MIRAI_PS_REP_DAT PSRP ON PSRP.MIRAI_PS_CD = D008.REMARK3 "; // 変換テーブル（塾）
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '{$model->field["YEAR"]}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND BASE.JUDGEMENT    = '1' "; //合格
        $query .= "     AND BASE.PROCEDUREDIV = '1' "; //手続済み
        $query .= "     AND BASE.ENTDIV       = '1' "; //入学有り
        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }

    //保護者のCSV
    public function selectCvsGuardQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.ENTEXAMYEAR, ";
        $query .= "     BASE.REMARK1 AS GRADE, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     '' AS NAME, ";
        $query .= "     BASE.EXAMNO AS SCHREGNO, ";
        $query .= "     '' AS ENT_DATE, ";
        $query .= "     value(ADDR.RELATIONSHIP, '01') AS RELATIONSHIP, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     '' AS SEITO_SEX, ";
        $query .= "     '' AS BIRTHDAY, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     ADDR.GFAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     '' AS GUARD_JOBCD, ";
        $query .= "     '' AS GUARD_WORK_NAME, ";
        $query .= "     '' AS GUARD_WORK_TELNO, ";
        $query .= "     value(ADDR.RELATIONSHIP, '01') AS GUARANTOR_RELATIONSHIP, ";
        $query .= "     ADDR.GNAME AS GUARANTOR_NAME, ";
        $query .= "     ADDR.GKANA AS GUARANTOR_KANA, ";
        $query .= "     '' AS GUARANTOR_SEX, ";
        $query .= "     ADDR.GZIPCD AS GUARANTOR_ZIPCD, ";
        $query .= "     ADDR.GADDRESS1 AS GUARANTOR_ADDR1, ";
        $query .= "     ADDR.GADDRESS2 AS GUARANTOR_ADDR2, ";
        $query .= "     ADDR.GTELNO AS GUARANTOR_TELNO, ";
        $query .= "     '' AS GUARANTOR_JOBCD, ";
        $query .= "     '' AS PUBLIC_OFFICE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON  ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "                                              AND ADDR.EXAMNO      = BASE.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT RCPT ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                       AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                       AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                       AND RCPT.DISTINCT_ID  = '{$model->field["DISTINCT_ID"]}' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '{$model->field["YEAR"]}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND BASE.JUDGEMENT    = '1' "; //合格
        $query .= "     AND BASE.PROCEDUREDIV = '1' "; //手続済み
        $query .= "     AND BASE.ENTDIV       = '1' "; //入学有り

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }
}
