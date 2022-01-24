<?php

require_once('for_php7.php');

class knjl420query extends Query {

    //募集データ
    function getPriSchoolInfo($model)
    {
        $query  = " WITH STATION AS ( ";
        $query .= " SELECT ";
        $query .= "     LINE_CD, ";
        $query .= "     MAX(LINE_NAME) AS LINE_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " GROUP BY ";
        $query .= "     LINE_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     PRI_CLASS.PRISCHOOLCD, ";
        $query .= "     PRI_CLASS.PRISCHOOL_CLASS_CD, ";
        $query .= "     PRI_M.PRISCHOOL_NAME, ";
        $query .= "     PRI_M.PRISCHOOL_KANA, ";
        $query .= "     PRI_M.PRINCNAME, ";
        $query .= "     PRI_CLASS.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME, ";
        $query .= "     PRI_CLASS.PRISCHOOL_KANA AS PRISCHOOL_CLASS_KANA, ";
        $query .= "     PRI_CLASS.PRINCNAME AS PRINCNAME_CLASS, ";
        $query .= "     PRI_CLASS.PRISCHOOL_ZIPCD, ";
        $query .= "     Z003.NAME1 AS DISTRICT_NAME, ";
        $query .= "     PRI_CLASS.PRISCHOOL_ADDR1, ";
        $query .= "     PRI_CLASS.PRISCHOOL_ADDR2, ";
        $query .= "     PRI_CLASS.PRISCHOOL_TELNO, ";
        $query .= "     PRI_CLASS.PRISCHOOL_FAXNO, ";
        $query .= "     STATION1.LINE_NAME AS ROSEN_1, ";
        $query .= "     STATION2.LINE_NAME AS ROSEN_2, ";
        $query .= "     STATION3.LINE_NAME AS ROSEN_3, ";
        $query .= "     STATION4.LINE_NAME AS ROSEN_4, ";
        $query .= "     STATION5.LINE_NAME AS ROSEN_5, ";
        $query .= "     PRI_CLASS.NEAREST_STATION_NAME1, ";
        $query .= "     PRI_CLASS.NEAREST_STATION_KANA1, ";
        $query .= "     PRI_CLASS.NEAREST_STATION_NAME2, ";
        $query .= "     PRI_CLASS.NEAREST_STATION_KANA2, ";
        $query .= "     PRI_CLASS.DIRECT_MAIL_FLG, ";
        $query .= "     PRI_001.REMARK1 AS STAFF_NAME, ";
        $query .= "     PRI_001.REMARK2 AS MOBILE_PHONE_NUMBER, ";
        $query .= "     PRI_001.REMARK3 AS EMAIL ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST PRI_CLASS ";
        $query .= "     INNER JOIN PRISCHOOL_MST PRI_M ON PRI_CLASS.PRISCHOOLCD = PRI_M.PRISCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST Z003 ON Z003.NAMECD1 = 'Z003' ";
        $query .= "          AND PRI_CLASS.DISTRICTCD = Z003.NAMECD2 ";
        $query .= "     LEFT JOIN STATION STATION1 ON PRI_CLASS.ROSEN_1 = STATION1.LINE_CD ";
        $query .= "     LEFT JOIN STATION STATION2 ON PRI_CLASS.ROSEN_2 = STATION2.LINE_CD ";
        $query .= "     LEFT JOIN STATION STATION3 ON PRI_CLASS.ROSEN_3 = STATION3.LINE_CD ";
        $query .= "     LEFT JOIN STATION STATION4 ON PRI_CLASS.ROSEN_4 = STATION4.LINE_CD ";
        $query .= "     LEFT JOIN STATION STATION5 ON PRI_CLASS.ROSEN_5 = STATION5.LINE_CD ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_DETAIL_YDAT PRI_001 ON PRI_001.YEAR = '{$model->recruitYear}' ";
        $query .= "          AND PRI_CLASS.PRISCHOOLCD = PRI_001.PRISCHOOLCD ";
        $query .= "          AND PRI_CLASS.PRISCHOOL_CLASS_CD = PRI_001.PRISCHOOL_CLASS_CD ";
        $query .= "          AND PRI_001.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     PRI_CLASS.PRISCHOOLCD = '{$model->prischoolCd}' ";
        $query .= "     AND PRI_CLASS.PRISCHOOL_CLASS_CD = '{$model->prischoolClassCd}' ";

        return $query;
    }

