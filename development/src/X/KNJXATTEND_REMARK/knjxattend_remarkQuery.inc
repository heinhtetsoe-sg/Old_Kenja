<?php

require_once('for_php7.php');

class knjxattend_remarkQuery extends Query {
    //名前
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //出欠備考
    function getRemark($schregno, $year, $sdate, $edate) {

        $query .= " WITH ATTEND_AB AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ATTEND_ABSENCE_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '{$schregno}' ";
        $query .= "         AND YEAR = '{$year}' ";
        $query .= "         AND ABSENCE_DATE <= date('{$edate}') ";
        $query .= " ), ATTEND_D AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         MIN(T1.PERIODCD) AS PERIODCD ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT T1, ";
        $query .= "         ATTEND_AB T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND T1.ATTENDDATE = T2.ABSENCE_DATE ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE ";
        $query .= " ), ATTEND_MIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         T1.DI_REMARK ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT T1, ";
        $query .= "         ATTEND_D T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND T1.ATTENDDATE = T2.ATTENDDATE ";
        $query .= "         AND T1.PERIODCD = T2.PERIODCD ";
        $query .= "     ORDER BY ";
        $query .= "         T1.ATTENDDATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.ABSENCE_DATE AS ATTENDDATE, ";
        $query .= "     L1.DI_REMARK ";
        $query .= " FROM ";
        $query .= "     ATTEND_AB T1 ";
        $query .= "     LEFT JOIN ATTEND_MIN L1 ON T1.ABSENCE_DATE = L1.ATTENDDATE ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ABSENCE_DATE ";

        return $query;
    }
    
    function getSemesRemark($schregno, $year) {

        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      COPYCD = '0' ";
        $query .= "  AND YEAR = '{$year}' ";
        $query .= "  AND SCHREGNO = '{$schregno}' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";

        return $query;
    }
}
?>
