<?php

require_once('for_php7.php');

class knjd643Query extends Query {

    //推薦希望順位を取得
    function getWishRank()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     INT(WISH_RANK) AS VALUE ";
        $query .= " FROM   ";
        $query .= "     SCHREG_RECOMMENDATION_WISH_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     WISH_RANK ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //学部学科候補を取得
    function getBukaList($model)
    {
        $query  = "";
        $query .= "SELECT  T2.BU_NAME || '　' || T2.KA_NAME AS LABEL, ";
        $query .= "        T2.BU_CD || T2.KA_CD AS VALUE ";
        $query .= "  FROM  COLLEGE_RECOMMENDATION_DAT T2 ";
        $query .= " WHERE  T2.YEAR = '".CTRL_YEAR."' ";
        //希望順位2位以下
        if ("1" < $model->field["WISH_RANK"]) {
            $query .= "   AND  NOT EXISTS( ";
            $query .= "            SELECT  'X' ";
            $query .= "            FROM (SELECT  T1.SCHOOL_CD, T1.BU_CD, T1.KA_CD, COUNT(*) AS SCHCNT ";
            $query .= "                    FROM  SCHREG_RECOMMENDATION_WISH_DAT T1 ";
            $query .= "                   WHERE  T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "                     AND  T1.RECOMMENDATION_FLG = '1' ";
            $query .= "                   GROUP BY T1.SCHOOL_CD, T1.BU_CD, T1.KA_CD) W1 ";
            $query .= "             WHERE  T2.SCHOOL_CD = W1.SCHOOL_CD ";
            $query .= "               AND  T2.BU_CD = W1.BU_CD ";
            $query .= "               AND  T2.KA_CD = W1.KA_CD ";
            $query .= "               AND  T2.FRAME = W1.SCHCNT ";
            $query .= "        ) ";
        }
        $query .= " ORDER BY T2.BU_CD, T2.KA_CD ";

        return $query;
    }

}
?>
