<?php

require_once('for_php7.php');

class knjx_club_selectQuery extends Query
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

    //部活動取得
    public function getClub($model, $sdate, $edate)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T2.CLUBNAME AS CLUB_SHOW, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 AS EXECUTIVE_SHOW, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     CLUB_MST T2 ON  T1.CLUBCD = T2.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        }
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' AND ";
        $query .= "     ( ";
        $query .= "         (T1.SDATE                      <= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$edate}') OR ";
        $query .= "         (T1.SDATE                      >= '{$sdate}' AND T1.SDATE                      <= '{$edate}') OR ";
        $query .= "         (VALUE(T1.EDATE, '9999-12-31') >= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') <= '{$edate}') ";
        $query .= "     ) ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND IN ( SELECT ";
            $query .= "                             S2.SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_DAT S1, ";
            $query .= "                             SCHREG_REGD_GDAT S2 ";
            $query .= "                         WHERE ";
            $query .= "                             S1.YEAR     = S2.YEAR AND ";
            $query .= "                             S1.YEAR     = '".$model->exp_year."' AND ";
            $query .= "                             S1.SEMESTER = '".$model->exp_semester."' AND ";
            $query .= "                             S1.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "                             S1.GRADE    = S2.GRADE ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }
}
