<?php

require_once('for_php7.php');

class knjf175cQuery extends Query
{
    //校種取得
    public function getSchkind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     ABBV3 ";

        return $query;
    }

    //印影カウント
    public function getStampCnt($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '9' ";
        $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= "     AND PROGRAMID   = 'KNJF175C' ";

        return $query;
    }

    //印影名称取得
    public function getStampName($model)
    {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     TITLE ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '9' ";
        $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= "     AND PROGRAMID   = 'KNJF175C' ";
        $query .= "     AND SEQ BETWEEN 1 AND 5";

        return $query;
    }
}
