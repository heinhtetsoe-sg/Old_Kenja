<?php

require_once('for_php7.php');

class knjd624qQuery extends Query {

    //学期取得
    function getSemester($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL ";
        if ($flg) {
            $query .= "     ,T2.SEMESTERDIV ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        if ($flg) {
            $query .= "     LEFT JOIN SCHOOL_MST T2 ON T2.YEAR = T1.YEAR ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "         AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                    $query .= "         AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "         AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "         AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($flg) {
            $query .= "     AND T1.SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model, $div = "")
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " INNER JOIN CHAIR_CLS_DAT T3    ON T3.YEAR  = T1.YEAR";
        $query .= "                              AND T3.SEMESTER = T1.SEMESTER";
        $query .= "                              AND T3.TRGTGRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($semester !== '9') {
            $query .= "     AND T1.SEMESTER = '".$semester."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= "     AND T2.SCHOOL_KIND <> 'P' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($div == "GRADE") {
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND EXISTS ( ";
            $query .= "     SELECT 'X' FROM SCHREG_REGD_DAT REGD ";
            $query .= "     WHERE REGD.YEAR = T1.YEAR ";
            $query .= "     AND REGD.SEMESTER = T1.SEMESTER ";
            $query .= "     AND REGD.GRADE = T1.GRADE ";
            $query .= "     AND REGD.HR_CLASS = T1.HR_CLASS ";
            $query .= "     AND REGD.COURSECD || '-' || REGD.MAJORCD = '".$model->field["MAJOR"]."' ";
            $query .= " ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 学校種別
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        return $query;
    }

    //テスト取得
    function getTest($model, $semester, $grade)
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
            $query .= " INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            $query .= " INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ";
        }
        $query .= "    AND T11.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T11.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T11.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T11.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "    AND T11.CLASSCD      = '00' ";
        $query .= "    AND T11.CURRICULUM_CD  = '00' ";
        $query .= "    AND T11.SUBCLASSCD  = '000000' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "    AND T11.GRADE = T1.GRADE ";
            $query .= "    AND T11.COURSECD = T1.COURSECD ";
            $query .= "    AND T11.MAJORCD = T1.MAJORCD ";
            $query .= "    AND T11.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "    AND T11.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " INNER JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "    AND L1.GRADE = '".$grade."' ";
        $query .= "    AND L1.SCHOOL_KIND = T11.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.GRADE = '00' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
                }
            } else if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCourseMajor($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     VALUE(NAME1, 'H') IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
            $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
        }
        return $query;
    }
}
?>
