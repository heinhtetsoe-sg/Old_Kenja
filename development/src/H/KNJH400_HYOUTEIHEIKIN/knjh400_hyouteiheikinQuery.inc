<?php

require_once('for_php7.php');

class knjh400_hyouteiheikinQuery extends Query
{
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
    //校種取得
    public function getSchoolKind($model)
    {
        $query .= " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }
    public function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     GDAT.GRADE_NAME2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "     ON T1.YEAR = GDAT.YEAR ";
        $query .= "     AND GDAT.GRADE = T1.ANNUAL ";
        $query .= "     AND GDAT.SCHOOL_KIND = '{$model->schKind}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' AND ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ANNUAL ";

        return $query;
    }
    public function selectQuery2($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD || ':' || T2.CLASSNAME AS CLASSNAME, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) AS CREDIT, ";
        $query .= "     T1.VALUATION ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1";
        $query .= " LEFT JOIN CLASS_MST T2 ";
        $query .= "     ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= " LEFT JOIN SUBCLASS_MST T3 ";
        $query .= "     ON T1.CLASSCD = T3.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD <> '1' ";
        $query .= "     AND T1.YEAR='{$model->year}' ";
        $query .= "     AND T1.SCHREGNO='{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD,VALUE,T1.ANNUAL ";

        return $query;
    }
}
