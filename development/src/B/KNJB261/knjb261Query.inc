<?php

require_once('for_php7.php');

class knjb261Query extends Query {

    //学期取得
    function getSemester($model, $seme = "") {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->year}' AND ";
        if ($seme == "9") {
            $query .= "    SEMESTER = '9' ";
        } else {
            $query .= "    SEMESTER <> '9' ";
        }
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

}
?>
