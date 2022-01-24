<?php

require_once('for_php7.php');


class knjmp961Query extends Query {

    //年度
    function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //月
    function getMonth($model)
    {
        $query  = " WITH MONTH_T(LABEL, VALUE, SORT) AS ( ";
        $setUnion = "";
        $sort = 0;
        foreach ($model->month as $value => $label) {
            $query .= $setUnion."     VALUES('{$label}', '{$value}', {$sort}) ";
            $setUnion = " UNION ";
            $sort++;
        }
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL, ";
        $query .= "     SORT ";
        $query .= " FROM ";
        $query .= "     MONTH_T ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

}
?>
