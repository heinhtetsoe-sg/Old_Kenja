<?php

require_once('for_php7.php');


class knjd624aQuery extends Query {

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
        if ($div == "GRADE") {
            $query .= "     AND T1.GRADE < '12' ";
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
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

    //テスト取得
    function getTest($semester)
    {

        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER       = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.TESTKINDCD     = '".substr($model->field["TESTCD"], 0, 2)."' AND ";
        $query .= "     T1.TESTITEMCD     = '".substr($model->field["TESTCD"], 2, 2)."' ";
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
            $query .= "       				AND  T2.CLASSCD       = T1.CLASSCD ";
            $query .= "       				AND  T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "       				AND  T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN SUBCLASS_WEIGHTING_COURSE L1 ON L1.COMBINED_SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       				AND  L1.COMBINED_CLASSCD       = T2.CLASSCD ";
            $query .= "       				AND  L1.COMBINED_SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .= "       				AND  L1.COMBINED_CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= " INNER JOIN CHAIR_SUBCLASS T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       			   AND  T3.CLASSCD       = T1.CLASSCD ";
            $query .= "       			   AND  T3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "       			   AND  T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
}
?>
