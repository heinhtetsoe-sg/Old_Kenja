<?php

class knjh081Query extends Query {

    //CSVデータ出力
    function selectMainQuery($model) {
        $query .= " SELECT ";
        $query .= "     '".$model->field["SDATE"]."' AS SDATE, ";
        $query .= "     '".$model->field["EDATE"]."' AS EDATE, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.TRAINDATE, ";
        $query .= "     T4.HR_NAME, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T5.NAME, ";
        $query .= "     T6.NAME1 AS PATIENT_NAME, ";
        $query .= "     T1.HOWTOTRAINCD, ";
        $query .= "     T7.NAME1 AS HOWTOTRAIN_NAME, ";
        $query .= "     T1.CONTENT, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T8.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRAINHIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SEMESTER_MST T2 ON T2.YEAR  = T1.YEAR ";
        $query .= "                    AND T2.SDATE <= T1.TRAINDATE ";
        $query .= "                    AND T2.EDATE >= T1.TRAINDATE ";
        $query .= "                    AND T2.SEMESTER <> '9' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                       AND T3.YEAR     = T1.YEAR ";
        $query .= "                       AND T3.SEMESTER = T2.SEMESTER ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT T4 ON T4.YEAR     = T1.YEAR ";
        $query .= "                        AND T4.SEMESTER = T2.SEMESTER ";
        $query .= "                        AND T4.GRADE    = T3.GRADE ";
        $query .= "                        AND T4.HR_CLASS = T3.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST T5 ON T5.SCHREGNO     = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     V_NAME_MST T6 ON T6.YEAR    = T1.YEAR ";
        $query .= "                  AND T6.NAMECD1 = 'H301' ";
        $query .= "                  AND T6.NAMECD2 = T1.PATIENTCD ";
        $query .= " LEFT JOIN ";
        $query .= "     V_NAME_MST T7 ON T7.YEAR    = T1.YEAR ";
        $query .= "                  AND T7.NAMECD1 = 'H302' ";
        $query .= "                  AND T7.NAMECD2 = T1.HOWTOTRAINCD ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_MST T8 ON T8.STAFFCD = T1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.TRAINDATE BETWEEN '".$model->field["SDATE"]."' AND '".$model->field["EDATE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TRAINDATE ";

        return $query;
    }
}
?>