    function getRecruitPsDatCnt($model)
    {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "    RECRUIT_PS_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->recruitYear}' ";
        $query .= "    AND RECRUIT_NO = '{$model->recruitNo}' ";

        return $query;
    }

    //イベント取得
    function getEventInfoData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.TOUROKU_DATE, ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     EMST.EVENT_NAME, ";
        $query .= "     T1.MEDIA_CD, ";
        $query .= "     L1.NAME1 AS MEDIA_NAME, ";
        $query .= "     T1.STATE_CD, ";
        $query .= "     L2.NAME1 AS STATE_NAME, ";
        $query .= "     CASE WHEN T1.ATTEND_MEETING_FLG = '1' THEN 'レ' ELSE '' END AS ATTEND_MEETING_FLG, ";
        $query .= "     T1.DOC_REQ_NUMBER, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_EVENT_DAT T1 ";
        $query .= "     LEFT JOIN RECRUIT_PS_EVENT_YMST EMST ON EMST.YEAR = T1.YEAR ";
        $query .= "          AND EMST.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "          AND EMST.EVENT_CD = T1.EVENT_CD ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L411' ";
        $query .= "          AND L1.NAMECD2 = T1.MEDIA_CD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L412' ";
        $query .= "          AND L2.NAMECD2 = T1.STATE_CD ";
        $query .= " WHERE ";
        $query .= "     T1.RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TOUROKU_DATE DESC, ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.MEDIA_CD ";

