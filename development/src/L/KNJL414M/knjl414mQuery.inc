<?php

require_once('for_php7.php');

class knjl414mQuery extends Query
{
    //校種コンボ
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->examyear."' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getExamId($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || EXAM_NAME AS LABEL, ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //CSV出力
    public function getCsvList($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.YEAR, ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RECEPT.EXAM_SCHOOL_KIND, ";
        $query .= "     RECEPT.APPLICANT_DIV, ";
        $query .= "     APP.APPLICANT_NAME, ";
        $query .= "     RECEPT.COURSE_DIV, ";
        $query .= "     COURSE.COURSECODENAME, ";
        $query .= "     RECEPT.FREQUENCY, ";
        $query .= "     INT(RECEPT.FREQUENCY) || '回' AS FREQUENCY_NAME, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     STD.EXAM_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     Z002.NAME1 AS SEX, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FINSCHOOLCD, ";
        $query .= "     FINS.FINSCHOOL_NAME, ";
        $query .= "     BASE.ZIPCD, ";
        $query .= "     BASE.ADDR1, ";
        $query .= "     BASE.ADDR2, ";
        $query .= "     BASE.TELNO, ";
        $query .= "     BASE.EMAIL, ";
        $query .= "     BASE.FINISH_DATE, ";
        $query .= "     BASE.REMARK1, ";
        $query .= "     BASE.REMARK2, ";
        $query .= "     BASE.REMARK3, ";
        $query .= "     BASE.REMARK4, ";
        $query .= "     BASE.REMARK5, ";
        $query .= "     GUARDIAN.GUARD_NAME, ";
        $query .= "     GUARDIAN.GUARD_NAME_KANA, ";
        $query .= "     H201.NAME1 AS RELATION, ";
        $query .= "     GUARDIAN.GUARD_TELNO, ";
        $query .= "     GUARDIAN.GUARD_ADDR1, ";
        $query .= "     GUARDIAN.GUARD_ADDR2, ";
        $query .= "     GUARDIAN.GUARD_ZIP, ";
        $query .= "     REMARK.SPECIAL_REMARK, ";
        $query .= "     REMARK.SPECIAL_ACT1, ";
        $query .= "     REMARK.SPECIAL_ACT2, ";
        $query .= "     REMARK.SPECIAL_ACT3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "             ON RECEPT.YEAR   = BASE.YEAR ";
        $query .= "            AND RECEPT.EXAMNO = BASE.EXAMNO ";
        $query .= "            AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '{$model->examId}' ";
        $query .= "     LEFT JOIN ENTEXAM_STD_APPLICANTGUARDIAN_DAT GUARDIAN ";
        $query .= "            ON GUARDIAN.YEAR   = BASE.YEAR ";
        $query .= "           AND GUARDIAN.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT REMARK ";
        $query .= "            ON REMARK.YEAR   = BASE.YEAR ";
        $query .= "           AND REMARK.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_STD_MST STD ";
        $query .= "             ON STD.YEAR             = RECEPT.YEAR ";
        $query .= "            AND STD.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "            AND STD.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "            AND STD.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "            AND STD.FREQUENCY        = RECEPT.FREQUENCY ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ";
        $query .= "            ON FINS.FINSCHOOLCD = BASE.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST Z002 ";
        $query .= "             ON Z002.NAMECD2 = BASE.SEX ";
        $query .= "            AND Z002.NAMECD1 = 'Z002' ";
        $query .= "     LEFT JOIN NAME_MST H201 ";
        $query .= "             ON H201.NAMECD2 = GUARDIAN.RELATION ";
        $query .= "            AND H201.NAMECD1 = 'H201' ";

        $query .= "     LEFT JOIN ENTEXAM_STD_APPLICANT_MST APP ";
        $query .= "            ON APP.YEAR             = RECEPT.YEAR ";
        $query .= "           AND APP.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "           AND APP.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "     LEFT JOIN V_COURSECODE_MST COURSE ";
        $query .= "            ON COURSE.YEAR       = RECEPT.YEAR ";
        $query .= "           AND COURSE.COURSECODE = RECEPT.COURSE_DIV ";
        $query .= " WHERE ";
        $query .= "     BASE.YEAR = '{$model->examyear}' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";
        
        return $query;
    }
}
