<?php

require_once('for_php7.php');


class knjd041Query extends Query {
    //学期取得
    function getSemester() {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($model) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || '  ' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable ;
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TESTKINDCD IN ('01','02','99') ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND SEMESTER = '".$model->field["GAKKI"]."' ";
            if ($model->field["GAKKI"] == "9") {
                $query .= "     AND TESTITEMCD = '00' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getClassData($model)
    {
        $query  = "SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        } else {
        $query .= "substr(T1.SUBCLASSCD,1,2) AS VALUE, ";
        }
        $query .= "T2.CLASSNAME AS LABEL";
        $query .= " FROM chair_dat T1 INNER JOIN class_mst T2";
        $query .= "                         ON substr(T1.SUBCLASSCD,1,2)=T2.CLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND";
        }
        $query .= " WHERE ((T1.YEAR='".CTRL_YEAR."')";
        if ($model->field["GAKKI"] != 9 ) {
            $query .= " AND (T1.SEMESTER='".$model->field["GAKKI"]."')";
        }
//        $query .= " AND (substr(T1.SUBCLASSCD,1,2) < '"."90"."')";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " )";
        $query .= " order by ";
        $query .= " VALUE ";

        return $query;
    }
}
?>
