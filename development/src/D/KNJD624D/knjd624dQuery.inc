<?php

require_once('for_php7.php');


class knjd624dQuery extends Query {

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
        $query  .= " WITH RECORD_SCORE AS ( ";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.YEAR ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND T2.YEAR ='".CTRL_YEAR."'";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= " AND T2.SEMESTER='".CTRL_SEMESTER ."'";
        } else {
            $query .= " AND T2.SEMESTER='".$model->field["SEMESTER"]."'";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T2.COURSECD || '-' || T2.MAJORCD ='".$model->field["MAJOR"]."'";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER       = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.TESTKINDCD     = '".substr($model->field["TESTCD"], 0, 2)."' AND ";
        $query .= "     T1.TESTITEMCD     = '".substr($model->field["TESTCD"], 2, 2)."' AND ";
        $query .= "     T1.SCORE_DIV      = '".substr($model->field["TESTCD"], 4, 2)."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, T1.YEAR ";
        $query .= " ) ";
        $query .= " , CHAIR_SUBCLASS AS ( ";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T4.TRGTGRADE AS GRADE ";
        $query .= " FROM CHAIR_DAT T3 ";
        $query .= " INNER JOIN CHAIR_CLS_DAT T4 ON T4.YEAR  = T3.YEAR";
        $query .= "                            AND T4.SEMESTER = T3.SEMESTER";
        $query .= "                            AND ((T4.GROUPCD = '0000' AND T4.CHAIRCD = T3.CHAIRCD) OR ";
        $query .= "                                 (T4.GROUPCD <> '0000' AND T4.GROUPCD = T3.GROUPCD))   ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " INNER JOIN CHAIR_STF_DAT TSTF ON TSTF.YEAR  = T3.YEAR";
            $query .= "     AND TSTF.SEMESTER = T3.SEMESTER";
            $query .= "     AND TSTF.CHAIRCD = T3.CHAIRCD ";
            $query .= "     AND TSTF.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " WHERE ";
        $query .= "     T3.YEAR           = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] !== '9') {
            $query .= "     AND T3.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= "     AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, T4.TRGTGRADE ";
        $query .= " ) ";
        $query .= " , SUBCLASS_WEIGHTING_COURSE AS ( ";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.COMBINED_CLASSCD, T3.COMBINED_SCHOOL_KIND, T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T3.COMBINED_SUBCLASSCD ";
        $query .= " FROM SUBCLASS_WEIGHTING_COURSE_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T3.YEAR           = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.COMBINED_CLASSCD, T3.COMBINED_SCHOOL_KIND, T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T3.COMBINED_SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T3.GRADE AS VALUE, ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE T1 ";
        $query .= " LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND  T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                     AND  T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                     AND  T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " INNER JOIN CHAIR_SUBCLASS T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND  T3.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND  T3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND  T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "                    AND  GDAT.GRADE = T3.GRADE ";
        $query .= "  LEFT JOIN SUBCLASS_DETAIL_DAT T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T4.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                    AND T4.YEAR = '".CTRL_YEAR."' ";
        $query .= "                    AND T4.SUBCLASS_SEQ      = '012' ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR IS NOT NULL ";
        if ($model->field["SUBSEL"] == '1' || $model->field["SUBSEL"] == '2' || $model->field["SUBSEL"] == '3') {
            $query .= "  AND ((T4.SUBCLASS_REMARK".$model->field["SUBSEL"]." = '1' ";
            if ($model->field["SUBSEL"] == $model->field["SEMESTERDIV"]) {
                $query .= "     OR  T4.SUBCLASS_REMARK1 IS NULL AND T4.SUBCLASS_REMARK2 IS NULL AND T4.SUBCLASS_REMARK3 IS NULL ";
            }
            $query .= "   ) ";
            if ($model->field["SUBSEL"] == $model->field["SEMESTERDIV"]) {
                $query .= "     OR  T4.SUBCLASSCD IS NULL ";
            }
            $query .= " )";
        }
        $query .= " GROUP BY ";
        $query .= "     T3.GRADE , ";
        $query .= "     GDAT.GRADE_NAME1 ";
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
    function getTest($model, $semester)
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
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T11.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T11.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.GRADE = '00' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
            if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND T11.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //試験科目取得
    function getTestSubclass($model)
    {
        $query  = " WITH RECORD_SCORE AS ( ";
        $query .= " SELECT ";
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
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER       = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.TESTKINDCD     = '".substr($model->field["TESTCD"], 0, 2)."' AND ";
        $query .= "     T1.TESTITEMCD     = '".substr($model->field["TESTCD"], 2, 2)."' AND ";
        $query .= "     T1.SCORE_DIV      = '".substr($model->field["TESTCD"], 4, 2)."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " , CHAIR_SUBCLASS AS ( ";
        $query .= " SELECT ";
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
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " INNER JOIN CHAIR_STF_DAT TSTF ON TSTF.YEAR  = T3.YEAR";
            $query .= "     AND TSTF.SEMESTER = T3.SEMESTER";
            $query .= "     AND TSTF.CHAIRCD = T3.CHAIRCD ";
            $query .= "     AND TSTF.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " WHERE ";
        $query .= "     T3.YEAR           = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] !== '9') {
            $query .= "     AND T3.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= "     AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " , SUBCLASS_WEIGHTING_COURSE AS ( ";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.COMBINED_CLASSCD, T3.COMBINED_SCHOOL_KIND, T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T3.COMBINED_SUBCLASSCD ";
        $query .= " FROM SUBCLASS_WEIGHTING_COURSE_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T3.YEAR           = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.COMBINED_CLASSCD, T3.COMBINED_SCHOOL_KIND, T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T3.COMBINED_SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT ";
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
        if ($model->field["SUBSEL"] == '1' || $model->field["SUBSEL"] == '2' || $model->field["SUBSEL"] == '3') {
            $query .= "  AND ((T4.SUBCLASS_REMARK".$model->field["SUBSEL"]." = '1' ";
            if ($model->field["SUBSEL"] == $model->field["SEMESTERDIV"]) {
                $query .= "     OR  T4.SUBCLASS_REMARK1 IS NULL AND T4.SUBCLASS_REMARK2 IS NULL AND T4.SUBCLASS_REMARK3 IS NULL ";
            }
            $query .= "   ) ";
            if ($model->field["SUBSEL"] == $model->field["SEMESTERDIV"]) {
                $query .= "     OR  T4.SUBCLASSCD IS NULL ";
            }
            $query .= " )";
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.COMBINED_CLASSCD, ";
            $query .= "     L1.COMBINED_SCHOOL_KIND, ";
            $query .= "     L1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     L1.COMBINED_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD , T1.SCHOOL_KIND , T1.CURRICULUM_CD , T1.SUBCLASSCD , T2.SUBCLASSNAME ";
        } else {
            $query .= "     T1.SUBCLASSCD , T2.SUBCLASSNAME ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD , T1.SCHOOL_KIND , T1.CURRICULUM_CD , T1.SUBCLASSCD ";
        } else {
            $query .= "     T1.SUBCLASSCD ";
        }
        
        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model)
    {
        //参照・更新可
        $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= " AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " AND T2.GRADE = T1.GRADE ";
        $query .= " AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "INNER JOIN RECORD_SCORE_DAT T3 ON T3.YEAR = T1.YEAR AND ";
        $query .= "     T3.SEMESTER       = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T3.TESTKINDCD     = '".substr($model->field["TESTCD"], 0, 2)."' AND ";
        $query .= "     T3.TESTITEMCD     = '".substr($model->field["TESTCD"], 2, 2)."' AND ";
        $query .= "     T3.SCORE_DIV      = '".substr($model->field["TESTCD"], 4, 2)."' AND ";
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
            $query .= " OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T2.COURSECD || '-' || T2.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " GROUP BY T1.GRADE, T1.HR_CLASS, HR_NAME ";
        $query .= " ORDER BY T1.GRADE || T1.HR_CLASS ";

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
