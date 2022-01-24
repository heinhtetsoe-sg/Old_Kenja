<?php

require_once('for_php7.php');


class knje370aQuery extends Query {
    //年度取得
    function getYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR AS VALUE, ";
        $query .= "     T1.YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT T2 ON  T1.YEAR     = T2.YEAR ";
        $query .= "                         AND T1.GRADE    = T2.GRADE ";
        $query .= "                         AND T2.SCHOOL_KIND IN ('H') ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //MAX学期取得
    function getMaxSemester($model) {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //既卒生有無チェック
    function checkGradCnt($model, $semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(T1.SCHREGNO) ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT E1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR     = E1.YEAR AND ";
        $query .= "                     E1.SEMESTER = '".$semester."' AND ";
        $query .= "                     T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                 ) ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $semester, $kisotsu) {
        $query .= " SELECT ";
        $query .= "     '既卒生' AS LABEL, ";
        $query .= "     'ZZZZZ' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudent($model, $semester) {
        $query  = " SELECT ";
        $query .= "     '0-' || REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD_H.HR_NAME || '　' || REGD.ATTENDNO || '番　' || BASE.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = REGD.YEAR ";
        $query .= "           AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND REGD_H.GRADE    = REGD.GRADE ";
        $query .= "           AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "     AND REGD.SEMESTER = '".$semester."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->field["GRADE_HR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //既卒生取得
    function getGradStudent($model, $semester) {
        $senkou_kind = ($model->field["OUT_DIV"] == "1") ? "0" : "1";

        $query  = " SELECT DISTINCT ";
        $query .= "     '1-ZZZZZZZZ-' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     INNER JOIN GRD_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SENKOU_KIND  = '".$senkou_kind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudentLeft($model, $semester) {
        $senkou_kind = ($model->field["OUT_DIV"] == "1") ? "0" : "1";

        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         '0-' || REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "         REGD_H.HR_NAME || '　' || REGD.ATTENDNO || '番　' || BASE.NAME_SHOW AS LABEL, ";
        $query .= "         REGD.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = REGD.YEAR ";
        $query .= "               AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "               AND REGD_H.GRADE    = REGD.GRADE ";
        $query .= "               AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "         AND REGD.SEMESTER = '".$semester."' ";
        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1-ZZZZZZZZ-' || T1.SCHREGNO AS VALUE, ";
        $query .= "         T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SENKOU_KIND  = '".$senkou_kind."' AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT E1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = E1.YEAR AND ";
        $query .= "                         E1.SEMESTER = '".$semester."' AND ";
        $query .= "                         T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                     ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO IN ('{$model->selectdata}') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>