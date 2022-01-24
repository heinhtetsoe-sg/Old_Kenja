<?php

require_once('for_php7.php');

class knjxtrainQuery extends Query {
    //名前
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }
    
    //生徒環境調査データ取得
    function getSchreg_Envir_dat($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //路線名、駅名を取得
    function getStationName($station_cd) {
        $query  = " SELECT ";
        $query .= "     STATION_NAME, ";
        $query .= "     LINE_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     STATION_CD = '{$station_cd}' ";

        return $query;
    }

    //名称マスタから名称取得
    function getV_name_mst($namecd,$model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM
                        V_NAME_MST ";
        $query .= " WHERE
                        NAMECD1 = '{$namecd}' ";
        $query .= " AND YEAR    = '{$model->year}'";

        return $query;
    }


}
?>
