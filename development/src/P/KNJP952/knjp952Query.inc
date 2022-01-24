<?php

require_once('for_php7.php');


class knjp952Query extends Query {

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

    //支出科目
    function getLMdat($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.OUTGO_L_CD || T1.OUTGO_M_CD AS VALUE, ";
        $query .= "     T1.OUTGO_L_CD || T1.OUTGO_M_CD || ':' || L1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_M_MST L1 ON T1.SCHOOLCD = L1.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.OUTGO_L_CD = L1.LEVY_L_CD ";
        $query .= "          AND T1.OUTGO_M_CD = L1.LEVY_M_CD ";
        $query .= "          AND L1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}'";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //伝票番号
    function getRequestNo($model)
    {
        $query  = " SELECT ";
        $query .= "     REQUEST_NO AS VALUE, ";
        $query .= "     REQUEST_NO || ':' || VALUE(REQUEST_REASON, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}'";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND OUTGO_L_CD || OUTGO_M_CD = '{$model->field["OUTGO_L_M_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
