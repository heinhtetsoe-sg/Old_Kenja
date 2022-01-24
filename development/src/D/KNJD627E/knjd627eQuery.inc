<?php

require_once('for_php7.php');

class knjd627eQuery extends Query
{
    //前期後期リスト取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE  ";
        $query .= "     NAMECD1 = 'D039' "; // 関学専用を指す

        return $query;
    }

    //年取得
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_NAME1 AS NAME, ";
        $query .= "     GRADE_CD,";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "    YEAR='{$model->year}' ";

        if ($model->isIndicateSchoolKind()) {
            $query .= "    AND SCHOOL_KIND ='{$model->selectSchoolKind}' ";
        }

        $query .= " ORDER BY SCHOOL_KIND DESC, GRADE_CD ASC";

        return $query;
    }

    //年組取得
    public function getHrClass($model, $semester = "", $grade = "")
    {
        $query  = " SELECT ";
        $query .= "     HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME  AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        if ($semester != "") {
            $query .= " AND SEMESTER = '{$semester}' ";
        }
        if ($grade != "") {
            $query .= " AND GRADE = '{$grade}' ";
        }

        return $query;
    }
}
