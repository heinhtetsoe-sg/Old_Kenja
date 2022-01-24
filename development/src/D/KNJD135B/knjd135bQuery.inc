<?php

require_once('for_php7.php');

class knjd135bQuery extends Query {
    //学期コンボ取得
    function getSemesterQuery() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno, $semester) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SEMESTER = '{$semester}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields, $schregno, $semester) {
        $db = Query::dbCheckOut();
        $ctrl_year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$ctrl_year}' AND ";
        $query .= "     SEMESTER = '{$semester}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        $communication = $db->getOne($query);

        if ($communication > 0) {
            $data = array();
            $data["COMMUNICATION"][TEXT] = $fields["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $where  = " WHERE YEAR     = '{$ctrl_year}'";
            $where .= "   AND SEMESTER = '{$semester}'";
            $where .= "   AND SCHREGNO = '{$schregno}'";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        } else {
            $data = array();
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = $semester;
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["COMMUNICATION"][TEXT]       = $fields["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }
}
?>
