<?php

require_once('for_php7.php');

class knjp910Query extends Query {

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

    //年度を取得
    function getYear() {
        $setYear = CTRL_YEAR;
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR AS VALUE, ";
        $query .= "     T1.YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '{$setYear}' AS VALUE, ";
        $query .= "     '{$setYear}' || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    
    //支出科目
    function getLevyLDiv($model, $cd="") {
        $setYear = ($model->field["YEAR"]) ? $model->field["YEAR"] : CTRL_YEAR;
        $query  = "";
        $query .= " SELECT ";
        $query .= "     LEVY_L_CD || ':' || LEVY_L_NAME AS LABEL, ";
        $query .= "     LEVY_L_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_L_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND YEAR        = '{$setYear}' ";
        if ($cd) {
            $query .= " AND LEVY_L_CD = '{$cd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //支出項目
    function getLevyMDiv($model, $lcd="", $mcd="") {
        $setYear = ($model->field["YEAR"]) ? $model->field["YEAR"] : CTRL_YEAR;
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR        = '{$setYear}' ";
        if ($lcd) {
            $query .= " AND T1.LEVY_L_CD = '{$lcd}' ";
        } else {
            $query .= " AND T1.LEVY_L_CD = '{$model->field["OUTGO_L_CD"]}' ";
        }
        if ($mcd) {
            $query .= " AND T1.LEVY_M_CD = '{$mcd}' ";
        }
        $query .= " AND T1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧取得
    function selectQuery($db, $model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->field["YEAR"]}' ";
        if ($model->field["OUTGO_L_CD"]) {
            $query .= "     AND T1.OUTGO_L_CD = '{$model->field["OUTGO_L_CD"]}' ";
        }
        if ($model->field["OUTGO_L_M_CD"]) {
            $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->field["OUTGO_L_M_CD"]}' ";
        }
        if ($model->field["REQUEST_NO"]) {
            $query .= "     AND T1.REQUEST_NO LIKE '%".$model->field["REQUEST_NO"]."%' ";
        }
        if ($model->field["REQUEST_DATE"]) {
            $query .= "     AND T1.REQUEST_DATE = '".str_replace("/", "-", $model->field["REQUEST_DATE"])."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.REQUEST_DATE DESC, ";
        $query .= "     T1.OUTGO_L_CD, ";
        $query .= "     T1.OUTGO_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        return $query;
    }
}
?>
