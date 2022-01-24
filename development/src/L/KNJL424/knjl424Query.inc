<?php

require_once('for_php7.php');

class knjl424Query extends Query {

    //分類取得
    function getRecruitClass() {
        $query  = " SELECT ";
        $query .= "     EVENT_CLASS_CD VALUE, ";
        $query .= "     EVENT_CLASS_CD || ':' || EVENT_CLASS_NAME LABEL ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_CLASS_MST ";
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
        $query .= "     RECRUIT_PS_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "         YEAR           = '".$model->leftYear."' ";
        $query .= "     AND EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //イベント毎のカウント
    function getEventCnt($model) {
        $query  = " SELECT ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_SEND_YMST T1";
        $query .= "     LEFT JOIN RECRUIT_PS_EVENT_YMST L1 ON L1.YEAR     = T1.YEAR ";
        $query .= "                                       AND L1.EVENT_CD = T1.EVENT_CD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR           = '{$model->leftYear}' ";
        $query .= "     AND T1.EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.EVENT_CD ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.EVENT_CD || ':' || L1.EVENT_NAME AS SET_EVENT, ";
        $query .= "     T1.SEND_CD, ";
        $query .= "     T1.SEND_NAME ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_SEND_YMST T1";
        $query .= "     LEFT JOIN RECRUIT_PS_EVENT_YMST L1 ON L1.YEAR           = T1.YEAR ";
        $query .= "                                       AND L1.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "                                       AND L1.EVENT_CD       = T1.EVENT_CD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR           = '{$model->leftYear}' ";
        $query .= "     AND T1.EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.SEND_CD ";

        return $query;
    }

    //データ取得
    function getRecruitSendYmst($model, $getDiv = "") {
        $query  = " SELECT ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.SEND_CD, ";
        $query .= "     T1.SEND_NAME ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_SEND_YMST T1";
        $query .= " WHERE ";
        $query .= "         T1.YEAR           = '{$model->leftYear}' ";
        $query .= "     AND T1.EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        if ($getDiv == "CHECK") {
            $query .= "     AND T1.EVENT_CD = '{$model->field["EVENT_CD"]}' ";
            $query .= "     AND T1.SEND_CD  = '{$model->field["SEND_CD"]}' ";
        } else {
            $query .= "     AND T1.EVENT_CD = '{$model->leftEventCd}' ";
            $query .= "     AND T1.SEND_CD  = '{$model->leftSendCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.SEND_CD ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]             = $model->leftYear;
        $data["EVENT_CLASS_CD"][TEXT]   = $model->leftEventClassCd;
        $data["EVENT_CD"][TEXT]         = $fields["EVENT_CD"];
        $data["SEND_CD"][TEXT]          = $fields["SEND_CD"];
        $data["SEND_NAME"][TEXT]        = $fields["SEND_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "RECRUIT_PS_SEND_YMST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SEND_NAME"][TEXT]    = $fields["SEND_NAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR           = '".$model->leftYear."' ";
        $where .= "   AND EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        $where .= "   AND EVENT_CD       = '{$model->leftEventCd}' ";
        $where .= "   AND SEND_CD        = '".$fields["SEND_CD"]."' ";

        $query = Query::updateSQL($data, "RECRUIT_PS_SEND_YMST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_PS_SEND_YMST ";
        $query .= " WHERE ";
        $query .= "         YEAR           = '".$model->leftYear."' ";
        $query .= "     AND EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        $query .= "     AND EVENT_CD       = '{$model->leftEventCd}' ";
        $query .= "     AND SEND_CD        = '".$fields["SEND_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //データ件数取得
    function getRecruitSendYmstCopy($year, $div) {
        $query  = " SELECT ";
        if ($div == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_SEND_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //コピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $preYear = $model->leftYear-1;
        $result = $db->query(knjl424Query::getRecruitSendYmstCopy($preYear, "data"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->leftYear;
            $data["EVENT_CLASS_CD"][TEXT]   = $model->leftEventClassCd;
            $data["EVENT_CD"][TEXT]         = $row["EVENT_CD"];
            $data["SEND_CD"][TEXT]          = $row["SEND_CD"];
            $data["SEND_NAME"][TEXT]        = $row["SEND_NAME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "RECRUIT_PS_SEND_YMST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
