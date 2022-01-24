<?php

require_once('for_php7.php');

class knjb103dQuery extends Query
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

    //学年取得
    public function getGrade()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //考査取得
    public function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //教科取得
    public function getClass($model)
    {
        $query  = " WITH CLASSCD_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN SCH_CHR_TEST T2 ";
        $query .= "              ON T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "              ON T3.YEAR  = T1.YEAR ";
        $query .= "             AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR            = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER    = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T1.SCHOOL_KIND = 'H' ";
        $query .= "         AND T3.GRADE       = '".$model->field["GRADE"]."' ";
        $query .= "         AND T2.TESTKINDCD  = '".SUBSTR($model->field["TESTCD"], 0, 2)."' ";
        $query .= "         AND T2.TESTITEMCD  = '".SUBSTR($model->field["TESTCD"], 2, 2)."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CD.CLASSCD AS VALUE, ";
        $query .= "     MST.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASSCD_DAT CD ";
        $query .= "     LEFT JOIN V_CLASS_MST MST ";
        $query .= "          ON MST.YEAR        = CD.YEAR ";
        $query .= "         AND MST.CLASSCD     = CD.CLASSCD ";
        $query .= "         AND MST.SCHOOL_KIND = CD.SCHOOL_KIND ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //科目取得
    public function getSubclass($model)
    {
        $query  = " WITH SUBCLASSCD_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CURRICULUM_CD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN SCH_CHR_TEST T2 ";
        $query .= "              ON T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "              ON T3.YEAR  = T1.YEAR ";
        $query .= "             AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR            = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER    = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T3.GRADE       = '".$model->field["GRADE"]."' ";
        $query .= "         AND T1.CLASSCD     = '".$model->field["CLASSCD"]."' ";
        $query .= "         AND T2.TESTKINDCD  = '".SUBSTR($model->field["TESTCD"], 0, 2)."' ";
        $query .= "         AND T2.TESTITEMCD  = '".SUBSTR($model->field["TESTCD"], 2, 2)."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CURRICULUM_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CD.CLASSCD || '-' || CD.SCHOOL_KIND || '-' || CD.CURRICULUM_CD || '-' || CD.SUBCLASSCD AS VALUE, ";
        $query .= "     MST.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASSCD_DAT CD ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST MST ";
        $query .= "          ON MST.YEAR        = CD.YEAR ";
        $query .= "         AND MST.CLASSCD     = CD.CLASSCD ";
        $query .= "         AND MST.SCHOOL_KIND = CD.SCHOOL_KIND ";
        $query .= "         AND MST.CURRICULUM_CD = CD.CURRICULUM_CD ";
        $query .= "         AND MST.SUBCLASSCD = CD.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座一覧
    public function getChairList($model)
    {
        $query .= " SELECT DISTINCT";
        $query .= "     T4.CHAIRCD AS VALUE, ";
        $query .= "     T4.CHAIRCD || ':' || T4.CHAIRNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "          ON T2.YEAR  = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T2.GRADE = '".$model->field["GRADE"]."'";
        $query .= "     INNER JOIN SCH_CHR_TEST T3 ";
        $query .= "          ON T3.CHAIRCD  = T1.CHAIRCD ";
        $query .= "         AND T3.TESTKINDCD = '".SUBSTR($model->field["TESTCD"], 0, 2)."' ";
        $query .= "         AND T3.TESTITEMCD = '".SUBSTR($model->field["TESTCD"], 2, 2)."' ";
        $query .= "     INNER JOIN CHAIR_DAT T4 ";
        $query .= "          ON T4.YEAR  = T1.YEAR ";
        $query .= "         AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T4.CHAIRCD = T1.CHAIRCD ";
        $query .= "         AND T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER    = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
