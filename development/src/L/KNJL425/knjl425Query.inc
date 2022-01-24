<?php

require_once('for_php7.php');

class knjl425Query extends Query {

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.EVENT_CLASS_CD, ";
        $query .= "     T1.EVENT_CLASS_NAME ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_CLASS_MST T1";
        $query .= " ORDER BY ";
        $query .= "     T1.EVENT_CLASS_CD ";

        return $query;
    }

    //データ取得
    function getRecruitSendYmst($model, $getDiv = "") {
        $query  = " SELECT ";
        $query .= "     T1.EVENT_CLASS_CD, ";
        $query .= "     T1.EVENT_CLASS_NAME ";
        $query .= " FROM ";
        $query .= "     RECRUIT_PS_CLASS_MST T1";
        $query .= " WHERE ";
        if ($getDiv == "CHECK") {
            $query .= "     T1.EVENT_CLASS_CD = '{$model->field["EVENT_CLASS_CD"]}' ";
        } else {
            $query .= "     T1.EVENT_CLASS_CD = '{$model->leftSendCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EVENT_CLASS_CD ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["EVENT_CLASS_CD"][TEXT]   = $fields["EVENT_CLASS_CD"];
        $data["EVENT_CLASS_NAME"][TEXT] = $fields["EVENT_CLASS_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "RECRUIT_PS_CLASS_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["EVENT_CLASS_NAME"][TEXT] = $fields["EVENT_CLASS_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE EVENT_CLASS_CD   = '".$fields["EVENT_CLASS_CD"]."' ";

        $query = Query::updateSQL($data, "RECRUIT_PS_CLASS_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_PS_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     EVENT_CLASS_CD = '".$fields["EVENT_CLASS_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
