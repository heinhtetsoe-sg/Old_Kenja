<?php

require_once('for_php7.php');

class knjl013aQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     NULL AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     L012.NAME1 AS COURSENAME, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     PRIS.PRISCHOOL_NAME, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.TELNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B008 ";
        $query .= "          ON B008.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND B008.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND B008.EXAMNO         = RCPT.EXAMNO ";
        $query .= "         AND B008.SEQ            = '008' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B022 ";
        $query .= "          ON B022.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND B022.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND B022.EXAMNO         = RCPT.EXAMNO ";
        $query .= "         AND B022.SEQ            = '022' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B030 ";
        $query .= "          ON B030.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND B030.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND B030.EXAMNO         = RCPT.EXAMNO ";
        $query .= "         AND B030.SEQ            = '030' ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "          ON Z002.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND Z002.NAMECD1    = 'Z002' ";
        $query .= "         AND Z002.NAMECD2    = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON FINS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST PRIS ON PRIS.PRISCHOOLCD = B008.REMARK1 ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST PRIC ON PRIC.PRISCHOOLCD = B008.REMARK1 AND PRIC.PRISCHOOL_CLASS_CD = B008.REMARK3 ";
        $query .= "     LEFT JOIN V_NAME_MST L003 ";
        $query .= "          ON L003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L003.NAMECD1    = 'L003' ";
        $query .= "         AND L003.NAMECD2    = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L012 ";
        $query .= "          ON L012.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L012.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ";
        $query .= "         AND L012.NAMECD2    = BASE.ENTDIV ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '".$model->applicantdiv."' ";
        if ($model->testdiv != "ALL") {
            $query .= "     AND RCPT.TESTDIV    = '".$model->testdiv."' ";
        }
        $query .= "     AND RCPT.EXAM_TYPE      = '".$model->examType."' ";
        $query .= "     AND BASE.ENTDIV IS NOT NULL ";              //入学コース(入学コース決定)
        $query .= "     AND B030.REMARK1        = RCPT.TESTDIV ";   //合格試験(入学コース決定)
        $query .= "     AND B030.REMARK2        = RCPT.RECEPTNO ";  //合格受験番号(入学コース決定)
        $query .= "     AND BASE.PROCEDUREDIV   = '1' ";            //払込フラグ(チェックON)
        $query .= "     AND VALUE(B022.REMARK2,'0') != '1' ";       //辞退フラグ(チェックOFF)
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }
}
?>
