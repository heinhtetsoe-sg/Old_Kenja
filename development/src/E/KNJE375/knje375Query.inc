<?php

require_once('for_php7.php');


class knje375Query extends Query {

    //年度取得
    function getYear($sort = "" ,$model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '" .SCHOOLKIND ."' ";
        }
        $query .= " ORDER BY ";
        if ($sort) {
            $query .= "     VALUE ASC ";
        } else {
            $query .= "     VALUE DESC ";
        }

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME_M.NAME1 AS VALUE, ";
        $query .= "     NAME_M.ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST NAME_M ";
        $query .= " WHERE ";
        $query .= "     NAME_M.NAMECD1 = 'A023' ";
        $query .= "     AND NAME_M.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME_M.NAME1 IN (SELECT ";
                $query .= "                              REGD_G.SCHOOL_KIND ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME_M.NAME1 IN (SELECT ";
            $query .= "                              REGD_G.SCHOOL_KIND ";
            $query .= "                          FROM ";
            $query .= "                              SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                              REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME_M.NAMECD2 ";

        return $query;
    }
}
?>
