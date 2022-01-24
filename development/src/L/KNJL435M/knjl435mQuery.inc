<?php

require_once('for_php7.php');

class knjl435mQuery extends Query
{
    // 入試区分
    public function getApplicant($model)
    {
        $query  = " SELECT ";
        $query .= "     applicant_div || ':' || applicant_name AS label, ";
        $query .= "     applicant_div AS VALUE ";
        $query .= " FROM ";
        $query .= "     entexam_std_applicant_mst ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->test_year}' ";
        $query .= "     AND exam_school_kind = '{$model->field["EXAM_SCHOOL_KIND"]}' ";

        return $query;
    }

    // コース
    public function selectCourseDivQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY AS course_div, ";
        $query .= "     exam_name ";
        $query .= " FROM ";
        $query .= "     entexam_std_mst ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->test_year}' ";
        $query .= "     AND exam_school_kind = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        $query .= "     AND applicant_div = '{$model->field["APPLICANT_DIV"]}' ";

        return $query;
    }

    // 校種取得
    public function getSchoolKindName($year)
    {
        $query  = " SELECT ";
        $query .= "     namecd2 || ':' || abbv1 AS label, ";
        $query .= "     namecd2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     name_mst ";
        $query .= " WHERE ";
        $query .= "     namecd1 = 'L003' ";
        $query .= "     AND namecd2 IN ( ";
        $query .= "         SELECT ";
        $query .= "             exam_school_kind ";
        $query .= "         FROM ";
        $query .= "             entexam_std_mst ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$year}' ";
        $query .= "         GROUP BY ";
        $query .= "             exam_school_kind ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     namecd2 ";

        return $query;
    }

    //CSVデータ取得
    public function selectCvsQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     APP_BASE.NAME, ";
        $query .= "     APP_BASE.NAME_KANA ";

        foreach ($model->courseDivs as $key => $val) {
            $query .= " , case RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY when '".$val."' then RECEPT.receptno  else null  end ";
            $query .= " , case RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY when '".$val."' then RECEPT.JUDGEMENT else null  end ";
        }

        $query .= " FROM ";
        $query .= "     entexam_std_recept_dat RECEPT ";
        $query .= "     RIGHT JOIN entexam_std_applicantbase_dat APP_BASE ";
        $query .= "         ON RECEPT.YEAR = APP_BASE.YEAR ";
        $query .= "         AND RECEPT.examno = APP_BASE.examno ";
        $query .= " ORDER BY ";
        $query .= "     APP_BASE.name_kana ";

        return $query;
    }
}
