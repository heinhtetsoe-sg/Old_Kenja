<?php

require_once('for_php7.php');

class knjxattend_htrainremark_trainrefQuery extends Query
{
    //年度コンボ
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";

        return $query;
    }

    //年度コンボ
    public function getYear($schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TRAINREF_DAT ";
        $query .= " WHERE ";
        $query .= "       SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名前
    public function getName($schregno)
    {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //出欠備考
    public function getRemark($schregno, $year)
    {
        $query  = " SELECT ";
        $query .= "     TRAIN_SEQ, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TRAINREF_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$year}' ";
        $query .= "   AND SCHREGNO = '{$schregno}' ";
        $query .= "   AND TRAIN_SEQ BETWEEN '101' AND '106' ";

        return $query;
    }
}
