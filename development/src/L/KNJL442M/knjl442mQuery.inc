<?php

require_once('for_php7.php');

class knjl442mQuery extends Query
{
    // 入試区分
    public function getApplicant($model)
    {
        $query  = " SELECT ";
        $query  .= "      APPLICANT_DIV || ':' || APPLICANT_NAME AS LABEL ";
        $query  .= "      , APPLICANT_DIV AS VALUE  ";
        $query  .= "  FROM ";
        $query  .= "      ENTEXAM_STD_APPLICANT_MST ";
        $query  .= "  WHERE ";
        $query  .= "      YEAR = '". $model->test_year."' ";
        $query  .= "      AND EXAM_SCHOOL_KIND = '". $model->field["EXAM_SCHOOL_KIND"] ."' ";

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
        $query .= "           STDMST.YEAR             = '{$model->test_year}' ";
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
        $query .= "           YEAR             = '{$model->test_year}' ";
        $query .= "       AND EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        $query .= "       AND APPLICANT_DIV    = '{$model->field["APPLICANT_DIV"]}' ";
        $query .= "       AND COURSE_DIV       = '{$model->field["COURSECODE"]}' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    // 校種取得
    public function getSchoolKindName($year)
    {
        $query = " SELECT ";
        $query .= "     NAMECD2 || ':' || ABBV1 AS LABEL ";
        $query .= "     , NAMECD2 AS VALUE  ";
        $query .= " FROM ";
        $query .= "     NAME_MST  ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L003'  ";
        $query .= "     AND NAMECD2 IN (  ";
        $query .= "         SELECT ";
        $query .= "             EXAM_SCHOOL_KIND  ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_STD_MST  ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '" . $year . "'  ";
        $query .= "         GROUP BY ";
        $query .= "             EXAM_SCHOOL_KIND ";
        $query .= "     )  ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //CSVデータ取得
    public function selectCvsQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "      RECEPT.RECEPTNO, ";
        $query .= "      FLOOR(SUM(RANK_D.EXAM_SCORE)) AS SUM_SCORE ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "      INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "           ON BASE.YEAR     = RECEPT.YEAR ";
        $query .= "          AND BASE.EXAMNO   = RECEPT.EXAMNO ";
        $query .= "      LEFT JOIN ENTEXAM_STD_RANK_DAT RANK_D ";
        $query .= "           ON RANK_D.YEAR     = RECEPT.YEAR ";
        $query .= "          AND RANK_D.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "          AND RANK_D.SUMMARY_DIV  = 'S' ";
        $query .= "  WHERE ";
        $query .= "      RECEPT.YEAR                 = '{$model->test_year}' ";
        $query .= "      AND RECEPT.EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        $query .= "      AND RECEPT.APPLICANT_DIV    = '{$model->field["APPLICANT_DIV"]}' ";
        if ($model->field["COURSECODE"] != "") {
            $query .= "      AND RECEPT.COURSE_DIV = '{$model->field["COURSECODE"]}' ";
        }
        if ($model->field["FREQUENCY"] != "") {
            $query .= "      AND RECEPT.FREQUENCY  = '{$model->field["FREQUENCY"]}' ";
        }
        $query .= "      AND BASE.ENTERING_FLG   = '1' ";
        $query .= "  GROUP BY ";
        $query .= "      RECEPT.RECEPTNO ";

        return $query;
    }
}
