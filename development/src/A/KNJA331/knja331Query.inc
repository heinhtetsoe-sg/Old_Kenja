<?php

require_once('for_php7.php');

class knja331Query extends Query {

    //CONTROL_MSTの年度取得
    function getCtrl()
    {
        $query  = " SELECT CTRL_YEAR FROM CONTROL_MST ";
        return $query;
    }

    //DELETE
    function getDeleteQuery($year)
    {
        $query  = " DELETE FROM CHAIR_CLS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        return $query;
    }

}
?>
