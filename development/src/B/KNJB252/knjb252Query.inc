<?php

require_once('for_php7.php');

class knjb252Query extends Query {
    //学期取得
    function getSemester($semester="") {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE, ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($semester) {
            $query .= "     SEMESTER = '".$semester."' ";
        } else {
            $query .= "     SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND ";
            $query .= "    (TR_CD1      = '".STAFFCD."' OR ";
            $query .= "     TR_CD2      = '".STAFFCD."' OR ";
            $query .= "     TR_CD3      = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得
    function getStudentList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>