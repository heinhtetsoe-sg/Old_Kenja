<?php

require_once('for_php7.php');

class knjz281Query extends Query {

    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JOB_MST ";
        $query .= " ORDER BY  ";
        $query .= "     JOBCD ";

        return $query;
    }

    //１レコード取得
    function getRow($jobcd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JOB_MST ";
        $query .= " WHERE ";
        $query .= "     JOBCD = '".$jobcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //学校設定職名コードチェック
    function getEdboardJobDat($jobcd, $flg="") {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     EDBOARD_JOB_DAT ";
        $query .= " WHERE ";
        $query .= "     JOBCD = '".$jobcd."' ";
        if ($flg) {
            $query .= "     AND EDBOARD_FLG = '2' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //UPDATE 
    function &getUpdateQuery($fields, $div) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($div == "add") {
            $data["JOBCD"][TEXT]      = $fields["JOBCD"];
        }
        $data["JOBNAME"][TEXT]        = $fields["JOBNAME"];
        $data["BASE_JOBNAME"][TEXT]     = $fields["BASE_JOBNAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "JOB_MST");
            $db->query($query);
        } else {
            $where = " WHERE JOBCD  = '".$fields["JOBCD"]."' ";
            $query = Query::updateSQL($data, "JOB_MST", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JOB_MST ";
        $query .= " WHERE ";
        $query .= "     JOBCD = '".$fields["JOBCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
