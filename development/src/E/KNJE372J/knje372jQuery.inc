<?php

require_once('for_php7.php');

class knje372jQuery extends Query
{

    //CSV出力
    public function getCsvList($model)
    {
        $query  = " SELECT ";
        $query .= "     RECOM.FACULTY_LIST_NAME, ";
        $query .= "     RECOM.DEPARTMENT_LIST_NAME, ";
        $query .= "     RECOM.DEPARTMENT_LIST_CD, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     TRANSLATE_H_K(BASE.NAME_KANA) AS NAME_KANA, ";
        $query .= "     HOPE.RECOMMENDATION_BASE_DIV AS BASE_DIV, ";
        $query .= "     NULL AS JUKEN_NO, ";
        $query .= "     NULL AS REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT CONV ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = CONV.SCHREGNO ";
        $query .= "     INNER JOIN AFT_RECOMMENDATION_LIMIT_MST RECOM ON RECOM.YEAR = CONV.YEAR ";
        $query .= "         AND (RECOM.DEPARTMENT_S = CONV.RECOMMENDATION_DEPARTMENT_CD OR RECOM.DEPARTMENT_H = CONV.RECOMMENDATION_DEPARTMENT_CD) ";
        $query .= "     LEFT JOIN AFT_SCHREG_HOPE_DEPARTMENT HOPE ON HOPE.YEAR = CONV.YEAR ";
        $query .= "         AND HOPE.SCHREGNO = CONV.SCHREGNO ";
        $query .= "         AND HOPE.DEPARTMENT_CD = CONV.RECOMMENDATION_DEPARTMENT_CD ";
        $query .= " WHERE ";
        $query .= "     CONV.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     RECOM.DEPARTMENT_LIST_ORDER, ";   //推薦枠マスタ.推薦名簿出力順
        $query .= "     HOPE.RECOMMENDATION_BASE_DIV, ";  //志望学科データ.推薦基準区分
        $query .= "     CONV.CONVERT_SCORE DESC ";        //換算値データ.換算値の降順

        return $query;
    }
}
