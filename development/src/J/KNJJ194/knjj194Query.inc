<?php

require_once('for_php7.php');

class knjj194Query extends Query {
    //支部取得
    function getBranchMst() {
        $query  = " SELECT ";
        $query .= "     BRANCHNAME AS LABEL, ";
        $query .= "     BRANCHCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     BRANCH_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
