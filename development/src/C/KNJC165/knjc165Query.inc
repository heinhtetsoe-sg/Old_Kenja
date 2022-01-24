<?php

require_once('for_php7.php');

class knjc165Query extends Query {
//漢字
    //校種
    function getA023($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
 }
?>
