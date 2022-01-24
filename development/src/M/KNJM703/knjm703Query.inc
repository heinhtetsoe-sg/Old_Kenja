<?php

require_once('for_php7.php');

class knjm703Query extends Query {

    //教科
    function getClassCd($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
        }
        $query .= "     CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD >= '92' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目
    function getSubClassCd($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
        }
        $query .= "     SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND substr(SUBCLASSCD, 1, 2) || '-' || SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
        } else {
            $query .= "     AND substr(SUBCLASSCD, 1, 2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座
    function getChairCd($model) {
        $query  = " SELECT ";
        $query .= "     CHAIRCD AS VALUE, ";
        $query .= "     CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出席日付
    function getAttendDate($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     ATTENDDATE AS LABEL, ";
        $query .= "     ATTENDDATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SPECIALACT_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->field["CHAIRCD"]) {
            $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校時
    function getPeriodCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.NAME1 AS LABEL, ";
        $query .= "     T1.PERIODF AS VALUE ";
        $query .= " FROM ";
        $query .= "     SPECIALACT_ATTEND_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'B001' ";
        $query .= "          AND T1.PERIODF = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->field["ATTENDDATE"]) {
            $query .= "     AND T1.ATTENDDATE = '".$model->field["ATTENDDATE"]."' ";
        } else {
            //日付指定が無い場合は、レコード取得出来ないようにする。
            $query .= "     AND T1.SUBCLASSCD = '' ";
        }
        if ($model->field["CHAIRCD"]) {
            $query .= "     AND T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
