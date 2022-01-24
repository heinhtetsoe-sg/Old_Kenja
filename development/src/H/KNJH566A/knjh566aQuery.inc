<?php

require_once('for_php7.php');

class knjh566aQuery extends Query {

    //学期
    function getSemesterMst() {

        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model) {

        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getStudent($model) {

        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.SCHREGNO || '　' || T2.ATTENDNO || '番　' || T1.NAME_SHOW AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
}
?>
