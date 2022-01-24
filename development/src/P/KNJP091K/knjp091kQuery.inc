<?php

require_once('for_php7.php');

class knjp091kQuery extends Query {

    //年度データ取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query ;
    }

    //一覧用データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA_REPLACE ";
        $query .= " WHERE ";
        $query .= "     PAID_MONEY_DATE BETWEEN '".$model->exe_year."-04-01' AND '".($model->exe_year + 1)."-03-31' ";
        $query .= " ORDER BY ";
        $query .= "     PAID_MONEY_DATE DESC ";

        return $query ;
    }

    //１レコード取得
    function getRow($day) {
        $query  = " SELECT * FROM BANK_RESULT_TMP_DATA_REPLACE WHERE PAID_MONEY_DATE = '".str_replace("/", "-", $day)."'";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["PAID_MONEY_DATE"][TEXT]      = str_replace("/", "-", $fields["PAID_MONEY_DATE"]);
        $data["REPLACE_MONEY_DATE"][TEXT]   = str_replace("/", "-", $fields["REPLACE_MONEY_DATE"]);
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "BANK_RESULT_TMP_DATA_REPLACE");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();
        $data["REPLACE_MONEY_DATE"][TEXT]   = str_replace("/", "-", $fields["REPLACE_MONEY_DATE"]);
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = "WHERE PAID_MONEY_DATE = '".str_replace("/", "-", $fields["PAID_MONEY_DATE"])."'";
        $query = Query::updateSQL($data, "BANK_RESULT_TMP_DATA_REPLACE", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($db, $fields) {
        $query  = " DELETE FROM BANK_RESULT_TMP_DATA_REPLACE ";
        $query .= " WHERE PAID_MONEY_DATE  = '".str_replace("/", "-", $fields["PAID_MONEY_DATE"])."' ";

        $db->query($query);
        return true;
    }
}
?>
