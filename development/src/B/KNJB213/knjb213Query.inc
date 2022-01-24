<?php

require_once('for_php7.php');

class knjb213Query extends Query {
    //職名取得
    function getJobMst($model) {
        $query  = " SELECT ";
        $query .= "     JOBCD AS VALUE, ";
        $query .= "     JOBCD || ':' || JOBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_JOB_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
