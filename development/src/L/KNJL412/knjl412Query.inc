<?php

require_once('for_php7.php');
class knjl412Query extends Query {
    //分類取得
    function getRecruitClass($model) {
        $query  = " SELECT ";
        $query .= "     EVENT_CLASS_CD || ':' || EVENT_CLASS_NAME LABEL, ";
        $query .= "     EVENT_CLASS_CD VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_CLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";
        $query .= "     AND EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        $query .= " ORDER BY ";
        $query .= "     EVENT_CD ";

        return $query;
    }

    //データ取得
    function getRecruitEventYmst($model, $getDiv = "") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";
        $query .= "     AND EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        if ($getDiv == "CHECK") {
            $query .= "     AND EVENT_CD = '{$model->field["EVENT_CD"]}' ";
        } else {
            $query .= "     AND EVENT_CD = '{$model->leftEventCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     EVENT_CD ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]             = $model->leftYear;
        $data["EVENT_CLASS_CD"][TEXT]   = $model->leftEventClassCd;
        $data["EVENT_CD"][TEXT]         = $fields["EVENT_CD"];
        $data["EVENT_NAME"][TEXT]       = $fields["EVENT_NAME"];
        $data["EVENT_ABBV"][TEXT]       = $fields["EVENT_ABBV"];
        $data["REMARK"][TEXT]           = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "RECRUIT_EVENT_YMST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["EVENT_NAME"][TEXT]   = $fields["EVENT_NAME"];
        $data["EVENT_ABBV"][TEXT]   = $fields["EVENT_ABBV"];
        $data["REMARK"][TEXT]       = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '".$model->leftYear."' ";
        $query .= "   AND EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        $where .= "   AND EVENT_CD  = '".$fields["EVENT_CD"]."' ";

        $query = Query::updateSQL($data, "RECRUIT_EVENT_YMST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->leftYear."' ";
        $query .= "     AND EVENT_CLASS_CD = '{$model->leftEventClassCd}' ";
        $query .= "     AND EVENT_CD = '".$fields["EVENT_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //データ件数取得
    function getRecruitEventYmstCopy($year, $div) {
        $query  = " SELECT ";
        if ($div == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //コピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $preYear = $model->leftYear-1;
        $result = $db->query(knjl412Query::getRecruitEventYmstCopy($preYear, "data"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->leftYear;
            $data["EVENT_CLASS_CD"][TEXT]   = $row["EVENT_CLASS_CD"];
            $data["EVENT_CD"][TEXT]         = $row["EVENT_CD"];
            $data["EVENT_NAME"][TEXT]       = $row["EVENT_NAME"];
            $data["EVENT_ABBV"][TEXT]       = $row["EVENT_ABBV"];
            $data["REMARK"][TEXT]           = $row["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "RECRUIT_EVENT_YMST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
