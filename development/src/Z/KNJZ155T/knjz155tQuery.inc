<?php

require_once('for_php7.php');


class knjz155tQuery extends Query {

    //１レコード取得
    function getYear()
    {

        $query  = "SELECT DISTINCT ";
        $query .= "    year ";
        $query .= "FROM ";
        $query .= "    textbook_ydat ";
        $query .= "ORDER BY ";
        $query .= "    1 desc ";

        return $query;
    }

}
?>
