<?php

require_once('for_php7.php');

class knjd682Query extends Query {
    //学期取得
    function getSemester() {
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

    //学年／年組取得
    function getGradeHrClass($model, $div) {
        $query  = " SELECT DISTINCT ";
        if ($div == "grade") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.SCHOOL_KIND = 'H' ";
        if ($div == "hr_class") {
            $query .= " AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   ('".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3)) ";
            $query .= "      OR ";
            $query .= "         (T1.GRADE IN (  SELECT ";
            $query .= "                             S1.FIELD2 ";
            $query .= "                         FROM ";
            $query .= "                             STAFF_DETAIL_MST S1 ";
            $query .= "                         WHERE ";
            $query .= "                             T1.YEAR     = S1.YEAR AND ";
            $query .= "                             S1.STAFFCD  = '".STAFFCD."' AND ";
            $query .= "                             S1.STAFF_SEQ IN ('005', '006', '007') AND ";
            $query .= "                             S1.FIELD1   = '0200')) ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
