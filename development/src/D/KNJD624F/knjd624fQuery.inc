<?php

require_once('for_php7.php');

class knjd624fQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model, $div = "")  {
        if ($semester == "9") {
            $semester = CTRL_SEMESTER;
        }

        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                                  AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($div == "GRADE") {
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= "     AND EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT T3 ";
        $query .= "                 WHERE ";
        $query .= "                     T3.YEAR = T1.YEAR ";
        $query .= "                     AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "                     AND T3.GRADE = T1.GRADE ";
        $query .= "                     AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "                 ) ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
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
    function getTest($model) {
        $testCdIn = $sep = "";
        foreach ($model->testCd as $key => $testCd) {
            $testCdIn .= $sep.$testCd;
            $sep = "', '";
        }

        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ";
        $query .= "    AND T11.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T11.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T11.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T11.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "    AND T11.CLASSCD      = '00' ";
        $query .= "    AND T11.CURRICULUM_CD  = '00' ";
        $query .= "    AND T11.SUBCLASSCD  = '000000' ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T11.SCHOOL_KIND = '".$model->schoolKind."' ";
        //$query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ('{$testCdIn}') ";
        $query .= "     AND T1.SCORE_DIV IN ('01') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
