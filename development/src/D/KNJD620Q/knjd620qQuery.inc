<?php

require_once('for_php7.php');

class knjd620qQuery extends Query {

    //中学なのか高校なのか
    function getGdat($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     GRADE = '$grade' ";

        return $query;
    }

    //学期取得
    function getSemester($flg) {
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

    //年度開始日取得
    function getYearSdate() {
        $query  = " SELECT ";
        $query .= "     T1.SDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '9' ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($semester, $model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('J','H') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     S1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_HDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR     = T1.YEAR AND ";
            $query .= "                     S1.GRADE    = T1.GRADE AND ";
            $query .= "                     S1.SEMESTER = '".$semester."' AND ";
            $query .= "                     '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    function getTest($model, $semester, $grade) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ' ' || L1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        $query .= "                                                AND T1.GRADE = L1.GRADE ";
        $query .= "                                                AND T1.COURSECD = L1.COURSECD ";
        $query .= "                                                AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR   = T1.YEAR ";
        $query .= "                                   AND L2.GRADE  = '".$grade."' ";
        $query .= "                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$semester."' ";
        $query .= "     AND T1.CLASSCD      = '00' ";
        $query .= "     AND T1.CURRICULUM_CD  = '00' ";
        $query .= "     AND T1.SUBCLASSCD  = '000000' ";
        $query .= "     AND T1.GRADE = '00' ";
        $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
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
