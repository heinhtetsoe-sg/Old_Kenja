<?php

require_once('for_php7.php');

class knjd130oQuery extends Query {

    //学期コンボ取得---NO001
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

        $query  = " SELECT schregno, specialactremark, communication ";//NO002
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '".$semester."' ";//NO001
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //１レコード取得//NO002
    function getTrainRow2($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT schregno, totalstudytime ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '9' ";
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
        $data["SEMESTER"][TEXT]            = $semester;//NO001
        $data["SCHREGNO"][TEXT]            = $schregno;
//        $data["TOTALSTUDYTIME"][TEXT]      = $fields["TOTALSTUDYTIME"];//NO002
        $data["SPECIALACTREMARK"][TEXT]      = $fields["SPECIALACTREMARK"];//NO002
        $data["COMMUNICATION"][TEXT]       = $fields["COMMUNICATION"];
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
        $query .= "   AND semester = '".$semester."' ";//NO001
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

    //INSERT//NO002
    function &getInsertQuery2($fields,$schregno)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = "9";
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["TOTALSTUDYTIME"][TEXT]      = $fields["TOTALSTUDYTIME"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }

   //DELETE//NO002
   function &getDeleteQuery2($schregno)
   {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM hreportremark_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND semester = '9' ";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

}
?>
