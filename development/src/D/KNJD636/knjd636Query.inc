<?php

require_once('for_php7.php');

class knjd636Query extends Query {

    function getSemeter($year) {

        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($semester, $schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT schregno, totalstudytime ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '{$semester}' ";
        $query .= "    AND schregno = '{$schregno}'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = $fields["SEMESTER"];
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["TOTALSTUDYTIME"][TEXT]      = $fields["TOTALSTUDYTIME"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data, "hreportremark_dat");
        $db->query($query);
        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($semester, $schregno)
   {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM hreportremark_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER = '{$semester}'";
        $query .= "   AND SCHREGNO = '{$schregno}'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

}
?>
