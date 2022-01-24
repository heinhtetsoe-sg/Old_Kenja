<?php

require_once('for_php7.php');
class knjh400_SeitoKankyou2Query extends Query
{
    //生徒環境調査データ取得
    public function getSchregEnvirDat($model)
    {
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

    //生徒環境調査データ取得(subfrm)
    public function getSchregsubEnvirDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->sch}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //名称マスタから名称取得
    public function getVNameMst($namecd, $model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM
                        V_NAME_MST ";
        $query .= " WHERE
                        NAMECD1 = '{$namecd}' ";
        $query .= " AND YEAR    = '{$model->year}'";
        $query .= " ORDER BY ";
        $query .= "     ABBV3, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //災害時帰宅グループ
    public function getGoHome()
    {
        $query  = " SELECT ";
        $query .= "     GO_HOME_GROUP_NO || ':' || GO_HOME_GROUP_NAME AS LABEL, ";
        $query .= "     GO_HOME_GROUP_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     GO_HOME_GROUP_MST ";

        return $query;
    }

    //責任者
    public function getResponsibility()
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H109' ";

        return $query;
    }

    //通学経路登録（スクールバス）対象の通学方法取得
    public function getSchoolBus($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     NAMECD1     = 'H100' AND ";
        $query .= "     NAMESPARE1  = '1' ";

        return $query;
    }

    //路線名、駅名を取得
    public function getStationName($station_cd)
    {
        $query  = " SELECT ";
        $query .= "     STATION_NAME, ";
        $query .= "     LINE_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     STATION_CD = '{$station_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     STATION_SORT, ";
        $query .= "     STATION_CD ";

        return $query;
    }

    //エリア名取得
    public function getArea()
    {
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
    public function getRosen($area_cd)
    {
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

    //とある路線の各駅を取得
    public function getStation($rosen_cd)
    {
        $query  = " SELECT ";
        $query .= "     STATION_NAME AS LABEL, ";
        $query .= "     STATION_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     LINE_CD = '{$rosen_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     STATION_SORT, ";
        $query .= "     STATION_CD ";

        return $query;
    }

    //通学方法名称取得
    public function getHowToCommutecdName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H100' AND ";
        $query .= "     NAMECD2 = '1' ";

        return $query;
    }

    //バスコース取得
    public function getBusCourse($bus_course = "")
    {
        $query  = " SELECT ";
        $query .= "     COURSE_CD AS VALUE, ";
        $query .= "     BUS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_BUS_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($bus_course == "") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= " AND COURSE_CD = '".$bus_course."' ";
        }

        return $query;
    }

    //SCHREG_ENVIR_DATにレコードがあるかどうかチェック
    public function insCount($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS INCHECK ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
}
