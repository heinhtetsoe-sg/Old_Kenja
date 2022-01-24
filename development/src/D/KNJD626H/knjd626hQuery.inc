<?php

require_once('for_php7.php');

class knjd626hQuery extends Query
{
    //学期取得
    public function getSemester()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getSubclass($model)
    {
        $query .= " SELECT ";
        $query .= "     MST.CLASSCD || '-' || MST.SCHOOL_KIND || '-' || MST.CURRICULUM_CD || '-' || MST.SUBCLASSCD AS VALUE, ";
        $query .= "     MST.CLASSCD || '-' || MST.SCHOOL_KIND || '-' || MST.CURRICULUM_CD || '-' || MST.SUBCLASSCD || ':' || MST.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "    V_SUBCLASS_MST MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = 'H' "; //高校固定
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座一覧
    public function getChairList($model)
    {
        $query .= " SELECT DISTINCT";
        $query .= "     T2.CHAIRCD AS VALUE, ";
        $query .= "     T2.CHAIRCD || ':' || T2.CHAIRNAME || ' ' || REPLACE(T1.APPDATE, '-', '/') || '～' || REPLACE(T1.APPENDDATE, '-', '/')";
        $query .= "     || ' ' || T4.STAFFNAME_SHOW AS LABEL";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ";
        $query .= "          ON T2.YEAR  = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "         AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T3 ";
        $query .= "          ON T3.YEAR  = T2.YEAR ";
        $query .= "         AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T3.CHAIRCD = T2.CHAIRCD ";
        $query .= "         AND T3.CHARGEDIV = '1' "; //正担任
        $query .= "     INNER JOIN STAFF_MST T4 ";
        $query .= "          ON T4.STAFFCD  = T3.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER    = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
