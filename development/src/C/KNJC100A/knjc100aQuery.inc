<?php

require_once('for_php7.php');

class knjc100aQuery extends Query
{

    //学年名コンボ取得
    public function getGradeName()
    {
        $query   = " SELECT ";
        $query  .= "     H.GRADE AS VALUE, ";
        $query  .= "     G.GRADE_NAME1 AS LABEL ";
        $query  .= " FROM ";
        $query  .= "     SCHREG_REGD_HDAT AS H ";
        $query  .= "     JOIN SCHREG_REGD_GDAT AS G ";
        $query  .= " ON ( ";
        $query  .= "         G.YEAR  = H.YEAR ";
        $query  .= "     AND G.GRADE = H.GRADE) ";
        $query  .= " WHERE ";
        $query  .= "     H.YEAR = '".CTRL_YEAR."' ";
        $query  .= "     AND G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query  .= " GROUP BY ";
        $query  .= "     H.GRADE, ";
        $query  .= "     G.GRADE_NAME1 ";
        $query  .= " ORDER BY ";
        $query  .= "     H.GRADE ";

        return $query;
    }

    //NAME_MSTを取得
    public function getNameMst()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if (SCHOOLKIND == 'H') {
            $query .= "     NAMECD1 = 'ZH05' ";
        } else {
            $query .= "     NAMECD1 = 'ZJ05' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME  AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND";
        $query .= "     GRADE    = '".$model->field["GRADE_NAME"]."' AND";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     HR_CLASS ";

        return $query;
    }
}
