<?php

require_once('for_php7.php');

class knja128j_shokenQuery extends Query {
    //１レコード取得
    function getTrainRow($model, $flg) {
        $query  = " SELECT ";
        if ($flg == "year_anuual") {
            $query .= "     YEAR || '年度　' || ANNUAL || '学年' AS LABEL, ";
            $query .= "     YEAR || ':' || ANNUAL  AS VALUE ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        if ($flg === 'setyear') {
            $year_annual = array();
            $tmp = $model->field["YEAR_ANNUAL"] ? $model->field["YEAR_ANNUAL"] : ":" ; //DBエラー回避
            $year_annual = explode(":", $tmp);
            $query .= "     YEAR     = '".$year_annual[0]."' AND ";
            $query .= "     ANNUAL   = '".$year_annual[1]."' AND ";
        }
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR IN (SELECT ";
        $query .= "                 S1.YEAR ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT S1, ";
        $query .= "                 NAME_MST S2 ";
        $query .= "             WHERE ";
        $query .= "                 S1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "                 S2.NAMECD1  = 'A023' AND ";
        $query .= "                 S2.NAME1    = 'J' AND ";
        $query .= "                 S1.GRADE BETWEEN S2.NAME2 AND S2.NAME3) ";
        if ($flg === 'sanshou') {
            $query .= " ORDER BY ";
            $query .= "     YEAR ";
        } else if ($flg == "year_anuual") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //名前の取得
    function getName($model) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //行動記録の取得
    function getBehavior($model) {
        $year_annual = array();
        $year_annual = explode(":", $model->field["YEAR_ANNUAL"]);
    
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     ANNUAL, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year_annual[0]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }
}
?>
