<?php

require_once('for_php7.php');

class knjl331rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMECD2 = '{$model->fixApplicantDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名
    function getSchoolName($model) {
        $certif_kindcd = ($model->field["APPLICANTDIV"] == "2") ? "105" : "106";

        $query  = "";
        $query .= " SELECT ";
        $query .= "     SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' AND ";
        $query .= "     CERTIF_KINDCD = '{$certif_kindcd}' ";

        return $query;
    }

    //CSV出力
    function getCsvQuery($model) {
        $query .= " SELECT ";
        $query .= "     BD030.REMARK2 AS EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     NMZ002.ABBV1 AS SEX, ";
        $query .= "     ADDR1.ZIPCD, ";
        $query .= "     VALUE(ADDR1.ADDRESS1,'') || VALUE(ADDR1.ADDRESS2,'') AS ADDRESS, ";
        $query .= "     ADDR1.TELNO, ";
        $query .= "     ADDR1.GNAME, ";
        $query .= "     FINSCHOOL.FINSCHOOL_NAME, ";
        $query .= "     '' AS REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ON ADDR1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "         AND ADDR1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND ADDR1.EXAMNO       = B1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD2 = B1.SEX ";
        $query .= "         AND NMZ002.NAMECD1 = 'Z002' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON FINSCHOOL.FINSCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD2 = B1.JUDGEMENT ";
        $query .= "         AND NML013.NAMECD1 = 'L013' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD030 ON BD030.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "         AND BD030.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND BD030.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND BD030.SEQ          = '030' ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR        = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV   = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND NML013.NAMESPARE1 = '1' ";
        //1:合格者 2:入学者
        if ($model->field["PRINT_DIV"] == "2") {
            $query .= "     AND B1.PROCEDUREDIV = '1' ";
            $query .= "     AND B1.ENTDIV       = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }
}
?>
