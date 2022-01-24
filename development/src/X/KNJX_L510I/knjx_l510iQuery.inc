<?php

require_once('for_php7.php');
class knjx_l510iquery extends Query
{
    //入試区分マスタ取得
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || VALUE(TESTDIV_NAME, '') AS LABEL, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($testdiv) {
            $query .= " AND TESTDIV = '".$testdiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     ROWNUMBER() OVER() AS ROW_NUMBER, ";
        $query .= "     B1.TESTDIV0, ";
        $query .= "     B1.TESTDIV, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.BIRTHDAY, ";
        $query .= "     B1.SEX, ";
        $query .= "     B1.FS_CD AS FINSCHOOLCD, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     B1.FS_GRDYEAR, ";
        $query .= "     ADDR1.GNAME, ";
        $query .= "     ADDR1.GKANA, ";
        $query .= "     ADDR1.GZIPCD, ";
        $query .= "     ADDR1.GADDRESS1 || ADDR1.GADDRESS2 AS GADDRESS, ";
        $query .= "     ADDR1.GTELNO, ";
        $query .= "     BD031.REMARK1 AS HOPE_COURSE1, ";
        $query .= "     BD031.REMARK2 AS HOPE_COURSE2, ";
        $query .= "     BD031.REMARK3 AS HOPE_COURSE3, ";
        $query .= "     BD031.REMARK4 AS HOPE_COURSE4, ";
        $query .= "     B1.DORMITORY_FLG, ";
        $query .= "     BD031.REMARK6 AS SEQ031_REMARK6 ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ";
        $query .= "            ON ADDR1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "           AND ADDR1.EXAMNO      = B1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ";
        $query .= "            ON BD031.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD031.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD031.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD031.SEQ          = '031' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD012 ";
        $query .= "            ON BD012.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD012.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD012.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD012.SEQ          = '012' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ";
        $query .= "            ON F1.FINSCHOOLCD  = B1.FS_CD ";
        $query .= " WHERE  ";
        $query .= "     B1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND B1.TESTDIV0     = '".$model->field["TESTDIV0"]."' ";
        $query .= " AND B1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query.= " ORDER BY ";
        $query.= "     B1.EXAMNO ";

        return $query;
    }
}
