<?php
class knjl043vQuery extends Query
{
    //校種コンボ
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(ABBV1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '{$model->examyear}' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 入試区分
    public function getApplicant($model)
    {
        $query  = "  SELECT ";
        $query .= "      APPLICANT_DIV || ':' || APPLICANT_NAME AS LABEL, ";
        $query .= "      APPLICANT_DIV AS VALUE ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_APPLICANT_MST ";
        $query .= "  WHERE ";
        $query .= "          YEAR             = '{$model->examyear}' ";
        $query .= "      AND EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    // コース一覧
    public function getCoursecode($model)
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      COURSECODE || ':' || COURSECODENAME AS LABEL, ";
        $query .= "      COURSECODE AS VALUE ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_MST STDMST ";
        $query .= "      INNER JOIN V_COURSECODE_MST COURSE ";
        $query .= "           ON STDMST.YEAR       = COURSE.YEAR ";
        $query .= "          AND STDMST.COURSE_DIV = COURSE.COURSECODE ";
        $query .= "  WHERE ";
        $query .= "           STDMST.YEAR             = '{$model->examyear}' ";
        $query .= "       AND STDMST.EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        $query .= "       AND STDMST.APPLICANT_DIV    = '{$model->field["APPLICANT_DIV"]}' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    // 回数一覧
    public function getFrequency($model)
    {
        $query = " SELECT ";
        $query .= "     INT(FREQUENCY) || '回' AS LABEL, ";
        $query .= "     FREQUENCY AS VALUE  ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST  ";
        $query .= "  WHERE ";
        $query .= "           YEAR             = '{$model->examyear}' ";
        $query .= "       AND EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        $query .= "       AND APPLICANT_DIV    = '{$model->field["APPLICANT_DIV"]}' ";
        $query .= "       AND COURSE_DIV       = '{$model->field["COURSE_DIV"]}' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //試験ID取得
    public function getExamId($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || '-' || APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY || ':' || EXAM_NAME AS LABEL, ";
        $query .= "     EXAM_SCHOOL_KIND || '-' || APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR             = '{$model->examyear}' ";
        $query .= "   AND EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入学者のCSV
    public function selectCsvQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "      BASE.YEAR, ";
        $query .= "      BASE.EXAMNO, ";
        $query .= "      RECEPT.RECEPTNO, ";
        $query .= "      RECEPT.SUC_COURSECD AS COURSECD, ";
        $query .= "      RECEPT.SUC_MAJORCD AS MAJORCD, ";
        $query .= "      RECEPT.SUC_COURSECODE AS COURSECODE, ";
        $query .= "      BASE.NAME, ";
        $query .= "      BASE.NAME_KANA, ";
        $query .= "      BASE.BIRTHDAY, ";
        $query .= "      BASE.SEX, ";
        $query .= "      BASE.FINSCHOOLCD, ";
        $query .= "      FINS.FINSCHOOL_NAME, ";
        $query .= "      BASE.FINISH_DATE, ";
        $query .= "      BASE.ZIPCD, ";
        $query .= "      BASE.ADDR1, ";
        $query .= "      BASE.ADDR2, ";
        $query .= "      BASE.TELNO, ";
        $query .= "      RECEPT.APPLICANT_DIV, ";
        $query .= "      GUARDIAN.RELATION, ";
        $query .= "      GUARDIAN.GUARD_NAME, ";
        $query .= "      GUARDIAN.GUARD_NAME_KANA, ";
        $query .= "      GUARDIAN.GUARD_ZIP, ";
        $query .= "      GUARDIAN.GUARD_ADDR1, ";
        $query .= "      GUARDIAN.GUARD_ADDR2, ";
        $query .= "      GUARDIAN.GUARD_TELNO ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "      INNER JOIN ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "          ON RECEPT.YEAR      = BASE.YEAR ";
        $query .= "          AND RECEPT.EXAMNO   = BASE.EXAMNO ";
        $query .= "      LEFT JOIN ENTEXAM_STD_APPLICANTGUARDIAN_DAT GUARDIAN ";
        $query .= "          ON GUARDIAN.YEAR    = BASE.YEAR ";
        $query .= "          AND GUARDIAN.EXAMNO = BASE.EXAMNO ";
        $query .= "      LEFT JOIN FINSCHOOL_MST FINS ";
        $query .= "          ON FINS.FINSCHOOLCD = BASE.FINSCHOOLCD ";
        $query .= "      LEFT JOIN ENTEXAM_STD_RANK_DAT RANK ";
        $query .= "          ON RANK.YEAR           = RECEPT.YEAR ";
        $query .= "          AND RANK.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "          AND RANK.EXAM_SUBCLASS = '99' ";
        $query .= "          AND RANK.SUMMARY_DIV   = 'TS' ";
        $query .= "  WHERE ";
        $query .= "          BASE.YEAR = '{$model->examyear}' ";
        $query .= "      AND EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        $query .= "      AND APPLICANT_DIV    = '{$model->field["APPLICANT_DIV"]}' ";
        if ($model->field["COURSE_DIV"]) {
            $query .= "      AND COURSE_DIV = '{$model->field["COURSE_DIV"]}' ";
        }
        if ($model->field["FREQUENCY"]) {
            $query .= "      AND FREQUENCY = '{$model->field["FREQUENCY"]}' ";
        }
        $query .= "      AND BASE.ENTERING_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= $model->selected_data;

        return $query;
    }
}
