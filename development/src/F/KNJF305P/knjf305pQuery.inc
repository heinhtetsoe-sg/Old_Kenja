<?php

require_once('for_php7.php');

class knjf305pQuery extends Query {

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
