<?php

require_once('for_php7.php');

class knjd616fQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";  //9学期のみ
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度開始日付
    function getSdate($check) {
        $query  = " SELECT ";
        if($check == sdate){
            $query .= "     SDATE ";
        } else if($check == edate){
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    //学期取得
    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "   NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "   NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   VALUE ";
        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        // if ($model->Properties["use_prg_schoolkind"] == "1") {
        //     if ($model->selectSchoolKind) {
        //         $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
        //     }
        // } else if ($model->Properties["useSchool_KindField"] == "1") {
        //     $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        // }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T1.YEAR ";
        $query .= "         AND G1.GRADE        = T1.GRADE ";
        // if ($model->Properties["use_prg_schoolkind"] == "1") {
        //     if ($model->selectSchoolKind) {
        //         $query .= "     AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
        //     }
        // } else if ($model->Properties["useSchool_KindField"] == "1") {
        //     $query .= "     AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        // }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->setSemester."' ";
        // $query .= "     AND G1.SCHOOL_KIND = 'P' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "  AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    // //生徒取得
    // function getSchList($model) {
    //     $query  = " SELECT ";
    //     $query .= "     T1.SCHREGNO, ";
    //     $query .= "     T1.GRADE, ";
    //     $query .= "     T1.HR_CLASS, ";
    //     $query .= "     T1.ATTENDNO, ";
    //     $query .= "     T2.NAME_SHOW, ";
    //     $query .= "     T3.HR_NAME, ";
    //     $query .= "     T1.SCHREGNO || '-' || T1.GRADE || T1.HR_CLASS || T1.ATTENDNO AS VALUE ";
    //     $query .= " FROM ";
    //     $query .= "     SCHREG_REGD_DAT T1 ";
    //     $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    //     $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
    //     $query .= "          ON T3.YEAR     = T1.YEAR ";
    //     $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
    //     $query .= "         AND T3.GRADE    = T1.GRADE ";
    //     $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
    //     $query .= " WHERE ";
    //     $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
    //     $query .= "     T1.SEMESTER = '".$model->setSemester."' AND ";
    //     $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
    //     $query .= " ORDER BY ";
    //     $query .= "     T1.GRADE, ";
    //     $query .= "     T1.HR_CLASS, ";
    //     $query .= "     T1.ATTENDNO ";
    // 
    //     return $query;
    // }
}
?>