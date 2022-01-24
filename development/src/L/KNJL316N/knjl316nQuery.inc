<?php

require_once('for_php7.php');

class knjl316nquery extends Query
{
    //名称マスタ
    public function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->test_year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    public function getTestSubclasscd($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->test_year}' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ出力
    public function getCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     N1.NAME1 AS SHDIV_NAME, ";
        foreach ($model->subclassList as $subCd => $subName) {
            $query .= "     S{$subCd}.SCORE AS SCORE{$subCd}, ";
        }
        $query .= "     C1.EXAMCOURSE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "         AND B1.EXAMNO       = RCPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD1 ";
        $query .= "          ON BD1.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BD1.EXAMNO      = RCPT.EXAMNO ";
        $query .= "         AND BD1.SEQ         = '001' ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L006' AND N1.NAMECD2 = B1.SHDIV ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "          ON C1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND C1.TESTDIV      = B1.TESTDIV ";
        $query .= "         AND C1.COURSECD     = BD1.REMARK8 ";
        $query .= "         AND C1.MAJORCD      = BD1.REMARK9 ";
        $query .= "         AND C1.EXAMCOURSECD = BD1.REMARK10 ";
        foreach ($model->subclassList as $subCd => $subName) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S{$subCd} ";
            $query .= "          ON S{$subCd}.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
            $query .= "         AND S{$subCd}.APPLICANTDIV = RCPT.APPLICANTDIV ";
            $query .= "         AND S{$subCd}.TESTDIV      = RCPT.TESTDIV  ";
            $query .= "         AND S{$subCd}.EXAM_TYPE    = RCPT.EXAM_TYPE ";
            $query .= "         AND S{$subCd}.RECEPTNO     = RCPT.RECEPTNO ";
            $query .= "         AND S{$subCd}.TESTSUBCLASSCD = '{$subCd}' ";
        }
        $query .= " WHERE  ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->test_year}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND RCPT.TESTDIV        = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '1' ";
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }
}
