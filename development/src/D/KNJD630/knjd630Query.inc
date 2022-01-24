<?php

require_once('for_php7.php');

class knjd630Query extends Query {

    //学期コンボ取得
    function getSemesterQuery()
    {
        $query  = "SELECT SEMESTER, SEMESTERNAME ";
        $query .= "  FROM SEMESTER_MST ";
        $query .= " WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <> '9' ";
        $query .= "ORDER BY SEMESTER ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno, $semester)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT schregno, totalstudytime ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '".$semester."' ";
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno, $semester)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = $semester;
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["TOTALSTUDYTIME"][TEXT]      = $fields["TOTALSTUDYTIME"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno, $semester)
   {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM hreportremark_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER = '".$semester."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

}
?>
