<?php

require_once('for_php7.php');


class knja224gQuery extends Query
{
    //学年一覧を取得する
    public function getGrade($model)
    {
        $query   = " SELECT ";
        $query  .= "     GRADE AS VALUE, ";
        $query  .= "     GRADE_NAME1 AS LABEL ";
        $query  .= " FROM ";
        $query  .= "     SCHREG_REGD_GDAT ";
        $query  .= " WHERE ";
        $query  .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //年組取得
    public function getHr($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON ";
        $query .= "                T2.YEAR  = T1.YEAR  AND ";
        $query .= "                T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR ."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' ";

        return $query;
    }
}
