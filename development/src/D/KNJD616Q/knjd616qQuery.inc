<?php

require_once('for_php7.php');


class knjd616qQuery extends Query {

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
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
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
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
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

    //試験科目取得
    function getTestSubclass($model)
    {
        $query  = " WITH RECORD_SCORE AS ( ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND T2.YEAR ='".CTRL_YEAR."'";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= " AND T2.SEMESTER='".CTRL_SEMESTER ."'";
        } else {
            $query .= " AND T2.SEMESTER='".$model->field["SEMESTER"]."'";
        }
        $query .= " AND T2.GRADE = '".$model->field["GRADE"]."'";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T2.COURSECD || '-' || T2.MAJORCD ='".$model->field["MAJOR"]."'";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";
        $query .= " , CHAIR_SUBCLASS AS ( ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD ";
        $query .= " FROM CHAIR_DAT T3 ";
        $query .= " INNER JOIN CHAIR_CLS_DAT T4 ON T4.YEAR  = T3.YEAR";
        $query .= "                            AND T4.SEMESTER = T3.SEMESTER";
        $query .= "                            AND ((T4.GROUPCD = '0000' AND T4.CHAIRCD = T3.CHAIRCD) OR ";
        $query .= "                                 (T4.GROUPCD <> '0000' AND T4.GROUPCD = T3.GROUPCD))   ";
        $query .= " WHERE ";
        $query .= "     T3.YEAR           = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] !== '9') {
            $query .= "     AND T3.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= "     AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= " ) ";
        $query .= " , SUBCLASS_WEIGHTING_COURSE AS ( ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.COMBINED_CLASSCD, T3.COMBINED_SCHOOL_KIND, T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T3.COMBINED_SUBCLASSCD ";
        $query .= " FROM SUBCLASS_WEIGHTING_COURSE_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T3.YEAR           = '".CTRL_YEAR."' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.COMBINED_CLASSCD, ";
            $query .= "     L1.COMBINED_SCHOOL_KIND, ";
            $query .= "     L1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     L1.COMBINED_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T2.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || ':' || T2.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     RECORD_SCORE T1 ";
        $query .= " LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND  T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                     AND  T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                     AND  T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN SUBCLASS_WEIGHTING_COURSE L1 ON L1.COMBINED_SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND  L1.COMBINED_CLASSCD       = T2.CLASSCD ";
            $query .= "                     AND  L1.COMBINED_SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .= "                     AND  L1.COMBINED_CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= " INNER JOIN CHAIR_SUBCLASS T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND  T3.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND  T3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND  T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "  LEFT JOIN SUBCLASS_DETAIL_DAT T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T4.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                    AND T4.YEAR = '".CTRL_YEAR."' ";
        $query .= "                    AND T4.SUBCLASS_SEQ      = '012' ";
        $query .= " WHERE ";
        $query .= "    T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model)
    {
        //参照・更新可
        $query  = "SELECT DISTINCT T1.GRADE || T1.HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= " AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " AND T2.GRADE = T1.GRADE ";
        $query .= " AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "INNER JOIN RECORD_SCORE_DAT T3 ON T3.YEAR = T1.YEAR AND ";
        $query .= "     T3.SCHREGNO       = T2.SCHREGNO ";
        $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."'";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "AND T1.SEMESTER='".CTRL_SEMESTER ."'";
        } else {
            $query .= "AND T1.SEMESTER='".$model->field["SEMESTER"]."'";
        }
        $query .= "AND T1.GRADE ='".$model->field["GRADE"]."'";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T2.COURSECD || '-' || T2.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
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
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
        return $query;
    }
}
?>
