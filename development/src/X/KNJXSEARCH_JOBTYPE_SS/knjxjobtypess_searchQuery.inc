<?php

require_once('for_php7.php');

class knjxjobtypess_searchQuery extends Query
{
    //大分類
    public function getJobtypeL()
    {
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
    public function getJobtypeM($model)
    {
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
    public function getJobtypeS($model)
    {
        $query  = " SELECT ";
        $query .= "     JOBTYPE_SCD ||'-' ||JOBTYPE_SSCD  AS VALUE, ";
        $query .= "     JOBTYPE_SCD ||'-' || JOBTYPE_SSCD ||  '-' || JOBTYPE_SNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_SS_MST ";
        $query .= " WHERE ";
        $query .= "         JOBTYPE_LCD = '".$model->field["JOBTYPE_LCD"]."' ";
        $query .= "     AND JOBTYPE_MCD = '".$model->field["JOBTYPE_MCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     JOBTYPE_SCD ";
        $query .= "    ,JOBTYPE_SSCD ";

        return $query;
    }
}
