<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjg052Query.inc 56587 2017-10-22 12:54:51Z maeshiro $

class knjg052Query extends Query {

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "     T2.NAMECD1  = 'A023' AND ";
        $query .= "     T1.GRADE BETWEEN T2.NAMESPARE2 AND T2.NAMESPARE3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getGradeClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["GAKKI"]."' AND ";
        $query .= "     T2.NAMECD1  = 'A023' AND ";
        $query .= "     T1.GRADE BETWEEN T2.NAMESPARE2 AND T2.NAMESPARE3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（リストtoリスト）
    function getGradeClassList($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["GAKKI"]."' AND ";
        $query .= "     GRADE       = '".$model->field["CMBCLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得（リストtoリスト）
    function getStudentList($model) {
        $query  = " SELECT ";
        $query .= "     T1.ATTENDNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["GAKKI"]."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["CMBCLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
