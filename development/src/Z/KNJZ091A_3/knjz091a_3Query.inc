<?php

require_once('for_php7.php');

class knjz091a_3Query extends Query {

    //一覧用データ取得
    function selectQuery($model) {
        $query  = "";
        $query .= "  SELECT ";
        $query .= "      PRISCHOOLCD, ";
        $query .= "      PRISCHOOL_CLASS_CD, ";
        $query .= "      PRISCHOOL_NAME, ";
        $query .= "      PRISCHOOL_KANA, ";
        $query .= "      PRINCNAME, ";
        $query .= "      PRINCNAME_SHOW, ";
        $query .= "      PRINCKANA, ";
        $query .= "      DISTRICTCD || ':' || MEISYOU_GET(DISTRICTCD,'Z003',1) AS DISTRICTCD, ";
        $query .= "      PRISCHOOL_ZIPCD, ";
        $query .= "      PRISCHOOL_ADDR1, ";
        $query .= "      PRISCHOOL_ADDR2, ";
        $query .= "      PRISCHOOL_TELNO, ";
        $query .= "      PRISCHOOL_FAXNO ";
        $query .= "  FROM ";
        $query .= "      PRISCHOOL_CLASS_MST ";
        $query .= "  WHERE ";
        $query .= "      PRISCHOOLCD = '{$model->prischoolCd}' ";
        $query .= "  ORDER BY ";
        $query .= "      PRISCHOOLCD ";

        return $query ;
    }

    //塾名
    function getPriname($model) {
        $query  = "";
        $query .= "  SELECT ";
        $query .= "      PRISCHOOLCD || ':' || PRISCHOOL_NAME ";
        $query .= "  FROM ";
        $query .= "      PRISCHOOL_MST ";
        $query .= "  WHERE ";
        $query .= "      PRISCHOOLCD = '{$model->prischoolCd}' ";
        $query .= "  ORDER BY ";
        $query .= "      PRISCHOOLCD ";

        return $query ;
    }

