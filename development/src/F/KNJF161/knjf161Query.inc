<?php

require_once('for_php7.php');

class knjf161Query extends Query {
    //学校名称2取得
    function getSchoolName2($model, $year) {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME2 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";

        return $query;
    }
}
?>
