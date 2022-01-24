<?php

require_once('for_php7.php');

class knjd626cQuery extends Query {

    //学期取得
    function getSemester() {
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
    function getGrade($semester, $model)  {
        if ($semester == "9") {
            $semester = CTRL_SEMESTER;
        }

        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T2.SCHOOL_KIND = 'H' ";
        // if ($model->Properties["use_prg_schoolkind"] == "1") {
        //     if ($model->selectSchoolKind) {
        //         $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
        //     }
        // } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
        //     $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        // }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    function getTest($semester, $grade) {

        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
		$query .= "         AND T2.SEMESTER = T1.SEMESTER ";
		$query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
		$query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
		$query .= "         AND T2.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "         AND T2.CLASSCD      = '00' ";
        $query .= "         AND T2.CURRICULUM_CD  = '00' ";
        $query .= "         AND T2.SUBCLASSCD  = '000000' ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR ";
		$query .= "         AND T3.GRADE = '".$grade."' ";
		$query .= "         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.SCORE_DIV = '08'";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