    //１レコード取得
    function getRow($model, $priClassCd) {
        $db = Query::dbCheckOut();
        $query = "SELECT * FROM PRISCHOOL_CLASS_MST WHERE PRISCHOOLCD = '{$model->prischoolCd}' AND PRISCHOOL_CLASS_CD = '{$priClassCd}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //路線名
    function getStationName($line_cd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     LINE_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     LINE_CD = '{$line_cd}' ";

        return $query;
    }

    //地区コード
    function getDistinct() {
        return "SELECT DISTINCT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z003' ORDER BY NAMECD2";
    }

    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["PRISCHOOLCD"][TEXT]          = $model->prischoolCd;
        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $fields["PRISCHOOL_CLASS_CD"];
        $data["PRISCHOOL_NAME"][TEXT]       = $fields["PRISCHOOL_NAME"];
        $data["PRISCHOOL_KANA"][TEXT]       = $fields["PRISCHOOL_KANA"];
        $data["PRINCNAME"][TEXT]            = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]       = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]            = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]           = $fields["DISTRICTCD"];
        $data["PRISCHOOL_ZIPCD"][TEXT]      = $fields["PRISCHOOL_ZIPCD"];
        $data["PRISCHOOL_ADDR1"][TEXT]      = $fields["PRISCHOOL_ADDR1"];
        $data["PRISCHOOL_ADDR2"][TEXT]      = $fields["PRISCHOOL_ADDR2"];
        $data["PRISCHOOL_TELNO"][TEXT]      = $fields["PRISCHOOL_TELNO"];
        $data["PRISCHOOL_FAXNO"][TEXT]      = $fields["PRISCHOOL_FAXNO"];
        for ($i = 1; $i <= 5; $i++) {
            $rosen = $fields["HIDDEN_ROSEN_".$i];
            $data["ROSEN_".$i][TEXT]           = $rosen;            //路線1
        }
        $data["NEAREST_STATION_NAME1"][TEXT] = $fields["NEAREST_STATION_NAME1"];
        $data["NEAREST_STATION_KANA1"][TEXT] = $fields["NEAREST_STATION_KANA1"];
        $data["NEAREST_STATION_NAME2"][TEXT] = $fields["NEAREST_STATION_NAME2"];
        $data["NEAREST_STATION_KANA2"][TEXT] = $fields["NEAREST_STATION_KANA2"];
        $data["DIRECT_MAIL_FLG"][TEXT]       = $fields["DIRECT_MAIL_FLG"];
        $data["REGISTERCD"][TEXT]            = STAFFCD;
        $data["UPDATED"][FUNC]               = "sysdate()";

        $query = Query::insertSQL($data, "PRISCHOOL_CLASS_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["PRISCHOOLCD"][TEXT]          = $model->prischoolCd;
        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $fields["PRISCHOOL_CLASS_CD"];
        $data["PRISCHOOL_NAME"][TEXT]       = $fields["PRISCHOOL_NAME"];
        $data["PRISCHOOL_KANA"][TEXT]       = $fields["PRISCHOOL_KANA"];
        $data["PRINCNAME"][TEXT]            = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]       = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]            = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]           = $fields["DISTRICTCD"];
        $data["PRISCHOOL_ZIPCD"][TEXT]      = $fields["PRISCHOOL_ZIPCD"];
        $data["PRISCHOOL_ADDR1"][TEXT]      = $fields["PRISCHOOL_ADDR1"];
        $data["PRISCHOOL_ADDR2"][TEXT]      = $fields["PRISCHOOL_ADDR2"];
        $data["PRISCHOOL_TELNO"][TEXT]      = $fields["PRISCHOOL_TELNO"];
        $data["PRISCHOOL_FAXNO"][TEXT]      = $fields["PRISCHOOL_FAXNO"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        for ($i = 1; $i <= 5; $i++) {
            $rosen = $fields["HIDDEN_ROSEN_".$i];
            $data["ROSEN_".$i][TEXT]           = $rosen;            //路線1
        }
        $data["NEAREST_STATION_NAME1"][TEXT] = $fields["NEAREST_STATION_NAME1"];
        $data["NEAREST_STATION_KANA1"][TEXT] = $fields["NEAREST_STATION_KANA1"];
        $data["NEAREST_STATION_NAME2"][TEXT] = $fields["NEAREST_STATION_NAME2"];
        $data["NEAREST_STATION_KANA2"][TEXT] = $fields["NEAREST_STATION_KANA2"];
        $data["DIRECT_MAIL_FLG"][TEXT]       = $fields["DIRECT_MAIL_FLG"];

        $where  = "WHERE PRISCHOOLCD = '{$model->prischoolCd}' ";
        $where .= "      AND PRISCHOOL_CLASS_CD = '{$fields["PRISCHOOL_CLASS_CD"]}' ";
        $query = Query::updateSQL($data, "PRISCHOOL_CLASS_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $query = "DELETE FROM PRISCHOOL_CLASS_MST WHERE PRISCHOOLCD = '{$model->prischoolCd}' AND PRISCHOOL_CLASS_CD = '{$fields["PRISCHOOL_CLASS_CD"]}'";
        $query;
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

/*** Rosen.phpで使用 ***/

    //エリア名取得
    function getArea() {
        $query  = " SELECT ";
        $query .= "     AREA_NAME AS LABEL, ";
        $query .= "     AREA_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     AREA_MST ";
        $query .= " ORDER BY ";
        $query .= "     INT(AREA_CD) ";

        return $query;
    }

    //とあるエリアの各路線を取得
    function getRosen($area_cd) {
        if ($area_cd == '3') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '21') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '22') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '23') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '24') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '25') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '26') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '27') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '28') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '29') ";
        } elseif ($area_cd == '5') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '30') ";
        } elseif ($area_cd == '6') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '31') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '32') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '33') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '34') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '35') ";
        } elseif ($area_cd == '9') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '36') ";
        } else {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
        }
            $query .= " ORDER BY ";
            $query .= "     RR_CD, ";
            $query .= "     LINE_SORT ";

        return $query;
    }

}
?>
