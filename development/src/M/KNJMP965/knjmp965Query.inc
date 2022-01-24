<?php

require_once('for_php7.php');


class knjmp965Query extends Query {

    //収入科目
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
