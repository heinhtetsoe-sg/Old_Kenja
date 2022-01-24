<?php

require_once('for_php7.php');


class knjh562Query extends Query {

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getDataDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getProName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1";
        $query .= " LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV = T2.PROFICIENCYDIV";
        $query .= "                             AND T1.PROFICIENCYCD  = T2.PROFICIENCYCD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' AND ";
        $query .= "     T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //試験科目取得
    function getTestSubclass($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     CASE WHEN T3.ATTEND_SUBCLASSCD IS NULL THEN '●' ELSE '　' END ";
        $query .= "       || T1.PROFICIENCY_SUBCLASS_CD || ':' || T2.SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT T1 ";
        $query .= " LEFT JOIN PROFICIENCY_SUBCLASS_MST T2 ON T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " LEFT JOIN  ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             W2.ATTEND_SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT W2 ";
        $query .= "         WHERE ";
        $query .= "                 W2.YEAR           = '".CTRL_YEAR."' ";
        $query .= "             AND W2.SEMESTER       = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND W2.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "             AND W2.PROFICIENCYCD  = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "             AND W2.GRADE          = '".$model->field["GRADE"]."' ";
        $query .= "         GROUP BY ";
        $query .= "             W2.ATTEND_SUBCLASSCD ";
        $query .= "     ) T3 ON T3.ATTEND_SUBCLASSCD = T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER       = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' AND ";
        $query .= "     T1.PROFICIENCYCD  = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "     AND T1.SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             W1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT W1 ";
        $query .= "         WHERE ";
        $query .= "                 W1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "             AND W1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND W1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
    
        return $query;
    }
}
?>
