<?php

require_once('for_php7.php');

class knjd231dQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        }
        $query .= "     T1.GRADE    = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    function getTest($model) {
        $grade = $hr_class = array();
        if ($model->field["GRADE_HR_CLASS"]) list ($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ";
        $query .= "          ON T2.YEAR             = T1.YEAR ";
		$query .= "         AND T2.SEMESTER         = T1.SEMESTER ";
		$query .= "         AND T2.TESTKINDCD       = T1.TESTKINDCD ";
		$query .= "         AND T2.TESTITEMCD       = T1.TESTITEMCD ";
		$query .= "         AND T2.SCORE_DIV        = T1.SCORE_DIV ";
        $query .= "         AND T2.CLASSCD          = '00' ";
        $query .= "         AND T2.CURRICULUM_CD    = '00' ";
        $query .= "         AND T2.SUBCLASSCD       = '000000' ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "          ON T3.YEAR         = T1.YEAR ";
		$query .= "         AND T3.GRADE        = '".$grade."' ";
		$query .= "         AND T3.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.SCORE_DIV    = '08' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //評定マスタ
    function getAssessHigh() {
        $query  = " SELECT ";
        $query .= "     INT(ASSESSHIGH) AS ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     ASSESSCD    = '3' AND ";
        $query .= "     ASSESSLEVEL = 1 ";

        return $query;
    }

    //データ一覧取得
    function selectQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S1.GRADE, ";
        $query .= "     S1.HR_CLASS, ";
        $query .= "     S1.ATTENDNO, ";
        $query .= "     S2.NAME_SHOW, ";
        $query .= "     S2.NAME, ";
        $query .= "     G1.GUARD_NAME, ";
        $query .= "     L1.SEMESTERNAME, ";
        $query .= "     L2.TESTITEMNAME, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L3.SUBCLASSNAME, ";
        $query .= "     T1.SCORE, ";
        $query .= "     L4.CREDITS ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT S1 ";
        $query .= "          ON T1.YEAR         = S1.YEAR ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     AND S1.SEMESTER     = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER     = S1.SEMESTER ";
        }
        $query .= "         AND T1.SCHREGNO     = S1.SCHREGNO ";
        $query .= "         AND S1.GRADE || '-' || S1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     INNER JOIN SCHREG_BASE_MST S2 ON S2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT G1 ";
        $query .= "          ON T1.SCHREGNO     = G1.SCHREGNO ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ";
        $query .= "          ON T1.YEAR         = L1.YEAR ";
        $query .= "         AND T1.SEMESTER     = L1.SEMESTER ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L2 ";
        $query .= "          ON T1.YEAR         = L2.YEAR ";
        $query .= "         AND T1.SEMESTER     = L2.SEMESTER ";
        $query .= "         AND T1.TESTKINDCD   = L2.TESTKINDCD ";
        $query .= "         AND T1.TESTITEMCD   = L2.TESTITEMCD ";
        $query .= "         AND T1.SCORE_DIV    = L2.SCORE_DIV ";
        $query .= "     LEFT JOIN SUBCLASS_MST L3 ";
        $query .= "          ON T1.CLASSCD          = L3.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = L3.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = L3.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = L3.SUBCLASSCD ";
        $query .= "     LEFT JOIN CREDIT_MST L4 ";
        $query .= "          ON T1.YEAR             = L4.YEAR ";
        $query .= "         AND S1.COURSECD         = L4.COURSECD ";
        $query .= "         AND S1.MAJORCD          = L4.MAJORCD ";
        $query .= "         AND S1.GRADE            = L4.GRADE ";
        $query .= "         AND S1.COURSECODE       = L4.COURSECODE ";
        $query .= "         AND T1.CLASSCD          = L4.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = L4.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = L4.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = L4.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' AND ";
        $query .= "     T1.SCORE   <= ".$model->field["KETTEN"]." ";
        $query .= " ORDER BY ";
        $query .= "     S1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }
}
?>
