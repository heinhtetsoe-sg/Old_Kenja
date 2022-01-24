<?php

require_once('for_php7.php');

class knjd665mQuery extends Query
{
    //学期を取得
    public function getSchKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     ABBV2 DESC ";
        return $query;
    }

    //学年取得()
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "   GRADE AS VALUE, ";
        $query .= "   GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        return $query;
    }

    public function getTest($model, $searchBeforeSems = false)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "         AND T2.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";

        return $query;
    }

    public function getClassMst($model)
    {
        $query  = " SELECT ";
        $query .= "   CLASSCD AS VALUE, ";
        $query .= "   CLASSCD || ':' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "   SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " ORDER BY ";
        $query .= "   CLASSCD ";
        return $query;
    }

    public function getSubclassMst($model)
    {
        $query  = " SELECT ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "   AND CLASSCD = '{$model->field["CLASSCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "   CLASSCD ";
        return $query;
    }
}
