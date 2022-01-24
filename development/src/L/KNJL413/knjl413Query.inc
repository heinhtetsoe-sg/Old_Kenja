<?php

require_once('for_php7.php');

class knjl413Query extends Query {

    //分類取得
    function getRecruitClass() {
        $query  = " SELECT ";
        $query .= "     EVENT_CLASS_CD || ':' || EVENT_CLASS_NAME LABEL, ";
        $query .= "     EVENT_CLASS_CD VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_CLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //イベント取得
    function getEvent($model) {
        $query  = " SELECT ";
        $query .= "     EVENT_CD || ':' || EVENT_NAME LABEL, ";
        $query .= "     EVENT_CD VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR+1)."' ";
        $query .= "     AND EVENT_CLASS_CD = '{$model->field["EVENT_CLASS_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //送付名一覧取得
    function getRecruitSendYmst($model) {
        $query  = " SELECT ";
        $query .= "     SEND_CD || ':' || VALUE(SEND_PRGID,'') AS VALUE, ";
        $query .= "     SEND_CD || ':' || SEND_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECRUIT_SEND_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR+1)."' ";
        $query .= "     AND EVENT_CLASS_CD = '{$model->field["EVENT_CLASS_CD"]}' ";
        $query .= "     AND EVENT_CD = '{$model->field["EVENT_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getRecruitDat() {
        $query  = " SELECT ";
        $query .= "     RECRUIT_NO AS VALUE, ";
        $query .= "     RECRUIT_NO || '：' || NAME AS LABEL, ";
        $query .= "     EMAIL ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR+1)."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";


        return $query;
    }

    //案内取得
    function getL403() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR+1)."' ";
        $query .= "     AND NAMECD1 = 'L403' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";


        return $query;
    }

    //--- INSERT
    function &getInsertSendHitsQuery($fields, $model, $recruit_no, $max) {
        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $fields["SEND_DATE"]);
        list ($send_cd, $prgid) = explode(':', $fields["SEND_CD_PRGID"]);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR+1;
        $data["EVENT_CLASS_CD"][TEXT]   = $fields["EVENT_CLASS_CD"];
        $data["EVENT_CD"][TEXT]         = $fields["EVENT_CD"];
        $data["SEND_CD"][TEXT]          = $send_cd;
        $data["SEND_COUNT"][TEXT]       = $max;
        $data["SEND_DATE"][TEXT]        = $date;
        $data["SEND_METHOD"][TEXT]      = $fields["SEND_METHOD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "RECRUIT_SEND_HIST_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return $max + 1;
    }

    //--- INSERT
    function &getInsertSendHitsStdQuery($fields, $model, $recruit_no, $max) {
        $db = Query::dbCheckOut();

        list ($send_cd, $prgid) = explode(':', $fields["SEND_CD_PRGID"]);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR+1;
        $data["EVENT_CLASS_CD"][TEXT]   = $fields["EVENT_CLASS_CD"];
        $data["EVENT_CD"][TEXT]         = $fields["EVENT_CD"];
        $data["SEND_CD"][TEXT]          = $send_cd;
        $data["SEND_COUNT"][TEXT]       = $max;
        $data["RECRUIT_NO"][TEXT]       = $recruit_no;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "RECRUIT_SEND_HIST_STD_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //RECRUIT_SEND_HIST_DAT取得
    function getRecruitSendHistDat($fields, $div) {

        list ($send_cd, $prgid) = explode(':', $fields["SEND_CD_PRGID"]);

        $query  = " SELECT ";
        if ($div == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     MAX(SEND_COUNT) ";
        }
        $query .= " FROM ";
        $query .= "     RECRUIT_SEND_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR+1)."' AND ";
        $query .= "     EVENT_CLASS_CD  = '".$fields["EVENT_CLASS_CD"]."' AND ";
        $query .= "     EVENT_CD        = '".$fields["EVENT_CD"]."' AND ";
        $query .= "     SEND_CD         = '".$send_cd."' ";

        return $query;
    }
}
?>
