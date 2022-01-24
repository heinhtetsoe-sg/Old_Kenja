<?php

require_once('for_php7.php');

class knjm442mQuery extends Query {
    //学期取得
    function getSemesterMst() {
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

    //科目取得
    function getSubclassList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_PASS_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD      = T2.SUBCLASSCD ";
        $query .= "                          AND T1.CLASSCD         = T2.CLASSCD ";
        $query .= "                          AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
        $query .= "                          AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                          AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                          AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.SEM_PASS_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
