<?php

require_once('for_php7.php');


class knjp953Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //購入科目
    function getLMdat($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.KOUNYU_L_CD || T1.KOUNYU_M_CD AS VALUE, ";
        $query .= "     T1.KOUNYU_L_CD || T1.KOUNYU_M_CD || ':' || L1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_M_MST L1 ON T1.SCHOOLCD    = L1.SCHOOLCD ";
        $query .= "                            AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "                            AND T1.YEAR        = L1.YEAR ";
        $query .= "                            AND T1.KOUNYU_L_CD = L1.LEVY_L_CD ";
        $query .= "                            AND T1.KOUNYU_M_CD = L1.LEVY_M_CD ";
        $query .= "                            AND L1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}'";
        $query .= "     AND T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //伝票番号
    function getRequestNo($model)
    {
        $query  = " WITH MEISAI AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     YEAR, ";
        $query .= "     KOUNYU_L_CD, ";
        $query .= "     KOUNYU_M_CD, ";
        $query .= "     REQUEST_NO, ";
        $query .= "     MIN(KOUNYU_S_CD) AS KOUNYU_S_CD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}'";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND KOUNYU_L_CD || KOUNYU_M_CD = '{$model->field["KOUNYU_L_M_CD"]}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
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
        $query .= "     INNER JOIN MEISAI ON T1.SCHOOLCD    = MEISAI.SCHOOLCD ";
        $query .= "                      AND T1.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "                      AND T1.YEAR        = MEISAI.YEAR ";
        $query .= "                      AND T1.KOUNYU_L_CD = MEISAI.KOUNYU_L_CD ";
        $query .= "                      AND T1.KOUNYU_M_CD = MEISAI.KOUNYU_M_CD ";
        $query .= "                      AND T1.REQUEST_NO  = MEISAI.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_S_MST L1 ON MEISAI.SCHOOLCD    = L1.SCHOOLCD ";
        $query .= "                            AND MEISAI.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "                            AND MEISAI.YEAR        = L1.YEAR ";
        $query .= "                            AND MEISAI.KOUNYU_L_CD = L1.LEVY_L_CD ";
        $query .= "                            AND MEISAI.KOUNYU_M_CD = L1.LEVY_M_CD ";
        $query .= "                            AND MEISAI.KOUNYU_S_CD = L1.LEVY_S_CD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
