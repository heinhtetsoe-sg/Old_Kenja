<?php

require_once('for_php7.php');

class knji070Query extends Query {

    //記載責任者（職員一覧）取得
    function getSelectStaff($year) {

        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFCD || ' ' || STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
