<?php

require_once('for_php7.php');

class knjd130tQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJD130T' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getProgrampath() {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID ='KNJD130T' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJD130T' ";

        return $query;
    }

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

        $query  = " SELECT schregno, totalstudytime, specialactremark, communication ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '".$semester."' ";//NO001
        $query .= "    AND schregno = '".$schregno."'";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno, $semester)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = $semester;//NO001
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["TOTALSTUDYTIME"][TEXT]      = $fields["TOTALSTUDYTIME"];
        $data["SPECIALACTREMARK"][TEXT]    = $fields["SPECIALACTREMARK"];
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
        $query .= "   AND SEMESTER = '".$semester."'";//NO001
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

}
?>
