<?php

require_once('for_php7.php');

class knjd625bQuery extends Query {

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGrade($model)
    {
        //参照・更新可
        $query .= " SELECT ";
        $query .= "     HDAT.GRADE AS VALUE, ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR";
        $query .= "                              AND GDAT.GRADE = HDAT.GRADE";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " INNER JOIN SCHREG_REGD_DAT REGD ";
            $query .= "     ON REGD.YEAR = HDAT.YEAR ";
            $query .= "     AND REGD.SEMESTER = HDAT.SEMESTER ";
            $query .= "     AND REGD.GRADE = HDAT.GRADE ";
            $query .= "     AND REGD.HR_CLASS = HDAT.HR_CLASS ";
            $query .= "     AND REGD.COURSECD || '-' || REGD.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     HDAT.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] !== '9') {
            $query .= "     AND HDAT.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= "     AND HDAT.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= "     AND GDAT.SCHOOL_KIND <> 'P' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   HDAT.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR HDAT.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR HDAT.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR HDAT.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR HDAT.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR HDAT.SUBTR_CD3 = '".STAFFCD."' ";
            $query .= "          OR HDAT.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200') ";
            $query .= "          OR HDAT.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200') ";
            $query .= "         ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     HDAT.GRADE , ";
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
    function getTest($model)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.GRADE = '00' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
            if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        $query .= " AND T1.SCORE_DIV = '08' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    function getCourse($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     VALUE(MM.MAJORNAME, '') ||  VALUE(CCM.COURSECODENAME, '') AS LABEL, ";
        $query .= "     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ";
        $query .= "     INNER JOIN MAJOR_MST MM ON MM.COURSECD = REGD.COURSECD AND MM.MAJORCD = REGD.MAJORCD ";
        $query .= "     INNER JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND REGD.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
