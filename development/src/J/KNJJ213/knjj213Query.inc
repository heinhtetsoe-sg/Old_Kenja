<?php

require_once('for_php7.php');

class knjj213Query extends Query {

    //年組取得（権限チェック）
    function getEvaluationCommittee()
    {
        $query .= " SELECT ";
        $query .= "     T1.COMMITTEECD AS VALUE, ";
        $query .= "     T1.COMMITTEECD || ':' || T1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     EVALUATION_COMMITTEE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.POSITION_DIV, ";
        $query .= "     T1.POSITION_CD, ";
        $query .= "     T1.COMMITTEECD ";

        return $query;
    }
}
?>
