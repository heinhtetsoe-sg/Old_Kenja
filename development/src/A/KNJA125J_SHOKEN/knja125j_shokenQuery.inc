<?php

require_once('for_php7.php');

class knja125j_shokenQuery extends Query
{

    //１レコード取得
    public function getTrainRow($model, $flg)
    {
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
            $year_annual = explode(":", $model->field["YEAR_ANNUAL"]);
            $query .= "     YEAR     = '".$year_annual[0]."' AND ";
            $query .= "     ANNUAL   = '".$year_annual[1]."' AND ";
        } elseif ($flg === 'year_anuual') {
            $query .= "     YEAR     < '".CTRL_YEAR."' AND ";
        }
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if ($flg === 'sanshou') {
            $query .= " ORDER BY ";
            $query .= "     YEAR ";
        } elseif ($flg == "year_anuual") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //名前の取得
    public function getName($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //行動記録の取得
    public function getBehavior($model)
    {
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
