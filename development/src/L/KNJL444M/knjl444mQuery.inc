<?php

require_once('for_php7.php');

class knjl444mQuery extends Query
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
        $query .= "     exam_name AS label, ";
        $query .= "     (exam_school_kind || applicant_div || course_div || frequency) AS VALUE ";
        $query .= " FROM ";
        $query .= "     entexam_std_mst ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND exam_school_kind = '{$schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // CSVデータ取得
    public function getCSVdata($year, $keys)
    {
        $query  = " SELECT ";
        $query .= "     a.examno, ";
        $query .= "     a.RECEPTNO, ";
        $query .= "     b.name, ";
        $query .= "     b.name_kana, ";
        $query .= "     a.judgement, ";
        $query .= "     b.deposit, ";
        $query .= "     b.fee, ";
        $query .= "     b.decline ";
        $query .= " FROM ";
        $query .= "     entexam_std_recept_dat a JOIN entexam_std_applicantbase_dat b ";
        $query .= "         ON a.YEAR = b.YEAR ";
        $query .= "         AND a.examno = b.examno ";
        $query .= " WHERE ";

        foreach ($keys as $key => $val) {
            // 指定された「出力対象志望区分」の数分ループする
            $v1 = substr($val, 0, 1);       // 校種
            $v2 = substr($val, 1, 2);       // 入試区分
            $v3 = substr($val, 3, 4);       // コース
            $v4 = substr($val, 7, 2);       // 回数
            $query .= $key > 0 ? " or ": "";
            $query .= "     ( ";
            $query .= "         a.YEAR = '{$year}' ";
            $query .= "         AND a.exam_school_kind = '{$v1}' ";
            $query .= "         AND a.applicant_div = '{$v2}' ";
            $query .= "         AND a.course_div = '{$v3}' ";
            $query .= "         AND a.frequency = '{$v4}' ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     A.RECEPTNO ";
        return $query;
    }
}
