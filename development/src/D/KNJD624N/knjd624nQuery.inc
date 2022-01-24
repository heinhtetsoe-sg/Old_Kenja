<?php

require_once('for_php7.php');


class knjd624nQuery extends Query {

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
        $query  = " WITH CHAIRSTF AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
        $query .= "           AND T1.SEMESTER = CHAIR.SEMESTER ";
        $query .= "           AND T1.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($semester !== '9') {
            $query .= "     AND T1.SEMESTER = '".$semester."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.STAFFCD = '".STAFFCD."' ";
            $query .= "     OR CHAIR.CLASSCD IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006') ";
            $query .= "                  AND ST.FIELD1 = '1050') ";
            $query .= "         ) ";
        }
        $query .= " ), GRADE_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REG.GRADE ";
        $query .= " FROM ";
        $query .= "     CHAIRSTF T1, ";
        $query .= "     CHAIR_STD_DAT STD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REG ON STD.YEAR = REG.YEAR ";
        $query .= "           AND STD.SEMESTER = REG.SEMESTER ";
        $query .= "           AND STD.SCHREGNO = REG.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = STD.YEAR ";
        $query .= "     AND T1.SEMESTER = STD.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = STD.CHAIRCD ";
        $query .= " ) ";
        $query .= "  ";

        //参照・更新可
        $query .= " SELECT DISTINCT ";
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
            $query .= "     AND T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  GRADE_T.GRADE ";
            $query .= "              FROM ";
            $query .= "                  GRADE_T) ";
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
        $query  = " WITH CHAIRSTF AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
        $query .= "           AND T1.SEMESTER = CHAIR.SEMESTER ";
        $query .= "           AND T1.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($semester !== '9') {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.STAFFCD = '".STAFFCD."' ";
            $query .= "     OR CHAIR.CLASSCD IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006') ";
            $query .= "                  AND ST.FIELD1 = '1050') ";
            $query .= "         ) ";
        }
        $query .= " ), SUB_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD ";
        $query .= "      ";
        $query .= " FROM ";
        $query .= "     CHAIRSTF T1, ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = CHAIR.YEAR ";
        $query .= "     AND T1.SEMESTER = CHAIR.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " ), RECORD_SCORE AS ( ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REG ON T1.YEAR = REG.YEAR ";
        if ($semester !== '9') {
            $query .= "     AND REG.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= "     AND REG.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= "           AND REG.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "           AND T1.SCHREGNO = REG.SCHREGNO ";
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
        $query .= " , SUBCLASS_REPLACE_COMBINED AS ( ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.COMBINED_CLASSCD, T3.COMBINED_SCHOOL_KIND, T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T3.COMBINED_SUBCLASSCD ";
        $query .= " FROM SUBCLASS_REPLACE_COMBINED_DAT T3 ";
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
        $query .= "     SUB_T, ";
        $query .= "     RECORD_SCORE T1 ";
        $query .= " LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND  T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                     AND  T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                     AND  T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN SUBCLASS_REPLACE_COMBINED L1 ON L1.COMBINED_SUBCLASSCD = T2.SUBCLASSCD ";
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
        $query .= " WHERE ";
        $query .= "     SUB_T.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SUB_T.CLASSCD       = T1.CLASSCD ";
            $query .= "     AND SUB_T.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "     AND SUB_T.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合併先科目
    function getSubclassReplaceCombinedCnt($model, $subclasscd, $div) {
        //合併科目
        $fieldName = "COMBINED";
        if ($div == "SAKI") $fieldName = "COMBINED";
        if ($div == "MOTO") $fieldName = "ATTEND";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        $query  = "SELECT COUNT(*) AS CNT ";
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  {$fieldName}_CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND  {$fieldName}_SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND  {$fieldName}_CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND  {$fieldName}_SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND  {$fieldName}_SUBCLASSCD     = '".$subclasscd."' ";
        }
        return $query;
    }
}
?>
