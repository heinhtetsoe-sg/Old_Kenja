<?php

require_once('for_php7.php');


class knjc190Query extends Query {
    
    //科目取得
    function GetSubclass($model)
    {
        $query  = "SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD AS CD, ";
        } else {
            $query .= "    SUBCLASSCD AS CD, ";
        }
        $query .= "    SUBCLASSNAME AS NAME ";
        $query .= "FROM ";
        $query .= "    V_SUBCLASS_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    CD ";

        return $query;
    }

    //教員取得
    function GetStaff()
    {
        $query  = "SELECT ";
        $query .= "    T1.STAFFCD AS CD, ";
        $query .= "    L1.STAFFNAME AS NAME ";
        $query .= "FROM ";
        $query .= "    CHAIR_STF_DAT T1 ";
        $query .= "    LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.STAFFCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "UNION ";
        $query .= "SELECT ";
        $query .= "    T1.STAFFCD AS CD, ";
        $query .= "    L1.STAFFNAME AS NAME ";
        $query .= "FROM ";
        $query .= "    SCH_STF_DAT T1 ";
        $query .= "    LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.STAFFCD ";
        $query .= "WHERE ";
        $query .= "   Fiscalyear(T1.EXECUTEDATE) = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    CD ";

        return $query;
    }
}
?>
