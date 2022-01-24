<?php

require_once('for_php7.php');


class knjp320Query extends Query {

    //申込み名称取得
    function getappli(){
        $query  = " SELECT APPLICATIONCD,APPLICATIONNAME ";
        $query .= " FROM ";
        $query .= "      APPLICATION_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

}
?>
