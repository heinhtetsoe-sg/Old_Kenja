<?php

require_once('for_php7.php');

class knjd135cQuery extends Query {
    //１レコード取得
    function getTrainRow($schregno) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     COMMUNICATION, ";
        $query .= "     REMARK3 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '9'   AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno) {
        $db = Query::dbCheckOut();
        $ctrl_year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$ctrl_year}' AND ";
        $query .= "     SEMESTER = '9' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        $count = $db->getOne($query);

        if ($count > 0) {
            $data = array();
            $data["COMMUNICATION"][TEXT] = $fields["COMMUNICATION"];
            $data["REMARK3"][TEXT]       = $fields["REMARK3"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $where  = " WHERE YEAR     = '{$ctrl_year}'";
            $where .= "   AND SEMESTER = '9'";
            $where .= "   AND SCHREGNO = '{$schregno}'";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        } else {
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = '9';
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["COMMUNICATION"][TEXT] = $fields["COMMUNICATION"];
            $data["REMARK3"][TEXT]       = $fields["REMARK3"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }
}
?>
