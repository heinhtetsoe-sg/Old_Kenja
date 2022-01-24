<?php

class knje140Query extends Query {

    //エラーデータ取得
    function getgrade($model)
    {

        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ORDER BY ";
        $query .= "     GRADE DESC ";

        return $query;
    }

}
?>
