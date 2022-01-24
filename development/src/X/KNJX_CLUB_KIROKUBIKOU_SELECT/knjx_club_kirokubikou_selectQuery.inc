<?php

require_once('for_php7.php');

class knjx_club_kirokubikou_selectQuery extends Query {
    //記録備考参照
    function getSchregClubHdetailDat($model) {
        $query  = "";
        if ($model->send_prgid == 'KNJE020') {
            $query .= " WITH T_YEAR AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         R1.YEAR ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'A023' AND T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3, ";
            $query .= "         SCHREG_REGD_DAT R1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
            $query .= "         T1.SEMESTER = '".$model->exp_semester."' AND ";
            $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "         T1.SCHREGNO = R1.SCHREGNO AND ";
            $query .= "         R1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";
            $query .= " ) ";
        }

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.CLUBNAME AS CLUB_SHOW, ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     L2.HOSTNAME, ";
        $query .= "     T1.MEET_NAME, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     CASE T1.DIV WHEN '1' THEN '個人' WHEN '2' THEN '団体' ELSE '' END AS DIV_NAME, ";
        $query .= "     T1.RECORDCD, ";
        $query .= "     L3.RECORDNAME, ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     T1.KINDCD, ";
        $query .= "     L4.KINDNAME, ";
        $query .= "     T1.DOCUMENT, ";
        $query .= "     T1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST L1 ";
        $query .= "          ON T1.CLUBCD       = L1.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L1.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_HOST_MST L2 ";
        $query .= "          ON T1.HOSTCD       = L2.HOSTCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L2.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L2.SCHOOL_KIND ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L2.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L2.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_RECORD_MST L3 ";
        $query .= "          ON T1.RECORDCD     = L3.RECORDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L3.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L3.SCHOOL_KIND ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L3.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L3.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_ITEM_KIND_MST L4 ";
        $query .= "          ON T1.ITEMCD       = L4.ITEMCD ";
        $query .= "         AND T1.KINDCD       = L4.KINDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L4.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L4.SCHOOL_KIND ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L4.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L4.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if ($model->send_prgid == 'KNJE020') {
            $query .= " AND FISCALYEAR(T1.DETAIL_DATE) IN (SELECT YEAR FROM T_YEAR) ";
        } else {
            $query .= " AND FISCALYEAR(T1.DETAIL_DATE) = '".$model->exp_year."' ";
        }
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND IN ( SELECT ";
            $query .= "                             S2.SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_DAT S1, ";
            $query .= "                             SCHREG_REGD_GDAT S2 ";
            $query .= "                         WHERE ";
            $query .= "                             S1.YEAR     = S2.YEAR AND ";
            $query .= "                             S1.YEAR     = '".$model->exp_year."' AND ";
            $query .= "                             S1.SEMESTER = '".$model->exp_semester."' AND ";
            $query .= "                             S1.GRADE    = S2.GRADE AND ";
            $query .= "                             S1.SCHREGNO = '".$model->schregno."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     T1.DETAIL_DATE DESC, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }
}
?>
