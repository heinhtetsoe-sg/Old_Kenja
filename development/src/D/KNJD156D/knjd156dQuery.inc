<?php

require_once('for_php7.php');


class knjd156dQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期日付取得
    function getSemesterDate($field, $semester)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";

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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($year, $semester, $model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["DISP"] == 1) {
            $query .= " AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        }
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

    //対象生徒
    function getStudent($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
//        $query .= "     (CASE WHEN S1.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) || T2.SCHREGNO AS SCHREGNO_SHOW, ";
        $query .= "     T2.SCHREGNO AS SCHREGNO_SHOW, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_SEND_ADDRESS_DAT S1 ON S1.SCHREGNO = T2.SCHREGNO AND S1.DIV = '1' ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

}
?>