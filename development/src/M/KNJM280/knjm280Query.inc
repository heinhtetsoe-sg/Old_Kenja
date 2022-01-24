<?php

require_once('for_php7.php');


class knjm280Query extends Query {

    //担当者
    function Getstaff($model)
    {
        $query  = "SELECT ";
        $query .= "    t1.STAFFCD, ";
        $query .= "    t2.STAFFNAME ";
        $query .= "FROM ";
        $query .= "    REP_PRESENT_DAT t1 LEFT JOIN STAFF_MST t2 ON t1.STAFFCD = t2.STAFFCD ";
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    t1.STAFFCD IS NOT NULL AND ";
        $query .= "    t1.STAFFCD <> '' ";
        $query .= "GROUP BY ";
        $query .= "    t1.STAFFCD, ";
        $query .= "    t2.STAFFNAME ";

        return $query;
    }

}
?>
