<?php
class knjd619eQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "            AND T1.GRADE = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //考査種別取得
    function getTestItem($model)
    {
        $query .= " SELECT ";
        $query .= "     SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER  = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCORE_DIV = '08' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV出力
    function getCsvQuery($model)
    {
        $query .= " SELECT ";
        $query .= "   T2.GRADE, ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   L1.NAME, ";
        $query .= "   T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "   L2.SUBCLASSNAME, ";
        $query .= "   T1.SCORE ";
        $query .= " FROM ";
        $query .= "   RECORD_SCORE_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "           ON T2.YEAR     = T1.YEAR ";
        $query .= "          AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T2.SCHREGNO = T1.SCHREGNO ";
        if($model->field["GRADE"] != "ALL"){
            $query .= "          AND T2.GRADE    = '".$model->field["GRADE"]."' ";
        }
        $query .= "   INNER JOIN STANDARD_SCORE_DAT T3 ";
        $query .= "          ON T3.YEAR           = T1.YEAR ";
        $query .= "         AND T3.SEMESTER       = T1.SEMESTER ";
        $query .= "         AND T3.TESTKINDCD     = T1.TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD     = T1.TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV      = T1.SCORE_DIV ";
        $query .= "         AND T3.CLASSCD        = T1.CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND    = T1.SCHOOL_KIND ";
        $query .= "         AND T3.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        $query .= "         AND T3.SUBCLASSCD     = T1.SUBCLASSCD ";
        $query .= "         AND T3.GRADE          = T2.GRADE ";
        $query .= "         AND T3.STANDARD_SCORE > T1.SCORE ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "           ON L1.SCHREGNO = T2.SCHREGNO ";
        $query .= "    LEFT JOIN SUBCLASS_MST L2 ";
        $query .= "           ON L2.CLASSCD       = T1.CLASSCD ";
        $query .= "          AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "          AND L2.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTITEMCD || T1.TESTKINDCD || T1.SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }
}
?>
