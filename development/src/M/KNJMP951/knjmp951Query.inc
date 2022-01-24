<?php

require_once('for_php7.php');


class knjmp951Query extends Query {

    //収入科目
    function getLMdat()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.INCOME_L_CD || T1.INCOME_M_CD AS VALUE, ";
        $query .= "     T1.INCOME_L_CD || T1.INCOME_M_CD || ':' || L1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_M_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.INCOME_L_CD = L1.LEVY_L_CD ";
        $query .= "          AND T1.INCOME_M_CD = L1.LEVY_M_CD ";
        $query .= "          AND L1.LEVY_IN_OUT_DIV = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
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
        $query .= "     LEVY_REQUEST_INCOME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND INCOME_L_CD || INCOME_M_CD = '{$model->field["INCOME_L_M_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
