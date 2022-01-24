<?php

require_once('for_php7.php');

class knjx_transfer_selectQuery extends Query
{
    //日付範囲取得
    public function getDate($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CASE WHEN L1.ENT_DATE IS NOT NULL THEN L1.ENT_DATE ELSE T2.ENT_DATE END AS ENT_DATE, ";
        $query .= "     CASE WHEN L1.ENT_DATE IS NOT NULL THEN VALUE(L1.GRD_DATE,'9999-12-31') ELSE VALUE(T2.GRD_DATE,'9999-12-31') END AS GRD_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO AND T3.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //異動情報取得
    public function getSchregTransferDat($model, $sdate, $edate)
    {
        $query  =" SELECT ";
        $query .= "   T1.TRANSFERCD, ";
        $query .= "   L1.NAME1 AS TRANSFERNAME, ";
        $query .= "   T1.TRANSFER_SDATE, ";
        $query .= "   T1.TRANSFER_EDATE, ";
        $query .= "   T1.TRANSFERREASON, ";
        $query .= "   T1.TRANSFERPLACE ";
        $query .= " FROM ";
        $query .= "   SCHREG_TRANSFER_DAT T1 ";
        $query .= "   LEFT JOIN V_NAME_MST L1 ";
        $query .= "        ON L1.YEAR = '".$model->exp_year."' ";
        $query .= "       AND L1.NAMECD1 = 'A004' ";
        $query .= "       AND L1.NAMECD2 = T1.TRANSFERCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     ( ";
        $query .= "         (T1.TRANSFER_SDATE                      <= '{$sdate}' AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') >= '{$edate}') OR ";
        $query .= "         (T1.TRANSFER_SDATE                      >= '{$sdate}' AND T1.TRANSFER_SDATE                      <= '{$edate}') OR ";
        $query .= "         (VALUE(T1.TRANSFER_EDATE, '9999-12-31') >= '{$sdate}' AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') <= '{$edate}') ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.TRANSFER_SDATE, ";
        $query .= "     T1.TRANSFERCD ";

        return $query;
    }
}
