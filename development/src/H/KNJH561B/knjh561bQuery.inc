<?php

require_once('for_php7.php');


class knjh561bQuery extends Query {

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
        $query .= "     T1.PROFICIENCYCD || ':' || PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     INNER JOIN PROFICIENCY_YMST T2 ON T2.YEAR = '".CTRL_YEAR."'";
        $query .= "         AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ";
        $query .= "         AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        
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
        $query .= " LEFT JOIN PROFICIENCY_YMST T3 ON T3.YEAR  = T1.YEAR";
        $query .= "                              AND T3.SEMESTER = '".$semester."' ";
        $query .= "                              AND T3.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "                              AND T3.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T3.GRADE = T1.GRADE ";
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

    //年組取得（権限チェック）
    function getHrClass($model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS AS VALUE, ";
        $query .= "     REGD.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
                $query .= "           AND REGD.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
            $query .= "           AND REGD.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND REGD.GRADE = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   REGD.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR REGD.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR REGD.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得（権限チェック）
    function getCourse($model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || '　' || S1.COURSENAME || S2.MAJORNAME || S3.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_MST S1 ON T1.COURSECD = S1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST S2 ON T1.COURSECD = S2.COURSECD AND T1.MAJORCD = S2.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST S3 ON T1.COURSECODE = S3.COURSECODE, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T2.YEAR = REG_G.YEAR ";
                $query .= "           AND T2.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T2.YEAR = REG_G.YEAR ";
            $query .= "           AND T2.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T1.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.GRADE = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
