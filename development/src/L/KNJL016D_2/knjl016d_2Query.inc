<?php

require_once('for_php7.php');

class knjl016d_2Query extends Query {

    //一覧用データ取得
    function selectQuery() {
        $query  = " SELECT ";
        $query .= "     DECISION_CD, ";
        $query .= "     DECISION_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERNAL_DECISION_MST ";
        $query .= " ORDER BY ";
        $query .= "     DECISION_CD ";

        return $query ;
    }

    //１レコード取得
    function getDecisionData($cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERNAL_DECISION_MST ";
        $query .= " WHERE ";
        $query .= "     DECISION_CD = '".$cd ."' ";

        return $query;
    }

/***** 追加更新処理 *****/
    //年度データの確認
    function IsExisting($decisionCd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERNAL_DECISION_YDAT ";
        $query .= " WHERE ";
        $query .= "     DECISION_CD = '".$decisionCd."' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["DECISION_CD"][TEXT]          = $fields["DECISION_CD"];
        $data["DECISION_NAME"][TEXT]        = $fields["DECISION_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "ENTEXAM_INTERNAL_DECISION_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["DECISION_CD"][TEXT]          = $fields["DECISION_CD"];
        $data["DECISION_NAME"][TEXT]        = $fields["DECISION_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = " WHERE DECISION_CD = '".$fields["DECISION_CD"]."' ";
        $query = Query::updateSQL($data, "ENTEXAM_INTERNAL_DECISION_MST", $where);

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_INTERNAL_DECISION_MST ";
        $query .= " WHERE DECISION_CD = '".$fields["DECISION_CD"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
?>
