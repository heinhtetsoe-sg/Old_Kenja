<?php

require_once('for_php7.php');

class knja190kQuery extends Query {
    //生徒取得//---2005.07.20
    function getSchno($model,$year,$semester)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "     ON T3.YEAR=T1.YEAR ";
        $query .= "     AND T3.SEMESTER=T1.SEMESTER ";
        $query .= "     AND T3.GRADE=T1.GRADE ";
        $query .= "     AND T3.HR_CLASS=T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '{$year}' ";
        $query .= "     AND T1.SEMESTER   = '{$semester}' ";
        $query .= "     AND T1.GRADE||T1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        $query .= "     AND T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

}
?>
