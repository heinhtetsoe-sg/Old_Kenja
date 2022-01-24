<?php

require_once('for_php7.php');

class knjl330aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     L003.NAME1 AS APPDIVNAME, ";
        $query .= "     TDIV.TESTDIV_NAME AS TDIV_NAME, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     L012.NAME1 AS COURSENAME, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     BASE.PROCEDUREDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD030 ";
        $query .= "          ON AD030.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND AD030.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND AD030.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         AND AD030.SEQ           = '030' ";
        $query .= "         AND AD030.REMARK1       = RCPT.TESTDIV ";
        $query .= "         AND AD030.REMARK2       = RCPT.RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST TDIV ";
        $query .= "       ON TDIV.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "       AND TDIV.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "       AND TDIV.TESTDIV = RCPT.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "          ON Z002.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND Z002.NAMECD1    = 'Z002' ";
        $query .= "         AND Z002.NAMECD2    = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L003 ";
        $query .= "          ON L003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L003.NAMECD1    = 'L003' ";
        $query .= "         AND L003.NAMECD2    = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L012 ";
        $query .= "          ON L012.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L012.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ";
        $query .= "         AND L012.NAMECD2    = BASE.ENTDIV ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '".$model->examyear."' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '".$model->field["APPLICANTDIV"]."' ";
        if ($model->field["TESTDIV"] != "ALL") {
            $query .= "     AND RCPT.TESTDIV        = '".$model->field["TESTDIV"]."' ";
        }
        $query .= "     AND RCPT.EXAM_TYPE      = '".$model->examType."' ";
        $query .= "     AND BASE.ENTDIV IS NOT NULL ";
        $query .= "     AND BASE.PROCEDUREDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     RCPT.TESTDIV, RCPT.RECEPTNO ";

        return $query;
    }
}
