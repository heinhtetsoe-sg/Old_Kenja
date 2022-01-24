<?php

require_once('for_php7.php');

class knjc181kQuery extends Query {

    function getSemester() {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";

        return $query;
    }
}
?>
