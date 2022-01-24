<?php

require_once('for_php7.php');

class knjh400_kekkazisuuQuery extends Query
{
    //年-学年
    public function getYearGrade($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '-' || T1.GRADE AS VALUE, ";
        $query .= "     VALUE(T1.YEAR, '') || '年 ' || VALUE(T2.GRADE_NAME1,T1.GRADE) AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= " ORDER BY ";
        $query .= "    VALUE DESC ";

        return $query;
    }

    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    //学校情報取得
    public function getSchoolMst($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.SUB_OFFDAYS, ";
        $query .= "     T1.SUB_ABSENT, ";
        $query .= "     T1.SUB_SUSPEND, ";
        $query .= "     T1.SUB_MOURNING, ";
        $query .= "     T1.SUB_VIRUS ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T2.GRADE = '{$model->grade}' ";

        return $query;
    }

    public function selectQuery($model, $offdays, $absent, $suspend, $mourning, $virus)
    {
        $query  = " WITH TEMPTABLE AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "      ON T1.YEAR     = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " INNER JOIN CHAIR_DAT T3 ";
        $query .= "      ON T1.YEAR     = T3.YEAR ";
        $query .= "     AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T2.CHAIRCD  = T3.CHAIRCD ";
        $query .= " LEFT JOIN SUBCLASS_MST T4 ";
        $query .= "      ON T3.CLASSCD       = T4.CLASSCD ";
        $query .= "     AND T3.SCHOOL_KIND   = T4.SCHOOL_KIND ";
        $query .= "     AND T3.CURRICULUM_CD = T4.CURRICULUM_CD ";
        $query .= "     AND T3.SUBCLASSCD    = T4.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.YEAR     = '{$model->year}' ";
        $query .= "     AND T1.GRADE    = '{$model->grade}' ";
        $query .= " ) ";
        $query .= "  ";
        $query .= " SELECT  ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     VALUE(SUM(T5.LESSON), 0) AS LESSON, ";
        $query .= "     SUM(VALUE(SICK, 0) + VALUE(NOTICE, 0) + VALUE(NONOTICE, 0) + VALUE(NURSEOFF, 0) ";
        if ($offdays == 1) {
            $query .= "     + VALUE(OFFDAYS, 0) ";
        }
        if ($absent == 1) {
            $query .= "     + VALUE(ABSENT, 0) ";
        }
        if ($suspend == 1) {
            $query .= "     + VALUE(SUSPEND, 0) ";
        }
        if ($mourning == 1) {
            $query .= "     + VALUE(MOURNING, 0) ";
        }
        if ($virus == 1) {
            $query .= "     + VALUE(VIRUS, 0) ";
        }
        $query .= "     )AS SUMVALUE ";
        $query .= " FROM ";
        $query .= "     TEMPTABLE T1 ";
        $query .= " LEFT JOIN ATTEND_SUBCLASS_DAT T5 ";
        $query .= "      ON T5.COPYCD = '0' ";
        $query .= "     AND T1.YEAR = T5.YEAR ";
        $query .= "     AND T1.SEMESTER = T5.SEMESTER ";
        $query .= "     AND T1.SCHREGNO = T5.SCHREGNO ";
        $query .= "     AND T1.CLASSCD = T5.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "         VALUE ";

        return $query;
    }
}
