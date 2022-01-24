<?php

require_once('for_php7.php');


class knjmp955Query extends Query {

    //精算科目
    function getLMdat()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEISAN_L_CD || T1.SEISAN_M_CD AS VALUE, ";
        $query .= "     T1.SEISAN_L_CD || T1.SEISAN_M_CD || ':' || L1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_SEISAN_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_M_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEISAN_L_CD = L1.LEVY_L_CD ";
        $query .= "          AND T1.SEISAN_M_CD = L1.LEVY_M_CD ";
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
        $query  = " SELECT ";
        $query .= "     REQUEST_NO AS VALUE, ";
        $query .= "     REQUEST_NO || ':' || VALUE(SEISAN_TITLE, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_SEISAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEISAN_L_CD || SEISAN_M_CD = '{$model->field["SEISAN_L_M_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
