<?php

require_once('for_php7.php');

class knjx_sienkeikakuQuery extends Query
{
    //所見データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     CASE WHEN RECORD_DATE = 'NEW' THEN '0000-00-00' ELSE RECORD_DATE END DESC; ";

        return $query;
    }
}
