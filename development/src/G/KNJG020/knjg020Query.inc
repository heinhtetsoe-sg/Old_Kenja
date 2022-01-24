<?php

require_once('for_php7.php');

class knjg020Query extends Query {

    //証明書マスタから情報を取得
    function get_certif_kind($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CERTIF_KINDCD AS VALUE, ";
        $query .= "     T1.CERTIF_KINDCD || '　' || T1.KINDNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CERTIF_KIND_MST T1, ";
        $query .= "     CERTIF_KIND_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.CERTIF_KINDCD = T1.CERTIF_KINDCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
