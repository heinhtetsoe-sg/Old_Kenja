<?php

require_once('for_php7.php');

class knjx_ziritukatudou_selectQuery extends Query
{
    //自立活動取得
    public function getZiritukatudou($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT T1 ";
        $query .= " LEFT JOIN SEMESTER_MST T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$model->exp_year}' ";
        $query .= "     AND T1.SCHREGNO='{$model->schregno}' ";
        $query .= "     AND T1.DIV = '01' ";
        $query .= "     AND T1.CODE = '01' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER ";

        return $query;
    }
}
