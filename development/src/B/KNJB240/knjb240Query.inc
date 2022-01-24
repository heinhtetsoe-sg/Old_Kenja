<?php

require_once('for_php7.php');

class knjb240Query extends Query {
    //学期取得
    function getSemester($date="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        if ($date) {
            $query .= "     AND '".$date."' BETWEEN SDATE AND EDATE ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.CLASSCD || '-' || T1.SUBCLASSCD AS SORT, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = T2.YEAR AND ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER         = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.CLASSCD          = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND EXISTS (SELECT ";
            $query .= "                     'X' ";
            $query .= "                 FROM ";
            $query .= "                     CHAIR_STF_DAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR     = T1.YEAR AND ";
            $query .= "                     S1.SEMESTER = T1.SEMESTER AND ";
            $query .= "                     S1.CHAIRCD  = T1.CHAIRCD AND ";
            $query .= "                     S1.STAFFCD  = '".STAFFCD."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    //講座一覧取得
    function getChairList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIRCD AS VALUE, ";
        $query .= "     CHAIRCD || ' ' || VALUE(CHAIRNAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND EXISTS (SELECT ";
            $query .= "                     'X' ";
            $query .= "                 FROM ";
            $query .= "                     CHAIR_STF_DAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR     = T1.YEAR AND ";
            $query .= "                     S1.SEMESTER = T1.SEMESTER AND ";
            $query .= "                     S1.CHAIRCD  = T1.CHAIRCD AND ";
            $query .= "                     S1.STAFFCD  = '".STAFFCD."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
