<?php

require_once('for_php7.php');

class knjd627dQuery extends Query
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

    //下級年次取得
    public function getLowerGrade($model, $school_kind, $gradecd)
    {
        $query  = " SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";

        $year = (int)$model->year - 1;
        $grade_lower = $gradecd - 1;
        $join_condition = "";
        for (; 0 < $year; $year--) {
            $lowergcd = sprintf("%02d", $grade_lower);
            $query .= $join_condition." ( ";
            $query .= "     YEAR = '{$year}' ";
            $query .= " AND SCHOOL_KIND = '{$school_kind}' ";
            $query .= " AND GRADE_CD = '{$lowergcd}'";
            $query .= ")";
            $join_condition = " OR ";
            $grade_lower--;
            if ($grade_lower < 1) {
                break;
            }
        }

        $query .= " ORDER BY GRADE ASC";

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
