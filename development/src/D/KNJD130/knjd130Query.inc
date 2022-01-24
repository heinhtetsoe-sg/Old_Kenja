<?php

require_once('for_php7.php');

class knjd130Query extends Query {

    //１レコード取得
    function getTrainRow($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT schregno, totalstudytime, communication ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '9' ";//NO001
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = "9";//NO001
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["TOTALSTUDYTIME"][TEXT]      = $fields["TOTALSTUDYTIME"];
        $data["COMMUNICATION"][TEXT]       = $fields["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno)
   {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM hreportremark_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER = '9'";//NO001
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

}
?>
