<?php

require_once('for_php7.php');

class knjd627bQuery extends Query
{
    //指定年度・指定学期・指定学年におけるクラス一覧を取得
    public function selectClassListInYear($model, $semester, $gradecd = 0)
    {
        $query   = " SELECT  ";
        $query  .= "     CLASSES.GRADE AS GRADE,";
        $query  .= "     CLASSES.HR_CLASS AS HRCLASS, ";
        $query  .= "     CLASSES.HR_NAME AS LABEL ";
        $query  .= " FROM ";
        $query  .= "     SCHREG_REGD_HDAT CLASSES ";
        $query  .= " INNER JOIN ";
        $query  .= "     SCHREG_REGD_GDAT GRADES ";
        $query  .= " ON ";
        $query  .= "     GRADES.YEAR  = CLASSES.YEAR ";
        $query  .= " AND GRADES.GRADE = CLASSES.GRADE ";
        if (0 < $gradecd) {
            $query .= " AND VALUE(GRADES.GRADE_CD, 0) = {$gradecd} ";
        }
        if ($model->isIndicateSchoolKind()) {
            $query .= " AND GRADES.SCHOOL_KIND = '{$model->selectSchoolKind}' ";
        }
        $query  .= " WHERE ";
        $query  .= "     CLASSES.YEAR = '{$model->year}' ";
        $query  .= " AND CLASSES.SEMESTER = '{$semester}' ";

        return $query;
    }

    //指定年度クラスにおける生徒一覧を取得
    public function selectStudentOfClsOnYr($model, $semester)
    {
        $query   = " WITH STUDENTS AS ( ";
        $query  .= "     SELECT  ";
        $query  .= "         SCHREGNO , ";
        $query  .= "         ATTENDNO ";
        $query  .= "     FROM ";
        $query  .= "         SCHREG_REGD_DAT ";
        $query  .= "     WHERE ";
        $query  .= "         YEAR     = '{$model->year}' ";
        $query  .= "     AND SEMESTER = '{$semester}' ";
        $query  .= "     AND GRADE    = '{$model->grade}' ";
        $query  .= "     AND HR_CLASS = '{$model->hrclass}' ";
        $query  .= "     ORDER BY ATTENDNO ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "     NMLST.NAME        AS NAME,  ";
        $query  .= "     NMLST.SCHREGNO    AS SCHREGNO, ";
        $query  .= "     STUDENTS.ATTENDNO AS ATTENDNO ";
        $query  .= " FROM ";
        $query  .= "     SCHREG_BASE_MST NMLST ";
        $query  .= " INNER JOIN ";
        $query  .= "     STUDENTS ";
        $query  .= " ON ";
        $query  .= "     STUDENTS.SCHREGNO = NMLST.SCHREGNO ";
        $query  .= " ORDER BY NMLST.SCHREGNO ASC ";

        return $query;
    }
}
