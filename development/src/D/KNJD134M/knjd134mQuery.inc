<?php

require_once('for_php7.php');

class knjd134mQuery extends Query
{
    //学期取得
    public function getSemesterName($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->year."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' ";

        return $query;
    }

    //年組取得
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     T3.GRADE || T3.HR_CLASS AS VALUE, ";
        $query .= "     T3.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE    = T1.GRADE ";
        $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' ";
        $query .= " GROUP BY ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.HR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS ";

        return $query;
    }

    //生徒取得
    public function getSchList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO || '-'  || T1.ATTENDNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE    = T1.GRADE ";
        $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
