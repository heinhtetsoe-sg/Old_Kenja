<?php

require_once('for_php7.php');

class knjg105Query extends Query {
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

    //年組取得（権限チェック）
    function getHrClass($model) {
        $tablename = ($model->field["HR_CLASS_TYPE"] == "2") ? "SCHREG_REGD_FI_HDAT" : "SCHREG_REGD_HDAT";

        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $tablename;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getKisaiStaff($model) {
        $tablename = ($model->field["HR_CLASS_TYPE"] == "2") ? "SCHREG_REGD_FI_HDAT" : "SCHREG_REGD_HDAT";

        $query  = " WITH HR_STAFF AS ( ";
        $query .= " SELECT ";
        $query .= "     TR_CD1   , ";
        $query .= "     TR_CD2   , ";
        $query .= "     TR_CD3   , ";
        $query .= "     SUBTR_CD1, ";
        $query .= "     SUBTR_CD2, ";
        $query .= "     SUBTR_CD3 ";
        $query .= " FROM ";
        $query .=       $tablename;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ), HR_STAFF2 AS ( ";
        $query .= "           SELECT 1 AS ORDER, TR_CD1    AS VALUE, T2.STAFFNAME AS LABEL FROM HR_STAFF T1 INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 WHERE TR_CD1 IS NOT NULL ";
        $query .= " UNION ALL SELECT 2 AS ORDER, TR_CD2    AS VALUE, T2.STAFFNAME AS LABEL FROM HR_STAFF T1 INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD2 WHERE TR_CD2 IS NOT NULL ";
        $query .= " UNION ALL SELECT 3 AS ORDER, TR_CD3    AS VALUE, T2.STAFFNAME AS LABEL FROM HR_STAFF T1 INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD3 WHERE TR_CD3 IS NOT NULL ";
        $query .= " UNION ALL SELECT 4 AS ORDER, SUBTR_CD1 AS VALUE, T2.STAFFNAME AS LABEL FROM HR_STAFF T1 INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.SUBTR_CD1 WHERE SUBTR_CD1 IS NOT NULL ";
        $query .= " UNION ALL SELECT 5 AS ORDER, SUBTR_CD2 AS VALUE, T2.STAFFNAME AS LABEL FROM HR_STAFF T1 INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.SUBTR_CD2 WHERE SUBTR_CD2 IS NOT NULL ";
        $query .= " UNION ALL SELECT 6 AS ORDER, SUBTR_CD3 AS VALUE, T2.STAFFNAME AS LABEL FROM HR_STAFF T1 INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.SUBTR_CD3 WHERE SUBTR_CD3 IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT VALUE, LABEL ";
        $query .= " FROM HR_STAFF2 T1 ";
        $query .= " WHERE ORDER IN (SELECT MIN(ORDER) FROM HR_StAFF2 GROUP BY VALUE) ";
        $query .= " ORDER BY ORDER ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $tablename = ($model->field["HR_CLASS_TYPE"] == "2") ? "SCHREG_REGD_FI_DAT" : "SCHREG_REGD_DAT";

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO ||  '番　' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $tablename." T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
