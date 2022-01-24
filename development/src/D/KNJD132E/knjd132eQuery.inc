<?php

require_once('for_php7.php');

class knjd132eQuery extends Query {

    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //HREPORT_CONDITION_DAT取得
    function getConditionDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->school_kind}' ";
        $query .= "     AND GRADE       = '".sprintf("%02d", "")."' ";
        $query .= "     AND COURSECD    = '".sprintf("%01d", "")."' ";
        $query .= "     AND MAJORCD     = '".sprintf("%03d", "")."' ";
        $query .= "     AND COURSECODE  = '".sprintf("%04d", "")."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SEQ) ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno, $model) {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     SPECIALACTREMARK, ";
        $query .= "     COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        return $query;
    }

    /********/
    /* 更新 */
    /********/
    //INSERT
    function &getInsertQuery($fields, $schregno, $model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = "9";
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["TOTALSTUDYTIME"][TEXT]   = $fields["TOTALSTUDYTIME"];
        $data["SPECIALACTREMARK"][TEXT] = $fields["SPECIALACTREMARK"];
        $data["COMMUNICATION"][TEXT]    = $fields["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));

        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno, $model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

}
?>
