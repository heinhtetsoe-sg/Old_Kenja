<?php

require_once('for_php7.php');
class knjh010Query extends Query {
    //生徒環境調査データ取得
    function getSchreg_envir_dat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM SCHREG_ENVIR_DAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' ";
//NO002
//        $query .= "     AND YEAR = '".CTRL_YEAR."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //生徒環境調査データ取得(subfrm)
    function getSchregsub_envir_dat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM SCHREG_ENVIR_DAT ";
        $query .= "WHERE SCHREGNO = '".$model->sch."' ";
//NO002
//        $query .= "      AND YEAR = '".$model->year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //名称マスタから名称取得
    function getV_name_mst($namecd,$model)
    {
        $query  = "SELECT * FROM V_NAME_MST ";
        $query .= "WHERE NAMECD1 = '".$namecd."' AND ";
        $query .= "      YEAR = '".$model->year."'";

        return $query;
    }
    //駅マスタから名称取得
    function getstation_mst($model)
    {
        $query  = "SELECT ";
        $query .= "    t1.STATIONCD,t1.STATIONNAME ";
        $query .= "FROM ";
        $query .= "    STATION_MST t1, ";
        $query .= "    STATION_YDAT t2 ";
        $query .= "WHERE ";
        $query .= "    t2.YEAR = '".$model->year."' ";
        $query .= "    AND t1.STATIONCD = t2.STATIONCD ";

        return $query;
    }
    //SCHREG_ENVIR_DATを削除
    function InsCount($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) AS INCHECK FROM SCHREG_ENVIR_DAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' ";
//NO002
//        $query .= "      AND YEAR = '".CTRL_YEAR."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
     }
    //SCHREG_ENVIR_DATを削除
    function getDeleteQuery($model)
    {
//        $row = knjh010Query::InsCount($model);
//        if ($row["INCHECK"] == 0){
            $db = Query::dbCheckOut();

            $query  = "DELETE FROM SCHREG_ENVIR_DAT ";
            $query .= "WHERE SCHREGNO = '".$model->schregno."' ";
//NO002
//            $query .= "      AND YEAR = '".$model->year."'";

            $db->query($query);
            Query::dbCheckIn($db);
//        }
        return;
     }
     //SCHREG_ENVIR_DATを追加
     function getInsertQuery($model,$field)
     {
        $row = knjh010Query::InsCount($model);

        $db = Query::dbCheckOut();
//NO002
//        $data["YEAR"][TEXT]                 = $model->year;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["MERITS"][TEXT]               = $field["MERITS"];
        $data["DEMERITS"][TEXT]             = $field["DEMERITS"];
        $data["GOOD_SUBJECT"][TEXT]         = $field["GOOD_SUBJECT"];
        $data["BAD_SUBJECT"][TEXT]          = $field["BAD_SUBJECT"];
        $data["HOBBY"][TEXT]                = $field["HOBBY"];
        $data["OLD_CRAM"][TEXT]             = $field["OLD_CRAM"];
        $data["CUR_CRAMCD"][TEXT]           = sprintf("%d",$field["CUR_CRAMCD"]);
        $data["CUR_CRAM"][TEXT]             = $field["CUR_CRAM"];
        $data["LESSONCD"][TEXT]             = sprintf("%d",$field["LESSONCD"]);
        $data["LESSON"][TEXT]               = $field["LESSON"];
        $data["PRIZES"][TEXT]               = $field["PRIZES"];
        $data["BRO_SISCD"][TEXT]            = sprintf("%d",$field["BRO_SISCD"]);
        $data["READING"][TEXT]              = $field["READING"];
        $data["SPORTS"][TEXT]               = $field["SPORTS"];
        $data["FRIENDSHIP"][TEXT]           = $field["FRIENDSHIP"];
        $data["PLANUNIV"][TEXT]             = $field["PLANUNIV"];
        $data["PLANJOB"][TEXT]              = $field["PLANJOB"];
        $data["ED_ACT"][TEXT]               = $field["ED_ACT"];
        $data["HOWTOCOMMUTECD"][TEXT]       = sprintf("%d",$field["HOWTOCOMMUTECD"]);
        $data["COMMUTE_HOURS"][TEXT]        = sprintf("%02d",$field["COMMUTE_HOURS"]);
        $data["COMMUTE_MINUTES"][TEXT]        = sprintf("%02d",$field["COMMUTE_MINUTES"]);
        $data["OTHERHOWTOCOMMUTE"][TEXT]    = $field["OTHERHOWTOCOMMUTE"];
        $data["STATIONNAME"][TEXT]            = $field["STATIONNAME"];
        $data["CHANGETRAIN1"][TEXT]           = $field["CHANGETRAIN1"];
        $data["CHANGETRAIN2"][TEXT]           = $field["CHANGETRAIN2"];
        $data["CHANGETRAIN3"][TEXT]           = $field["CHANGETRAIN3"];
        $data["CHANGETRAIN4"][TEXT]           = $field["CHANGETRAIN4"];
        $data["CHANGETRAIN5"][TEXT]           = $field["CHANGETRAIN5"];
        $data["CHANGETRAIN6"][TEXT]           = $field["CHANGETRAIN6"];
        $data["CHANGETRAIN7"][TEXT]           = $field["CHANGETRAIN7"];
        $data["CHANGETRAIN8"][TEXT]           = $field["CHANGETRAIN8"];

        $data["STUDYTIME"][TEXT]            = sprintf("%d",$field["STUDYTIME"]);
        $data["REMARK"][TEXT]               = $field["REMARK"];
        $data["RESIDENTCD"][TEXT]           = sprintf("%d",$field["RESIDENTCD"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($row["INCHECK"] > 0){
//NO002
//            $where  = " WHERE YEAR        = '" .CTRL_YEAR. "'";
//            $where .= " AND SCHREGNO      = '" .$model->schregno."'";
            $where  = " WHERE SCHREGNO      = '" .$model->schregno."'";
            $db->query(Query::updateSQL($data, "SCHREG_ENVIR_DAT", $where));
        }else {
            $db->query(Query::insertSQL($data, "SCHREG_ENVIR_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
     }

     //SCHREG_ENVIR_DATを更新
     function getUpdateQuery($model,$field)
     {
        $row = knjh010Query::InsCount($model);

        $db = Query::dbCheckOut();

        $data["DISEASE"][TEXT]              = $field["DISEASE"];
        $data["HEALTHCONDITION"][TEXT]      = $field["HEALTHCONDITION"];
        $data["POCKETMONEYCD"][TEXT]        = sprintf("%d",$field["POCKETMONEYCD"]);
        $data["POCKETMONEY"][NUMBER]        = $field["POCKETMONEY"];
        if(is_numeric($field["BEDTIME_HOURS"])){
            $data["BEDTIME_HOURS"][TEXT]          = sprintf("%02d",$field["BEDTIME_HOURS"]);
        }else{
            $data["BEDTIME_HOURS"][FUNC]          = "null";
        }
        if(is_numeric($field["BEDTIME_MINUTES"])){
            $data["BEDTIME_MINUTES"][TEXT]          = sprintf("%02d",$field["BEDTIME_MINUTES"]);
        }else{
            $data["BEDTIME_MINUTES"][FUNC]          = "null";
        }
        if(is_numeric($field["RISINGTIME_HOURS"])){
            $data["RISINGTIME_HOURS"][TEXT]       = sprintf("%02d",$field["RISINGTIME_HOURS"]);
        }else{
            $data["RISINGTIME_HOURS"][FUNC]        = "null";
        }
        if(is_numeric($field["RISINGTIME_MINUTES"])){
            $data["RISINGTIME_MINUTES"][TEXT]       = sprintf("%02d",$field["RISINGTIME_MINUTES"]);
        }else{
            $data["RISINGTIME_MINUTES"][FUNC]        = "null";
        }
        $data["TVVIEWINGHOURSCD"][TEXT]     = sprintf("%d",$field["TVVIEWINGHOURSCD"]);
        $data["TVPROGRAM"][TEXT]            = $field["TVPROGRAM"];
        $data["PC_HOURS"][TEXT]             = sprintf("%d",$field["PC_HOURS"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($row["INCHECK"] > 0){
//NO002
//            $where  = " WHERE YEAR        = '" .CTRL_YEAR. "'";
//            $where .= " AND SCHREGNO      = '" .$model->schregno."'";
            $where  = " WHERE SCHREGNO      = '" .$model->schregno."'";
            $db->query(Query::updateSQL($data, "SCHREG_ENVIR_DAT", $where));
        }else {
//NO002
//            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $db->query(Query::insertSQL($data, "SCHREG_ENVIR_DAT"));
        }
        Query::dbCheckIn($db);
        return ;
     }

}
?>
