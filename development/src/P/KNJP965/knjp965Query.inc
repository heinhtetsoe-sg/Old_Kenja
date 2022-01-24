<?php

require_once('for_php7.php');


class knjp965Query extends Query {

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     '' AS VALUE, ";
        $query .= "     '--全て--' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //収入科目
    function getMonth($model)
    {
        $query  = " WITH MONTH_T(LABEL, VALUE, SORT) AS ( ";
        $setUnion = "";
        $sort = 0;
        foreach ($model->yearMonth as $value => $label) {
            $query .= $setUnion."     VALUES('{$label}', '{$value}', {$sort}) ";
            $setUnion = " UNION ";
            $sort++;
        }
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL, ";
        $query .= "     SORT ";
        $query .= " FROM ";
        $query .= "     MONTH_T ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

}
?>
