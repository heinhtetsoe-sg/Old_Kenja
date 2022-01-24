<?php

require_once('for_php7.php');

class knjl421Query extends Query {

    //Z010
    function getZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //イベント参加者データ取得
    function getRecruitDat($model) {
        $query  = " SELECT ";
        $query .= "     RECRUIT_PS.*, ";
        $query .= "     PRI_001.REMARK1 AS STAFF_NAME, ";
        $query .= "     PRI_001.REMARK2 AS MOBILE_PHONE_NUMBER, ";
        $query .= "     PRI_001.REMARK3 AS EMAIL ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_DAT RECRUIT_PS ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_DETAIL_YDAT PRI_001 ON RECRUIT_PS.YEAR = PRI_001.YEAR ";
        $query .= "          AND RECRUIT_PS.PRISCHOOLCD = PRI_001.PRISCHOOLCD ";
        $query .= "          AND RECRUIT_PS.PRISCHOOL_CLASS_CD = PRI_001.PRISCHOOL_CLASS_CD ";
        $query .= "          AND PRI_001.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     RECRUIT_PS.YEAR        = '{$model->recruitYear}' AND ";
        $query .= "     RECRUIT_PS.RECRUIT_NO  = '{$model->recruit_no}' ";

        return $query;
    }

    //イベント参加登録データ取得
    function getRecruitEventDat($model, $div="") {

        $date = ($model->touroku_date == "") ? "" : str_replace("/", "-", $model->touroku_date);

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     S2.EVENT_CLASS_NAME, ";
        $query .= "     S1.EVENT_NAME, ";
        $query .= "     N1.NAME1 AS MEDIA_NAME, ";
        $query .= "     N2.NAME1 AS STATE_NAME ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_EVENT_DAT T1 ";
        $query .= "     LEFT JOIN RECRUIT_PS_EVENT_YMST S1 ON S1.YEAR      = T1.YEAR ";
        $query .= "                                    AND S1.EVENT_CLASS_CD  = T1.EVENT_CLASS_CD ";
        $query .= "                                    AND S1.EVENT_CD  = T1.EVENT_CD ";
        $query .= "     LEFT JOIN RECRUIT_PS_CLASS_MST S2 ON S2.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR      = T1.YEAR ";
        $query .= "                            AND N1.NAMECD1   = 'L411' ";
        $query .= "                            AND N1.NAMECD2   = T1.MEDIA_CD ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON N2.YEAR      = T1.YEAR ";
        $query .= "                            AND N2.NAMECD1   = 'L412' ";
        $query .= "                            AND N2.NAMECD2   = T1.STATE_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '{$model->recruitYear}' ";
        $query .= " AND T1.RECRUIT_NO   = '{$model->recruit_no}' ";
        if ($div) {
            $query .= " AND T1.TOUROKU_DATE = '{$date}' ";
            $query .= " AND T1.EVENT_CLASS_CD     = '{$model->event_class_cd}' ";
            $query .= " AND T1.EVENT_CD     = '{$model->event_cd}' ";
            $query .= " AND T1.MEDIA_CD     = '{$model->media_cd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.TOUROKU_DATE DESC, ";
        $query .= "     T1.EVENT_CLASS_CD, ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.MEDIA_CD ";

        return $query;
    }

    //分類取得
    function getRecruitClass() {
        $query  = " SELECT ";
        $query .= "     EVENT_CLASS_CD || ':' || EVENT_CLASS_NAME LABEL, ";
        $query .= "     EVENT_CLASS_CD VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_CLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //イベントマスタ取得
    function getRecruitEventYmst($model, $event_class_cd) {
        $query  = " SELECT ";
        $query .= "     EVENT_CD AS VALUE, ";
        $query .= "     EVENT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->recruitYear}' ";
        $query .= "     AND EVENT_CLASS_CD = '{$event_class_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->recruitYear}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function getRow_check($model) {

        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $model->field["TOUROKU_DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '{$model->recruitYear}' AND ";
        $query .= "     RECRUIT_NO      = '{$model->recruit_no}' AND ";
        $query .= "     TOUROKU_DATE    = '{$date}' AND ";
        $query .= "     EVENT_CLASS_CD  = '{$model->field["EVENT_CLASS_CD"]}' AND ";
        $query .= "     EVENT_CD        = '{$model->field["EVENT_CD"]}' AND ";
        $query .= "     MEDIA_CD        = '{$model->field["MEDIA_CD"]}' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    // UPDATE
    function &getUpdateQuery($model, $fields) {

        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $fields["TOUROKU_DATE"]);

        $data = array();
        $data["YEAR"][TEXT]                 = (CTRL_YEAR + 1);
        $data["RECRUIT_NO"][TEXT]           = $model->recruit_no;
        $data["TOUROKU_DATE"][TEXT]         = $date;
        $data["EVENT_CLASS_CD"][TEXT]       = $fields["EVENT_CLASS_CD"];
        $data["EVENT_CD"][TEXT]             = $fields["EVENT_CD"];
        $data["MEDIA_CD"][TEXT]             = $fields["MEDIA_CD"];
        $data["PRISCHOOLCD"][TEXT]          = $model->prischoolCd;
        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $model->prischoolClassCd;
        $data["STATE_CD"][TEXT]             = $fields["STATE_CD"];
        $data["ATTEND_MEETING_FLG"][TEXT]   = $fields["ATTEND_MEETING_FLG"];
        $data["DOC_REQ_NUMBER"][TEXT]       = $fields["DOC_REQ_NUMBER"];
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($model->cmd == "insert") {

            $db->query(Query::insertSQL($data, "RECRUIT_PS_EVENT_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR            = '{$model->recruitYear}' AND ";
            $where .= "     RECRUIT_NO      = '{$model->recruit_no}' AND ";
            $where .= "     TOUROKU_DATE    = '{$date}' AND ";
            $where .= "     EVENT_CLASS_CD  = '{$model->event_class_cd}' AND ";
            $where .= "     EVENT_CD        = '{$model->event_cd}' AND ";
            $where .= "     MEDIA_CD        = '{$model->media_cd}' ";

            $db->query(Query::updateSQL($data, "RECRUIT_PS_EVENT_DAT", $where));
        }
        Query::dbCheckIn($db);

        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $model->field["TOUROKU_DATE"]);

        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_PS_EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '{$model->recruitYear}' AND ";
        $query .= "     RECRUIT_NO      = '{$model->recruit_no}' AND ";
        $query .= "     TOUROKU_DATE    = '{$date}' AND ";
        $query .= "     EVENT_CLASS_CD  = '{$model->event_class_cd}' AND ";
        $query .= "     EVENT_CD        = '{$model->event_cd}' AND ";
        $query .= "     MEDIA_CD        = '{$model->media_cd}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return ;
    }
}
?>
