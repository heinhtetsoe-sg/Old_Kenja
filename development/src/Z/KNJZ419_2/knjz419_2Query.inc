<?php

require_once('for_php7.php');

class knjz419_2Query extends Query {
    //リスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_MST ";
        $query .= " ORDER BY ";
        $query .= "     QUESTIONNAIRECD ";

        return $query;
    }

    //校時名称取得
    function getName() {
        return " SELECT namecd2,name1 FROM name_mst WHERE namecd1 = 'B001' ORDER BY namecd2 ";
    }

    //１レコード取得
    function getRow($questionnairecd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_MST ";
        $query .= " WHERE ";
        $query .= "     QUESTIONNAIRECD = '{$questionnairecd}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度データの確認
    function IsExisting($questionnairecd) {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM QUESTIONNAIRE_YDAT WHERE QUESTIONNAIRECD = '{$questionnairecd}'";
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    //--- INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["QUESTIONNAIRECD"][TEXT]   = $fields["QUESTIONNAIRECD"];
        $data["QUESTIONNAIRENAME"][TEXT] = $fields["QUESTIONNAIRENAME"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "QUESTIONNAIRE_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["QUESTIONNAIRECD"][TEXT]   = $fields["QUESTIONNAIRECD"];
        $data["QUESTIONNAIRENAME"][TEXT] = $fields["QUESTIONNAIRENAME"];

        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $where = " WHERE QUESTIONNAIRECD = '{$fields["QUESTIONNAIRECD"]}' ";

        $query = Query::updateSQL($data, "QUESTIONNAIRE_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM QUESTIONNAIRE_MST "
                ." WHERE QUESTIONNAIRECD  = '{$fields["QUESTIONNAIRECD"]}'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
