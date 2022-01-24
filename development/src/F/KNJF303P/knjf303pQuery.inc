<?php

require_once('for_php7.php');

class knjf303pQuery extends Query {
    //学校名称2取得
    function getSchoolName2() {
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

    //学期名取得
   function getSemeName()
   {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

}
?>
