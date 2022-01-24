<?php

require_once('for_php7.php');


class knjmp953Query extends Query {

    //購入科目
    function getLMdat()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.KOUNYU_L_CD || T1.KOUNYU_M_CD AS VALUE, ";
        $query .= "     T1.KOUNYU_L_CD || T1.KOUNYU_M_CD || ':' || L1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_M_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.KOUNYU_L_CD = L1.LEVY_L_CD ";
        $query .= "          AND T1.KOUNYU_M_CD = L1.LEVY_M_CD ";
        $query .= "          AND L1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //伝票番号
    function getRequestNo($model)
    {
        $query  = " WITH MEISAI AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     KOUNYU_L_CD, ";
        $query .= "     KOUNYU_M_CD, ";
        $query .= "     REQUEST_NO, ";
        $query .= "     MIN(KOUNYU_S_CD) AS KOUNYU_S_CD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND KOUNYU_L_CD || KOUNYU_M_CD = '{$model->field["KOUNYU_L_M_CD"]}' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     KOUNYU_L_CD, ";
        $query .= "     KOUNYU_M_CD, ";
        $query .= "     REQUEST_NO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.REQUEST_NO AS VALUE, ";
        $query .= "     VALUE(L1.LEVY_S_NAME, '') || CASE WHEN MEISAI.CNT > 1 THEN '･･他' ELSE '' END AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_DAT T1 ";
        $query .= "     INNER JOIN MEISAI ON T1.YEAR = MEISAI.YEAR ";
        $query .= "           AND T1.KOUNYU_L_CD = MEISAI.KOUNYU_L_CD ";
        $query .= "           AND T1.KOUNYU_M_CD = MEISAI.KOUNYU_M_CD ";
        $query .= "           AND T1.REQUEST_NO = MEISAI.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_S_MST L1 ON MEISAI.YEAR = L1.YEAR ";
        $query .= "          AND MEISAI.KOUNYU_L_CD = L1.LEVY_L_CD ";
        $query .= "          AND MEISAI.KOUNYU_M_CD = L1.LEVY_M_CD ";
        $query .= "          AND MEISAI.KOUNYU_S_CD = L1.LEVY_S_CD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
