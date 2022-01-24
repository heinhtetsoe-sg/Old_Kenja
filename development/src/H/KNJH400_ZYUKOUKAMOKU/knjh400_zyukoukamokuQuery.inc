<?php

require_once('for_php7.php');

class knjh400_zyukoukamokuQuery extends Query
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

    public function selectQuery($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     T3.CHAIRCD, ";
        $query .= "     T3.CHAIRNAME, ";
        $query .= "     CASE WHEN T3.TAKESEMES = '0' THEN '通年' ELSE T6.SEMESTERNAME END AS SEMESTERNAME, ";
        $query .= "     T7.NAME1, ";
        $query .= "     T5.CREDITS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN CHAIR_STD_DAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " LEFT JOIN CHAIR_DAT T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR ";
        $query .= "     AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T2.CHAIRCD = T3.CHAIRCD ";
        $query .= " LEFT JOIN SUBCLASS_MST T4 ";
        $query .= "     ON T3.CLASSCD = T4.CLASSCD ";
        $query .= "     AND T3.SCHOOL_KIND = T4.SCHOOL_KIND ";
        $query .= "     AND T3.CURRICULUM_CD = T4.CURRICULUM_CD ";
        $query .= "     AND T3.SUBCLASSCD = T4.SUBCLASSCD ";
        $query .= " LEFT JOIN CREDIT_MST T5 ";
        $query .= "     ON T1.GRADE = T5.GRADE ";
        $query .= "     AND T1.COURSECD = T5.COURSECD ";
        $query .= "     AND T1.MAJORCD = T5.MAJORCD ";
        $query .= "     AND T1.COURSECODE = T5.COURSECODE ";
        $query .= " LEFT JOIN SEMESTER_MST T6 ";
        $query .= "     ON T1.YEAR = T6.YEAR ";
        $query .= "     AND T2.SEMESTER = T6.SEMESTER ";
        $query .= " LEFT JOIN NAME_MST T7 ";
        $query .= "     ON T7.NAMECD1 = 'Z011' ";
        $query .= "     AND T7.NAMECD2 = T5.REQUIRE_FLG ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "         VALUE,T3.CHAIRCD ";

        return $query;
    }
}
