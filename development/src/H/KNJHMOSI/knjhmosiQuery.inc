<?php

require_once('for_php7.php');

class knjhmosiQuery extends Query {

    //生徒情報取得
    function getName($model)
    {
        $query  = "SELECT ";
        $query .= "    L1.GRADE, ";
        $query .= "    L2.HR_NAME, ";
        $query .= "    L1.ATTENDNO, ";
        $query .= "    T1.NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.SEMESTER = '".$model->semester."' ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = L1.YEAR ";
        $query .= "         AND L2.SEMESTER = L1.SEMESTER ";
        $query .= "         AND L2.GRADE = L1.GRADE ";
        $query .= "         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

}
?>
