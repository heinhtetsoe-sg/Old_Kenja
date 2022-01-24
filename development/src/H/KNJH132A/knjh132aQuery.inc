<?php

require_once('for_php7.php');

class knjh132aQuery extends Query {

    //学期取得
    function getSemester($semester="")
    {
        $query  = "SELECT ";
        if($semester == ""){
            $query .= "    SEMESTER AS VALUE, ";
            $query .= "    SEMESTERNAME AS LABEL ";
        }else{
            $query .= "    SEMESTERNAME AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if($semester == ""){
            $query .= "    AND SEMESTER < '9' ";
        }else{
            $query .= "    AND SEMESTER = '$semester' ";
        }
        if($semester == ""){
            $query .= "ORDER BY ";
            $query .= "    VALUE ";
        }
        return $query;
    }

}
?>
