<?php

require_once('for_php7.php');

class knjxattendQuery extends Query 
{
    //去年度取得
    function getLastYear($model)
    {
        $query  = "WITH YEART AS ( ";
        $query .= "SELECT ";
        $query .= "    MAX(YEAR) AS YEART ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR < '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    MAX(YEAR) AS YEAR, ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = (SELECT YEART FROM YEART) ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //次年度取得
    function getNextYear($model)
    {
        $query  = "WITH YEART AS ( ";
        $query .= "SELECT ";
        $query .= "    MIN(YEAR) AS YEART ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR > '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    MIN(YEAR) AS YEAR, ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = (SELECT YEART FROM YEART) ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //生徒情報取得
    function getName($model)
    {
        $query  = "SELECT ";
        $query .= "    L2.HR_NAME, ";
        $query .= "    L1.ATTENDNO, ";
        $query .= "    T1.NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.SEMESTER = '".$model->semester."' ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = L1.YEAR ";
        $query .= "         AND L2.SEMESTER = L1.SEMESTER ";
        $query .= "         AND L2.GRADE = L1.GRADE ";
        $query .= "         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = "WITH TOTAL_T(SEMESTER, SEMESTERNAME) AS ( ";
        $query .= "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    (VALUES('9', '合計')) T1 ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    SEMESTERNAME ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    T.SEMESTER, ";
        $query .= "    T.SEMESTERNAME ";
        $query .= "FROM ";
        $query .= "    TOTAL_T T ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学期取得
    function getNameMst($year, $namecd2)
    {
        $query  = "SELECT ";
        $query .= "    NAME1 ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$year."' ";
        $query .= "    AND NAMECD1 = 'C001' ";
        $query .= "    AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //学校マスタの取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //出欠情報書
    function getMeisaiData($model, $semester, $knjSchoolMst)
    {
        $query  = "SELECT ";
        $query .= "    SUM(LESSON) AS LESSON, ";
        $query .= "    SUM(OFFDAYS) AS OFFDAYS, ";
        if ($model->virus) {
            if ($model->koudome) {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ";
                $query .= "                WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NOT NULL THEN SUM(SUSPEND) + SUM(KOUDOME) ";
                $query .= "                WHEN SUM(VIRUS) IS NOT NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) + SUM(VIRUS) ";
                $query .= "                ELSE SUM(SUSPEND) + SUM(VIRUS) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(VIRUS) END AS SUSPEND, ";
            }
        } else {
            if ($model->koudome) {
                $query .= "           CASE WHEN SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           SUM(SUSPEND) SUSPEND, ";
            }
        }
        $query .= "    SUM(MOURNING) AS MOURNING, ";
        $query .= "    SUM(ABROAD) AS ABROAD, ";
        $query .= "    SUM(LESSON) - SUM(SUSPEND) - SUM(MOURNING) - SUM(OFFDAYS) - SUM(ABROAD) ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + SUM(OFFDAYS) ";
        }
        $query .= "    AS CLASSDAYS2, ";
        $query .= "    SUM(SICK) AS SICK, ";
        $query .= "    SUM(NOTICE) AS NOTICE, ";
        $query .= "    SUM(NONOTICE) AS NONOTICE, ";
        $query .= "    ((SUM(LESSON) - (SUM(SUSPEND) + SUM(MOURNING) + SUM(OFFDAYS) + SUM(ABROAD))) - ";
        $query .= "     (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE))) AS CLASSDAYS3 ";
        $query .= "FROM ";
        $query .= "    ATTEND_SEMES_DAT ";
        $query .= "WHERE ";
        $query .= "    COPYCD = '0' ";
        $query .= "    AND YEAR = '".$model->year."' ";
        if ($semester < 9) {
            $query .= "    AND SEMESTER = '".$semester."' ";
        }
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }
}
?>
