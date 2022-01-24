<?php

require_once('for_php7.php');

class knjx_sogosokenQuery extends Query
{

    //調査書所見データ
    public function selectQuery($model, $annual)
    {
        $query  = " SELECT ";
        $query .= "   T1.TOTALREMARK ";
        $query .= " FROM ";
        $query .= "   HTRAINREMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR     = '{$model->year}' ";
        $query .= "   AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND T1.ANNUAL = '{$annual}' ";

        return $query;
    }
}
