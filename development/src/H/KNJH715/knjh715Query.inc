<?php

require_once('for_php7.php');

class knjh715Query extends Query
{
    //年組取得
    public function getHrClass($model)
    {
        $query  = "   SELECT GRADE || HR_CLASS AS VALUE ";
        $query .= "        , HR_NAME  AS LABEL";
        $query .= "     FROM SCHREG_REGD_HDAT ";
        $query .= "    WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "      AND GRADE    = '03' ";
        $query .= "      AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY HR_CLASS ";

        return $query;
    }
}
