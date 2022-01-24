<?php

require_once('for_php7.php');

class knjx_committee_selectQuery extends Query
{
    //対象年度取得
    public function getRegdYear($model)
    {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T2.SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T1.GRADE = T2.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM MAIN WHERE YEAR = '".$model->exp_year."') ";

        return $query;
    }

    //委員会活動参照
    public function getCommittee($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L3.NAME1 AS SEMESTER_SHOW, ";
        $query .= "     T1.SEQ, ";
        $query .= "     L1.COMMITTEENAME AS COMMITTEE_SHOW, ";
        $query .= "     T1.CHARGENAME AS CHARGE_SHOW, ";
        $query .= "     L2.NAME1 AS EXECUTIVE_SHOW, ";
        $query .= "     L4.NAME1 AS COMMITTEE_FLG_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR         = G1.YEAR ";
        $query .= "         AND T1.GRADE        = G1.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND  = G1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN COMMITTEE_MST L1 ";
        $query .= "          ON T1.COMMITTEE_FLG    = L1.COMMITTEE_FLG ";
        $query .= "         AND T1.COMMITTEECD      = L1.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD         = L1.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1      = 'J002' ";
        $query .= "         AND T1.EXECUTIVECD  = L2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L3 ";
        $query .= "          ON L3.NAMECD1      = 'J004' ";
        $query .= "         AND T1.SEMESTER     = L3.NAMECD2 ";
        $query .= "     LEFT JOIN V_NAME_MST L4 ";
        $query .= "          ON L4.YEAR         = T1.YEAR ";
        $query .= "         AND L4.NAMECD1      = 'J003' ";
        $query .= "         AND L4.NAMECD2      = T1.COMMITTEE_FLG ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR IN ('".implode($year, "','")."') AND ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }
}
