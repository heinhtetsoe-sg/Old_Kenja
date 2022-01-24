<?php

require_once('for_php7.php');

class knjb3048Query extends Query {
    //年度取得
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学期取得
    function getSemester($model, $semester="") {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".$model->field["YEAR"]."' AND ";
        if ($semester) {
            $query .= "     SEMESTER  = '".$semester."' ";
        } else {
            $query .= "     SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ取得（クラスシート）
    function getCsvQuery1($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     T2.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "          ON T1.YEAR     = T2.YEAR ";
        $query .= "         AND T1.GRADE    = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }

    //CSVデータ取得（特別教室）
    function getCsvQuery2($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     FACILITY_MST T1 ";
        $query .= "     INNER JOIN FACILITY_YDAT T2 ";
        $query .= "          ON T1.FACCD    = T2.FACCD ";
        $query .= "         AND T2.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.FACCD ";

        return $query;
    }

    //CSVデータ取得（科目）
    function getCsvQuery3($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     L1.CLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= "     INNER JOIN SUBCLASS_YDAT T2 ";
        $query .= "          ON T1.CLASSCD      = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "         AND T2.YEAR         = '".$model->field["YEAR"]."' ";
        $query .= "     LEFT JOIN CLASS_MST L1 ";
        $query .= "          ON T1.CLASSCD      = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //CSVデータ取得（先生）
    function getCsvQuery4($model) {
        $query  = " WITH CHARGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.STAFFCD, ";
        $query .= "         T1.HR_CLASS_NAME1 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_HDAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             S1.TR_CD1 AS STAFFCD, ";
        $query .= "             MIN(S1.GRADE || S1.HR_CLASS) AS GRADE_HR_CLASS ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_HDAT S1 ";
        $query .= "             INNER JOIN SCHREG_REGD_GDAT S2 ";
        $query .= "                  ON S1.YEAR     = S2.YEAR ";
        $query .= "                 AND S1.GRADE    = S2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "             AND S2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else {
            $query .= "             AND S2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         WHERE ";
        $query .= "             S1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "             S1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         GROUP BY ";
        $query .= "             S1.TR_CD1 ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = T2.GRADE_HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.SECTIONNAME, ";
        $query .= "     CASE WHEN L2.JOBNAME LIKE '%非常勤%' THEN '非常勤' ELSE '' END AS PART_TIME, ";
        $query .= "     L3.HR_CLASS_NAME1 ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1 ";
        $query .= "     LEFT JOIN SECTION_MST L1 ON T1.SECTIONCD = L1.SECTIONCD ";
        $query .= "     LEFT JOIN JOB_MST L2 ON T1.JOBCD = L2.JOBCD ";
        $query .= "     LEFT JOIN CHARGE L3 ON T1.STAFFCD = L3.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.CHARGECLASSCD = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD ";

        return $query;
    }
}
?>
