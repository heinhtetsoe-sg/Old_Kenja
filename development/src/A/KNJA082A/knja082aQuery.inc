<?php

require_once('for_php7.php');

class knja082aQuery extends Query {

    //学年取得
    function GetGrade($model)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR      = T2.YEAR AND ";
        $query .= "     T1.YEAR      = '".$model->nextyear."' AND ";
        $query .= "     T1.SEMESTER  = '1' AND ";
        $query .= "     T1.GRADE     = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //中高判定用
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLNAME1 LIKE '%近畿%中学%' ";

        return $query;
    }

    //クラス数取得
    function GetHr($model){

        $query  = "WITH MAIN_T AS ( ";
        $query .= "SELECT DISTINCT ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS ";
        $query .= "FROM ";
        $query .= "    CLASS_FORMATION_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T1.YEAR ";
        $query .= "      AND T6.GRADE = T1.GRADE ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '{$model->nextyear}' ";
        $query .= "    AND T1.SEMESTER = '1' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "        AND T6.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->grade != "99" ) {
            $query .= "    AND T1.GRADE = '{$model->grade}' ";
        }
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    COUNT(T1.HR_CLASS) AS CLASS_COUNT ";
        $query .= "FROM  ";
        $query .= "    MAIN_T T1 ";
        $query .= "FETCH FIRST 1 ROWS ONLY ";
        
        return $query;
    }
}
?>
