<?php

require_once('for_php7.php');

class knjl315nquery extends Query
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
        $query .= "     ABBV1 AS LABEL ";
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
        $query .= "     N1.NAME1 AS KYOIKU_NAME, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     C1.EXAMCOURSE_NAME, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        foreach ($model->subclassList as $subCd => $subName) {
            $query .= "     S{$subCd}.SCORE AS SCORE{$subCd}, ";
        }
        $query .= "     RCPT.TOTAL4, ";
        $query .= "     '' AS TEST_KAIJOU, ";
        $query .= "     '' AS INTERVIEW_KAIJOU, ";
        $query .= "     I1.INTERVIEW_VALUE, ";
        $query .= "     I1.INTERVIEW_REMARK, ";
        $query .= "     I1.INTERVIEW_VALUE2, ";
        $query .= "     I1.INTERVIEW_REMARK2, ";
        $query .= "     RCPT.DIV_RANK4, ";
        $query .= "     CNF.TOTAL_ALL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "         AND B1.EXAMNO       = RCPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD1 ";
        $query .= "          ON BD1.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BD1.EXAMNO      = RCPT.EXAMNO ";
        $query .= "         AND BD1.SEQ         = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD9 ";
        $query .= "          ON BD9.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BD9.EXAMNO      = RCPT.EXAMNO ";
        $query .= "         AND BD9.SEQ         = '009' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CNF ";
        $query .= "          ON CNF.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "         AND CNF.EXAMNO      = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT I1 ";
        $query .= "          ON I1.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "         AND I1.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "         AND I1.TESTDIV      = RCPT.TESTDIV ";
        $query .= "         AND I1.EXAMNO       = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L026' AND N1.NAMECD2 = BD9.REMARK9 ";
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
        $query .= "     AND B1.SHDIV            = '{$model->field["SHDIV"]}' ";
        $query .= "     AND I1.INTERVIEW_REMARK IS NOT NULL "; //面接内容
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }
}
