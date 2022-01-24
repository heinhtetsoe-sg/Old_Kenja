<?php

require_once('for_php7.php');

class knjd041vQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($model) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND GRADE = '00' ";
            $query .= " AND COURSECD || '-' || MAJORCD = '".$model->field["MAJOR"]."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                    $query .= " AND SCHOOLCD IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getClassData($model) {
        $query  = "SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.CLASSCD AS SORTVAL, ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD,1,2) AS VALUE, ";
        }
        $query .= "     T2.CLASSNAME AS LABEL ";
        $query .= " FROM chair_dat T1 INNER JOIN class_mst T2";
        $query .= "                         ON substr(T1.SUBCLASSCD,1,2)=T2.CLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "                    AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " WHERE ((T1.YEAR='".CTRL_YEAR."')";
        if ($model->field["SEMESTER"] != 9 ) {
            $query .= " AND (T1.SEMESTER='".$model->field["SEMESTER"]."')";
        }
        $query .= " AND (substr(T1.SUBCLASSCD,1,2) < '"."90"."'))";
        $query .= " order by ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SORTVAL ";
        } else {
            $query .= "     VALUE ";
        }
        return $query;
    }

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
        return $query;
    }
}
?>
