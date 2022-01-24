<?php

require_once('for_php7.php');

class knjx_special_activity_selectQuery extends Query
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
        if ($model->send_prgid != "KNJE020") {
            $query .= "         AND T1.YEAR = '".$model->exp_year."' ";
        }
        $query .= " ), SEME AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "           T2.YEAR, T2.SEMESTER, T2.SEMESTERNAME ";
        $query .= "     FROM SPECIAL_ACTIVITY_MST T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "                               AND T2.SEMESTER <> '9' ";
        $query .= "                               AND T1.SPECIAL_SDATE BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "     INNER JOIN SPECIAL_ACTIVITY_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                               AND L1.SPECIALCD = T1.SPECIALCD ";
        $query .= "      WHERE ";
        $query .= "            L1.SCHREGNO = '".$model->schregno."' ";
        $query .= "        AND L1.SPECIAL_FLG = '1' ";
        $query .= "     ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        //$query .= "     INNER JOIN SEME T2 ON T2.YEAR = T1.YEAR  ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM MAIN WHERE YEAR = '".$model->exp_year."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";

        return $query;
    }

    //特別活動参照
    public function getSpecialActivity($model, $year)
    {
        $query .= " SELECT ";
        $query .= "       T1.SPECIALCD ";
        $query .= "     , T1.SPECIAL_SDATE ";
        $query .= "     , T1.SPECIALACTIVITYNAME ";
        $query .= "     , T1.SPECIALACTIVITYNAME AS SPECIALACTIVITYNAME_SHOW ";
        $query .= " FROM SPECIAL_ACTIVITY_MST T1 ";
        $query .= " INNER JOIN SPECIAL_ACTIVITY_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                           AND L1.SPECIALCD = T1.SPECIALCD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR IN ('".implode($year, "','")."') ";
        $query .= "    AND L1.SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND L1.SPECIAL_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "       T1.SPECIAL_SDATE ";
        $query .= "     , T1.SPECIALCD ";
        return $query;
    }
}
