<?php

require_once('for_php7.php');

class knjp375Query extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //担当
    function getStaff($model){
        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getLList($model) {
        $query  = " SELECT ";
        $query .= "     TRANS_DIV.TRANSFER_DIV AS VALUE, ";
        $query .= "     TRANS_DIV.TRANSFER_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MONEY_TRANSFER_DIV_MST TRANS_DIV ";
        $query .= " WHERE ";
        $query .= "     TRANS_DIV.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND TRANS_DIV.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     AND TRANS_DIV.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
