<?php

require_once('for_php7.php');


class knjl434mQuery extends Query
{

    //校種取得
    public function getSchoolKindName($year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(ABBV1, '') AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '{$year}' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分一覧取得
    public function getExamList($year, $schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_NAME AS LABEL, ";
        $query .= "     (EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY) AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験マスタ取得
    public function getExamHeader($year, $key)
    {
        $key1 = substr($key, 0, 1);
        $key2 = substr($key, 1, 2);
        $key3 = substr($key, 3, 4);
        $key4 = substr($key, 7, 2);

        $query  = " SELECT ";
        $query .= "     exam_name, ";
        $query .= "     exam_date, ";
        $query .= "     CASE ";
        $query .= "         WHEN am_pm = '1' ";
        $query .= "         THEN '午前' ";
        $query .= "         WHEN am_pm = '2' ";
        $query .= "         THEN '午後' ";
        $query .= "         END AS am_pm ";
        $query .= " FROM ";
        $query .= "     entexam_std_mst ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND exam_school_kind = '{$key1}' ";
        $query .= "     AND applicant_div = '{$key2}' ";
        $query .= "     AND course_div = '{$key3}' ";
        $query .= "     AND frequency = '{$key4}' ";

        return $query;
    }

    //試験学校名取得
    public function getExamSchoolName($year, $kind)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     certif_school_dat ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND certif_kindcd = '{$kind}' ";

        return $query;
    }

    //合格した受験番号取得
    public function getRecept($year, $key)
    {
        $key1 = substr($key, 0, 1);
        $key2 = substr($key, 1, 2);
        $key3 = substr($key, 3, 4);
        $key4 = substr($key, 7, 2);

        $query  = " SELECT ";
        $query .= "     receptno ";
        $query .= " FROM ";
        $query .= "     entexam_std_recept_dat ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND exam_school_kind = '{$key1}' ";
        $query .= "     AND applicant_div = '{$key2}' ";
        $query .= "     AND course_div = '{$key3}' ";
        $query .= "     AND frequency = '{$key4}' ";
        $query .= "     AND judgement IN ('1', '2') ";  // 合格、繰り上げ合格

        return $query;
    }
}
