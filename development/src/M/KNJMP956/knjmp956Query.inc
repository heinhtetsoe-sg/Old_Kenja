<?php

require_once('for_php7.php');


class knjmp956Query extends Query {

    //年度
    function getYear()
    {
        $query .= " WITH YEAR_T(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."年度', '".CTRL_YEAR."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".(CTRL_YEAR + 1)."年度', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     YEAR_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
