<?php

require_once('for_php7.php');

class knjh180Query extends Query {
    //すでに存在するのかしないのかチェック
    function checkExist($station_cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     STATION_CD = '{$station_cd}' ";

        return $query;
    }

    //すでにレコードが存在していたらupate
    function updateQueryCsv($data)
    {
        $data1["RR_CD"][TEXT]           = $data["RR_CD"];
        $data1["LINE_CD"][TEXT]         = $data["LINE_CD"];
        $data1["STATION_CD"][TEXT]      = $data["STATION_CD"];
        $data1["LINE_SORT"][TEXT]       = $data["LINE_SORT"];
        $data1["STATION_SORT"][TEXT]    = $data["STATION_SORT"];
        $data1["STATION_G_CD"][TEXT]    = $data["STATION_G_CD"];
        $data1["R_TYPE"][TEXT]          = $data["R_TYPE"];
        $data1["RR_NAME"][TEXT]         = $data["RR_NAME"];
        $data1["LINE_NAME"][TEXT]       = $data["LINE_NAME"];
        $data1["STATION_NAME"][TEXT]    = $data["STATION_NAME"];
        $data1["PREF_CD"][TEXT]         = $data["PREF_CD"];
        $data1["LON"][NUMBER]           = $data["LON"];
        $data1["LAT"][NUMBER]           = $data["LAT"];
        $data1["F_FLAG"][TEXT]          = $data["F_FLAG"];

        $data1["REGISTERCD"][TEXT]      = $registercd;
        $data1["UPDATED"][NUMBER]       = "sysdate()";

        $where  = " WHERE ";
        $where .= "         STATION_CD  = '{$data["STATION_CD"]}' ";

        $query = Query::updateSQL($data1, "STATION_NETMST", $where);

        return $query;
    }

    //レコードが存在していなかったらinsert
    function insertQueryCsv($data)
    {
        $data1["RR_CD"][TEXT]          = $data["RR_CD"];
        $data1["LINE_CD"][TEXT]        = $data["LINE_CD"];
        $data1["STATION_CD"][TEXT]     = $data["STATION_CD"];
        $data1["LINE_SORT"][TEXT]      = $data["LINE_SORT"];
        $data1["STATION_SORT"][TEXT]   = $data["STATION_SORT"];
        $data1["STATION_G_CD"][TEXT]   = $data["STATION_G_CD"];
        $data1["R_TYPE"][TEXT]         = $data["R_TYPE"];
        $data1["RR_NAME"][TEXT]        = $data["RR_NAME"];
        $data1["LINE_NAME"][TEXT]      = $data["LINE_NAME"];
        $data1["STATION_NAME"][TEXT]   = $data["STATION_NAME"];
        $data1["PREF_CD"][TEXT]        = $data["PREF_CD"];
        $data1["LON"][NUMBER]          = $data["LON"];
        $data1["LAT"][NUMBER]          = $data["LAT"];
        $data1["F_FLAG"][TEXT]         = $data["F_FLAG"];

        $data1["REGISTERCD"][TEXT]     = STAFFCD;
        $data1["UPDATED"][FUNC]        = "sysdate()";

        $query = Query::insertSQL($data1, "STATION_NETMST");
        return $query;
    }

    //路線マスタを一旦削除
    function deleteTrainLine() {
        $query = " DELETE FROM TRAIN_LINE_MST ";
        return $query;
    }

    //駅マスタを元に路線マスタを作成
    function insertTrainLine() {
        $query  = " INSERT INTO ";
        $query .= "     TRAIN_LINE_MST ";
        $query .= "         (RR_CD, ";
        $query .= "          AREA_CD, ";
        $query .= "          LINE_CD, ";
        $query .= "          LINE_SORT, ";
        $query .= "          LINE_NAME, ";
        $query .= "          REGISTERCD, ";
        $query .= "          UPDATED ";
        $query .= "         ) ";
        $query .= " SELECT ";
        $query .= "     RR_CD, ";
        $query .= "     SUBSTR(LINE_CD,3,1) AS AREA_CD, ";
        $query .= "     LINE_CD, ";
        $query .= "     LINE_SORT, ";
        $query .= "     LINE_NAME, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " GROUP BY ";
        $query .= "     RR_CD, ";
        $query .= "     LINE_CD, ";
        $query .= "     LINE_SORT, ";
        $query .= "     LINE_NAME ";

        return $query;
    }
}
?>
