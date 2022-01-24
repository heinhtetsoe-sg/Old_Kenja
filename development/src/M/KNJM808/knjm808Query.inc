<?php

require_once('for_php7.php');


class knjm808Query extends Query {

    //年度取得
    function getYear($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
