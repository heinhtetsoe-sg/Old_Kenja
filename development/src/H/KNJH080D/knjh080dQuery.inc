<?php

require_once('for_php7.php');


class knjh080dQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = " SELECT ";
            $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒一覧
    function getStudentSql($model) {

        $query  = " SELECT ";
        $query .= "     BASE.SCHREGNO AS SCHREGNO, ";
        $query .= "     REGD.SCHREGNO || '　' || REGD.ATTENDNO || '番' || '　' || BASE.NAME AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '" .$model->field["GRADE_HR_CLASS"] ."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //名称マスタ(データ種別)
    function getNameMstSql($nameid) {
        $query  = " SELECT ";
        $query .= "   NAMECD2 AS VALUE, ";
        $query .= "   NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = '".$nameid."' ";
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";

        return $query;
    }
    
    //実力科目一覧
    function getProfSubclassMst($model)
    {
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L1.GROUP_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= "          AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ), GROUP_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= "     LEFT JOIN REGD_T L1 ON L1.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.PROFICIENCYDIV = '{$model->field["PROFICIENCY_TYPE"]}' ";
        $query .= "     AND T1.GRADE = CASE WHEN DIV = '01' ";
        $query .= "                         THEN '00' ";
        $query .= "                         ELSE L1.GRADE ";
        $query .= "                    END ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "                                                           THEN '00000000' ";
        $query .= "                                                           ELSE CASE WHEN T1.DIV = '04' ";
        $query .= "                                                                     THEN '0' || L1.GROUP_CD || '0000' ";
        $query .= "                                                                     ELSE L1.COURSECD || L1.MAJORCD || L1.COURSECODE ";
        $query .= "                                                                END ";
        $query .= "                                                      END ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD || ':' || T1.SUBCLASS_ABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD IN (SELECT I1.PROFICIENCY_SUBCLASS_CD FROM GROUP_T I1) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "         AND T1.SCHOOL_KIND IN (SELECT ";
            $query .= "                                    SCHOOL_KIND ";
            $query .= "                                FROM ";
            $query .= "                                    SCHREG_REGD_GDAT ";
            $query .= "                                WHERE ";
            $query .= "                                        YEAR  = '".CTRL_YEAR."' ";
            $query .= "                                    AND GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
            $query .= "                                ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
