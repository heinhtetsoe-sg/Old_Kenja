<?php

require_once('for_php7.php');

class knjxjobtypes_searchQuery extends Query {
    //大分類
    function getJobtypeL($model) {
        $query  = " SELECT ";
        $query .= "     JOBTYPE_LCD AS VALUE, ";
        $query .= "     JOBTYPE_LCD || '：' || JOBTYPE_LNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_L_MST ";
        $query .= " ORDER BY ";
        $query .= "     JOBTYPE_LCD ";

        return $query;
    }

    //中分類
    function getJobtypeM($model) {
        $query  = " SELECT ";
        $query .= "     JOBTYPE_MCD AS VALUE, ";
        $query .= "     JOBTYPE_MCD || '：' || JOBTYPE_MNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_M_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '".$model->field["JOBTYPE_LCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     JOBTYPE_MCD ";

        return $query;
    }

    //小分類(検索結果)
    function getJobtypeS($model) {
        $query  = " SELECT ";
        $query .= "     JOBTYPE_SCD AS VALUE, ";
        $query .= "     JOBTYPE_SCD || ' | ' || JOBTYPE_SNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_S_MST ";
        $query .= " WHERE ";
        $query .= "         JOBTYPE_LCD = '".$model->field["JOBTYPE_LCD"]."' ";
        $query .= "     AND JOBTYPE_MCD = '".$model->field["JOBTYPE_MCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     JOBTYPE_SCD ";

        return $query;
    }
}
?>
