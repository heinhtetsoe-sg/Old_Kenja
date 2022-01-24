<?php

require_once('for_php7.php');

class knjmp900Query extends Query {

    //年度を取得
    function getYear() {
        $setYear = CTRL_YEAR;
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR AS VALUE, ";
        $query .= "     T1.YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
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

    
    //入金科目
    function getLevyLDiv($model, $cd="") {
        $setYear = ($model->field["YEAR"]) ? $model->field["YEAR"] : CTRL_YEAR;
        $query  = "";
        $query .= " SELECT ";
        $query .= "     LEVY_L_CD || ':' || LEVY_L_NAME AS LABEL, ";
        $query .= "     LEVY_L_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$setYear}' ";
        if ($cd) {
            $query .= " AND LEVY_L_CD = '{$cd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //収入科目
    function getLevyMDiv($model, $lcd="", $mcd="") {
        $setYear = ($model->field["YEAR"]) ? $model->field["YEAR"] : CTRL_YEAR;
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$setYear}' ";
        if ($lcd) {
            $query .= " AND T1.LEVY_L_CD = '{$lcd}' ";
        } else {
            $query .= " AND T1.LEVY_L_CD = '{$model->field["INCOME_L_CD"]}' ";
        }
        if ($mcd) {
            $query .= " AND T1.LEVY_M_CD = '{$mcd}' ";
        }
        $query .= " AND T1.LEVY_IN_OUT_DIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧取得
    function selectQuery($db, $model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        if ($model->field["INCOME_L_CD"]) {
            $query .= "     AND T1.INCOME_L_CD = '{$model->field["INCOME_L_CD"]}' ";
        }
        if ($model->field["INCOME_L_M_CD"]) {
            $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->field["INCOME_L_M_CD"]}' ";
        }
        if ($model->field["REQUEST_NO"]) {
            $query .= "     AND T1.REQUEST_NO LIKE '%".$model->field["REQUEST_NO"]."%' ";
        }
        if ($model->field["REQUEST_DATE"]) {
            $query .= "     AND T1.REQUEST_DATE = '".str_replace("/", "-", $model->field["REQUEST_DATE"])."' ";
        }
        //繰越金データは表示しない
        $query .= "     AND T1.REQUEST_NO NOT LIKE '%A00000' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.REQUEST_DATE DESC, ";
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     T1.INCOME_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        return $query;
    }
}
?>