        return $query;
    }

    //発送物一覧取得
    function getSendInfoData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.RECRUIT_NO, ";
        $query .= "     T1.EVENT_CLASS_CD, ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.SEND_CD, ";
        $query .= "     T1.SEND_COUNT, ";
        $query .= "     SMST.SEND_NAME, ";
        $query .= "     T1.SEND_COUNT, ";
        $query .= "     L1.NAME1 AS METHODNAME, ";
        $query .= "     SEND_HIST.SEND_DATE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_SEND_HIST_PS_DAT T1 ";
        $query .= "     LEFT JOIN RECRUIT_PS_SEND_HIST_DAT SEND_HIST ON SEND_HIST.YEAR = T1.YEAR ";
        $query .= "          AND SEND_HIST.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "          AND SEND_HIST.EVENT_CD = T1.EVENT_CD ";
        $query .= "          AND SEND_HIST.SEND_CD = T1.SEND_CD ";
        $query .= "          AND SEND_HIST.SEND_COUNT = T1.SEND_COUNT ";
        $query .= "     LEFT JOIN RECRUIT_PS_SEND_YMST SMST ON SMST.YEAR = T1.YEAR ";
        $query .= "          AND SMST.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "          AND SMST.EVENT_CD = T1.EVENT_CD ";
        $query .= "          AND SMST.SEND_CD = T1.SEND_CD ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L413' ";
        $query .= "          AND L1.NAMECD2 = SEND_HIST.SEND_METHOD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->recruitYear}' ";
        $query .= "     AND T1.RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= " ORDER BY ";
        $query .= "     SEND_HIST.SEND_DATE, ";
        $query .= "     T1.SEND_CD ";

        return $query;
    }

    function delDetail($model)
    {

        $query  = " DELETE FROM ";
        $query .= "     PRISCHOOL_CLASS_DETAIL_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->recruitYear}' ";
        $query .= "     AND PRISCHOOLCD = '{$model->prischoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$model->prischoolClassCd}' ";
        $query .= "     AND SEQ = '001' ";

        return $query;
    }

    function insDetail($model)
    {

        $data["YEAR"][TEXT]                 = $model->recruitYear;
        $data["PRISCHOOLCD"][TEXT]          = $model->prischoolCd;
        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $model->prischoolClassCd;
        $data["SEQ"][TEXT]                  = "001";
        $data["REMARK1"][TEXT]              = $model->field["STAFF_NAME"];
        $data["REMARK2"][TEXT]              = $model->field["MOBILE_PHONE_NUMBER"];
        $data["REMARK3"][TEXT]              = $model->field["EMAIL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "PRISCHOOL_CLASS_DETAIL_YDAT");

        return $query;
    }

    function updPriSchoolClass($model)
    {

        $data["DIRECT_MAIL_FLG"][TEXT]      = $model->field["DIRECT_MAIL_FLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "     PRISCHOOLCD = '{$model->prischoolCd}' ";
        $where .= "     AND PRISCHOOL_CLASS_CD = '{$model->prischoolClassCd}' ";
        $query = Query::updateSQL($data, "PRISCHOOL_CLASS_MST", $where);

        return $query;
    }

    function getPriSchoolClass($model)
    {

        $query  = " SELECT ";
        $query .= "     PRI_CLASS.*, ";
        $query .= "     PRI_001.REMARK1 AS STAFF_NAME, ";
        $query .= "     PRI_001.REMARK2 AS MOBILE_PHONE_NUMBER, ";
        $query .= "     PRI_001.REMARK3 AS EMAIL ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST PRI_CLASS ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_DETAIL_YDAT PRI_001 ON PRI_001.YEAR = '{$model->recruitYear}' ";
        $query .= "          AND PRI_CLASS.PRISCHOOLCD = PRI_001.PRISCHOOLCD ";
        $query .= "          AND PRI_CLASS.PRISCHOOL_CLASS_CD = PRI_001.PRISCHOOL_CLASS_CD ";
        $query .= "          AND PRI_001.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     PRI_CLASS.PRISCHOOLCD = '{$model->prischoolCd}' ";
        $query .= "     AND PRI_CLASS.PRISCHOOL_CLASS_CD = '{$model->prischoolClassCd}' ";

        return $query;
    }

    /**
     * 削除 RECRUIT_DAT
     */
    function delRecruitPsDat($model)
    {

        $query  = "DELETE FROM ";
        $query .= "    RECRUIT_PS_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->recruitYear}' ";
        $query .= "    AND RECRUIT_NO = '{$model->recruitNo}' ";

        return $query;
    }

    /**
     * 新規 RECRUIT_DAT
     */
    function insRecruitPsDat($model)
    {

        $data["YEAR"][TEXT]                 = $model->recruitYear;
        $data["RECRUIT_NO"][TEXT]           = $model->recruitNo;
        $data["NAME"][TEXT]                 = $model->priClassInfo["PRISCHOOL_NAME"];
        $data["NAME_KANA"][TEXT]            = $model->priClassInfo["PRISCHOOL_KANA"];
        $data["PRISCHOOLCD"][TEXT]          = $model->prischoolCd;
        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $model->prischoolClassCd;
        $data["PRISCHOOL_TEACHER"][TEXT]    = $model->priClassInfo["PRINCNAME"];
        $data["ZIPCD"][TEXT]                = $model->priClassInfo["PRISCHOOL_ZIPCD"];
        $data["ADDR1"][TEXT]                = $model->priClassInfo["PRISCHOOL_ADDR1"];
        $data["ADDR2"][TEXT]                = $model->priClassInfo["PRISCHOOL_ADDR2"];
        $data["TELNO"][TEXT]                = $model->priClassInfo["PRISCHOOL_TELNO"];
        $data["TELNO2"][TEXT]               = $model->priClassInfo["MOBILE_PHONE_NUMBER"];
        $data["FAXNO"][TEXT]                = $model->priClassInfo["PRISCHOOL_FAXNO"];
        $data["EMAIL"][TEXT]                = $model->priClassInfo["EMAIL"];
        $data["REMARK"][TEXT]               = $model->priClassInfo["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "RECRUIT_PS_DAT");

        return $query;
    }

    /**
     * 削除 RECRUIT_DAT
     */
    function delSendHistStdDat($model, $key)
    {

        $query  = " DELETE FROM ";
        $query .= "    RECRUIT_PS_SEND_HIST_PS_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR || '-' || EVENT_CLASS_CD || '-' || EVENT_CD || '-' || SEND_CD || '-' || SEND_COUNT || '-' || RECRUIT_NO = '{$key}' ";
        return $query;
    }

}
?>
